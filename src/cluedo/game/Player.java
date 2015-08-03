package cluedo.game;

import java.awt.Point;
import java.util.List;

import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;

public class Player {

	private String name;
	private List<Card> hand;
	private Suspect token;
	private Boolean canMove;
	private Boolean inGame = true;
	private Boolean inRoom = false;
	private boolean canSuggest = true;
	private Room room = null;
	private Point location;
	private int movesLeft = 0;


	public Player(String name, Suspect token){
		this.name = name;
		this.token = token;
		//this.location = location;
	}

	public String getName(){
		return this.name;
	}

	public List<Card>getHand(){
		return this.hand;
	}

	public boolean inRoom(){
		return inRoom;
	}

	public void addCardToHand(Card c){
		hand.add(c);
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

	public void setMovesLeft(int roll) {
		this.movesLeft = roll;

	}

	public int getMovesLeft() {
		return this.movesLeft;
	}

	public boolean getCanSuggest() {
		return this.canSuggest;
	}

	public void setCanSuggest(boolean b) {
		this.canSuggest = b;
	}

	public Boolean getInGame() {
		return this.inGame;
	}

	public void setInGame(Boolean inGame) {
		this.inGame = inGame;
	}

	public void setLocation(Point location) {
		this.location = location;
	}
}
