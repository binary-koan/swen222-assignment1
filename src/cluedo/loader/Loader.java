package cluedo.loader;

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

import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;

/**
 * Loads information about suspects, rooms, weapons and board layout from a
 * file. See "data/standard.txt" for an example and explanation of the syntax.
 */
public class Loader {
	/**
	 * Exception thrown if the syntax of the game file is not understood by the
	 * parser
	 */
	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

	private static final int MAX_LINE_LENGTH = 1000;

	// Matches group headers like "suspects:" or "rooms:"
	private static final Pattern GROUP_HEADER = Pattern.compile("([a-z]+):$");
	// Matches suspect definitions of the form "  b: Mr. Black [black]"
	private static final Pattern SUSPECT_ENTRY = Pattern
			.compile("^\\s+(.)\\s*:\\s*([a-zA-Z0-9\\.\\-\\s]+)(#[0-9a-fA-F]{6})$");
	// Matches room definitions of the form "  S: Sitting Room"
	private static final Pattern ROOM_ENTRY = Pattern
			.compile("^\\s+(.)\\s*:\\s*([a-zA-Z\\.\\-\\s]+)$");
	// Matches passage definitions of the form "Sitting Room: Bathroom"
	private static final Pattern PASSAGE_ENTRY = Pattern
			.compile("^\\s+([a-zA-Z\\.\\-\\s]+):\\s*([a-zA-Z\\.\\-\\s]+)$");
	private static final Pattern WEAPON_ENTRY = Pattern
			.compile("^\\s+-\\s+([a-zA-Z\\.\\-\\s]+)$");
	// Matches comments, ie. any line starting with a "#", optionally with
	// whitespace before it
	private static final Pattern COMMENT = Pattern.compile("^\\s*#");

	private Map<String, Room> rooms;
	private Map<Character, Room> roomsById;
	private Map<String, Suspect> suspects;
	private Map<Character, Suspect> suspectsById;
	private Map<String, Weapon> weapons;

	private int boardWidth;
	private int boardHeight;
	private BitSet corridors;

	/**
	 * Constructs a new object by loading data from the specified file
	 *
	 * @param filename
	 *            file to load data from
	 * @throws IOException
	 * @throws SyntaxException
	 *             if unknown syntax is encountered while loading
	 */
	public Loader(String filename) throws IOException, SyntaxException {
		loadData(filename);
	}

	/**
	 * Returns all room names in the file, mapped to the corresponding Room
	 *
	 * @return
	 */
	public Map<String, Room> getRooms() {
		return rooms;
	}

	/**
	 * Returns all suspect names in the file, mapped to the corresponding
	 * Suspect
	 *
	 * @return
	 */
	public Map<String, Suspect> getSuspects() {
		return suspects;
	}

	/**
	 * Returns all suspect ids in the file, mapped to the corresponding Suspect
	 *
	 * @return
	 */
	public Map<Character, Suspect> getSuspectsById() {
		return suspectsById;
	}

	/**
	 * Returns all weapons loaded from the file
	 *
	 * @return
	 */
	public Map<String, Weapon> getWeapons() {
		return weapons;
	}

	/**
	 * Returns the board width used in the file
	 *
	 * @return
	 */
	public int getBoardWidth() {
		return boardWidth;
	}

	/**
	 * Returns the board height used in the file
	 */
	public int getBoardHeight() {
		return boardHeight;
	}

	/**
	 * Returns a representation of the corridors on the file's board
	 *
	 * @return A bit set containing (boardSize * boardSize) bits in 'blocks' of
	 *         (size), such that the bit at (x + boardSize * y) is true if the
	 *         point (x,y) is a corridor
	 */
	public BitSet getCorridors() {
		return corridors;
	}

	/**
	 * Loads data from the file(name) passed to the constructor into the fields
	 * of this object
	 */
	private void loadData(String filename) throws IOException, SyntaxException {
		BufferedReader br = new BufferedReader(new FileReader(filename));

		try {
			String line = readDataLine(br);
			if (line == null || !line.equals("---")) {
				fail("YAML header not found");
			}
			loadGroups(br);
		}
		finally {
			br.close();
		}
	}

