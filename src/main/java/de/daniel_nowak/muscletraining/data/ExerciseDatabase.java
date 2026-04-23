package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.io.*;
import java.util.*;

import de.daniel_nowak.muscletraining.model.Exercise;

public class ExerciseDatabase {

    private static final String FILE_NAME = "exercises.db";
    private final File file;

    public Map<String, Exercise> exercises = new HashMap<>();

    public ExerciseDatabase(Context context) {
        file = new File(context.getFilesDir(), FILE_NAME);
    }

    public void load() {
        exercises.clear();

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] p = line.split(";", 11);

                Exercise ex = new Exercise(p[0], p[1]);

                ex.setMinWeight(Float.parseFloat(p[2]));
                ex.setMaxWeight(Float.parseFloat(p[3]));
                ex.setWeightStep(Float.parseFloat(p[4]));

                ex.setSetsMin(Integer.parseInt(p[5]));
                ex.setSetsMax(Integer.parseInt(p[6]));

                ex.setRepsMin(Integer.parseInt(p[7]));
                ex.setRepsMax(Integer.parseInt(p[8]));
                ex.setRepsStep(Integer.parseInt(p[9]));

                if (p.length == 11 && !p[10].isEmpty()) {
                    ex.muscleIds = new ArrayList<>(Arrays.asList(p[10].split(",")));
                }

                exercises.put(ex.getId(), ex);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Exercise ex : exercises.values()) {
                bw.write(ex.getId() + ";" +
                        ex.getName() + ";" +
                        ex.getMinWeight() + ";" +
                        ex.getMaxWeight() + ";" +
                        ex.getWeightStep() + ";" +
                        ex.getSetsMin() + ";" +
                        ex.getSetsMax() + ";" +
                        ex.getRepsMin() + ";" +
                        ex.getRepsMax() + ";" +
                        ex.getRepsStep() + ";");

                if (!ex.muscleIds.isEmpty()) {
                    bw.write(String.join(",", ex.muscleIds));
                }

                bw.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Exercise add(String id, String name) {
        Exercise ex = new Exercise(id, name);
        exercises.put(id, ex);
        save();
        return ex;
    }

    public void delete(String id) {
        exercises.remove(id);
        save();
    }


    public void addDemoData() {

        // --- Kurzhanteln (5–32.5 kg, 2.5 kg Schritte) ---
        addExercise("kh_bank", "KH Bankdrücken",
                5f, 32.5f, 2.5f,
                2, 5,
                8, 40, 2,
                "pectoralis_major", "triceps", "anterior_deltoid");

        addExercise("kh_schraeg", "KH Schrägbankdrücken",
                5f, 32.5f, 2.5f,
                2, 5,
                8, 40, 2,
                "pectoralis_major", "anterior_deltoid", "triceps");

        addExercise("kh_shoulder", "KH Schulterdrücken",
                5f, 32.5f, 2.5f,
                2, 5,
                8, 40, 2,
                "anterior_deltoid", "triceps");

        addExercise("kh_row", "KH Rudern einarmig",
                5f, 32.5f, 2.5f,
                2, 5,
                8, 40, 2,
                "latissimus", "upper_back", "biceps");

        addExercise("kh_reverse", "KH Reverse Fly",
                5f, 32.5f, 2.5f,
                2, 5,
                8, 40, 2,
                "posterior_deltoid", "upper_back");

        addExercise("kh_squat", "KH Kniebeuge",
                5f, 32.5f, 2.5f,
                2, 5,
                8, 40, 2,
                "quadriceps", "gluteus");

        addExercise("kh_lunge", "KH Ausfallschritte",
                5f, 32.5f, 2.5f,
                2, 5,
                8, 40, 2,
                "quadriceps", "gluteus", "hamstrings");

        addExercise("kh_sidebend", "KH Side Bend",
                5f, 32.5f, 2.5f,
                2, 5,
                8, 40, 2,
                "core");

        // --- Kettlebell (2–18 kg, 2 kg Schritte) ---
        addExercise("kb_swing", "KB Swing",
                2f, 18f, 2f,
                2, 5,
                8, 40, 2,
                "gluteus", "hamstrings", "core");

        addExercise("kb_goblet", "KB Goblet Squat",
                2f, 18f, 2f,
                2, 5,
                8, 40, 2,
                "quadriceps", "gluteus", "core");

        addExercise("kb_deadlift", "KB Deadlift",
                2f, 18f, 2f,
                2, 5,
                8, 40, 2,
                "hamstrings", "gluteus", "upper_back");

        addExercise("kb_row", "KB Rudern",
                2f, 18f, 2f,
                2, 5,
                8, 40, 2,
                "latissimus", "biceps", "upper_back");

        addExercise("kb_press", "KB Press",
                2f, 18f, 2f,
                2, 5,
                8, 40, 2,
                "anterior_deltoid", "triceps", "core");

        save();
    }

    private void addExercise(String id, String name,
                             float minW, float maxW, float stepW,
                             int setsMin, int setsMax,
                             int repsMin, int repsMax, int repsStep,
                             String... muscles) {

        Exercise ex = new Exercise(id, name);

        ex.setMinWeight(minW);
        ex.setMaxWeight(maxW);
        ex.setWeightStep(stepW);

        ex.setSetsMin(setsMin);
        ex.setSetsMax(setsMax);

        ex.setRepsMin(repsMin);
        ex.setRepsMax(repsMax);
        ex.setRepsStep(repsStep);

        ex.muscleIds = new ArrayList<>(Arrays.asList(muscles));

        exercises.put(id, ex);
    }

}
