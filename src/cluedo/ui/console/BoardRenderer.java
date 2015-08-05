package cluedo.ui.console;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import cluedo.game.Board;
import cluedo.game.Door;
import cluedo.game.Game;
import cluedo.game.GameData;
import cluedo.game.Player;
import cluedo.game.objects.Room;

public class BoardRenderer {
	private static String SPACE = "\u00A0";
	private static String BLANK = SPACE+SPACE+SPACE;

	private Board board;
	private StringBuilder[] boardBase;

	public BoardRenderer(Board board, GameData data) {
		this.board = board;
		calculateBoardBase(data);
	}

	public void drawRoomWithExits(Room room, Game game) {
		if(room == null) {
			System.out.println("You are not in a room");
			return;
		}

		Room.BoundingBox boundingBox = room.getBoundingBox();
		StringBuilder[] roomDisplay = new StringBuilder[boundingBox.getMaxY() - boundingBox.getMinY() + 1];
		for (Point point : room.getPoints()) {
			int y = point.y - boundingBox.getMinY();
			if (roomDisplay[y] == null) {
				roomDisplay[y] = new StringBuilder();
				for (int x = boundingBox.getMinX(); x <= boundingBox.getMaxX(); x++) {
					appendTile(roomDisplay[y], BLANK);
				}
			}
			setTile(roomDisplay[y], point.x - boundingBox.getMinX(), roomPointTile(point, room.getPoints()));
		}

		drawRoomName(room, boundingBox.getMinX(), boundingBox.getMinY(), roomDisplay);
		drawRoomContents(room, boundingBox.getMinX(), boundingBox.getMinY(), roomDisplay, game);

		int i = 1;
		for (Door door : room.getDoors()) {
			drawDoor(door, boundingBox.getMinX(), boundingBox.getMinY(), roomDisplay, Integer.toString(i));
			i++;
		}

		for (StringBuilder builder : roomDisplay) {
			System.out.println(builder.toString());
		}
	}

	public void drawBoard(Game game) {
		drawBoard(0, board.getHeight(), game);
	}

	public void drawBoard(int startY, int endY, Game game) {
		startY = Math.max(0,  startY);
		endY = Math.min(boardBase.length, endY);

		String[] board = buildCurrentBoard(game);
		for (int y = startY; y < endY; y++) {
			System.out.println(board[y]);
		}
	}

	private void calculateBoardBase(GameData data) {
		int height = board.getHeight();
		int width = board.getWidth();

		boardBase = new StringBuilder[height];
		for (int y = 0; y < height; y++) {
			boardBase[y] = new StringBuilder(board.getWidth());
			for (int x = 0; x < width; x++) {
				if (board.isCorridor(x, y)) {
					appendTile(boardBase[y], SPACE+'\u00B7'+SPACE);
				}
				else {
					appendTile(boardBase[y], BLANK);
				}
			}
		}
		for (Room room : data.getRooms()) {
			for (Point point : room.getPoints()) {
				setTile(boardBase[point.y], point.x, roomPointTile(point, room.getPoints()));
			}
			for (Door door : room.getDoors()) {
				drawDoor(door, 0, 0, boardBase, "*");
			}
			drawRoomName(room, 0, 0, boardBase);
		}
	}

	private void drawDoor(Door door, int baseX, int baseY, StringBuilder[] boardBase, String rep) {
		Point location = door.getLocation();
		int x = location.x - baseX;
		int y = location.y - baseY;

		if (door.isVertical()) {
			if (location.x == door.getRoom().getBoundingBox().getMinX()) {
				// Left of room
				setTile(boardBase[y], x, rep+SPACE+SPACE);
			}
			else {
				// Right of room
				setTile(boardBase[y], x, SPACE+SPACE+rep);
			}
		}
		else if (rep.equals("*")) {
			// Top or bottom of room
			setTile(boardBase[y], x, "***");
		}
		else {
			setTile(boardBase[y], x, SPACE+rep+SPACE);
		}
	}

	private void drawRoomName(Room room, int baseX, int baseY, StringBuilder[] base) {
		Point center = room.getCenterPoint();
		String name = room.getName();

		int startX = (center.x * 3) - baseX - (name.length() / 2) + 1;
		int endX = startX + name.length();

		StringBuilder nameRow = base[center.y - baseY];
		for(int realX = startX; realX < endX; ++realX) {
			nameRow.setCharAt(realX, name.charAt(realX - startX));
		}
	}

	private void drawRoomContents(Room room, int baseX, int baseY, StringBuilder[] base, Game game) {
		Point center = room.getCenterPoint();
		String contents = "(";
		if (room.getWeapon() != null) {
			contents += room.getWeapon().getName() + "; ";
		}
		for (Player player : game.getPlayers()) {
			if (room.equals(player.getRoom())) {
				contents += player.getToken().getIdentifier() + ", ";
			}
		}
		if (contents.equals("(")) {
			return;
		}
		contents = contents.substring(0, contents.length() - 2) + ")";

		int startX = (center.x * 3) - baseX - (contents.length() / 2) + 1;
		int endX = startX + contents.length();

		StringBuilder contentsRow = boardBase[center.y + 1];
		for(int realX = startX; realX < endX; ++realX) {
			contentsRow.setCharAt(realX, contents.charAt(realX - startX));
		}
	}

	private String[] buildCurrentBoard(Game game) {
		Board board = game.getBoard();

		StringBuilder[] copies = new StringBuilder[board.getHeight()];
		for (int i = 0; i < boardBase.length; i++) {
			copies[i] = new StringBuilder(boardBase[i]);
		}

		for (Player player : game.getPlayers()) {
			if (!player.getInGame()) {
				continue;
			}
			Point location = board.getPlayerLocation(player);
			setTile(copies[location.y], location.x, SPACE+player.getToken().getIdentifier()+SPACE);
		}
		for (Room room : game.getData().getRooms()) {
			drawRoomContents(room, 0, 0, copies, game);
		}
		String[] result = new String[board.getHeight()];
		for (int i = 0; i < board.getHeight(); i++) {
			result[i] = copies[i].toString();
		}
		return result;
	}

	private void appendTile(StringBuilder row, String str) {
		appendTile(row, str.charAt(0), str.charAt(1), str.charAt(2));
	}

	private void appendTile(StringBuilder row, char first, char second, char third) {
		row.append(first);
		row.append(second);
		row.append(third);
	}

	private void setTile(StringBuilder row, int x, String str) {
		setTile(row, x, str.charAt(0), str.charAt(1), str.charAt(2));
	}

	private void setTile(StringBuilder row, int x, char first, char second, char third) {
		row.setCharAt((x * 3), first);
		row.setCharAt((x * 3) + 1, second);
		row.setCharAt((x * 3) + 2, third);
	}

	// A character is chosen from this list for each point in the room based on which
	// directions contain another point in that room. Directions are OR'd together
	// with 0001 = tile above, 0010 = tile left, 0100 = tile below, 1000 = tile right
	private static final String[] ROOM_AUTOTILE = {
		"???", "???", "???", "__/",                      // bottom right corner = 0011
		"???", "???", "\u203E\u203E\\", SPACE+SPACE+"|", // top right corner = 0110, right side = 0111
		"???", "\\__", "???", "___", "/\u203E\u203E",    // bottom left = 1001, bottom = 1011, top left = 1100
		"|"+SPACE+SPACE, "\u203E\u203E\u203E", BLANK     // left side = 1101, top = 1110, middle = 1111
	};

	private String roomPointTile(Point current, Set<Point> all) {
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
