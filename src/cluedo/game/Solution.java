package cluedo.game;

import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;

/**
 * Represents a solution to a game (the suspect, room and weapon which describe
 * the actual murder)
 */
public class Solution {
	private Suspect suspect;
	private Room room;
	private Weapon weapon;

	/**
	 * Create a new solution
	 * @param suspect the murderer
	 * @param room the room where the murder was committed
	 * @param weapon the weapon used by the murderer
	 */
	public Solution(Suspect suspect, Room room, Weapon weapon) {
		this.suspect = suspect;
		this.room = room;
		this.weapon = weapon;
	}

	/**
	 * Returns the murderer in this scenario
	 */
	public Suspect getSuspect() {
		return suspect;
	}

	/**
	 * Returns the room the murder was committed in in this scenario
	 */
	public Room getRoom() {
		return room;
	}

	/**
	 * Returns the murder weapon in this scenario
	 */
	public Weapon getWeapon() {
		return weapon;
	}

	/**
	 * Returns true if the suggestion is correct, false otherwise
	 * @param suggestion suggestion to check
	 */
	public boolean checkAgainst(Game.Suggestion suggestion) {
		return suggestion.getRoom() == room && suggestion.getSuspect() == suspect && suggestion.getWeapon() == weapon;
	}
}
