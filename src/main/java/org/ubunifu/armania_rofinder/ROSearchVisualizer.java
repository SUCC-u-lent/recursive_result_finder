package org.ubunifu.armania_rofinder;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A live Swing view of the search tree. Pass an instance to
 * RecursionFinder.findResult(processor, visualizer).
 */
public final class ROSearchVisualizer implements ROSearchListener {

    private final JFrame frame = new JFrame("Recursive Outcome Finder");
    private final DefaultMutableTreeNode root =
            new DefaultMutableTreeNode(new NodeInfo("Search", "Waiting to start", "WAITING"));
    private final DefaultTreeModel treeModel = new DefaultTreeModel(root);
    private final JTree tree = new JTree(treeModel);
    private final JLabel summary = new JLabel("Waiting for search...");
    private final Map<Long, DefaultMutableTreeNode> nodes = new ConcurrentHashMap<>();

    private final AtomicInteger visited = new AtomicInteger();
    private final AtomicInteger running = new AtomicInteger();
    private final AtomicInteger resolved = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();

    public ROSearchVisualizer() {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        frame.add(summary, BorderLayout.NORTH);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new StatusRenderer());
        frame.add(new JScrollPane(tree), BorderLayout.CENTER);
        frame.setMinimumSize(new Dimension(700, 450));
        frame.setSize(950, 650);
        frame.setLocationByPlatform(true);
    }

    /** Opens the visualiser window. Safe to call from any thread. */
    public void showWindow() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    @Override
    public void onNodeCreated(
            long nodeId,
            Long parentId,
            String branchLabel,
            ROPro<?> processor) {

        visited.incrementAndGet();
        running.incrementAndGet();

        String context = String.valueOf(processor.getContext());
        NodeInfo info = new NodeInfo(
                branchLabel + "  [#" + nodeId + "]",
                context,
                "RUNNING"
        );
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(info);
        nodes.put(nodeId, node);

        SwingUtilities.invokeLater(() -> {
            DefaultMutableTreeNode parent = parentId == null
                    ? root
                    : nodes.getOrDefault(parentId, root);
            treeModel.insertNodeInto(node, parent, parent.getChildCount());
            tree.expandPath(new TreePath(parent.getPath()));
            updateSummary();
        });
    }

    @Override
    public void onNodeCompleted(long nodeId, ROStatus status, ROResult result) {
        running.decrementAndGet();

        if (status == ROStatus.RESOLVED) {
            resolved.incrementAndGet();
        } else if (status == ROStatus.FAILED) {
            failed.incrementAndGet();
        }

        SwingUtilities.invokeLater(() -> {
            DefaultMutableTreeNode node = nodes.get(nodeId);
            if (node != null) {
                NodeInfo info = (NodeInfo) node.getUserObject();
                info.status = status.name();
                if (result != null) {
                    info.details += " | weight=" + result.getWeight()
                            + ", heuristic=" + result.getEffectiveHeuristic();
                }
                treeModel.nodeChanged(node);
            }
            updateSummary();
        });
    }

    private void updateSummary() {
        summary.setText("  Visited: " + visited.get()
                + "    Running: " + running.get()
                + "    Resolved: " + resolved.get()
                + "    Failed: " + failed.get());
    }

    private static final class NodeInfo {
        private final String label;
        private String details;
        private String status;

        private NodeInfo(String label, String details, String status) {
            this.label = label;
            this.details = details;
            this.status = status;
        }

        @Override
        public String toString() {
            return label + "  —  " + status + "  —  " + details;
        }
    }

    private static final class StatusRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode node
                    && node.getUserObject() instanceof NodeInfo info) {
                if (!selected) {
                    switch (info.status) {
                        case "RUNNING" -> setForeground(new Color(30, 100, 190));
                        case "RESOLVED" -> setForeground(new Color(20, 135, 65));
                        case "FAILED" -> setForeground(new Color(175, 55, 55));
                        default -> setForeground(tree.getForeground());
                    }
                }
            }
            return this;
        }
    }
}
