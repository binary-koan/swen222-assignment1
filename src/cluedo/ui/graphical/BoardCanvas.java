package cluedo.ui.graphical;

import cluedo.game.Board;
import cluedo.game.Door;
import cluedo.game.Game;
import cluedo.game.Player;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.ui.graphical.util.Autotiler;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class BoardCanvas extends JPanel {
    private static final Color CORRIDOR_COLOR = Color.LIGHT_GRAY;
    private static final Color WALL_COLOR = Color.BLACK;
    private static final Color ROOM_COLOR = Color.ORANGE;
    private static final Color DOOR_COLOR = Color.RED;

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
        if (!(g instanceof Graphics2D)) {
            throw new RuntimeException("Unsupported Java environment: paintComponent() must be called with Graphics2D");
        }

        Graphics2D g2 = (Graphics2D) g;
        // Use antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int xTileSize = getWidth() / board.getWidth();
        int yTileSize = getHeight() / board.getHeight();
        int tileSize = Math.min(xTileSize, yTileSize);

        g2.setColor(WALL_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());

        int startX = (getWidth() - (tileSize * board.getWidth())) / 2;
        int startY = (getHeight() - (tileSize * board.getHeight())) / 2;
        g2.translate(startX, startY);

        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                drawTile(g2, tileSize, x, y);
            }
        }

        for (Room room : game.getData().getRooms()) {
            drawRoomInfo(g2, room, tileSize);

            for (Door door : room.getDoors()) {
                drawDoor(g2, room, door, tileSize);
            }
        }

        if (isEnabled()) {
            for (Player player : game.getPlayers()) {
                if (player.isInGame()) {
                    drawPlayer(player, tileSize, g2);
                }
            }
        }
        else {
            g.translate(-startX, -startY);
            g.setColor(new Color(1f, 1f, 1f, 0.5f));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void drawPlayer(Player player, int tileSize, Graphics2D g2) {
        Suspect token = player.getToken();

        if (player.getRoom() == null) {
            Point point = board.getPlayerLocation(player);
            int offset = tileSize / 8;
            int size = tileSize - offset * 2;
            g2.setColor(token.getColor());
            g2.fillOval(point.x * tileSize + offset, point.y * tileSize + offset, size, size);
        }
        else {
            //TODO: Draw player in room
        }
    }

    private void drawDoor(Graphics2D g2, Room room, Door door, int tileSize) {
        g2.setColor(DOOR_COLOR);

        Point point = door.getLocation();
        int realX = point.x * tileSize;
        int realY = point.y * tileSize;
        int thickness = tileSize / 4;
        int offset = tileSize / 8;

        if (door.isVertical()) {
            if (point.x == room.getBoundingBox().getMinX()) {
                g2.fillRect(realX - offset, realY, thickness, tileSize); // Left side
            }
            else {
                g2.fillRect((int) (realX + tileSize * (3.0 / 4)) + offset, realY, thickness, tileSize); // Right side
            }
        }
        else {
            if (point.y == room.getBoundingBox().getMinY()) {
                g2.fillRect(realX, realY - offset, tileSize, thickness); // Top
            }
            else {
                g2.fillRect(realX, (int) (realY + tileSize * (3.0 / 4)) + offset, tileSize, thickness); // Bottom
            }
        }
    }

    private void drawRoomInfo(Graphics2D g2, Room room, int tileSize) {
        drawRoomName(room, tileSize, g2);
        drawRoomWeapon(room, tileSize, g2);
    }

    private void drawRoomWeapon(Room room, int tileSize, Graphics2D g2) {
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, (int) (tileSize * 0.5));
        g2.setFont(font);

        Weapon weapon = room.getWeapon();
        if (weapon == null) {
            return;
        }

        Point2D.Float center = room.getCenterPoint();
        TextLayout txt = new TextLayout(weapon.getName(), g2.getFont(), g2.getFontRenderContext());
        Rectangle2D bounds = txt.getBounds();

        int x = (int) (center.x * tileSize - bounds.getWidth() / 2.0);
        int y = (int) (center.y * tileSize + bounds.getHeight() * 2.5);

        double padding = bounds.getHeight() / 3;
        int width = (int) (bounds.getWidth() + padding * 2);
        int height = (int) (bounds.getHeight() + padding * 2);

        g2.setColor(WALL_COLOR);
        g2.fillRoundRect((int) (x - padding), (int) (y - padding - height / 2), width, height, height / 4, height / 4);
        g2.setColor(CORRIDOR_COLOR);
        g2.drawString(weapon.getName(), x, y);
    }

    private void drawRoomName(Room room, int tileSize, Graphics2D g2) {
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, (int) (tileSize * 0.6));
        g2.setFont(font);

        Point2D.Float center = room.getCenterPoint();
        TextLayout txt = new TextLayout(room.getName(), g2.getFont(), g2.getFontRenderContext());
        Rectangle2D bounds = txt.getBounds();
        int x = (int) (center.x * tileSize - bounds.getWidth() / 2.0);
        int y = (int) (center.y * tileSize + bounds.getHeight() / 2.0);

        g2.setColor(DOOR_COLOR);
        g2.drawString(room.getName(), x, y);
    }

    private void drawTile(Graphics g, int tileSize, int x, int y) {
        int realX = x * tileSize;
        int realY = y * tileSize;
        Color color = cellColors[x][y];

        if (color == null) {
            g.setColor(WALL_COLOR);
            g.fillRect(realX, realY, tileSize, tileSize);
        }
        else {
//            int flags = getAutoTileFlags(x, y, color);
            Autotiler.Flags flags = Autotiler.getFlags(cellColors, x, y);
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

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }

    private void autoTile(Graphics g, Color color, Autotiler.Flags flags, int tileSize, int realX, int realY) {
        g.setColor(color);
        g.fillRect(realX, realY, tileSize, tileSize);
        g.setColor(color.darker());

        int wallThickness = tileSize / 8;
        int eastX = realX + tileSize - wallThickness;
        int southY = realY + tileSize - wallThickness;

        if (flags.emptyNorth()) {
            g.fillRect(realX, realY, tileSize, wallThickness);
        }
        if (flags.emptyEast()) {
            g.fillRect(eastX, realY, wallThickness, tileSize);
        }
        if (flags.emptyWest()) {
            g.fillRect(realX, realY, wallThickness, tileSize);
        }
        if (flags.emptySouth()) {
            g.fillRect(realX, southY, tileSize, wallThickness);
        }
        if (flags.emptyNorthEast()) {
            g.fillRect(eastX, realY, wallThickness, wallThickness);
        }
        if (flags.emptyNorthWest()) {
            g.fillRect(realX, realY, wallThickness, wallThickness);
        }
        if (flags.emptySouthEast()) {
            g.fillRect(eastX, southY, wallThickness, wallThickness);
        }
        if (flags.emptySouthWest()) {
            g.fillRect(realX, southY, wallThickness, wallThickness);
        }
    }
}
