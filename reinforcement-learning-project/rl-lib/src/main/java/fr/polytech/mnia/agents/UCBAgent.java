package fr.polytech.mnia.agents;

import de.prob.statespace.State;

// Upper Confidence Bound agent: it explores intelligently by considering uncertainty
public class UCBAgent implements Agent {

    private int nbActions;             // total number of actions
    private double[] qValues;         // estimated average reward
    private int[] actionCounts;       // number of times each action was tested
    private int totalCount;           // total number of selected actions
    private double c;                 // confidence hyperparameter (exploration)
    private State currentState;           // not used here

    public UCBAgent(int nbActions, double c) {
        this.nbActions = nbActions;
        this.qValues = new double[nbActions];
        this.actionCounts = new int[nbActions];
        this.totalCount = 0;
        this.c = c; // the larger it is, the more we explore
    }

    @Override
    public int chooseAction() {
        totalCount++; // move to the next step

        // test each action at least once
        for (int i = 0; i < nbActions; i++) {
            if (actionCounts[i] == 0) {
                return i; // not tested yet, so test it directly
            }
        }

        int bestAction = 0;
        // UCB formula: Q + c * sqrt(ln(t)/N)
        double bestUCB = qValues[0] + c * Math.sqrt(Math.log(totalCount) / actionCounts[0]);

        for (int i = 1; i < nbActions; i++) {
            double ucb = qValues[i] + c * Math.sqrt(Math.log(totalCount) / actionCounts[i]);
            if (ucb > bestUCB) {
                bestUCB = ucb;
                bestAction = i;
            }
        }

        return bestAction; // return the most promising arm
    }

    @Override
    public void update(int action, double reward) {
        actionCounts[action]++; // maj le nb de times qu'on a pris cette action
        double alpha = 1.0 / actionCounts[action]; // update the incremental average
        qValues[action] += alpha * (reward - qValues[action]);
    }

    public double[] getQValues() {
        return qValues; // to view the scores
    }

    @Override // not used in this context
    public void setCurrentState(State state) {
        this.currentState = state;
    }
}