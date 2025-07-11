package ca.sheridancollege.project;

import java.util.Random;

public class ComputerPlayer extends ExplodingKittensPlayer{
    private Random random = new Random();
    
    public ComputerPlayer(String name){
        super(name);
    }
    @Override
    public void play() {
        System.out.println("\n--- " + getName() + "'s Turn ---");
        
        // Simulate thinking time
        try {
            Thread.sleep(1000); // Wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println(getName() + " is drawing a card...");
    }

}
