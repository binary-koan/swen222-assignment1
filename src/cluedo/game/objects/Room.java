package cluedo.game.objects;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cluedo.game.Door;

public class Room implements Card {
	public class BoundingBox {
		private int minX = Integer.MAX_VALUE;
		private int maxX = Integer.MIN_VALUE;
		private int minY = Integer.MAX_VALUE;
		private int maxY = Integer.MIN_VALUE;

		public int getMinX() {
			return minX;
		}
		public int getMaxX() {
			return maxX;
		}
		public int getMinY() {
			return minY;
		}
		public int getMaxY() {
			return maxY;
		}
	}

	private char id;
	private final String name;
	private Weapon weapon;
	private Room passageExit;
	private List<Door> doors = new ArrayList<Door>();

	private Set<Point> points = new HashSet<Point>();
	private BoundingBox boundingBox = new BoundingBox();

	private List<Suspect> occupants = new ArrayList<Suspect>();

	public Room(char id, String name) {
		this.name = name;
		this.id = id;
	}

	/**
	 * Returns the name of the room.
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	/**
	 * Adds an occupant to the list of tokens in a room.
	 * @param character
	 */
	public void addOccupent(Suspect character){
		occupants.add(character);
	}

	/**
	 * Returns the occupants/tokens in the room.
	 * @return
	 */
	public List<Suspect> getOccupants(){
		return occupants;
	}

	/**
	 * Removes an occupant from the list of tokens in a room.
	 * @param character
	 */
	public void removeOccupant(Suspect character){
		occupants.remove(character);
	}

	public void setWeapon(Weapon weapon){
		this.weapon = weapon;
	}

	/**
	 * Returns the weapon in this room, null if none.
	 */
	public Weapon getWeapon(){
		return this.weapon;

	}

	public void addPoint(int x, int y){
		points.add(new Point(x, y));

		if (x < boundingBox.minX) {
			boundingBox.minX = x;
		}
		if (x > boundingBox.maxX) {
			boundingBox.maxX = x;
		}
		if (y < boundingBox.minY) {
			boundingBox.minY = y;
		}
		if (y > boundingBox.maxY) {
			boundingBox.maxY = y;
		}
	}

	public Set<Point> getPoints(){
		return this.points;
	}

	public List<Door> getDoors(){
		return this.doors;
	}

	public Door getDoor(int index) {
		return doors.get(index);
	}

	public void setPassageExit(Room room){
		this.passageExit = room;
	}

	public Room getPassageExit(){
		return this.passageExit;
	}

	public Point getCenterPoint(){
		return new Point(
			boundingBox.minX + (boundingBox.maxX - boundingBox.minX) / 2,
			boundingBox.minY + (boundingBox.maxY - boundingBox.minY) / 2
		);
	}

	public void addDoor(Point point, boolean isVertical) {
		doors.add(new Door(this, point, isVertical));
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

}