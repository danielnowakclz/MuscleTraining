package de.daniel_nowak.muscletraining;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.*;

import java.util.*;
import java.util.stream.Collectors;

import de.daniel_nowak.muscletraining.logic.RecommendationService;
import de.daniel_nowak.muscletraining.model.*;
import de.daniel_nowak.muscletraining.ui.MuscleRegenView;

public class MainActivity extends BaseActivity {

    private Spinner spinner;
    private TextView txtLast, txtRec, txtIntensity, txtMuscleWarning, txtRegen;
    private TextView hintSets, hintReps, hintWeight;
    private EditText inputSets, inputReps, inputWeight;
    private Button btnSave;
    private SeekBar seekETL;

    private Button btnSetsMinus, btnSetsPlus;
    private Button btnRepsMinus, btnRepsPlus;
    private Button btnWeightMinus, btnWeightPlus;

    private MuscleRegenView regenView;

    private List<RecommendationService.Recommendation> etlCombos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar(R.id.toolbar);
        applyEdgeToEdge();

        // ---------------------------------------------------------
        // VIEW BINDING (einmalig, sauber)
        // ---------------------------------------------------------
        spinner = findViewById(R.id.spinner_exercise);
        txtLast = findViewById(R.id.txt_last_training);
        txtRec = findViewById(R.id.txt_recommendation);
        txtIntensity = findViewById(R.id.txt_intensity);
        txtMuscleWarning = findViewById(R.id.txt_muscle_warning);
        txtRegen = findViewById(R.id.txt_regen);

        inputSets = findViewById(R.id.input_sets);
        inputReps = findViewById(R.id.input_reps);
        inputWeight = findViewById(R.id.input_weight);

        hintSets = findViewById(R.id.hint_sets);
        hintReps = findViewById(R.id.hint_reps);
        hintWeight = findViewById(R.id.hint_weight);

        btnSave = findViewById(R.id.btn_save_training);

        btnSetsMinus = findViewById(R.id.btn_sets_minus);
        btnSetsPlus = findViewById(R.id.btn_sets_plus);
        btnRepsMinus = findViewById(R.id.btn_reps_minus);
        btnRepsPlus = findViewById(R.id.btn_reps_plus);
        btnWeightMinus = findViewById(R.id.btn_weight_minus);
        btnWeightPlus = findViewById(R.id.btn_weight_plus);

        regenView = findViewById(R.id.view_regen);

        Button btnFront = findViewById(R.id.btn_front);
        Button btnBack = findViewById(R.id.btn_back);

        seekETL = findViewById(R.id.seek_etl);

