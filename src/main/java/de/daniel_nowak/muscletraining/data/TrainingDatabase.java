package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.io.*;
import java.util.*;

import de.daniel_nowak.muscletraining.model.Exercise;
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

                String[] p = line.split(";", -1);
                Training t = new Training(
                        p[0],
                        Long.parseLong(p[1]),
                        Integer.parseInt(p[2]),
                        Integer.parseInt(p[3]),
                        Float.parseFloat(p[4]),
                        p[5]
                );

                t.muscleIds = new ArrayList<>(Arrays.asList(p[6].split(",")));
                try {
                    t.difficulty = Integer.parseInt(p[7]);
                } catch (Exception ignored) {
                }

                trainings.put(t.getId(), t);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {

            for (Training t : trainings.values()) {

                boolean exerciseDeleted = t.getExerciseId().startsWith("deleted:");

                boolean allMusclesDeleted = true;
                for (String mId : t.muscleIds) {
                    if (!mId.startsWith("deleted:")) {
                        allMusclesDeleted = false;
                        break;
                    }
                }

                if (exerciseDeleted && allMusclesDeleted) {
                    continue;
                }

                String muscleList = String.join(",", t.muscleIds);

                bw.write(
                        t.getId() + ";" +
                                t.getTime() + ";" +
                                t.getSets() + ";" +
                                t.getReps() + ";" +
                                t.getWeight() + ";" +
                                t.getExerciseId() + ";" +
                                muscleList + ";" +
                                t.difficulty +
                                "\n"
                );

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void markExerciseDeleted(String exerciseId) {

        for (Training t : trainings.values()) {
            if (t.getExerciseId().equals(exerciseId)) {
                t.setExerciseId("deleted:" + exerciseId);
            }
        }

        save();
    }

    public void markMuscleDeleted(String muscleId) {

        for (Training t : trainings.values()) {

            List<String> updated = new ArrayList<>();

            for (String mId : t.muscleIds) {
                if (mId.equals(muscleId)) {
                    updated.add("deleted:" + muscleId);
                } else {
                    updated.add(mId);
                }
            }

            t.muscleIds = updated;
        }

        save();
    }

}
