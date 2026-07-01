package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.io.*;
import java.util.*;

import de.daniel_nowak.muscletraining.R;
import de.daniel_nowak.muscletraining.model.Exercise;

public class ExerciseDatabase {

    private static final String FILE_NAME = "exercises.db";
    private final File file;

    public Map<String, Exercise> exercises = new HashMap<>();

    private final Context context;

    public ExerciseDatabase(Context context) {
        this.context = context;
        file = new File(context.getFilesDir(), FILE_NAME);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(";", "\\;");
    }

    private String unesc(String s) {
        if (s == null) return "";
        return s.replace("\\;", ";").replace("\\\\", "\\");
    }

    private List<String> parseLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean escMode = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (escMode) {
                sb.append(c);
                escMode = false;
            } else if (c == '\\') {
                escMode = true;
            } else if (c == ';') {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        out.add(sb.toString());
        return out;
    }

    public void load() {
        exercises.clear();

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {

                List<String> p = parseLine(line);

                Exercise ex = new Exercise(p.get(0), unesc(p.get(1)));

                try {
                    ex.setWeightMin(Float.parseFloat(p.get(2)));
                } catch (Exception ignored) {
                }
                try {
                    ex.setWeightMax(Float.parseFloat(p.get(3)));
                } catch (Exception ignored) {
                }
                try {
                    ex.setWeightStep(Float.parseFloat(p.get(4)));
                } catch (Exception ignored) {
                }

                try {
                    ex.setSetsMin(Integer.parseInt(p.get(5)));
                } catch (Exception ignored) {
                }
                try {
                    ex.setSetsMax(Integer.parseInt(p.get(6)));
                } catch (Exception ignored) {
                }

                try {
                    ex.setRepsMin(Integer.parseInt(p.get(7)));
                } catch (Exception ignored) {
                }
                try {
                    ex.setRepsMax(Integer.parseInt(p.get(8)));
                } catch (Exception ignored) {
                }
                try {
                    ex.setRepsStep(Integer.parseInt(p.get(9)));
                } catch (Exception ignored) {
                }

                try {
                    ex.setLastTraining(Long.parseLong(p.get(10)));
                } catch (Exception ignored) {
                }
                try {
                    ex.setLastSets(Integer.parseInt(p.get(11)));
                } catch (Exception ignored) {
                }
                try {
                    ex.setLastReps(Integer.parseInt(p.get(12)));
                } catch (Exception ignored) {
                }
                try {
                    ex.setLastWeight(Float.parseFloat(p.get(13)));
                } catch (Exception ignored) {
                }
                try {
                    ex.setLastDifficulty(Integer.parseInt(p.get(14)));
                } catch (Exception ignored) {
                }

                try {
                        ex.muscleIds = new ArrayList<>(Arrays.asList(p.get(15).split(",")));
                } catch (Exception ignored) {
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

                String muscleList = String.join(",", ex.muscleIds);

                bw.write(
                        ex.getId() + ";" +
                                esc(ex.getName()) + ";" +
                                ex.getWeightMin() + ";" +
                                ex.getWeightMax() + ";" +
                                ex.getWeightStep() + ";" +
                                ex.getSetsMin() + ";" +
                                ex.getSetsMax() + ";" +
                                ex.getRepsMin() + ";" +
                                ex.getRepsMax() + ";" +
                                ex.getRepsStep() + ";" +
                                ex.getLastTraining() + ";" +
                                ex.getLastSets() + ";" +
                                ex.getLastReps() + ";" +
                                ex.getLastWeight() + ";" +
                                ex.getLastDifficulty() + ";" +
                                muscleList +
                                "\n"
                );
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

    private Exercise addDemo(String id, String name) {
        Exercise ex = add(id, name);
        if (name.startsWith("KH")) {
            ex.setSetsMin(2);
            ex.setSetsMax(5);
            ex.setRepsMin(8);
            ex.setRepsMax(30);
            ex.setRepsStep(2);
            ex.setWeightMin(5.0f);
            ex.setWeightMax(32.0f);
            ex.setWeightStep(2.5f);
        } else {
            ex.setSetsMin(2);
            ex.setSetsMax(5);
            ex.setRepsMin(8);
            ex.setRepsMax(30);
            ex.setRepsStep(2);
            ex.setWeightMin(2.0f);
            ex.setWeightMax(18.0f);
            ex.setWeightStep(2.0f);
        }
        return ex;
    }

    public void addDemoData() {

        exercises.clear();

        addDemo("kh01", context.getString(R.string.exercise_kh01)).muscleIds.addAll(Arrays.asList(
                "biceps", "shoulders", "forearms"
        ));

        addDemo("kh03", context.getString(R.string.exercise_kh03)).muscleIds.addAll(Arrays.asList(
                "biceps", "forearms"
        ));

        addDemo("kh02", context.getString(R.string.exercise_kh02)).muscleIds.addAll(Arrays.asList(
                "triceps", "shoulders", "core_stabilizers"
        ));

        addDemo("kh04", context.getString(R.string.exercise_kh04)).muscleIds.addAll(Arrays.asList(
                "triceps", "shoulders", "core_stabilizers"
        ));

        addDemo("kh06", context.getString(R.string.exercise_kh06)).muscleIds.addAll(Arrays.asList(
                "shoulders", "trapezius"
        ));

        addDemo("kh11", context.getString(R.string.exercise_kh11)).muscleIds.addAll(Arrays.asList(
                "shoulders", "triceps", "core_stabilizers"
        ));

        addDemo("kh15", context.getString(R.string.exercise_kh15)).muscleIds.addAll(Arrays.asList(
                "chest", "triceps", "shoulders"
        ));

        addDemo("kh16", context.getString(R.string.exercise_kh16)).muscleIds.addAll(Arrays.asList(
                "chest", "shoulders"
        ));

        addDemo("kh13", context.getString(R.string.exercise_kh13)).muscleIds.addAll(Arrays.asList(
                "back_upper", "biceps", "shoulders", "core_stabilizers"
        ));

        addDemo("kh36", context.getString(R.string.exercise_kh36)).muscleIds.addAll(Arrays.asList(
                "back_upper", "biceps", "shoulders", "core_stabilizers"
        ));

        addDemo("kh40", context.getString(R.string.exercise_kh40)).muscleIds.addAll(Arrays.asList(
                "hamstrings", "gluteus"
        ));

        addDemo("kh49", context.getString(R.string.exercise_kh49)).muscleIds.addAll(Arrays.asList(
                "hamstrings", "gluteus", "back_lower", "core_stabilizers"
        ));

        addDemo("kh18", context.getString(R.string.exercise_kh18)).muscleIds.addAll(Arrays.asList(
                "abs_straight", "core_stabilizers"
        ));

        addDemo("kh20", context.getString(R.string.exercise_kh20)).muscleIds.addAll(Arrays.asList(
                "abs_straight", "hip_flexors", "core_stabilizers"
        ));

        addDemo("kh19", context.getString(R.string.exercise_kh19)).muscleIds.addAll(Arrays.asList(
                "abs_oblique", "core_rotators", "core_stabilizers"
        ));

        addDemo("kh30", context.getString(R.string.exercise_kh30)).muscleIds.addAll(Arrays.asList(
                "core_rotators", "abs_oblique", "core_stabilizers"
        ));

        addDemo("kh24", context.getString(R.string.exercise_kh24)).muscleIds.addAll(Arrays.asList(
                "core_stabilizers", "abs_straight", "shoulders"
        ));

        addDemo("kh28", context.getString(R.string.exercise_kh28)).muscleIds.addAll(Arrays.asList(
                "core_stabilizers", "abs_oblique", "gluteus"
        ));

        addDemo("kh38", context.getString(R.string.exercise_kh38)).muscleIds.addAll(Arrays.asList(
                "quadriceps", "gluteus", "core_stabilizers"
        ));

        addDemo("kh41", context.getString(R.string.exercise_kh41)).muscleIds.addAll(Arrays.asList(
                "quadriceps", "gluteus", "core_stabilizers"
        ));

        addDemo("kh43", context.getString(R.string.exercise_kh43)).muscleIds.addAll(Arrays.asList(
                "abductors", "core_stabilizers"
        ));

        addDemo("kh44", context.getString(R.string.exercise_kh44)).muscleIds.addAll(Arrays.asList(
                "adductors", "core_stabilizers"
        ));

        addDemo("kh42", context.getString(R.string.exercise_kh42)).muscleIds.addAll(Arrays.asList(
                "gluteus", "hamstrings", "core_stabilizers"
        ));

        addDemo("kh45", context.getString(R.string.exercise_kh45)).muscleIds.addAll(Arrays.asList(
                "gluteus", "hamstrings", "core_stabilizers"
        ));

        addDemo("kb01", context.getString(R.string.exercise_kb01)).muscleIds.addAll(Arrays.asList(
                "hamstrings", "gluteus", "back_lower", "core_stabilizers"
        ));

        addDemo("kb49", context.getString(R.string.exercise_kb49)).muscleIds.addAll(Arrays.asList(
                "hamstrings", "gluteus", "back_lower", "core_stabilizers"
        ));

        addDemo("kb03", context.getString(R.string.exercise_kb03)).muscleIds.addAll(Arrays.asList(
                "gluteus", "hamstrings", "core_stabilizers", "back_lower"
        ));

        addDemo("kb15", context.getString(R.string.exercise_kb15)).muscleIds.addAll(Arrays.asList(
                "gluteus", "hamstrings", "shoulders", "core_stabilizers"
        ));

        addDemo("kb16", context.getString(R.string.exercise_kb16)).muscleIds.addAll(Arrays.asList(
                "shoulders", "triceps", "core_stabilizers", "quadriceps"
        ));

        addDemo("kb47", context.getString(R.string.exercise_kb47)).muscleIds.addAll(Arrays.asList(
                "shoulders", "quadriceps", "gluteus", "core_stabilizers", "triceps"
        ));

        addDemo("kb31", context.getString(R.string.exercise_kb31)).muscleIds.addAll(Arrays.asList(
                "chest", "triceps", "shoulders", "core_stabilizers"
        ));

        addDemo("kb32", context.getString(R.string.exercise_kb32)).muscleIds.addAll(Arrays.asList(
                "chest", "triceps", "shoulders"
        ));

        addDemo("kb33", context.getString(R.string.exercise_kb33)).muscleIds.addAll(Arrays.asList(
                "back_upper", "biceps", "shoulders", "core_stabilizers"
        ));

        addDemo("kb35", context.getString(R.string.exercise_kb35)).muscleIds.addAll(Arrays.asList(
                "back_upper", "biceps", "shoulders", "core_stabilizers", "abs_straight"
        ));

        addDemo("kb38", context.getString(R.string.exercise_kb38)).muscleIds.addAll(Arrays.asList(
                "abs_straight", "shoulders", "core_stabilizers"
        ));

        addDemo("kb40", context.getString(R.string.exercise_kb40)).muscleIds.addAll(Arrays.asList(
                "abs_straight", "core_stabilizers", "hip_flexors"
        ));

        addDemo("kb39", context.getString(R.string.exercise_kb39)).muscleIds.addAll(Arrays.asList(
                "abs_oblique", "core_rotators", "core_stabilizers"
        ));

        addDemo("kb30", context.getString(R.string.exercise_kb30)).muscleIds.addAll(Arrays.asList(
                "core_rotators", "abs_oblique", "core_stabilizers"
        ));

        addDemo("kb12", context.getString(R.string.exercise_kb12)).muscleIds.addAll(Arrays.asList(
                "core_stabilizers", "shoulders", "grip", "back_upper"
        ));

        addDemo("kb41", context.getString(R.string.exercise_kb41)).muscleIds.addAll(Arrays.asList(
                "core_stabilizers", "abs_oblique", "gluteus"
        ));

        addDemo("kb48", context.getString(R.string.exercise_kb48)).muscleIds.addAll(Arrays.asList(
                "quadriceps", "gluteus", "core_stabilizers", "shoulders"
        ));

        addDemo("kb50", context.getString(R.string.exercise_kb50)).muscleIds.addAll(Arrays.asList(
                "quadriceps", "gluteus", "core_stabilizers"
        ));

        addDemo("kb42", context.getString(R.string.exercise_kb42)).muscleIds.addAll(Arrays.asList(
                "abductors", "core_stabilizers", "abs_oblique"
        ));

        addDemo("kb46", context.getString(R.string.exercise_kb46)).muscleIds.addAll(Arrays.asList(
                "adductors", "quadriceps", "gluteus", "core_stabilizers"
        ));

        save();
    }

}
