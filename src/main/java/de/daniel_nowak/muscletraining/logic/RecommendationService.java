package de.daniel_nowak.muscletraining.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.daniel_nowak.muscletraining.model.Training;

public class RecommendationService extends OneRmModel {

    public static class Recommendation {
        public final int sets;
        public final int reps;
        public final float weight;

        public Recommendation(int sets, int reps, float weight) {
            this.sets = sets;
            this.reps = reps;
            this.weight = weight;
        }

    }


    private static class Combo {
        int sets;
        int reps;
        float weight;
        float rm;

        Combo(int sets, int reps, float weight, float rm) {
            this.sets = sets;
            this.reps = reps;
            this.weight = weight;
            this.rm = rm;
        }
    }

    // Sets als Multiplikator für RM
    private static float setsFactor(int sets) {
        return 1f + 0.02f * (sets - 1);
    }

    public static Recommendation next(
            List<Training> trainings,
            int minSets, int maxSets, int stepSets,
            int minReps, int maxReps, int stepReps,
            float minWeight, float maxWeight, float stepWeight
    ) {

        // Letztes Training
        Training last = trainings.get(trainings.size() - 1);

        // RM des letzten Trainings
        float lastRM = estimateAverage1RM(last.getWeight(), last.getReps())
                * setsFactor(last.getSets());

        // Alle Kombinationen vorbereiten
        List<Combo> combos = new ArrayList<>();

        for (int sets = minSets; sets <= maxSets; sets += stepSets) {
            for (int reps = minReps; reps <= maxReps; reps += stepReps) {
                for (float weight = minWeight; weight <= maxWeight; weight += stepWeight) {

                    float rm = estimateAverage1RM(weight, reps)
                            * setsFactor(sets);

                    combos.add(new Combo(sets, reps, weight, rm));
                }
            }
        }

        // Sortieren:
        // 1. RM aufsteigend
        // 2. bei gleichem RM → Gewicht aufsteigend
        combos.sort(Comparator
                .comparingDouble((Combo c1) -> c1.rm)
                .thenComparingDouble(c1 -> c1.weight)
        );

        // Nächst höheren RM finden
        for (Combo c1 : combos) {
            if (c1.rm > lastRM) {
                return new Recommendation(c1.sets, c1.reps, c1.weight);
            }
        }

        // Falls nichts höheres existiert → minimal mögliche Steigerung
        float nextWeight = last.getWeight() + stepWeight;
        return new Recommendation(last.getSets(), last.getReps(), nextWeight);
    }

}
