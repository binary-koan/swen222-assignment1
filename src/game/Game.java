package game;

import loader.Loader;
import ui.base.Renderer;

public class Game {
	private GameData data;
	private Board board;
	private Renderer renderer;

	public Game(Loader loader, Renderer renderer) {
		this.data = new GameData(loader);
		this.board = new Board(loader);
		this.renderer = renderer;
	}
}
