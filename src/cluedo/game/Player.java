package cluedo.game;

import java.awt.Point;
import java.util.List;

import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;

public class Player {

	public final String name;
	private List<Card> hand;
	private Suspect token;
	private Boolean inGame = true;
	public Room room = null;
	public Point location;

	public Player(String name, Suspect token, Point location){
		this.name = name;
		this.token = token;
		this.location = location;
	}

	public String getName(){
		return this.name;
	}

	public void assignToken(Suspect token) {
		this.token = token;
	}

	public Suspect getToken(){
		return this.token;
	}

	public Room getRoom(){
		return this.room;
	}

	public void setRoom(Room room){
		this.room = room;
	}
}
