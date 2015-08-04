package cluedo.game.objects;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import cluedo.game.Door;

public class Room implements Card {

	private char id;
	private final String name;
	private Weapon weapon;
	private Room passageExit;
	private List<Door> doors = new ArrayList<Door>();

	private List<Point> points = new ArrayList<Point>();
	private Rectangle boundingBox;

	private List<Suspect> occupants = new ArrayList<Suspect>();

	public Room(char id, String name){
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
		if (x < boundingBox.x) {
			boundingBox.width += (boundingBox.x - x);
			boundingBox.x = x;
		}
		else if (x > (boundingBox.x + boundingBox.width)) {
			boundingBox.width = (x - boundingBox.x);
		}
	}

	public List<Point> getPoints(){
		return this.points;
	}

	public List<Door> getDoors(){
		return this.doors;
	}

	public Door getDoor(int number){									//For when doors are displayed as numbers
		return doors.get(number);
	}

	public void setPassageExit(Room room){
		this.passageExit = room;
	}

	public Room getPassageExit(){
		return this.passageExit;
	}

	public Point getCenterPoint(){
		return null;
	}

	public void addDoor(Point point, boolean isVertical) {
		doors.add(new Door(this, point, isVertical));
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}

}