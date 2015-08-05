package cluedo.ui.console;

import java.awt.Point;
import java.util.Set;

import cluedo.game.Board;
import cluedo.game.Door;
import cluedo.game.Game;
import cluedo.game.GameData;
import cluedo.game.Player;
import cluedo.game.objects.Room;

/**
 * Renders a game board to the console
 */
public class BoardRenderer {
	// Use non-breaking spaces to make sure everything aligns properly
	private static String SPACE = "\u00A0";
	private static String BLANK = SPACE + SPACE + SPACE;

	private Board board;
	private StringBuilder[] boardBase;

	/**
	 * Construct a new board renderer
	 *
	 * @param board
	 *            board to render
	 * @param data
	 *            associated data used to retrieve and position rooms
	 */
	public BoardRenderer(Board board, GameData data) {
		this.board = board;
		calculateBoardBase(data);
	}

	/**
	 * Prints out a representation of the given room, with doors numbered
	 * according to their index in the room's list. Note that the numbering
	 * starts from 1, not 0
	 *
	 * @param room
	 *            room to render
	 * @param game
	 *            current game state
	 */
	public void drawRoomWithExits(Room room, Game game) {
		if (room == null) {
			System.out.println("You are not in a room");
			return;
		}

		Room.BoundingBox boundingBox = room.getBoundingBox();
		StringBuilder[] roomDisplay = new StringBuilder[boundingBox.getMaxY() - boundingBox.getMinY() + 1];

		// Autotile the room's points
		for (Point point : room.getPoints()) {
			int y = point.y - boundingBox.getMinY();
			if (roomDisplay[y] == null) {
				roomDisplay[y] = new StringBuilder();
				for (int x = boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++) {
					appendTile(roomDisplay[y], BLANK);
				}
			}
			setTile(roomDisplay[y], point.x - boundingBox.getMinX(),
					getRoomTile(point, room.getPoints()));
		}

		// Draw basic information about the room
		drawRoomName(room, boundingBox.getMinX(), boundingBox.getMinY(),
				roomDisplay);
		drawRoomContents(room, boundingBox.getMinX(), boundingBox.getMinY(),
				roomDisplay, game);

		// Draw up to 9 doors, represented as numbers
		char i = '1';
		if (room.getDoors().size() > 9) {
			throw new RuntimeException("Room '" + room.getName() + "' has too many doors (max 9)");
		}
		for (Door door : room.getDoors()) {
			drawDoor(door, boundingBox.getMinX(), boundingBox.getMinY(),
					roomDisplay, i);
			i++;
		}

		for (StringBuilder builder : roomDisplay) {
			System.out.println(builder.toString());
		}
	}

	/**
	 * Draws the entire board in its current state
	 *
	 * @param game
	 *            current game state
	 */
	public void drawBoard(Game game) {
		drawBoard(0, board.getHeight(), game);
	}

	/**
	 * Draws a part of the board in its current state
	 *
	 * @param startY
	 *            y-index to start drawing (inclusive)
	 * @param endY
	 *            y-index to end drawing (exclusive)
	 * @param game
	 *            current game state (for retrieving player positions etc.)
	 */
	public void drawBoard(int startY, int endY, Game game) {
		startY = Math.max(0, startY);
		endY = Math.min(boardBase.length, endY);

		String[] board = buildCurrentBoard(game);
		for (int y = startY; y < endY; y++) {
			System.out.println(board[y]);
		}
	}

	/**
	 * Calculates as much of the board as can be discovered without knowing the
	 * state of the game (corridors, room walls, doors, etc.)
	 */
	private void calculateBoardBase(GameData data) {
		int height = board.getHeight();
		int width = board.getWidth();

		// Start by drawing corridors and blank space
		boardBase = new StringBuilder[height];
		for (int y = 0; y < height; y++) {
			boardBase[y] = new StringBuilder(board.getWidth());
			for (int x = 0; x < width; x++) {
				if (board.isCorridor(x, y)) {
					appendTile(boardBase[y], SPACE + '\u00B7' + SPACE);
				}
				else {
					appendTile(boardBase[y], BLANK);
				}
			}
		}

		// Then autotile rooms and add their doors
		for (Room room : data.getRooms()) {
			for (Point point : room.getPoints()) {
				setTile(boardBase[point.y], point.x,
						getRoomTile(point, room.getPoints()));
			}
			for (Door door : room.getDoors()) {
				drawDoor(door, 0, 0, boardBase, '*');
			}
			drawRoomName(room, 0, 0, boardBase);
		}
	}

	/**
	 * Returns the current board state, including players, weapon locations, etc
	 */
	private String[] buildCurrentBoard(Game game) {
		Board board = game.getBoard();

		StringBuilder[] copies = new StringBuilder[board.getHeight()];
		for (int i = 0; i < boardBase.length; i++) {
			copies[i] = new StringBuilder(boardBase[i]);
		}

		// Show player positions
		for (Player player : game.getPlayers()) {
			if (player.getRoom() != null) {
				continue;
			}
			Point location = board.getPlayerLocation(player);
			setTile(copies[location.y], location.x, SPACE + player.getToken().getIdentifier() + SPACE);
		}

		// Show room contents
		for (Room room : game.getData().getRooms()) {
			drawRoomContents(room, 0, 0, copies, game);
		}

		String[] result = new String[board.getHeight()];
		for (int i = 0; i < board.getHeight(); i++) {
			result[i] = copies[i].toString();
		}
		return result;
	}

