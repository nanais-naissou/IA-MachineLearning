package fr.polytech.mnia.graphs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;


public class Visualization {
    private List<Double> rewards;
    private List<double[]> policies;
    private int nbActions;
    
    public Visualization() {
        this.rewards = new ArrayList<>();
        this.policies = new ArrayList<>();
        this.nbActions = 0;
    }
    
    public void addEpisodeReward(int episode, double reward) {
        // make sure the index corresponds to the episode
        while (rewards.size() < episode) {
            rewards.add(0.0);
        }
        rewards.set(episode - 1, reward);
    }
    
    public void addPolicyDistribution(double[] policy) {
        if (nbActions == 0) {
            nbActions = policy.length;
        }
        policies.add(policy.clone());
    }
    
    public void showChart() {
        JFrame frame = new JFrame("Résultats d'apprentissage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(2, 1));
        
        // policy graph
        XYSeriesCollection policyDataset = new XYSeriesCollection();
        for (int i = 0; i < nbActions; i++) {
            XYSeries series = new XYSeries("Action " + (i + 1));
            for (int t = 0; t < policies.size(); t++) {
                series.add(t, policies.get(t)[i]);
            }
            policyDataset.addSeries(series);
        }
        
        JFreeChart policyChart = ChartFactory.createXYLineChart(
                "Évolution des probabilités des actions (Soft-Max)",
                "Itérations",
                "Probabilité",
                policyDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        XYPlot policyPlot = policyChart.getXYPlot();
        XYLineAndShapeRenderer policyRenderer = new XYLineAndShapeRenderer();
        policyRenderer.setSeriesPaint(0, Color.BLUE);    // Action 1
        policyRenderer.setSeriesPaint(1, Color.ORANGE);  // Action 2
        policyRenderer.setSeriesPaint(2, Color.GREEN);   // Action 3
        policyPlot.setRenderer(policyRenderer);
        
        ChartPanel policyPanel = new ChartPanel(policyChart);
        frame.add(policyPanel);
        
        // cumulative average reward graph
        XYSeries rewardSeries = new XYSeries("Récompense moyenne cumulée");
        double cumulativeSum = 0;
        for (int i = 0; i < rewards.size(); i++) {
            cumulativeSum += rewards.get(i);
            double average = cumulativeSum / (i + 1);
            rewardSeries.add(i, average);
        }
        
        XYSeriesCollection rewardDataset = new XYSeriesCollection();
        rewardDataset.addSeries(rewardSeries);
        
        // add the optimal reward line
        XYSeries optimalRewardSeries = new XYSeries("Récompense optimale (1.5)");
        optimalRewardSeries.add(0, 1.0);
        optimalRewardSeries.add(rewards.size() - 1, 1.0);
        rewardDataset.addSeries(optimalRewardSeries);
        
        JFreeChart rewardChart = ChartFactory.createXYLineChart(
                "Récompense moyenne cumulée au fil des itérations",
                "Itérations",
                "Récompense moyenne",
                rewardDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        XYPlot rewardPlot = rewardChart.getXYPlot();
        XYLineAndShapeRenderer rewardRenderer = new XYLineAndShapeRenderer();
        rewardRenderer.setSeriesPaint(0, Color.BLUE);
        rewardRenderer.setSeriesPaint(1, Color.RED);
        rewardRenderer.setSeriesStroke(1, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {6.0f, 6.0f}, 0.0f));
        rewardPlot.setRenderer(rewardRenderer);
        
        ChartPanel rewardPanel = new ChartPanel(rewardChart);
        frame.add(rewardPanel);
        
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    // Tic-Tac-Toe learning evolution graph

    public void showChartTTT(){

    JFrame frame = new JFrame("Résultats d'apprentissage");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new GridLayout(1, 1));

    // cumulative average reward graph
    XYSeries rewardSeries = new XYSeries("Récompense moyenne cumulée");
    double cumulativeSum = 0;
    for (int i = 0; i < rewards.size(); i++) {
        cumulativeSum += rewards.get(i);
        double average = cumulativeSum / (i + 1);
        rewardSeries.add(i, average);
    }
    
    XYSeriesCollection rewardDataset = new XYSeriesCollection();
    rewardDataset.addSeries(rewardSeries);
    
    // add the optimal reward line
    XYSeries optimalRewardSeries = new XYSeries("Récompense optimale (1)");
    optimalRewardSeries.add(0, 1.0);
    optimalRewardSeries.add(rewards.size() - 1, 1.0);
    rewardDataset.addSeries(optimalRewardSeries);
    
    JFreeChart rewardChart = ChartFactory.createXYLineChart(
            "Récompense moyenne cumulée au fil des itérations",
            "Itérations",
            "Récompense moyenne",
            rewardDataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
    );
    
    XYPlot rewardPlot = rewardChart.getXYPlot();
    XYLineAndShapeRenderer rewardRenderer = new XYLineAndShapeRenderer();
    rewardRenderer.setSeriesPaint(0, Color.BLUE);
    rewardRenderer.setSeriesPaint(1, Color.RED);
    rewardRenderer.setSeriesStroke(1, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {6.0f, 6.0f}, 0.0f));
    rewardPlot.setRenderer(rewardRenderer);
    
    ChartPanel rewardPanel = new ChartPanel(rewardChart);
    frame.add(rewardPanel);
    
    frame.setSize(800, 600);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
}
    
    // new method to display the video selection percentage graph
    public void plotSelectionPercentages(List<String> agentNames, List<int[]> actionsCountByAgent, int totalEpisodes) {
        String[] videoNames = {"Tutoriel_Python", "Vlog_de_voyage", "Musique_populaire", "Gaming"};
    
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    
        for (int i = 0; i < agentNames.size(); i++) {
            int[] counts = actionsCountByAgent.get(i);
            for (int j = 0; j < counts.length; j++) {
                double percentage = (double) counts[j] * 100 / totalEpisodes;
                dataset.addValue(percentage, agentNames.get(i), videoNames[j]);
            }
        }
    
        JFreeChart barChart = ChartFactory.createBarChart(
                "Pourcentage de sélection des vidéos par agent",
                "Vidéo",
                "Pourcentage (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    
        ChartPanel chartPanel = new ChartPanel(barChart);
        JFrame frame = new JFrame("Comparaison des sélections");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chartPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    
    public void compareAgents(List<String> agentNames, List<List<Double>> rewardsByAgent, int maxEpisodes) {
        JFrame frame = new JFrame("Comparaison des agents");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        for (int i = 0; i < agentNames.size(); i++) {
            XYSeries series = new XYSeries(agentNames.get(i));
            List<Double> agentRewards = rewardsByAgent.get(i);
            
            double cumulativeSum = 0;
            for (int j = 0; j < Math.min(agentRewards.size(), maxEpisodes); j++) {
                cumulativeSum += agentRewards.get(j);
                double average = cumulativeSum / (j + 1);
                series.add(j, average);
            }
            
            dataset.addSeries(series);
        }
        
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Comparaison des performances des agents",
                "Épisodes",
                "Récompense moyenne cumulée",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        ChartPanel panel = new ChartPanel(chart);
        frame.add(panel);
        
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}