package de.daniel_nowak.muscletraining.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import de.daniel_nowak.muscletraining.model.Exercise;

public class RecommendationService {

    public static float calcETL(int sets, int reps, float weight, int difficulty) {

        // 1RM Schätzung (Epley)
        float oneRm = weight * (1f + reps / 30f);

        // Intensität als %1RM
        float intensity = weight / oneRm;

        // Volume Load
        float volume = sets * reps * weight;

        // RIR basierter Effort
        int rir;
        switch (difficulty) {
            case 0: rir = 0; break;
            case 1: rir = 2; break;
            case 2: rir = 4; break;
            case 3: rir = 6; break; // besser als 5
            default: rir = 3; break;
        }

        float effort = 1f - (rir / 10f);

        return volume * intensity * effort;
    }

    public static Recommendation next(Exercise ex, List<Recommendation> all) {

        int lastSets = ex.getLastSets();
        int lastReps = ex.getLastReps();
        float lastWeight = ex.getLastWeight();
        int diff = ex.getLastDifficulty();

        int index = -1;

        for (int i = 0; i < all.size(); i++) {
            Recommendation c = all.get(i);
            if (c.sets == lastSets &&
                    c.reps == lastReps &&
                    c.weight == lastWeight) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            float lastETL = RecommendationService.calcETL(
                    lastSets, lastReps, lastWeight, 1
            );

            float bestDiff = Float.MAX_VALUE;
            int bestIndex = 0;

            for (int i = 0; i < all.size(); i++) {
                float d = Math.abs(all.get(i).etl - lastETL);
                if (d < bestDiff) {
                    bestDiff = d;
                    bestIndex = i;
                }
            }

            index = bestIndex;
        }

        int newIndex = index;

        switch (diff) {
            case 0:
                newIndex = index  - 1;
                break;

            case 2:
                newIndex = index + 1;
                break;

            case 3:
                newIndex = index + 2;
                break;

            default:
                newIndex = index;
        }

        if (newIndex < 0) newIndex = 0;
        if (newIndex >= all.size()) newIndex = all.size() - 1;

        return all.get(newIndex);
    }


    public static List<Recommendation> allCombos(
            int minSets, int maxSets,
            int minReps, int maxReps, int stepReps,
            float minWeight, float maxWeight, float stepWeight
    ) {
        TreeMap<Float, Recommendation> bestByEtl = new TreeMap<>();

        for (int sets = minSets; sets <= maxSets; sets++) {
            for (int reps = minReps; reps <= maxReps; reps += stepReps) {
                for (float weight = minWeight; weight <= maxWeight; weight += stepWeight) {

                    float etl = calcETL(sets, reps, weight, 1);

                    float key = Math.round(etl * 1000f) / 1000f;

                    Recommendation candidate =
                            new Recommendation(sets, reps, weight, etl);

                    if (!bestByEtl.containsKey(key)) {
                        bestByEtl.put(key, candidate);
                    } else {
                        Recommendation existing = bestByEtl.get(key);
                        if (candidate.weight < existing.weight) {
                            bestByEtl.put(key, candidate);
                        }
                    }
                }
            }
        }

        return new ArrayList<>(bestByEtl.values());
    }


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
}
