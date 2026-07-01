package de.daniel_nowak.muscletraining;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.daniel_nowak.muscletraining.logic.RecommendationService;
import de.daniel_nowak.muscletraining.model.Exercise;
import de.daniel_nowak.muscletraining.model.Muscle;
import de.daniel_nowak.muscletraining.ui.MuscleRegenView;

public class MainActivity extends BaseActivity {

    Spinner spinner;
    TextView txtLast, txtRec, txtIntensity, txtMuscleWarning, txtRegen;
    TextView hintSets, hintReps, hintWeight;
    EditText inputSets, inputReps, inputWeight;
    Button btnSave;

    Button btnSetsMinus, btnSetsPlus;
    Button btnRepsMinus, btnRepsPlus;
    Button btnWeightMinus, btnWeightPlus;

    SeekBar seekETL;
    MuscleRegenView regenView;

    SeekBar seekDifficulty;
    TextView txtDifficultyLabel;
    int selectedDifficulty = 1;

    List<RecommendationService.Recommendation> etlCombos;

    MainUiBinder binder;
    MainUiController ui;
    MainUiUpdater updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar(R.id.toolbar);
        applyOrientationLayout();

        binder = new MainUiBinder(this);
        binder.bind();
        
        highlightFront();

        ui = new MainUiController(this);
        ui.setupButtons();
        ui.setupSeekbar();
        ui.setupSpinner();
        ui.setupDifficultySlider();

        updater = new MainUiUpdater(this);

