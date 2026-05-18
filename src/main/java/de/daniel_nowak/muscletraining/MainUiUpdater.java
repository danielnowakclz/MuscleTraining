package de.daniel_nowak.muscletraining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.daniel_nowak.muscletraining.logic.RecommendationService;
import de.daniel_nowak.muscletraining.model.Exercise;
import de.daniel_nowak.muscletraining.model.Muscle;

public class MainUiUpdater {

    private final MainActivity a;

    public MainUiUpdater(MainActivity activity) {
        this.a = activity;
    }

    public void update() {

        Exercise ex = (Exercise) a.spinner.getSelectedItem();
        if (ex == null) {
            a.resetUI();
            return;
        }

        a.hintSets.setText("Min: " + ex.getSetsMin() + " – Max: " + ex.getSetsMax());
        a.hintReps.setText("Min: " + ex.getRepsMin() + " – Max: " + ex.getRepsMax() + " – Schritt: " + ex.getRepsStep());
        a.hintWeight.setText("Min: " + ex.getWeightMin() + " – Max: " + ex.getWeightMax() + " – Schritt: " + ex.getWeightStep());

        if (ex.muscleIds == null)
            ex.muscleIds = new ArrayList<>();

        List<Muscle> muscles = ex.muscleIds.stream()
                .map(id -> a.db.muscles.muscles.get(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        updateWarnings(muscles);
        updateRegen(muscles);
        updateLastTraining(ex);
        updateRecommendation(ex, muscles);
        updateHeatmap();

        a.selectedDifficulty=1;
        a.seekDifficulty.setProgress(a.selectedDifficulty);

        enableControls();

    }

    private void updateWarnings(List<Muscle> muscles) {
        List<String> warnings = new ArrayList<>();
        float worstRegen = 100f;

        for (Muscle m : muscles) {
            float regen = a.db.calculateRegeneration(m.getId()) * 100f;
            worstRegen = Math.min(worstRegen, regen);
            warnings.add(m.getName() + " (" + Math.round(regen) + "%)");
        }

        if (warnings.isEmpty()) {
            a.txtMuscleWarning.setText("–");
            a.txtMuscleWarning.setTextColor(0xFF777777);
        } else if (worstRegen < 40f) {
            a.txtMuscleWarning.setText("🔴 Zu früh: " + String.join(", ", warnings));
            a.txtMuscleWarning.setTextColor(0xFFFF4444);
        } else if (worstRegen < 70f) {
            a.txtMuscleWarning.setText("🟡 Vorsicht: " + String.join(", ", warnings));
            a.txtMuscleWarning.setTextColor(0xFFFFBB33);
        } else {
            a.txtMuscleWarning.setText("🟢 OK: " + String.join(", ", warnings));
            a.txtMuscleWarning.setTextColor(0xFF99CC00);
        }
    }

    private void updateRegen(List<Muscle> muscles) {
        float regenPercent = muscles.stream()
                .map(m -> a.db.calculateRegeneration(m.getId()) * 100f)
                .min(Float::compare)
                .orElse(100f);

        int regenInt = Math.round(regenPercent);

        if (regenInt < 40) {
            a.txtRegen.setText("Regeneration: " + regenInt + " %");
            a.txtRegen.setTextColor(0xFFFF4444);
        } else if (regenInt < 70) {
            a.txtRegen.setText("Regeneration: " + regenInt + " %");
            a.txtRegen.setTextColor(0xFFFFBB33);
        } else {
            a.txtRegen.setText("Regeneration: " + regenInt + " %");
            a.txtRegen.setTextColor(0xFF99CC00);
        }
    }

    private void updateLastTraining(Exercise ex) {
        long last = ex.getLastTraining();

        if (last == 0L) {
            a.txtLast.setText("Letztes Training: –");
        } else {
            String difficulty = "";
            if (ex.getLastDifficulty() == 0) difficulty = "leicht";
            else if (ex.getLastDifficulty() == 1) difficulty = "angenehm";
            else if (ex.getLastDifficulty() == 2) difficulty = "schwer";
            if (difficulty.length() > 0) difficulty = " (" + difficulty + ")";
            a.txtLast.setText("Letztes Training: " + ex.getLastSets() + "×" + ex.getLastReps() +
                    " @ " + a.formatWeight(ex.getLastWeight()) + " kg" + difficulty);
        }
    }

    private void updateRecommendation(Exercise ex, List<Muscle> muscles) {

        a.etlCombos = RecommendationService.allCombos(
                ex.getSetsMin(), ex.getSetsMax(),
                ex.getRepsMin(), ex.getRepsMax(), ex.getRepsStep(),
                ex.getWeightMin(), ex.getWeightMax(), ex.getWeightStep()
        );

        a.seekETL.setMax(a.etlCombos.size() - 1);
        RecommendationService.Recommendation rec  = RecommendationService.next(ex, a.etlCombos);

        a.txtRec.setText("Empfehlung: " +
              rec.sets + "×" + rec.reps +
                    " @ " + a.formatWeight(rec.weight) + " kg");

            a.inputSets.setText(String.valueOf(rec.sets));
            a.inputReps.setText(String.valueOf(rec.reps));
            a.inputWeight.setText(a.formatWeight(rec.weight));

            int recIndex = 0;
            for (int i = 0; i < a.etlCombos.size(); i++) {
                RecommendationService.Recommendation c = a.etlCombos.get(i);
                if (c.sets == rec.sets && c.reps == rec.reps && c.weight == rec.weight) {
                    recIndex = i;
                    break;
                }
            }
            a.seekETL.setProgress(recIndex);
            a.updateIntensityLabel(rec.etl);
    }

    private void updateHeatmap() {
        Map<String, Float> regenMap = a.calculateRegenForAllMuscles();
        a.regenView.setMuscles(a.db.muscles.muscles);
        a.regenView.setRegenData(regenMap);
        a.regenView.invalidate();
    }

    private void enableControls() {
        a.btnSave.setEnabled(true);
        a.btnSetsMinus.setEnabled(true);
        a.btnSetsPlus.setEnabled(true);
        a.btnRepsMinus.setEnabled(true);
        a.btnRepsPlus.setEnabled(true);
        a.btnWeightMinus.setEnabled(true);
        a.btnWeightPlus.setEnabled(true);
        a.seekETL.setEnabled(true);
        a.seekDifficulty.setEnabled(true);
    }
}
