package cluedo.game.objects;

public class Weapon implements Card {

	private String name;
	private Room room;

	public Weapon(String name){
		this.name = name;

	}

	public String getName() {
		return name;
	}

	public void setRoom(Room room){
		this.room = room;
	}

	public Room getRoom(){
		return this.room;
	}

}
