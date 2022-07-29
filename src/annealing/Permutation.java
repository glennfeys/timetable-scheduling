package annealing;

public interface Permutation {

    double scoreDifference(AnTimeTable timeTable);
    void permute(AnTimeTable timeTable);
}
