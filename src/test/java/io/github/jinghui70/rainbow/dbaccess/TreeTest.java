package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.model.OrgNode;
import io.github.jinghui70.rainbow.dbaccess.tree.Tree;
import io.github.jinghui70.rainbow.dbaccess.tree.TreeObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 树查询测试 — queryForTree / queryForWrapTree。
 */
class TreeTest extends BaseTest {

    @Test void testQueryForTree() {
        createOrgTable();
        dba.insert(new OrgNode[]{
                new OrgNode("1", null, "Root", "R001"),
                new OrgNode("2", "1", "Child1", "C001"),
                new OrgNode("3", "1", "Child2", "C002"),
                new OrgNode("4", "2", "Grandchild", "G001")
        });
        Tree<OrgNode> tree = dba.select().from("T_ORG").orderBy("ID").queryForTree(OrgNode.class);
        List<OrgNode> roots = tree.getRoots();
        assertEquals(1, roots.size());
        OrgNode root = roots.get(0);
        assertEquals("1", root.getId());
        assertEquals(2, root.getChildren().size());
        assertEquals(1, root.getChildren().get(0).getChildren().size());
    }

    @Test void testQueryForTreeEmpty() {
        createOrgTable();
        Tree<OrgNode> tree = dba.select().from("T_ORG").queryForTree(OrgNode.class);
        assertTrue(tree.getRoots().isEmpty());
    }

    @Test void testQueryForWrapTree() {
        createOrgTable();
        dba.insert(new OrgNode[]{
                new OrgNode("1", null, "Root", "R001"),
                new OrgNode("2", "1", "Child1", "C001"),
                new OrgNode("3", "1", "Child2", "C002"),
                new OrgNode("4", "2", "Grandchild", "G001")
        });
        Tree<TreeObject<OrgNode>> tree =
                dba.select().from("T_ORG").orderBy("ID").queryForWrapTree(OrgNode.class);
        List<TreeObject<OrgNode>> roots = tree.getRoots();
        assertEquals(1, roots.size());

        TreeObject<OrgNode> root = roots.get(0);
        assertNull(root.getParent());
        assertEquals("1", root.getData().getId());
        assertEquals("Root", root.getData().getName());

        List<TreeObject<OrgNode>> children = root.getChildren();
        assertEquals(2, children.size());
        assertEquals("2", children.get(0).getData().getId());
        assertEquals("3", children.get(1).getData().getId());
        assertSame(root, children.get(0).getParent());

        TreeObject<OrgNode> grandchild = children.get(0).getChildren().get(0);
        assertEquals("4", grandchild.getData().getId());
        assertSame(children.get(0), grandchild.getParent());
        assertNull(grandchild.getChildren());
    }

    @Test void testQueryForWrapTreeEmpty() {
        createOrgTable();
        Tree<TreeObject<OrgNode>> tree =
                dba.select().from("T_ORG").queryForWrapTree(OrgNode.class);
        assertTrue(tree.getRoots().isEmpty());
    }
}
