package cluedo.game;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.ui.graphical.components.PlayerDisplay;

/**
 * Represents a player in the game - a human who takes turns and moves around
 * the board.
 */
public class Player {
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);

	private String name;
	private Suspect token;
	private List<Card> hand = new ArrayList<Card>();

	private Room room;
	private int movesRemaining;
	private boolean inGame = true;

	/**
	 * Construct a new player
	 *
	 * @param name
	 *            the name of the player
	 * @param token
	 *            a token to assign to the player
	 */
	public Player(String name, Suspect token) {
		this.name = name;
		this.token = token;
	}

	/**
	 * Returns the player's name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the token being used by the player
	 */
	public Suspect getToken() {
		return this.token;
	}

	/**
	 * Returns a list of all cards in the player's hand
	 */
	public List<Card> getHand() {
		return this.hand;
	}

	/**
	 * Returns the room that the player is currently in, or null if they are in
	 * a corridor
	 */
	public Room getRoom() {
		return this.room;
	}

	/**
	 * Sets the player's current room
	 */
	public void setRoom(Room room) {
		this.room = room;
	}

	/**
	 * Returns true if the player is still in the game, or false if they have
	 * lost
	 */
	public boolean isInGame() {
		return inGame;
	}

	/**
	 * Sets whether the player is in the game (ie. whether they have lost yet)
	 */
	public void setInGame(boolean inGame) {
		this.inGame = inGame;
	}

    /**
     * Get the number the player rolled this turn, or 0 if it isn't this
     * player's turn
     */
    public int getMovesRemaining() {
        return this.movesRemaining;
    }

	/**
	 * Sets the number of moves the player has left
	 *
	 * @param movesRemaining
	 *            number of moves left this turn
	 */
	public void setMovesRemaining(int movesRemaining) {
        changes.firePropertyChange("movesRemaining", this.movesRemaining, movesRemaining);
		this.movesRemaining = movesRemaining;
	}

	/**
	 * Resets the player's remaining move count to zero
	 */
	public void resetMovesRemaining() {
		this.movesRemaining = 0;
	}

    /**
     * Adds a property change listener, which will be notified when "movesRemaining" is changed
     *
     * @param listener listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener
     * @param listener listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }
}
