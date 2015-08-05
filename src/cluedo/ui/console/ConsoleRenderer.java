package cluedo.ui.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cluedo.game.Game;
import cluedo.game.Player;
import cluedo.game.objects.Suspect;

/**
 * Renders a text based version of the game, and allows gameplay via console
 * commands
 */
public class ConsoleRenderer {
	private static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * Ask the user for a filename
	 *
	 * @param defaultName
	 *            default filename to use
	 * @return the provided filename, or defaultName if none is entered
	 */
	public static String queryFilename(String defaultName) {
		System.out.println("Which setup would you like to use?");
		System.out.println("Enter a filename or press ENTER for default.");
		String line = readLine("> ");
		return line.isEmpty() ? defaultName : line;
	}

	/**
	 * Read a line from the console (utility method to make sure reading input
	 * works cross-platform)
	 *
	 * @param prompt
	 *            string to print at the start of the input line
	 * @return the string that was read
	 */
	public static String readLine(String prompt) {
		System.out.print(prompt);
		while (true) {
			try {
				return consoleReader.readLine();
			}
			catch (IOException e) {
				throw new RuntimeException("Could not read from console");
			}
		}
	}

	private Game game;
	private BoardRenderer boardRenderer;

	/**
	 * Construct a new ConsoleRenderer
	 *
	 * @param game
	 *            game to play
	 */
	public ConsoleRenderer(Game game) {
		this.game = game;
		this.boardRenderer = new BoardRenderer(game.getBoard(), game.getData());
	}

	/**
	 * Runs the game, returning when a player wins
	 */
	public void run() {
		for (Player player : queryPlayers(game.getData().getSuspects())) {
			game.addPlayer(player);
		}
		game.distributeCards();

		int remainingPlayers = game.getPlayers().size();

		for (int turn = 1;; turn++) {
			System.out.println("======== Turn " + turn + " ========");

			for (Player player : game.getPlayers()) {
				if (!player.isInGame()) {
					continue;
				}
				TurnController.Result result = TurnController.run(player, this);

				// Check if the player won or lost as a result of this turn
				if (result == TurnController.Result.WON) {
					System.out.println(player.getName() + "wins!");
					return;
				}
				else if (result == TurnController.Result.LOST) {
					remainingPlayers--;
					if (remainingPlayers == 1) {
						break;
					}
				}
			}

			// Check if someone won by default
			if (remainingPlayers == 1) {
				for (Player player : game.getPlayers()) {
					if (player.isInGame()) {
						System.out.println(player.getName() + " wins! All other players have been knocked out.");
						return;
					}
				}
			}
		}
	}

	/**
	 * Returns the game that is being (or will be) played by this renderer
	 */
	public Game getGame() {
		return game;
	}

	/**
	 * Returns the object this renderer is using to render the board
	 */
	public BoardRenderer getBoardRenderer() {
		return boardRenderer;
	}

	/**
	 * Ask the player to enter a number of players, along with their names
	 *
	 * @param list
	 *            collection of suspects. Player tokens will be assigned
	 *            randomly from this list
	 * @return the list of players, once they have been entered by the user
	 */
	private List<Player> queryPlayers(List<Suspect> list) {
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
			System.out.println("Player " + i + " name?");
			String name = readLine("> ");
			Suspect token = suspects.remove((int) (Math.random() * suspects.size()));
			result.add(new Player(name, token));
		}
		return result;
	}
}
