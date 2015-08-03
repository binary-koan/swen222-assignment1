package cluedo.ui;

import game.Solution;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.ui.base.Renderer;

public class ConsoleRenderer implements Renderer {
	private static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

	public static String getFilename(String defaultName) {
		System.out.println("Which setup would you like to use?");
		System.out.println("Enter a filename or press ENTER for default.");
		String line = readLine("> ");
		return line.isEmpty() ? defaultName : line;
	}

	private static String readLine(String prompt) {
		System.out.print(prompt + " ");
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
		calculateBoardBase();

		for (Player player : queryPlayers(game.getData().getSuspects())) {
			game.addPlayer(player);
		}

		game.assignHands(); // remove cast?

		int turn = 1;
//		int i = 0;

		while(true){
			System.out.println("======== Turn "+ turn +" ========");

			for(Player p: players){
				if(p.getInGame() == true){
					int diceRoll = (int) (Math.random() * 6);
					p.startTurn(diceRoll);
					System.out.println("Currently "+ p.getName() +"'s turn");
					System.out.println("What would you like to do?");
//					p.setCanSuggest(true);	// not needed if suggesting is the last thing in a turn
//					p.setMovesLeft(diceRoll); // player now knows about dice roll from startTurn()
					doTurn(p); //remove cast?
					p.endTurn();
				}
				//If all players have made an incorrect guess and are knocked out.
				//If a player has won
				//Should these be here? Seems kind of messy
				// Probably fine :)
				if(getWinningPlayer() != null){
					System.out.println("Player " + getWinningPlayer().getName() + "wins!");
					return;
				}

				if(nullPlayers.size() == players.size()){
					System.out.println("All players have been knocked out! Game over.");
					return;
				}
			}
			turn++;
		}
	}

	private StringBuilder[] boardBase;
	private Game game;
	private static List<Player> players;				//should these be here?
	private static ArrayList<Player> nullPlayers;		//should these be here?
	private static Player winningPlayer;

