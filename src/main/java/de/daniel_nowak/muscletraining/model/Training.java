package de.daniel_nowak.muscletraining.model;

import java.util.ArrayList;
import java.util.List;

public class Training {

    private final String id;
    private final long time;
    private final int sets;
    private final int reps;
    private final float weight;

    private String exerciseId;

    // NEU: trainierte Muskeln
    public List<String> muscleIds = new ArrayList<>();

    // NEU: subjektive Schwierigkeit
    // -1 = nicht bewertet
    //  0 = leicht
    //  1 = angenehm
    //  2 = schwer
    public int difficulty = -1;

    public Training(String id, long time, int sets, int reps, float weight, String exerciseId) {
        this.id = id;
        this.time = time;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.exerciseId = exerciseId;
    }

    public String getId() { return id; }
    public long getTime() { return time; }
    public int getSets() { return sets; }
    public int getReps() { return reps; }
    public float getWeight() { return weight; }
    public String getExerciseId() { return exerciseId; }

    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    // NEU
    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
}
