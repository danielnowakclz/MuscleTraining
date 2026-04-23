package de.daniel_nowak.muscletraining;

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
    private EditText inputSets, inputReps, inputWeight;
    private Button btnSave;

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
        btnSave = findViewById(R.id.btn_save_training);

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

    private void refreshExerciseSpinner() {
        List<Exercise> list = new ArrayList<>(db.exercises.exercises.values());
        ExerciseSpinnerAdapter adapter =
                new ExerciseSpinnerAdapter(this, list, selectedExercises);
        spinner.setAdapter(adapter);
    }

    private void updateUI() {
        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex == null) {
            txtLast.setText("Letztes Training: –");
            txtRec.setText("Empfehlung: –");

            inputSets.setText("");
            inputReps.setText("");
            inputWeight.setText("");
            return;
        }

        List<Training> trainings = db.trainings.trainings.values().stream()
                .filter(t -> t.getExerciseId().equals(ex.getId()))
                .sorted(Comparator.comparingLong(Training::getTime))
                .collect(Collectors.toList());

        if (trainings.isEmpty()) {
            txtLast.setText("Letztes Training: –");
            txtRec.setText("Empfehlung: –");

            inputSets.setText(String.valueOf(ex.getSetsMin()));
            inputReps.setText(String.valueOf(ex.getRepsMin()));
            inputWeight.setText(formatWeight(ex.getMinWeight()));
            return;
        }

        Training last = trainings.get(trainings.size() - 1);
        txtLast.setText("Letztes Training: " + last.getSets() + "×" + last.getReps() + " @ " + formatWeight(last.getWeight()) + " kg");

        RecommendationService.Recommendation rec =
                RecommendationService.next(
                        trainings,
                        ex.getSetsMin(), ex.getSetsMax(), ex.getSetsStep(),
                        ex.getRepsMin(), ex.getRepsMax(), ex.getRepsStep(),
                        ex.getMinWeight(), ex.getMaxWeight(), ex.getWeightStep()
                );

        txtRec.setText("Empfehlung: " + rec.sets + "×" + rec.reps + " @ " + formatWeight(rec.weight) + " kg");

        inputSets.setText(String.valueOf(rec.sets));
        inputReps.setText(String.valueOf(rec.reps));
        inputWeight.setText(formatWeight(rec.weight));
    }

    private void saveTraining() {
        Exercise ex = (Exercise) spinner.getSelectedItem();
        if (ex == null) return;

        int sets = Integer.parseInt(inputSets.getText().toString());
        int reps = Integer.parseInt(inputReps.getText().toString());
        float weight = Float.parseFloat(inputWeight.getText().toString());

        db.trainings.add(ex.getId(), sets, reps, weight);

        updateUI();
    }

    @Override
    protected void onMenuRefresh() {
        refreshExerciseSpinner();
        updateUI();
    }
}
