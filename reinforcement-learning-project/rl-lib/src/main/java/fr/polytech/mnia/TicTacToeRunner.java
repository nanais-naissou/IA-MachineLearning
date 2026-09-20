package fr.polytech.mnia;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner; // for the interactive game

import de.prob.statespace.State;
import de.prob.statespace.Transition;
import fr.polytech.mnia.agents.Agent;
import fr.polytech.mnia.agents.PolicyIterationAgent;
import fr.polytech.mnia.agents.QlearningAgent;
import fr.polytech.mnia.agents.ValueIterationAgent;
import fr.polytech.mnia.graphs.Visualization;

public class TicTacToeRunner extends Runner {

    // implementation choice: Player 1 learns, Player 0 plays randomly.

    public TicTacToeRunner() {
        super("/TicTacToe/tictac.mch");
        this.initialiseAleaInitialState();
    }

   // number of possible states = 19683 (3^9) -> a minimum exploration phase is required for Policy and Value Iteration

   // parameter selection
    private static final int NB_ACTIONS = 9;
    private static final double GAMMA = 0.9;
    private static final double EPSILON = 0.1;
    private static final double ALPHA= 0.1;

    // episode count selection
    private static final int NB_EPISODES = 100; // 100 selected by default because execution is very long (especially Policy Iteration during exploitation)
                                                // we cannot use fewer episodes because the evolution of the policy would not be visible enough

    private Scanner scanner;
    private List<TransitionDataTTT> transitionList = new LinkedList<>();
    private List<RewardDataTTT> rewardList = new LinkedList<>();

    private void addTransition(State state, int action, State nextState) {
            transitionList.add(new TransitionDataTTT(state, action, nextState));
    }

    private void addReward(State state, int action, double reward) {
        if (reward != 0) {
            rewardList.add(new RewardDataTTT(state, action, reward));
        }
    }

    public double getReward(State state, int action) {
        for (RewardDataTTT r : rewardList) {
            if (r.state.equals(state) && r.action == action) {
                return r.reward;
            }
        }
        return 0.0;
    }

    private double computeReward(State currentState, int action, State nextState) {
        String win1 = nextState.eval("win(1)").toString();
        String win0 = nextState.eval("win(0)").toString();
        if (win1.equals("TRUE")) return 1.0;
        if (win0.equals("TRUE")) return -1.0;
        return 0.0;
    }

