package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cluedo.game.objects.Suspect;
import org.junit.Test;

import cluedo.game.Game;
import cluedo.game.GameData;
import cluedo.game.Player;
import cluedo.game.Solution;
import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Weapon;
import cluedo.loader.Loader;

public class GameTests {
	@Test
	public void testDistributesAllWeapons() {
		Loader loader = loadDefaultFile();
		Game game = new Game(loader);
		List<Weapon> assignedWeapons = new ArrayList<Weapon>();

		for (Room room : game.getData().getRooms()) {
			if (room.getWeapon() != null) {
				assignedWeapons.add(room.getWeapon());
			}
		}

		assertEquals(assignedWeapons.size(), game.getData().getWeapons().size());
	}

	@Test
	public void testDistributesAllCards() {
		Game game = mockGame();

		Set<Card> cards = new HashSet<Card>();
		for (Player player : game.getPlayers()) {
			cards.addAll(player.getHand());
		}

		GameData data = game.getData();
		int totalCards = data.getSuspects().size() + data.getWeapons().size() + data.getRooms().size();
		assertEquals(totalCards - 3, cards.size());
	}

	@Test
	public void testCreatesSolution() {
		Game game = mockGame();

		assertNotNull(game.getSolution());
		assertNotNull(game.getSolution().getRoom());
		assertNotNull(game.getSolution().getSuspect());
		assertNotNull(game.getSolution().getWeapon());
	}

	@Test
	public void testDisprovesSuggestion() {
		Game game = mockGame();
		Player player = game.getPlayers().get(0);
		Player other = game.getPlayers().get(2);
		Solution solution = game.getSolution();

		Game.Disprover disprover = game.disproveSuggestion(player, new Game.Suggestion(
				(Suspect)other.getHand().get(1), solution.getWeapon(), solution.getRoom()
		));
		assertEquals(disprover.getPlayer(), other);
		assertEquals(disprover.getCard(), other.getHand().get(1));
	}

	@Test
	public void testCannotDisproveSolution() {
		Game game = mockGame();
		Player player = game.getPlayers().get(0);
		Solution solution = game.getSolution();

		Game.Disprover disprover = game.disproveSuggestion(player, new Game.Suggestion(
				solution.getSuspect(), solution.getWeapon(), solution.getRoom()
		));
		assertNull(disprover);
	}

	private static Game mockGame() {
		Loader loader = loadDefaultFile();
		Game game = new Game(loader);
		game.addPlayer(new Player("Player 1", game.getData().getSuspects().get(0)));
		game.addPlayer(new Player("Player 2", game.getData().getSuspects().get(1)));
		game.addPlayer(new Player("Player 3", game.getData().getSuspects().get(2)));
		game.distributeCards();

		return game;
	}

	private static Loader loadDefaultFile() {
		try {
			return new Loader("data/standard.txt");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