	public List<Player> queryPlayers(List<Suspect> list) {
		List<Suspect> suspects = new ArrayList<Suspect>(list);

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
			System.out.println("Player "+ i +" name?");
			String pname;
			pname = readLine("> ");							//player name funct, check needed?
			Suspect token = suspects.remove((int)(Math.random() * suspects.size()));
			result.add(new Player(pname, token));
		}
		return result;
	}





	public void doTurn(Player player){

		if(player.getRoom()!=null){
			System.out.println("You are located in "+ player.getRoom().getName());
		}
		else{
			System.out.println("You are located in the corridor");
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

		if(player.getRoom() != null && player.getRoom().getPassageExit() != null){
			player.setRoom(player.getRoom().getPassageExit());
			//TO DO: Change player location
			//TO DO: Draw player token in different room
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
		System.out.println("Make your accusation in the following order: suspect (eg ***properformat***), room, weapon");
		String suspect;
		String room;
		String weapon;

		suspect = System.console().readLine("> ");
		room = System.console().readLine("> ");
		weapon = System.console().readLine("> ");
		//To Do: check for proper format

		for(Player p : players){
			for(Card c: p.getHand()){
				if(c.getName() == suspect || c.getName() == room || c.getName() == weapon){
					System.out.println("Your accusation was incorrect! You have lost.");

					player.setInGame(false);//TO DO: player state set null
					nullPlayers.add(player);
					return;
				}
			}
			//actual check with solution needed here, how to do without many ifs()?

			System.out.println("Your accussation was correct! You have won.");
			setWinningPlayer(player);

			//TO DO:set game state to ended
		}


	}

	private void makeSuggestion(Player player) {
		if(player.getRoom() == null){
			System.out.println("You must be in a room to make a suggestion");
			return;
		}
		// Shouldn't be needed if it's the last thing in a turn
//		if(player.getCanSuggest() == false){
//			System.out.println("You have already made a suggestion");
//			return;
//		}
		System.out.println("Make your suggestion in the following order: suspect (eg ***properformat***), weapon");
		String suspect;
		String room = player.getRoom().getName();
		String weapon;

		suspect = System.console().readLine("> ");
		weapon = System.console().readLine("> ");
		//To Do: check for proper format

		for(Player p : players){
			for(Card c: p.getHand()){
				if(c.getName() == suspect || c.getName() == room || c.getName() == weapon){
					System.out.println("Your suggestion was proved incorrect by "+ p.getName());
					System.out.println("They are holding card "+ c.getName() +"- note this down.");
//					player.setCanSuggest(false);
					return;
				}
			}
		}
		System.out.println("No one was able to disprove your suggestion.");
//		player.setCanSuggest(false);
	}

	private void move(Player player) {
		// Would it make sense to only be able to move once? Like, once you type "m" you're not allowed to move again?

		// /S/ Ok so apparently if you move into a room it ends your turn, and I dont see why if you would move anywhere
		// if youre about to make an accusation, so I just made move end the player's turn in doTurn.

//		if(player.getMovesLeft() <= 0){
//			System.out.println("You have already used up all your moves.");
//			return;
//		}
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
		if(player.getRoom() != null){
			System.out.println("Enter the door you wish to leave from");
//			Door door = new Door(null, null);         //Not sure how weare doing the doors
//			Door door = player.getRoom().getDoor(...);
//			try {
//				board.movePlayer(player, directions, door);
//			} catch (UnableToMoveException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}				   //Also need safety checks, static or not?
		}																	//Also are we redrawing the board?
		else{																//What if player collides in wall?
			//board.movePlayer(player, directions); 						//static or not?
		}
	}



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
		int minX = 99999999;
		int maxX = 0;
		int minY = 99999999;
		int maxY = 0;


		//compute smallest area that contains all room points
		//forgot to use a bounding box...
		//way ive done it seems kind of verbose...
		for(Point p : room.getPoints()){
			if(p.getX() < minX){
				minX = (int) p.getX();
			}
			if(p.getX() > maxX){
				maxX = (int) p.getX();
			}
			if(p.getY() < minY){
				minY = (int) p.getY();
			}
			if(p.getY() > minY){
				maxY = (int) p.getY();
			}
		}

		//bounding box lengths
		int width = maxX - minX;
		int height = maxY - minY;

		char roomChars[][] = new char[height][width];

		//=========alternate



		//=========alternate

		//adding characters
		for(int i = 0; i < maxY; i++){
			for(int j = 0; j<maxX; i++){							// /S/
				if(room.getPoints().contains(new Point(i, j))){		//Ok will this actually compare it properly
					roomChars[i][j] = room.getName().charAt(0);		//and return it, as i am making a new point?
				}
				else{
					roomChars[i][j] = ' ';							//space outside room
				}
			}
		}
		for(Door door : room.getDoors()){
			if(door.isVertical() == true){
				roomChars[door.getLocation().y][door.getLocation().x] = '/';
			}
			else{
				roomChars[door.getLocation().y][door.getLocation().x] = '_';
			}
		}

		//printing room
		for(int i = 0; i < height; i++){
			for(int j = 0; j < height; j++){
				System.out.println(roomChars[i][j]);
			}
		}
	}







	public void drawBoard() {
//		drawBoard(0, game.getBoard().getSize());
	}

	public void drawBoard(int startY, int endY) {
		String[] board = buildCurrentBoard();
		for (int y = startY; y < endY; y++) {
			System.out.println(board[y]);
		}
	}

	private void calculateBoardBase() {
//		boardBase = new StringBuilder[board.getSize()];
//		for (int y = 0; y < board.getSize(); y++) {
//			boardBase[y] = new StringBuilder(board.getSize());
//			for (int x = 0; x < board.getSize(); x++) {
//				if (board.isCorridor(x, y)) {
//					boardBase[y].setCharAt(x, '\u2592'); // Medium shade
//				}
//				else {
//					boardBase[y].setCharAt(x, '\u2588'); // Solid block
//				}
//			}
//		}
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
//		StringBuilder[] copies = new StringBuilder[board.getSize()];
//		for (Player player : board.getPlayers()) {
//			Point location = board.getPlayerLocation(player);
//			//TODO: Handle players in rooms
//			if (copies[location.y] == null) {
//				copies[location.y] = new StringBuilder(boardBase[location.y]);
//			}
//			copies[location.y].setCharAt(location.x, player.getToken().getIdentifier());
//		}
		//TODO: Add weapons too
//		String[] result = new String[board.getSize()];
//		for (int i = 0; i < board.getSize(); i++) {
//			result[i] = (copies[i] == null) ? boardBase[i].toString() : copies[i].toString();
//		}
//		return result;
		return null;
	}
}
