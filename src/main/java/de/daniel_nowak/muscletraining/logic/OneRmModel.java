package de.daniel_nowak.muscletraining.logic;

import java.util.List;

import de.daniel_nowak.muscletraining.model.Training;

public class OneRmModel {

    // -----------------------------
    // 1RM Formeln (Einzelmodelle)
    // -----------------------------

    private static float estimateEpley(float weight, int reps) {
        return weight * (1f + reps / 30f);
    }

    private static float estimateBrzycki(float weight, int reps) {
        return weight * (36f / (37f - reps));
    }

    private static float estimateLombardi(float weight, int reps) {
        return (float) (weight * Math.pow(reps, 0.10));
    }

    private static float estimateOConner(float weight, int reps) {
        return weight * (1f + 0.025f * reps);
    }

    private static float estimateMayhew(float weight, int reps) {
        return (float) ((100f * weight) / (52.2 + 41.9 * Math.exp(-0.055f * reps)));
    }

    // Durchschnitt aller Modelle
    public static float estimateAverage1RM(float weight, int reps) {
        float epley = estimateEpley(weight, reps);
        float brzycki = estimateBrzycki(weight, reps);
        float lombardi = estimateLombardi(weight, reps);
        float oconner = estimateOConner(weight, reps);
        float mayhew = estimateMayhew(weight, reps);

        return (epley + brzycki + lombardi + oconner + mayhew) / 5f;
    }

}
