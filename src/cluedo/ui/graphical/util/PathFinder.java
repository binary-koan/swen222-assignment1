package cluedo.ui.graphical.util;

import cluedo.game.Board;
import cluedo.game.Board.Direction;
import cluedo.game.Door;
import cluedo.game.objects.Room;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Uses A* path-finding to construct a path from one point to another
 */
public class PathFinder {
    /**
     * Represents a movement path on the board, optionally ending in a door
     */
    public static class MovePath {
        private List<Point> points;
        private Door door;

        /**
         * Construct a new movement path
         *
         * @param points points on the path (including start and end)
         * @param door door at the start of the path
         */
        public MovePath(List<Point> points, Door door) {
            this.points = points;
            this.door = door;
        }

        /**
         * Returns the door that this path was constructed with
         */
        public Door getDoor() {
            return door;
        }

        /**
         * Returns the path as a list of points
         */
        public List<Point> asPoints() {
            return points;
        }

        /**
         * Calculates a list of directions representing the path
         *
         * @return the computed list
         */
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

        /**
         * Returns the number of points in the path, including the start and end
         */
        public int size() {
            return points.size();
        }
    }

    /**
     * Represents a node in the A* search queue
     */
    private static class Node implements Comparable<Node> {
        private Node parent;
        private Point point;
        private int distanceSoFar;
        private double heuristic;

        /**
         * Construct a new node
         *
         * @param parent the parent node in the tree
         * @param current the point that this node represents
         * @param goal the desired end point (used to calculate heuristic etc.)
         */
        public Node(Node parent, Point current, Point goal) {
            this.parent = parent;
            this.point = current;
            this.distanceSoFar = (parent == null) ? 0 : parent.getDistanceSoFar() + 1;
            this.heuristic = distanceSoFar + distanceTo(goal);
        }

        /**
         * Returns the point that this node represents
         */
        public Point getPoint() {
            return point;
        }

        /**
         * Returns the parent node in the tree
         */
        public Node getParent() {
            return parent;
        }

        /**
         * Returns the number of moves required to get to this node
         */
        public int getDistanceSoFar() {
            return distanceSoFar;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(Node other) {
            return Double.compare(this.heuristic, other.heuristic);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof Node && ((Node) other).getPoint().equals(point);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return point.hashCode();
        }

        /**
         * Returns the Euclidean distance to the specified point
         *
         * @param goal point to calculate distance to
         */
        private double distanceTo(Point goal) {
            int xDistance = point.x - goal.x;
            int yDistance = point.y - goal.y;
            return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
        }
    }

    /**
     * Calculates a movement path between two nodes on the board, ensuring it is at most maxSteps in length
     *
     * @param board board to calculate path on
     * @param start starting point
     * @param room room to start from (the positions of the room's doors will override `start` unless this is null)
     * @param goal point to move to
     * @param maxSteps maximum length of the path
     * @return a path from `start` (or `room`) to goal, or null if no path is found or if the path length exceeds
     *         maxSteps
     */
    public static MovePath calculate(Board board, Point start, Room room, Point goal, int maxSteps) {
        Door exitDoor = null;
        if (room != null) {
            exitDoor = room.getClosestDoor(goal);
            start = exitDoor.getPointBeside();
            maxSteps--;
        }

        PriorityQueue<Node> nodes = new PriorityQueue<>();
        Set<Node> visited = new HashSet<>();
        nodes.add(new Node(null, start, goal));
        while (!nodes.isEmpty()) {
            Node current = nodes.poll();
            if (visited.contains(current) || current.getDistanceSoFar() > maxSteps) {
                continue;
            }
            else if (exitDoor != null && current.getPoint().equals(exitDoor.getLocation())) {
                continue;
            }
            else if (current.getPoint().equals(goal)) {
                return buildPath(current, exitDoor);
            }
            visited.add(current);

            if (!board.isDoor(current.getPoint())) {
                addNeighbours(board, goal, nodes, current);
            }
        }
        return null;
    }

    /**
     * Adds traversable neighbours of the specified node into the priority queue
     *
     * @param board board to calculate based on
     * @param goal desired end point of the path
     * @param nodes priority queue of nodes
     * @param current node to check neighbours of
     */
    private static void addNeighbours(Board board, Point goal, PriorityQueue<Node> nodes, Node current) {
        Point point = current.getPoint();
        Point[] surrounding = new Point[] {
                new Point(point.x - 1, point.y),
                new Point(point.x + 1, point.y),
                new Point(point.x, point.y - 1),
                new Point(point.x, point.y + 1)
        };
        for (Point beside : surrounding) {
            if (board.isCorridor(beside) || board.canEnterDoor(beside, point)) {
                nodes.add(new Node(current, beside, goal));
            }
        }
    }

    /**
     * Builds a movement path from the specified end node
     *
     * @param node end point of the A* search
     * @param door door at the start of the path, or null if not exiting a room
     * @return the calculated movement path
     */
    private static MovePath buildPath(Node node, Door door) {
        List<Point> points = new ArrayList<>();
        while (node != null) {
            points.add(node.getPoint());
            node = node.getParent();
        }

        Collections.reverse(points);
        return new MovePath(points, door);
    }
}
