package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.daniel_nowak.muscletraining.R;
import de.daniel_nowak.muscletraining.model.Exercise;
import de.daniel_nowak.muscletraining.model.Group;

public class GroupDatabase {

    private static final String FILE_NAME = "groups.db";
    private final File file;

    public Map<String, Group> groups = new HashMap<>();

    private final Context context;

    public GroupDatabase(Context context) {
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

    public void addDemoData(Database db) {

        // Gruppe: Kurzhantel
        Group gKH = groups.get("group_kh");
        if (null == gKH) gKH = new Group("group_kh",context.getString(R.string.group_ks));

        // Gruppe: Kettlebell
        Group gKB = groups.get("group_kb");
        if (null == gKB) gKB = new Group("group_kb",context.getString(R.string.group_kb));

        // Gruppe: Studio
        Group gCF = groups.get("group_cf");
        if (null == gCF) gCF = new Group("group_cf",context.getString(R.string.group_cf));

        // Alle Übungen durchgehen und zuordnen
        for (Exercise ex : db.exercises.exercises.values()) {

            String id = ex.getId().toLowerCase();

            if (id.startsWith("kh") && !gKH.exerciseIds.contains(ex.getId())) {
                gKH.exerciseIds.add(ex.getId());
            }

            if (id.startsWith("kb") && !gKB.exerciseIds.contains(ex.getId())) {
                gKB.exerciseIds.add(ex.getId());
            }

            if (id.startsWith("cf") && !gCF.exerciseIds.contains(ex.getId())) {
                gCF.exerciseIds.add(ex.getId());
            }
        }

        // Gruppen speichern
        groups.put(gKH.getId(), gKH);
        groups.put(gKB.getId(), gKB);
        groups.put(gCF.getId(), gCF);

        save();

        // Exercise.groupIds aktualisieren
        for (Exercise ex : db.exercises.exercises.values()) {

            String id = ex.getId().toLowerCase();

            if (id.startsWith("kh") && !ex.groupIds.contains("group_kh")) {
                ex.groupIds.add("group_kh");
            }

            if (id.startsWith("kb") && !ex.groupIds.contains("group_kb")) {
                ex.groupIds.add("group_kb");
            }

            if (id.startsWith("cf") && !ex.groupIds.contains("group_cf")) {
                ex.groupIds.add("group_cf");
            }
        }

        db.exercises.save();
    }


    public void load() {
        groups.clear();

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {

                List<String> p = parseLine(line);

                    Group g = new Group(p.get(0), unesc(p.get(1)));

                try {
                    if (p.size() > 2 && !p.get(2).isEmpty()) {
                        g.exerciseIds = new ArrayList<>(Arrays.asList(p.get(2).split(",")));
                    } else {
                        g.exerciseIds = new ArrayList<>();
                    }
                } catch (Exception ignored) {
                    g.exerciseIds = new ArrayList<>();
                }

                    groups.put(g.getId(), g);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {

            for (Group g : groups.values()) {

                String exerciseList = String.join(",", g.exerciseIds);

                bw.write(
                        g.getId() + ";" +
                                esc(g.getName()) + ";" +
                                exerciseList + ";"  +
                                "\n"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
