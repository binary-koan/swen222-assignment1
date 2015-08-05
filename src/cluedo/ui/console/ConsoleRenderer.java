package cluedo.ui.console;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

	public static String getFilename(String defaultName) {
		System.out.println("Which setup would you like to use?");
		System.out.println("Enter a filename or press ENTER for default.");
		String line = readLine("> ");
		return line.isEmpty() ? defaultName : line;
	}

	public static String readLine(String prompt) {
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

			List<Player> losingPlayers = new ArrayList<Player>();

			for(Player player : game.getPlayers()) {
				TurnController.Result result = TurnController.run(player, this);

				if (result == TurnController.Result.WON) {
					System.out.println(player.getName() + "wins!");
					return;
				}
				else if (result == TurnController.Result.LOST) {
					losingPlayers.add(player);

					if (losingPlayers.size() == game.getPlayers().size() - 1) {
						break;
					}
				}
			}

			for (Player player : losingPlayers) {
				game.removePlayer(player);
			}

			if(game.getPlayers().size() == 1) {
				Player winner = game.getPlayers().get(0);
				System.out.println(winner.getName() + " wins! All other players have been knocked out.");
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

	public Game getGame() {
		return game;
	}

	public BoardRenderer getBoardRenderer() {
		return boardRenderer;
	}
}
