package de.daniel_nowak.muscletraining.model;

import java.util.ArrayList;
import java.util.List;

public class Muscle {

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

    // Grobe funktionelle Kategorien
    public enum Category {
        ARM,
        SHOULDER,
        CHEST,
        BACK,
        CORE,
        LEG
    }

    public float getRegenerationHours() {
        switch (category) {
            case ARM: return 24f;
            case SHOULDER: return 36f;
            case CHEST: return 48f;
            case BACK: return 48f;
            case CORE: return 36f;
            case LEG: return 72f;
        }
        return 48f;
    }

    private long lastTraining = 0L;
    private int lastSets = 0;
    private int lastReps = 0;
    private float lastWeight = 0f;


    private final String id;
    private String name;

    // Kategorie (NEU)
    public Category category = Category.CORE;

    // Beziehungen über IDs
    public List<String> exerciseIds = new ArrayList<>();

    // Mehrere Marker
    public List<Float> posXList = new ArrayList<>();
    public List<Float> posYList = new ArrayList<>();
    public List<String> sideList = new ArrayList<>();

    public Muscle(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return getName() != null ? getName() : "(Unbenannt)";
    }
}
