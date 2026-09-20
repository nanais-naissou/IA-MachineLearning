package fr.polytech.mnia.agents;

import java.util.Random;

import de.prob.statespace.State;

public class BanditGradientAgent implements Agent {

    private int nbActions;                 // total number of possible actions
    private double[] preferences;         // scores (preferences) for each action
    private double[] policy;              // action probabilities based on Softmax
    private double alpha;                 // learning rate (step size)
    private double averageReward;         // average reward so far
    private int timeStep;                 // count the number of steps to update the average
    private Random random;                // to choose a random action
    private State currentState;           // not used here

    public BanditGradientAgent(int nbActions, double alpha) {
        this.nbActions = nbActions;
        this.preferences = new double[nbActions];
        this.policy = new double[nbActions];
        this.alpha = alpha;
        this.averageReward = 0.0;
        this.timeStep = 0;
        this.random = new Random();
    }

    // update action probabilities (the policy) using Softmax
    private void updatePolicy() {
        double sumExp = 0.0;
        for (double pref : preferences) {
            sumExp += Math.exp(pref);  // compute the Softmax denominator
        }
        for (int i = 0; i < nbActions; i++) {
            policy[i] = Math.exp(preferences[i]) / sumExp;// each probability
        }
    }

    @Override
    public int chooseAction() {
        updatePolicy(); // update the policy before choosing
        double r = random.nextDouble();  // random draw between 0 and 1
        double cumulative = 0.0;
        for (int i = 0; i < nbActions; i++) {
            cumulative += policy[i];
            if (r < cumulative) return i; // return the corresponding action
        }
        return nbActions - 1; // if on tombe pas dans la boucle, on prend la derniere
    }

    @Override
    public void update(int action, double reward) {
        timeStep++; // +1 step
        // update the average reward using an incremental average
        averageReward += (reward - averageReward) / timeStep;
        updatePolicy();

        // adjust preferences according to the gradient rule
        for (int i = 0; i < nbActions; i++) {
            if (i == action) {
                preferences[i] += alpha * (reward - averageReward) * (1 - policy[i]);
            } else {
                preferences[i] -= alpha * (reward - averageReward) * policy[i];
            }
        }
    }

    public double[] getPolicy() {
        return policy; // useful for viewing the current probabilities
    }

    public double[] getPreferences() {
        return preferences; // debug ou analyse
    }

    @Override // not used in this context
    public void setCurrentState(State state) {
        this.currentState = state;
    }
}