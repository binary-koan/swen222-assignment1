package cluedo.ui.graphical;

import cluedo.game.Board;
import cluedo.game.Door;
import cluedo.game.Game;
import cluedo.game.Player;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.ui.graphical.util.Autotiler;
import cluedo.ui.graphical.util.PathFinder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders a game board
 */
public class BoardCanvas extends JPanel implements MouseListener, MouseMotionListener {
    // Colours used for board objects
    private static final Color CORRIDOR_COLOR = Color.LIGHT_GRAY;
    private static final Color WALL_COLOR = Color.BLACK;
    private static final Color ROOM_COLOR = Color.ORANGE;
    private static final Color DOOR_COLOR = Color.RED;
    private static final Color CAN_MOVE_COLOR = new Color(1, 1, 1, 0.5f);
    private static final Color CANNOT_MOVE_COLOR = new Color(1, 0, 0, 0.5f);

    private Game game;
    private Board board;

    private int startX;
    private int startY;
    private int tileSize;

    private Player currentPlayer;
    private int movesRemaining;
    private Point mouseLocation;
    private PathFinder.MovePath movePath;

    Color[][] cellColors;

    /**
     * Construct a new board canvas
     *
     * @param game game to render
     */
    public BoardCanvas(Game game) {
        this.game = game;
        this.board = game.getBoard();

        cellColors = new Color[board.getWidth()][board.getHeight()];
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                cellColors[x][y] = board.isCorridor(new Point(x, y)) ? CORRIDOR_COLOR : null;
            }
        }
        for (Room room : game.getData().getRooms()) {
            for (Point p : room.getPoints()) {
                cellColors[p.x][p.y] = ROOM_COLOR;
            }
        }

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Starts a particular player's turn
     *
     * @param player current player
     */
    public void startTurn(Player player) {
        currentPlayer = player;
        movesRemaining = player.getMovesRemaining();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        int xTileSize = getWidth() / board.getWidth();
        int yTileSize = getHeight() / board.getHeight();
        tileSize = Math.min(xTileSize, yTileSize);

        g.setColor(WALL_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        startX = (getWidth() - (tileSize * board.getWidth())) / 2;
        startY = (getHeight() - (tileSize * board.getHeight())) / 2;
        g.translate(startX, startY);

        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                drawTile(g, x, y);
            }
        }

        for (Room room : game.getData().getRooms()) {
            drawRoomInfo(g, room);

            for (Door door : room.getDoors()) {
                drawDoor(g, room, door);
            }
        }

        if (isEnabled()) {
            for (Player player : game.getPlayers()) {
                if (player.isInGame()) {
                    drawPlayer(player, tileSize, g);
                }
            }
            drawMovePath(g);
        }
        else {
            g.translate(-startX, -startY);
            g.setColor(new Color(1f, 1f, 1f, 0.5f));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void drawMovePath(Graphics g) {
        if (mouseLocation == null || board.getPlayerLocation(currentPlayer) == null) {
            return;
        }

        movePath = PathFinder.calculate(
                board, board.getPlayerLocation(currentPlayer), currentPlayer.getRoom(), mouseLocation, movesRemaining
        );
        if (movePath == null) {
            drawTile(g, mouseLocation.x, mouseLocation.y, CANNOT_MOVE_COLOR);
        }
        else {
            for (Point point : movePath.asPoints()) {
                drawTile(g, point.x, point.y, CAN_MOVE_COLOR);
            }
        }
    }

    private void drawPlayer(Player player, int tileSize, Graphics g) {
        if (player.getRoom() == null) {
            Point point = board.getPlayerLocation(player);
            drawPlayerToken(player.getToken(), point.x * tileSize, point.y * tileSize, g);
        }
    }

    private void drawPlayerToken(Suspect token, int realX, int realY, Graphics g) {
        int offset = tileSize / 8;
        int size = tileSize - offset * 2;

        g.setColor(token.getColor());
        g.fillOval(realX + offset, realY + offset, size, size);
    }

    private void drawDoor(Graphics g, Room room, Door door) {
        g.setColor(DOOR_COLOR);

        Point point = door.getLocation();
        int realX = point.x * tileSize;
        int realY = point.y * tileSize;
        int thickness = tileSize / 4;
        int offset = tileSize / 8;

        if (door.isVertical()) {
            if (point.x == room.getBoundingBox().getMinX()) {
                g.fillRect(realX - offset, realY, thickness, tileSize); // Left side
            }
            else {
                g.fillRect((int) (realX + tileSize * (3.0 / 4)) + offset, realY, thickness, tileSize); // Right side
            }
        }
        else {
            if (point.y == room.getBoundingBox().getMinY()) {
                g.fillRect(realX, realY - offset, tileSize, thickness); // Top
            }
            else {
                g.fillRect(realX, (int) (realY + tileSize * (3.0 / 4)) + offset, tileSize, thickness); // Bottom
            }
        }
    }

    private void drawRoomInfo(Graphics g, Room room) {
        drawRoomName(room, tileSize, g);
        drawRoomWeapon(room, tileSize, g);
        drawRoomPlayers(room, tileSize, g);
    }

    private void drawRoomPlayers(Room room, int tileSize, Graphics g) {
        List<Player> roomPlayers = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            if (player.getRoom() != null && player.getRoom().equals(room)) {
                roomPlayers.add(player);
            }
        }

        Point2D.Float center = room.getCenterPoint();
        int realX = (int)((center.x - (roomPlayers.size() / 2.0f)) * tileSize);
        int realY = (int)((center.y * tileSize) - tileSize * 1.5);
        for (Player player : roomPlayers) {
            drawPlayerToken(player.getToken(), realX, realY, g);
            realX += tileSize;
        }
    }

    private void drawRoomWeapon(Room room, int tileSize, Graphics g) {
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, (int) (tileSize * 0.5));
        g.setFont(font);

        Weapon weapon = room.getWeapon();
        if (weapon == null) {
            return;
        }

        Point2D.Float center = room.getCenterPoint();
        Rectangle2D bounds = getTextBounds(weapon.getName(), g);

        int x = (int) (center.x * tileSize - bounds.getWidth() / 2.0);
        int y = (int) (center.y * tileSize + bounds.getHeight() * 2.5);

        double padding = bounds.getHeight() / 3;
        int width = (int) (bounds.getWidth() + padding * 2);
        int height = (int) (bounds.getHeight() + padding * 2);

        g.setColor(WALL_COLOR);
        g.fillRoundRect((int) (x - padding), (int) (y - padding - height / 2), width, height, height / 4, height / 4);
        g.setColor(CORRIDOR_COLOR);
        g.drawString(weapon.getName(), x, y);
    }

    private void drawRoomName(Room room, int tileSize, Graphics g) {
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, (int) (tileSize * 0.6));
        g.setFont(font);

        Point2D.Float center = room.getCenterPoint();
        Rectangle2D bounds = getTextBounds(room.getName(), g);
        int x = (int) (center.x * tileSize - bounds.getWidth() / 2.0);
        int y = (int) (center.y * tileSize + bounds.getHeight() / 2.0);

        g.setColor(DOOR_COLOR);
        g.drawString(room.getName(), x, y);
    }

    private Rectangle2D getTextBounds(String text, Graphics g) {
        Rectangle2D bounds;
        if (g instanceof Graphics2D) {
            TextLayout txt = new TextLayout(text, g.getFont(), ((Graphics2D)g).getFontRenderContext());
            bounds = txt.getBounds();
        }
        else {
            bounds = new Rectangle2D.Float();
        }
        return bounds;
    }

    private void drawTile(Graphics g, int x, int y) {
        drawTile(g, x, y, cellColors[x][y]);
    }

    private void drawTile(Graphics g, int x, int y, Color color) {
        int realX = x * tileSize;
        int realY = y * tileSize;

        if (color != null && (color.equals(CORRIDOR_COLOR) || color.equals(ROOM_COLOR))) {
            Autotiler.Flags flags = Autotiler.getFlags(cellColors, x, y);
            autoTile(g, color, flags, tileSize, realX, realY);
        }
        else if (color == null) {
            g.setColor(WALL_COLOR);
            g.fillRect(realX, realY, tileSize, tileSize);
        }
        else {
            g.setColor(color);
            g.fillRect(realX, realY, tileSize, tileSize);
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

    @Override
    public void mouseClicked(MouseEvent e) {
        if (movePath != null) {
            try {
                board.movePlayer(currentPlayer, movePath.asDirections(), movePath.getDoor());
                updateMovesRemaining(movePath.asPoints().get(movePath.size() - 1));
                repaint();
            } catch (Board.UnableToMoveException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Can't move!", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void updateMovesRemaining(Point endPoint) {
        int newMovesRemaining;
        if (board.isDoor(endPoint)) {
            newMovesRemaining = 0;
        }
        else {
            newMovesRemaining = movesRemaining - movePath.size() + 1;
        }
        movesRemaining = newMovesRemaining;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (currentPlayer != null) {
            mouseLocation = getTilePosition(e.getX(), e.getY());
            repaint();
        }
    }

    private Point getTilePosition(int x, int y) {
        return new Point(
                (x - startX) / tileSize,
                (y - startY) / tileSize
        );
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseDragged(MouseEvent e) { }
}
