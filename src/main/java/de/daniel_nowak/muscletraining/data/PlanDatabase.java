package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.io.*;
import java.util.*;

public class PlanDatabase {

    private static final String FILE_NAME = "plan.db";
    private final File file;

    // Reihenfolge bleibt erhalten, keine Duplikate
    public LinkedHashSet<String> plan = new LinkedHashSet<>();

    public PlanDatabase(Context context) {
        file = new File(context.getFilesDir(), FILE_NAME);
    }

    public void load() {
        plan.clear();
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    plan.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String id : plan) {
                bw.write(id + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPlan(Collection<String> ids) {
        plan.clear();
        plan.addAll(ids);
        save();
    }

    public void clear() {
        plan.clear();
        save();
    }
}
