import java.util.*;
class Player {

    final int emissions = 9;
    final int states = 5;
    final int species = Constants.COUNT_SPECIES;
    HMM[] models = new HMM[species];
    int numberBirds;
    int currentRound;
    int start =2;
    int time;

    public Player() {
        
        //initialize our models
        for (int i = 0; i < species; i++) {
            models[i] = new HMM(states, emissions);
        }
    }

    /**
     * Shoot!
     *
     * This is the function where you start your work.
     *
     * You will receive a variable pState, which contains information about all
     * birds, both dead and alive. Each bird contains all past moves.
     *
     * The state also contains the scores for all players and the number of
     * time steps elapsed since the last time this function was called.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
     */
    public Action shoot(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to get the best action.
         * This skeleton never shoots.
         */
        
        //get the number of birds in this state
        //return the most likely action that matches the move for each bird
            //
            //
            //
       // for (int i = 0; i < pState.getNumBirds(); i++ ) {
          //  Action a = getNextMove( )
       // }
       // 
       // 
        this.time++;
        this.numberBirds = pState.getNumBirds();
        if (pState.getRound() > this.currentRound) {
            this.currentRound = pState.getRound();
        }
        if (time < 70)
            return cDontShoot;
    

        if(this.currentRound > start) {
            for (int i = 0; i < pState.getNumBirds(); i++){
                Action a = getNextMove(pState.getBird(i),i);
                if (a != null){
                    return a;
                }
            }
            
        }



        // This line chooses not to shoot.
        return cDontShoot;

        // This line would predict that bird 0 will move right and shoot at it.
        // return Action(0, MOVE_RIGHT);
    }

    public Action getNextMove(Bird bird, int index){
        if (bird.isDead()){
            return null;
        }

        int species = getLikelySpecies(bird);
        if (species == Constants.SPECIES_BLACK_STORK) {
            return null;
        }

        int[] obs = new int[bird.getSeqLength() + 1];
        for (int i = 0; i < bird.getSeqLength(); i++) {
            obs[i] = bird.getObservation(i);
        }


        HMM model = this.models[species];
        System.err.println("SPECIES :"+ species);
        //model.estimateModel(getObservations(bird));
        //model.printMatrix();

        double[] probabilities = new double[Constants.COUNT_MOVE];
        for (int i = 0; i < Constants.COUNT_MOVE; i++) {
            obs[bird.getSeqLength()] = i;

            probabilities[i] = model.estimateProbabilityOfEmissionSequence(obs);
            // System.err.println("PROBABILITY :"+ probabilities[i]);
        }
        for (int i = 0; i < probabilities.length; i++) {          
            if (probabilities[i] > 0.5)
                //System.err.println("PROBABILITY :"+ probabilities[i]);
                return new Action(index, i);
        }

        return null;
    }

    public int getLikelySpecies(Bird bird) {
        //for each one of our models(for each species), return the one that maximises the probability of the observation sequence(for our bird) given the model.
        
        double bestP = 0;
        int bestIndex = 0;
        for (int i = 0; i < species; i++) {
            double p = this.models[i].estimateProbabilityOfEmissionSequence(getObservations(bird));
             //System.err.println("P :"+ p);
            if (bestP < p) {
                bestP = p;
                bestIndex = i;
            }

        }

        return bestIndex;
    }

    public int[] getObservations(Bird bird) {
        ArrayList<Integer> obsList = new ArrayList<Integer>();
        for (int i = 0; i < bird.getSeqLength(); i++) {
            if (bird.wasDead(i)) {
                break;
            }
            else {
                obsList.add(bird.getObservation(i));
            }
        }

        int[] obs = new int[obsList.size()];
        for (int i = 0; i < obsList.size(); i++) {
            obs[i] = obsList.get(i);
        }

        return obs; 
    }

    public void printObservations(int[] observations) {
        System.err.println("OBSERVATIONS");
        for (int i = 0; i < observations.length; i++) {
            System.err.print(observations[i]);
        }
        System.err.println("Size: "+ observations.length );

    }


    /**
     * Guess the species!
     * This function will be called at the end of each round, to give you
     * a chance to identify the species of the birds for extra points.
     *
     * Fill the vector with guesses for the all birds.
     * Use SPECIES_UNKNOWN to avoid guessing.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return a vector with guesses for all the birds
     */
    public int[] guess(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to guess the species of
         * each bird. This skeleton makes no guesses, better safe than sorry!
         */

        int[] lGuess = new int[pState.getNumBirds()];
        for (int i = 0; i < pState.getNumBirds(); ++i) {
            Bird bird = pState.getBird(i);
            lGuess[i] = getLikelySpecies(bird);
            //lGuess[i] = Constants.SPECIES_UNKNOWN;
        }            
        return lGuess;
    }

    /**
     * If you hit the bird you were trying to shoot, you will be notified
     * through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pBird the bird you hit
     * @param pDue time before which we must have returned
     */
    public void hit(GameState pState, int pBird, Deadline pDue) {
        System.err.println("HIT BIRD!!!");
    }

    /**
     * If you made any guesses, you will find out the true species of those
     * birds through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pSpecies the vector with species
     * @param pDue time before which we must have returned
     */
    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {

        for (int i = 0; i < pSpecies.length; i++) {
            Bird bird = pState.getBird(i);

            this.models[pSpecies[i]].estimateModel(getObservations(bird));
            //printObservations(getObservations(bird));
            //System.err.println("Size2: "+ bird.getSeqLength() );
            this.models[pSpecies[i]].printMatrix();
        }


    }

    public static final Action cDontShoot = new Action(-1, -1);
}
