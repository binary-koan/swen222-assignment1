package cluedo.ui.console;

import java.awt.Point;
import java.awt.geom.Point2D;
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
import cluedo.game.objects.Weapon;

/**
 * An instance of this class is created per player per turn. It handles printing
 * the player's location and allowed actions, and processes selected actions
 */
public class TurnController {
	/**
	 * The result of the turn - whether the player won or lost as a result of
	 * their actions
	 */
	public enum Result {
		NONE, WON, LOST
	}

	private Player player;
	private Game game;
	private BoardRenderer boardRenderer;

	/**
	 * Create and run a new TurnController
	 *
	 * @param player
	 *            current player
	 * @param renderer
	 *            renderer to use for input and drawing
	 * @return the result of the turn
	 */
	public static Result run(Player player, ConsoleRenderer renderer) {
		return new TurnController(player, renderer).run();
	}

	/**
	 * Construct a new TurnController
	 *
	 * @param player
	 *            current player
	 * @param renderer
	 *            renderer to use for input and drawing
	 */
	public TurnController(Player player, ConsoleRenderer renderer) {
		this.player = player;
		this.game = renderer.getGame();
		this.boardRenderer = renderer.getBoardRenderer();
	}

	/**
	 * Run the controller and returns the result of the player's actions this
	 * turn
	 */
	public Result run() {
		int diceRoll = (int) (Math.random() * 6) + 1;
		player.startTurn(diceRoll);

		System.out.println("Currently " + player.getName() + "'s turn."
				+ " You are " + player.getToken().getName()
				+ " (" + player.getToken().getIdentifier() + ")");
		System.out.println("You rolled a " + player.getDieRoll() + "!");

		Result result = doTurn();

		player.endTurn();

		return result;
	}

	/**
	 * Process input and passes on actions to their respective handlers
	 */
	private Result doTurn() {
		Map<Character, String> allowedActions = getAllowedActions();
		showPlayerPosition();
		System.out.println("What would you like to do?");

		while (true) {
			for (Map.Entry<Character, String> entry : allowedActions.entrySet()) {
				System.out.println("- " + entry.getValue() + " (" + entry.getKey() + ")");
			}

			String line = ConsoleRenderer.readLine("> ");
			if (line.length() == 0) {
				System.out.println("Please enter the single character ID of the action you want to take.");
				continue;
			}

			char input = line.charAt(0);
			if (!allowedActions.containsKey(input)) {
				System.out.println("You can't do that right now.\n"
						+ "Make sure you enter the single character ID of the action you want to take.");
				continue;
			}

			if (input == 'm') {
				if (move()) {
					showPlayerPosition();
					allowedActions.remove('m');
					if (player.getRoom() != null) {
						allowedActions.put('s', "End your turn by making a suggestion");
					}
					else {
						allowedActions.remove('s');
					}
				}
			}
			else if (input == 't') {
				takePassage();
				showPlayerPosition();
				allowedActions.remove('t');
			}
			else if (input == 's') {
				makeSuggestion();
				return Result.NONE;
			}
			else if (input == 'a') {
				return makeAccusation();
			}
			else if (input == 'e') {
				return Result.NONE;
			}
			else if (input == 'b') {
				boardRenderer.drawBoard(game);
			}
			else if (input == 'h') {
				showHand();
			}

			System.out.println("\nWhat would you like to do now?");
		}
	}

	/**
	 * Return the actions the player is allowed to take in the current context
	 */
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

	/**
	 * Print the player's current room (if it exists) or a segment of the board
	 * containing their token
	 */
	private void showPlayerPosition() {
		if (player.getRoom() != null) {
			System.out.println("You are in the " + player.getRoom().getName() + ".");
			Point2D.Float center = player.getRoom().getCenterPoint();
			boardRenderer.drawBoard(Math.round(center.y) - 2, Math.round(center.y) + 3, game);
		}
		else {
			System.out.println("You are in a corridor.");
			Point position = game.getBoard().getPlayerLocation(player);
			boardRenderer.drawBoard(position.y - 2, position.y + 3, game);
		}
	}

	/**
	 * Print the cards the player is currently holding
	 */
	private void showHand() {
		System.out.println("You are holding:");
		for (Card card : player.getHand()) {
			System.out.println("- " + card.getName());
		}
	}

	/**
	 * Set the player's location to the opposite end of the passage leading from
	 * their current room
	 */
	private void takePassage() {
		if (player.getRoom() == null) {
			System.out.println("You are not in a room!");
		}
		else if (player.getRoom().getPassageExit() == null) {
			System.out.println("This room does not have a secret exit");
		}
		else {
			player.setRoom(player.getRoom().getPassageExit());
		}
	}

	/**
	 * Get a suggestion from the player and attempts to disprove it, printing
	 * out the result
	 */
	private void makeSuggestion() {
		if (player.getRoom() == null) {
			System.out.println("You must be in a room to make a suggestion");
			return;
		}
		while (true) {
			System.out.println("You are suggesting that the murder was committed in the " + player.getRoom().getName());
			System.out.println("Make your accusation in the following format: suspect, weapon.");
			System.out.println("  eg. Mr. Beige, Shotgun");
			System.out.println("Alternatively, press Enter without typing to cancel.\n");
			displayCards(game.getData().getSuspects(), "Suspects");
			displayCards(game.getData().getWeapons(), "Weapons");

			String suggestionString = ConsoleRenderer.readLine("> ");
			if (suggestionString == null || suggestionString.isEmpty()) {
				return;
			}

			Game.Suggestion suggestion = parseSuggestion(suggestionString, player);
			if (suggestion == null) {
				System.out.println("Make sure you include a suspect and a weapon in the required format!");
				continue;
			}

			Game.Disprover disprover = game.disproveSuggestion(player, suggestion);
			if (disprover == null) {
				System.out.println("No one was able to disprove your suggestion.");
			}
			else {
				if (disprover.getPlayer().equals(player)) {
					System.out.println("You can prove your own suggestion incorrect.");
					System.out.println("You are holding card " + disprover.getCard().getName());
				}
				else {
					System.out.println("Your suggestion was proved incorrect by " + disprover.getPlayer().getName());
					System.out.println("They are holding card " + disprover.getCard().getName() + " - note this down.");
				}
				return;
			}
		}
	}

