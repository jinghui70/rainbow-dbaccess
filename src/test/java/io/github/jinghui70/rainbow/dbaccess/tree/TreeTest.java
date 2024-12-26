package io.github.jinghui70.rainbow.dbaccess.tree;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaTestUtil;
import io.github.jinghui70.rainbow.utils.tree.FilterType;
import io.github.jinghui70.rainbow.utils.tree.Tree;
import io.github.jinghui70.rainbow.utils.tree.TreeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TreeTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaTestUtil.initTable(dba, "TREE_OBJECT");

        TreeObject root1 = new TreeObject();
        root1.setId("root1");
        root1.setCode("1");
        root1.setName("ZZZ");
        dba.insert(root1);

        root1.setId(IdUtil.fastSimpleUUID());
        root1.setPid("root1");
        root1.setCode("11");
        root1.setName("AAA");
        dba.insert(root1);

        TreeObject root2 = new TreeObject();
        root2.setId("root2");
        root2.setPid("0");
        root2.setCode("2");
        root2.setName("HHH");
        dba.insert(root2);

        TreeObject treeObject = new TreeObject();
        treeObject.setId(IdUtil.fastSimpleUUID());
        treeObject.setPid(root2.getId());
        treeObject.setCode("21");
        treeObject.setName("XXX");
        dba.insert(treeObject);

        treeObject = new TreeObject();
        treeObject.setId(IdUtil.fastSimpleUUID());
        treeObject.setPid(root2.getId());
        treeObject.setCode("22");
        treeObject.setName("DDD");
        dba.insert(treeObject);

        treeObject = new TreeObject();
        treeObject.setId(IdUtil.fastSimpleUUID());
        treeObject.setPid(root2.getId());
        treeObject.setCode("23");
        treeObject.setName("CCC");
        dba.insert(treeObject);
    }

    @Test
    void testOrder() {
        List<TreeObject> list = dba.select(TreeObject.class).orderBy("NAME").queryForList();
        assertEquals(6, list.size());
        assertEquals("AAA", list.get(0).getName());
        assertEquals("CCC", list.get(1).getName());
        assertEquals("DDD", list.get(2).getName());
        assertEquals("HHH", list.get(3).getName());
        assertEquals("XXX", list.get(4).getName());
        assertEquals("ZZZ", list.get(5).getName());

        Tree<TreeObject> tree = dba.select(TreeObject.class).orderBy("NAME").queryForTree();
        assertEquals(2, tree.getRoots().size());
        assertEquals("HHH", tree.getRoots().get(0).getName());
        assertEquals("ZZZ", tree.getRoots().get(1).getName());

        TreeObject treeObject = tree.getRoot(0);
        assertEquals(3, treeObject.getChildren().size());
        assertEquals("CCC", treeObject.getChildren().get(0).getName());
        assertEquals("DDD", treeObject.getChildren().get(1).getName());
        assertEquals("XXX", treeObject.getChildren().get(2).getName());

        tree = dba.select(TreeObject.class).orderBy("CODE").queryForTree();
        assertEquals("ZZZ", tree.getRoots().get(0).getName());
        assertEquals("HHH", tree.getRoots().get(1).getName());

        treeObject = tree.getRoot(1);
        assertEquals(3, treeObject.getChildren().size());
        assertEquals("XXX", treeObject.getChildren().get(0).getName());
        assertEquals("DDD", treeObject.getChildren().get(1).getName());
        assertEquals("CCC", treeObject.getChildren().get(2).getName());
    }

    @Test
    public void testTraverse() {
        List<TreeObject> result = new ArrayList<>();
        Tree<TreeObject> tree = dba.select(TreeObject.class).orderBy("CODE").queryForTree();
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
        String pid = dba.select("ID").from(TreeObject.class).where("CODE", "22")
                .queryForString();

        TreeObject treeObject = new TreeObject();
        treeObject.setId(IdUtil.fastSimpleUUID());
        treeObject.setPid(pid);
        treeObject.setCode("221");
        treeObject.setName("221");
        dba.insert(treeObject);
        treeObject = new TreeObject();
        treeObject.setId(IdUtil.fastSimpleUUID());
        treeObject.setPid(pid);
        treeObject.setCode("222");
        treeObject.setName("222");
        dba.insert(treeObject);

        Tree<TreeObjectClonable> tree = dba.select().from("TREE_OBJECT").orderBy("CODE")
                .queryForTree(TreeObjectClonable.class);
        List<TreeObjectClonable> filteredTree = TreeUtils.filter(tree.getRoots(), o -> "22".equals(o.getCode()), FilterType.MATCH_FIRST);
        assertEquals(1, filteredTree.size()); // 2
        TreeObjectClonable node = filteredTree.get(0);
        assertEquals("2", node.getCode());
        assertEquals(1, node.getChildren().size()); // 22
        node = node.getChildren().get(0);
        assertEquals("22", node.getCode());
        assertTrue(CollUtil.isEmpty(node.getChildren()));

        // 因为是 clone 版，不应该改变原树的数据
        node = tree.getRoot(1).getChildren().get(1);
        assertEquals("22", node.getCode());
        assertEquals(2, node.getChildren().size());

        filteredTree = TreeUtils.filter(tree.getRoots(), o -> "22".equals(o.getCode()), FilterType.MATCH_FIRST_FULL);
        node = filteredTree.get(0).getChildren().get(0);
        assertEquals("22", node.getCode());
        assertEquals(2, node.getChildren().size());

        filteredTree = TreeUtils.filter(tree.getRoots(), o -> o.getCode().startsWith("2") && o.getCode().length() == 2, FilterType.MATCH_ALL);
        node = filteredTree.get(0);
        assertEquals(3, node.getChildren().size());
        node = node.getChildren().get(1);
        assertEquals("22", node.getCode());
        assertTrue(CollUtil.isEmpty(node.getChildren()));

        filteredTree = TreeUtils.filter(tree.getRoots(), o -> o.getCode().startsWith("2") && o.getCode().length() == 2, FilterType.MATCH_ALL_FULL);
        node = filteredTree.get(0);
        assertEquals(3, node.getChildren().size());
        node = node.getChildren().get(1);
        assertEquals("22", node.getCode());
        assertEquals(2, node.getChildren().size());
    }

}
