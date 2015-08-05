package cluedo.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cluedo.game.Game.Disprover;
import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.loader.Loader;

public class Game {
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
	private List<Player> lostPlayers = new ArrayList<Player>();

	public Game(Loader loader) {
		this.data = new GameData(loader);
		this.board = new Board(loader);
		distributeWeapons();
	}

	public void addPlayer(Player player) {
		players.add(player);
		getBoard().addPlayer(player);
	}

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

	public GameData getData() {
		return data;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board){
		this.board = board;
	}

	public List<Player> getPlayers() {
		return players;
	}

	private void distributeWeapons() {
		List<Room> roomsWithoutWeapon = new ArrayList<Room>(data.getRooms());
		for (Weapon weapon : data.getWeapons()) {
			Room room = roomsWithoutWeapon.remove(random.nextInt(roomsWithoutWeapon.size()));
			room.setWeapon(weapon);
		}
	}

	private void distributeToPlayers(List<? extends Card> cards) {
		while (true) {
			for (Player p : players) {
				if (cards.isEmpty())
					return;

				p.getHand().add(cards.remove(random.nextInt(cards.size())));
			}
		}
	}

	public void removePlayer(Player player) {
		players.remove(player);
		lostPlayers.add(player);
	}

	public Solution getSolution() {
		return solution;
	}

	public Disprover disproveSuggestion(Card suspect, Card room, Card weapon) {
		for(Player player : players) {
			for(Card card : player.getHand()){
				if(card.equals(suspect) || card.equals(room) || card.equals(weapon)) {
					return new Disprover(player, card);
				}
			}
		}
		// Players who have lost can still disprove suggestions
		for(Player player : lostPlayers) {
			for(Card card : player.getHand()){
				if(card.equals(suspect) || card.equals(room) || card.equals(weapon)) {
					return new Disprover(player, card);
				}
			}
		}
		return null;
	}
}