        refreshExerciseSpinner();
        updater.update();
    }

    @Override
    protected void onMenuRefresh() {
        refreshExerciseSpinner();
        updater.update();
    }

    void highlightFront() {
        findViewById(R.id.btn_front).setAlpha(1f);
        findViewById(R.id.btn_back).setAlpha(0.4f);
    }

    void highlightBack() {
        findViewById(R.id.btn_front).setAlpha(0.4f);
        findViewById(R.id.btn_back).setAlpha(1f);
    }

    void adjustSets(boolean increase) {
        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex != null)
            adjustInt(inputSets, 1, ex.getSetsMin(), ex.getSetsMax(), increase);
    }

    void adjustReps(boolean increase) {
        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex != null)
            adjustInt(inputReps, ex.getRepsStep(), ex.getRepsMin(), ex.getRepsMax(), increase);
    }

    void adjustWeight(boolean increase) {
        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex != null)
            adjustFloat(inputWeight, ex.getWeightStep(), ex.getWeightMin(), ex.getWeightMax(), increase);
    }

    void updateIntensityLabel(float etl) {
        float minRM = etlCombos.get(0).etl;
        float maxRM = etlCombos.get(etlCombos.size() - 1).etl;
        float range = maxRM - minRM;

        if (range <= 0.0001f) {
            txtIntensity.setText(R.string.intensity_label_zero);
            return;
        }

        float percent = (etl - minRM) / range * 100f;
        percent = Math.max(0, Math.min(100, percent));

        txtIntensity.setText(getString(R.string.intensity_label_format, Math.round(percent)));
    }

    void resetUI() {

        txtLast.setText(R.string.label_last_training);
        txtRec.setText(R.string.label_recommendation);
        txtIntensity.setText(R.string.label_intensity);
        txtMuscleWarning.setText(R.string.placeholder_dash);
        txtMuscleWarning.setTextColor(0xFF777777);
        txtRegen.setText(R.string.label_regeneration);
        txtRegen.setTextColor(0xFF777777);

        inputSets.setText("");
        inputReps.setText("");
        inputWeight.setText("");

        hintSets.setText(R.string.hint_min_max);
        hintReps.setText(R.string.hint_min_max_step);
        hintWeight.setText(R.string.hint_min_max_step);

        btnSave.setEnabled(false);
        btnSetsMinus.setEnabled(false);
        btnSetsPlus.setEnabled(false);
        btnRepsMinus.setEnabled(false);
        btnRepsPlus.setEnabled(false);
        btnWeightMinus.setEnabled(false);
        btnWeightPlus.setEnabled(false);

        seekETL.setEnabled(false);
        seekETL.setProgress(0);

        selectedDifficulty = -1;
        seekDifficulty.setEnabled(false);
        seekDifficulty.setProgress(2);
    }

    boolean validateInputs() {
        String sSets = inputSets.getText().toString().trim();
        String sReps = inputReps.getText().toString().trim();
        String sWeight = normalizeNumberInput(inputWeight.getText().toString());

        if (sSets.isEmpty() || sReps.isEmpty() || sWeight.isEmpty()) {
            Toast.makeText(this, R.string.toast_fill_all_fields, Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int sets = Integer.parseInt(sSets);
            int reps = Integer.parseInt(sReps);
            float weight = Float.parseFloat(sWeight);

            if (sets <= 0 || reps <= 0 || weight <= 0) {
                Toast.makeText(this, R.string.toast_invalid_values_positive, Toast.LENGTH_SHORT).show();
                return false;
            }

            inputWeight.setText(sWeight);

        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_invalid_numbers, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    String normalizeNumberInput(String s) {
        if (s == null) return "";
        s = s.trim().replace(",", ".");
        if (s.startsWith(".")) s = "0" + s;
        if (s.endsWith(".")) s = s + "0";
        return s;
    }

    void saveTraining() {
        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex == null) return;

        if (!validateInputs()) return;

        int sets = Integer.parseInt(inputSets.getText().toString());
        int reps = Integer.parseInt(inputReps.getText().toString());
        float weight = Float.parseFloat(inputWeight.getText().toString());

        db.addTraining(ex, sets, reps, weight, selectedDifficulty);

        updater.update();

        int count = spinner.getCount();
        if (count == 0) return;

        int currentPos = spinner.getSelectedItemPosition();
        int nextPos = -1;

        for (int i = currentPos + 1; i < count; i++) {
            Exercise nextEx = (Exercise) spinner.getItemAtPosition(i);
            if (selectedExercises.contains(nextEx.getId())) {
                nextPos = i;
                break;
            }
        }

        if (nextPos == -1) {
            for (int i = 0; i < currentPos; i++) {
                Exercise nextEx = (Exercise) spinner.getItemAtPosition(i);
                if (selectedExercises.contains(nextEx.getId())) {
                    nextPos = i;
                    break;
                }
            }
        }

        if (nextPos != -1) {
            spinner.setSelection(nextPos);
        }
    }

    Map<String, Float> calculateRegenForAllMuscles() {
        Map<String, Float> map = new HashMap<>();
        for (Muscle m : db.muscles.muscles.values()) {
            float regen = db.calculateRegeneration(m.getId()) * 100f;
            map.put(m.getId(), regen);
        }
        return map;
    }

    void refreshExerciseSpinner() {
        List<Exercise> list = new ArrayList<>(db.exercises.exercises.values());

        list.sort((a, b) -> {
            boolean aSel = selectedExercises.contains(a.getId());
            boolean bSel = selectedExercises.contains(b.getId());
            if (aSel && !bSel) return -1;
            if (!aSel && bSel) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        ExerciseSpinnerAdapter adapter =
                new ExerciseSpinnerAdapter(this, list, selectedExercises);
        spinner.setAdapter(adapter);
    }

    void adjustInt(EditText field, int step, int min, int max, boolean increase) {
        try {
            int value = Integer.parseInt(field.getText().toString());
            value = increase ? value + step : value - step;
            value = Math.max(min, Math.min(max, value));
            field.setText(String.valueOf(value));
        } catch (Exception ignored) {
        }
    }

    void adjustFloat(EditText field, float step, float min, float max, boolean increase) {
        try {
            float value = Float.parseFloat(normalizeNumberInput(field.getText().toString()));
            value = increase ? value + step : value - step;
            value = Math.max(min, Math.min(max, value));
            field.setText(formatWeight(value));
        } catch (Exception ignored) {
        }
    }

}
