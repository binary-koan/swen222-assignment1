package cluedo.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.loader.Loader;

/**
 * Represents a game of Cluedo. A central class which contains references to the
 * various components of the game
 */
public class Game {
	/**
	 * A combination of player and card which disproves a player's suggestion
	 */
	public class Disprover {
		private Player player;
		private Card card;

		public Disprover(Player player, Card card) {
			this.player = player;
			this.card = card;
		}

		public Player getPlayer() {
			return player;
		}

		public Card getCard() {
			return card;
		}
	}

	private static Random random = new Random();

	private GameData data;
	private Board board;
	private Solution solution;
	private List<Player> players = new ArrayList<Player>();

	/**
	 * Construct a new game
	 *
	 * @param loader
	 *            object to retrieve game and board info from
	 */
	public Game(Loader loader) {
		this.data = new GameData(loader);
		this.board = new Board(loader);
		distributeWeapons();
	}

	/**
	 * Adds a player to the game
	 *
	 * @param player
	 *            player to add
	 */
	public void addPlayer(Player player) {
		players.add(player);
		getBoard().addPlayer(player);
	}

	/**
	 * Returns a list of all players (including those who have already lost the
	 * game)
	 */
	public List<Player> getPlayers() {
		return players;
	}

	/**
	 * Creates a random solution for the game, and distributes all other cards
	 * randomly to the current set of players
	 */
	public void distributeCards() {
		List<Weapon> weapons = data.getWeapons();
		List<Suspect> suspects = data.getSuspects();
		List<Room> rooms = data.getRooms();

		Suspect murderer = suspects.remove(random.nextInt(suspects.size()));
		Room murderRoom = rooms.remove(random.nextInt(rooms.size()));
		Weapon murderWeapon = weapons.remove(random.nextInt(weapons.size()));
		solution = new Solution(murderer, murderRoom, murderWeapon);

		distributeToPlayers(weapons);
		distributeToPlayers(suspects);
		distributeToPlayers(rooms);
	}

	/**
	 * Returns the data this game is associated with (suspects, rooms, etc)
	 */
	public GameData getData() {
		return data;
	}

	/**
	 * Represents the board the game is being played on
	 */
	public Board getBoard() {
		return board;
	}

	/**
	 * Returns the game's solution - whodunnit, where and how
	 */
	public Solution getSolution() {
		return solution;
	}

	/**
	 * If one of the given cards is in a player's hand, this will return a
	 * Disprover containing that player and the card that disproved the
	 * suggestion
	 *
	 * @param room
	 *            room that was suggested
	 * @param weapon
	 *            weapon that was suggested
	 * @return an object disproving the suggestion, or null if it could not be
	 *         disproved
	 */
	public Disprover disproveSuggestion(Player player, Card suspect, Card room,
			Card weapon) {
		int startingIndex = players.indexOf(player);

		for (int i = startingIndex, j = 0; j < players.size(); i++, j++) {
			for (Card card : players.get(i).getHand()) {
				if (card.equals(suspect) || card.equals(room)
						|| card.equals(weapon)) {
					return new Disprover(players.get(i), card);
				}
			}
		}

		return null;
	}

	/**
	 * Randomly assigns weapons to rooms
	 */
	private void distributeWeapons() {
		List<Room> roomsWithoutWeapon = new ArrayList<Room>(data.getRooms());
		for (Weapon weapon : data.getWeapons()) {
			Room room = roomsWithoutWeapon.remove(random.nextInt(roomsWithoutWeapon.size()));
			room.setWeapon(weapon);
		}
	}

	/**
	 * Distributes a list of cards to all players in sequence
	 */
	private void distributeToPlayers(List<? extends Card> cards) {
		while (true) {
			for (Player p : players) {
				if (cards.isEmpty())
					return;

				p.getHand().add(cards.remove(random.nextInt(cards.size())));
			}
		}
	}
}
