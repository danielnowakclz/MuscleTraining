package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import java.util.Iterator;
import java.util.Map;

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
    // DEMO-DATEN
    // ---------------------------------------------------------

    public void addDemoData() {
        muscles.addDemoData();
        exercises.addDemoData();
        syncAllRelations();
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

        Iterator<Map.Entry<String, Training>> it = trainings.trainings.entrySet().iterator();
        while (it.hasNext()) {
            Training t = it.next().getValue();
            if (t.getExerciseId().equals(id)) {
                it.remove();
            }
        }

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

        muscles.save();
        exercises.save();
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