	/**
	 * Draws a door on the given board representation
	 *
	 * @param door
	 *            door to draw
	 * @param baseX
	 *            origin x-coordinate
	 * @param baseY
	 *            origin y-coordinate
	 * @param base
	 *            intermediate board representation to draw onto
	 * @param rep
	 *            a how to represent the door (ie. whether to use a number or a
	 *            '*')
	 */
	private void drawDoor(Door door, int baseX, int baseY,
			StringBuilder[] base, char rep) {
		Point location = door.getLocation();
		int x = location.x - baseX;
		int y = location.y - baseY;

		if (door.isVertical()) {
			if (location.x == door.getRoom().getBoundingBox().getMinX()) {
				// Left of room
				setTile(base[y], x, rep + SPACE + SPACE);
			}
			else {
				// Right of room
				setTile(base[y], x, SPACE + SPACE + rep);
			}
		}
		else if (rep == '*') {
			// Top or bottom of room
			setTile(base[y], x, "***");
		}
		else {
			setTile(base[y], x, SPACE + rep + SPACE);
		}
	}

	/**
	 * Draws a room name onto it (using the given board representation)
	 *
	 * @param room
	 *            room to get the name from
	 * @param baseX
	 *            origin x-coordinate
	 * @param baseY
	 *            origin y-coordinate
	 * @param base
	 *            intermediate board representation to draw onto
	 */
	private void drawRoomName(Room room, int baseX, int baseY,
			StringBuilder[] base) {
		Point center = room.getCenterPoint();
		String name = room.getName();

		int startX = (center.x * 3) - baseX - (name.length() / 2) + 1;
		int endX = startX + name.length();

		StringBuilder nameRow = base[center.y - baseY];
		for (int realX = startX; realX < endX; ++realX) {
			nameRow.setCharAt(realX, name.charAt(realX - startX));
		}
	}

	/**
	 * Draws the contents of a room onto it (using the given board
	 * representation)
	 *
	 * @param room
	 *            room to get the contents from
	 * @param baseX
	 *            origin x-coordinate
	 * @param baseY
	 *            origin y-coordinate
	 * @param base
	 *            intermediate board representation to draw onto
	 * @param game
	 *            current game state
	 */
	private void drawRoomContents(Room room, int baseX, int baseY,
			StringBuilder[] base, Game game) {
		Point center = room.getCenterPoint();
		String contents = "(";
		if (room.getWeapon() != null) {
			contents += room.getWeapon().getName() + "; ";
		}
		for (Player player : game.getPlayers()) {
			if (player.getRoom() != null) {
				System.err.print(player.getToken().getIdentifier() + ":" + player.getRoom().getName() + "; ");
			}
			if (room.equals(player.getRoom())) {
				contents += player.getToken().getIdentifier() + ", ";
			}
		}
		System.err.println();
		if (contents.equals("(")) {
			return;
		}
		contents = contents.substring(0, contents.length() - 2) + ")";

		int startX = (center.x * 3) - baseX - (contents.length() / 2) + 1;
		int endX = startX + contents.length();

		StringBuilder contentsRow = boardBase[center.y + 1];
		for (int realX = startX; realX < endX; ++realX) {
			contentsRow.setCharAt(realX, contents.charAt(realX - startX));
		}
	}

	/**
	 * Append the tile represented by str to a row
	 *
	 * @param row
	 *            partial board row
	 * @param str
	 *            3-character string to append as a tile
	 */
	private void appendTile(StringBuilder row, String str) {
		row.append(str);
	}

	/**
	 * Set the tile at the specified position to str
	 *
	 * @param row
	 *            complete board row
	 * @param x
	 *            position (in tiles) to set the string
	 * @param str
	 */
	private void setTile(StringBuilder row, int x, String str) {
		setTile(row, x, str.charAt(0), str.charAt(1), str.charAt(2));
	}

	/**
	 * Sets the tile at the specified row to a sequence of characters
	 */
	private void setTile(StringBuilder row, int x, char first, char second,
			char third) {
		row.setCharAt((x * 3), first);
		row.setCharAt((x * 3) + 1, second);
		row.setCharAt((x * 3) + 2, third);
	}

	// A character is chosen from this list for each point in the room based on
	// which directions contain another point in that room. Directions are OR'd
	// together with 0001 = tile above, 0010 = tile left, 0100 = tile below and
	// 1000 = tile right
	private static final String[] ROOM_AUTOTILE = {
			"???", "???", "???", "__/", // bottom right corner = 0011
			"???", "???", "\u203E\u203E\\", SPACE + SPACE + "|", // top right corner = 0110, right side = 0111
			"???", "\\__", "???", "___", "/\u203E\u203E", // bottom left = 1001, bottom = 1011, top left = 1100
			"|" + SPACE + SPACE, "\u203E\u203E\u203E", BLANK // left side = 1101, top = 1110, middle = 1111
	};

	/**
	 * Returns a 3-character string representing a point in a room, calculated
	 * based on the tiles around it (so a tile in the top left corner will be
	 * represented as a '/', bottom will be '_', etc
	 *
	 * @param current
	 *            point to calculate based on
	 * @param all
	 *            set of all points contained in the room
	 */
	private String getRoomTile(Point current, Set<Point> all) {
		int tileIndex = 0;
		if (all.contains(new Point(current.x, current.y - 1)))
			tileIndex |= 0b1;
		if (all.contains(new Point(current.x - 1, current.y)))
			tileIndex |= 0b10;
		if (all.contains(new Point(current.x, current.y + 1)))
			tileIndex |= 0b100;
		if (all.contains(new Point(current.x + 1, current.y)))
			tileIndex |= 0b1000;
		return ROOM_AUTOTILE[tileIndex];
	}
}
