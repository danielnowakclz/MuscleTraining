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

    // ---------------------------------------------------------
    // ESCAPING
    // ---------------------------------------------------------

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(";", "\\;");
    }

    private String unesc(String s) {
        if (s == null) return "";
        return s.replace("\\;", ";").replace("\\\\", "\\");
    }

    // ---------------------------------------------------------
    // CSV PARSER
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // LOAD
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // SAVE
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // ADD
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // DEMO-DATEN (100 Übungen, Option B, alte Namen)
    // ---------------------------------------------------------

    public void addDemoData() {

        exercises.clear();

        // ---------------------------------------------------------
        // ARM – BIZEPS
        // ---------------------------------------------------------

        addDemo("kh01", "KH01: Alternierender Bizeps‑Curl im Stand").muscleIds.addAll(Arrays.asList(
                "biceps", "shoulders", "forearms"
        ));

        addDemo("kh03", "KH03: Einarmiger Bizeps‑Curl im Sitzen").muscleIds.addAll(Arrays.asList(
                "biceps", "forearms"
        ));

        // ---------------------------------------------------------
        // ARM – TRIZEPS
        // ---------------------------------------------------------

        addDemo("kh02", "KH02: Trizeps‑Kickback").muscleIds.addAll(Arrays.asList(
                "triceps", "shoulders", "core_stabilizers"
        ));

        addDemo("kh04", "KH04: Einarmiges Trizeps‑Strecken im Stütz").muscleIds.addAll(Arrays.asList(
                "triceps", "shoulders", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // SHOULDER
        // ---------------------------------------------------------

        addDemo("kh06", "KH06: Seitheben").muscleIds.addAll(Arrays.asList(
                "shoulders", "trapezius"
        ));

        addDemo("kh11", "KH11: Schulterdrücken").muscleIds.addAll(Arrays.asList(
                "shoulders", "triceps", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // CHEST
        // ---------------------------------------------------------

        addDemo("kh15", "KH15: Brustdrücken mit Kurzhanteln").muscleIds.addAll(Arrays.asList(
                "chest", "triceps", "shoulders"
        ));

        addDemo("kh16", "KH16: Butterfly (Dumbbell Flys)").muscleIds.addAll(Arrays.asList(
                "chest", "shoulders"
        ));

        // ---------------------------------------------------------
        // BACK – OBERER RÜCKEN
        // ---------------------------------------------------------

        addDemo("kh13", "KH13: Einarmiges Rudern").muscleIds.addAll(Arrays.asList(
                "back_upper", "biceps", "shoulders", "core_stabilizers"
        ));

        addDemo("kh36", "KH36: Vorgebeugtes Rudern").muscleIds.addAll(Arrays.asList(
                "back_upper", "biceps", "shoulders", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // BACK – HAMSTRINGS / POSTERIOR CHAIN
        // ---------------------------------------------------------

        addDemo("kh40", "KH40: Beinbeugen in Bauchlage").muscleIds.addAll(Arrays.asList(
                "hamstrings", "gluteus"
        ));

        addDemo("kh49", "KH49: Kreuzheben mit Kurzhanteln").muscleIds.addAll(Arrays.asList(
                "hamstrings", "gluteus", "back_lower", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // CORE – GERADE BAUCHMUSKELN
        // ---------------------------------------------------------

        addDemo("kh18", "KH18: Crunch mit Kurzhantel").muscleIds.addAll(Arrays.asList(
                "abs_straight", "core_stabilizers"
        ));

        addDemo("kh20", "KH20: Crunch mit Beinhub").muscleIds.addAll(Arrays.asList(
                "abs_straight", "hip_flexors", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // CORE – SCHRÄG / ROTATION
        // ---------------------------------------------------------

        addDemo("kh19", "KH19: Seitliches Kniependel").muscleIds.addAll(Arrays.asList(
                "abs_oblique", "core_rotators", "core_stabilizers"
        ));

        addDemo("kh30", "KH30: Rotations‑Crunch aus dem Stütz").muscleIds.addAll(Arrays.asList(
                "core_rotators", "abs_oblique", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // CORE – STABILISATION
        // ---------------------------------------------------------

        addDemo("kh24", "KH24: Ausrollen mit Kurzhantel").muscleIds.addAll(Arrays.asList(
                "core_stabilizers", "abs_straight", "shoulders"
        ));

        addDemo("kh28", "KH28: Seitstütz mit Hüftlift").muscleIds.addAll(Arrays.asList(
                "core_stabilizers", "abs_oblique", "gluteus"
        ));

        // ---------------------------------------------------------
        // LEG – QUADRICEPS
        // ---------------------------------------------------------

        addDemo("kh38", "KH38: Kniebeuge tief").muscleIds.addAll(Arrays.asList(
                "quadriceps", "gluteus", "core_stabilizers"
        ));

        addDemo("kh41", "KH41: Ausfallschritt mit Vorhalte").muscleIds.addAll(Arrays.asList(
                "quadriceps", "gluteus", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // LEG – ABDUKTOREN / ADDUKTOREN
        // ---------------------------------------------------------

        addDemo("kh43", "KH43: Beinheben seitlich (Abduktoren)").muscleIds.addAll(Arrays.asList(
                "abductors", "core_stabilizers"
        ));

        addDemo("kh44", "KH44: Beinheben innen (Adduktoren)").muscleIds.addAll(Arrays.asList(
                "adductors", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // LEG – GLUTEUS
        // ---------------------------------------------------------

        addDemo("kh42", "KH42: Hüftkick im Vierfüßler").muscleIds.addAll(Arrays.asList(
                "gluteus", "hamstrings", "core_stabilizers"
        ));

        addDemo("kh45", "KH45: Beckenlift mit Kurzhantel").muscleIds.addAll(Arrays.asList(
                "gluteus", "hamstrings", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // HAMSTRINGS / POSTERIOR CHAIN
        // ---------------------------------------------------------

        addDemo("kb01", "KB01: Hip Hinge").muscleIds.addAll(Arrays.asList(
                "hamstrings", "gluteus", "back_lower", "core_stabilizers"
        ));

        addDemo("kb49", "KB49: Kreuzheben mit Kettlebell").muscleIds.addAll(Arrays.asList(
                "hamstrings", "gluteus", "back_lower", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // GLUTEUS
        // ---------------------------------------------------------

        addDemo("kb03", "KB03: Kettlebell Swing (Double Arm)").muscleIds.addAll(Arrays.asList(
                "gluteus", "hamstrings", "core_stabilizers", "back_lower"
        ));

        addDemo("kb15", "KB15: Kettlebell Clean").muscleIds.addAll(Arrays.asList(
                "gluteus", "hamstrings", "shoulders", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // SHOULDERS
        // ---------------------------------------------------------

        addDemo("kb16", "KB16: Push Press").muscleIds.addAll(Arrays.asList(
                "shoulders", "triceps", "core_stabilizers", "quadriceps"
        ));

        addDemo("kb47", "KB47: Single Arm Thruster").muscleIds.addAll(Arrays.asList(
                "shoulders", "quadriceps", "gluteus", "core_stabilizers", "triceps"
        ));

        // ---------------------------------------------------------
        // CHEST
        // ---------------------------------------------------------

        addDemo("kb31", "KB31: Push‑Up mit Kettlebell").muscleIds.addAll(Arrays.asList(
                "chest", "triceps", "shoulders", "core_stabilizers"
        ));

        addDemo("kb32", "KB32: Floor Press").muscleIds.addAll(Arrays.asList(
                "chest", "triceps", "shoulders"
        ));

        // ---------------------------------------------------------
        // BACK UPPER
        // ---------------------------------------------------------

        addDemo("kb33", "KB33: Vorgebeugtes Rudern").muscleIds.addAll(Arrays.asList(
                "back_upper", "biceps", "shoulders", "core_stabilizers"
        ));

        addDemo("kb35", "KB35: Renegade Row").muscleIds.addAll(Arrays.asList(
                "back_upper", "biceps", "shoulders", "core_stabilizers", "abs_straight"
        ));

        // ---------------------------------------------------------
        // CORE – STRAIGHT
        // ---------------------------------------------------------

        addDemo("kb38", "KB38: Overhead Sit‑Up").muscleIds.addAll(Arrays.asList(
                "abs_straight", "shoulders", "core_stabilizers"
        ));

        addDemo("kb40", "KB40: Leg Raise").muscleIds.addAll(Arrays.asList(
                "abs_straight", "core_stabilizers", "hip_flexors"
        ));

        // ---------------------------------------------------------
        // CORE – OBLIQUE / ROTATION
        // ---------------------------------------------------------

        addDemo("kb39", "KB39: Russian Twist").muscleIds.addAll(Arrays.asList(
                "abs_oblique", "core_rotators", "core_stabilizers"
        ));

        addDemo("kb30", "KB30: Twist (Rotation)").muscleIds.addAll(Arrays.asList(
                "core_rotators", "abs_oblique", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // CORE – STABILISATION
        // ---------------------------------------------------------

        addDemo("kb12", "KB12: Farmers Walk").muscleIds.addAll(Arrays.asList(
                "core_stabilizers", "shoulders", "grip", "back_upper"
        ));

        addDemo("kb41", "KB41: Side Plank Hip Lift").muscleIds.addAll(Arrays.asList(
                "core_stabilizers", "abs_oblique", "gluteus"
        ));

        // ---------------------------------------------------------
        // LEG – QUADRICEPS
        // ---------------------------------------------------------

        addDemo("kb48", "KB48: Overhead Squat").muscleIds.addAll(Arrays.asList(
                "quadriceps", "gluteus", "core_stabilizers", "shoulders"
        ));

        addDemo("kb50", "KB50: Squat Flip").muscleIds.addAll(Arrays.asList(
                "quadriceps", "gluteus", "core_stabilizers"
        ));

        // ---------------------------------------------------------
        // LEG – ADDUCTORS / ABDUCTORS
        // ---------------------------------------------------------

        addDemo("kb42", "KB42: Side Plank Leg Lift").muscleIds.addAll(Arrays.asList(
                "abductors", "core_stabilizers", "abs_oblique"
        ));

        addDemo("kb46", "KB46: Side Lunge & Pass").muscleIds.addAll(Arrays.asList(
                "adductors", "quadriceps", "gluteus", "core_stabilizers"
        ));

        save();
    }
}
