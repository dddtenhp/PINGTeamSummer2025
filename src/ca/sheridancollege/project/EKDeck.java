package ca.sheridancollege.project;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Random;

public class EKDeck extends GroupOfCards {

    private final ArrayList<Card> cards = new ArrayList<>();
    private final Deque<Card>    discard = new ArrayDeque<>();
    private final Random         rand    = new Random();

    public EKDeck() {
        super(56);
    }

    @Override
    public ArrayList<Card> getCards() {
        return cards;          
    }

    public void addTop(Card c)         { cards.add(0, c); }
    public Card draw()                 { return cards.remove(0); }
    public void discard(Card c)        { discard.push(c); }
    @Override
    public void shuffle()              { Collections.shuffle(cards, rand); }

    public static EKDeck standardDeck(int players) {
        EKDeck deck = new EKDeck();
        deck.addMany(CardType.EXPLODING_KITTEN, 4);
        deck.addMany(CardType.DEFUSE,           6);
        deck.addMany(CardType.ATTACK,           4);
        deck.addMany(CardType.SKIP,             4);
        deck.addMany(CardType.NOPE,             5);
        deck.addMany(CardType.FAVOR,            4);
        deck.addMany(CardType.SHUFFLE,          4);
        deck.addMany(CardType.SEE_THE_FUTURE,   5);
        deck.addMany(CardType.CAT,             20);
        deck.shuffle();
        return deck;
    }

    private void addMany(CardType t, int n) {
        for (int i = 0; i < n; i++) cards.add(new EKCard(t));
    }
}
