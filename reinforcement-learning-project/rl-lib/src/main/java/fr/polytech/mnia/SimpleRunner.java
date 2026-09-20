package fr.polytech.mnia;

import de.prob.statespace.Transition;
import java.util.List;
import java.util.ArrayList;

import fr.polytech.mnia.agents.*;
import fr.polytech.mnia.graphs.Visualization;

public class SimpleRunner extends Runner {
    
    // load the corresponding B machine and initialize it
    public SimpleRunner() throws Exception {
        super("/Simple/SimpleRL.mch");
        this.initialise(); // reinitialize here, otherwise it does not work correctly
    }
    
    public void compareAgents() throws Exception {
        String[] films = {"A", "B", "C"}; // 3 arms → 3 movies
        int maxSteps = 500; // 500 episodes per agent

        // create the agents, each with its own parameters
        Agent epsilonGreedy = new EpsilonGreedyAgent(3, 0.1);
        Agent ucb = new UCBAgent(3, 2.0);
        Agent banditGradient = new BanditGradientAgent(3, 0.1);

        // names used for display later
        List<String> agentNames = new ArrayList<>();
        agentNames.add("ε-Greedy (ε=0.1)");
        agentNames.add("UCB (c=2.0)");
        agentNames.add("Bandit Gradient (α=0.1)");

        // store rewards for the comparison graph
        List<List<Double>> rewardsByAgent = new ArrayList<>();
        rewardsByAgent.add(new ArrayList<>()); 
        rewardsByAgent.add(new ArrayList<>()); 
        rewardsByAgent.add(new ArrayList<>()); 

        Agent[] agents = {epsilonGreedy, ucb, banditGradient};

        for (int agentIndex = 0; agentIndex < agents.length; agentIndex++) {
            Agent agent = agents[agentIndex];
            initialise(); // reset the state machine (otherwise everything breaks after the first agent)

            System.out.println("\n=== Test de l'agent: " + agentNames.get(agentIndex) + " ===");
            Visualization plotter = new Visualization(); // used to plot the curves

            for (int step = 0; step < maxSteps; step++) {
                int actionIndex = agent.chooseAction(); // ask the agent what to do
                String film = films[actionIndex]; // map the index to A/B/C

                // find the B-machine transition corresponding to the selected movie
                List<Transition> transitions = getState().getOutTransitions();
                Transition chosenTransition = null;
                for (Transition t : transitions) {
                    String param = t.getParameterPredicate().toString();
                    if (param.contains("film = " + film)) {
                        chosenTransition = t;
                        break;
                    }
                }

                if (chosenTransition == null) {
                    System.out.println("Aucune transition trouvée pour le film " + film);
                    continue; // skip this step if nothing is found (this should normally not happen)
                }

                this.applyTransition(chosenTransition); // apply the action

                // reward = 1 if "OK", 0 otherwise
                String res = getState().eval("res").toString();
                double reward = res.equals("OK") ? 1.0 : 0.0;

                agent.update(actionIndex, reward); // update the agent
                rewardsByAgent.get(agentIndex).add(reward); // store the reward

                plotter.addEpisodeReward(step + 1, reward); // plot it

                // for the gradient agent, also display the probability distribution
                if (agent instanceof BanditGradientAgent) {
                    plotter.addPolicyDistribution(((BanditGradientAgent) agent).getPolicy());
                }

                // print a small log every 50 steps
                if (step % 50 == 0) {
                    System.out.printf("Étape %d | Film choisi: %s | Récompense: %.2f\n", step, film, reward);
                }
            }

            // display the final results for this agent
            System.out.println("\n--- Valeurs finales pour " + agentNames.get(agentIndex) + " ---");
            if (agent instanceof EpsilonGreedyAgent) {
                double[] qValues = ((EpsilonGreedyAgent) agent).getQValues();
                for (int i = 0; i < films.length; i++) {
                    System.out.printf("Film %s : %.2f\n", films[i], qValues[i]);
                }
            } else if (agent instanceof UCBAgent) {
                double[] qValues = ((UCBAgent) agent).getQValues();
                for (int i = 0; i < films.length; i++) {
                    System.out.printf("Film %s : %.2f\n", films[i], qValues[i]);
                }
            } else if (agent instanceof BanditGradientAgent) {
                double[] policy = ((BanditGradientAgent) agent).getPolicy();
                for (int i = 0; i < films.length; i++) {
                    System.out.printf("Film %s : %.2f\n", films[i], policy[i]);
                }
            }

            // for the gradient agent, display the complete distribution
            if (agent instanceof BanditGradientAgent) {
                plotter.showChart();
            }
        }

        // once all agents have run → comparison
        Visualization comparisonPlotter = new Visualization();
        comparisonPlotter.compareAgents(agentNames, rewardsByAgent, maxSteps);
    }

    @Override
    public void execSequence() throws Exception {
        compareAgents(); // this is what launches everything
    }
}