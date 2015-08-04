package cluedo.game;

import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;

public class Solution {
	private Suspect suspect;
	private Room room;
	private Weapon weapon;

	public Solution(Suspect suspect, Room room, Weapon weapon) {
		this.suspect = suspect;
		this.room = room;
		this.weapon = weapon;
	}
	
	public Suspect getSuspect() {
		return suspect;
	}

	public Room getRoom() {
		return room;
	}

	public Weapon getWeapon() {
		return weapon;
	}
}
