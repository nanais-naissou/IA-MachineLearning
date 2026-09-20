Bienvenue sur notre projet de Reinforcement Learning !

Ce projet est le fruit de notre travail autour de l'apprentissage par renforcement, une approche fascinante pour enseigner à un agent à apprendre de ses expériences.

------------------------------------------------------------
 Pour lancer le projet :
------------------------------------------------------------
1. Se placer dans le dossier :
   /ProjetMN_BELAL_Anais_/project/rl-lib

2. Compiler le projet avec Maven :
   mvn clean compile

3. Lancer la classe App.java

4. Choisir le scénario souhaité :
   1 : Simple
   2 : YouTube
   3 : TicTacToe

------------------------------------------------------------
 Remarques importantes :
------------------------------------------------------------
- Lors de l'exécution des runners, des fenêtres de graphes s’ouvriront.
   Ne les fermez pas tant que l’exécution n’est pas terminée, sinon le processus s’arrêtera.

- Pour arrêter le processus après exécution :
   Appuyer sur Ctrl-C dans le terminal
   Ou fermer une fenêtre de graphe une fois le programme terminé

------------------------------------------------------------
Personnalisation :
------------------------------------------------------------
Le nombre d’épisodes et certains paramètres peuvent être modifiés directement
dans le fichier App.java (voir les commentaires dans le code).

------------------------------------------------------------
 Documentation :
------------------------------------------------------------
Un compte rendu est disponible dans le dossier reinforcement-learning-project/rl-lib/src/main/resources/docs. Il contient toutes les informations
utiles : explications techniques, fonctionnement des algorithmes, structure du code, etc.

------------------------------------------------------------
 Un mot de l'auteur: 
------------------------------------------------------------
Ce travail nous a permis de découvrir les rouages d’un domaine passionnant, 
et de mieux comprendre comment le monde fonctionne autour de nous.

Qui aurait cru qu’une simple formule mathématique pouvait prédire mes vidéos préférées du dimanche?

Bonne exploration ! 

#######################################################################################ENGLISH VERSION########################################################################################################################

# Reinforcement Learning Project

## 1. Introduction

This project was developed as part of the **Numerical Methods for Artificial Intelligence** course. Its main objective is to design, in Java, a library of **Reinforcement Learning (RL)** algorithms.

The goal is to implement different learning strategies that allow an agent to interact with formally described environments and learn to maximize a reward. The environments are represented using formal specifications interpreted with **ProB**, which dynamically generates states and transitions.

The library includes several classical RL algorithms: **ε-Greedy, UCB, Bandit Gradient, Value Iteration, Policy Iteration, and Q-learning**. They are tested on several environments, including movie recommendation, YouTube video recommendation, and Tic-Tac-Toe.

## 2. RL Library Architecture

### 2.1 Code Organization

The source code is located in `src/main/java/fr/polytech/mnia/`.

The code is divided into several packages and classes:

- `agents`: implementations of the different Reinforcement Learning agents.
- `graphs`: visualization tools for learning results.
- `App.java` and the different Runner classes: test and demonstration scenarios.
- `resources` and `docs`: models and additional project files.

This organization separates learning agents, visualization, and execution/testing responsibilities.

### 2.2 Class Structure

- **Agent.java**: interface defining the basic behavior of agents. Classes such as `QlearningAgent` and `EpsilonGreedyAgent` implement this behavior.
- **QlearningAgent.java**, **PolicyIterationAgent.java**, **ValueIterationAgent.java**: implementations of RL algorithms.
- **BanditGradientAgent.java**, **UCBAgent.java**, **EpsilonGreedyAgent.java**: implementations for multi-armed bandit problems.
- **Visualization.java**: displays learning results, including action probabilities and cumulative rewards, and allows agents to be compared graphically.
- **App.java**: main entry point of the application. It allows a scenario to be selected by entering its number.
- **Runner classes**: execute learning scenarios in different environments (`TicTacToeRunner`, `SimpleRunner`, and `YoutubeRunner`) and compare the corresponding agents.

### 2.3 Technical Choices

- **Modularity**: each agent is implemented in a separate class.
- **Clear naming**: class and file names describe their purpose.
- **Separate runners**: different environments can be tested without modifying the core agents.
- **Reward functions**: implemented directly in the runners.
- **Integrated visualization**: graphs make the learning behavior easier to analyze.

## 3. Reinforcement Learning Algorithms

The following algorithms are implemented and tested in the project:

- **ε-Greedy**
- **Upper Confidence Bound (UCB)**
- **Bandit Gradient**
- **Policy Iteration**
- **Value Iteration**
- **Q-learning**

The implementations are based on the algorithms studied during the course and are documented directly in the source code comments.

## 4. Case Studies and Experiments

### 4.1 SimpleRunner: Movie Recommendation

`SimpleRunner` compares ε-Greedy, UCB, and Bandit Gradient on the `SimpleRL.mch` model. The environment represents a user who likes movies A and B but not movie C. Each agent interacts with the environment for 500 steps.

At each step, the agent:

