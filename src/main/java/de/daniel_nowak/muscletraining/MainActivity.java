package de.daniel_nowak.muscletraining;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.*;

import java.util.*;
import java.util.stream.Collectors;

import de.daniel_nowak.muscletraining.logic.RecommendationService;
import de.daniel_nowak.muscletraining.model.*;

public class MainActivity extends BaseActivity {

    private Spinner spinner;
    private TextView txtLast;
    private TextView txtRec;

    private TextView hintSets, hintReps, hintWeight;
    private EditText inputSets, inputReps, inputWeight;
    private Button btnSave;

    private SeekBar seekRM;
    private TextView txtIntensity;

    private TextView txtMuscleWarning;
    private TextView txtRegen;



    private List<RecommendationService.Recommendation> rmCombos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar(R.id.toolbar);
        applyEdgeToEdge();

        spinner = findViewById(R.id.spinner_exercise);
        txtLast = findViewById(R.id.txt_last_training);
        txtRec = findViewById(R.id.txt_recommendation);

        inputSets = findViewById(R.id.input_sets);
        inputReps = findViewById(R.id.input_reps);
        inputWeight = findViewById(R.id.input_weight);

        hintSets = findViewById(R.id.hint_sets);
        hintReps = findViewById(R.id.hint_reps);
        hintWeight = findViewById(R.id.hint_weight);

        btnSave = findViewById(R.id.btn_save_training);

        seekRM = findViewById(R.id.seek_rm);
        txtIntensity = findViewById(R.id.txt_intensity);

        txtMuscleWarning = findViewById(R.id.txt_muscle_warning);
        txtRegen = findViewById(R.id.txt_regen);


