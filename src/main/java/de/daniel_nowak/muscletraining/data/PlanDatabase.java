package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.LinkedHashSet;

public class PlanDatabase {

    private static final String FILE_NAME = "plan.db";
    private final File file;

    // Reihenfolge bleibt erhalten, keine Duplikate
    public LinkedHashSet<String> plan = new LinkedHashSet<>();

    // NEU: Zeitstempel der letzten Planerstellung
    public long lastPlanTime = 0L;

    public PlanDatabase(Context context) {
        file = new File(context.getFilesDir(), FILE_NAME);
    }

    public void load() {
        plan.clear();
        lastPlanTime = 0L;

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                // NEU: Zeitstempel-Zeile erkennen
                if (line.startsWith("lastPlanTime=")) {
                    try {
                        lastPlanTime = Long.parseLong(
                                line.substring("lastPlanTime=".length())
                        );
                    } catch (Exception ignored) {}
                    continue;
                }

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

            // NEU: Zeitstempel zuerst schreiben
            bw.write("lastPlanTime=" + lastPlanTime + "\n");

            // Bestehendes Verhalten: IDs schreiben
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
        // NEU: Zeitstempel hier setzen
        lastPlanTime = System.currentTimeMillis();
        save();
    }

    public void clear() {
        plan.clear();
        lastPlanTime = 0L;
        save();
    }
}