1. Chooses a movie to recommend.
2. Observes a reward: `1` if the movie is liked and `0` otherwise.
3. Updates its strategy.
4. Stores the reward for analysis.

**ε-Greedy** learns the rewarding movies quickly but performs limited exploration. **UCB** balances exploration and exploitation using an exploration bonus. **Bandit Gradient** learns a stochastic policy using Softmax probabilities and gradually assigns low probability to the non-rewarding movie.

In this static environment, all three approaches learn the useful preferences, with different exploration behaviors and convergence speeds.

### 4.2 YoutubeRunner: Dynamic Video Recommendation

`YoutubeRunner` simulates a YouTube recommendation system using the same three bandit agents. The `YouTube.mch` model is non-deterministic, and the reward depends on the viewing duration of a video.

The runner records rewards and video selection frequencies in order to compare the algorithms when user preferences evolve over time.

- **ε-Greedy** strongly exploits the best known option and performs limited exploration.
- **UCB** balances exploration and exploitation while considering uncertainty in the estimated rewards.
- **Bandit Gradient** adjusts its action preferences according to the rewards received and can adapt its probabilities over time.

The experiments show how the different strategies react to changing user preferences.

### 4.3 TicTacToeRunner: Learning Through a Game

`TicTacToeRunner` tests Policy Iteration, Value Iteration, and Q-learning in a deterministic Tic-Tac-Toe environment. The agent learns by playing against a random opponent.

The reward function is:

- **+1** for a win
- **0** for a draw or an unfinished game
- **-1** for a loss

The three algorithms are compared using their accumulated average rewards. The implementation also provides an interactive version of Tic-Tac-Toe against the trained agent.

For the experiments, the following parameters are used by default:

- `epsilon = 0.1`
- `gamma = 0.9`
- `alpha = 0.1`

Different initial states are generated between episodes to improve exploration during training.

#### Policy Iteration

Policy Iteration uses a long exploration phase before exploitation because Tic-Tac-Toe contains a large number of possible states. The implementation starts exploitation after enough states and transitions have been collected.

The learning phase is computationally expensive because the algorithm requires knowledge of states, transitions, and rewards.

#### Value Iteration

Value Iteration also uses an exploration phase followed by exploitation. Its execution is faster than Policy Iteration because its calculations are less expensive in this implementation.

#### Q-learning

Q-learning is model-free and uses a Q-table to store state-action values. Its implementation is simpler and the Q-table can be updated efficiently during learning. It therefore converges faster during shorter training sessions.

With more training episodes, the iterative model-based approaches can improve significantly as more states and transitions become available.

## 5. Reward Function and Learning

### 5.1 Reward Modeling

#### SimpleRunner

The `SimpleRL.mch` model is deterministic. The user likes movies A and B and dislikes C. The reward is therefore binary:

```java
String res = getState().eval("res").toString();
double reward = res.equals("OK") ? 1.0 : 0.0;
```

If the user likes the movie, the reward is `1`; otherwise it is `0`.

#### YoutubeRunner

The `YouTube.mch` model is non-deterministic. The reward depends on the viewing duration of the selected video:

```java
String rawDuration = getState().eval("duration").toString();
double reward = parseDuration(rawDuration);
```

The duration is converted into a normalized value between `0.0` and `1.0`, representing the user's engagement with the video.

#### TicTacToeRunner

The `TicTacToe.mch` model is deterministic and has three possible outcomes:

- Win: `+1`
- Draw or game in progress: `0`
- Loss: `-1`

The state is evaluated to determine whether the player has won.

### 5.2 Adaptation to Environments

- **SimpleRunner**: the environment is stable and user preferences do not change.
- **YoutubeRunner**: the environment is dynamic and the algorithms must adapt to changing preferences.
- **TicTacToeRunner**: the environment is stable, with a finite set of states and fixed winning conditions.

## 6. Conclusion and Remarks

This project resulted in a versatile Reinforcement Learning library capable of working with several types of environments. By studying both simple and more complex scenarios, such as recommendation systems and Tic-Tac-Toe, the project highlights the differences between several RL approaches.

Model-based iterative methods such as **Value Iteration** and **Policy Iteration** are particularly suited to environments where the model is available, while **Q-learning** and multi-armed bandit methods can learn directly from interactions and rewards.

The project also provides practical insight into the **exploration/exploitation trade-off** and the importance of choosing an algorithm according to the characteristics of the environment.

### Additional remarks

The `Visualization.java` class was implemented with assistance from ChatGPT. The project focused primarily on the implementation and evaluation of Reinforcement Learning algorithms, while graph generation was treated as a supporting component.

The `YouTube.mch` model was also adapted so that the `duration` value could be accessed directly from Java using `eval("duration")`. This was done by making `duration` a state variable and updating it directly inside the `choose` operation.

------------------------------------------------------------
 Documentation :
------------------------------------------------------------
A project report is available in the reinforcement-learning-project/rl-lib/src/main/resources/docs folder. It contains all the necessary information and useful resources, including technical explanations, descriptions of the algorithms, and the code structure. The report is available in two versions: French and English.
------------------------------------------------------------
## Author

**Anaïs BELAL**
