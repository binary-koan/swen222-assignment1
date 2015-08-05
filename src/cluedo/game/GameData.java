package cluedo.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.loader.Loader;

/**
 * Represents a collection of data that defines how the current game behaves
 * (eg. lists of all suspects, rooms and weapons)
 */
public class GameData {
	private Map<String, Room> rooms;
	private Map<String, Suspect> suspects;
	private Map<Character, Suspect> suspectsById;
	private Map<String, Weapon> weapons;

	/**
	 * Load data from the specified loader
	 *
	 * @param loader
	 * 			  object to load data from
	 */
	public GameData(Loader loader) {
		this.rooms = loader.getRooms();
		this.suspects = loader.getSuspects();
		this.suspectsById = loader.getSuspectsById();
		this.weapons = loader.getWeapons();
	}

	/**
	 * Returns a list of all suspects in the game
	 */
	public List<Suspect> getSuspects() {
		List<Suspect> result = new ArrayList<Suspect>();
		result.addAll(suspects.values());
		return result;
	}

	/**
	 * Returns a collection mapping single-character suspect IDs to their associated suspects
	 */
	public Map<Character, Suspect> getSuspectsById() {
		return suspectsById;
	}

	/**
	 * Returns the suspect with the given name
	 */
	public Suspect getSuspect(String name) {
		if (suspects.containsKey(name)) {
			return suspects.get(name);
		}
		return suspectsById.get(name);
	}

	/**
	 * Returns a collection of all weapons in the game
	 */
	public List<Weapon> getWeapons() {
		List<Weapon> result = new ArrayList<Weapon>();
		result.addAll(weapons.values());
		return result;
	}

	/**
	 * Returns the weapon with the specified name
	 */
	public Weapon getWeapon(String name) {
		return weapons.get(name);
	}

	/**
	 * Returns a list of all rooms in the game
	 */
	public List<Room> getRooms() {
		List<Room> result = new ArrayList<Room>();
		result.addAll(rooms.values());
		return result;
	}

	/**
	 * Returns the room with the specified name
	 */
	public Room getRoom(String name) {
		return rooms.get(name);
	}
}
