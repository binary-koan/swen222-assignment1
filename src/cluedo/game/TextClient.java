package cluedo.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import cluedo.game.Player;
import cluedo.game.objects.Suspect;
import cluedo.loader.Loader;
import cluedo.loader.Loader.SyntaxException;

public class TextClient {

	public static String filename = "test";
	public static Loader Game;

	public static void main(String[] args) throws IOException, SyntaxException {
		// TODO Auto-generated method stub
		System.out.println("***Cluedo***");
		System.out.println("By Jono Mingard and Scott Holdaway, 2015");


		Game = new Loader(filename);

		//input players
		int nplayers = consoleNumber("How many players?");
		ArrayList<Player> players = assignPlayers(nplayers);

		int turn = 1;

		while(true){
			System.out.println("======== Turn "+ turn +" ========");

			for(Player p: players){
				System.out.println("Currently "+ p.getName() +"'s turn");
				System.out.println("What would you like to do?");
				gameOptions(p);
			}


		}


	}

	/**
	 * Method for receiving user string input, prompted by @param msg.
	 * @param msg
	 * @return
	 */
	private static String consoleString(String msg){
		System.out.print(msg + ""); 							//print a separator from user input
		while (true) { 											//Continuously await user input
			BufferedReader input = new BufferedReader(new InputStreamReader(
					System.in));
			try {
				return input.readLine();
			} catch (IOException e) {
				System.out.println("I/O Error ... please try again!");
			}
		}
	}

	/**
	 * Method for receiving user number input, prompted by @param msg.
	 * @param msg
	 * @return
	 */
	private static int consoleNumber(String msg) {
		System.out.print(msg + " ");
		while (true) {
			BufferedReader input = new BufferedReader(new InputStreamReader(
					System.in));
			try {
				String typed = input.readLine();
				if(typed != null){
					if(Integer.parseInt(typed) > 6 ||Integer.parseInt(typed) < 3){ //requires >=3, <=6 players
						return Integer.parseInt(typed);
					}
				}
			} catch (IOException e) {
				System.out.println("Please enter a number that is between 3 and 6");
			}
		}
	}

	public static ArrayList<Player> assignPlayers(int nplayers){
		//assign the players
		Map<String, Suspect> tokens = Game.getSuspects();
		ArrayList<Player> players = new ArrayList<Player>();

		for (int i = 0; i != nplayers; ++i) {
			String name = consoleString("Player #" + i + " name?");
			String token = consoleString("Player #" + i + " token?");
			if (!tokens.containsKey(token)) {
				System.out.print("Invalid token!  Must be one of: (Awaiting proper string format)");
			}
			tokens.remove(token);
			players.add(new Player(name, tokens.get(token), tokens.get(token).getStartLocation()));
		}
		return players;
	}

	public static void gameOptions(Player player){
		System.out.println("What would you like to do?");

		while(true){
			String input = consoleString("Move (m), make a suggestion (s), make an accusation (a), review the board (r) "
					+ "or end your turn (e)?");
			if(input == "m"){
				move(player);
			}
			if(input == "s"){
				makeSuggestion(player);
			}
			if(input == "a"){
				makeAccusation(player);
			}
			if(input == "r"){
				displayBoard();
			}
			if(input == "e"){
				return;
			}


		}


	}

	private static void displayBoard() {
		// TODO Auto-generated method stub

	}

	private static void makeAccusation(Player player) {
		// TODO Auto-generated method stub

	}

	private static void makeSuggestion(Player player) {
		// TODO Auto-generated method stub

	}

	private static void move(Player player) {
		// TODO Auto-generated method stub

	}

}
