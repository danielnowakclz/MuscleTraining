package de.daniel_nowak.muscletraining.data;

import android.content.Context;

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

    public void addDemoData() {
        muscles.addDemoData();
        exercises.addDemoData();
    }

}
