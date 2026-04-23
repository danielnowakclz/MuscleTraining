package de.daniel_nowak.muscletraining.model;

import java.util.ArrayList;
import java.util.List;

public class Muscle {

    private final String id;
    private String name;

    // Beziehungen über IDs
    public List<String> exerciseIds = new ArrayList<>();
    public List<String> trainingIds = new ArrayList<>();

    public Muscle(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
