package fr.polytech.mnia.agents;

import java.util.Random;

import de.prob.statespace.State;

public class EpsilonGreedyAgent implements Agent {

    private int nbActions;               // number of available actions
    private double[] qValues;           // reward estimates for each action
    private int[] actionCounts;         // number of times each action was selected
    private double epsilon;             // probability of exploring instead of exploiting
    private Random random;
    private State currentState;           // not used here

    public EpsilonGreedyAgent(int nbActions, double epsilon) {
        this.nbActions = nbActions;
        this.qValues = new double[nbActions];   // initialize to 0
        this.actionCounts = new int[nbActions];
        this.epsilon = epsilon;
        this.random = new Random(); // for exploration
    }

    @Override
    public int chooseAction() {
        if (random.nextDouble() < epsilon) {
            return random.nextInt(nbActions); // random exploration: test a random arm
        } else {
            int bestAction = 0;
            double bestValue = qValues[0];
            for (int i = 1; i < nbActions; i++) {
                if (qValues[i] > bestValue) {
                    bestValue = qValues[i];
                    bestAction = i;
                }
            }
            return bestAction; // take the best known action so far
        }
    }

    @Override
    public void update(int action, double reward) {
        actionCounts[action]++; // record how many times this action was selected
        double alpha = 1.0 / actionCounts[action]; // incremental average
        qValues[action] += alpha * (reward - qValues[action]); // update Q-value
    }

    public double[] getQValues() {
        return qValues; // to see the current progress
    }

    public int[] getActionCounts() {
        return actionCounts; // for statistics/debugging
    }

    @Override // not used in this context
    public void setCurrentState(State state) {
        this.currentState = state;
    }
}