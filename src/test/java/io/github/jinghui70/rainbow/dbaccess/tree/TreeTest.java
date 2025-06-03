package io.github.jinghui70.rainbow.dbaccess.tree;

import cn.hutool.core.util.IdUtil;
import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaTestUtil;
import io.github.jinghui70.rainbow.utils.tree.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaTestUtil.initTable(dba, "TREE_ORG");

        TreeOrg root1 = new TreeOrg();
        root1.setId("root1");
        root1.setCode("1");
        root1.setName("ZZZ");
        dba.insert(root1);

        root1.setId(IdUtil.fastSimpleUUID());
        root1.setPid("root1");
        root1.setCode("11");
        root1.setName("AAA");
        dba.insert(root1);

        TreeOrg root2 = new TreeOrg();
        root2.setId("root2");
        root2.setPid("0");
        root2.setCode("2");
        root2.setName("HHH");
        dba.insert(root2);

        TreeOrg treeOrg = new TreeOrg();
        treeOrg.setId(IdUtil.fastSimpleUUID());
        treeOrg.setPid(root2.getId());
        treeOrg.setCode("21");
        treeOrg.setName("XXX");
        dba.insert(treeOrg);

        treeOrg = new TreeOrg();
        treeOrg.setId(IdUtil.fastSimpleUUID());
        treeOrg.setPid(root2.getId());
        treeOrg.setCode("22");
        treeOrg.setName("DDD");
        dba.insert(treeOrg);

        treeOrg = new TreeOrg();
        treeOrg.setId(IdUtil.fastSimpleUUID());
        treeOrg.setPid(root2.getId());
        treeOrg.setCode("23");
        treeOrg.setName("CCC");
        dba.insert(treeOrg);
    }

    @Test
    void testOrder() {
        List<TreeOrg> list = dba.select(TreeOrg.class).orderBy("NAME").queryForList();
        assertEquals(6, list.size());
        assertEquals("AAA", list.get(0).getName());
        assertEquals("CCC", list.get(1).getName());
        assertEquals("DDD", list.get(2).getName());
        assertEquals("HHH", list.get(3).getName());
        assertEquals("XXX", list.get(4).getName());
        assertEquals("ZZZ", list.get(5).getName());

        Tree<TreeOrg> tree = dba.select().from("TREE_ORG").orderBy("NAME").queryForTree(TreeOrg.class);
        String treeInfo = TreeUtils.printTree(tree.getRoots(), TreeOrg::getName);
        System.out.println(treeInfo);
        assertEquals(2, tree.getRoots().size());
        assertEquals("HHH", tree.getRoots().get(0).getName());
        assertEquals("ZZZ", tree.getRoots().get(1).getName());

        TreeOrg org = tree.getRoots().get(0);
        assertEquals(3, org.getChildren().size());
        assertEquals("CCC", org.getChildren().get(0).getName());
        assertEquals("DDD", org.getChildren().get(1).getName());
        assertEquals("XXX", org.getChildren().get(2).getName());

        tree = dba.select().from("TREE_ORG").orderBy("CODE").queryForTree(TreeOrg.class);
        assertEquals("ZZZ", tree.getRoots().get(0).getName());
        assertEquals("HHH", tree.getRoots().get(1).getName());

        org = tree.getRoots().get(1);
        assertEquals(3, org.getChildren().size());
        assertEquals("XXX", org.getChildren().get(0).getName());
        assertEquals("DDD", org.getChildren().get(1).getName());
        assertEquals("CCC", org.getChildren().get(2).getName());
    }

    @Test
    public void testTraverse() {
        List<TreeOrg> result = new ArrayList<>();
        Tree<TreeOrg> tree = dba.select().from("TREE_ORG").orderBy("CODE").queryForTree(TreeOrg.class);
        TreeUtils.traverse(tree.getRoots(), result::add, false);

        assertEquals(6, result.size());
        assertEquals("AAA", result.get(0).getName());
        assertEquals("ZZZ", result.get(1).getName());
        assertEquals("XXX", result.get(2).getName());
        assertEquals("DDD", result.get(3).getName());
        assertEquals("CCC", result.get(4).getName());
        assertEquals("HHH", result.get(5).getName());
    }

    @Test
    public void testFilter() {
        Tree<TreeOrg> tree = dba.select().from("TREE_ORG").queryForTree(TreeOrg.class);
        System.out.println(TreeUtils.printTree(tree.getRoots(), TreeOrg::getName));
        // 测试基本过滤功能
        List<TreeOrg> filtered = TreeUtils.filter(tree.getRoots(), node -> node.getName().equals("HHH"), false);
        assertTreeStructure(tree.getRoots(),
                "├── ZZZ\n" +
                        "│   └── AAA\n" +
                        "└── HHH\n" +
                        "    ├── XXX\n" +
                        "    ├── DDD\n" +
                        "    └── CCC\n");
        assertTreeStructure(filtered,
                "└── HHH\n" +
                        "    ├── XXX\n" +
                        "    ├── DDD\n" +
                        "    └── CCC\n");
        // 测试递归过滤功能
        filtered = TreeUtils.filter(tree.getRoots(), node -> node.getName().equals("HHH"), true);
        assertTreeStructure(tree.getRoots(),
                "├── ZZZ\n" +
                        "│   └── AAA\n" +
                        "└── HHH\n");
        assertTreeStructure(filtered,
                "└── HHH\n");
        // 测试空树情况
        List<TreeOrg> emptyList = new ArrayList<>();
        filtered = TreeUtils.filter(emptyList, node -> true, false);
        assertEquals(0, filtered.size());

        // 测试无匹配情况
        filtered = TreeUtils.filter(tree.getRoots(), node -> false, false);
        assertEquals(0, filtered.size());
    }

    @Test
    public void testFilterWrap() {
        WrapTree<TreeOrg> tree = dba.select().from("TREE_ORG").queryForWrapTree(TreeOrg.class);

        // 测试基本过滤功能
        List<TreeObject<TreeOrg>> filtered = TreeUtils.filter(tree.getRoots(), node -> node.getData().getName().equals("HHH"), false);
        assertEquals(1, filtered.size());
        assertEquals("HHH", filtered.get(0).getData().getName());
        assertEquals(3, filtered.get(0).getChildren().size()); // 不递归过滤子节点

        assertTreeStructure(tree.getRoots(),
                "├── ZZZ\n" +
                        "│   └── AAA\n" +
                        "└── HHH\n" +
                        "    ├── XXX\n" +
                        "    ├── DDD\n" +
                        "    └── CCC\n");
        assertTreeStructure(filtered,
                "└── HHH\n" +
                        "    ├── XXX\n" +
                        "    ├── DDD\n" +
                        "    └── CCC\n");
        // 测试递归过滤功能
        filtered = TreeUtils.filter(tree.getRoots(), node -> node.getData().getName().equals("HHH"), true);
        assertTreeStructure(tree.getRoots(),
                "├── ZZZ\n" +
                        "│   └── AAA\n" +
                        "└── HHH\n" +
                        "    ├── XXX\n" +
                        "    ├── DDD\n" +
                        "    └── CCC\n");
        assertTreeStructure(filtered,
                "└── HHH\n");

        // 测试空树情况
        List<TreeObject<TreeOrg>> emptyList = new ArrayList<>();
        filtered = TreeUtils.filter(emptyList, node -> true, false);
        assertEquals(0, filtered.size());

        // 测试无匹配情况
        filtered = TreeUtils.filter(tree.getRoots(), node -> false, false);
        assertEquals(0, filtered.size());
    }

    private <T extends ITreeNode<T>> void assertTreeStructure(List<T> nodes, String expect) {
        assertEquals(expect, TreeUtils.printTree(nodes));
    }

}
