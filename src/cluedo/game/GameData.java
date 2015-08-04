package cluedo.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.loader.Loader;

public class GameData {
	private Map<String, Room> rooms;
	private Map<String, Suspect> suspects;
	private Map<Character, Suspect> suspectsById;
	private Map<String, Weapon> weapons;


	public GameData(Loader loader) {
		this.rooms = loader.getRooms();
		this.suspects = loader.getSuspects();
		this.suspectsById = loader.getSuspectsById();
		this.weapons = loader.getWeapons();
	}

	public List<Suspect> getSuspects() {
		List<Suspect> result = new ArrayList<Suspect>();
		result.addAll(suspects.values());
		return result;
	}

	public Map<Character, Suspect> getSuspectsById(){
		return suspectsById;
	}

	public List<Weapon> getWeapons(){
		List<Weapon> result = new ArrayList<Weapon>();
		result.addAll(weapons.values());
		return result;
	}

	public List<Room> getRooms(){
		List<Room> result = new ArrayList<Room>();
		result.addAll(rooms.values());
		return result;
	}
}
