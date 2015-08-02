package cluedo.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;
import cluedo.loader.Loader;

public class GameData {
	private Map<String, Room> rooms;
	private Map<String, Suspect> suspects;
	private List<Weapon> weapons;
	
	public GameData(Loader loader) {
		this.rooms = loader.getRooms();
		this.suspects = loader.getSuspects();
		this.weapons = loader.getWeapons();
	}

	public List<Suspect> getSuspects() {
		List<Suspect> result = new ArrayList<Suspect>();
		result.addAll(suspects.values());
		return result;
	}
}
