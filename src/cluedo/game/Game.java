package cluedo.game;

import cluedo.loader.Loader;
import cluedo.ui.base.Renderer;

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
		for (Player player : renderer.queryPlayers(data)) {
			board.addPlayer(player);
		}
	}
}
