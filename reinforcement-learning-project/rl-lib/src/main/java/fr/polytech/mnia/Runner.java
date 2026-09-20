package fr.polytech.mnia;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;

public abstract class Runner {
    protected MyProb animator = MyProb.INJECTOR.getInstance(MyProb.class);
    protected State initial ; // state initial
    protected State state ;   // this field represents the current state

    public Runner(String filePath){
        try {
            animator.load(filePath) ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.initial = animator.getStateSpace().getRoot() ;        
    }


    public void initialise(){
        Transition setup = initial.findTransition(Transition.SETUP_CONSTANTS_NAME);
        if (setup != null) {
            initial = setup.getDestination();
        }

        Transition initialisation = initial.findTransition(Transition.INITIALISE_MACHINE_NAME);
        if (initialisation != null) {
            initial = initialisation.getDestination();
        }

        this.state = initial.exploreIfNeeded() ;
    }


    public void initialiseAleaInitialState() { // new initialize version that varies initial states -> more exploration, especially at the beginning
        Transition setup = initial.findTransition(Transition.SETUP_CONSTANTS_NAME);
        if (setup != null) {
            initial = setup.getDestination();
        }

        List<Transition> all = initial.getOutTransitions();
        List<Transition> inits = new ArrayList<>();
        for (Transition t : all) {
            if (t.getName().equals(Transition.INITIALISE_MACHINE_NAME)) {
                inits.add(t);
            }
        }

        if (!inits.isEmpty()) {
            Transition chosenInit = inits.get(new Random().nextInt(inits.size()));
            initial = chosenInit.getDestination();
        }

        this.state = initial.exploreIfNeeded();
    }

    public State getState(){
        return this.state ;
    }

    public State getInitialState(){
        return this.initial ;
    }

    public StateSpace getStateSpace(){
        return this.animator.getStateSpace() ;
    }

    /*
     * Cette méthode affiche la source et la cible d'une
     * transition sans explorer la cible.
     */
    public void showTransition(Transition t){
        System.out.println("\nTransition : " + t.getId() + " - " + t.getName() + "[" + t.getParameterPredicate() + "]");
        System.out.println("\nSource : ") ; animator.printState(t.getSource()) ;
        System.out.println("\nDestination : ") ; animator.printState(t.getDestination()) ;
    }

    public abstract void execSequence() throws Exception ;

    // new method that applies a transition t and changes the current state accordingly
    public void applyTransition(Transition t) {
        this.state = this.state.perform(t.getName(), t.getParameterPredicate()).explore();
    }
   
    
    
}
