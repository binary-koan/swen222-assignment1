package cluedo.ui.console;

import java.awt.Point;

import cluedo.game.Board;
import cluedo.game.Game;
import cluedo.game.GameData;
import cluedo.game.Player;
import cluedo.game.objects.Room;

public class BoardRenderer {
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
					boardBase[y].append('\u2592'); // Medium shade
				}
				else {
					boardBase[y].append(' '); // Solid block
				}
			}
		}
		for (Room room : data.getRooms()) {
			for (Point point : room.getPoints()) {
				boardBase[point.y].setCharAt(point.x, '\u2588');
			}
//			drawRoomName(room, chars);
			//TODO: Draw doors
		}
	}

	private void drawRoomName(Room room, StringBuilder[] boardBase) {
//		Point center = room.getCenterPoint();
//		String name = room.getName();
//		int startX = center.x - (name.length() / 2);
//		int endX = startX + name.length();
//		for (int x = startX; x < endX; x++) {
//			chars[center.y][x] = Character.toString(name.charAt(x - startX));
//		}
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
}
