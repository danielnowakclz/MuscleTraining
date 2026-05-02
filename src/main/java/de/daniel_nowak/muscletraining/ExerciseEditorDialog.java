package de.daniel_nowak.muscletraining;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.*;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;
import java.util.stream.Collectors;

import de.daniel_nowak.muscletraining.data.Database;
import de.daniel_nowak.muscletraining.model.Exercise;
import de.daniel_nowak.muscletraining.model.Muscle;

public class ExerciseEditorDialog extends Dialog {

    private final Database db;
    private final Exercise ex;
    private final Runnable onSaveCallback;

    private EditText editName, editMinW, editMaxW, editStepW;
    private EditText editSetsMin, editSetsMax;
    private EditText editRepsMin, editRepsMax, editRepsStep;
    private EditText editSearch;

    private RecyclerView recycler;
    private MuscleMultiSelectAdapter adapter;

    private List<Muscle> allMuscles;

    public ExerciseEditorDialog(Context ctx, Database db, Exercise ex, Runnable onSaveCallback) {
        super(ctx);
        this.db = db;
        this.ex = ex;
        this.onSaveCallback = onSaveCallback;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.dialog_exercise_editor);

        getWindow().setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        editName = findViewById(R.id.edit_name);
        editMinW = findViewById(R.id.edit_min_weight);
        editMaxW = findViewById(R.id.edit_max_weight);
        editStepW = findViewById(R.id.edit_weight_step);

        editSetsMin = findViewById(R.id.edit_sets_min);
        editSetsMax = findViewById(R.id.edit_sets_max);

        editRepsMin = findViewById(R.id.edit_reps_min);
        editRepsMax = findViewById(R.id.edit_reps_max);
        editRepsStep = findViewById(R.id.edit_reps_step);

        // Buttons
        // Sätze
        findViewById(R.id.btn_sets_min_minus).setOnClickListener(v ->
                adjustInt(editSetsMin, 1, 1, 999, false));
        findViewById(R.id.btn_sets_min_plus).setOnClickListener(v ->
                adjustInt(editSetsMin, 1, 1, 999, true));

        findViewById(R.id.btn_sets_max_minus).setOnClickListener(v ->
                adjustInt(editSetsMax, 1, 1, 999, false));
        findViewById(R.id.btn_sets_max_plus).setOnClickListener(v ->
                adjustInt(editSetsMax, 1, 1, 999, true));

        // Wiederholungen
        findViewById(R.id.btn_reps_min_minus).setOnClickListener(v ->
                adjustInt(editRepsMin, 1, 1, 999, false));
        findViewById(R.id.btn_reps_min_plus).setOnClickListener(v ->
                adjustInt(editRepsMin, 1, 1, 999, true));

        findViewById(R.id.btn_reps_max_minus).setOnClickListener(v ->
                adjustInt(editRepsMax, 1, 1, 999, false));
        findViewById(R.id.btn_reps_max_plus).setOnClickListener(v ->
                adjustInt(editRepsMax, 1, 1, 999, true));

        findViewById(R.id.btn_reps_step_minus).setOnClickListener(v ->
                adjustInt(editRepsStep, 1, 1, 999, false));
        findViewById(R.id.btn_reps_step_plus).setOnClickListener(v ->
                adjustInt(editRepsStep, 1, 1, 999, true));

        // Gewichte
        findViewById(R.id.btn_min_weight_minus).setOnClickListener(v ->
                adjustFloat(editMinW, 0.5f, 0.5f, 999f, false));
        findViewById(R.id.btn_min_weight_plus).setOnClickListener(v ->
                adjustFloat(editMinW, 0.5f, 0.5f, 999f, true));

        findViewById(R.id.btn_max_weight_minus).setOnClickListener(v ->
                adjustFloat(editMaxW, 0.5f, 0.5f, 999f, false));
        findViewById(R.id.btn_max_weight_plus).setOnClickListener(v ->
                adjustFloat(editMaxW, 0.5f, 0.5f, 999f, true));