	private void loadGroups(BufferedReader br) throws IOException,
			SyntaxException {
		while (loadGroup(br)) {
			continue;
		}

		if (rooms == null) {
			fail("No rooms definition found");
		}
		else if (suspects == null) {
			fail("No suspects definition found");
		}
		else if (weapons == null) {
			fail("No weapons definition found");
		}
		else if (corridors == null) {
			fail("No board definition found");
		}
	}

	/**
	 * Tries to load a group (title + entries) from the given reader. If it
	 * fails to find a group title, it will load the board instead.
	 */
	private boolean loadGroup(BufferedReader br) throws IOException,
			SyntaxException {
		String line = readDataLine(br);
		Matcher matcher;

		if (line == null) {
			return false;
		}
		else if ((matcher = GROUP_HEADER.matcher(line)).find()) {
			String title = matcher.group(1);
			switch (title) {
			case "suspects":
				loadSuspects(br);
				return true;
			case "rooms":
				loadRooms(br);
				return true;
			case "passages":
				loadPassages(br);
				return true;
			case "weapons":
				loadWeapons(br);
				return true;
			default:
				fail("Unrecognized group: " + title);
				return false;
			}
		}
		else if (Pattern.matches("^---+$", line)) {
			if (suspects == null || rooms == null || weapons == null) {
				fail("All groups must come before the start of the board");
			}
			loadBoard(br, line.length());
			return true;
		}
		else {
			fail("Expected group header or start of board");
			return false;
		}
	}

	/**
	 * Loads entries in the 'suspects' group, populating the (suspects) and
	 * (suspectsById) fields. Assumes the group title has already been read.
	 */
	private void loadSuspects(BufferedReader br) throws IOException,
			SyntaxException {
		suspects = new HashMap<String, Suspect>();
		suspectsById = new HashMap<Character, Suspect>();
		for (Matcher match : loadEntries(br, SUSPECT_ENTRY, "suspect")) {
			char id = match.group(1).charAt(0);
			String name = match.group(2).trim();
			Color color = Color.decode(match.group(3));
			Suspect suspect = new Suspect(id, name, color);
			suspects.put(name, suspect);
			suspectsById.put(id, suspect);
		}
	}

	/**
	 * Loads entries in the 'rooms' group, populating the (rooms) and
	 * (roomsById) fields. Assumes the group title has already been read.
	 */
	private void loadRooms(BufferedReader br) throws IOException,
			SyntaxException {
		rooms = new HashMap<String, Room>();
		roomsById = new HashMap<Character, Room>();
		for (Matcher match : loadEntries(br, ROOM_ENTRY, "room")) {
			char id = match.group(1).charAt(0);
			String name = match.group(2);
			Room room = new Room(name);
			rooms.put(name, room);
			roomsById.put(id, room);
		}
	}

	/**
	 * Loads entries in the 'passages' group, updating (rooms) with their links.
	 * Assumes the group title has already been read.
	 */
	private void loadPassages(BufferedReader br) throws IOException,
			SyntaxException {
		for (Matcher match : loadEntries(br, PASSAGE_ENTRY, "passage")) {
			String from = match.group(1);
			String to = match.group(2);
			if (!rooms.containsKey(from) || !rooms.containsKey(to)) {
				fail("Cannot find room(s) for passage '" + from + "' -> '" + to + "'");
			}
			rooms.get(from).setPassageExit(rooms.get(to));
		}
	}

	/**
	 * Loads entries in the 'weapons' group, populating the (weapons) field.
	 * Assumes the group title has already been read.
	 */
	private void loadWeapons(BufferedReader br) throws IOException,
			SyntaxException {
		weapons = new HashMap<String, Weapon>();
		for (Matcher match : loadEntries(br, WEAPON_ENTRY, "weapon")) {
			String name = match.group(1);
			weapons.put(name, new Weapon(name));
		}
	}

