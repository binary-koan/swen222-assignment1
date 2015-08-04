package cluedo.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.loader.Loader;

public class Game {
	private GameData data;
	private Board board;
	private Solution solution;
	private List<Player> players = new ArrayList<Player>();


	public Game(Loader loader) {
		this.data = new GameData(loader);
		this.board = new Board(loader);
	}


	public void addPlayer(Player player) {
		players.add(player);
		getBoard().addPlayer(player);
	}


	public void assignHands() {
		List<Weapon> weapons = data.getWeapons();
		List<Suspect> suspects = data.getSuspects();
		List<Room> rooms = data.getRooms();


		//method doing two things - bad design?
		//Will the following revert changes back to the arraylists?
		//also empty checks needed?

		solution = new Solution();
		solution.addCard(suspects.remove((int)(Math.random() * suspects.size())));
		solution.addCard(rooms.remove((int)(Math.random() * rooms.size())));
		solution.addCard(weapons.remove((int)(Math.random() * weapons.size())));

		while(!weapons.isEmpty() || !suspects.isEmpty() || !rooms.isEmpty()){
			for(Player p : players){
				if(!weapons.isEmpty()){
					p.addCardToHand(weapons.remove((int)(Math.random() * weapons.size())));
				}
				if(!suspects.isEmpty()){
					p.addCardToHand(suspects.remove((int)(Math.random() * suspects.size())));
				}
				if(rooms.isEmpty()){
					p.addCardToHand(rooms.remove((int)(Math.random() * rooms.size())));
				}
			}
		}
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
}
