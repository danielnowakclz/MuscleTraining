package de.daniel_nowak.muscletraining.model;

import java.util.ArrayList;
import java.util.List;

public class Exercise {

    private final String id;
    private String name;

    // Trainingsparameter
    private float weightMin = 20f;
    private float weightMax = 200f;
    private float weightStep = 2.5f;

    private int setsMin = 3;
    private int setsMax = 5;

    private int repsMin = 5;
    private int repsMax = 15;
    private int repsStep = 1;

    private long lastTraining = 0L;
    private int lastSets = 0;
    private int lastReps = 0;
    private float lastWeight = 0f;

    private int lastDifficulty = -1;

    // Zugehörige Muskeln
    public List<String> muscleIds = new ArrayList<>();

    public Exercise(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getter/Setter
    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public float getWeightMin() { return weightMin; }
    public void setWeightMin(float weightMin) { this.weightMin = weightMin; }

    public float getWeightMax() { return weightMax; }
    public void setWeightMax(float weightMax) { this.weightMax = weightMax; }

    public float getWeightStep() { return weightStep; }
    public void setWeightStep(float weightStep) { this.weightStep = weightStep; }

    public int getSetsMin() { return setsMin; }
    public void setSetsMin(int setsMin) { this.setsMin = setsMin; }

    public int getSetsMax() { return setsMax; }
    public void setSetsMax(int setsMax) { this.setsMax = setsMax; }

    public int getRepsMin() { return repsMin; }
    public void setRepsMin(int repsMin) { this.repsMin = repsMin; }

    public int getRepsMax() { return repsMax; }
    public void setRepsMax(int repsMax) { this.repsMax = repsMax; }

    public int getRepsStep() { return repsStep; }
    public void setRepsStep(int repsStep) { this.repsStep = repsStep; }

    @Override
    public String toString() {
        return name;
    }

    public long getLastTraining() {
        return lastTraining;
    }

    public void setLastTraining(long lastTraining) {
        this.lastTraining = lastTraining;
    }

    public int getLastSets() {
        return lastSets;
    }

    public void setLastSets(int lastSets) {
        this.lastSets = lastSets;
    }

    public int getLastReps() {
        return lastReps;
    }

    public void setLastReps(int lastReps) {
        this.lastReps = lastReps;
    }

    public float getLastWeight() {
        return lastWeight;
    }

    public void setLastWeight(float lastWeight) {
        this.lastWeight = lastWeight;
    }

    public float getLastVolume() { return lastSets * lastReps * lastWeight;
    }

    public int getLastDifficulty() {
        return lastDifficulty;
    }

    public void setLastDifficulty(int lastDifficulty) {
        this.lastDifficulty = lastDifficulty;
    }
}
