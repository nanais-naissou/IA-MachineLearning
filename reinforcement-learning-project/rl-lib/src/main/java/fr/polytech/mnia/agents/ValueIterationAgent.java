package fr.polytech.mnia.agents;

import java.util.LinkedList;
import java.util.List;
import de.prob.statespace.State;
import fr.polytech.mnia.RewardDataTTT;
import fr.polytech.mnia.TransitionDataTTT;
import fr.polytech.mnia.TicTacToeRunner;


public class ValueIterationAgent implements Agent {
    private int nbActions;
    private List<TransitionDataTTT> transitionList;
    private List<RewardDataTTT> rewardList;
    private List<StateValue> values;
    private double gamma;
    private double epsilon;
    private State currentState;
    private TicTacToeRunner runner;
    private boolean valueComputed = false;
    private int MIN_EXPERIENCE = 2000; // minimum number of visited states before learning (exploration phase)

    // inner class used to store state values
    private static class StateValue {
        State state;
        double value;

        public StateValue(State state, double value) {
            this.state = state;
            this.value = value;
        }
    }
    
    public ValueIterationAgent(int nbActions, List<TransitionDataTTT> transitionList, 
                               List<RewardDataTTT> rewardList, double gamma, double epsilon, TicTacToeRunner runner) {
        this.nbActions = nbActions;
        this.transitionList = transitionList;
        this.rewardList = rewardList;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.values = new LinkedList<>();
        this.runner = runner;
        this.currentState = runner.getCurrentState();
        this.initializeValues(); 
        this.valueIteration(); 
    }

    public void valueIteration() {
        boolean converged;

        do {
            converged = true;
            List<StateValue> newValues = new LinkedList<>();

            for (StateValue sv : values) {
                State s = sv.state;
                double maxQ = Double.NEGATIVE_INFINITY;

                // for each possible action, compute the expected Q-value
                for (int a = 0; a < nbActions; a++) {
                    double q = 0;
                    for (TransitionDataTTT t : transitionList) {
                        if (t.state.equals(s) && t.action == a) {
                            q += (getReward(s, a) + gamma * getValue(t.nextState));
                        }
                    }
                    maxQ = Math.max(maxQ, q); // keep the best action
                }

                newValues.add(new StateValue(s, maxQ));

                if (Math.abs(maxQ - sv.value) > epsilon) {
                    converged = false;
                }
            }

            // replace the old values with the new ones
            values.clear();
            values.addAll(newValues);
        } while (!converged); // jusqu'a convergence
    }

    // choose the action to play using the epsilon-greedy strategy
    @Override
    public int chooseAction() {
        // random exploration if there is not enough data
        if (transitionList.size() < MIN_EXPERIENCE) {
            return (int) (Math.random() * nbActions);
        }

        // exploitation: choose the action with the highest Q-value
        double maxQ = Double.NEGATIVE_INFINITY;
        int bestAction = -1;

        for (int a = 0; a < nbActions; a++) {
            double q = 0;
            for (TransitionDataTTT t : transitionList) {
                if (t.state.equals(currentState) && t.action == a) {
                    q += (getReward(currentState, a) + gamma * getValue(t.nextState));
                }
            }
            if (q > maxQ) {
                maxQ = q;
                bestAction = a;
            }
        }

        return bestAction;
    }

    // update the agent's knowledge after each move
    @Override
    public void update(int action, double reward) {
        State nextState = runner.getCurrentState();

        // add the transition and reward
        transitionList.add(new TransitionDataTTT(currentState, action, nextState));
        rewardList.add(new RewardDataTTT(currentState, action, reward));

        // add states to the value table if they are new
        if (getStateValue(currentState) == null) {
            values.add(new StateValue(currentState, 0.0));
        }
        if (getStateValue(nextState) == null) {
            values.add(new StateValue(nextState, 0.0));
        }

        // update the value to speed up learning according to the course formula
        updateValue(currentState, reward + gamma * getValue(nextState));

        // run Value Iteration once enough experience has been collected
        if (transitionList.size() >= MIN_EXPERIENCE && !valueComputed) {
            valueIteration();
            valueComputed = true;
            System.out.println("VI active");
        }
    }

    // eenvoie la recompense for une transition (state+ action)
    private double getReward(State state, int action) {
        for (RewardDataTTT r : rewardList) {
            if (r.state.equals(state) && r.action == action) {
                return r.reward;
            }
        }
        return 0.0;
    }

    // return the estimated value of a state
    private double getValue(State state) {
        for (StateValue sv : values) {
            if (sv.state.equals(state)) {
                return sv.value;
            }
        }
        return 0.0;
    }

    // initialize values to 0 for all known states
    private void initializeValues() {
        for (TransitionDataTTT t : transitionList) {
            if (getStateValue(t.state) == null) {
                values.add(new StateValue(t.state, 0.0));
            }
            if (getStateValue(t.nextState) == null) {
                values.add(new StateValue(t.nextState, 0.0));
            }
        }
    }

    // returns la paire (state, value) if elle existe
    private StateValue getStateValue(State s) {
        for (StateValue sv : values) {
            if (sv.state.equals(s)) return sv;
        }
        return null;
    }

    // update the value of a state
    private void updateValue(State state, double newValue) {
        for (StateValue sv : values) {
            if (sv.state.equals(state)) {
                sv.value = newValue;
                return;
            }
        }
        // if the state does not exist, add it
        values.add(new StateValue(state, newValue));
    }

    // allows updating the current state in the agent
    public void setCurrentState(State state) {
        this.currentState = state;
    }
}