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

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(";", "\\;");
    }

    private String unesc(String s) {
        if (s == null) return "";
        return s.replace("\\;", ";").replace("\\\\", "\\");
    }

    public void load() {
        muscles.clear();

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {

                List<String> parts = parseLine(line);

                if (parts.size() >= 2) {
                    Muscle m = new Muscle(parts.get(0), unesc(parts.get(1)));

                    if (parts.size() == 3 && !parts.get(2).isEmpty()) {
                        m.exerciseIds = new ArrayList<>(Arrays.asList(parts.get(2).split(",")));
                    }

                    muscles.put(m.getId(), m);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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


    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {

            for (Muscle m : muscles.values()) {

                String exerciseList = String.join(",", m.exerciseIds);

                bw.write(
                        m.getId() + ";" +
                                esc(m.getName()) + ";" +
                                exerciseList +
                                "\n"
                );
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

    // ---------------------------------------------------------
    // DEMO-DATEN
    // ---------------------------------------------------------

    public void addDemoData() {

        add("pectoralis_major", "Brust (Pectoralis Major)");
        add("anterior_deltoid", "Vordere Schulter");
        add("triceps", "Trizeps");

        add("latissimus", "Latissimus");
        add("upper_back", "Oberer Rücken / Rhomboiden");
        add("posterior_deltoid", "Hintere Schulter");
        add("biceps", "Bizeps");

        add("quadriceps", "Quadrizeps (Vorderer Oberschenkel)");
        add("hamstrings", "Hamstrings (Hinterer Oberschenkel)");
        add("gluteus", "Gluteus / Po");
        add("calves", "Waden");

        add("core", "Rumpf / Bauch");
    }
}