	/**
	 * Get an accusation from the player, validates it against the solution and
	 * returns the result
	 */
	private Result makeAccusation() {
		System.out.println("Make your accusation in the following format: suspect, room, weapon.");
		System.out.println("  eg. Mr. Beige, Bathroom, Shotgun");
		System.out.println("Alternatively, press Enter without typing to cancel.\n");
		displayCards(game.getData().getSuspects(), "Suspects");
		displayCards(game.getData().getRooms(), "Rooms");
		displayCards(game.getData().getWeapons(), "Weapons");

		while (true) {
			String accusationString = ConsoleRenderer.readLine("> ");
			if (accusationString == null || accusationString.isEmpty()) {
				return Result.NONE;
			}

			Game.Suggestion accusation = parseAccusation(accusationString);
			if (accusation == null) {
				System.out
						.println("Make sure you include a suspect, room and weapon in the required format!");
				continue;
			}

			Solution solution = game.getSolution();
			if (game.getSolution().checkAgainst(accusation)) {
				System.out.println("You are correct!");
				return Result.WON;
			}
			else {
				System.out.println("Sorry, that's not the solution.");
				return Result.LOST;
			}
		}
	}

	/**
	 * Get movement directions from the player and moves them based on that
	 * input
	 */
	private boolean move() {
		while (true) {
			Door door = null;
			if (player.getRoom() != null) {
				door = queryLeavingDoor(player.getRoom());
			}

			List<Direction> directions = queryMovement();
			if (directions == null) {
				return false;
			}

			try {
				game.getBoard().movePlayer(player, directions, door);
				break;
			}
			catch (UnableToMoveException e) {
				System.out.println(e.getMessage());
				continue;
			}
		}
		return true;
	}

	/**
	 * Ask the player which door they want to leave their room from, and return
	 * the result
	 *
	 * @param room
	 *            the player's current room
	 */
	private Door queryLeavingDoor(Room room) {
		System.out.println("You are in the " + room.getName());
		boardRenderer.drawRoomWithExits(room, game);
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

	/**
	 * Get a list of directions from the player
	 */
	private List<Direction> queryMovement() {
		while (true) {
			List<Direction> result = new ArrayList<Direction>();
			System.out.println("Enter up to " + player.getDieRoll() + " steps you want to take, with no spaces.");
			System.out.println("Valid directions are (u)p, (l)eft, (d)own and (r)ight.");
			String dirString = ConsoleRenderer.readLine("> ");

			if (dirString == null || dirString.isEmpty()) {
				return null;
			}
			else if (dirString.length() > player.getDieRoll()) {
				System.out.println("You can't move more than " + player.getDieRoll() + " steps!");
				continue;
			}
			else if (!Pattern.matches("^[uldr]+$", dirString)) {
				System.out.println("Make sure you only enter single-character directions (u, l, r and d)");
				continue;
			}

			for (int i = 0; i < dirString.length(); i++) {
				result.add(parseDirection(dirString.charAt(i)));
			}
			return result;
		}
	}

	/**
	 * Parse the given character as a direction (assumes it is one of {uldr})
	 */
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

	/**
	 * Displays the given collection of cards
	 */
	private void displayCards(Collection<? extends Card> cards, String title) {
		System.out.println(title + ":");
		String line = "  ";
		for (Card card : cards) {
			line += card.getName();
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

	/**
	 * Parses a suggestion given by the player as input
	 *
	 * @return the suspect and weapon that were suggested
	 */
	private Game.Suggestion parseSuggestion(String suggestion, Player player) {
		String[] parts = parseArray(suggestion, 2);
		Suspect suspect = game.getData().getSuspect(parts[0]);
		Weapon weapon = game.getData().getWeapon(parts[1]);
		if (suspect == null || weapon == null) {
			return null;
		}
		else {
			return new Game.Suggestion(suspect, weapon, player.getRoom());
		}
	}

	/**
	 * Parses an accusation given by the player as input
	 *
	 * @return the suspect, room and weapon that were suggested
	 */
	private Game.Suggestion parseAccusation(String accusation) {
		String[] parts = parseArray(accusation, 3);
		Suspect suspect = game.getData().getSuspect(parts[0]);
		Room room = game.getData().getRoom(parts[1]);
		Weapon weapon = game.getData().getWeapon(parts[2]);
		if (suspect == null || room == null || weapon == null) {
			return null;
		}
		else {
			return new Game.Suggestion(suspect, weapon, room);
		}
	}

	/**
	 * Assume the string is a comma-separated array and split it into its parts
	 *
	 * @param input
	 *            the string to split
	 * @param desiredLength
	 *            length to check the result against
	 * @return the resulting array of strings, or null if it is not of the
	 *         desired length
	 */
	private String[] parseArray(String input, int desiredLength) {
		String[] strings = input.split(",\\s+");
		if (strings.length != desiredLength) {
			return null;
		}
		return strings;
	}
}
