package cluedo.game;

import java.util.ArrayList;

import cluedo.game.objects.Card;
import cluedo.game.objects.Room;
import cluedo.game.objects.Suspect;
import cluedo.game.objects.Weapon;

public class Solution {
	public ArrayList<Card> hand;

	public Solution(){

	}

	public void addCard(Card card) {
		this.hand.add(card);
	}
}
