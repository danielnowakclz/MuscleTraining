package de.daniel_nowak.muscletraining.model;

import java.util.ArrayList;
import java.util.List;

public class Training {

    private final String id;
    private long time;
    private int sets;
    private int reps;
    private float weight;

    private String exerciseId;

    // NEU: trainierte Muskeln
    public List<String> muscleIds = new ArrayList<>();

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

    public void setExerciseId(String exerciseId) { this.exerciseId=exerciseId; }
}
