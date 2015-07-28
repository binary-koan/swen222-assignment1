package loader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;

import game.GameData;
import game.objects.Room;
import game.objects.Suspect;
import game.objects.Weapon;

public class Loader {
	private String filename;
	private List<Room> rooms;
	private List<Suspect> suspects;
	private List<Weapon> weapons;

	public Loader(String filename) throws FileNotFoundException {
		this.filename = filename;
		loadData();
	}

	private void loadData() throws FileNotFoundException {
		Scanner scanner = new Scanner(new FileInputStream(filename));
		if (!scanner.hasNext("---")) {
			fail(scanner, "YAML header not found");
		}
		scanner.next("---");
		loadGroup(scanner);
	}

	private void loadGroup(Scanner scanner) {
		if (scanner.hasNext("[a-z]+:")) {
			String title = scanner.next("[a-z]+:");
			switch(title) {
			case "suspects":
				loadSuspects(scanner);
				break;
			case "rooms":
				loadRooms(scanner);
				break;
			case "passages":
				loadPassages(scanner);
				break;
			case "weapons":
				loadWeapons(scanner);
				break;
			default:
				fail(scanner, "Unrecognized group: " + title);
			}
		}
	}

	private void loadSuspects(Scanner scanner) {
		suspects = new ArrayList<Suspect>();
		while(scanner.hasNext("^\\s+([a-z])\\s*:\\s*([\\w\\.\\-]+)\\s*\\[([a-z]+)\\]")) {
			scanner.next("...");
			MatchResult match = scanner.match();
			// suspects.add(match.group(0), match.group(1), match.group(2));
		}
	}

	private void loadRooms(Scanner scanner) {
	}

	private void loadPassages(Scanner scanner) {
	}

	private void loadWeapons(Scanner scanner) {
	}

	private void fail(Scanner scanner, String message) {
		scanner.close();
		//TODO: Change to something catch'y
		throw new RuntimeException(message);
	}
}
