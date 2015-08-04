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
import cluedo.ui.Renderer;

public class ConsoleRenderer implements Renderer {
	private static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

	private BoardRenderer boardRenderer;

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

	public void run(Game game) {
		this.game = game;
		this.boardRenderer = new BoardRenderer(game.getBoard(), game.getData());

		for (Player player : queryPlayers(game.getData().getSuspects())) {
			game.addPlayer(player);
		}

		game.assignHands();

		int turn = 1;

		while(true){
			System.out.println("======== Turn "+ turn +" ========");

			for(Player p: players){
				if(p.getInGame() == true){
					int diceRoll = (int) (Math.random() * 6);
					p.startTurn(diceRoll);
					System.out.println("Currently "+ p.getName() +"'s turn");
					System.out.println("What would you like to do?");
					doTurn(p);
					p.endTurn();
				}

				// Check if a player has won
				if(getWinningPlayer() != null){
					System.out.println("Player " + getWinningPlayer().getName() + "wins!");
					return;
				}

				// Check if all players have lost
				if(nullPlayers.size() == players.size()){
					System.out.println("All players have been knocked out! Game over.");
					return;
				}
			}
			turn++;
		}
	}

	private Game game;
	private static List<Player> players;
	private static ArrayList<Player> nullPlayers;
	private static Player winningPlayer;

	public List<Player> queryPlayers(List<Suspect> list) {
		List<Suspect> suspects = new ArrayList<Suspect>(list);

		int players = 0;

		while (players < 3 || players > suspects.size()) {
			boardRenderer.drawBoard(game);
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

	public void doTurn(Player player){
		if(player.getRoom()!=null){
			System.out.println("You are located in "+ player.getRoom().getName());
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

		while(true){
			String input = readLine("Move (m), make a suggestion (s), make an accusation (a), review your room (r)"
					+ "review the board (d), take secret passageway (t) or end your turn (e)?");
			if(input == "m"){
				move(player);
				return;								// /S/ ?? moving ends a players turn
			}
			else if(input == "s"){
				makeSuggestion(player);
				return;
			}
			else if(input == "a"){
				makeAccusation(player);
				return;
			}
			else if(input == "r"){
				displayRoom(player.getRoom());
			}
			else if(input == "d"){
				displayBoard();
			}
			else if(input == "t"){
				takePassage(player);
			}
			else if(input == "e"){
				return;
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

	private void displayBoard() {
		// TODO Auto-generated method stub

	}



	private void makeAccusation(Player player) {
		System.out.println("Make your accusation in the following format: <suspectId room weapon>, "
				+ "eg <g hall candlestick>");
		displayElements();

		String suspect = "";
		String room = "";
		String weapon = "";

		String accusation = readLine("> ");
		String[] elements = accusation.split("[\\s\\xA0]+"); //splits it by whitespace (i think/hope)

		if(elements.length > 0){
			suspect = elements[0];
			room = elements[1];
			weapon = elements[2];
		}

		try{
			for(Player p : players){
				for(Card c: p.getHand()){
					if(c.getName() == suspect || c.getName() == room || c.getName() == weapon){
						System.out.println("Your accusation was incorrect! You have lost.");
						player.setInGame(false);
						nullPlayers.add(player);
						return;
					}
				}
				//actual check with solution needed here, how to do without many ifs()?
				System.out.println("Your accussation was correct! You have won.");
				setWinningPlayer(player);
			}
		}catch(NullPointerException e){// /s/ is it actually nullpointer? what kind of exception?
			System.out.println("No such card(s). Please use the valid format");
		}




	}

	private void makeSuggestion(Player player) {
		if(player.getRoom() == null){
			System.out.println("You must be in a room to make a suggestion");
			return;
		}
		System.out.println("Make your suggestion in the following format: <suspectId weapon>, "
				+ "eg <g candlestick>");
		displayElements();

		String suspect = "";
		String room = player.getRoom().getName();
		String weapon = "";

		String suggestion = readLine("> ");
		String[] elements = suggestion.split("[\\s\\xA0]+"); //splits it by whitespace (i think/hope)

		if(elements.length > 0){
			suspect = game.getData().getSuspectsById().get(elements[0]).getName();
			weapon = elements[1];
		}

		try{
			for(Player p : players){
				for(Card c: p.getHand()){
					if(c.getName() == suspect || c.getName() == room || c.getName() == weapon){
						System.out.println("Your suggestion was proved incorrect by "+ p.getName());
						System.out.println("They are holding card "+ c.getName() +"- note this down.");
//						player.setCanSuggest(false);
						return;
					}
				}
			}
			System.out.println("No one was able to disprove your suggestion.");
		}catch(NullPointerException e){ // /s/ is it actually nullpointer? what kind of exception?
			System.out.println("No such card(s). Please use the valid format");
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

		Door door = null;

		displayRoom(player.getRoom());

		if(player.getRoom() != null){
			System.out.println("Enter the door you wish to leave from");
			try{
				int d =  Integer.parseInt(System.console().readLine("> "));
				door = player.getRoom().getDoor(d);
			}catch (NumberFormatException e) {
				System.out.println("Please enter a number!");
			}
		}

		List<Direction> directions = new ArrayList<Direction>();
		System.out.println("Enter the directions you wish to take (no spaces): NORTH(N), EAST(E), SOUTH(S), WEST(W)");
		String dir = System.console().readLine(">");

		// /S/ Player only has to enter one string, if they enter more than their turn amount the rest are ignored
		// Also if they enter invalid character that is ignored essentially apart from error message

		for(int i = player.getDiceRoll(); i > 0; i--){
			int j = 0;
			char singleDir = dir.charAt(j);

			switch(singleDir){ //TO DO: figure out directions/error handling
			case 'N':
				directions.add(Direction.NORTH);
			case 'E':
				directions.add(Direction.EAST);
			case 'S':
				directions.add(Direction.SOUTH);
			case 'W':
				directions.add(Direction.WEST);
			default :
				System.out.println("Please enter a valid direction (N, S, E or W)");
			}
			j ++;
		}

			try {
				game.getBoard().movePlayer(player, directions, door);

			} catch (UnableToMoveException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}										//Also are we redrawing the board every step?

	public Player getWinningPlayer(){
		return winningPlayer;
	}

	public static void setWinningPlayer(Player player){
		winningPlayer = player;
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

	//Convenience method for printing out collections
	private void displayElements(){
		System.out.println("Suspect --- Id");
		for(Map.Entry<Character, Suspect> id : game.getData().getSuspectsById().entrySet()){
			//line formating?
			System.out.print(game.getData().getSuspectsById().get(id).getName()+ "--- ");
			System.out.print(id);
		}
		System.out.println("");
		System.out.println("Rooms");
		for(Room room : game.getData().getRooms()){
			System.out.println(room.getName());
		}
		System.out.println("");
		System.out.println("Weapons");
		for(Weapon weapon: game.getData().getWeapons()){
			System.out.println(weapon.getName());
		}
		System.out.println("");
	}
}