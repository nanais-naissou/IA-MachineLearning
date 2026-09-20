package fr.polytech.mnia;

import java.util.Scanner;


// close the plots or press Ctrl+C in the terminal to stop execution

public class App {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Choisissez un scénario à exécuter ===");
        System.out.println("1. SimpleRunner");
        System.out.println("2. YoutubeRunner");
        System.out.println("3. TicTacToeRunner");
        System.out.print("Votre choix (1-3) : ");

        int choix = scanner.nextInt();

        switch (choix) {
            case 1: // tested with 500 episodes per agent by default -> see SimpleRunner line 20 to change it
                    // Algorithms: ε-Greedy (ε=0.1), UCB (c=2.0), Bandit Gradient (a=0.1)
                                 // see SimpleRunner lines 23-25 to change the parameters
                SimpleRunner sr = new SimpleRunner();
                sr.execSequence();
                break;

            case 2: // tested with 5000 episodes per agent by default (long) -> see YoutubeRunner line 35 to change it
                    // Algorithms: ε-Greedy (ε=0.1), UCB (c=2.0), Bandit Gradient (a=0.1)
                                  // see YoutubeRunner lines 40-42 to change the parameters
                YoutubeRunner yr = new YoutubeRunner();
                yr.execSequence();
                break;
            
            case 3: // tested with 200 episodes by default (long) -> see TicTacToeRunner line 35 to change it
                    // parameters: see TicTacToeRunner lines 30-32 to change them
                                  
                TicTacToeRunner tr = new TicTacToeRunner();
                tr.execSequence();
                break;

            default:
                System.out.println("Choix invalide. Veuillez relancer le programme.");
                break;
        }

        scanner.close();
    }
}

