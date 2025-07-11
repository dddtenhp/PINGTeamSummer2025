package ca.sheridancollege.project;

public class ExplodingKittensGame extends Game {
    private ExplodingKittensDeck deck;

    public ExplodingKittensGame(String name) {
        super(name);
        deck = new ExplodingKittensDeck();
    }

    @Override
    public void play() {
        System.out.println("Starting Exploding Kittens...");

        while (getPlayers().size() > 1 && deck.getCards().size() > 0) {
            for (int i = 0; i < getPlayers().size(); i++) {
                ExplodingKittensPlayer player = (ExplodingKittensPlayer) getPlayers().get(i);
                player.play();

                Card drawn = deck.getCards().remove(0);
                System.out.println(player.getName() + " draws a card: " + drawn);

                if (drawn instanceof ExplodingKittenCard) {
                    if (player.hasDefuseCard()) {
                        player.playDefuseCard();
                        deck.getCards().add(new ExplodingKittenCard());
                        deck.shuffle();
                    } else {
                        System.out.println(player.getName() + " exploded and is eliminated!");
                        getPlayers().remove(player);
                        i--; 
                    }
                } else {
                    player.drawCard(drawn);
                }
            }
        }
    }

    @Override
    public void declareWinner() {
        if (getPlayers().size() == 1) {
            System.out.println("Winner: " + getPlayers().get(0).getName());
        } else {
            System.out.println("No winner — deck exhausted.");
        }
    }
}
