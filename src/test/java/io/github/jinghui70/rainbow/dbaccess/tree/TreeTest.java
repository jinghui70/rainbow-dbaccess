package io.github.jinghui70.rainbow.dbaccess.tree;

import cn.hutool.core.util.IdUtil;
import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaConfig;
import io.github.jinghui70.rainbow.utils.WrapTreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaConfig.initTable(dba, "TREE_OBJECT");

        TreeObject root1 = new TreeObject();
        root1.setId("root1");
        root1.setCode("1");
        root1.setName("ZZZ");
        dba.insert(root1);

        TreeObject treeObject = new TreeObject();
        root1.setId(IdUtil.fastSimpleUUID());
        root1.setPid("root1");
        root1.setCode("11");
        root1.setName("AAA");
        dba.insert(root1);

        String root2Id = "root2";
        treeObject = new TreeObject();
        treeObject.setId(root2Id);
        treeObject.setPid("0");
        treeObject.setCode("2");
        treeObject.setName("HHH");
        dba.insert(treeObject);

        treeObject = new TreeObject();
        treeObject.setId(IdUtil.fastSimpleUUID());
        treeObject.setPid(root2Id);
        treeObject.setCode("21");
        treeObject.setName("XXX");
        dba.insert(treeObject);

        treeObject = new TreeObject();
        treeObject.setId(IdUtil.fastSimpleUUID());
        treeObject.setPid(root2Id);
        treeObject.setCode("22");
        treeObject.setName("DDD");
        dba.insert(treeObject);

        treeObject = new TreeObject();
        treeObject.setId(IdUtil.fastSimpleUUID());
        treeObject.setPid(root2Id);
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
    void testWrapOrder() {
        List<WrapTreeNode<TreeObject>> tree = dba.select(TreeObject.class).orderBy("NAME").queryForWrapTree();
        assertEquals(2, tree.size());
        assertEquals("HHH", tree.get(0).getData().getName());
        assertEquals("ZZZ", tree.get(1).getData().getName());

        WrapTreeNode<TreeObject> treeNode = tree.get(0);
        assertEquals(3, treeNode.getChildren().size());
        assertEquals("CCC", treeNode.getChildren().get(0).getData().getName());
        assertEquals("DDD", treeNode.getChildren().get(1).getData().getName());
        assertEquals("XXX", treeNode.getChildren().get(2).getData().getName());

        tree = dba.select(TreeObject.class).orderBy("CODE").queryForWrapTree();
        assertEquals("ZZZ", tree.get(0).getData().getName());
        assertEquals("HHH", tree.get(1).getData().getName());

        treeNode = tree.get(1);
        assertEquals(3, treeNode.getChildren().size());
        assertEquals("XXX", treeNode.getChildren().get(0).getData().getName());
        assertEquals("DDD", treeNode.getChildren().get(1).getData().getName());
        assertEquals("CCC", treeNode.getChildren().get(2).getData().getName());
    }

}
