package de.daniel_nowak.muscletraining.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.daniel_nowak.muscletraining.model.Training;

public class RecommendationService {

    public static class Recommendation {
        public final int sets;
        public final int reps;
        public final float weight;
        public final float etl;

        public Recommendation(int sets, int reps, float weight, float etl) {
            this.sets = sets;
            this.reps = reps;
            this.weight = weight;
            this.etl = etl;
        }
    }

    private static float calcETL(int sets, int reps, float weight) {
        return sets * reps * weight;
    }

    public static Recommendation next(
            List<Training> trainings,
            int minSets, int maxSets,
            int minReps, int maxReps, int stepReps,
            float minWeight, float maxWeight, float stepWeight
    ) {

        Training last = trainings.get(trainings.size() - 1);

        float lastETL = calcETL(last.getSets(), last.getReps(), last.getWeight());

        float factor;
        if (lastETL < 2000f) factor = 1.03f;
        else if (lastETL < 5000f) factor = 1.02f;
        else factor = 1.015f;

        float targetETL = lastETL * factor;

        List<Recommendation> combos = allCombos(
                minSets, maxSets,
                minReps, maxReps, stepReps,
                minWeight, maxWeight, stepWeight
        );

        for (Recommendation c : combos) {
            if (c.etl >= targetETL) return c;
        }

        return new Recommendation(
                last.getSets(),
                last.getReps(),
                last.getWeight(),
                lastETL
        );
    }

    public static List<Recommendation> allCombos(
            int minSets, int maxSets,
            int minReps, int maxReps, int stepReps,
            float minWeight, float maxWeight, float stepWeight
    ) {
        List<Recommendation> list = new ArrayList<>();

        for (int sets = minSets; sets <= maxSets; sets++) {
            for (int reps = minReps; reps <= maxReps; reps += stepReps) {
                for (float weight = minWeight; weight <= maxWeight; weight += stepWeight) {

                    float etl = calcETL(sets, reps, weight);

                    list.add(new Recommendation(sets, reps, weight, etl));
                }
            }
        }

        list.sort(Comparator
                .comparingDouble((Recommendation r) -> r.etl)
                .thenComparingInt(r -> r.sets)
                .thenComparingInt(r -> r.reps)
                .thenComparingDouble(r -> r.weight)
        );

        return list;
    }
}