        // ---------------------------------------------------------
        // SEEKBAR-GRADIENT (Material-kompatibel)
        // ---------------------------------------------------------
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF4CAF50, 0xFFFFEB3B, 0xFFF44336}
        );
        gradient.setCornerRadius(20f);
        seekETL.setProgressDrawable(gradient);

        // ---------------------------------------------------------
        // FRONT/BACK SWITCH
        // ---------------------------------------------------------
        btnFront.setOnClickListener(v -> regenView.setSide(MuscleRegenView.Side.FRONT));
        btnBack.setOnClickListener(v -> regenView.setSide(MuscleRegenView.Side.BACK));

        // ---------------------------------------------------------
        // PLUS/MINUS BUTTONS
        // ---------------------------------------------------------
        btnSetsMinus.setOnClickListener(v -> adjustIntForSelectedExercise(inputSets, false));
        btnSetsPlus.setOnClickListener(v -> adjustIntForSelectedExercise(inputSets, true));

        btnRepsMinus.setOnClickListener(v -> adjustReps(false));
        btnRepsPlus.setOnClickListener(v -> adjustReps(true));

        btnWeightMinus.setOnClickListener(v -> adjustWeight(false));
        btnWeightPlus.setOnClickListener(v -> adjustWeight(true));

        // ---------------------------------------------------------
        // SEEKBAR LISTENER (einmalig!)
        // ---------------------------------------------------------
        seekETL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser || etlCombos == null || etlCombos.isEmpty()) return;
                if (progress < 0 || progress >= etlCombos.size()) return;

                RecommendationService.Recommendation c = etlCombos.get(progress);

                inputSets.setText(String.valueOf(c.sets));
                inputReps.setText(String.valueOf(c.reps));
                inputWeight.setText(formatWeight(c.weight));

                updateIntensityLabel(c);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // ---------------------------------------------------------
        // SPINNER
        // ---------------------------------------------------------
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                updateUI();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> saveTraining());

        refreshExerciseSpinner();
        updateUI();
    }

    // ---------------------------------------------------------
    // HELFER: PLUS/MINUS
    // ---------------------------------------------------------

    private void adjustIntForSelectedExercise(EditText field, boolean increase) {
        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex == null) return;
        adjustInt(field, 1, ex.getSetsMin(), ex.getSetsMax(), increase);
    }

    private void adjustReps(boolean increase) {
        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex == null) return;
        adjustInt(inputReps, ex.getRepsStep(), ex.getRepsMin(), ex.getRepsMax(), increase);
    }

    private void adjustWeight(boolean increase) {
        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex == null) return;
        adjustFloat(inputWeight, ex.getWeightStep(), ex.getMinWeight(), ex.getMaxWeight(), increase);
    }

    // ---------------------------------------------------------
    // UI UPDATE
    // ---------------------------------------------------------

    private void updateIntensityLabel(RecommendationService.Recommendation c) {
        float minRM = etlCombos.get(0).etl;
        float maxRM = etlCombos.get(etlCombos.size() - 1).etl;
        float range = maxRM - minRM;

        if (range <= 0.0001f) {
            txtIntensity.setText("Intensität: 0 %");
            return;
        }

        float percent = (c.etl - minRM) / range * 100f;
        percent = Math.max(0, Math.min(100, percent));

        txtIntensity.setText("Intensität: " + Math.round(percent) + " %");
    }

    private void resetUI() {
        txtLast.setText("Letztes Training: –");
        txtRec.setText("Empfehlung: –");
        txtIntensity.setText("Intensität: –");
        txtMuscleWarning.setText("–");
        txtMuscleWarning.setTextColor(0xFF777777);
        txtRegen.setText("Regeneration: –");
        txtRegen.setTextColor(0xFF777777);

        inputSets.setText("");
        inputReps.setText("");
        inputWeight.setText("");
        inputSets.setHint("");
        inputReps.setHint("");
        inputWeight.setHint("");

        hintSets.setText("Min – Max");
        hintReps.setText("Min – Max – Schritt");
        hintWeight.setText("Min – Max – Schritt");

        btnSave.setEnabled(false);
        findViewById(R.id.btn_sets_minus).setEnabled(false);
        findViewById(R.id.btn_sets_plus).setEnabled(false);
        findViewById(R.id.btn_reps_minus).setEnabled(false);
        findViewById(R.id.btn_reps_plus).setEnabled(false);
        findViewById(R.id.btn_weight_minus).setEnabled(false);
        findViewById(R.id.btn_weight_plus).setEnabled(false);

        seekETL.setEnabled(false);
        seekETL.setProgress(0);
    }

    private void adjustInt(EditText field, int step, int min, int max, boolean increase) {
        try {
            int value = Integer.parseInt(field.getText().toString());
            value = increase ? value + step : value - step;
            value = Math.max(min, Math.min(max, value));
            field.setText(String.valueOf(value));
        } catch (Exception ignored) {}
    }

    private void adjustFloat(EditText field, float step, float min, float max, boolean increase) {
        try {
            float value = Float.parseFloat(normalizeNumberInput(field.getText().toString()));
            value = increase ? value + step : value - step;
            value = Math.max(min, Math.min(max, value));
            field.setText(formatWeight(value));
        } catch (Exception ignored) {}
    }

    private void refreshExerciseSpinner() {
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

    private void updateUI() {

        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex == null) {
            resetUI();
            return;
        }

        hintSets.setText("Min: " + ex.getSetsMin() + " – Max: " + ex.getSetsMax());
        hintReps.setText("Min: " + ex.getRepsMin() + " – Max: " + ex.getRepsMax() + " – Schritt: " + ex.getRepsStep());
        hintWeight.setText("Min: " + ex.getMinWeight() + " – Max: " + ex.getMaxWeight() + " – Schritt: " + ex.getWeightStep());

        if (ex.muscleIds == null)
            ex.muscleIds = new ArrayList<>();

        List<Muscle> muscles = ex.muscleIds.stream()
                .map(id -> db.muscles.muscles.get(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // -------------------------------------------------------------
        // 1. MUSKEL-WARNUNGEN (Regeneration 2.1)
        // -------------------------------------------------------------
        List<String> warnings = new ArrayList<>();
        float worstRegen = 100f;

        for (Muscle m : muscles) {
            float regen = db.calculateRegeneration(m.getId()) * 100f;
            worstRegen = Math.min(worstRegen, regen);
            warnings.add(m.getName() + " (" + Math.round(regen) + "%)");
        }

        if (warnings.isEmpty()) {
            txtMuscleWarning.setText("–");
            txtMuscleWarning.setTextColor(0xFF777777);
        } else if (worstRegen < 40f) {
            txtMuscleWarning.setText("🔴 Zu früh: " + String.join(", ", warnings));
            txtMuscleWarning.setTextColor(0xFFFF4444);
        } else if (worstRegen < 70f) {
            txtMuscleWarning.setText("🟡 Vorsicht: " + String.join(", ", warnings));
            txtMuscleWarning.setTextColor(0xFFFFBB33);
        } else {
            txtMuscleWarning.setText("🟢 OK: " + String.join(", ", warnings));
            txtMuscleWarning.setTextColor(0xFF99CC00);
        }

        // -------------------------------------------------------------
        // 2. ÜBUNGS-REGENERATION (min aller Muskeln)
        // -------------------------------------------------------------
        float regenPercent = muscles.stream()
                .map(m -> db.calculateRegeneration(m.getId()) * 100f)
                .min(Float::compare)
                .orElse(100f);

        int regenInt = Math.round(regenPercent);

        if (regenInt < 40) {
            txtRegen.setText("Regeneration: " + regenInt + " %");
            txtRegen.setTextColor(0xFFFF4444);
        } else if (regenInt < 70) {
            txtRegen.setText("Regeneration: " + regenInt + " %");
            txtRegen.setTextColor(0xFFFFBB33);
        } else {
            txtRegen.setText("Regeneration: " + regenInt + " %");
            txtRegen.setTextColor(0xFF99CC00);
        }

        // -------------------------------------------------------------
        // 3. LETZTES TRAINING (optimiert)
        // -------------------------------------------------------------
        long last = ex.getLastTraining();

        if (last == 0L) {
            txtLast.setText("Letztes Training: –");
        } else {
            txtLast.setText("Letztes Training: " + formatDate(last));
        }

        // -------------------------------------------------------------
        // 4. EMPFEHLUNG & ETL-LOGIK (unverändert)
        // -------------------------------------------------------------
        List<Training> trainings = db.trainings.trainings.values().stream()
                .filter(t -> t.getExerciseId().equals(ex.getId()))
                .sorted(Comparator.comparingLong(Training::getTime))
                .collect(Collectors.toList());

        RecommendationService.Recommendation rec = null;

        if (trainings.isEmpty()) {
            txtRec.setText("Empfehlung: –");

            inputSets.setText(String.valueOf(ex.getSetsMin()));
            inputReps.setText(String.valueOf(ex.getRepsMin()));
            inputWeight.setText(formatWeight(ex.getMinWeight()));
            inputSets.setHint(String.valueOf(ex.getSetsMin()));
            inputReps.setHint(String.valueOf(ex.getRepsMin()));
            inputWeight.setHint(formatWeight(ex.getMinWeight()));

        } else {
            Training lastT = trainings.get(trainings.size() - 1);

            txtLast.setText("Letztes Training: " +
                    lastT.getSets() + "×" + lastT.getReps() +
                    " @ " + formatWeight(lastT.getWeight()) + " kg");

            rec = RecommendationService.next(
                    trainings,
                    ex.getSetsMin(), ex.getSetsMax(),
                    ex.getRepsMin(), ex.getRepsMax(), ex.getRepsStep(),
                    ex.getMinWeight(), ex.getMaxWeight(), ex.getWeightStep()
            );

            txtRec.setText("Empfehlung: " +
                    rec.sets + "×" + rec.reps +
                    " @ " + formatWeight(rec.weight) + " kg");

            inputSets.setText(String.valueOf(rec.sets));
            inputReps.setText(String.valueOf(rec.reps));
            inputWeight.setText(formatWeight(rec.weight));
            inputSets.setHint(String.valueOf(rec.sets));
            inputReps.setHint(String.valueOf(rec.reps));
            inputWeight.setHint(formatWeight(rec.weight));
        }

        etlCombos = RecommendationService.allCombos(
                ex.getSetsMin(), ex.getSetsMax(),
                ex.getRepsMin(), ex.getRepsMax(), ex.getRepsStep(),
                ex.getMinWeight(), ex.getMaxWeight(), ex.getWeightStep()
        );

        seekETL.setMax(etlCombos.size() - 1);

        int recIndex = 0;
        if (rec != null) {
            for (int i = 0; i < etlCombos.size(); i++) {
                RecommendationService.Recommendation c = etlCombos.get(i);
                if (c.sets == rec.sets && c.reps == rec.reps && c.weight == rec.weight) {
                    recIndex = i;
                    break;
                }
            }
        }

        seekETL.setProgress(recIndex);

        if (!etlCombos.isEmpty()) {
            RecommendationService.Recommendation c = etlCombos.get(recIndex);

            float minRM = etlCombos.get(0).etl;
            float maxRM = etlCombos.get(etlCombos.size() - 1).etl;
            float range = maxRM - minRM;

            if (range <= 0.0001f) {
                txtIntensity.setText("Intensität: 0 %");
            } else {
                float percent = (c.etl - minRM) / range * 100f;
                percent = Math.max(0, Math.min(100, percent));
                txtIntensity.setText("Intensität: " + Math.round(percent) + " %");
            }
        }

        seekETL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;

                if (progress < 0 || progress >= etlCombos.size()) return;

                RecommendationService.Recommendation c = etlCombos.get(progress);

                inputSets.setText(String.valueOf(c.sets));
                inputReps.setText(String.valueOf(c.reps));
                inputWeight.setText(formatWeight(c.weight));

                float minRM = etlCombos.get(0).etl;
                float maxRM = etlCombos.get(etlCombos.size() - 1).etl;
                float range = maxRM - minRM;

                if (range <= 0.0001f) {
                    txtIntensity.setText("Intensität: 0 %");
                    return;
                }

                float percent = (c.etl - minRM) / range * 100f;
                percent = Math.max(0, Math.min(100, percent));

                txtIntensity.setText("Intensität: " + Math.round(percent) + " %");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // -------------------------------------------------------------
        // 5. HEATMAP (Regeneration 2.1)
        // -------------------------------------------------------------
        MuscleRegenView regenView = findViewById(R.id.view_regen);

        Map<String, Float> regenMap = calculateRegenForAllMuscles();

        regenView.setMuscles(db.muscles.muscles);
        regenView.setRegenData(regenMap);
        regenView.invalidate();

        btnSave.setEnabled(true);
        findViewById(R.id.btn_sets_minus).setEnabled(true);
        findViewById(R.id.btn_sets_plus).setEnabled(true);
        findViewById(R.id.btn_reps_minus).setEnabled(true);
        findViewById(R.id.btn_reps_plus).setEnabled(true);
        findViewById(R.id.btn_weight_minus).setEnabled(true);
        findViewById(R.id.btn_weight_plus).setEnabled(true);
        seekETL.setEnabled(true);
    }

    private void saveTraining() {
        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex == null) return;

        if (!validateInputs()) return;

        int sets = Integer.parseInt(inputSets.getText().toString());
        int reps = Integer.parseInt(inputReps.getText().toString());
        float weight = Float.parseFloat(inputWeight.getText().toString());

        db.addTraining(ex, sets, reps, weight);

        updateUI();

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

        // wenn nichts gefunden → oben weitersuchen
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

    @Override
    protected void onMenuRefresh() {
        refreshExerciseSpinner();
        updateUI();
    }

    private String normalizeNumberInput(String s) {
        if (s == null) return "";

        s = s.trim();
        s = s.replace(",", ".");

        if (s.startsWith(".")) s = "0" + s;
        if (s.endsWith(".")) s = s + "0";

        return s;
    }

    private boolean validateInputs() {
        String sSets = inputSets.getText().toString().trim();
        String sReps = inputReps.getText().toString().trim();
        String sWeight = normalizeNumberInput(inputWeight.getText().toString());

        if (sSets.isEmpty() || sReps.isEmpty() || sWeight.isEmpty()) {
            Toast.makeText(this, "Bitte alle Felder ausfüllen.", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int sets = Integer.parseInt(sSets);
            int reps = Integer.parseInt(sReps);
            float weight = Float.parseFloat(sWeight);

            if (sets <= 0 || reps <= 0 || weight <= 0) {
                Toast.makeText(this, "Ungültige Werte: Sätze, Wiederholungen und Gewicht müssen > 0 sein.", Toast.LENGTH_SHORT).show();
                return false;
            }

            inputWeight.setText(sWeight);

        } catch (Exception e) {
            Toast.makeText(this, "Bitte gültige Zahlen eingeben.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // -------------------------------------------------------------
    // Regeneration für ALLE Muskeln (String‑IDs!)
    // -------------------------------------------------------------
    private Map<String, Float> calculateRegenForAllMuscles() {
        Map<String, Float> map = new HashMap<>();

        for (Muscle m : db.muscles.muscles.values()) {
            float regen = db.calculateRegeneration(m.getId()) * 100f;
            map.put(m.getId(), regen);
        }

        return map;
    }

    private String formatDate(long time) {
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(new java.util.Date(time));
    }

}
