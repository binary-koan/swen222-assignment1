package cluedo.ui.graphical.util;

/**
 * Calculates what sort of tiles are around a particular tile, allowing arbitrary areas to be bordered neatly
 */
public class Autotiler {
    // Bitmask for the primary four directions
    private static final int HAS_NORTH = 0b0000_0001;
    private static final int HAS_EAST = 0b0000_0010;
    private static final int HAS_SOUTH = 0b0000_0100;
    private static final int HAS_WEST = 0b0000_1000;

    // Bitmask for diagonals
    private static final int HAS_NORTH_EAST = 0b0001_0000;
    private static final int HAS_NORTH_WEST = 0b0010_0000;
    private static final int HAS_SOUTH_EAST = 0b0100_0000;
    private static final int HAS_SOUTH_WEST = 0b1000_0000;

    /**
     * Represents the placement of a single tile - allows for checking which surrounding tiles are the same type
     */
    public static class Flags {
        private int flags;

        public Flags(int flags) {
            this.flags = flags;
        }

        public boolean emptyNorth() {
            return (flags & HAS_NORTH) == 0;
        }

        public boolean emptyEast() {
            return (flags & HAS_EAST) == 0;
        }

        public boolean emptySouth() {
            return (flags & HAS_SOUTH) == 0;
        }

        public boolean emptyWest() {
            return (flags & HAS_WEST) == 0;
        }

        public boolean emptyNorthEast() {
            return (flags & HAS_NORTH_EAST) == 0;
        }

        public boolean emptyNorthWest() {
            return (flags & HAS_NORTH_WEST) == 0;
        }

        public boolean emptySouthEast() {
            return (flags & HAS_SOUTH_EAST) == 0;
        }

        public boolean emptySouthWest() {
            return (flags & HAS_SOUTH_WEST) == 0;
        }
    }

    /**
     * Check the tiles surrounding the specified position to see if they are the same type as the tile at that position
     *
     * @param board the grid of tiles to search in
     * @param x x-coordinate of the tile to search from
     * @param y y-coordinate of the tile to search from
     * @return an instance of Flags describing the tiles surrounding this one
     */
    public static Flags getFlags(Object[][] board, int x, int y) {
        Object object = board[x][y];

        int flags = 0;
        int penultimateX = board.length - 1;
        int penultimateY = board[0].length - 1;

        if (x > 0) {
            if (object.equals(board[x - 1][y])) {
                flags |= HAS_WEST;
            }
            if (y > 0 && object.equals(board[x - 1][y - 1])) {
                flags |= HAS_NORTH_WEST;
            }
            if (y < penultimateY && object.equals(board[x - 1][y + 1])) {
                flags |= HAS_SOUTH_WEST;
            }
        }

        if (x < penultimateX) {
            if (object.equals(board[x + 1][y])) {
                flags |= HAS_EAST;
            }
            if (y > 0 && object.equals(board[x + 1][y - 1])) {
                flags |= HAS_NORTH_EAST;
            }
            if (y < penultimateY && object.equals(board[x + 1][y + 1])) {
                flags |= HAS_SOUTH_EAST;
            }
        }

        if (y > 0 && object.equals(board[x][y - 1])) {
            flags |= HAS_NORTH;
        }

        if (y < penultimateY && object.equals(board[x][y + 1])) {
            flags |= HAS_SOUTH;
        }

        return new Flags(flags);
    }
}
