package cluedo.ui;

import game.Solution;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cluedo.game.Board;
import cluedo.game.Door;
import cluedo.game.GameData;
import cluedo.game.Player;
import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
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
		System.out.print(prompt + " ");
		while(true){
			try {
				return consoleReader.readLine();
			} catch (IOException e) {
				throw new RuntimeException("Could not read from console");
			}
		}

	}

	private static int readNumber(String prompt) {
		System.out.print(prompt + " ");
		while (true) {
			try {
				String typed = consoleReader.readLine();
				if(typed != null){
					return Integer.parseInt(typed);
				}
			} catch (IOException e) {
				System.out.println("Please enter a valid number");
			}
		}
	}



	//Bad design to repeat enum?
	enum Direction {
		NORTH,
		EAST,
		SOUTH,
		WEST
	}








	private GameData data;
	private Board board;
	private StringBuilder[] boardBase;
	private static List<Player> players;				//should these be here?
	private static ArrayList<Player> nullPlayers;		//should these be here?
	private static Player winningPlayer;

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
			System.out.println("Player "+ i +" name?");
			String pname;
			pname = readLine("> ");
			Suspect token = suspects.remove((int)(Math.random() * suspects.size()));
			result.add(new Player(pname, token));
		}
		return result;
	}






	public Solution assignHands(ArrayList<Player> players){

		List<Weapon> weapons = data.getWeapons();
		List<Suspect> suspects = data.getSuspects();
		List<Room> rooms = data.getRooms();


		//method doing two things - bad design?
		//Will the following revert changes back to the arraylists?
		//also empty checks needed?

		Solution solution = new Solution();
		solution.addCard(suspects.remove((int)Math.random() * suspects.size()));
		solution.addCard(rooms.remove((int)Math.random() * rooms.size()));
		solution.addCard(weapons.remove((int)Math.random() * weapons.size()));

		while(!weapons.isEmpty() || !suspects.isEmpty() || !rooms.isEmpty()){
			for(Player p : players){
				if(!weapons.isEmpty()){
					p.addCardToHand(weapons.remove((int)Math.random() * weapons.size()));
				}
				if(!suspects.isEmpty()){
					p.addCardToHand(suspects.remove((int)Math.random() * suspects.size()));
				}
				if(rooms.isEmpty()){
					p.addCardToHand(rooms.remove((int)Math.random() * rooms.size()));
				}
			}
		}

		return solution;
	}





	public static void gameOptions(Player player, int diceRoll){

		if(player.getRoom()!=null){
			System.out.println("You are located in "+ player.getRoom().getName());
		}
		else{
			System.out.println("You are located in the corridor");
		}

		System.out.println("What would you like to do?");
		System.out.println("Your roll is "+ diceRoll);

		while(true){
			String input = readLine("Move (m), make a suggestion (s), make an accusation (a), review the board (r), "
					+ "take secret passageway (t) or end your turn (e)?");
			if(input == "m"){
				move(player);
			}
			else if(input == "s"){
				makeSuggestion(player);
			}
			else if(input == "a"){
				makeAccusation(player);
			}
			else if(input == "r"){
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

	private static void takePassage(Player player) {

		if(player.getRoom() != null && player.getRoom().getPassageExit() != null && player.canMove == true
				 && player.getMovesLeft() >= 1){
			player.setRoom(player.getRoom().getPassageExit());
			//TO DO: Change player location
			//TO DO: Draw player token in different room
			player.setMovesLeft(player.getMovesLeft() - 1);
		}
		else{
			System.out.println("Not in room/room has no secret passage exit/you have no move points left");
		}
	}

	private static void displayBoard() {
		// TODO Auto-generated method stub

	}

	private static void makeAccusation(Player player) {
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
			//actual check with solution needed here
			System.out.println("Your accussation was correct! You have won.");
			setWinningPlayer(player);

			//TO DO:set game state to ended
		}


	}

	private static void makeSuggestion(Player player) {
		if(player.inRoom() == false){
			System.out.println("You must be in a room to make a suggestion");
			return;
		}
		if(player.getCanSuggest() == false){
			System.out.println("You have already made a suggestion");
			return;
		}
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
					System.out.println("The incorrect card was "+ c.getName() +"- note this down.");
					player.setCanSuggest(false);
					return;
				}
			}
		}
		System.out.println("No one was able to disprove your suggestion");
	}

	private static void move(Player player) {
		List<Direction> directions = new ArrayList<Direction>();
		System.out.println("Enter the directions you wish to take: NORTH(1), EAST(2), SOUTH(3), WEST(4)");

		for(int i = player.getMovesLeft(); i > 0; i--){
			int dir = Integer.parseInt(System.console().readLine("> "));

			switch(dir){ //TO DO: figure out directions
			case 1:
				directions.add(null);
			case 2:
				directions.add(null);
			case 3:
				directions.add(null);
			case 4:
				directions.add(null);
			default :
				System.out.println("Please enter a direction");
			}
		}

		if(player.getRoom() != null){
			System.out.println("Enter the door you wish to leave from");
			Door door = new Door(System.console().readLine("> "));         //Not sure how weare doing the doors
			//board.movePlayer(player, directions, door);				   //Also need safety checks, static or not?
		}
		else{
			//board.movePlayer(player, directions); 						//static or not?
		}
	}

	public ArrayList<Player> getNullPlayers(){
		return nullPlayers;
	}

	public Player getWinningPlayer(){
		return winningPlayer;
	}

	public static void setWinningPlayer(Player player){
		winningPlayer = player;
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
