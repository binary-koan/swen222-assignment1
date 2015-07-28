package game;

import ui.base.Renderer;

public class Game {
	private GameData data;
	private Renderer renderer;

	public Game(GameData data, Renderer renderer) {
		this.data = data;
		this.renderer = renderer;
	}
}
