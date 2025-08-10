package ca.sheridancollege.project;

public final class EKCard extends Card {

    private final CardType type;

    public EKCard(CardType type) {
        this.type = type;
    }

    public CardType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.name().replace('_', ' ');
    }
}