        // ---------------------------------------------
        // SeekBar Farbverlauf (grün → gelb → rot)
        // ---------------------------------------------
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        0xFF4CAF50, // Grün
                        0xFFFFEB3B, // Gelb
                        0xFFF44336  // Rot
                }
        );
        seekRM.setProgressDrawable(gradient);

        // Buttons
        Button btnSetsMinus = findViewById(R.id.btn_sets_minus);
        Button btnSetsPlus = findViewById(R.id.btn_sets_plus);
        Button btnRepsMinus = findViewById(R.id.btn_reps_minus);
        Button btnRepsPlus = findViewById(R.id.btn_reps_plus);
        Button btnWeightMinus = findViewById(R.id.btn_weight_minus);
        Button btnWeightPlus = findViewById(R.id.btn_weight_plus);

        btnSetsMinus.setOnClickListener(v -> {
            Exercise ex = (Exercise) spinner.getSelectedItem();
            adjustInt(inputSets, 1, ex.getSetsMin(), ex.getSetsMax(), false);
        });

        btnSetsPlus.setOnClickListener(v -> {
            Exercise ex = (Exercise) spinner.getSelectedItem();
            adjustInt(inputSets, 1, ex.getSetsMin(), ex.getSetsMax(), true);
        });

        btnRepsMinus.setOnClickListener(v -> {
            Exercise ex = (Exercise) spinner.getSelectedItem();
            adjustInt(inputReps, ex.getRepsStep(), ex.getRepsMin(), ex.getRepsMax(), false);
        });

        btnRepsPlus.setOnClickListener(v -> {
            Exercise ex = (Exercise) spinner.getSelectedItem();
            adjustInt(inputReps, ex.getRepsStep(), ex.getRepsMin(), ex.getRepsMax(), true);
        });

        btnWeightMinus.setOnClickListener(v -> {
            Exercise ex = (Exercise) spinner.getSelectedItem();
            adjustFloat(inputWeight, ex.getWeightStep(), ex.getMinWeight(), ex.getMaxWeight(), false);
        });

        btnWeightPlus.setOnClickListener(v -> {
            Exercise ex = (Exercise) spinner.getSelectedItem();
            adjustFloat(inputWeight, ex.getWeightStep(), ex.getMinWeight(), ex.getMaxWeight(), true);
        });

        refreshExerciseSpinner();
        updateUI();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                updateUI();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> saveTraining());
    }

    private void resetUI() {

        // -------------------------------------------------------------
        // Texte neutralisieren
        // -------------------------------------------------------------
        txtLast.setText("Letztes Training: –");
        txtRec.setText("Empfehlung: –");
        txtIntensity.setText("Intensität: –");
        txtMuscleWarning.setText("–");
        txtMuscleWarning.setTextColor(0xFF777777);
        txtRegen.setText("Regeneration: –");
        txtRegen.setTextColor(0xFF777777);

        // -------------------------------------------------------------
        // Eingabefelder leeren
        // -------------------------------------------------------------
        inputSets.setText("");
        inputReps.setText("");
        inputWeight.setText("");

        // -------------------------------------------------------------
        // Hints zurücksetzen
        // -------------------------------------------------------------
        hintSets.setText("Min – Max");
        hintReps.setText("Min – Max – Schritt");
        hintWeight.setText("Min – Max – Schritt");

        // -------------------------------------------------------------
        // Buttons deaktivieren
        // -------------------------------------------------------------
        btnSave.setEnabled(false);

        findViewById(R.id.btn_sets_minus).setEnabled(false);
        findViewById(R.id.btn_sets_plus).setEnabled(false);
        findViewById(R.id.btn_reps_minus).setEnabled(false);
        findViewById(R.id.btn_reps_plus).setEnabled(false);
        findViewById(R.id.btn_weight_minus).setEnabled(false);
        findViewById(R.id.btn_weight_plus).setEnabled(false);

        // -------------------------------------------------------------
        // SeekBar neutralisieren
        // -------------------------------------------------------------
        seekRM.setEnabled(false);
        seekRM.setProgress(0);

        // Optional: Intensität neutral anzeigen
        txtIntensity.setText("Intensität: –");
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

        // -------------------------------------------------------------
        // 0. Spinner leer? → UI zurücksetzen
        // -------------------------------------------------------------
        if (spinner.getSelectedItem() == null) {
            resetUI();
            return;
        }

        Exercise ex = (Exercise) spinner.getSelectedItem();

        // -------------------------------------------------------------
        // 1. Exercise null? → UI zurücksetzen
        // -------------------------------------------------------------
        if (ex == null) {
            resetUI();
            return;
        }

        // -------------------------------------------------------------
        // 2. muscleIds null? → leere Liste statt Crash
        // -------------------------------------------------------------
        if (ex.muscleIds == null) {
            ex.muscleIds = new ArrayList<>();
        }

        // -------------------------------------------------------------
        // 3. MUSKEL-WARNUNG
        // -------------------------------------------------------------
        List<Muscle> muscles = ex.muscleIds.stream()
                .map(id -> db.muscles.muscles.get(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long now = System.currentTimeMillis();
        List<String> warnings = new ArrayList<>();
        int worstLevel = 0; // 0 = grün, 1 = gelb, 2 = rot

        for (Muscle m : muscles) {

            List<Training> mTrainings = db.trainings.trainings.values().stream()
                    .filter(t -> t.muscleIds.contains(m.getId()))
                    .sorted(Comparator.comparingLong(Training::getTime).reversed())
                    .collect(Collectors.toList());

            if (mTrainings.isEmpty()) {
                warnings.add(m.getName() + " (nie trainiert)");
                continue;
            }

            long last = mTrainings.get(0).getTime();
            long diff = now - last;
            long hours = diff / (1000 * 60 * 60);

            if (hours < 6) {
                warnings.add(m.getName() + " (" + hours + "h)");
                worstLevel = Math.max(worstLevel, 2);
            } else if (hours < 24) {
                warnings.add(m.getName() + " (" + hours + "h)");
                worstLevel = Math.max(worstLevel, 1);
            } else {
                warnings.add(m.getName() + " (" + hours + "h)");
            }
        }

        // -------------------------------------------------------------
        // 4. MUSKEL-WARNUNG anzeigen
        // -------------------------------------------------------------
        if (warnings.isEmpty()) {
            txtMuscleWarning.setText("–");
            txtMuscleWarning.setTextColor(0xFF777777);
        } else if (worstLevel == 2) {
            txtMuscleWarning.setText("🔴 Zu früh: " + String.join(", ", warnings));
            txtMuscleWarning.setTextColor(0xFFFF4444);
        } else if (worstLevel == 1) {
            txtMuscleWarning.setText("🟡 Vorsicht: " + String.join(", ", warnings));
            txtMuscleWarning.setTextColor(0xFFFFBB33);
        } else {
            txtMuscleWarning.setText("🟢 OK: " + String.join(", ", warnings));
            txtMuscleWarning.setTextColor(0xFF99CC00);
        }

        // -------------------------------------------------------------
        // 5. REGENERATIONSPROZENT
        // -------------------------------------------------------------
        float regenPercent = 100f;

        for (Muscle m : muscles) {

            List<Training> mTrainings = db.trainings.trainings.values().stream()
                    .filter(t -> t.muscleIds.contains(m.getId()))
                    .sorted(Comparator.comparingLong(Training::getTime).reversed())
                    .collect(Collectors.toList());

            if (mTrainings.isEmpty()) continue;

            long last = mTrainings.get(0).getTime();
            float hours = (now - last) / (1000f * 60f * 60f);

            float r = Math.min(100f, (hours / 48f) * 100f);
            regenPercent = Math.min(regenPercent, r);
        }

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
        // 6. Buttons aktivieren
        // -------------------------------------------------------------
        btnSave.setEnabled(true);
        findViewById(R.id.btn_sets_minus).setEnabled(true);
        findViewById(R.id.btn_sets_plus).setEnabled(true);
        findViewById(R.id.btn_reps_minus).setEnabled(true);
        findViewById(R.id.btn_reps_plus).setEnabled(true);
        findViewById(R.id.btn_weight_minus).setEnabled(true);
        findViewById(R.id.btn_weight_plus).setEnabled(true);
        seekRM.setEnabled(true);

        // -------------------------------------------------------------
        // 7. Letztes Training + Empfehlung
        // -------------------------------------------------------------
        List<Training> trainings = db.trainings.trainings.values().stream()
                .filter(t -> t.getExerciseId().equals(ex.getId()))
                .sorted(Comparator.comparingLong(Training::getTime))
                .collect(Collectors.toList());

        RecommendationService.Recommendation rec = null;

        if (trainings.isEmpty()) {
            txtLast.setText("Letztes Training: –");
            txtRec.setText("Empfehlung: –");

            inputSets.setText(String.valueOf(ex.getSetsMin()));
            inputReps.setText(String.valueOf(ex.getRepsMin()));
            inputWeight.setText(formatWeight(ex.getMinWeight()));
        } else {
            Training last = trainings.get(trainings.size() - 1);
            txtLast.setText("Letztes Training: " + last.getSets() + "×" + last.getReps() + " @ " + formatWeight(last.getWeight()) + " kg");

            rec = RecommendationService.next(
                    trainings,
                    ex.getSetsMin(), ex.getSetsMax(),
                    ex.getRepsMin(), ex.getRepsMax(), ex.getRepsStep(),
                    ex.getMinWeight(), ex.getMaxWeight(), ex.getWeightStep()
            );

            txtRec.setText("Empfehlung: " + rec.sets + "×" + rec.reps + " @ " + formatWeight(rec.weight) + " kg");

            inputSets.setText(String.valueOf(rec.sets));
            inputReps.setText(String.valueOf(rec.reps));
            inputWeight.setText(formatWeight(rec.weight));
        }

        // -------------------------------------------------------------
        // 8. RM-Kombinationen
        // -------------------------------------------------------------
        rmCombos = RecommendationService.allCombos(
                ex.getSetsMin(), ex.getSetsMax(),
                ex.getRepsMin(), ex.getRepsMax(), ex.getRepsStep(),
                ex.getMinWeight(), ex.getMaxWeight(), ex.getWeightStep()
        );

        float minRM = rmCombos.get(0).rm;
        float maxRM = rmCombos.get(rmCombos.size() - 1).rm;

        seekRM.setMax(rmCombos.size() - 1);

        int recIndex = 0;
        if (rec != null) {
            for (int i = 0; i < rmCombos.size(); i++) {
                RecommendationService.Recommendation c = rmCombos.get(i);
                if (c.sets == rec.sets && c.reps == rec.reps && c.weight == rec.weight) {
                    recIndex = i;
                    break;
                }
            }
        }

        seekRM.setProgress(recIndex);

        // -------------------------------------------------------------
        // 9. SeekBar Listener
        // -------------------------------------------------------------
        seekRM.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (rmCombos == null || rmCombos.isEmpty()) return;
                if (progress < 0 || progress >= rmCombos.size()) return;

                RecommendationService.Recommendation c = rmCombos.get(progress);

                inputSets.setText(String.valueOf(c.sets));
                inputReps.setText(String.valueOf(c.reps));
                inputWeight.setText(formatWeight(c.weight));

                float range = maxRM - minRM;
                if (range <= 0.0001f) {
                    txtIntensity.setText("Intensität: 0 %");
                    return;
                }

                float percent = (c.rm - minRM) / range * 100f;
                percent = Math.max(0, Math.min(100, percent));

                txtIntensity.setText("Intensität: " + Math.round(percent) + " %");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void saveTraining() {
        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex == null) return;

        if (!validateInputs()) return;

        int sets = Integer.parseInt(inputSets.getText().toString());
        int reps = Integer.parseInt(inputReps.getText().toString());
        float weight = Float.parseFloat(inputWeight.getText().toString());

        db.trainings.add(ex, sets, reps, weight);

        updateUI();

        // Variante B: Nur zu aktiven Übungen springen
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
}
