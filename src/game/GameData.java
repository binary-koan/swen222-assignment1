package game;

import java.util.List;
import java.util.Map;

import game.objects.Room;
import game.objects.Suspect;
import game.objects.Weapon;
import loader.Loader;

public class GameData {
	private Map<String, Room> rooms;
	private Map<String, Suspect> suspects;
	private List<Weapon> weapons;
	
	public GameData(Loader loader) {
		this.rooms = loader.getRooms();
		this.suspects = loader.getSuspects();
		this.weapons = loader.getWeapons();
	}
}
