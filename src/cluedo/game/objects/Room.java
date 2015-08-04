package cluedo.game.objects;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import cluedo.game.Door;

public class Room implements Card {

	private String id;
	private final String name;
	private Weapon weapon;
	private Room passageExit;
	private ArrayList<Door> doors;

	private ArrayList<Point> points = new ArrayList<Point>();
	private ArrayList<Suspect> occupants = new ArrayList<Suspect>();

	public Room(String id, String name){
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
	public ArrayList<Suspect> getOccupants(){
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
	}

	public ArrayList<Point> getPoints(){
		return this.points;
	}

	public ArrayList<Door> getDoors(){
		return this.doors;
	}

	public Door getDoor(int number){									//For when doors are displayed as numbers
		return doors.get(number);
	}

	public void addDoor(Door door) {									//Are rooms adding doors?
		this.doors.add(door);
	}

	public void setPassageExit(Room room){
		this.passageExit = room;
	}

	public Room getPassageExit(){
		return this.passageExit;
	}

	public void getCenterPoint(){

	}

}