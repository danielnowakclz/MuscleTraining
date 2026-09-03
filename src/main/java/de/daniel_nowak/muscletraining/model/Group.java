package de.daniel_nowak.muscletraining.model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    public String id;
    public String name;

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> exerciseIds = new ArrayList<>();
}
