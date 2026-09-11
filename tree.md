# 树结构

很多业务数据天然是树形的：组织机构、菜单、地区、商品分类……这类表通常有一列指向父记录的外键（PID），用一条普通的平铺查询就能把整棵树取出来。rainbow-dbaccess 提供了 `queryForTree` 和 `queryForWrapTree` 两个链式方法，在查询结束后自动根据 ID/PID 关系把平铺的行组装成树，省去手工递归组装的样板代码。

## queryForTree：实体直接挂树

结果集必须包含 `ID` 和 `PID` 两列——这是组装父子关系的依据。如果实际列名不同，用 `AS` 别名对齐即可：

```java
Tree<OrgNode> tree = dba.select().from("T_ORG").orderBy("ID")
    .queryForTree(OrgNode.class);

dba.select("ORG_ID AS ID", "PARENT_ID AS PID", "NAME").from("T_ORG")
    .queryForTree(OrgNode.class); // 列名不同时用别名
```

实体类实现 `ITreeNode` 接口：

```java
public class OrgNode implements ITreeNode<OrgNode> {
    private String id;
    private String pid;
    private String name;
    @Transient
    private List<OrgNode> children = new ArrayList<>();

    // 实现 getChildren() / setChildren()
    // addChild() 有默认实现，children 为 null 时会自动创建列表
}
```

最省事的方式是继承 `TreeNode` 基类，children 相关代码它已经写好了：

```java
public class OrgNode extends TreeNode<OrgNode> {
    private String id;
    private String pid;
    private String name;
}
```

组装规则很简单：遍历结果集，每行的 `PID` 在结果集中能找到对应记录就挂为它的子节点，找不到（包括 PID 为 null 的顶级记录）就作为根节点。所以：

- 想取整棵树就别加 WHERE 限制到某一行，否则父链断掉的子树会变成新的"根"
- `orderBy` 决定兄弟节点的排列顺序，通常应该带上
- 结果集为空时返回的 `Tree` 的 `getRoots()` 是空列表，不会抛异常

## Tree 容器

`queryForTree` 返回 `Tree<T>`，除了树形结构本身，还附带一张 ID 到节点的映射表，任意节点 O(1) 直达：

```java
List<OrgNode> roots = tree.getRoots();   // 根节点列表
OrgNode node = tree.getNode("2");        // 按 ID 快速查找任意节点
Map<String, OrgNode> all = tree.getNodeMap(); // 全部节点的映射表
```

## queryForWrapTree：包裹为 TreeObject

`ITreeNode` 要求实体类持有 children 列表，会污染实体模型。`queryForWrapTree` 提供另一种思路：实体类保持纯净的平铺结构，由框架把每一行包裹进 `TreeObject<T>` 再组装成树。

```java
Tree<TreeObject<OrgNode>> tree =
    dba.select().from("T_ORG").orderBy("ID").queryForWrapTree(OrgNode.class);

TreeObject<OrgNode> root = tree.getRoots().get(0);
root.getData().getName();            // 原始实体在 getData() 里
root.getParent();                    // 可以向上拿到父节点
root.getChildren();                  // 子节点列表来自 TreeNode 基类
```

`TreeObject` 继承自 `TreeNode`，天然具备 children 管理；`addChild` 时会自动回填子节点的 `parent` 引用，所以它是双向的——既能向下遍历也能向上回溯，这在"选中某个节点后要反查祖先链"的场景里特别好用。

注意：因为存在 `parent` 回溯引用，`TreeObject` 树**不适合直接序列化返回给前端**——双向引用会让 JSON 序列化陷入无限循环（或依赖 `@JsonBackReference` 之类的注解才能脱险）。返回前端时用 `getData()` 取出实体，或用 `TreeUtils.transform` 转成不含 parent 的 DTO 再输出。

两种方式的取舍：

| 方式 | 实体类要求 | 父节点引用 | 适用场景 |
|------|-----------|-----------|---------|
| `queryForTree` | 实现 `ITreeNode` 或继承 `TreeNode` | 无 | 实体本身就要作为树节点返回给前端 |
| `queryForWrapTree` | 纯平铺实体即可 | `getParent()` | 不想污染实体模型，或需要向上回溯 |

也可以用 `queryForTree(RowMapper)` 传入自定义 RowMapper，配合自己实现的 `ITreeNode` 类型完成行映射，用法与 `queryForList(RowMapper)` 一致。

## TreeUtils：树上的常用操作

查询拿到树之后，遍历、过滤、转换、打印这些高频操作由 `TreeUtils` 提供：

```java
// 前序遍历，只处理节点
TreeUtils.traverse(roots, node -> System.out.println(node.getName()));

// 带 parent 和 level（根为 1）的遍历，第二个参数控制前序/后序
TreeUtils.traverse(roots, (node, parent, level) -> {
    System.out.println("  ".repeat(level - 1) + node.getName());
}, true);

// 过滤：命中节点的子树是否保留由 recurseOnMatch 控制
List<OrgNode> filtered = TreeUtils.filter(roots, node -> node.isActive(), true);

// 转换：整棵树映射为另一种节点类型（如 DTO）
List<OrgNodeDTO> dtos = TreeUtils.transform(roots, node -> toDto(node));

// 打印成缩进树形字符串，方便日志调试
String print = TreeUtils.printTree(roots, OrgNode::getName);
```

一个需要注意的坑：`filter` 会直接修改节点的 children 列表来剪枝，**原有的树结构会被破坏**。如果过滤之后还要继续使用原来的树，先用 `transform` 复制一份再过滤，或者直接在 `TreeObject` 包裹的树上操作。
