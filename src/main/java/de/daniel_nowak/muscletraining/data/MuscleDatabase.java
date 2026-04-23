package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.io.*;
import java.util.*;

import de.daniel_nowak.muscletraining.model.Muscle;

public class MuscleDatabase {

    private static final String FILE_NAME = "muscles.db";
    private final File file;

    public Map<String, Muscle> muscles = new HashMap<>();

    public MuscleDatabase(Context context) {
        file = new File(context.getFilesDir(), FILE_NAME);
    }

    public void load() {
        muscles.clear();

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";", 2);
                if (parts.length == 2) {
                    Muscle m = new Muscle(parts[0], parts[1]);
                    muscles.put(m.getId(), m);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Muscle m : muscles.values()) {
                bw.write(m.getId() + ";" + m.getName() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Muscle add(String id, String name) {
        Muscle m = new Muscle(id, name);
        muscles.put(id, m);
        save();
        return m;
    }

    public void addDemoData() {

        // Brust / Drücken
        add("pectoralis_major", "Brust (Pectoralis Major)");
        add("anterior_deltoid", "Vordere Schulter");
        add("triceps", "Trizeps");

        // Rücken / Ziehen
        add("latissimus", "Latissimus");
        add("upper_back", "Oberer Rücken / Rhomboiden");
        add("posterior_deltoid", "Hintere Schulter");
        add("biceps", "Bizeps");

        // Beine
        add("quadriceps", "Quadrizeps (Vorderer Oberschenkel)");
        add("hamstrings", "Hamstrings (Hinterer Oberschenkel)");
        add("gluteus", "Gluteus / Po");
        add("calves", "Waden");

        // Core
        add("core", "Rumpf / Bauch");
    }

}
