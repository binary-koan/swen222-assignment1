package cluedo.ui;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cluedo.game.Board;
import cluedo.game.GameData;
import cluedo.game.Player;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.ui.base.Renderer;
import cluedo.ui.base.TurnController;

public class ConsoleRenderer implements Renderer {
	public class ConsoleTurnController implements TurnController {
	}
	
	private static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
	
	public static String getFilename(String defaultName) {
		System.out.println("Which setup would you like to use?");
		System.out.println("Enter a filename or press ENTER for default.");
		String line = readLine("> ");
		return line.isEmpty() ? defaultName : line;
	}
	
	private static String readLine(String prompt) {
		try {
			System.out.print(prompt);
			return consoleReader.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Could not read from console");
		}
	}

	private GameData data;
	private Board board;
	private StringBuilder[] boardBase;
	
	public ConsoleRenderer() { }
	
	@Override
	public void setup(GameData data, Board board) {
		this.data = data;
		this.board = board;
		calculateBoardBase();
	}
	
	@Override
	public List<Player> queryPlayers(GameData data) {
		List<Suspect> suspects = data.getSuspects();
		int players = 0;
		
		while (players < 3 || players > suspects.size()) {
			System.out.println("How many players (3-" + suspects.size() + ")?");
			try {
				players = Integer.parseInt(System.console().readLine("> "));
				if (players < 3 || players > 6) {
					System.out.println("Please enter a number between 3 and " + suspects.size());
				}
			}
			catch (NumberFormatException e) {
				System.out.println("Please enter a number!");
			}
		}
		
		List<Player> result = new ArrayList<Player>();
		for (int i = 0; i < players; i++) {
			Suspect token = suspects.remove((int)(Math.random() * suspects.size()));
//			result.add(new Player(token));
		}
		return result;
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
}
