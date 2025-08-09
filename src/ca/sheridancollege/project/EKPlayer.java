package ca.sheridancollege.project;

import java.util.*;
import java.util.stream.Collectors;

public class EKPlayer extends Player {

    private final List<Card> hand = new ArrayList<>();
    private boolean alive = true;

    public EKPlayer(String name) {
        super(name);
    }

    public boolean isAlive() {
        return alive;
    }

    public List<Card> getHand() {
        return hand;
    }

    /* give random card for Favor or 2-of-a-kind steal */
    public Card giveRandomCard() {
        return hand.remove(new Random().nextInt(hand.size()));
    }

    /* ============================================================ */
 /*                            TURN                              */
 /* ============================================================ */
    @Override
    public void play() {
         }

    public void takeTurn(EKGame game, Scanner in) {

        int turns = game.getPendingTurns(this);

        while (turns-- > 0 && alive) {

            /* win check (can happen after steals etc.) */
            if (hand.isEmpty()) {
                System.out.println("\n*** " + getName()
                        + " has no cards left and WINS! ***");
                System.exit(0);
            }

            boolean endWithoutDraw = false;

            while (true) {
                displayHand();
                System.out.println("Choose ONE card # to play, or press Enter to pass:");
                String line = in.nextLine().trim();

                /* player passes → draw later */
                if (line.isEmpty()) {
                    break;
                }

                try {
                    int idx = Integer.parseInt(line) - 1;
                    if (idx < 0 || idx >= hand.size()) {
                        continue;
                    }

                    EKCard chosenPreview = (EKCard) hand.get(idx);

                    if (chosenPreview.getType() == CardType.CAT) {
                        long totalCats = hand.stream()
                                .filter(c -> ((EKCard) c).getType() == CardType.CAT)
                                .count();            
                        if (totalCats < 2) {
                            System.out.println("You need at least TWO Cat cards "
                                    + "for a combo. Cannot play a single Cat.");
                            continue;                            
                        }
                    }

                    /* remove and process */
                    EKCard chosen = (EKCard) hand.remove(idx);
                    endWithoutDraw = game.processCard(this, chosen, in);

                    /* win check after playing */
                    if (hand.isEmpty()) {
                        System.out.println("\n*** " + getName()
                                + " has no cards left and WINS! ***");
                        System.exit(0);
                    }
                    break;  
                } catch (NumberFormatException ignored) {
                }
            }

            if (!endWithoutDraw) {
                drawPhase(game, in);

                if (hand.isEmpty()) {
                    System.out.println("\n*** " + getName()
                            + " has no cards left and WINS! ***");
                    System.exit(0);
                }
            }
        }
    }

    private void drawPhase(EKGame game, Scanner in) {
        Card drawn = game.getDeck().draw();
        System.out.println(getName() + " draws a card: " + drawn);

        if (((EKCard) drawn).getType() == CardType.EXPLODING_KITTEN) {

            Optional<Card> defuse = hand.stream()
                    .filter(c -> ((EKCard) c).getType() == CardType.DEFUSE)
                    .findFirst();

            if (defuse.isPresent()) {
                hand.remove(defuse.get());
                game.getDeck().discard(defuse.get());
                System.out.println(getName() + " used a DEFUSE!");

                int pos = new Random().nextInt(game.getDeck().getCards().size() + 1);
                game.getDeck().getCards().add(pos, drawn);

            } else {
                System.out.println(getName() + " exploded!");
                alive = false;
            }
        } else {
            hand.add(drawn);
        }
    }

    public List<CardType> getDuplicates(int count) {
        Map<CardType, Long> freq = hand.stream()
                .collect(Collectors.groupingBy(
                        c -> ((EKCard) c).getType(), Collectors.counting()));
        return freq.entrySet().stream()
                .filter(e -> e.getValue() >= count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void displayHand() {
        System.out.println("\n" + getName() + "'s hand:");
        for (int i = 0; i < hand.size(); i++) {
            System.out.printf("[%d] %s%n", i + 1, hand.get(i));
        }
    }
}
