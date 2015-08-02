package cluedo.ui.base;

import java.util.List;

import cluedo.game.Board;
import cluedo.game.GameData;
import cluedo.game.Player;

public interface Renderer {
	public void setup(GameData data, Board board);
	public List<Player> queryPlayers(GameData data);
}
