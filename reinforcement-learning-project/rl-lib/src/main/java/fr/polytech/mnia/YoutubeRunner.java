package fr.polytech.mnia;

import de.prob.statespace.Transition;
import fr.polytech.mnia.agents.*;
import fr.polytech.mnia.graphs.Visualization;

import java.util.*;

public class YoutubeRunner extends Runner {

    public YoutubeRunner() throws Exception {
        super("/Simple/YouTube.mch");
        this.initialise();
    }

    /**
      Convertit un string de durée en reward utilisable (entre 0 et 1).
      ca fait "100" en 1.0, "25" en 0.25. Si on peut pas parser, on renvoi -1.
     */
    public static double parseDuration(String durationStr) {
        String cleaned = durationStr.replaceAll("[^0-9.]", ""); // on vire les trucs chelous

        if (cleaned.isEmpty()) return -1; // invalid

        double duration = Double.parseDouble(cleaned);
        return duration / 100.0; // simplification: divide by 100 to normalize everything
    }

    @Override
    public void execSequence() throws Exception {

        // the "arms" of our bandit here
        String[] videos = {"Tutoriel_Python", "Vlog_de_voyage", "Musique_populaire", "Gaming"};

        int maxSteps = 5000; // 5000 per agent for more consistent results (training is somewhat long)
                             // test with 500 for faster execution but lower precision

        // the 3 agents to test
        Agent[] agents = {
            new EpsilonGreedyAgent(4, 0.1),
            new UCBAgent(4, 2.0),
            new BanditGradientAgent(4, 0.1)
        };
        String[] agentNames = {
            "ε-Greedy (ε=0.1)",
            "UCB (c=2.0)",
            "Bandit Gradient (α=0.1)"
        };

        List<List<Double>> rewardsByAgent = new ArrayList<>(); // store the rewards
        List<int[]> actionsCountByAgent = new ArrayList<>();   // number of times each action was selected

        // initialize the reward lists
        for (int i = 0; i < agents.length; i++) {
            rewardsByAgent.add(new ArrayList<>());}

        for (int i = 0; i < agents.length; i++) {
            Agent agent = agents[i];
            initialise(); // reset the system for each new agent
            Visualization plotter = new Visualization(); 

            int[] actionCounts = new int[videos.length]; // counter for each video selection
            actionsCountByAgent.add(actionCounts);       // keep this for the final display

            System.out.println("\n=== Agent: " + agentNames[i] + " ===");

            for (int step = 0; step < maxSteps; step++) {
                int actionIndex = agent.chooseAction(); // the agent decides what to watch
                actionCounts[actionIndex]++; // record the choice
                String video = videos[actionIndex];

                // find the transition in the B model corresponding to this action
                Transition chosen = null;
                for (Transition t : getState().getOutTransitions()) {
                    if (t.getName().equals("choose") && t.getParameterPredicate().toString().contains(video)) {
                        chosen = t;
                        break;
                    }
                }

                applyTransition(chosen); // simulate clicking the video

                String rawDuration = getState().eval("duration").toString(); // get how long the user watched
                double reward = parseDuration(rawDuration); // convert this into a score

                if (reward == -1) {
                    System.err.println("Erreur de parsing sur : " + rawDuration);
                    continue; // skip if an error occurs
                    }

                agent.update(actionIndex, reward); // the agent learns from the received reward
                rewardsByAgent.get(i).add(reward); // keep this for the graph
                plotter.addEpisodeReward(step + 1, reward); // add the point to the graph

                // for the gradient agent, also plot the probability distribution
                if (agent instanceof BanditGradientAgent) {
                    plotter.addPolicyDistribution(((BanditGradientAgent) agent).getPolicy());
                }

                // log every 30 steps to monitor the evolution
                if (step % 30 == 0) {
                    System.out.printf("Étape %d | Vidéo : %s | Récompense : %.2f\n", step, video, reward);}
            }

            // final summary of the results for this agent
            System.out.println();
            System.out.println("--- Résultats finaux pour : " + agentNames[i] + " ---");

            // nice table :))
            System.out.printf("%-20s | %-10s | %-10s | %-10s\n", "Vidéo", "Valeur", "Sélections", "Pourcentage");
            System.out.println("---------------------------------------------------------------");

            double[] values;
            if (agent instanceof EpsilonGreedyAgent) {
                values = ((EpsilonGreedyAgent) agent).getQValues();
            } else if (agent instanceof UCBAgent) {
                values = ((UCBAgent) agent).getQValues();
            } else {
                values = ((BanditGradientAgent) agent).getPolicy(); // these are probabilities here
            }

            for (int j = 0; j < videos.length; j++) {
                double pourcentage = 100.0 * actionCounts[j] / maxSteps;
                System.out.printf("%-20s | %-10.4f | %-10d | %-10.2f%%\n",
                        videos[j], values[j], actionCounts[j], pourcentage);
            }

            // show the final graph if this is a gradient agent
            if (agent instanceof BanditGradientAgent) {
                plotter.showChart();
            }
        }

        // at the very end, a comparison graph between all agents
        Visualization comparisonPlot = new Visualization();
        comparisonPlot.compareAgents(Arrays.asList(agentNames), rewardsByAgent, maxSteps);
        comparisonPlot.plotSelectionPercentages(Arrays.asList(agentNames), actionsCountByAgent, maxSteps);
    }
}