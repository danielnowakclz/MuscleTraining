package de.daniel_nowak.muscletraining.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import de.daniel_nowak.muscletraining.model.Exercise;

public class RecommendationService {

    // ---------------------------------------------------------
    // Moderne ETL-Berechnung: exponentielle Kraftkurve + RIR-Effort
    // ---------------------------------------------------------
    public static float calcETL(int sets, int reps, float weight, int difficulty) {

        // 1) Volumen
        float volume = sets * reps * weight;

        // 2) Moderne Intensität (exponentielle Kraftkurve)
        float intensity = (float) Math.exp(-0.035f * reps);

        // 3) RIR aus Difficulty ableiten
        int rir;
        switch (difficulty) {
            case 0:
                rir = 5;
                break; // leicht
            case 1:
                rir = 2;
                break; // angenehm
            case 2:
                rir = 0;
                break; // schwer
            default:
                rir = 3;
                break;
        }

        // 4) Effort-Faktor
        float effort = 1f - (rir / 10f);

        // 5) Moderne ETL-Formel
        return volume * intensity * effort;
    }


    // ---------------------------------------------------------
    // Zielverschiebung
    // ---------------------------------------------------------
    public static Recommendation next(Exercise ex, List<Recommendation> all) {

        int lastSets = ex.getLastSets();
        int lastReps = ex.getLastReps();
        float lastWeight = ex.getLastWeight();
        int diff = ex.getLastDifficulty();

        // ---------------------------------------------------------
        // 1. Startindex suchen: exakte Kombination
        // ---------------------------------------------------------
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

        // ---------------------------------------------------------
        // 2. Wenn exakte Kombination nicht existiert:
        //    → Kombination mit minimaler ETL-Differenz finden
        // ---------------------------------------------------------
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

        // ---------------------------------------------------------
        // 3. Zeigerbewegung abhängig von Difficulty
        // ---------------------------------------------------------
        int newIndex = index;

        switch (diff) {
            case 0: // leicht → 2 Schritte nach oben
                newIndex = index + 2;
                break;

            case 1: // angenehm → 1 Schritt nach oben
                newIndex = index + 1;
                break;

            case 2: // schwer → 1 Schritt nach unten
                newIndex = index - 1;
                break;

            default:
                newIndex = index;
        }

        // ---------------------------------------------------------
        // 4. Grenzen einhalten
        // ---------------------------------------------------------
        if (newIndex < 0) newIndex = 0;
        if (newIndex >= all.size()) newIndex = all.size() - 1;

        // ---------------------------------------------------------
        // 5. Empfehlung zurückgeben
        // ---------------------------------------------------------
        return all.get(newIndex);
    }


    // ---------------------------------------------------------
    // Moderne allCombos(): ETL-Deduplizierung + exponentielle Kurve
    // ---------------------------------------------------------
    public static List<Recommendation> allCombos(
            int minSets, int maxSets,
            int minReps, int maxReps, int stepReps,
            float minWeight, float maxWeight, float stepWeight
    ) {
        TreeMap<Float, Recommendation> bestByEtl = new TreeMap<>();

        for (int sets = minSets; sets <= maxSets; sets++) {
            for (int reps = minReps; reps <= maxReps; reps += stepReps) {
                for (float weight = minWeight; weight <= maxWeight; weight += stepWeight) {

                    // Difficulty = 1 (neutral) für ETL-Basisberechnung
                    float etl = calcETL(sets, reps, weight, 1);

                    // ETL runden, um Float-Duplikate zu vermeiden
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


    // ---------------------------------------------------------
    // Recommendation-Datenklasse
    // ---------------------------------------------------------
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
