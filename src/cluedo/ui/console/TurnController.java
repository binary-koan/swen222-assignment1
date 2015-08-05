package cluedo.ui.console;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cluedo.game.Door;
import cluedo.game.Game;
import cluedo.game.Player;
import cluedo.game.Solution;
import cluedo.game.Board.Direction;
import cluedo.game.Board.UnableToMoveException;
import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;

public class TurnController {
	public enum Result {
		NONE,
		WON,
		LOST
	}

	private Player player;
	private Game game;
	private BoardRenderer boardRenderer;

	public static Result run(Player player, ConsoleRenderer renderer) {
		return new TurnController(player, renderer).run();
	}

	public TurnController(Player player, ConsoleRenderer renderer) {
		this.player = player;
		this.game = renderer.getGame();
		this.boardRenderer = renderer.getBoardRenderer();
	}

	public Result run() {
		int diceRoll = (int)(Math.random() * 6) + 1;
		player.startTurn(diceRoll);

		System.out.println("Currently "+ player.getName() +"'s turn." +
				" You are " + player.getToken().getName() + " (" + player.getToken().getIdentifier() + ")");
		System.out.println("You rolled a " + player.getDiceRoll() + "!");

		Result result = doTurn();

		player.endTurn();

		return result;
	}

	private Result doTurn() {
		Map<Character, String> allowedActions = getAllowedActions();
		showPlayerPosition();
		System.out.println("What would you like to do?");

		while(true) {
			for (Map.Entry<Character, String> entry : allowedActions.entrySet()) {
				System.out.println("- " + entry.getValue() + " (" + entry.getKey() + ")");
			}

			char input = ConsoleRenderer.readLine("> ").charAt(0);
			if (!allowedActions.containsKey(input)) {
				System.out.println("You can't do that right now.\n" +
						"Make sure you enter the single character ID of the action you want to take.");
				continue;
			}

			if (input == 'm') {
				move();
				showPlayerPosition();
				allowedActions.remove('m');
			}
			else if(input == 't'){
				takePassage();
				showPlayerPosition();
				allowedActions.remove('t');
			}
			else if (input == 's'){
				makeSuggestion();
				return Result.NONE;
			}
			else if (input == 'a') {
				return makeAccusation();
			}
			else if (input == 'e'){
				return Result.NONE;
			}
			else if (input == 'b'){
				boardRenderer.drawBoard(game);
			}
			else if (input == 'h') {
				showHand();
			}

			System.out.println("What would you like to do now?");
		}
	}

	private Map<Character, String> getAllowedActions() {
		Map<Character, String> allowedActions = new LinkedHashMap<Character, String>();
		allowedActions.put('m', "Move");
		if (player.getRoom() != null) {
			allowedActions.put('s', "End your turn by making a suggestion");
		}
		allowedActions.put('a', "End your turn by making an accusation");
		if (player.getRoom() != null && player.getRoom().getPassageExit() != null) {
			allowedActions.put('t', "Take secret passageway to " + player.getRoom().getPassageExit().getName());
		}
		allowedActions.put('b', "Review the board");
		allowedActions.put('h', "View your hand");
		allowedActions.put('e', "End your turn without suggesting or accusing");
		return allowedActions;
	}

	private void showPlayerPosition() {
		if (player.getRoom()!=null) {
			System.out.println("You are in the " + player.getRoom().getName() + ".");
			Point center = player.getRoom().getCenterPoint();
			boardRenderer.drawBoard(center.y - 2, center.y + 3, game);
		}
		else {
			System.out.println("You are in a corridor.");
			Point position = game.getBoard().getPlayerLocation(player);
			boardRenderer.drawBoard(position.y - 2, position.y + 3, game);
		}
	}

	private void showHand() {
		System.out.println("You are holding:");
		for (Card card : player.getHand()) {
			System.out.println("- " + card.getName());
		}
	}

	private void takePassage() {
		if(player.getRoom() == null) {
			System.out.println("You are not in a room!");
		}
		else if (player.getRoom().getPassageExit() == null) {
			System.out.println("This room does not have a secret exit");
		}
		else {
			player.setRoom(player.getRoom().getPassageExit());
		}
	}

	private Result makeAccusation() {
		System.out.println("Make your accusation in the following format: suspect, room, weapon.");
		System.out.println("  eg. Mr. Beige, Bathroom, Shotgun");
		System.out.println("Alternatively, press Enter without typing to cancel.");
		displayCards(game.getData().getSuspects(), "Suspects");
		displayCards(game.getData().getRooms(), "Rooms");
		displayCards(game.getData().getWeapons(), "Weapons");

		while (true) {
			String accusation = ConsoleRenderer.readLine("> ");
			if (accusation == null || accusation.isEmpty()) {
				return Result.NONE;
			}

			Card[] elements = parseAccusation(accusation);
			if (elements == null) {
				System.out.println("Make sure you include a suspect, room and weapon in the required format!");
				continue;
			}

			Solution solution = game.getSolution();
			if (elements[0].equals(solution.getSuspect()) &&
					elements[1].equals(solution.getRoom()) &&
					elements[2].equals(solution.getWeapon())) {
				System.out.println("You are correct!");
				return Result.WON;
			}
			else {
				System.out.println("Sorry, that's not the solution.");
				return Result.LOST;
			}
		}
	}

