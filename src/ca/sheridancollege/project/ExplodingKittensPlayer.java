package ca.sheridancollege.project;
import java.util.ArrayList;

public class ExplodingKittensPlayer extends Player {
    private ArrayList<Card> hand;

    public ExplodingKittensPlayer(String name) {
        super(name);
        hand = new ArrayList<>();
    }

    public ArrayList<Card> getHand() { return hand; }

    public void drawCard(Card card) {
        hand.add(card);
        System.out.println(getName() + " drew: " + card);
    }

    public boolean hasDefuseCard() {
        return hand.stream().anyMatch(c -> c instanceof DefuseCard);
    }

    public void playDefuseCard() {
        hand.removeIf(c -> c instanceof DefuseCard);
        System.out.println(getName() + " played a Defuse card.");
    }

    @Override
    public void play() {
        System.out.println(getName() + "'s turn begins.");
        // Game play logic handled in ExplodingKittensGame
    }
}
