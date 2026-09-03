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
                try {
                    ex.groupIds = new ArrayList<>(Arrays.asList(p.get(16).split(",")));
                    ex.groupIds.removeIf(x -> x == null || x.trim().isEmpty());
                } catch (Exception ignored) {
                    ex.groupIds = new ArrayList<>();
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
                String groupList = String.join(",", ex.groupIds);

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
                                muscleList + ";" +
                                groupList +
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

    private Exercise addDemo(String id, String name, String... muscles) {
        Exercise ex = exercises.get(id);
        if (null == ex) ex = new Exercise(id, name);
        else ex.setName(name);
        if (id.startsWith("kh")) {
            ex.setSetsMin(2);
            ex.setSetsMax(5);
            ex.setRepsMin(8);
            ex.setRepsMax(50);
            ex.setRepsStep(2);
            ex.setWeightMin(5.0f);
            ex.setWeightMax(32.0f);
            ex.setWeightStep(2.5f);
        } else {
            ex.setSetsMin(2);
            ex.setSetsMax(5);
            ex.setRepsMin(8);
            ex.setRepsMax(50);
            ex.setRepsStep(2);
            ex.setWeightMin(2.0f);
            ex.setWeightMax(18.0f);
            ex.setWeightStep(2.0f);
        }

        for (String m : muscles) {
            if (!ex.muscleIds.contains(m)) {
                ex.muscleIds.add(m);
            }
        }

        exercises.put(id, ex);
        return ex;
    }

    private Exercise addDemo2(
            String id,
            String name,
            float weightMin,
            float weightMax,
            float weightStep,
            int setsMin,
            int setsMax,
            int repsMin,
            int repsMax,
            int repsStep,
            String... muscles
    ) {
        Exercise ex = exercises.get(id);
        if (null == ex) ex = new Exercise(id, name);
        else ex.setName(name);

        ex.setWeightMin(weightMin);
        ex.setWeightMax(weightMax);
        ex.setWeightStep(weightStep);

        ex.setSetsMin(setsMin);
        ex.setSetsMax(setsMax);

        ex.setRepsMin(repsMin);
        ex.setRepsMax(repsMax);
        ex.setRepsStep(repsStep);

        for (String m : muscles) {
            if (!ex.muscleIds.contains(m)) {
                ex.muscleIds.add(m);
            }
        }
        
        exercises.put(id, ex);
        return ex;
    }


    public void addDemoData() {

        addDemo("kh01", context.getString(R.string.exercise_kh01), "biceps", "shoulders");

        addDemo("kh03", context.getString(R.string.exercise_kh03), "biceps");

        addDemo("kh02", context.getString(R.string.exercise_kh02), "triceps", "shoulders", "core_stabilizers");

        addDemo("kh04", context.getString(R.string.exercise_kh04), "triceps", "shoulders", "core_stabilizers");

        addDemo("kh06", context.getString(R.string.exercise_kh06), "shoulders", "back_upper");

        addDemo("kh11", context.getString(R.string.exercise_kh11), "shoulders", "triceps", "core_stabilizers");

        addDemo("kh15", context.getString(R.string.exercise_kh15), "chest", "triceps", "shoulders");

        addDemo("kh16", context.getString(R.string.exercise_kh16), "chest", "shoulders");

        addDemo("kh13", context.getString(R.string.exercise_kh13), "back_upper", "biceps", "shoulders", "core_stabilizers");

        addDemo("kh36", context.getString(R.string.exercise_kh36), "back_upper", "biceps", "shoulders", "core_stabilizers");

        addDemo("kh40", context.getString(R.string.exercise_kh40), "hamstrings", "gluteus");

        addDemo("kh49", context.getString(R.string.exercise_kh49), "hamstrings", "gluteus", "back_lower", "core_stabilizers");

        addDemo("kh18", context.getString(R.string.exercise_kh18), "abs_straight", "core_stabilizers");

        addDemo("kh20", context.getString(R.string.exercise_kh20), "abs_straight", "quadriceps", "core_stabilizers");

        addDemo("kh19", context.getString(R.string.exercise_kh19), "abs_oblique", "core_rotators", "core_stabilizers");

        addDemo("kh30", context.getString(R.string.exercise_kh30), "core_rotators", "abs_oblique", "core_stabilizers");

        addDemo("kh24", context.getString(R.string.exercise_kh24), "core_stabilizers", "abs_straight", "shoulders");

        addDemo("kh28", context.getString(R.string.exercise_kh28), "core_stabilizers", "abs_oblique", "gluteus");

        addDemo("kh38", context.getString(R.string.exercise_kh38), "quadriceps", "gluteus", "core_stabilizers");

        addDemo("kh41", context.getString(R.string.exercise_kh41), "quadriceps", "gluteus", "core_stabilizers");

        addDemo("kh43", context.getString(R.string.exercise_kh43), "abductors", "core_stabilizers");

        addDemo("kh44", context.getString(R.string.exercise_kh44), "adductors", "core_stabilizers");

        addDemo("kh42", context.getString(R.string.exercise_kh42), "gluteus", "hamstrings", "core_stabilizers");

        addDemo("kh45", context.getString(R.string.exercise_kh45), "gluteus", "hamstrings", "core_stabilizers");

        addDemo("kb01", context.getString(R.string.exercise_kb01), "hamstrings", "gluteus", "back_lower", "core_stabilizers");

        addDemo("kb49", context.getString(R.string.exercise_kb49), "hamstrings", "gluteus", "back_lower", "core_stabilizers");

        addDemo("kb03", context.getString(R.string.exercise_kb03),"gluteus", "hamstrings", "core_stabilizers", "back_lower");

        addDemo("kb15", context.getString(R.string.exercise_kb15), "gluteus", "hamstrings", "shoulders", "core_stabilizers");

        addDemo("kb16", context.getString(R.string.exercise_kb16), "shoulders", "triceps", "core_stabilizers", "quadriceps");

        addDemo("kb47", context.getString(R.string.exercise_kb47), "shoulders", "quadriceps", "gluteus", "core_stabilizers", "triceps");

        addDemo("kb31", context.getString(R.string.exercise_kb31), "chest", "triceps", "shoulders", "core_stabilizers");

        addDemo("kb32", context.getString(R.string.exercise_kb32), "chest", "triceps", "shoulders");

        addDemo("kb33", context.getString(R.string.exercise_kb33), "back_upper", "biceps", "shoulders", "core_stabilizers");

        addDemo("kb35", context.getString(R.string.exercise_kb35), "back_upper", "biceps", "shoulders", "core_stabilizers", "abs_straight");

        addDemo("kb38", context.getString(R.string.exercise_kb38), "abs_straight", "shoulders", "core_stabilizers");

        addDemo("kb40", context.getString(R.string.exercise_kb40), "abs_straight", "core_stabilizers", "quadriceps");

        addDemo("kb39", context.getString(R.string.exercise_kb39), "abs_oblique", "core_rotators", "core_stabilizers");

        addDemo("kb30", context.getString(R.string.exercise_kb30), "core_rotators", "abs_oblique", "core_stabilizers");

        addDemo("kb12", context.getString(R.string.exercise_kb12), "core_stabilizers", "shoulders", "back_upper");

        addDemo("kb41", context.getString(R.string.exercise_kb41), "core_stabilizers", "abs_oblique", "gluteus");

        addDemo("kb48", context.getString(R.string.exercise_kb48), "quadriceps", "gluteus", "core_stabilizers", "shoulders");

        addDemo("kb50", context.getString(R.string.exercise_kb50), "quadriceps", "gluteus", "core_stabilizers");

        addDemo("kb42", context.getString(R.string.exercise_kb42), "abductors", "core_stabilizers", "abs_oblique");

        addDemo("kb46", context.getString(R.string.exercise_kb46), "adductors", "quadriceps", "gluteus", "core_stabilizers");

        addDemo2("cf1",  context.getString(R.string.exercise_cf01),
                10f, 120f, 10f,
                2, 5,
                8, 50, 2,
                "hamstrings", "gluteus");

        addDemo2("cf02", context.getString(R.string.exercise_cf02),
                10f, 120f, 10f,
                2, 5,
                8, 50, 2,
                "quadriceps");

        addDemo2("cf03", context.getString(R.string.exercise_cf03),
                10f, 120f, 10f,
                2, 5,
                8, 50, 2,
                "quadriceps", "gluteus", "hamstrings", "core_stabilizers", "back_lower");

        addDemo2("cf04", context.getString(R.string.exercise_cf04),
                10f, 120f, 10f,
                2, 5,
                8, 50, 2,
                "triceps", "shoulders");

        addDemo2("cf05", context.getString(R.string.exercise_cf05),
                10f, 120f, 10f,
                2, 5,
                8, 50, 2,
                "biceps");

        addDemo2("cf06", context.getString(R.string.exercise_cf06),
                10f, 120f, 10f,
                2, 5,
                8, 50, 2,
                "shoulders", "triceps");

        addDemo2("cf07", context.getString(R.string.exercise_cf07),
                10f, 120f, 10f,
                2, 5,
                8, 50, 2,
                "back_upper", "back_lower", "shoulders", "biceps");

        addDemo2("cf08", context.getString(R.string.exercise_cf08),
                10f, 120f, 10f,
                2, 5,
                8, 50, 2,
                "back_upper", "shoulders", "biceps");

        addDemo2("cf09", context.getString(R.string.exercise_cf09),
                10f, 120f, 10f,
                2, 5,
                8, 50, 2,
                "abs_straight");

        addDemo2("cf10", context.getString(R.string.exercise_cf10),
                10f, 120f, 10f,
                2, 5,
                8, 50, 2,
                "chest", "shoulders", "triceps");

        addDemo2("cf11", context.getString(R.string.exercise_cf11),
                5f, 145f, 2.5f,
                2, 5,
                8, 50, 2,
                "abductors");

        addDemo2("cf12", context.getString(R.string.exercise_cf12),
                5f, 145f, 2.5f,
                2, 5,
                8, 50, 2,
                "adductors");

        addDemo2("cf13", context.getString(R.string.exercise_cf13),
                15f, 195f, 2.5f,
                2, 5,
                8, 50, 2,
                "quadriceps", "gluteus", "hamstrings");

        addDemo2("cf14", context.getString(R.string.exercise_cf14),
                5f, 80f, 7.5f,
                2, 5,
                8, 50, 2,
                "hamstrings", "gluteus");

        addDemo2("cf15", context.getString(R.string.exercise_cf15),
                5f, 95f, 2.5f,
                2, 5,
                8, 50, 2,
                "gluteus");

        addDemo2("cf16", context.getString(R.string.exercise_cf16),
                5f, 75f, 2.5f,
                2, 5,
                8, 50, 2,
                "back_upper", "shoulders", "biceps", "triceps");

        addDemo2("cf17", context.getString(R.string.exercise_cf17),
                5f, 145f, 2.5f,
                2, 5,
                8, 50, 2,
                "shoulders", "back_upper");

        addDemo2("cf18", context.getString(R.string.exercise_cf18),
                5f, 130f, 2.5f,
                2, 5,
                8, 50, 2,
                "back_upper", "back_lower", "biceps", "shoulders");

        addDemo2("cf19", context.getString(R.string.exercise_cf19),
                5f, 95f, 2.5f,
                2, 5,
                8, 50, 2,
                "shoulders", "triceps");

        addDemo2("cf20", context.getString(R.string.exercise_cf20),
                5f, 95f, 2.5f,
                2, 5,
                8, 50, 2,
                "shoulders");

        addDemo2("cf21", context.getString(R.string.exercise_cf21),
                5f, 145f, 2.5f,
                2, 5,
                8, 50, 2,
                "back_upper", "back_lower", "biceps", "shoulders");

        addDemo2("cf22", context.getString(R.string.exercise_cf22),
                5f, 145f, 2.5f,
                2, 5,
                8, 50, 2,
                "back_upper", "shoulders", "biceps");

        addDemo2("cf23", context.getString(R.string.exercise_cf23),
                5f, 95f, 2.5f,
                2, 5,
                8, 50, 2,
                "core_rotators", "abs_oblique");

        addDemo2("cf24", context.getString(R.string.exercise_cf24),
                5f, 95f, 2.5f,
                2, 5,
                8, 50, 2,
                "biceps");

        addDemo2("cf25", context.getString(R.string.exercise_cf25),
                5f, 95f, 2.5f,
                2, 5,
                8, 50, 2,
                "quadriceps");

        addDemo2("cf26", context.getString(R.string.exercise_cf26),
                5f, 95f, 2.5f,
                2, 5,
                8, 50, 2,
                "hamstrings");

        addDemo2("cf27", context.getString(R.string.exercise_cf27),
                15f, 145f, 2.5f,
                2, 5,
                8, 50, 2,
                "back_lower", "gluteus");

        addDemo2("cf28", context.getString(R.string.exercise_cf28),
                5f, 95f, 2.5f,
                2, 5,
                8, 50, 2,
                "abs_straight");

        save();
    }

}
