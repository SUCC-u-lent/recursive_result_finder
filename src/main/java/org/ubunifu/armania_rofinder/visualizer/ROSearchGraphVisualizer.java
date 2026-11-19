package org.ubunifu.armania_rofinder.visualizer;

import org.ubunifu.armania_rofinder.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Optional Swing graph visualiser for recursive searches.
 *
 * Usage:
 *   ROSearchGraphVisualizer visualizer = new ROSearchGraphVisualizer();
 *   visualizer.showWindow();
 *   RecursionFinder.findResult(processor, visualizer);
 *
 * The finder itself has no dependency on this class. Applications that do not
 * want a visual window simply use the normal findResult(processor) overload.
 */
public final class ROSearchGraphVisualizer implements ROSearchListener {

    private static final int NODE_WIDTH = 230;
    private static final int NODE_HEIGHT = 94;
    private static final int HORIZONTAL_GAP = 54;
    private static final int VERTICAL_GAP = 76;
    private static final int MARGIN = 35;

    private final JFrame frame = new JFrame("Recursive Outcome Finder — Search Graph");
    private final GraphPanel graph = new GraphPanel();
    private final JLabel stats = new JLabel("  Waiting for search...");
    private final Map<Long, GraphNode> nodes = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicInteger> levelSlots = new ConcurrentHashMap<>();

    private final AtomicInteger visited = new AtomicInteger();
    private final AtomicInteger running = new AtomicInteger();
    private final AtomicInteger resolved = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();

    public ROSearchGraphVisualizer() {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(stats, BorderLayout.NORTH);
        frame.add(new JScrollPane(graph), BorderLayout.CENTER);
        frame.setSize(1100, 720);
        frame.setMinimumSize(new Dimension(700, 450));
        frame.setLocationByPlatform(true);
    }

    public void showWindow() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    @Override
    public void onNodeCreated(
            long nodeId,
            Long parentId,
            String branchLabel,
            ROPro<?> processor) {

        int depth = parentId == null ? 0 : nodes.get(parentId).depth + 1;
        int slot = levelSlots.computeIfAbsent(depth, ignored -> new AtomicInteger())
                .getAndIncrement();

        GraphNode node = new GraphNode(
                nodeId,
                parentId,
                depth,
                slot,
                branchLabel,
                String.valueOf(processor.getContext())
        );
        nodes.put(nodeId, node);

        visited.incrementAndGet();
        running.incrementAndGet();

        SwingUtilities.invokeLater(() -> {
            graph.refreshSize(nodes);
            graph.repaint();
            updateStats();
        });
    }

    @Override
    public void onNodeCompleted(long nodeId, ROStatus status, ROResult result) {
        GraphNode node = nodes.get(nodeId);
        if (node == null) return;

        node.status = status;
        node.result = result;

        running.decrementAndGet();
        if (status == ROStatus.RESOLVED) resolved.incrementAndGet();
        if (status == ROStatus.FAILED) failed.incrementAndGet();

        SwingUtilities.invokeLater(() -> {
            graph.repaint();
            updateStats();
        });
    }

    private void updateStats() {
        stats.setText("  Nodes: " + visited.get()
                + "     Active: " + running.get()
                + "     Resolved: " + resolved.get()
                + "     Failed: " + failed.get());
    }

    private static final class GraphNode {
        final long id;
        final Long parentId;
        final int depth;
        final int slot;
        final String label;
        final String context;
        volatile ROStatus status;
        volatile ROResult result;

        GraphNode(long id, Long parentId, int depth, int slot, String label, String context) {
            this.id = id;
            this.parentId = parentId;
            this.depth = depth;
            this.slot = slot;
            this.label = label;
            this.context = context;
        }

        int x() {
            return MARGIN + slot * (NODE_WIDTH + HORIZONTAL_GAP);
        }

        int y() {
            return MARGIN + depth * (NODE_HEIGHT + VERTICAL_GAP);
        }
    }

    private final class GraphPanel extends JPanel {
        GraphPanel() {
            setBackground(new Color(248, 249, 252));
            setOpaque(true);
        }

        void refreshSize(Map<Long, GraphNode> nodes) {
            int maxX = 900;
            int maxY = 600;
            for (GraphNode node : nodes.values()) {
                maxX = Math.max(maxX, node.x() + NODE_WIDTH + MARGIN);
                maxY = Math.max(maxY, node.y() + NODE_HEIGHT + MARGIN);
            }
            setPreferredSize(new Dimension(maxX, maxY));
            revalidate();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Map<Long, GraphNode> snapshot = Map.copyOf(nodes);
            // Edges are drawn before nodes so they remain behind the node cards.
            for (GraphNode node : snapshot.values()) {
                if (node.parentId == null) continue;
                GraphNode parent = snapshot.get(node.parentId);
                if (parent == null) continue;

                int x1 = parent.x() + NODE_WIDTH / 2;
                int y1 = parent.y() + NODE_HEIGHT;
                int x2 = node.x() + NODE_WIDTH / 2;
                int y2 = node.y();

                g.setColor(new Color(145, 153, 168));
                g.setStroke(new BasicStroke(2f));
                int bendY = y1 + (y2 - y1) / 2;
                g.drawLine(x1, y1, x1, bendY);
                g.drawLine(x1, bendY, x2, bendY);
                g.drawLine(x2, bendY, x2, y2);
            }

            for (GraphNode node : snapshot.values()) {
                drawNode(g, node);
            }
            g.dispose();
        }

        private void drawNode(Graphics2D g, GraphNode node) {
            Color border;
            Color fill;
            String statusText;

            if (node.status == null) {
                border = new Color(55, 115, 205);
                fill = new Color(232, 241, 255);
                statusText = "PROCESSING";
            } else if (node.status == ROStatus.RESOLVED) {
                border = new Color(35, 145, 85);
                fill = new Color(231, 248, 238);
                statusText = "RESOLVED";
            } else {
                border = new Color(190, 65, 65);
                fill = new Color(255, 237, 237);
                statusText = "FAILED";
            }

            int x = node.x();
            int y = node.y();

            g.setColor(fill);
            g.fill(new RoundRectangle2D.Float(x, y, NODE_WIDTH, NODE_HEIGHT, 16, 16));
            g.setColor(border);
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Float(x, y, NODE_WIDTH, NODE_HEIGHT, 16, 16));

            g.setFont(getFont().deriveFont(Font.BOLD, 13f));
            g.setColor(new Color(35, 43, 58));
            g.drawString(clip(g, node.label + "  #" + node.id, NODE_WIDTH - 20), x + 12, y + 22);

            g.setFont(getFont().deriveFont(Font.PLAIN, 11f));
            g.setColor(new Color(70, 78, 94));
            g.drawString(clip(g, node.context, NODE_WIDTH - 20), x + 12, y + 43);

            g.setFont(getFont().deriveFont(Font.BOLD, 11f));
            g.setColor(border);
            String detail = statusText;
            if (node.result != null) {
                detail += "  |  H: " + node.result.getEffectiveHeuristic();
            }
            g.drawString(clip(g, detail, NODE_WIDTH - 20), x + 12, y + 68);
        }

        private static String clip(Graphics2D g, String value, int width) {
            FontMetrics metrics = g.getFontMetrics();
            String text = value == null ? "" : value;
            if (metrics.stringWidth(text) <= width) return text;
            while (!text.isEmpty() && metrics.stringWidth(text + "...") > width) {
                text = text.substring(0, text.length() - 1);
            }
            return text + "...";
        }
    }
}
