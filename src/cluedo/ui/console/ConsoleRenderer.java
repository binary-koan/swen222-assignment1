package cluedo.ui.console;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cluedo.game.Board;
import cluedo.game.Board.Direction;
import cluedo.game.Board.UnableToMoveException;
import cluedo.game.Door;
import cluedo.game.Game;
import cluedo.game.GameData;
import cluedo.game.Player;
import cluedo.game.Solution;
import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;

public class ConsoleRenderer {
	private static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
	
	private enum Result {
		NONE,
		WON,
		LOST
	}

	public static String getFilename(String defaultName) {
		System.out.println("Which setup would you like to use?");
		System.out.println("Enter a filename or press ENTER for default.");
		String line = readLine("> ");
		return line.isEmpty() ? defaultName : line;
	}

	private static String readLine(String prompt) {
		System.out.print(prompt);
		while(true){
			try {
				return consoleReader.readLine();
			} catch (IOException e) {
				throw new RuntimeException("Could not read from console");
			}
		}
	}
	
	private Game game;
	private BoardRenderer boardRenderer;

	public void run(Game game) {
		this.game = game;
		this.boardRenderer = new BoardRenderer(game.getBoard(), game.getData());

		for (Player player : queryPlayers(game.getData().getSuspects())) {
			game.addPlayer(player);
		}

		game.distributeCards();

		for (int turn = 1 ;; turn++) {
			System.out.println("======== Turn "+ turn +" ========");

			for(Player p : game.getPlayers()) {
				int diceRoll = (int) (Math.random() * 6);
				p.startTurn(diceRoll);
				System.out.println("Currently "+ p.getName() +"'s turn");
				System.out.println("What would you like to do?");
				Result result = doTurn(p);
				p.endTurn();
				
				if (result == Result.WON) {
					System.out.println("Player " + p.getName() + "wins!");
					return;
				}
				else if (result == Result.LOST) {
					game.removePlayer(p);
				}
			}
			
			if(game.getPlayers().size() == 0){
				System.out.println("All players have been knocked out! Game over.");
				return;
			}
		}
	}

