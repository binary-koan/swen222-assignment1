package cluedo.game;

import java.util.ArrayList;
import java.util.List;

import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;

public class Player {

	private String name;
	private Suspect token;
	private Room room;
	private List<Card> hand = new ArrayList<Card>();
	
	private Boolean inGame = true;
	private int currentDiceRoll;

	public Player(String name, Suspect token){
		this.name = name;
		this.token = token;
	}

	public String getName(){
		return this.name;
	}

	public List<Card> getHand(){
		return this.hand;
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

	public Boolean getInGame() {
		return this.inGame;
	}

	public void setInGame(Boolean inGame) {
		this.inGame = inGame;
	}

	public void startTurn(int diceRoll) {
		this.currentDiceRoll = diceRoll;
	}

	public void endTurn() {
		this.currentDiceRoll = 0;
	}

	public int getDiceRoll() {
		return this.currentDiceRoll;
	}
}
