package cluedo.ui.console;

import java.awt.Point;
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
				drawDoor(door, boardBase);
			}
			drawRoomName(room, boardBase);
			//TODO: Draw doors
		}
	}
	
	// A character is chosen from this list for each point in the room based on which
	// directions contain another point in that room. Directions are OR'd together
	// with 0001 = tile above, 0010 = tile left, 0100 = tile below, 1000 = tile right
	private static final String[] ROOM_AUTOTILE = {
		"???", "???", "???", "__/",                   // bottom right corner = 0011
		"???", "???", "\u203E\u203E\\", SPACE+SPACE+"|",//"\u2595",   // top right corner = 0110, right side = 0111
		"???", "\\__", "???", "___", "/\u203E\u203E", // bottom left = 1001, bottom = 1011, top left = 1100
//		"\u258F"+SPACE+SPACE, "\u203E\u203E\u203E", BLANK       // left side = 1101, top = 1110, middle = 1111
		"|"+SPACE+SPACE, "\u203E\u203E\u203E", BLANK       // left side = 1101, top = 1110, middle = 1111
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

	private void drawDoor(Door door, StringBuilder[] boardBase) {
		Point location = door.getLocation();
		if (door.isVertical()) {
			if (location.x == door.getRoom().getBoundingBox().getMinX()) {
				// Left of room
				setTile(boardBase[location.y], location.x, "*"+SPACE+SPACE);
			}
			else {
				// Right of room
				setTile(boardBase[location.y], location.x, SPACE+SPACE+"*");
			}
		}
		else {
			// Top or bottom of room
			setTile(boardBase[location.y], location.x, "***");
		}
	}

	private void drawRoomName(Room room, StringBuilder[] boardBase) {
		Point center = room.getCenterPoint();
		String name = room.getName();
		
		System.out.println("Drawing room " + name);
		System.out.println(center);
		System.out.println(room.getBoundingBox());
		
		int startX = (center.x * 3) - (name.length() / 2);
		int endX = startX + name.length();
		
		StringBuilder nameRow = boardBase[center.y];
		for(int realX = startX; realX < endX; ++realX) {
			nameRow.setCharAt(realX, name.charAt(realX - startX));
		}
	}

	private String[] buildCurrentBoard(Game game) {
		Board board = game.getBoard();

		StringBuilder[] copies = new StringBuilder[board.getHeight()];
		for (Player player : game.getPlayers()) {
			if (!player.getInGame()) {
				continue;
			}
			Point location = board.getPlayerLocation(player);
			//TODO: Handle players in rooms
			if (copies[location.y] == null) {
				copies[location.y] = new StringBuilder(boardBase[location.y]);
			}
			copies[location.y].setCharAt(location.x, player.getToken().getIdentifier());
		}
		//TODO: Add weapons too
		String[] result = new String[board.getHeight()];
		for (int i = 0; i < board.getHeight(); i++) {
			result[i] = (copies[i] == null) ? boardBase[i].toString() : copies[i].toString();
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
}
