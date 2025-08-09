package ca.sheridancollege.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Number of players (2-5): ");
        int n = Integer.parseInt(in.nextLine().trim());

        List<EKPlayer> players = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            System.out.print("Player " + i + " name: ");
            players.add(new EKPlayer(in.nextLine().trim()));
        }

        EKDeck deck = EKDeck.standardDeck(n);

        List<Card> kittens = deck.getCards().stream()
                .filter(c -> ((EKCard) c).getType() == CardType.EXPLODING_KITTEN)
                .collect(Collectors.toList());
        deck.getCards().removeAll(kittens);

        List<Card> defuses = deck.getCards().stream()
                .filter(c -> ((EKCard) c).getType() == CardType.DEFUSE)
                .collect(Collectors.toList());
        deck.getCards().removeAll(defuses);

        for (EKPlayer p : players) {
            p.getHand().add(defuses.remove(0));
            for (int i = 0; i < 7; i++) p.getHand().add(deck.draw());
        }

        deck.getCards().addAll(defuses);
        deck.getCards().addAll(kittens.subList(0, n - 1));
        deck.shuffle();

        EKGame game = EKGame.getInstance("Exploding Kittens", players, deck);
        game.play();
    }
}