        findViewById(R.id.btn_weight_step_minus).setOnClickListener(v ->
                adjustFloat(editStepW, 0.5f, 0.5f, 999f, false));
        findViewById(R.id.btn_weight_step_plus).setOnClickListener(v ->
                adjustFloat(editStepW, 0.5f, 0.5f, 999f, true));


        editSearch = findViewById(R.id.edit_search);

        recycler = findViewById(R.id.recycler_muscles);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        allMuscles = new ArrayList<>(db.muscles.muscles.values());
        Collections.sort(allMuscles, Comparator.comparing(Muscle::getName));

        adapter = new MuscleMultiSelectAdapter(allMuscles, ex.muscleIds);
        recycler.setAdapter(adapter);

        fillFields();

        editSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMuscles(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> save());
        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dismiss());

        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            db.delExercise(ex.getId());
            onSaveCallback.run();
            dismiss();
        });

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
            float value = Float.parseFloat(field.getText().toString());
            value = increase ? value + step : value - step;
            value = Math.max(min, Math.min(max, value));
            field.setText(String.valueOf(value));
        } catch (Exception ignored) {}
    }


    private void fillFields() {
        editName.setText(ex.getName());
        editMinW.setText(String.valueOf(ex.getMinWeight()));
        editMaxW.setText(String.valueOf(ex.getMaxWeight()));
        editStepW.setText(String.valueOf(ex.getWeightStep()));

        editSetsMin.setText(String.valueOf(ex.getSetsMin()));
        editSetsMax.setText(String.valueOf(ex.getSetsMax()));

        editRepsMin.setText(String.valueOf(ex.getRepsMin()));
        editRepsMax.setText(String.valueOf(ex.getRepsMax()));
        editRepsStep.setText(String.valueOf(ex.getRepsStep()));
    }

    private void filterMuscles(String query) {
        List<Muscle> filtered = allMuscles.stream()
                .filter(m -> m.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        adapter.updateList(filtered);
    }

    private void save() {
        try {
            String name = editName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Name darf nicht leer sein", Toast.LENGTH_SHORT).show();
                return;
            }

            float minW = Float.parseFloat(editMinW.getText().toString());
            float maxW = Float.parseFloat(editMaxW.getText().toString());
            float stepW = Float.parseFloat(editStepW.getText().toString());

            int setsMin = Integer.parseInt(editSetsMin.getText().toString());
            int setsMax = Integer.parseInt(editSetsMax.getText().toString());

            int repsMin = Integer.parseInt(editRepsMin.getText().toString());
            int repsMax = Integer.parseInt(editRepsMax.getText().toString());
            int repsStep = Integer.parseInt(editRepsStep.getText().toString());

            if (minW <= 0 || maxW <= 0 || stepW <= 0) {
                Toast.makeText(getContext(), "Gewichte müssen > 0 sein", Toast.LENGTH_SHORT).show();
                return;
            }

            if (setsMin <= 0 || setsMax <= 0) {
                Toast.makeText(getContext(), "Sätze müssen > 0 sein", Toast.LENGTH_SHORT).show();
                return;
            }

            if (repsMin <= 0 || repsMax <= 0 || repsStep <= 0) {
                Toast.makeText(getContext(), "Wiederholungen müssen > 0 sein", Toast.LENGTH_SHORT).show();
                return;
            }

            ex.setName(name);
            ex.setMinWeight(minW);
            ex.setMaxWeight(maxW);
            ex.setWeightStep(stepW);

            ex.setSetsMin(setsMin);
            ex.setSetsMax(setsMax);

            ex.setRepsMin(repsMin);
            ex.setRepsMax(repsMax);
            ex.setRepsStep(repsStep);

            ex.muscleIds.clear();
            ex.muscleIds.addAll(adapter.getSelected());
            db.syncExercise(ex);

            onSaveCallback.run();
            dismiss();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Bitte alle Felder korrekt ausfüllen", Toast.LENGTH_SHORT).show();
        }
    }

}
