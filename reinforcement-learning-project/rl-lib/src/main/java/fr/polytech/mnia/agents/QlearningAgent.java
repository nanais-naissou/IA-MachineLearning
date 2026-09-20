package fr.polytech.mnia.agents;

import de.prob.statespace.State;
import de.prob.statespace.Transition;
import fr.polytech.mnia.TicTacToeRunner;
import java.util.*;

public class QlearningAgent implements Agent {
    // Q-value table: for each state, a map associates each action with a Q-value
    private final Map<State, Map<Integer, Double>> qTable;
    private final double alpha;    // learning rate
    private final double gamma;    // discount factor
    private final double epsilon;  // exploration rate
    private final Random random;
    private final TicTacToeRunner runner;
    private State currentState;

    public QlearningAgent(double alpha, double gamma, double epsilon, TicTacToeRunner runner) {
        this.qTable = new HashMap<>();
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.random = new Random();
        this.runner = runner;
    }

    // choose an action from the current state using the epsilon-greedy strategy
    @Override
    public int chooseAction() {
        List<Transition> transitions = runner.getCurrentState().getOutTransitions();
        if (transitions.isEmpty()) return -1; // no possible move

        // with probability epsilon, choose a random action (exploration)
        if (random.nextDouble() < epsilon) {
            return random.nextInt(transitions.size());
        } else {
            // otherwise, choose the best known action (exploitation)
            return getBestAction(transitions);
        }
    }

    // return the action with the best Q-value among the available actions
    private int getBestAction(List<Transition> transitions) {
        int bestAction = 0;
        double bestValue = getQValue(currentState, 0);

        for (int i = 1; i < transitions.size(); i++) {
            double q = getQValue(currentState, i);
            if (q > bestValue) {
                bestValue = q;
                bestAction = i;
            }
        }
        return bestAction;
    }

    // update the Q-table using the formula studied in class
   @Override
    public void update(int action, double reward) {
        State nextState = runner.getCurrentState(); // state after the action
        double oldQ = getQValue(currentState, action); 
        double nextMaxQ = maxQ(nextState); // best Q-value from the new state
        double newQ = oldQ + alpha * (reward + gamma * nextMaxQ - oldQ); // Q-learning formula
        setQValue(currentState, action, newQ); // update the Q-table
    }

    // return the Q-value for a state-action pair, or 0 if never seen
    private double getQValue(State state, int action) {
        return qTable.getOrDefault(state, new HashMap<>()).getOrDefault(action, 0.0);
    }

    // change the Q-value for a state and action
    private void setQValue(State state, int action, double value) {
        qTable.computeIfAbsent(state, k -> new HashMap<>()).put(action, value);
    }

    // return the highest possible Q-value from a state
    private double maxQ(State state) {
        Map<Integer, Double> map = qTable.getOrDefault(state, new HashMap<>());
        return map.values().stream().mapToDouble(v -> v).max().orElse(0.0);
    }

    // compute the reward from the state after an action
    public double getReward(State state, int action) {
        State nextState = getNextState(state, action);
        if (nextState == null) return 0.0;
        String win1 = nextState.eval("win(1)").toString();
        String win0 = nextState.eval("win(0)").toString();
        if (win1.equals("TRUE")) return 1.0;     
        if (win0.equals("TRUE")) return -1.0;    
        return 0.0;                               
    }

    // update the agent's current state
    @Override
    public void setCurrentState(State state) {
        this.currentState = state;
    }

    // return the new state after an action from the current state
    public State getNextState(State currentState, int action) {
        List<Transition> transitions = currentState.getOutTransitions();
        if (action < 0 || action >= transitions.size()) return null;
        return transitions.get(action).getDestination().explore();
    }

}
