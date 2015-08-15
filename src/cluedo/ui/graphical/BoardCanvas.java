package cluedo.ui.graphical;

import cluedo.game.Board;
import cluedo.game.Game;
import cluedo.game.objects.Room;

import javax.swing.*;
import java.awt.*;

public class BoardCanvas extends JPanel {
    private static final int HAS_NORTH = 0b0000_0001;
    private static final int HAS_EAST = 0b0000_0010;
    private static final int HAS_SOUTH = 0b0000_0100;
    private static final int HAS_WEST = 0b1000;

    private static final Color CORRIDOR_COLOR = Color.GRAY;
    private static final Color WALL_COLOR = Color.DARK_GRAY;
    private static final Color ROOM_COLOR = Color.ORANGE;

    private Game game;
    private Board board;

    Color[][] cellColors;

    public BoardCanvas(Game game) {
        this.game = game;
        this.board = game.getBoard();

        cellColors = new Color[board.getWidth()][board.getHeight()];
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                cellColors[x][y] = board.isCorridor(x, y) ? CORRIDOR_COLOR : null;
            }
        }
        for (Room room : game.getData().getRooms()) {
            for (Point p : room.getPoints()) {
                cellColors[p.x][p.y] = ROOM_COLOR;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        int xTileSize = getWidth() / board.getWidth();
        int yTileSize = getHeight() / board.getHeight();
        int tileSize = Math.min(xTileSize, yTileSize);

        int startX = (getWidth() - (tileSize * board.getWidth())) / 2;
        int startY = (getHeight() - (tileSize * board.getHeight())) / 2;

        g.setColor(WALL_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                drawTile(g, tileSize, startX, x, startY, y);
            }
        }

        if (isEnabled()) {
            // Draw players
        }
        else {
            g.setColor(new Color(1f, 1f, 1f, 0.5f));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void drawTile(Graphics g, int tileSize, int startX, int x, int startY, int y) {
        int realX = startX + (x * tileSize);
        int realY = startY + (y * tileSize);
        Color color = cellColors[x][y];

        if (color == null) {
            g.setColor(WALL_COLOR);
            g.fillRect(realX, realY, tileSize, tileSize);
        }
        else {
            int flags = getAutoTileFlags(x, y, color);
            autoTile(g, color, flags, tileSize, realX, realY);

            //TODO: Just do this when showing where to move
//            if (color.equals(CORRIDOR_COLOR)) {
//                int dotX = Math.round(realX + tileSize * 2 / 5.0f);
//                int dotY = Math.round(realY + tileSize * 2 / 5.0f);
//                int dotSize = Math.round(tileSize / 5.0f);
//                g.fillOval(dotX, dotY, dotSize, dotSize);
//            }
        }
    }

    private int getAutoTileFlags(int x, int y, Color color) {
        int flags = 0;
        if (x > 0 && color.equals(cellColors[x - 1][y])) {
            flags |= HAS_WEST;
        }
        if (x < (cellColors.length - 1) && color.equals(cellColors[x + 1][y])) {
            flags |= HAS_EAST;
        }
        if (y > 0 && color.equals(cellColors[x][y - 1])) {
            flags |= HAS_NORTH;
        }
        if (y < (cellColors[0].length - 1) && color.equals(cellColors[x][y + 1])) {
            flags |= HAS_SOUTH;
        }
        return flags;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }

    private void autoTile(Graphics g, Color color, int flags, int tileSize, int realX, int realY) {
        g.setColor(color);
        g.fillRect(realX, realY, tileSize, tileSize);
        g.setColor(color.darker());
        int wallThickness = tileSize / 8;
        if ((flags & HAS_NORTH) == 0) {
            g.fillRect(realX, realY, tileSize, wallThickness);
        }
        if ((flags & HAS_EAST) == 0) {
            g.fillRect(realX + tileSize - wallThickness, realY, wallThickness, tileSize);
        }
        if ((flags & HAS_WEST) == 0) {
            g.fillRect(realX, realY, wallThickness, tileSize);
        }
        if ((flags & HAS_SOUTH) == 0) {
            g.fillRect(realX, realY + tileSize - wallThickness, tileSize, wallThickness);
        }
    }
}
