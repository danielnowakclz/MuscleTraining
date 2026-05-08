package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.daniel_nowak.muscletraining.model.Exercise;
import de.daniel_nowak.muscletraining.model.Muscle;
import de.daniel_nowak.muscletraining.model.Training;

public class Database {

    public final MuscleDatabase muscles;
    public final ExerciseDatabase exercises;
    public final TrainingDatabase trainings;
    public final PlanDatabase plan;

    public Database(Context context) {
        muscles = new MuscleDatabase(context);
        exercises = new ExerciseDatabase(context);
        trainings = new TrainingDatabase(context);
        plan = new PlanDatabase(context);

        muscles.load();
        exercises.load();
        trainings.load();
        plan.load();
    }

    // ---------------------------------------------------------
    // ADD (mit Muskel-IDs)
    // ---------------------------------------------------------

    public Training addTraining(Exercise ex, int sets, int reps, float weight) {

        String id = UUID.randomUUID().toString();
        long time = System.currentTimeMillis();

        Training t = new Training(id, time, sets, reps, weight, ex.getId());

        // NEU: trainierte Muskeln speichern
        t.muscleIds.clear();
        t.muscleIds.addAll(ex.muscleIds);

        trainings.trainings.put(id, t);
        trainings.save();

        ex.setLastTraining(time);
        ex.setLastSets(sets);
        ex.setLastReps(reps);
        ex.setLastWeight(weight);
        exercises.save();

        for (String mId:ex.muscleIds) {
            Muscle m = muscles.muscles.get(mId);
            if (null!=m) {
            m.setLastTraining(time);
            m.setLastSets(sets);
            m.setLastReps(reps);
            m.setLastWeight(weight);
            }
        }
        muscles.save();

        return t;
    }

    public float getMaxVolumeForCategory(Muscle.Category cat) {

        float max = 0f;

        for (Training t : trainings.trainings.values()) {

            Exercise ex = exercises.exercises.get(t.getExerciseId());
            if (ex == null) continue;

            // Kategorie der Übung bestimmen
            Set<Muscle.Category> cats = ex.muscleIds.stream()
                    .map(id -> muscles.muscles.get(id))
                    .filter(Objects::nonNull)
                    .map(m -> m.category)
                    .collect(Collectors.toSet());

            if (!cats.contains(cat)) continue;

            float vol = t.getSets() * t.getReps() * t.getWeight();
            if (vol > max) max = vol;
        }

        return max;
    }


    // ---------------------------------------------------------
    // DEMO-DATEN
    // ---------------------------------------------------------

    public void addDemoData() {
        muscles.addDemoData();
        exercises.addDemoData();
        syncAllRelations();
    }

    public float calculateRegeneration(String muscleId) {

        Muscle m = muscles.muscles.get(muscleId);
        if (m == null) return 1.0f;

        long last = m.getLastTraining();

        // nie trainiert → vollständig regeneriert
        if (last == 0L) return 1.0f;

        long now = System.currentTimeMillis();
        float hours = (now - last) / 1000f / 3600f;

        float regenHours = m.getRegenerationHours();
        if (regenHours <= 0f) return 1.0f;

        float regen = hours / regenHours;

        if (regen > 1f) regen = 1f;
        if (regen < 0f) regen = 0f;

        return regen;
    }

    public void syncAllRelations() {

        // 1. Muskel->Übung Listen leeren
        for (Muscle m : muscles.muscles.values()) {
            m.exerciseIds.clear();
        }

        // 2. Ungültige muscleIds aus Übungen entfernen
        for (Exercise ex : exercises.exercises.values()) {
            ex.muscleIds.removeIf(id -> !muscles.muscles.containsKey(id));
        }

        // 3. Beziehungen beidseitig setzen
        for (Exercise ex : exercises.exercises.values()) {
            for (String mId : ex.muscleIds) {
                Muscle m = muscles.muscles.get(mId);
                if (m != null && !m.exerciseIds.contains(ex.getId())) {
                    m.exerciseIds.add(ex.getId());
                }
            }
        }

        muscles.save();
        exercises.save();
    }


    // ---------------------------------------------------------
    // DELETE EXERCISE
    // ---------------------------------------------------------

    public void delExercise(String id) {

        exercises.exercises.remove(id);

        for (Muscle m : muscles.muscles.values()) {
            m.exerciseIds.remove(id);
        }

        trainings.markExerciseDeleted(id);

        exercises.save();
        muscles.save();
        trainings.save();
    }

    // ---------------------------------------------------------
    // DELETE MUSCLE
    // ---------------------------------------------------------

    public void delMuscle(String id) {

        muscles.muscles.remove(id);

        for (Exercise ex : exercises.exercises.values()) {
            ex.muscleIds.remove(id);
        }

        trainings.markMuscleDeleted(id);

        muscles.save();
        exercises.save();
        trainings.save();
    }


    // ---------------------------------------------------------
    // SYNC EXERCISE
    // ---------------------------------------------------------

    public void syncExercise(Exercise ex) {

        for (Muscle m : muscles.muscles.values()) {
            m.exerciseIds.remove(ex.getId());
        }

        for (String muscleId : ex.muscleIds) {
            Muscle m = muscles.muscles.get(muscleId);
            if (m != null && !m.exerciseIds.contains(ex.getId())) {
                m.exerciseIds.add(ex.getId());
            }
        }

        exercises.save();
        muscles.save();
    }

    // ---------------------------------------------------------
    // SYNC MUSCLE
    // ---------------------------------------------------------

    public void syncMuscle(Muscle m) {

        for (Exercise ex : exercises.exercises.values()) {
            ex.muscleIds.remove(m.getId());
        }

        for (String exId : m.exerciseIds) {
            Exercise ex = exercises.exercises.get(exId);
            if (ex != null && !ex.muscleIds.contains(m.getId())) {
                ex.muscleIds.add(m.getId());
            }
        }

        exercises.save();
        muscles.save();
    }
}
