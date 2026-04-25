package de.daniel_nowak.muscletraining.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.daniel_nowak.muscletraining.model.Training;

public class RecommendationService extends OneRmModel {

    // -----------------------------------------
    // Recommendation-Datentransferobjekt
    // -----------------------------------------
    public static class Recommendation {
        public final int sets;
        public final int reps;
        public final float weight;
        public final float rm;   // RM-Wert für SeekBar

        public Recommendation(int sets, int reps, float weight, float rm) {
            this.sets = sets;
            this.reps = reps;
            this.weight = weight;
            this.rm = rm;
        }
    }

    // -----------------------------------------
    // Sets-Faktor (deine bestehende Logik)
    // -----------------------------------------
    private static float setsFactor(int sets) {
        return 1f + 0.02f * (sets - 1);
    }

    // -----------------------------------------
    // NÄCHSTE EMPFEHLUNG (unverändert)
    // -----------------------------------------
    public static Recommendation next(
            List<Training> trainings,
            int minSets, int maxSets,
            int minReps, int maxReps, int stepReps,
            float minWeight, float maxWeight, float stepWeight
    ) {

        // Letztes Training
        Training last = trainings.get(trainings.size() - 1);

        // RM des letzten Trainings
        float lastRM = estimateAverage1RM(last.getWeight(), last.getReps())
                * setsFactor(last.getSets());

        // Alle Kombinationen vorbereiten
        List<Recommendation> combos = allCombos(
                minSets, maxSets,
                minReps, maxReps, stepReps,
                minWeight, maxWeight, stepWeight
        );

        // Nächst höheren RM finden
        for (Recommendation c : combos) {
            if (c.rm > lastRM) {
                return c;
            }
        }

        // Falls nichts höheres existiert → minimal mögliche Steigerung
        float nextWeight = last.getWeight() + stepWeight;
        return new Recommendation(last.getSets(), last.getReps(), nextWeight,
                estimateAverage1RM(nextWeight, last.getReps()) * setsFactor(last.getSets()));
    }

    // -----------------------------------------
    // ALLE RM-KOMBINATIONEN (für SeekBar)
    // -----------------------------------------
    public static List<Recommendation> allCombos(
            int minSets, int maxSets,
            int minReps, int maxReps, int stepReps,
            float minWeight, float maxWeight, float stepWeight
    ) {
        List<Recommendation> list = new ArrayList<>();

        for (int sets = minSets; sets <= maxSets; sets++) {
            for (int reps = minReps; reps <= maxReps; reps += stepReps) {
                for (float weight = minWeight; weight <= maxWeight; weight += stepWeight) {

                    float rm = estimateAverage1RM(weight, reps) * setsFactor(sets);

                    list.add(new Recommendation(sets, reps, weight, rm));
                }
            }
        }

        // Sortieren nach RM, dann Gewicht
        list.sort(Comparator
                .comparingDouble((Recommendation r) -> r.rm)
                .thenComparingDouble(r -> r.weight)
        );

        return list;
    }
}