	public List<Player> queryPlayers(List<Suspect> list) {
		List<Suspect> suspects = new ArrayList<Suspect>(list);

		int players = 0;

		while (players < 3 || players > suspects.size()) {
			System.out.println("How many players (3-" + suspects.size() + ")?");
			try {
				players = Integer.parseInt(readLine("> "));
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
			System.out.println("Player "+ i +" name?");
			String name = readLine("> ");							//player name funct, check needed?
			Suspect token = suspects.remove((int)(Math.random() * suspects.size()));
			result.add(new Player(name, token));
		}
		return result;
	}

	public Result doTurn(Player player){
		if(player.getRoom()!=null){
			System.out.println("You are located in " + player.getRoom().getName());
			Point center = player.getRoom().getCenterPoint();
			boardRenderer.drawBoard(center.y - 2, center.y + 2, game);
		}
		else{
			System.out.println("You are located in a corridor.");
			Point position = game.getBoard().getPlayerLocation(player);
			boardRenderer.drawBoard(position.y - 2, position.y + 2, game);
		}

		System.out.println("What would you like to do?");
		System.out.println("Your roll is " + player.getDiceRoll());
		
		Map<String, String> allowedActions = new HashMap<String, String>();
		allowedActions.put("m", "Move");
		allowedActions.put("s", "Make a suggestion");
		allowedActions.put("b", "Review the board");
		if (player.getRoom() != null && player.getRoom().getPassageExit() != null) {
			allowedActions.put("t", "Take secret passageway to " + player.getRoom().getPassageExit().getName());
		}
		allowedActions.put("e", "End your turn");

		while(true){
			String input = readLine("Move (m), make a suggestion (s), make an accusation (a), review your room (r)"
					+ "review the board (d), take secret passageway (t) or end your turn (e)?");
			if(input == "r") {
				displayRoom(player.getRoom());
			}
			else if(input == "d"){
				boardRenderer.drawBoard(game);
			}
			else if(input == "m"){
				move(player);								// /S/ ?? moving ends a players turn
			}
			else if(input == "t"){
				takePassage(player);
			}
			else if(input == "s"){
				makeSuggestion(player);
			}
			else if(input == "a") {
				Result result = makeAccusation(player);
				if (result != Result.NONE) {
					return result;
				}
			}
			else if(input == "e"){
				return Result.NONE;
			}
			else{
				System.out.println("Please select a valid option");
			}
		}
	}


	private void takePassage(Player player) {
		// Should probably be a special case which doesn't use up a move ...
		// I don't think that's in the spec so let's just do the easiest thing

		// /S/ Soo I just made it so that it doesnt use up a move
		// Buuut im still not sure how this should update the location in the
		// board class, almost tempted to integrate this as move method in board?

		if(player.getRoom() != null && player.getRoom().getPassageExit() != null){
			player.setRoom(player.getRoom().getPassageExit());
		}
		else {
			// Maybe try and get a more informative error message?
			// Like: if (not in room) { print that } else if (no secret passage) { print that } else { do stuff }
			if(player.getRoom() == null){
				System.out.println("You are not in a room!");
			}
			else if (player.getRoom().getPassageExit() == null){
				System.out.println("This room does not have a secret exit");
			}
		}
	}

	private Result makeAccusation(Player player) {
		while (true) {
			System.out.println("Make your accusation in the following format: suspect, room, weapon.");
			System.out.println("  eg. Mrs. Peacock, Hall, Candlestick");
			System.out.println("Alternatively, press Enter without typing to cancel.");
			displaySuspects();
			displayRooms();
			displayWeapons();
			
			String accusation = readLine("> ");
			if (accusation == null || accusation.isEmpty()) {
				return Result.NONE;
			}
			
			String[] elements = accusation.split("\\s+");
			if (elements.length != 3) {
				System.out.println("Make sure you include a suspect, room and weapon in the required format!");
				continue;
			}
			
			Suspect suspect = game.getData().getSuspect(elements[0]);
			Room room = game.getData().getRoom(elements[1]);
			Weapon weapon = game.getData().getWeapon(elements[2]);
			if (suspect == null || weapon == null || weapon == null) {
				System.out.println("Make sure you include a suspect, room and weapon in the required format!");
				continue;
			}
			
			Solution solution = game.getSolution();
			if (suspect == solution.getSuspect() && room == solution.getRoom() && weapon == solution.getWeapon()) {
				return Result.WON;
			}
			else {
				return Result.LOST;
			}
		}
	}

	private void makeSuggestion(Player player) {
		if(player.getRoom() == null){
			System.out.println("You must be in a room to make a suggestion");
			return;
		}
		while (true) {
			System.out.println("You are suggesting that the murder was committed in the " + player.getRoom().getName());
			System.out.println("Make your accusation in the following format: suspect, weapon.");
			System.out.println("  eg. Mrs. Peacock, Candlestick");
			System.out.println("Alternatively, press Enter without typing to cancel.");
			displaySuspects();
			displayWeapons();
			
			String accusation = readLine("> ");
			if (accusation == null || accusation.isEmpty()) {
				return;
			}
	
			String[] elements = accusation.split("\\s+");
			if (elements.length != 2) {
				System.out.println("Make sure you include a suspect and a weapon in the required format!");
				continue;
			}
			
			Suspect suspect = game.getData().getSuspect(elements[0]);
			Weapon weapon = game.getData().getWeapon(elements[1]);
			if (suspect == null || weapon == null) {
				System.out.println("Make sure you include a suspect and a weapon in the required format!");
				continue;
			}
			
			Game.Disprover disprover = game.disproveSuggestion(suspect, player.getRoom(), weapon);
			if (disprover == null) {
				System.out.println("No one was able to disprove your suggestion.");
			}
			else {
				System.out.println("Your suggestion was proved incorrect by "+ disprover.getPlayer().getName());
				System.out.println("They are holding card "+ disprover.getCard().getName() +"- note this down.");
			}
		}
	}

	private void move(Player player) {
		// Would it make sense to only be able to move once? Like, once you type "m" you're not allowed to move again?

		// /S/ Ok so apparently if you move into a room it ends your turn, and I dont see why if you would move anywhere
		// if youre about to make an accusation, so I just made move() end the player's turn in doTurn.

//		if(player.getMovesLeft() <= 0){
//			System.out.println("You have already used up all your moves.");
//			return;
//		}

		while (true) {
			Door door = null;

			if(player.getRoom() != null){
				System.out.println("You are in the " + player.getRoom().getName());
				displayRoom(player.getRoom());
				System.out.println("Enter the door you wish to leave from");
				try{
					int d = Integer.parseInt(readLine("> "));
					door = player.getRoom().getDoor(d - 1);
				} catch (NumberFormatException e) {
					System.out.println("Please enter a number!");
				}
			}

			List<Direction> directions = new ArrayList<Direction>();
			System.out.println("Enter the directions you wish to take (no spaces): Up (u), Left (l), Down (d), Right (r)");
			String dirString = readLine(">");

			if (dirString.length() > player.getDiceRoll()) {
				System.out.println("You can't move more than " + player.getDiceRoll() + " steps!");
				continue;
			}
			else if (!Pattern.matches("^[uldr]+$", dirString)) {
				System.out.println("Make sure you only enter single-character directions (u, l, r and d)");
				continue;
			}

			for(int i = 0; i < dirString.length(); i++){
				char dir = dirString.charAt(i);
	
				if (dir == 'u') {
					directions.add(Direction.NORTH);
				}
				else if (dir == 'l') {
					directions.add(Direction.EAST);
				}
				else if (dir == 'd') {
					directions.add(Direction.SOUTH);
				}
				else {
					directions.add(Direction.WEST);
				}
			}

			try {
				game.getBoard().movePlayer(player, directions, door);
			} catch (UnableToMoveException e) {
				System.out.println(e.getMessage());
				continue;
			}
		}
	}

	private void displayRoom(Room room) {
		if(room == null){
			System.out.println("You are not in a room");
			return;
		}

		ArrayList<Integer> xPoints = new ArrayList<Integer>();
		ArrayList<Integer> yPoints = new ArrayList<Integer>();

		//compute smallest area that contains all room points
		//forgot to use a bounding box...
		//way ive done it seems kind of verbose...
		for(Point p : room.getPoints()){
			xPoints.add((int) p.getX());
			yPoints.add((int) p.getY());
		}
		int minX = Collections.min(xPoints);
		int maxX = Collections.max(xPoints);
		int minY = Collections.min(yPoints);
		int maxY = Collections.max(yPoints);
		//bounding box lengths
		int width = maxX - minX;
		int height = maxY - minY;

		char roomChars[][] = new char[height][width];

		//=========alternate way?

		Rectangle boundingBox = new Rectangle(minX, minY, width, height);
		//not sure if its easier this way?


		//=========alternate way?

		//adding characters
		for(int i = minY; i < maxY; i++){
			for(int j = minX; j<maxX; i++){							// /S/
				if(room.getPoints().contains(new Point(i, j))){		//Ok will this actually compare it properly
					roomChars[i][j] = room.getName().charAt(0);		//and return it, as i am making a new point?
				}
				else{
					roomChars[i][j] = ' ';							//space outside room
				}
			}
		}
		for(Door door : room.getDoors()){
			roomChars[door.getLocation().y][door.getLocation().x] = (char) door.getDisplayNumber();
		}

		//printing room
		for(int i = 0; i < height; i++){
			for(int j = 0; j < height; j++){
				System.out.println(roomChars[i][j]);
			}
		}
	}

	private void displaySuspects() {
		System.out.println("Suspects");
		for(Map.Entry<Character, Suspect> entry : game.getData().getSuspectsById().entrySet()) {
			System.out.println("  " + entry.getKey() + " : " + entry.getValue().getName());
		}
	}
	
	private void displayRooms() {
		System.out.println("Rooms");
		for(Room room : game.getData().getRooms()) {
			System.out.println("  - " + room.getName());
		}
	}
	
	private void displayWeapons() {
		System.out.println("Weapons");
		for(Weapon weapon : game.getData().getWeapons()) {
			System.out.println("  - " + weapon.getName());
		}
	}
}
