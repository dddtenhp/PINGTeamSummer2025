// ExplodingKittensDeck.java
package ca.sheridancollege.project;

import java.util.ArrayList; // Make sure ArrayList is imported

public class ExplodingKittensDeck extends GroupOfCards {

    public ExplodingKittensDeck() {
        super(0); // Initial size can be 0. Actual size will be determined by cards added.
        cards = new ArrayList<>(); // Initialize the ArrayList (assuming GroupOfCards 'cards' is protected or package-private)
                                  // If 'cards' is private in GroupOfCards, you'll need to use getCards().

        // Add action and cat cards (EXCLUDING Exploding Kittens and Defuse cards)
        // Based on your previous counts:
        for (int i = 0; i < 4; i++) { 
            getCards().add(new SkipCard());
            getCards().add(new ShuffleCard());
            getCards().add(new FavorCard());
            getCards().add(new AttackCard());

            // Cat cards (4 of each type)
            getCards().add(new TacocatCard());
            getCards().add(new HairyPotatoCatCard());
            getCards().add(new RainbowRalphingCatCard());
            getCards().add(new BeardCatCard());
            getCards().add(new CattermelonCard());
        }
        
        for (int i = 0; i < 5; i++) { 
            getCards().add(new Nope());
            getCards().add(new SeeTheFutureCard());
        }
    }
}