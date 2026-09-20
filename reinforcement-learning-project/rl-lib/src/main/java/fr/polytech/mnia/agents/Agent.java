package fr.polytech.mnia.agents;
import de.prob.statespace.State;

public interface Agent {
    int chooseAction();
    void update(int action, double reward);
    void setCurrentState(State state);
}
