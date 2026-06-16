package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.model.OrgNode;
import io.github.jinghui70.rainbow.dbaccess.tree.Tree;
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
}
