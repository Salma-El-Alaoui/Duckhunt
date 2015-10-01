import java.util.ArrayList;
import java.util.Arrays;


class Player1 {

    final int emissions = 9;
    final int states = 5;
    final int species = Constants.COUNT_SPECIES;

    ArrayList<ArrayList> models =  new ArrayList<ArrayList>();

    int numberBirds;
    int currentRound = 0;
    int start =2;
    int time = 0;

    public Player1() {
        
        //initialize our models
        for (int i = 0; i < species; i++) {
            models.add(i, new ArrayList<HMM>());
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


        this.time++;
        if(pState.getRound() > this.currentRound){
            this.currentRound = pState.getRound();
            this.time = 0;
        }


        if( time < 75){
            return cDontShoot;
        }

        if( currentRound > 0)
            return getNextMove(pState);
        return cDontShoot;
    }

    public Action getNextMove(GameState pState){


        double bestProbOverall = 0;
        int bestMoveOverall = -1;
        int bestBird = -1;

        for( int i = 0; i < pState.getNumBirds(); i++){

            if(pState.getBird(i).isDead()){
                break;
            }

            int species =  getLikelySpecies(pState.getBird(i));
            if( species == Constants.SPECIES_BLACK_STORK){
                break;
            }

            double bestProb = 0;
            int bestMove = 0;
            double[] probArray = new double[Constants.COUNT_MOVE];

            int[] obs = getObservations(pState.getBird(i));
            int[] obsNext = new int[obs.length+1];

            for(int x = 0; x < obs.length; x++){
                obsNext[x] = obs[x];
            }


            for(int j = 0; j < Constants.COUNT_MOVE; j++){

                obsNext[obsNext.length-1] = j;

                HMM moveJ = new HMM(states,emissions);
                moveJ.estimateModel(obsNext);

                probArray[j] = moveJ.estimateProbabilityOfEmissionSequence(obs);

//                p = moveJ.estimateProbabilityOfEmissionSequence(obs);
//                if( p > bestProb){
//                    bestProb = p;
//                    bestMove = j;
//                }
            }

            probArray = normalize(probArray);

            if( i == 0 ) {
                for (double d : probArray) {
                    System.err.print(" " + d);
                }
                System.err.println();
            }

            for(int j  = 0; j < probArray.length; j++){
                if( probArray[j] > bestProb ){
                    bestProb = probArray[j];
                    bestMove = j;
                }
            }


            if( bestProb > bestProbOverall){
                bestProbOverall = bestProb;
                bestMoveOverall = bestMove;
                bestBird = i;
            }
        }

        if( bestProbOverall > 0.9){
            return new Action(bestBird, bestMoveOverall);
        }
        return cDontShoot;

        
//       double[] probabilities = new double[Constants.COUNT_MOVE];
//        for (int i = 0; i < Constants.COUNT_MOVE; i++) {
//            obs[bird.getSeqLength()] = i;
//
//            probabilities[i] = model.estimateProbabilityOfEmissionSequence(obs);
//            System.err.println("PROBABILITY :"+ probabilities[i]);
//        }
//
//        probabilities = normalize(probabilities);
//        for (int i = 0; i < probabilities.length; i++) {
//            if (probabilities[i] > 0.65)
//                return new Action(index, i);
//                //System.err.println("PROBABILITY :"+ probabilities[i]);
//        }
//
        /*
        double[] stateDistribution = model.currentStateDistribution(this.time);
        double[] nextEmissions = model.estimateProbabilityDistributionOfNextEmission(stateDistribution);
        nextEmissions = normalize(nextEmissions);
        for (int i = 0; i < nextEmissions.length; i++) {          
            if (nextEmissions[i] > 0.45) {
                System.err.println("PROBABILITY :"+ nextEmissions[i]);
            }   return new Action(index, i);
        }
        */

    }

    public int getLikelySpecies(Bird bird) {
        //for each one of our models(for each species), return the one that maximises the probability of the observation sequence(for our bird) given the model.

        if(this.currentRound == 0){
            return 0;
//          return  (int)(Math.random() * 5);

        }

        double bestOverallP = 0;
        int bestSpecies = 0;
        double[] max  = new double[species];
        double[] average = new double[species];

        for (int i = 0; i < species; i++) {

            double bestP = 0;
            ArrayList<HMM> speciesArray = this.models.get(i);


            double avg = 0;

            for(int j = 0; j < speciesArray.size(); j++){

                double p = speciesArray.get(j).estimateProbabilityOfEmissionSequence(getObservations(bird));
                avg += p;

//                System.err.println("P :"+ p);
                if (bestP < p) {
                    bestP = p;
                }
            }

            average[i] = (double) avg / speciesArray.size();
            max[i] = bestP;

        }

        max  = normalize(max);

        for(int i  = 0; i < max.length; i++){
            if( max[i] > bestOverallP ){
                bestOverallP = max[i];
                bestSpecies = i;
            }
        }


//        if( currentRound < 3 ){
//            if( bestOverallP < 0.5){
//                bestSpecies = 0;
//            }
//        }


        return bestSpecies;
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
           // lGuess[i] = Constants.SPECIES_UNKNOWN;
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
//            System.err.println("LENGTH BIRD"+i+ "  "+getObservations(bird).length);
            int species = pSpecies[i];

            HMM birdModel = new HMM(states, emissions);
            birdModel.estimateModel(getObservations(bird));

            this.models.get(species).add(birdModel);

        }


    }

    public static final Action cDontShoot = new Action(-1, -1);

    public static double[] normalize(double[] a) {

        double sum =0.0;
        double[] a2 = new double[a.length];
        for (int i = 0; i < a.length; i++) {
          sum += a[i];
        }
        //System.err.println("SUM "+ sum);
        for (int i = 0; i < a.length; i++) {
          a2[i] = a[i] * (1.0 / sum);
        }

        return a2;
    }



}
