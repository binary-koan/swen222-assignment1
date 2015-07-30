package cluedo.ui;

import java.awt.Point;

import cluedo.game.Board;
import cluedo.game.GameData;
import cluedo.game.Player;
import cluedo.game.objects.Room;
import cluedo.ui.base.Renderer;
import cluedo.ui.base.TurnController;

public class ConsoleRenderer implements Renderer {
	private GameData data;
	private Board board;
	private StringBuilder[] boardBase;
	
	public ConsoleRenderer() { }
	
	public void setup(GameData data, Board board) {
		this.data = data;
		this.board = board;
		calculateBoardBase();
	}
	
	public void drawBoard() {
		drawBoard(0, board.getSize());
	}
	
	public void drawBoard(int startY, int endY) {
		String[] board = buildCurrentBoard();
		for (int y = startY; y < endY; y++) {
			System.out.println(board[y]);
		}
	}
	
	private void calculateBoardBase() {
		boardBase = new StringBuilder[board.getSize()];
		for (int y = 0; y < board.getSize(); y++) {
			boardBase[y] = new StringBuilder(board.getSize());
			for (int x = 0; x < board.getSize(); x++) {
				if (board.isCorridor(x, y)) {
					boardBase[y].setCharAt(x, '\u2592'); // Medium shade
				}
				else {
					boardBase[y].setCharAt(x, '\u2588'); // Solid block
				}
			}
		}
//		for (Room room : board.getRooms()) {
//			for (Point point : room.getPoints()) {
//				chars[point.y][point.x] = " ";
//			}
//			drawRoomName(room, chars);
//			//TODO: Draw doors
//		}
	}
	
	private void drawRoomName(Room room, String[][] chars) {
//		Point center = room.getCenterPoint();
//		String name = room.getName();
//		int startX = center.x - (name.length() / 2);
//		int endX = startX + name.length();
//		for (int x = startX; x < endX; x++) {
//			chars[center.y][x] = Character.toString(name.charAt(x - startX));
//		}
	}
	
	private String[] buildCurrentBoard() {
		StringBuilder[] copies = new StringBuilder[board.getSize()];
//		for (Player player : board.getPlayers()) {
//			Point location = board.getPlayerLocation(player);
//			//TODO: Handle players in rooms
//			if (copies[location.y] == null) {
//				copies[location.y] = new StringBuilder(boardBase[location.y]);
//			}
//			copies[location.y].setCharAt(location.x, player.getToken().getIdentifier());
//		}
		//TODO: Add weapons too
		String[] result = new String[board.getSize()];
		for (int i = 0; i < board.getSize(); i++) {
			result[i] = (copies[i] == null) ? boardBase[i].toString() : copies[i].toString();
		}
		return result;
	}
	
	public class ConsoleTurnController implements TurnController {
	}
}
