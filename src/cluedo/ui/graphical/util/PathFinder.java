package cluedo.ui.graphical.util;

import cluedo.game.Board;
import cluedo.game.Board.Direction;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PathFinder {
    public static class MovePath {
        private List<Point> points;

        public MovePath(List<Point> points) {
            this.points = points;
        }

        public List<Point> asPoints() {
            return points;
        }

        public List<Direction> asDirections() {
            List<Direction> result = new ArrayList<>();
            Point current = points.get(0);
            for (int i = 1; i < points.size(); i++) {
                Point next = points.get(i);
                if (next.x < current.x) {
                    result.add(Direction.LEFT);
                }
                else if (next.x > current.x) {
                    result.add(Direction.RIGHT);
                }
                else if (next.y < current.y) {
                    result.add(Direction.UP);
                }
                else {
                    result.add(Direction.DOWN);
                }
                current = next;
            }
            return result;
        }

        public int size() {
            return points.size();
        }
    }

    private static class Node implements Comparable<Node> {
        private Node parent;
        private Point point;
        private int distanceSoFar;
        private double heuristic;

        public Node(Node parent, Point current, Point goal) {
            this.parent = parent;
            this.point = current;
            this.distanceSoFar = (parent == null) ? 0 : parent.getDistanceSoFar() + 1;
            this.heuristic = distanceSoFar + distanceTo(goal);
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.heuristic, other.heuristic);
        }

        public Point getPoint() {
            return point;
        }

        public Node getParent() {
            return parent;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Node && ((Node) other).getPoint().equals(point);
        }

        @Override
        public int hashCode() {
            return point.hashCode();
        }

        public int getDistanceSoFar() {
            return distanceSoFar;
        }

        private double distanceTo(Point goal) {
            int xDistance = point.x - goal.x;
            int yDistance = point.y - goal.y;
            return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
        }
    }

    public static MovePath calculate(Board board, Point start, Point goal, int maxSteps) {
        PriorityQueue<Node> nodes = new PriorityQueue<>();
        Set<Node> visited = new HashSet<>();
        nodes.add(new Node(null, start, goal));
        while (!nodes.isEmpty()) {
            Node current = nodes.poll();
            if (visited.contains(current) || current.getDistanceSoFar() > maxSteps) {
                continue;
            }
            else if (current.getPoint().equals(goal)) {
                return buildPath(current);
            }
            visited.add(current);

            if (!board.isDoor(current.getPoint())) {
                addNeighbours(board, goal, nodes, current);
            }
        }
        return null;
    }

    private static void addNeighbours(Board board, Point goal, PriorityQueue<Node> nodes, Node current) {
        Point point = current.getPoint();
        Point[] surrounding = new Point[] {
                new Point(point.x - 1, point.y),
                new Point(point.x + 1, point.y),
                new Point(point.x, point.y - 1),
                new Point(point.x, point.y + 1)
        };
        for (Point beside : surrounding) {
            if (board.isCorridor(beside) || board.isDoor(beside)) {
                nodes.add(new Node(current, beside, goal));
            }
        }
    }

    private static MovePath buildPath(Node node) {
        List<Point> points = new ArrayList<>();
        do {
            points.add(node.getPoint());
            node = node.getParent();
        } while (node != null);

        Collections.reverse(points);
        return new MovePath(points);
    }
}
