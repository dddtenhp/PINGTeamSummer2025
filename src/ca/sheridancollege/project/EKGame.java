package ca.sheridancollege.project;

import java.util.*;
import java.util.stream.Collectors;

public class EKGame extends Game {

    private static EKGame INSTANCE;
    public static synchronized EKGame getInstance(String name, List<EKPlayer> players, EKDeck starterDeck) {
        if (INSTANCE == null) {
            INSTANCE = new EKGame(name, players, starterDeck);
        }
        return INSTANCE;
    }
    private EKGame(String name, List<EKPlayer> players, EKDeck starterDeck) {
        super(name);
        setPlayers(new ArrayList<>(players));
        this.deck = starterDeck;
    }

    private final EKDeck deck;
    private final Map<EKPlayer, Integer> pendingTurns = new HashMap<>();
    private int current = 0;

    public EKDeck getDeck() { return deck; }

    public int getPendingTurns(EKPlayer p) {
        return pendingTurns.getOrDefault(p, 1);
    }

    @Override
    public void play() {
        Scanner in = new Scanner(System.in);
        while (alivePlayers().size() > 1) {
            EKPlayer player = alivePlayers().get(current);
            System.out.println("\n=== " + player.getName() + "'s turn ===");
            player.takeTurn(this, in);

            // Clear consumed pending turns for this player (EKPlayer should loop internally).
            pendingTurns.remove(player);

            // Advance to next alive player
            current = (current + 1) % alivePlayers().size();
        }
        declareWinner();
    }

    @Override
    public void declareWinner() {
        System.out.println("\n*** " + alivePlayers().get(0).getName() + " WINS! ***");
    }

    public boolean processCard(EKPlayer user, EKCard card, Scanner in) {

        CardType t = card.getType();

        deck.discard(card);

        boolean actionCancelable =
                t != CardType.NOPE &&
                t != CardType.DEFUSE &&
                t != CardType.EXPLODING_KITTEN &&
                t != CardType.CAT;

        if (actionCancelable) {
            boolean canceled = offerNopeWar(user, in);
            if (canceled) {
                System.out.println("Action was NOPE'd and is cancelled. You may continue your turn.");
                return false; 
            }
        }

        boolean endTurn = false;

        switch (t) {
            case SKIP:
                endTurn = true;
                break;

            case ATTACK: {
                EKPlayer next = nextPlayer();
                int turns = pendingTurns.getOrDefault(next, 1) + 1;
                pendingTurns.put(next, turns);
                endTurn = true;
                System.out.println("ATTACK played. " + next.getName() + " now has " + turns + " turn(s) pending.");
                break;
            }

            case SHUFFLE:
                deck.shuffle();
                System.out.println("Deck shuffled.");
                endTurn = false;
                break;

            case SEE_THE_FUTURE:
                peekTop(3);
                endTurn = false;
                break;

            case FAVOR:
                favor(user, in);
                endTurn = false;
                break;

            case CAT:
                comboCheck(user, in);
                endTurn = false;
                break;

            default:
                endTurn = false;
                break;
        }

        return endTurn;
    }

    private boolean offerNopeWar(EKPlayer actor, Scanner in) {
        int nopeCount = 0;

        while (true) {
            boolean someonePlayedThisRound = false;

            for (EKPlayer p : alivePlayers()) {
                if (p == actor) continue;

                Optional<Card> nopeCard = p.getHand().stream()
                        .filter(c -> ((EKCard) c).getType() == CardType.NOPE)
                        .findFirst();

                if (nopeCard.isPresent()) {
                    System.out.println(p.getName() + ", you have a NOPE. Play it? (y/n)");
                    String ans = in.nextLine().trim();
                    if (ans.equalsIgnoreCase("y")) {
                        p.getHand().remove(nopeCard.get());
                        deck.discard(nopeCard.get());
                        nopeCount++;
                        someonePlayedThisRound = true;

                        System.out.println("NOPE played! (Total NOPEs: " + nopeCount + ")");
                    }
                }
            }

            if (!someonePlayedThisRound) {
                break; 
            } else {
                System.out.println("Counter-NOPE window... (another round of offers)");
            }
        }

        boolean cancelled = (nopeCount % 2 == 1);
        if (!cancelled && nopeCount > 0) {
            System.out.println("NOPEs cancelled out. Action proceeds.");
        }
        return cancelled;
    }

    private void peekTop(int n) {
        List<Card> cards = deck.getCards();
        int size = cards.size();
        if (size == 0) {
            System.out.println("Deck is empty.");
            return;
        }
        int show = Math.min(n, size);
        System.out.println("Top " + show + " card(s):");
        cards.stream().limit(show).forEach(System.out::println);
    }