    public void compareAgents() throws Exception {
        int maxEpisodes = NB_EPISODES;

        Agent policyIteration = new PolicyIterationAgent(NB_ACTIONS, transitionList, rewardList, GAMMA, EPSILON, this);
        Agent valueIteration = new ValueIterationAgent(NB_ACTIONS, transitionList, rewardList, GAMMA, EPSILON, this);
        Agent qLearning = new QlearningAgent(ALPHA, GAMMA, EPSILON, this);

        Agent[] agents = {policyIteration, valueIteration, qLearning};
        List<String> agentNames = List.of("Policy Iteration", "Value Iteration", "Q-Learning");

        List<List<Double>> rewardsByAgent = new ArrayList<>();
        for (int i = 0; i < agents.length; i++) {
            rewardsByAgent.add(new ArrayList<>());
        }

        for (int agentIndex = 0; agentIndex < agents.length; agentIndex++) {
            Agent agent = agents[agentIndex];
            System.out.println("\n=== Test de l'agent: " + agentNames.get(agentIndex) + " ===");
            Visualization plotter = new Visualization();

            transitionList.clear(); // move to another agent
            rewardList.clear(); // move to another agent

            for (int episode = 0; episode < maxEpisodes; episode++) {
                initialiseAleaInitialState();
                agent.setCurrentState(getState()); // initialize the current state in the agent

                boolean done = false;
                double totalReward = 0.0;

                while (!done) {
                    State currentState = getState();
                    agent.setCurrentState(currentState); // update the current state in the agent

                    List<Transition> transitions = currentState.getOutTransitions();
                    if (transitions.isEmpty()) break;

                    int actionIndex = agent.chooseAction();
                    if (actionIndex < 0 || actionIndex >= transitions.size()) {
                        actionIndex = Math.max(0, transitions.size() - 1);
                    }

                    Transition chosenTransition = transitions.get(actionIndex);
                    State nextState = chosenTransition.getDestination();

                    double reward = computeReward(currentState, actionIndex, nextState);
                    addReward(currentState, actionIndex, reward);

                    try {
                        this.applyTransition(chosenTransition);
                    } catch (IllegalArgumentException e) {
                        System.err.println("⚠ Erreur lors de l’application de la transition : " + e.getMessage());
                        break;
                    }

                    if (nextState.equals(currentState)) {
                        System.err.println("⚠ Problème : la transition ne change pas d'état !");
                    }

                    addTransition(currentState, actionIndex, nextState);

                    // victory check
                    String win1 = getState().eval("win(1)").toString();
                    String win0 = getState().eval("win(0)").toString();

                    if (win1.equals("TRUE")) {
                        totalReward += 1.0;
                        addReward(getState(), actionIndex, totalReward);
                        done = true;
                    } else if (win0.equals("TRUE")) {
                        totalReward -= 1.0;
                        addReward(getState(), actionIndex, totalReward);
                        done = true;
                    } else if (getState().getOutTransitions().isEmpty()) {
                        totalReward += 0.0;
                        addReward(getState(), actionIndex, totalReward);
                        done = true;
                    }

                    // Player 0 turn
                    List<Transition> envActions = getState().getOutTransitions();
                    List<Transition> validMoves = new ArrayList<>();
                    for (Transition t : envActions) {
                        if (t.getName().contains("play") && t.getParameterPredicate().toString().contains("joueur = 0")) {
                            validMoves.add(t);
                        }
                    }
                    if (!validMoves.isEmpty()) {
                        Transition randomMove = validMoves.get(new Random().nextInt(validMoves.size()));
                        this.applyTransition(randomMove);
                    }
                    // end of Player 0 turn

                    agent.update(actionIndex, reward);
                }

                rewardsByAgent.get(agentIndex).add(totalReward);
                plotter.addEpisodeReward(episode + 1, totalReward);
                
                // display the episode number every 10 episodes and the reward for that episode
                if (episode % 10 == 0) {
                    System.out.printf("Episode %d | Récompense cumulée: %.2f\n", episode, totalReward);
                }
            }

            // create another plotter
            plotter.showChartTTT(); 
        }

        // agent comparison graph
        Visualization comparisonPlotter = new Visualization();
        comparisonPlotter.compareAgents(agentNames, rewardsByAgent, maxEpisodes);

// =========================== Interactive Part =============================//

        // select the agent with the highest average reward
        int bestAgentIndex = 0;
        double bestAvgReward = -Double.MAX_VALUE;

        for (int i = 0; i < agents.length; i++) {
            double avg = rewardsByAgent.get(i).stream().mapToDouble(d -> d).average().orElse(0.0);
            if (avg > bestAvgReward) {
                bestAvgReward = avg;
                bestAgentIndex = i;
            }
        }
        System.out.println("\n>>> Meilleur agent : " + agentNames.get(bestAgentIndex) + " avec moyenne " + bestAvgReward);
        this.scanner = new Scanner(System.in);
        playAgainstAgent(agents[bestAgentIndex]);
    // ===============================================================================//
    }
    // function to set the current state in the agents
    public State getCurrentState() {
        return getState();
    }

    // function providing next-state information to the agents (especially for reward calculation)
    public State getNextState(State currentState, int action) {
        for (TransitionDataTTT t : transitionList) {
            if (t.state.equals(currentState) && t.action == action) {
                return t.nextState;
            }
        }
        return null;
    }

    @Override
    public void execSequence() throws Exception {
        compareAgents();
    }

