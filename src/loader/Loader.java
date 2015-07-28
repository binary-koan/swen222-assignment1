package loader;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import game.Door;
import game.GameData;
import game.objects.Room;
import game.objects.Suspect;
import game.objects.Weapon;

public class Loader {
	private static final Pattern GROUP_HEADER = Pattern.compile("[a-z]+:$");
	private static final Pattern SUSPECT_ENTRY =
			Pattern.compile("^\\s+(.)\\s*:\\s*([\\w\\.\\-\\s]+)\\[([a-z]+)\\]$");
	private static final Pattern ROOM_ENTRY =
			Pattern.compile("^\\s+(.)\\s*:\\s*([\\w\\.\\-\\s]+)$");
	private static final Pattern PASSAGE_ENTRY =
			Pattern.compile("^\\s+([\\w\\.\\-\\s]+):\\s*([\\w\\.\\-\\s]+)$");
	private static final Pattern WEAPON_ENTRY =
			Pattern.compile("^\\s+-\\s+([\\w\\.\\-\\s]+)$");

	private String filename;

	private Map<String, Room> rooms;
	private Map<Character, Room> roomsById;
	private Map<String, Suspect> suspects;
	private Map<Character, Suspect> suspectsById;
	private Map<String, Weapon> weapons;

	private BitSet corridors;
	private Map<Suspect, Point> startLocations;
	private Map<Point, Door> doorLocations;

	public Loader(String filename) throws IOException {
		this.filename = filename;
		loadData();
	}

	private void loadData() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));

		try {
			String line = br.readLine();
			if (line == null || !line.equals("---")) {
				fail("YAML header not found");
			}
			loadGroup(br);
		}
		finally {
			br.close();
		}
	}

	private void loadGroup(BufferedReader br) throws IOException {
		String line = br.readLine();
		Matcher matcher = GROUP_HEADER.matcher(line);
		if (matcher.find()) {
			String title = matcher.group(1);
			switch(title) {
			case "suspects":
				loadSuspects(br);
				break;
			case "rooms":
				loadRooms(br);
				break;
			case "passages":
				loadPassages(br);
				break;
			case "weapons":
				loadWeapons(br);
				break;
			default:
				fail("Unrecognized group: " + title);
			}
		}
		else if (Pattern.matches("^---+$", line)) {
			loadBoard(br, line.length());
		}
		else {
			fail("Expected group header or start of board");
		}
	}

	private void loadSuspects(BufferedReader br) throws IOException {
		suspects = new HashMap<String, Suspect>();
		suspectsById = new HashMap<Character, Suspect>();
		for (Matcher match : loadEntries(br, SUSPECT_ENTRY, "suspect")) {
			String id = match.group(1);
			String name = match.group(2);
			Color color = Color.getColor(match.group(3));
//			Suspect suspect = new Suspect(id, name, color);
//			suspects.put(name, suspect);
//			suspectsById.put(id, suspect);
		}
	}

	private void loadRooms(BufferedReader br) throws IOException {
		rooms = new HashMap<String, Room>();
		roomsById = new HashMap<Character, Room>();
		for (Matcher match : loadEntries(br, ROOM_ENTRY, "room")) {
			String id = match.group(1);
			String name = match.group(2);
//			Room room = new Room(id, name, color);
//			rooms.put(name, room);
//			roomsById.put(id, room);
		}
	}

	private void loadPassages(BufferedReader br) throws IOException {
		for (Matcher match : loadEntries(br, PASSAGE_ENTRY, "passage")) {
			String from = match.group(1);
			String to = match.group(2);
			if (!rooms.containsKey(from) || !rooms.containsKey(to)) {
				fail("Cannot find room(s) for passage '" + from + "' -> '" + to + "'");
			}
//			rooms.get(from).setPassageExit(rooms.get(to));
		}
	}

	private void loadWeapons(BufferedReader br) throws IOException {
		weapons = new HashMap<String, Weapon>();
		for (Matcher match : loadEntries(br, WEAPON_ENTRY, "weapon")) {
			String name = match.group(1);
//			weapons.put(name, new Weapon(name));
		}
	}

	private void loadBoard(BufferedReader br, int size) throws IOException {
		corridors = new BitSet(size * size);
		for (int y = 0; y < size; y++) {
			String line = br.readLine();
			if (line == null) {
				fail("Board definition must be at least " + size + " lines long");
			}
			if (line.charAt(line.length() - 1) == '|') {
				line = line.substring(1, line.length() - 1);
			}
			if (line.length() < size) {
				fail("Each row of the board must be at least " + size + " characters long");
			}
			for (int x = 0; x < size; x++) {
				parseBoardCharacter(line.charAt(x), x, y, size, line);
			}
		}
		String line = br.readLine();
		if (line == null || !Pattern.matches("^-{" + size + "}$", line)) {
			fail("This board should end with a line containing exactly " + size + " dashes");
		}
	}

	private void parseBoardCharacter(char chr, int x, int y, int size, String line) {
		if (chr == ' ') {
			return;
		}
		else if (chr == '.') {
			corridors.set(x + (y * size));
		}
		else if (chr == '_') {
			addDoor(chr, x, y, line);
		}
		else if (roomsById.containsKey(chr)) {
//			roomsById.get(chr).addPoint(x, y);
		}
		else if (suspectsById.containsKey(chr)) {
//			suspectsById.get(chr).setStartLocation(x, y);
		}
		else {
			fail("Unknown character on board at (" + x + "," + y + ")");
		}
	}

	private void addDoor(char chr, int x, int y, String line) {
		char beside = x == 0 ? line.charAt(x + 1) : line.charAt(x - 1);
		if (!roomsById.containsKey(beside) && x < line.length() - 1) {
			beside = line.charAt(x + 1);
		}

		Room room = roomsById.get(beside);
		if (room == null) {
			fail("Couldn't find room connected to door at (" + x + "," + y + ")");
		}
//		doorLocations.put(new Point(x, y), new Door(room));
	}

	private List<Matcher> loadEntries(BufferedReader br, Pattern pattern, String lookingFor) throws IOException {
		List<Matcher> result = new ArrayList<Matcher>();
		String line;
		while ((line = br.readLine()) != null) {
			Matcher matcher = pattern.matcher(line);
			if (!matcher.find()) {
				fail("Couldn't parse " + lookingFor + " entry");
			}
			result.add(matcher);
		}
		return result;
	}

	private void fail(String message) {
		//TODO: Change to something catch'y
		throw new RuntimeException(message);
	}
}
