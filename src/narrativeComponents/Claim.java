package narrativeComponents;

public class Claim extends Edge {

    //Flags to set whether a claim connects an event to an entity/literal (binary) or whether it is a boolean value (unary)
    boolean isUnary;

    public Claim(String label, Event nodeA, Node nodeB) {
        super(label, nodeA, nodeB);
        this.isUnary = true;
    }

    public Event getNodeA() {
        return (Event) this.nodeA;
    }

    public Event getEventB() {
        return (Event)this.nodeB;
    }

    public Entity getNodeB() {
        if (isUnary) {
            return null;
        }
        return (Entity)this.nodeB;
    }

    public boolean isBinary() {
        return !isUnary;
    }

    public void setBinary(boolean binary) {
        isUnary = !binary;
    }
}