    // ============================= Interactive Part ===============================//
public void playAgainstAgent(Agent agent) {
    
    initialiseAleaInitialState();

    agent.setCurrentState(getState());

    System.out.println("\n=== Partie interactive contre l’agent entraîné ===");

    prettyPrintTicTacToeVoid();

    boolean done = false;
    while (!done) {
        String currentPlayer = getState().eval("turn").toString();

        if (getState().getOutTransitions().isEmpty()) {
            System.out.println("Match nul !");
            break;
        }

        if (currentPlayer.equals("0")) {
            // player turn
            List<Transition> possibleMoves = getState().getOutTransitions();
            List<Transition> validMoves = new ArrayList<>();
            for (Transition t : possibleMoves) {
                if (t.getName().contains("place0")) { 
                    validMoves.add(t);
                }
            }

            if (validMoves.isEmpty()) {
                System.out.println("Aucun coup possible pour le joueur !");
                break;
            }

            System.out.println("C'est votre tour. Entrez les coordonnées x y (1 à 3) : ");

    while (true) {
        String[] parts = scanner.nextLine().trim().split("\\s+");
        if (parts.length != 2 || !parts[0].matches("[1-3]") || !parts[1].matches("[1-3]")) {
            System.out.println("Entrée invalide. Veuillez entrer deux entiers entre 1 et 3 séparés par un espace.");
            continue;
        }

        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);

        Transition userAction = validMoves.stream()
            .filter(t -> {
                List<String> params = t.getParams(); // no cleaner way to do this was found
                return params.size() == 2 &&
                    params.get(0).equals(String.valueOf(x)) &&
                    params.get(1).equals(String.valueOf(y));
            })
            .findFirst()
            .orElse(null);

        if (userAction == null) {
            System.out.println("Action invalide (case occupée ?), réessayez.");
            continue;
        }

        applyTransition(userAction);
        prettyPrintTicTacToe();
        break;
    }
            

        } else {
            // agent turn (Player 1)
            System.out.println("\nCoup de l'Agent");
            State currentState = getState();
            agent.setCurrentState(currentState);
            List<Transition> transitions = currentState.getOutTransitions();
            if (transitions.isEmpty()) {
                System.out.println("L’agent ne peut pas jouer !");
                break;
            }

            int actionIndex = agent.chooseAction();
            if (actionIndex < 0 || actionIndex >= transitions.size()) {
                actionIndex = Math.max(0, transitions.size() - 1);
            }

            Transition chosenTransition = transitions.get(actionIndex);
            applyTransition(chosenTransition);
            prettyPrintTicTacToe();
        }

        // game-over check
        String win1 = getState().eval("win(1)").toString();
        String win0 = getState().eval("win(0)").toString();

        if (win1.equals("TRUE")) {
            System.out.println("L'agent a gagné !");
            done = true;
        } else if (win0.equals("TRUE")) {
            System.out.println("Vous avez gagné !");
            done = true;
        } else if (getState().getOutTransitions().isEmpty()) {
            System.out.println("Match nul !");
            done = true;
        }
    }

    // replay option
    System.out.print("Voulez-vous rejouer ? (o/n) ");
    String replay = this.scanner.nextLine();
    if (replay.equalsIgnoreCase("o")) {
        playAgainstAgent(agent);
    } else {
        System.out.println("Fin de la partie.");
    }

    // close the scanner
}


// ============================Fin Partie Interactive=============================//



    // display the board state using the machine
    private void prettyPrintTicTacToe() {
        String input = state.eval("square").toString();
        String[][] board = {{" ", " ", " "}, {" ", " ", " "}, {" ", " ", " "}};
        input = input.replaceAll("[^0-9↦,]", "");
        String[] entries = input.split(",");

        for (String entry : entries) {
            String[] parts = entry.split("↦");
            int row = Integer.parseInt(parts[0]) - 1;
            int col = Integer.parseInt(parts[1]) - 1;
            String value = parts[2];
            board[row][col] = value;
        }

        for (int i = 0; i < 3; i++) {
            System.out.println(" " + board[i][0] + " | " + board[i][1] + " | " + board[i][2]);
            if (i < 2) System.out.println("---+---+---");
        }
    }
    // first board showing the possible moves
    void prettyPrintTicTacToeVoid(){
    System.out.println("1 1|1 2|1 3");
    System.out.println("---+---+---");
    System.out.println("2 1|2 2|2 3");
    System.out.println("---+---+---");
    System.out.println("3 1|3 2|3 3");
    }
    
    
}


