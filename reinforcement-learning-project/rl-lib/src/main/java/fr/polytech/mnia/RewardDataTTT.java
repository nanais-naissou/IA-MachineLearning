package fr.polytech.mnia;

import de.prob.statespace.State;

public class RewardDataTTT {
    public State state;
    public int action;
    public double reward;
  

    public RewardDataTTT(State state, int action, double reward) {
        this.state = state;
        this.action = action;
        this.reward = reward;
       
    }
}