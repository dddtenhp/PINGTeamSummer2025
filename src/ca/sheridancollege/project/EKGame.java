package ca.sheridancollege.project;

import java.util.*;
import java.util.stream.Collectors;

public class EKGame extends Game {

    private final EKDeck deck;

    private final Map<EKPlayer, Integer> pendingTurns = new HashMap<>();
    private int current = 0;

    public EKGame(String name, List<EKPlayer> players, EKDeck starterDeck) {
        super(name);
        setPlayers(new ArrayList<>(players));
        this.deck = starterDeck;
    }

    public EKDeck getDeck() {
        return deck;
    }

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

            pendingTurns.remove(player);                       
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

        if (t != CardType.NOPE
                && t != CardType.DEFUSE
                && t != CardType.EXPLODING_KITTEN
                && t != CardType.CAT) 
        {
            if (offerNope(user, in)) {
                System.out.println("Action was NOPE'd! Turn ends.");
                return true;             
            }
        }

        boolean endTurn = false;

        switch (t) {
            case SKIP:
                endTurn = true;
                break;

            case ATTACK:
                EKPlayer next = nextPlayer();
                int turns = pendingTurns.getOrDefault(next, 1) + 1; 
                pendingTurns.put(next, turns);
                endTurn = true;
                break;

            case SHUFFLE:
                deck.shuffle();
                endTurn = true;
                break;

            case SEE_THE_FUTURE:
                peekTop(3);
                endTurn = true;           
                break;

            case FAVOR:
                favor(user, in);
                endTurn = true;
                break;

            case CAT:
                comboCheck(user, in);
                endTurn = true;
                break;

            default:
                break;
        }
        return endTurn;
    }

    private boolean offerNope(EKPlayer actor, Scanner in) {
        List<EKPlayer> others = alivePlayers().stream()
                .filter(p -> p != actor)
                .collect(Collectors.toList());

        for (EKPlayer p : others) {
            Optional<Card> nope = p.getHand().stream()
                    .filter(c -> ((EKCard) c).getType() == CardType.NOPE)
                    .findFirst();
            if (nope.isPresent()) {
                System.out.println(p.getName() + ", you have a NOPE. Play it? (y/n)");
                if (in.nextLine().trim().equalsIgnoreCase("y")) {
                    p.getHand().remove(nope.get());
                    deck.discard(nope.get());
                    return true;           
                }
            }
        }
        return false;
    }

    private void peekTop(int n) {
        System.out.println("Top " + n + " cards:");
        deck.getCards().stream().limit(n).forEach(System.out::println);
    }

    private void favor(EKPlayer user, Scanner in) {
        EKPlayer target = choosePlayer(user, in);
        if (target == null) {
            return;
        }
        Card received = target.giveRandomCard();
        user.getHand().add(received);
        System.out.println(user.getName() + " received " + received
                + " from " + target.getName());
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
            if (in.nextLine().trim().equalsIgnoreCase("y")) {

                if (offerNope(user, in)) {
                    System.out.println("Combo was NOPE'd!");
                    return;
                }

                CardType chosen = triples.get(0);
                removeCards(user, chosen, 3);
                EKPlayer target = choosePlayer(user, in);

                System.out.println("Name a card type to steal "
                        + Arrays.stream(CardType.values())
                                .map(Enum::name)
                                .map(s -> s.replace('_', ' '))
                                .collect(Collectors.joining(", ")) + ":");

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
            if (in.nextLine().trim().equalsIgnoreCase("y")) {

                if (offerNope(user, in)) {
                    System.out.println("Combo was NOPE'd!");
                    return;
                }

                CardType chosen = pairs.get(0);
                removeCards(user, chosen, 2);
                EKPlayer target = choosePlayer(user, in);
                Card taken = target.giveRandomCard();
                user.getHand().add(taken);
                System.out.println("Stole " + taken + " from " + target.getName());
            }
        }
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

    /* ---------- player utilities ---------- */
    private EKPlayer choosePlayer(EKPlayer exclude, Scanner in) {
        List<EKPlayer> others = alivePlayers().stream()
                .filter(p -> p != exclude)
                .collect(Collectors.toList());
        if (others.isEmpty()) {
            return null;
        }
        for (int i = 0; i < others.size(); i++) {
            System.out.printf("[%d] %s%n", i + 1, others.get(i).getName());
        }
        int sel = Integer.parseInt(in.nextLine().trim()) - 1;
        return others.get(sel);
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

