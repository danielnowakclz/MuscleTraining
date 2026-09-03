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

        a.hintSets.setText(a.getString(R.string.hint_sets_format, ex.getSetsMin(), ex.getSetsMax()));
        a.hintReps.setText(a.getString(R.string.hint_reps_format, ex.getRepsMin(), ex.getRepsMax(), ex.getRepsStep()));
        a.hintWeight.setText(a.getString(R.string.hint_weight_format, a.formatWeight(ex.getWeightMin()), a.formatWeight(ex.getWeightMax()), a.formatWeight(ex.getWeightStep())));

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

        a.selectedDifficulty=2;
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
            a.txtMuscleWarning.setText(R.string.placeholder_dash);
            a.txtMuscleWarning.setTextColor(0xFF777777);
        } else if (worstRegen < 40f) {
            a.txtMuscleWarning.setText(a.getString(R.string.warning_too_early, String.join(", ", warnings)));
            a.txtMuscleWarning.setTextColor(0xFFFF4444);
        } else if (worstRegen < 70f) {
            a.txtMuscleWarning.setText(a.getString(R.string.warning_caution, String.join(", ", warnings)));
            a.txtMuscleWarning.setTextColor(0xFFFFBB33);
        } else {
            a.txtMuscleWarning.setText(a.getString(R.string.warning_ok, String.join(", ", warnings)));
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
            a.txtRegen.setText(a.getString(R.string.label_regeneration_format, regenInt));
            a.txtRegen.setTextColor(0xFFFF4444);
        } else if (regenInt < 70) {
            a.txtRegen.setText(a.getString(R.string.label_regeneration_format, regenInt));
            a.txtRegen.setTextColor(0xFFFFBB33);
        } else {
            a.txtRegen.setText(a.getString(R.string.label_regeneration_format, regenInt));
            a.txtRegen.setTextColor(0xFF99CC00);
        }
    }

    private void updateLastTraining(Exercise ex) {
        long last = ex.getLastTraining();

        if (last == 0L) {
            a.txtLast.setText(R.string.label_last_training);
        } else {
            String difficulty = "";
            if (ex.getLastDifficulty() == 0) difficulty = a.getString(R.string.difficulty_hard);
            else if (ex.getLastDifficulty() == 1) difficulty = a.getString(R.string.difficulty_repeatable);
            else if (ex.getLastDifficulty() == 2) difficulty = a.getString(R.string.difficulty_pleasant);
            else if (ex.getLastDifficulty() == 3) difficulty = a.getString(R.string.difficulty_easy);
            if (difficulty.length() > 0) difficulty = " (" + difficulty + ")";
            a.txtLast.setText(a.getString(R.string.label_last_training_format, ex.getLastSets(), ex.getLastReps(), a.formatWeight(ex.getLastWeight()), difficulty));
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

        a.txtRec.setText(a.getString(R.string.label_recommendation_format,
              rec.sets, rec.reps, a.formatWeight(rec.weight)));

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
