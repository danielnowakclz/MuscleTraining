package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.io.*;
import java.util.*;

import de.daniel_nowak.muscletraining.model.Training;

public class TrainingDatabase {

    private static final String FILE_NAME = "trainings.db";
    private final File file;

    public Map<String, Training> trainings = new HashMap<>();

    public TrainingDatabase(Context context) {
        file = new File(context.getFilesDir(), FILE_NAME);
    }

    public void load() {
        trainings.clear();

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";", 6);
                if (parts.length == 6) {
                    Training t = new Training(
                            parts[0],
                            Long.parseLong(parts[1]),
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]),
                            Float.parseFloat(parts[4]),
                            parts[5]
                    );
                    trainings.put(t.getId(), t);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Training t : trainings.values()) {
                bw.write(t.getId() + ";" +
                        t.getTime() + ";" +
                        t.getSets() + ";" +
                        t.getReps() + ";" +
                        t.getWeight() + ";" +
                        t.getExerciseId() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Training add(String exerciseId, int sets, int reps, float weight) {
        String id = UUID.randomUUID().toString();
        long time = System.currentTimeMillis();

        Training t = new Training(id, time, sets, reps, weight, exerciseId);
        trainings.put(id, t);
        save();
        return t;
    }

}
