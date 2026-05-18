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
    // ROBUSTER CSV-PARSER
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

    private List<Float> parseFloatList(String s) {
        List<Float> out = new ArrayList<>();
        if (s.isEmpty()) return out;
        for (String part : s.split(",")) {
            try {
                out.add(Float.parseFloat(part));
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private String joinFloatList(List<Float> list) {
        List<String> out = new ArrayList<>();
        for (Float f : list) out.add(String.valueOf(f));
        return String.join(",", out);
    }

    // ---------------------------------------------------------
    // LOAD
    // ---------------------------------------------------------

    public void load() {
        muscles.clear();

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {

                List<String> p = parseLine(line);


                    Muscle m = new Muscle(p.get(0), unesc(p.get(1)));

                    try {
                        m.setLastTraining(Long.parseLong(p.get(2)));
                    } catch (Exception ignored) {
                    }
                    try {
                        m.setLastSets(Integer.parseInt(p.get(3)));
                    } catch (Exception ignored) {
                    }
                    try {
                        m.setLastReps(Integer.parseInt(p.get(4)));
                    } catch (Exception ignored) {
                    }
                    try {
                        m.setLastWeight(Float.parseFloat(p.get(5)));
                    } catch (Exception ignored) {
                    }
                   try {
                            m.exerciseIds = new ArrayList<>(Arrays.asList(p.get(6).split(",")));
                    } catch (Exception ignored) {
                    }
                    try {
                            m.posXList = parseFloatList(p.get(7));
                    } catch (Exception ignored) {
                    }
                    try {
                            m.posYList = parseFloatList(p.get(8));
                    } catch (Exception ignored) {
                    }
                    try {
                            m.sideList = new ArrayList<>(Arrays.asList(p.get(9).split(",")));
                    } catch (Exception ignored) {
                    }
                    try {
                            m.category = Muscle.Category.valueOf(p.get(10));
                    } catch (Exception ignored) {
                    }
                    muscles.put(m.getId(), m);

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

            for (Muscle m : muscles.values()) {

                String exerciseList = String.join(",", m.exerciseIds);
                String posXList = joinFloatList(m.posXList);
                String posYList = joinFloatList(m.posYList);
                String sideList = String.join(",", m.sideList);

                bw.write(
                        m.getId() + ";" +
                                esc(m.getName()) + ";" +
                                m.getLastTraining() + ";" +
                                m.getLastSets() + ";" +
                                m.getLastReps() + ";" +
                                m.getLastWeight() + ";" +
                                exerciseList + ";" +
                                posXList + ";" +
                                posYList + ";" +
                                sideList + ";" +
                                m.category.name() +
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

    public Muscle add(String id, String name, Muscle.Category category) {
        Muscle m = new Muscle(id, name);
        m.category = category;
        muscles.put(id, m);
        save();
        return m;
    }

    public Muscle addDemo(String id, String name, Muscle.Category category) {
        Muscle m = new Muscle(id, name);
        m.category = category;
        muscles.put(id, m);
        return m;
    }

    // ---------------------------------------------------------
    // DEMO-DATEN (final, grobe Kategorien)
    // ---------------------------------------------------------

    public void addDemoData() {

        // ---------------------------------------------------------
        // SHOULDERS – FRONT
        // ---------------------------------------------------------
        Muscle shoulders = addDemo("shoulders", "Schultern", Muscle.Category.SHOULDER);
        shoulders.sideList.add("front"); shoulders.posXList.add(0.23f); shoulders.posYList.add(0.18f);
        shoulders.sideList.add("front"); shoulders.posXList.add(0.32f); shoulders.posYList.add(0.22f);
        shoulders.sideList.add("front"); shoulders.posXList.add(0.77f); shoulders.posYList.add(0.18f);
        shoulders.sideList.add("front"); shoulders.posXList.add(0.68f); shoulders.posYList.add(0.22f);

        // ---------------------------------------------------------
        // CHEST – FRONT
        // ---------------------------------------------------------
        Muscle chest = addDemo("chest", "Brust", Muscle.Category.CHEST);
        chest.sideList.add("front"); chest.posXList.add(0.38f); chest.posYList.add(0.27f);
        chest.sideList.add("front"); chest.posXList.add(0.62f); chest.posYList.add(0.27f);

        // ---------------------------------------------------------
        // BICEPS – FRONT
        // ---------------------------------------------------------
        Muscle biceps = addDemo("biceps", "Bizeps", Muscle.Category.ARM);
        biceps.sideList.add("front"); biceps.posXList.add(0.26f); biceps.posYList.add(0.32f);
        biceps.sideList.add("front"); biceps.posXList.add(0.74f); biceps.posYList.add(0.32f);

        // ---------------------------------------------------------
        // ABS_STRAIGHT – FRONT
        // ---------------------------------------------------------
        Muscle abs_straight = addDemo("abs_straight", "Gerade Bauchmuskeln", Muscle.Category.CORE);
        abs_straight.sideList.add("front"); abs_straight.posXList.add(0.50f); abs_straight.posYList.add(0.39f);
        abs_straight.sideList.add("front"); abs_straight.posXList.add(0.50f); abs_straight.posYList.add(0.44f);

        // ---------------------------------------------------------
        // ABS_OBLIQUE – FRONT
        // ---------------------------------------------------------
        Muscle abs_oblique = addDemo("abs_oblique", "Schräge Bauchmuskeln", Muscle.Category.CORE);
        abs_oblique.sideList.add("front"); abs_oblique.posXList.add(0.36f); abs_oblique.posYList.add(0.44f);
        abs_oblique.sideList.add("front"); abs_oblique.posXList.add(0.36f); abs_oblique.posYList.add(0.49f);
        abs_oblique.sideList.add("front"); abs_oblique.posXList.add(0.64f); abs_oblique.posYList.add(0.44f);
        abs_oblique.sideList.add("front"); abs_oblique.posXList.add(0.64f); abs_oblique.posYList.add(0.49f);

        // ---------------------------------------------------------
        // CORE_ROTATORS – FRONT
        // ---------------------------------------------------------
        Muscle core_rotators = addDemo("core_rotators", "Rumpfrotatoren", Muscle.Category.CORE);
        core_rotators.sideList.add("front"); core_rotators.posXList.add(0.44f); core_rotators.posYList.add(0.48f);
        core_rotators.sideList.add("front"); core_rotators.posXList.add(0.56f); core_rotators.posYList.add(0.48f);

        // ---------------------------------------------------------
        // CORE_STABILIZERS – FRONT
        // ---------------------------------------------------------
        Muscle core_stabilizers = addDemo("core_stabilizers", "Rumpfstabilisatoren", Muscle.Category.CORE);
        core_stabilizers.sideList.add("front"); core_stabilizers.posXList.add(0.47f); core_stabilizers.posYList.add(0.52f);
        core_stabilizers.sideList.add("front"); core_stabilizers.posXList.add(0.53f); core_stabilizers.posYList.add(0.52f);

        // ---------------------------------------------------------
        // ADDUCTORS – FRONT
        // ---------------------------------------------------------
        Muscle adductors = addDemo("adductors", "Adduktoren", Muscle.Category.LEG);
        adductors.sideList.add("front"); adductors.posXList.add(0.47f); adductors.posYList.add(0.64f);
        adductors.sideList.add("front"); adductors.posXList.add(0.53f); adductors.posYList.add(0.64f);

        // ---------------------------------------------------------
        // ABDUCTORS – FRONT
        // ---------------------------------------------------------
        Muscle abductors = addDemo("abductors", "Abduktoren", Muscle.Category.LEG);
        abductors.sideList.add("front"); abductors.posXList.add(0.38f); abductors.posYList.add(0.65f);
        abductors.sideList.add("front"); abductors.posXList.add(0.62f); abductors.posYList.add(0.65f);

        // ---------------------------------------------------------
        // QUADRICEPS – FRONT
        // ---------------------------------------------------------
        Muscle quadriceps = addDemo("quadriceps", "Quadrizeps", Muscle.Category.LEG);
        quadriceps.sideList.add("front"); quadriceps.posXList.add(0.44f); quadriceps.posYList.add(0.69f);
        quadriceps.sideList.add("front"); quadriceps.posXList.add(0.56f); quadriceps.posYList.add(0.69f);

        // ---------------------------------------------------------
        // NECK – BACK
        // ---------------------------------------------------------
        Muscle neck = addDemo("neck", "Nacken", Muscle.Category.BACK);
        neck.sideList.add("back"); neck.posXList.add(0.48f); neck.posYList.add(0.17f);
        neck.sideList.add("back"); neck.posXList.add(0.57f); neck.posYList.add(0.17f);

        // ---------------------------------------------------------
        // BACK_UPPER – BACK
        // ---------------------------------------------------------
        Muscle back_upper = addDemo("back_upper", "Oberer Rücken", Muscle.Category.BACK);
        back_upper.sideList.add("back"); back_upper.posXList.add(0.40f); back_upper.posYList.add(0.25f);
        back_upper.sideList.add("back"); back_upper.posXList.add(0.48f); back_upper.posYList.add(0.29f);
        back_upper.sideList.add("back"); back_upper.posXList.add(0.70f); back_upper.posYList.add(0.25f);
        back_upper.sideList.add("back"); back_upper.posXList.add(0.62f); back_upper.posYList.add(0.29f);

        // ---------------------------------------------------------
        // BACK_LOWER – BACK
        // ---------------------------------------------------------
        Muscle back_lower = addDemo("back_lower", "Unterer Rücken", Muscle.Category.BACK);
        back_lower.sideList.add("back"); back_lower.posXList.add(0.48f); back_lower.posYList.add(0.47f);
        back_lower.sideList.add("back"); back_lower.posXList.add(0.57f); back_lower.posYList.add(0.47f);

        // ---------------------------------------------------------
        // TRICEPS – BACK
        // ---------------------------------------------------------
        Muscle triceps = addDemo("triceps", "Trizeps", Muscle.Category.ARM);
        triceps.sideList.add("back"); triceps.posXList.add(0.40f); triceps.posYList.add(0.33f);
        triceps.sideList.add("back"); triceps.posXList.add(0.70f); triceps.posYList.add(0.33f);

        // ---------------------------------------------------------
        // GLUTEUS – BACK
        // ---------------------------------------------------------
        Muscle gluteus = addDemo("gluteus", "Gluteus", Muscle.Category.LEG);
        gluteus.sideList.add("back"); gluteus.posXList.add(0.48f); gluteus.posYList.add(0.56f);
        gluteus.sideList.add("back"); gluteus.posXList.add(0.62f); gluteus.posYList.add(0.56f);

        // ---------------------------------------------------------
        // HAMSTRINGS – BACK
        // ---------------------------------------------------------
        Muscle hamstrings = addDemo("hamstrings", "Hamstrings", Muscle.Category.LEG);
        hamstrings.sideList.add("back"); hamstrings.posXList.add(0.48f); hamstrings.posYList.add(0.67f);
        hamstrings.sideList.add("back"); hamstrings.posXList.add(0.62f); hamstrings.posYList.add(0.67f);

        save();

    }


}
