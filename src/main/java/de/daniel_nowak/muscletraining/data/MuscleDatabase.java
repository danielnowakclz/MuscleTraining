package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.io.*;
import java.util.*;

import de.daniel_nowak.muscletraining.R;
import de.daniel_nowak.muscletraining.model.Muscle;

public class MuscleDatabase {

    private static final String FILE_NAME = "muscles.db";
    private final File file;

    public Map<String, Muscle> muscles = new HashMap<>();

    private final Context context;

    public MuscleDatabase(Context context) {
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

    public Muscle add(String id, String name, Muscle.Category category) {
        Muscle m = new Muscle(id, name);
        m.category = category;
        muscles.put(id, m);
        save();
        return m;
    }

    private Muscle addDemo(String id, String name, Muscle.Category category) {
        Muscle m = muscles.get(id);
        if (null == m) m = new Muscle(id, name);
        else m.setName(name);
        m.category = category;
        muscles.put(id, m);
        return m;
    }

    private void addIfMissing(Muscle m, String side, float x, float y) {
        for (int i = 0; i < m.sideList.size(); i++) {
            if (m.sideList.get(i).equals(side)
                    && m.posXList.get(i) == x
                    && m.posYList.get(i) == y) {
                return; // existiert bereits → nichts tun
            }
        }

        // fehlt → hinzufügen
        m.sideList.add(side);
        m.posXList.add(x);
        m.posYList.add(y);
    }


    public void addDemoData() {

        Muscle shoulders = addDemo("shoulders",
                context.getString(R.string.muscle_shoulders),
                Muscle.Category.SHOULDER);

        Muscle chest = addDemo("chest",
                context.getString(R.string.muscle_chest),
                Muscle.Category.CHEST);

        Muscle biceps = addDemo("biceps",
                context.getString(R.string.muscle_biceps),
                Muscle.Category.ARM);

        Muscle abs_straight = addDemo("abs_straight",
                context.getString(R.string.muscle_abs_straight),
                Muscle.Category.CORE);

        Muscle abs_oblique = addDemo("abs_oblique",
                context.getString(R.string.muscle_abs_oblique),
                Muscle.Category.CORE);

        Muscle core_rotators = addDemo("core_rotators",
                context.getString(R.string.muscle_core_rotators),
                Muscle.Category.CORE);

        Muscle core_stabilizers = addDemo("core_stabilizers",
                context.getString(R.string.muscle_core_stabilizers),
                Muscle.Category.CORE);

        Muscle adductors = addDemo("adductors",
                context.getString(R.string.muscle_adductors),
                Muscle.Category.LEG);

        Muscle abductors = addDemo("abductors",
                context.getString(R.string.muscle_abductors),
                Muscle.Category.LEG);

        Muscle quadriceps = addDemo("quadriceps",
                context.getString(R.string.muscle_quadriceps),
                Muscle.Category.LEG);

        Muscle neck = addDemo("neck",
                context.getString(R.string.muscle_neck),
                Muscle.Category.BACK);

        Muscle back_upper = addDemo("back_upper",
                context.getString(R.string.muscle_back_upper),
                Muscle.Category.BACK);

        Muscle back_lower = addDemo("back_lower",
                context.getString(R.string.muscle_back_lower),
                Muscle.Category.BACK);

        Muscle triceps = addDemo("triceps",
                context.getString(R.string.muscle_triceps),
                Muscle.Category.ARM);

        Muscle gluteus = addDemo("gluteus",
                context.getString(R.string.muscle_gluteus),
                Muscle.Category.LEG);

        Muscle hamstrings = addDemo("hamstrings",
                context.getString(R.string.muscle_hamstrings),
                Muscle.Category.LEG);

        addIfMissing(shoulders, "front", 0.25287357f, 0.19692308f);
        addIfMissing(shoulders, "back",  0.29655173f, 0.20923077f);
        addIfMissing(shoulders, "front", 0.74482757f, 0.19692308f);
        addIfMissing(shoulders, "back",  0.7011494f,  0.20923077f);

        addIfMissing(abs_oblique, "front", 0.3586207f, 0.40923077f);
        addIfMissing(abs_oblique, "front", 0.3586207f, 0.45846155f);
        addIfMissing(abs_oblique, "front", 0.63908046f, 0.40923077f);
        addIfMissing(abs_oblique, "front", 0.63908046f, 0.45846155f);

        addIfMissing(triceps, "back", 0.21609196f, 0.29846153f);
        addIfMissing(triceps, "back", 0.7816092f,  0.29846153f);

        addIfMissing(chest, "front", 0.37931034f, 0.2697436f);
        addIfMissing(chest, "front", 0.6183908f,  0.2697436f);

        addIfMissing(core_rotators, "front", 0.46206897f, 0.44923076f);
        addIfMissing(core_rotators, "front", 0.5356322f,  0.44923076f);

        addIfMissing(adductors, "front", 0.4597701f,  0.58358973f);
        addIfMissing(adductors, "front", 0.537931f,   0.58358973f);

        addIfMissing(gluteus, "back", 0.4091954f, 0.5292308f);
        addIfMissing(gluteus, "back", 0.58850574f, 0.5292308f);

        addIfMissing(neck, "back", 0.4091954f, 0.16923077f);
        addIfMissing(neck, "back", 0.58850574f, 0.16923077f);

        addIfMissing(abductors, "front", 0.32413793f, 0.5774359f);
        addIfMissing(abductors, "front", 0.67356324f, 0.5774359f);

        addIfMissing(abs_straight, "front", 0.5f, 0.39f);
        addIfMissing(abs_straight, "front", 0.5f, 0.44f);

        addIfMissing(quadriceps, "front", 0.39310345f, 0.57641023f);
        addIfMissing(quadriceps, "front", 0.6045977f,  0.57641023f);

        addIfMissing(back_lower, "back", 0.43218392f, 0.45948717f);
        addIfMissing(back_lower, "back", 0.56551725f, 0.45948717f);

        addIfMissing(hamstrings, "back", 0.3632184f,  0.60820514f);
        addIfMissing(hamstrings, "back", 0.63448274f, 0.60820514f);

        addIfMissing(back_upper, "back", 0.445977f,    0.28f);
        addIfMissing(back_upper, "back", 0.55172414f,  0.28f);

        addIfMissing(core_stabilizers, "front", 0.46896553f, 0.4471795f);
        addIfMissing(core_stabilizers, "front", 0.52873564f, 0.4471795f);

        addIfMissing(biceps, "front", 0.23678161f, 0.30974358f);
        addIfMissing(biceps, "front", 0.7609195f,  0.30974358f);

        save();
    }



}
