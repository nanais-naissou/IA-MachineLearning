package fr.polytech.mnia;

import de.prob.statespace.State;

public class TransitionDataTTT {
    public State state;
    public int action;
    public State nextState;
    

    public TransitionDataTTT(State state, int action, State nextState) {
        this.state = state;
        this.action = action;
        this.nextState = nextState;
     
    }

}