	/**
	 * Attempts to load a board of the specified size, populating the
	 * (corridors) field and adding information to other fields as necessary.
	 * Assumes the opening line of dashes has been read, but reads the closing
	 * line.
	 */
	private void loadBoard(BufferedReader br, int width) throws IOException,
			SyntaxException {
		boardWidth = width;
		corridors = new BitSet();
		Pattern endRow = Pattern.compile("^-{" + width + "}$");

		int y;

		for (y = 0;; y++) {
			String line = readDataLine(br);
			if (line == null) {
				fail("Board definition must end with a sequence of " + width + " dashes");
			}
			else if (endRow.matcher(line).find()) {
				break;
			}
			else if (line.charAt(line.length() - 1) != '|') {
				fail("Each row of the board must end with a '|'");
			}

			line = line.substring(0, line.length() - 1);
			if (line.length() != width) {
				fail("Each row of the board must be exactly " + width
						+ " characters long (excluding the final '|')");
			}
			for (int x = 0; x < width; x++) {
				parseBoardCharacter(line.charAt(x), x, y, width, line);
			}
		}

		boardHeight = y + 1;
	}

	/**
	 * Attempts to parse the given character and update internal state depending
	 * on what it represents (corridor, door, room, start location, etc.)
	 *
	 * @param chr
	 *            character in the board definition
	 * @param x
	 *            x-position of the character
	 * @param y
	 *            y-position of the character
	 * @param width
	 *            width of the board
	 * @param line
	 *            the row that this character is part of
	 */
	private void parseBoardCharacter(char chr, int x, int y, int width,
			String line) throws SyntaxException {
		if (chr == ' ') {
			return;
		}
		else if (chr == '.') {
			corridors.set(x + (y * width));
		}
		else if (chr == '_' || chr == '/') {
			addDoor(chr, x, y, line);
		}
		else if (roomsById.containsKey(chr)) {
			roomsById.get(chr).addPoint(x, y);
		}
		else if (suspectsById.containsKey(chr)) {
			suspectsById.get(chr).setStartLocation(x, y);
			corridors.set(x + (y * width));
		}
		else {
			fail("Unknown character on board at (" + x + "," + y + "): " + chr);
		}
	}

	/**
	 * Creates a door at the specified position
	 *
	 * @param chr
	 *            either '/' (horizontal door) or '_' (vertical door)
	 * @param x
	 *            x-position of the door
	 * @param y
	 *            y-position of the door
	 * @param line
	 *            current board definition row (used to figure out which room
	 *            the door is part of)
	 * @throws SyntaxException
	 */
	private void addDoor(char chr, int x, int y, String line)
			throws SyntaxException {
		char beside = x == 0 ? line.charAt(x + 1) : line.charAt(x - 1);
		if (!roomsById.containsKey(beside) && x < line.length() - 1) {
			beside = line.charAt(x + 1);
		}

		Room room = roomsById.get(beside);
		if (room == null) {
			fail("Couldn't find room connected to door at (" + x + "," + y + ")");
		}

		room.addPoint(x, y);
		boolean isVertical = (chr == '/');
		room.addDoor(new Point(x, y), isVertical);
	}

	/**
	 * Convenience method to load all lines matching the specified pattern from
	 * the reader FAILS: Reads one line too many
	 *
	 * @param br
	 * @param pattern
	 * @param lookingFor
	 * @return
	 */
	private List<Matcher> loadEntries(BufferedReader br, Pattern pattern,
			String lookingFor) throws IOException, SyntaxException {
		List<Matcher> result = new ArrayList<Matcher>();
		String line;

		br.mark(MAX_LINE_LENGTH);
		while ((line = readDataLine(br)) != null) {
			if (!(line.startsWith(" ") || line.startsWith("\t"))) {
				br.reset();
				return result;
			}
			br.mark(MAX_LINE_LENGTH);

			Matcher matcher = pattern.matcher(line);
			if (!matcher.find()) {
				fail("Couldn't parse " + lookingFor + " entry");
			}
			result.add(matcher);
		}
		return result;
	}

	/**
	 * Returns the next relevant line from the reader (skips comments and empty
	 * lines)
	 *
	 * @param br
	 *            reader to read lines from
	 * @return
	 */
	private String readDataLine(BufferedReader br) throws IOException {
		String line;
		while ((line = br.readLine()) != null) {
			if (!COMMENT.matcher(line).find() && !line.isEmpty()) {
				return line;
			}
		}
		return null;
	}

	/**
	 * Throws a syntax error with the given message
	 */
	private void fail(String message) throws SyntaxException {
		throw new SyntaxException(message);
	}
}
