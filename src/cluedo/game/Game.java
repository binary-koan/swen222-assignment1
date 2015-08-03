package cluedo.game;

import java.awt.List;
import java.util.ArrayList;

import cluedo.loader.Loader;
import cluedo.ui.base.Renderer;
import cluedo.ui.ConsoleRenderer;

public class Game {
	private GameData data;
	private Board board;
	private Renderer renderer;


	public Game(Loader loader, Renderer renderer) {
		this.data = new GameData(loader);
		this.board = new Board(loader);
		this.renderer = renderer;
		renderer.setup(data, board);
	}

	public void run() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : renderer.queryPlayers(data)) {
			board.addPlayer(player);
			players.add(player);
		}

		((ConsoleRenderer) renderer).assignHands(players); // remove cast?

		int turn = 1;
		int i = 0;

		while(true){
			System.out.println("======== Turn "+ turn +" ========");

			for(Player p: players){
				if(p.getInGame() == true){
					System.out.println("Currently "+ p.getName() +"'s turn");
					System.out.println("What would you like to do?");
					int diceRoll = (int) Math.random() * 6;
					p.setMovesLeft(diceRoll);
					((ConsoleRenderer) renderer).gameOptions(p, diceRoll); //remove cast?
				}
				//If all players have made an incorrect guess and are knocked out.
				//If a player has won
				//Should these methods be here? Seems kind of messy
				if(((ConsoleRenderer) renderer).getWinningPlayer()!=null){
					System.out.println("Player "+((ConsoleRenderer) renderer).getWinningPlayer().getName()+"wins!");
					return;
				}

				if(((ConsoleRenderer) renderer).getNullPlayers().size() == players.size()){
					System.out.println("All players have been knocked out! Game over.");
					return;
				}


			}
			turn++;
		}
	}
}
