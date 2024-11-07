package io.github.jinghui70.rainbow.dbaccess.tree;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaConfig;
import io.github.jinghui70.rainbow.utils.tree.FilterType;
import io.github.jinghui70.rainbow.utils.tree.TreeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TreeTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaConfig.initTable(dba, "TREE_OBJECT");

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

        List<TreeObject> tree = dba.select(TreeObject.class).orderBy("NAME").queryForTree();
        assertEquals(2, tree.size());
        assertEquals("HHH", tree.get(0).getName());
        assertEquals("ZZZ", tree.get(1).getName());

        TreeObject treeObject = tree.get(0);
        assertEquals(3, treeObject.getChildren().size());
        assertEquals("CCC", treeObject.getChildren().get(0).getName());
        assertEquals("DDD", treeObject.getChildren().get(1).getName());
        assertEquals("XXX", treeObject.getChildren().get(2).getName());

        tree = dba.select(TreeObject.class).orderBy("CODE").queryForTree();
        assertEquals("ZZZ", tree.get(0).getName());
        assertEquals("HHH", tree.get(1).getName());

        treeObject = tree.get(1);
        assertEquals(3, treeObject.getChildren().size());
        assertEquals("XXX", treeObject.getChildren().get(0).getName());
        assertEquals("DDD", treeObject.getChildren().get(1).getName());
        assertEquals("CCC", treeObject.getChildren().get(2).getName());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testMapOrder() {
        List<Map<String, Object>> list = dba.selectAll().from("TREE_OBJECT").orderBy("NAME").queryForList();
        assertEquals(6, list.size());
        assertEquals("AAA", list.get(0).get("Name"));
        assertEquals("CCC", list.get(1).get("Name"));
        assertEquals("DDD", list.get(2).get("Name"));
        assertEquals("HHH", list.get(3).get("Name"));
        assertEquals("XXX", list.get(4).get("Name"));
        assertEquals("ZZZ", list.get(5).get("Name"));

        List<Map<String, Object>> tree = dba.selectAll().from("TREE_OBJECT").orderBy("NAME").queryForTree();
        assertEquals(2, tree.size());
        assertEquals("HHH", tree.get(0).get("Name"));
        assertEquals("ZZZ", tree.get(1).get("Name"));

        Map<String, Object> treeObject = tree.get(0);
        List<Map<String, Object>> children = (List<Map<String, Object>>) treeObject.get("Children");
        assertEquals(3, children.size());
        assertEquals("CCC", children.get(0).get("Name"));
        assertEquals("DDD", children.get(1).get("Name"));
        assertEquals("XXX", children.get(2).get("Name"));

        tree = dba.selectAll().from("TREE_OBJECT").orderBy("CODE").queryForTree();
        assertEquals("ZZZ", tree.get(0).get("Name"));
        assertEquals("HHH", tree.get(1).get("Name"));

        treeObject = tree.get(1);
        children = (List<Map<String, Object>>) treeObject.get("Children");
        assertEquals(3, children.size());
        assertEquals("XXX", children.get(0).get("Name"));
        assertEquals("DDD", children.get(1).get("Name"));
        assertEquals("CCC", children.get(2).get("Name"));

    }

    @Test
    public void testTraverse() {
        List<TreeObject> result = new ArrayList<>();
        List<TreeObject> tree = dba.select(TreeObject.class).orderBy("CODE").queryForTree();
        TreeUtils.traverse(tree, result::add);

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

        List<TreeObjectClonable> tree = dba.selectAll().from("TREE_OBJECT").orderBy("CODE")
                .queryForTree(TreeObjectClonable.class);
        List<TreeObjectClonable> filteredTree = TreeUtils.filter(tree, o -> "22".equals(o.getCode()), FilterType.MATCH_FIRST);
        assertEquals(1, filteredTree.size()); // 2
        TreeObjectClonable node = filteredTree.get(0);
        assertEquals("2", node.getCode());
        assertEquals(1, node.getChildren().size()); // 22
        node = node.getChildren().get(0);
        assertEquals("22", node.getCode());
        assertTrue(CollUtil.isEmpty(node.getChildren()));

        // 因为是 clone 版，不应该改变原树的数据
        node = tree.get(1).getChildren().get(1);
        assertEquals("22", node.getCode());
        assertEquals(2, node.getChildren().size());

        filteredTree = TreeUtils.filter(tree, o -> "22".equals(o.getCode()), FilterType.MATCH_FIRST_FULL);
        node = filteredTree.get(0).getChildren().get(0);
        assertEquals("22", node.getCode());
        assertEquals(2, node.getChildren().size());

        filteredTree = TreeUtils.filter(tree, o -> o.getCode().startsWith("2") && o.getCode().length() == 2, FilterType.MATCH_ALL);
        node = filteredTree.get(0);
        assertEquals(3, node.getChildren().size());
        node = node.getChildren().get(1);
        assertEquals("22", node.getCode());
        assertTrue(CollUtil.isEmpty(node.getChildren()));

        filteredTree = TreeUtils.filter(tree, o -> o.getCode().startsWith("2") && o.getCode().length() == 2, FilterType.MATCH_ALL_FULL);
        node = filteredTree.get(0);
        assertEquals(3, node.getChildren().size());
        node = node.getChildren().get(1);
        assertEquals("22", node.getCode());
        assertEquals(2, node.getChildren().size());
    }

}
