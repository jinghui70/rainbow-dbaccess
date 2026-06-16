package io.github.jinghui70.rainbow.dbaccess.tree;

import io.github.jinghui70.rainbow.dbaccess.model.OrgNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TreeUtilsTest {

    private List<OrgNode> tree;

    @BeforeEach
    void setUp() {
        OrgNode root = new OrgNode("1", null, "Root", "R001");
        OrgNode child1 = new OrgNode("2", "1", "Child1", "C001");
        OrgNode child2 = new OrgNode("3", "1", "Child2", "C002");
        OrgNode grandchild1 = new OrgNode("4", "2", "Grandchild1", "G001");
        OrgNode grandchild2 = new OrgNode("5", "2", "Grandchild2", "G002");
        root.addChild(child1);
        root.addChild(child2);
        child1.addChild(grandchild1);
        child1.addChild(grandchild2);
        tree = new ArrayList<>();
        tree.add(root);
    }

    private List<OrgNode> buildMultiRootTree() {
        OrgNode root1 = new OrgNode("1", null, "Root1", "R001");
        OrgNode root2 = new OrgNode("2", null, "Root2", "R002");
        OrgNode child1 = new OrgNode("3", "1", "Child1", "C001");
        OrgNode child2 = new OrgNode("4", "2", "Child2", "C002");
        root1.addChild(child1);
        root2.addChild(child2);
        List<OrgNode> roots = new ArrayList<>();
        roots.add(root1);
        roots.add(root2);
        return roots;
    }

    @Test
    void traversePreOrder() {
        List<String> names = new ArrayList<>();
        TreeUtils.traverse(tree, node -> names.add(node.getName()));
        assertEquals(List.of("Root", "Child1", "Grandchild1", "Grandchild2", "Child2"), names);
    }

    @Test
    void traversePostOrder() {
        List<String> names = new ArrayList<>();
        TreeUtils.traverse(tree, node -> names.add(node.getName()), false);
        assertEquals(List.of("Grandchild1", "Grandchild2", "Child1", "Child2", "Root"), names);
    }

    @Test
    void traverseEmptyList() {
        List<String> names = new ArrayList<>();
        List<OrgNode> emptyList = Collections.emptyList();
        TreeUtils.traverse(emptyList, node -> names.add(node.getName()));
        assertTrue(names.isEmpty());
    }

    @Test
    void traverseNullNode() {
        List<String> names = new ArrayList<>();
        TreeUtils.traverse((OrgNode) null, node -> names.add(node.getName()), true);
        assertTrue(names.isEmpty());
    }

    @Test
    void traverseSingleNodePreOrder() {
        OrgNode single = new OrgNode("1", null, "Single", "S001");
        List<String> names = new ArrayList<>();
        TreeUtils.traverse(single, node -> names.add(node.getName()), true);
        assertEquals(List.of("Single"), names);
    }

    @Test
    void traverseSingleNodePostOrder() {
        OrgNode single = new OrgNode("1", null, "Single", "S001");
        List<String> names = new ArrayList<>();
        TreeUtils.traverse(single, node -> names.add(node.getName()), false);
        assertEquals(List.of("Single"), names);
    }

    @Test
    void traverseMultiRootPreOrder() {
        List<String> names = new ArrayList<>();
        TreeUtils.traverse(buildMultiRootTree(), node -> names.add(node.getName()));
        assertEquals(List.of("Root1", "Child1", "Root2", "Child2"), names);
    }

    @Test
    void traverseWithTreeNodeConsumerPreOrder() {
        List<String> names = new ArrayList<>();
        List<Integer> levels = new ArrayList<>();
        TreeUtils.traverse(tree, (node, parent, level) -> {
            names.add(node.getName());
            levels.add(level);
        });
        assertEquals(List.of("Root", "Child1", "Grandchild1", "Grandchild2", "Child2"), names);
        assertEquals(List.of(1, 2, 3, 3, 2), levels);
    }

    @Test
    void traverseWithTreeNodeConsumerPostOrder() {
        List<String> names = new ArrayList<>();
        TreeUtils.traverse(tree, (node, parent, level) -> names.add(node.getName()), false);
        assertEquals(List.of("Grandchild1", "Grandchild2", "Child1", "Child2", "Root"), names);
    }

    @Test
    void traverseWithTreeNodeConsumerParentAndLevel() {
        List<String> parentNames = new ArrayList<>();
        List<Integer> levels = new ArrayList<>();
        TreeUtils.traverse(tree, (node, parent, level) -> {
            parentNames.add(parent == null ? "null" : parent.getName());
            levels.add(level);
        });
        assertEquals(List.of("null", "Root", "Child1", "Child1", "Root"), parentNames);
        assertEquals(List.of(1, 2, 3, 3, 2), levels);
    }

    @Test
    void traverseNodeDirectly() {
        OrgNode root = tree.get(0);
        List<String> names = new ArrayList<>();
        TreeUtils.traverseNode(root, null, 1,
                (node, parent, level) -> names.add(node.getName()), true);
        assertEquals(List.of("Root", "Child1", "Grandchild1", "Grandchild2", "Child2"), names);
    }

    @Test
    void traverseNodePostOrder() {
        OrgNode root = tree.get(0);
        List<String> names = new ArrayList<>();
        TreeUtils.traverseNode(root, null, 1, (node, parent, level) -> names.add(node.getName()), false);
        assertEquals(List.of("Grandchild1", "Grandchild2", "Child1", "Child2", "Root"), names);
    }

    @Test
    void transformTree() {
        List<SimpleNode> result = TreeUtils.transform(tree, org -> {
            SimpleNode sn = new SimpleNode();
            sn.setName(org.getName());
            return sn;
        });
        assertEquals(1, result.size());
        assertEquals("Root", result.get(0).getName());
        assertEquals(2, result.get(0).getChildren().size());
        assertEquals("Child1", result.get(0).getChildren().get(0).getName());
        assertEquals("Child2", result.get(0).getChildren().get(1).getName());
        assertEquals(2, result.get(0).getChildren().get(0).getChildren().size());
        assertEquals("Grandchild1", result.get(0).getChildren().get(0).getChildren().get(0).getName());
        assertEquals("Grandchild2", result.get(0).getChildren().get(0).getChildren().get(1).getName());
    }

    @Test
    void transformEmptyList() {
        List<OrgNode> emptyOrgList = Collections.emptyList();
        List<SimpleNode> result = TreeUtils.transform(emptyOrgList, org -> new SimpleNode());
        assertTrue(result.isEmpty());
    }

    @Test
    void transformLeafNode() {
        OrgNode leaf = new OrgNode("1", null, "Leaf", "L001");
        List<OrgNode> leafTree = new ArrayList<>();
        leafTree.add(leaf);
        List<SimpleNode> result = TreeUtils.transform(leafTree, org -> {
            SimpleNode sn = new SimpleNode();
            sn.setName(org.getName());
            return sn;
        });
        assertEquals(1, result.size());
        assertEquals("Leaf", result.get(0).getName());
        assertNull(result.get(0).getChildren());
    }

    @Test
    void filterRecurseOnMatch() {
        List<OrgNode> result = TreeUtils.filter(tree,
                node -> node.getName().contains("Child1"), true);
        assertEquals(1, result.size());
        assertEquals("Root", result.get(0).getName());
        OrgNode filteredChild1 = result.get(0).getChildren().get(0);
        assertEquals("Child1", filteredChild1.getName());
        assertNull(filteredChild1.getChildren());
    }

    @Test
    void filterNoRecurseOnMatch() {
        List<OrgNode> result = TreeUtils.filter(tree,
                node -> node.getName().contains("Child1"), false);
        assertEquals(1, result.size());
        assertEquals("Root", result.get(0).getName());
        OrgNode filteredChild1 = result.get(0).getChildren().get(0);
        assertEquals("Child1", filteredChild1.getName());
        assertEquals(2, filteredChild1.getChildren().size());
    }

    @Test
    void filterNoMatch() {
        List<OrgNode> result = TreeUtils.filter(tree,
                node -> node.getName().equals("NonExistent"), true);
        assertTrue(result.isEmpty());
    }

    @Test
    void filterAllMatch() {
        List<OrgNode> result = TreeUtils.filter(tree, node -> true, true);
        assertEquals(1, result.size());
        assertEquals("Root", result.get(0).getName());
        assertEquals(2, result.get(0).getChildren().size());
    }

    @Test
    void filterKeepsAncestorOfMatchingNode() {
        List<OrgNode> result = TreeUtils.filter(tree,
                node -> node.getName().equals("Grandchild1"), true);
        assertEquals(1, result.size());
        assertEquals("Root", result.get(0).getName());
        assertEquals(1, result.get(0).getChildren().size());
        assertEquals("Child1", result.get(0).getChildren().get(0).getName());
        assertEquals(1, result.get(0).getChildren().get(0).getChildren().size());
        assertEquals("Grandchild1", result.get(0).getChildren().get(0).getChildren().get(0).getName());
    }

    @Test
    void filterEmptyList() {
        List<OrgNode> emptyFilterList = Collections.emptyList();
        List<OrgNode> result = TreeUtils.filter(emptyFilterList, node -> true, false);
        assertTrue(result.isEmpty());
    }

    @Test
    void filterNodeMatching() {
        OrgNode root = tree.get(0);
        OrgNode result = TreeUtils.filterNode(root, node -> node.getName().equals("Root"), true);
        assertNotNull(result);
        assertEquals("Root", result.getName());
        assertNull(result.getChildren());
    }

    @Test
    void filterNodeNoMatch() {
        OrgNode root = tree.get(0);
        OrgNode result = TreeUtils.filterNode(root, node -> false, true);
        assertNull(result);
    }

    @Test
    void filterNodeDescendantMatch() {
        OrgNode root = tree.get(0);
        OrgNode result = TreeUtils.filterNode(root, node -> node.getName().equals("Grandchild1"), true);
        assertNotNull(result);
        assertEquals("Root", result.getName());
    }

    @Test
    void filterLeafNodeNoMatch() {
        OrgNode leaf = new OrgNode("1", null, "Leaf", "L001");
        OrgNode result = TreeUtils.filterNode(leaf, node -> false, true);
        assertNull(result);
    }

    @Test
    void filterLeafNodeMatch() {
        OrgNode leaf = new OrgNode("1", null, "Leaf", "L001");
        OrgNode result = TreeUtils.filterNode(leaf, node -> true, true);
        assertNotNull(result);
        assertEquals("Leaf", result.getName());
    }

    @Test
    void filterNodeRecurseOnMatchFalse() {
        OrgNode root = tree.get(0);
        OrgNode result = TreeUtils.filterNode(root, node -> node.getName().equals("Root"), false);
        assertNotNull(result);
        assertEquals("Root", result.getName());
        assertEquals(2, result.getChildren().size());
    }

    @Test
    void printTreeDefaultLabel() {
        String result = TreeUtils.printTree(tree);
        assertEquals("""
                └── R001 Root
                    ├── C001 Child1
                    │   ├── G001 Grandchild1
                    │   └── G002 Grandchild2
                    └── C002 Child2
                """, result);
    }

    @Test
    void printTreeCustomLabel() {
        String result = TreeUtils.printTree(tree, OrgNode::getCode);
        assertEquals("""
                └── R001
                    ├── C001
                    │   ├── G001
                    │   └── G002
                    └── C002
                """, result);
    }

    @Test
    void printTreeEmpty() {
        List<OrgNode> emptyPrintList = Collections.emptyList();
        String result = TreeUtils.printTree(emptyPrintList);
        assertEquals("(empty)", result);
    }

    @Test
    void printTreeStructure() {
        String result = TreeUtils.printTree(tree, OrgNode::getName);
        assertTrue(result.contains("├── Root") || result.contains("└── Root"));
        assertTrue(result.contains("├── Child1") || result.contains("└── Child1"));
        assertTrue(result.contains("└── Child2"));
        assertTrue(result.contains("├── Grandchild1"));
        assertTrue(result.contains("└── Grandchild2"));
    }

    @Test
    void printTreeMultiRoot() {
        String result = TreeUtils.printTree(buildMultiRootTree(), OrgNode::getName);
        assertEquals("""
                ├── Root1
                │   └── Child1
                └── Root2
                    └── Child2
                """, result);
    }

    @Test
    void printTreeSingleRoot() {
        OrgNode single = new OrgNode("1", null, "Single", "S001");
        List<OrgNode> singleList = new ArrayList<>();
        singleList.add(single);
        String result = TreeUtils.printTree(singleList, OrgNode::getName);
        assertEquals("""
                 └── Single
                 """, result);
    }

    static class SimpleNode implements ITreeNode<SimpleNode> {
        private String name;
        private List<SimpleNode> children;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @Override
        public List<SimpleNode> getChildren() { return children; }

        @Override
        public void setChildren(List<SimpleNode> children) { this.children = children; }

        @Override
        public void addChild(SimpleNode child) {
            if (children == null) children = new ArrayList<>();
            children.add(child);
        }
    }
}
