package cluedo.game.objects;

public class Weapon implements Card {

	String name;
	Room room;

	public Weapon(String name){
		this.name = name;

	}

	@Override
	public String getName() {

		// TODO Auto-generated method stub
		return name;
	}

	public void setRoom(Room room){
		this.room = room;
	}

	public Room getRoom(){
		return this.room;
	}

}
