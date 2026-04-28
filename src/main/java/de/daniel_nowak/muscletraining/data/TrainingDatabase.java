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

    // ---------------------------------------------------------
    // LOAD (abwärtskompatibel)
    // ---------------------------------------------------------

    public void load() {
        trainings.clear();

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {

                // Format alt: 6 Felder
                // Format neu: 7 Felder (muscleIds)
                String[] p = line.split(";", -1);

                if (p.length >= 6) {

                    Training t = new Training(
                            p[0],
                            Long.parseLong(p[1]),
                            Integer.parseInt(p[2]),
                            Integer.parseInt(p[3]),
                            Float.parseFloat(p[4]),
                            p[5]
                    );

                    // NEU: Muskel-IDs laden (falls vorhanden)
                    if (p.length >= 7 && !p[6].isEmpty()) {
                        t.muscleIds = new ArrayList<>(Arrays.asList(p[6].split(",")));
                    }

                    trainings.put(t.getId(), t);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------
    // SAVE (neues Format)
    // ---------------------------------------------------------

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

                // ---------------------------------------------
                // TRAINING ENTFERNEN, WENN ES KOMPLETT WERTLOS IST
                // ---------------------------------------------
                if (exerciseDeleted && allMusclesDeleted) {
                    continue; // NICHT speichern
                }

                String muscleList = String.join(",", t.muscleIds);

                bw.write(
                        t.getId() + ";" +
                                t.getTime() + ";" +
                                t.getSets() + ";" +
                                t.getReps() + ";" +
                                t.getWeight() + ";" +
                                t.getExerciseId() + ";" +
                                muscleList +
                                "\n"
                );
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------
    // ADD (mit Muskel-IDs)
    // ---------------------------------------------------------

    public Training add(Exercise ex, int sets, int reps, float weight) {

        String id = UUID.randomUUID().toString();
        long time = System.currentTimeMillis();

        Training t = new Training(id, time, sets, reps, weight, ex.getId());

        // NEU: trainierte Muskeln speichern
        t.muscleIds.clear();
        t.muscleIds.addAll(ex.muscleIds);

        trainings.put(id, t);
        save();
        return t;
    }

    // ---------------------------------------------------------
    // DELETE EXERCISE → Trainings NICHT löschen!
    // ---------------------------------------------------------

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