	private void makeSuggestion() {
		if(player.getRoom() == null) {
			System.out.println("You must be in a room to make a suggestion");
			return;
		}
		while (true) {
			System.out.println("You are suggesting that the murder was committed in the " + player.getRoom().getName());
			System.out.println("Make your accusation in the following format: suspect, weapon.");
			System.out.println("  eg. Mr. Beige, Shotgun");
			System.out.println("Alternatively, press Enter without typing to cancel.");
			displayCards(game.getData().getSuspects(), "Suspects");
			displayCards(game.getData().getWeapons(), "Weapons");

			String suggestion = ConsoleRenderer.readLine("> ");
			if (suggestion == null || suggestion.isEmpty()) {
				return;
			}

			Card[] elements = parseSuggestion(suggestion);
			if (elements == null) {
				System.out.println("Make sure you include a suspect and a weapon in the required format!");
				continue;
			}

			Game.Disprover disprover = game.disproveSuggestion(elements[0], player.getRoom(), elements[1]);
			if (disprover == null) {
				System.out.println("No one was able to disprove your suggestion.");
			}
			else {
				System.out.println("Your suggestion was proved incorrect by "+ disprover.getPlayer().getName());
				System.out.println("They are holding card "+ disprover.getCard().getName() +"- note this down.");
			}
		}
	}

	private void move() {
		while (true) {
			Door door = null;
			if(player.getRoom() != null){
				door = queryLeavingDoor(player.getRoom());
			}

			List<Direction> directions = queryMovement();
			if (directions == null) {
				return;
			}

			try {
				game.getBoard().movePlayer(player, directions, door);
				break;
			} catch (UnableToMoveException e) {
				System.out.println(e.getMessage());
				continue;
			}
		}
	}

	private Door queryLeavingDoor(Room room) {
		System.out.println("You are in the " + room.getName());
		boardRenderer.drawRoomWithExits(room);
		System.out.println("Enter the door you wish to leave from");

		while (true) {
			try {
				int d = Integer.parseInt(ConsoleRenderer.readLine("> "));
				if (d < 1 || d > room.getDoors().size() + 1) {
					System.out.println("Please enter one of the visible door numbers.");
					continue;
				}
				return room.getDoor(d - 1);
			}
			catch (NumberFormatException e) {
				System.out.println("Please enter a number!");
			}
		}
	}

	private List<Direction> queryMovement() {
		while (true) {
			List<Direction> result = new ArrayList<Direction>();
			System.out.println("Enter up to " + player.getDiceRoll() + " steps you want to take, with no spaces.");
			System.out.println("Valid directions are (u)p, (l)eft, (d)own and (r)ight.");
			String dirString = ConsoleRenderer.readLine("> ");

			if (dirString == null || dirString.isEmpty()) {
				return null;
			}
			else if (dirString.length() > player.getDiceRoll()) {
				System.out.println("You can't move more than " + player.getDiceRoll() + " steps!");
				continue;
			}
			else if (!Pattern.matches("^[uldr]+$", dirString)) {
				System.out.println("Make sure you only enter single-character directions (u, l, r and d)");
				continue;
			}

			for(int i = 0; i < dirString.length(); i++){
				result.add(parseDirection(dirString.charAt(i)));
			}
			return result;
		}
	}

	private Direction parseDirection(char dir) {
		if (dir == 'u') {
			return Direction.UP;
		}
		else if (dir == 'l') {
			return Direction.LEFT;
		}
		else if (dir == 'd') {
			return Direction.DOWN;
		}
		else {
			return Direction.RIGHT;
		}
	}

	private void displayCards(Collection<? extends Card> cards, String title) {
		System.out.println(title + ":");
		String line = "  ";
		for (Suspect suspect : game.getData().getSuspectsById().values()) {
			line += suspect.getName();
			if (line.length() > 70) {
				System.out.println(line);
				line = "  ";
			}
			else {
				line += ", ";
			}
		}

		if (line.equals("  ")) {
			return;
		}
		else if (line.endsWith(", ")) {
			System.out.println(line.substring(0, line.length() - 2));
		}
		else {
			System.out.println(line);
		}
	}

	private Card[] parseSuggestion(String suggestion) {
		String[] parts = parseArray(suggestion, 2);
		return new Card[] {
			game.getData().getSuspect(parts[0]),
			game.getData().getWeapon(parts[1])
		};
	}

	private Card[] parseAccusation(String accusation) {
		String[] parts = parseArray(accusation, 3);
		return new Card[] {
			game.getData().getSuspect(parts[0]),
			game.getData().getRoom(parts[2]),
			game.getData().getWeapon(parts[1])
		};
	}

	private String[] parseArray(String accusation, int desiredLength) {
		String[] strings = accusation.split(",\\s+");
		if (strings.length != desiredLength) {
			return null;
		}
		return strings;
	}
}
