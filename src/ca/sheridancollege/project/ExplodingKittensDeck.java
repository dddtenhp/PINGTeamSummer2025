package ca.sheridancollege.project;

public class ExplodingKittensDeck extends GroupOfCards {

    public ExplodingKittensDeck() {
        super(56);
        for (int i = 0; i < 4; i++) {
            getCards().add(new ExplodingKittenCard());
            getCards().add(new SkipCard());
            getCards().add(new ShuffleCard());
            getCards().add(new FavorCard());
            
            // cat cards
            getCards().add(new TacocatCard());
            getCards().add(new HairyPotatoCatCard());
            getCards().add(new RainbowRalphingCatCard());
            getCards().add(new BeardCatCard());
            getCards().add(new CattermelonCard());
        }
        for (int i = 0; i < 6; i++) {
            getCards().add(new DefuseCard());
        }

        for (int i = 0; i < 5; i++) {
            getCards().add(new AttackCard());
            getCards().add(new Nope());
            getCards().add(new SeeTheFutureCard());
        }

        shuffle();
    }
}
