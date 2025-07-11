package ca.sheridancollege.project;

import java.util.Scanner;

public class HumanPlayer extends ExplodingKittensPlayer {

    private Scanner input; // For player input

    public HumanPlayer(String name, Scanner scanner) {
        super(name);
        this.input = scanner; // Inject the scanner
    }

    @Override
    public void play() {
        System.out.println("\n--- " + getName() + "'s Turn (Human Player) ---");
        displayHand();

        System.out.println(getName() + ", press Enter to draw a card...");
        input.nextLine(); // Wait for user to press Enter
    }

    public void displayHand() {
        System.out.println(getName() + "'s hand:");
        if (getHand().isEmpty()) {
            System.out.println("  (Empty hand)");
            return;
        }
        for (int i = 0; i < getHand().size(); i++) {
            System.out.println("  " + (i + 1) + ". " + getHand().get(i).toString());
        }
    }
}