    private void favor(EKPlayer user, Scanner in) {
        EKPlayer target = choosePlayer(user, in);
        if (target == null) {
            System.out.println("No valid target for FAVOR.");
            return;
        }
        if (target.getHand().isEmpty()) {
            System.out.println(target.getName() + " has no cards. FAVOR fizzles.");
            return;
        }
        Card received = target.giveRandomCard();
        if (received != null) {
            user.getHand().add(received);
            System.out.println(user.getName() + " received " + received + " from " + target.getName());
        } else {
            System.out.println("Could not take a card (target hand empty).");
        }
    }

    private void comboCheck(EKPlayer user, Scanner in) {
        List<CardType> triples = user.getDuplicates(3).stream()
                .filter(t -> t == CardType.CAT)
                .collect(Collectors.toList());
        List<CardType> pairs = user.getDuplicates(2).stream()
                .filter(t -> t == CardType.CAT)
                .collect(Collectors.toList());

        if (!triples.isEmpty()) {
            System.out.println("Play 3-of-a-kind for named steal? (y/n)");
            if (yes(in)) {

                if (offerNopeWar(user, in)) {
                    System.out.println("Combo was NOPE'd!");
                    return;
                }

                CardType chosen = triples.get(0);
                removeCards(user, chosen, 3);
                EKPlayer target = choosePlayer(user, in);
                if (target == null) {
                    System.out.println("No valid target. Combo cancelled.");
                    return;
                }

                System.out.println("Name a card type to steal (e.g., SEE_THE_FUTURE, FAVOR, DEFUSE, etc.):");
                String raw = in.nextLine().trim().toUpperCase().replace(' ', '_');
                CardType named;
                try {
                    named = CardType.valueOf(raw);
                } catch (IllegalArgumentException ex) {
                    System.out.println("Invalid card type. Combo cancelled.");
                    return;
                }
                stealNamed(user, target, named);
            }

        } else if (!pairs.isEmpty()) {
            System.out.println("Play 2-of-a-kind to steal random card? (y/n)");
            if (yes(in)) {

                // Can be NOPE'd
                if (offerNopeWar(user, in)) {
                    System.out.println("Combo was NOPE'd!");
                    return;
                }

                CardType chosen = pairs.get(0);
                removeCards(user, chosen, 2);
                EKPlayer target = choosePlayer(user, in);
                if (target == null) {
                    System.out.println("No valid target. Combo cancelled.");
                    return;
                }
                if (target.getHand().isEmpty()) {
                    System.out.println(target.getName() + " has no cards to steal.");
                    return;
                }
                Card taken = target.giveRandomCard();
                if (taken != null) {
                    user.getHand().add(taken);
                    System.out.println("Stole " + taken + " from " + target.getName());
                } else {
                    System.out.println("Failed to steal (target hand empty).");
                }
            }
        }
    }

    private boolean yes(Scanner in) {
        String s = in.nextLine().trim();
        return s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes");
    }

    private void stealNamed(EKPlayer user, EKPlayer target, CardType named) {
        Optional<Card> found = target.getHand().stream()
                .filter(c -> ((EKCard) c).getType() == named)
                .findFirst();
        if (found.isPresent()) {
            target.getHand().remove(found.get());
            user.getHand().add(found.get());
            System.out.println("Received " + found.get());
        } else {
            System.out.println(target.getName() + " has none.");
        }
    }

    private void removeCards(EKPlayer player, CardType t, int count) {
        Iterator<Card> it = player.getHand().iterator();
        while (it.hasNext() && count > 0) {
            if (((EKCard) it.next()).getType() == t) {
                it.remove();
                count--;
            }
        }
    }

    private EKPlayer choosePlayer(EKPlayer exclude, Scanner in) {
        List<EKPlayer> others = alivePlayers().stream()
                .filter(p -> p != exclude)
                .collect(Collectors.toList());
        if (others.isEmpty()) {
            return null;
        }

        while (true) {
            System.out.println("Choose a player:");
            for (int i = 0; i < others.size(); i++) {
                System.out.printf("[%d] %s%n", i + 1, others.get(i).getName());
            }
            String raw = in.nextLine().trim();
            try {
                int sel = Integer.parseInt(raw) - 1;
                if (sel >= 0 && sel < others.size()) {
                    return others.get(sel);
                }
            } catch (NumberFormatException ignored) { }
            System.out.println("Invalid selection. Try again.");
        }
    }

    private EKPlayer nextPlayer() {
        List<EKPlayer> list = alivePlayers();
        return list.get((current + 1) % list.size());
    }

    private List<EKPlayer> alivePlayers() {
        return getPlayers().stream()
                .map(p -> (EKPlayer) p)
                .filter(EKPlayer::isAlive)
                .collect(Collectors.toList());
    }
}
