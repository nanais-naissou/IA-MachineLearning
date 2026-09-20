package fr.polytech.mnia.agents;

import java.util.LinkedList;
import java.util.List;
import de.prob.statespace.State;
import fr.polytech.mnia.RewardDataTTT;
import fr.polytech.mnia.TicTacToeRunner;
import fr.polytech.mnia.TransitionDataTTT;

public class PolicyIterationAgent implements Agent {
    private int nbActions;
    private List<TransitionDataTTT> transitionList;
    private List<RewardDataTTT> rewardList;
    private List<StateValue> values;
    private List<StatePolicy> policy;
    private double gamma;
    private double epsilon;
    private State currentState;
    private TicTacToeRunner runner;

    private int MINIMUM_EXP = 2000;  // minimum number of visited states before learning (exploration phase)

    // inner class used to store state values
    private static class StateValue {
        State state;
        double value;

        public StateValue(State state, double value) {
            this.state = state;
            this.value = value;
        }
    }

    // inner class used to store the policy
    private static class StatePolicy {
        State state;
        int action;

        public StatePolicy(State state, int action) {
            this.state = state;
            this.action = action;
        }
    }

    public PolicyIterationAgent(int nbActions, List<TransitionDataTTT> transitionList, 
                                List<RewardDataTTT> rewardList, double gamma, double epsilon, 
                                TicTacToeRunner runner) {
        this.nbActions = nbActions;
        this.transitionList = transitionList;
        this.rewardList = rewardList;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.runner = runner;
        this.values = new LinkedList<>();
        this.policy = new LinkedList<>();
        this.currentState = transitionList.isEmpty() ? null : transitionList.get(0).state;

        // initialize the policy and state values (default to 0)
        for (TransitionDataTTT t : transitionList) {
            if (!containsState(policy, t.state)) {
                policy.add(new StatePolicy(t.state, 0)); // initial action = 0
            }
            if (!containsState(values, t.state)) {
                values.add(new StateValue(t.state, 0.0)); // initial value = 0
            }
        }

        // assign random initial values to improve exploration at the beginning
        for (StateValue sv : values) {
            sv.value = Math.random() * 2 - 1; // values between -1 and 1
        }
    }

    // run the complete Policy Iteration algorithm (evaluation + improvement)
    public void policyIteration() {
        boolean policyStable;
        do {
            policyEvaluation();      // update state values
            policyStable = policyImprovement();  // change the policy if a better one is found
        } while (!policyStable);    // repeat until the policy is stable
    }

    // policy evaluation step: update state values
    private void policyEvaluation() {
        boolean converged;
        do {
            converged = true;
            List<StateValue> newValues = new LinkedList<>();

            for (StatePolicy sp : policy) {
                State s = sp.state;
                int action = sp.action;
                double q = 0;

                // compute the state-action value according to the policy
                for (TransitionDataTTT t : transitionList) {
                    if (t.state.equals(s) && t.action == action) {
                        q += (getReward(s, action) + gamma * getValue(t.nextState));
                    }
                }

                // store this new value for the state
                boolean found = false;
                for (StateValue sv : newValues) {
                    if (sv.state.equals(s)) {
                        sv.value = q;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    newValues.add(new StateValue(s, q));
                }

                // check convergence for this state
                if (Math.abs(q - getValue(s)) > epsilon) {
                    converged = false;
                }
            }
            values.clear();
            values.addAll(newValues);
        } while (!converged);
    }

    // policy improvement step: change the action if a better one exists
    private boolean policyImprovement() {
        boolean policyStable = true;

        for (StatePolicy sp : policy) {
            State s = sp.state;
            int oldAction = sp.action;
            int bestAction = -1;
            double maxQ = Double.NEGATIVE_INFINITY;

            // test all actions to find the best one
            for (int a = 0; a < nbActions; a++) {
                double q = 0;
                State nextState = runner.getNextState(s, a);
                if (nextState != null) {
                    double reward = getReward(s, a);
                    q += reward + gamma * getValue(nextState);
                }

                // if this action is better, keep it
                if (q > maxQ) {
                    maxQ = q;
                    bestAction = a;
                }
            }

            sp.action = bestAction;
            if (oldAction != bestAction) {
                policyStable = false;
            }
        }

        return policyStable;
    }

    @Override
    public int chooseAction() {
        // exploration: choose randomly until enough data is available
        if (transitionList.size() < MINIMUM_EXP) {
            return (int) (Math.random() * nbActions);
        }

        // exploitation: follow the learned policy
        for (StatePolicy sp : policy) {
            if (sp.state.equals(currentState)) {
                return sp.action;
            }
        }

        // if no known choice exists, play randomly
        return (int) (Math.random() * nbActions);
    }

    @Override
    public void update(int action, double reward) {
        if (currentState == null) {
            System.err.println("Erreur : 'currentState' est NULL avant update()");
            return;
        }

        State nextState = runner.getNextState(currentState, action);
        if (nextState == null) return;

        // add the new transition
        transitionList.add(new TransitionDataTTT(currentState, action, nextState));
        rewardList.add(new RewardDataTTT(nextState, action, reward));

        // add states if they are new
        if (!containsState(policy, nextState)) policy.add(new StatePolicy(nextState, 0));
        if (!containsState(values, nextState)) values.add(new StateValue(nextState, 0.0));

        currentState = nextState;

        // if enough experience has been collected, learn
        if (transitionList.size() >= MINIMUM_EXP) {
            policyIteration(); 
        }
    }

    // retrieve the reward associated with a transition (state + action)
    private double getReward(State state, int action) {
        State nextState = runner.getNextState(state, action);
        if (nextState == null) return 0.0;

        for (RewardDataTTT r : rewardList) {
            if (r.state.equals(nextState) && r.action == action) {
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

    // define the current game state in the agent through the runner
    public void setCurrentState(State state) {
        this.currentState = state;

        if (!containsState(policy, state)) {
            policy.add(new StatePolicy(state, 0));
        }
        if (!containsState(values, state)) {
            values.add(new StateValue(state, 0.0));
        }
    }

    // check whether a state is present in a given list
    private boolean containsState(List<? extends Object> list, State state) {
        for (Object obj : list) {
            if (obj instanceof StatePolicy && ((StatePolicy) obj).state.equals(state)) {
                return true;
            } else if (obj instanceof StateValue && ((StateValue) obj).state.equals(state)) {
                return true;
            }
        }
        return false;
    }
}