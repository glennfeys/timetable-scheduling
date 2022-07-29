package annealing;

import json.JsonModel;
import json.JsonReader;
import json.JsonWriter;
import models.JsonReservation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class SimAnnealing {
    private int maxIteraties = 1000;
    private double startTemp = 10;
    private double temperatureDecay = 0.995;

    private AnTimeTable temBest;
    private AnTimeTable currentSolution;
    private double currentScore;
    private double bestScore;

    private boolean stop;
    private Random random = new Random();

    private double temperature;
    private int iterations;
    private int sinceBestCounter = 0;

    SimAnnealing(AnTimeTable start){
        currentSolution = start;
        currentScore = currentSolution.getScore();
        temBest = start;
        bestScore = currentScore;
    }

    public void start(){
        temperature = startTemp;
        stop = false;
        System.out.println("start: " + currentScore);
        iterations = 0;
        anneal();
    }

    public void anneal(){

        while (!stop) {
            double r = random.nextDouble();
            Permutation perm = currentSolution.randomPerm(r);
            double scoreDiff = perm.scoreDifference(currentSolution);

            if(accept(currentScore, currentScore + scoreDiff)){
                System.out.println("difference: " + scoreDiff);
                perm.permute(currentSolution);
                currentScore += scoreDiff;
                System.out.println("currentscore:" + currentScore);

                if(currentScore<bestScore){
                    bestScore = currentScore;
                    System.out.println("NEW BEST: best: " + bestScore);
                    temBest = new AnTimeTable(currentSolution);
                }else{
                    sinceBestCounter++;
                    if(sinceBestCounter>60){
                        sinceBestCounter = 0;
                        currentSolution = new AnTimeTable(temBest);
                    }
                }
                iterations++;
                adjustTemperature();
            }

            if (temperature < 0.01 || iterations>maxIteraties) {
                System.out.println("iterations = " + iterations);;
                stop = true;
            }

        }
    }



    public boolean accept(double currentScore, double nextScore){
        if(nextScore<currentScore){
            return true;
        }
        if(Math.exp(-(nextScore-currentScore)*temperature)>random.nextDouble()){
            return true;
        }
        return false;
    }

    public void adjustTemperature(){
        temperature *= temperatureDecay;
    }


    public static void main(String[] args) {
        JsonReader jsonReader = new JsonReader();
        JsonModel jsonModel = jsonReader.readJson("project.json");
        jsonModel.init();

        ArrayList<JsonReservation> reservations = jsonReader.readReservations("resultToIncrease.json");
        System.out.println("Total number of reservations: " + reservations.size());
        AnTimeTable start = new AnTimeTable(jsonModel, reservations);

        SimAnnealing annealer = new SimAnnealing(start);
        System.out.println("Reservations in annealer: " + annealer.getBest().amountOfReservations());
        annealer.getBest().printScores();

        System.out.println("--START ANNEALING--");
        annealer.start();
        System.out.println("--ANNEALING FINISHED--");
        AnTimeTable best = annealer.getBest();
        best.printScores();
        best.multiplyWeekby13();
        Collection<JsonReservation> jsonReservations = best.toJsonReservations();
        new JsonWriter().writeJsonModels(jsonReservations, "resultAn.json");
    }

    private AnTimeTable getBest() {
        return temBest;
    }
}
