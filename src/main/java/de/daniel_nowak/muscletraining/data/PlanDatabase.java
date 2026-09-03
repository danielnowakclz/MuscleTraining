package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import de.daniel_nowak.muscletraining.model.Exercise;

public class PlanDatabase {

    private static final String FILE_NAME = "plan.db";
    private final File file;

    public LinkedHashSet<String> plan = new LinkedHashSet<>();
    public long lastPlanTime = 0L;

    // NEU: gespeicherte Gruppen
    public List<String> selectedGroupIds = new ArrayList<>();

    public PlanDatabase(Context context) {
        file = new File(context.getFilesDir(), FILE_NAME);
    }

    public void load() {
        plan.clear();
        selectedGroupIds.clear();
        lastPlanTime = 0L;

        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("lastPlanTime=")) {
                    try {
                        lastPlanTime = Long.parseLong(
                                line.substring("lastPlanTime=".length())
                        );
                    } catch (Exception ignored) {}
                    continue;
                }

                if (line.startsWith("groups=")) {
                    String raw = line.substring("groups=".length());
                    if (!raw.isEmpty()) {
                        selectedGroupIds = new ArrayList<>(Arrays.asList(raw.split(",")));
                    }
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

            bw.write("lastPlanTime=" + lastPlanTime + "\n");

            // NEU: Gruppen speichern
            bw.write("groups=" + String.join(",", selectedGroupIds) + "\n");

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
        lastPlanTime = System.currentTimeMillis();
        save();
    }

    public void clear() {
        plan.clear();
        selectedGroupIds.clear(); // NEU
        lastPlanTime = 0L;
        save();
    }

    public void remove(Exercise ex) {
        plan.remove(ex.getId());
        save();
    }
}
