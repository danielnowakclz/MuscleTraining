package de.daniel_nowak.muscletraining;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.daniel_nowak.muscletraining.data.Database;
import de.daniel_nowak.muscletraining.model.Exercise;
import de.daniel_nowak.muscletraining.model.Muscle;
import de.daniel_nowak.muscletraining.ui.MuscleBodyView;

public class MuscleEditorDialog extends Dialog {

    private final Database db;
    private final Muscle muscle;
    private final Runnable onSaveCallback;

    private EditText editName;
    private EditText editSearchEx;
    private RecyclerView recycler;

    private MuscleExerciseSelectAdapter adapter;
    private List<Exercise> allExercises;

    private MuscleBodyView bodyView;
    private Button btnFront, btnBack;

    private Spinner spinnerCategory;

    private static final Map<Muscle.Category, String> CATEGORY_LABELS_DE = new HashMap<>();
    private static final Map<String, Muscle.Category> CATEGORY_FROM_LABEL = new HashMap<>();

    static {
        CATEGORY_LABELS_DE.put(Muscle.Category.ARM, "Arme");
        CATEGORY_LABELS_DE.put(Muscle.Category.SHOULDER, "Schultern");
        CATEGORY_LABELS_DE.put(Muscle.Category.CHEST, "Brust");
        CATEGORY_LABELS_DE.put(Muscle.Category.BACK, "Rücken");
        CATEGORY_LABELS_DE.put(Muscle.Category.CORE, "Rumpf");
        CATEGORY_LABELS_DE.put(Muscle.Category.LEG, "Beine");

        for (Map.Entry<Muscle.Category, String> e : CATEGORY_LABELS_DE.entrySet()) {
            CATEGORY_FROM_LABEL.put(e.getValue(), e.getKey());
        }
    }

    public MuscleEditorDialog(Context ctx, Database db, Muscle muscle, Runnable onSaveCallback) {
        super(ctx);
        this.db = db;
        this.muscle = muscle;
        this.onSaveCallback = onSaveCallback;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.dialog_muscle_editor);

        getWindow().setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        editName = findViewById(R.id.edit_name);
        spinnerCategory = findViewById(R.id.spinner_category);
        editSearchEx = findViewById(R.id.edit_search_ex);
        recycler = findViewById(R.id.recycler_exercises);
        bodyView = findViewById(R.id.view_body);
        btnFront = findViewById(R.id.btn_front);
        btnBack = findViewById(R.id.btn_back);

        // Kategorien laden
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                CATEGORY_LABELS_DE.values().toArray(new String[0])
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        String currentLabel = CATEGORY_LABELS_DE.get(muscle.category);
        int index = new ArrayList<>(CATEGORY_LABELS_DE.values()).indexOf(currentLabel);
        spinnerCategory.setSelection(index);

        // Übungen laden
        allExercises = new ArrayList<>(db.exercises.exercises.values());
        Collections.sort(allExercises, Comparator.comparing(Exercise::getName));

        adapter = new MuscleExerciseSelectAdapter(allExercises, muscle.exerciseIds);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        editName.setText(muscle.getName());

        // Seite setzen
        MuscleBodyView.Side initialSide = MuscleBodyView.Side.FRONT;

        if (!muscle.sideList.isEmpty() && "back".equals(muscle.sideList.get(0))) {
            initialSide = MuscleBodyView.Side.BACK;
            highlightBack();
        } else {
            highlightFront();
        }

        bodyView.setSide(initialSide);

        // Marker erst setzen, wenn Layout fertig ist
        bodyView.post(() -> {
            List<MuscleBodyView.Marker> markers = new ArrayList<>();
            for (int i = 0; i < muscle.posXList.size(); i++) {
                markers.add(new MuscleBodyView.Marker(
                        muscle.posXList.get(i),
                        muscle.posYList.get(i),
                        muscle.sideList.get(i)
                ));
            }
            bodyView.setMarkers(markers);
        });


        // Buttons Front/Back
        btnFront.setOnClickListener(v -> {
            bodyView.setSide(MuscleBodyView.Side.FRONT);
            highlightFront();
        });

        btnBack.setOnClickListener(v -> {
            bodyView.setSide(MuscleBodyView.Side.BACK);
            highlightBack();
        });

        // Save
        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> save());

        // Cancel
        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> {
            onSaveCallback.run();
            dismiss();
        });

        // Delete
        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            db.delMuscle(muscle.getId());
            onSaveCallback.run();
            dismiss();
        });

        // Suche
        editSearchEx.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void highlightFront() {
        btnFront.setAlpha(1f);
        btnBack.setAlpha(0.5f);
    }

    private void highlightBack() {
        btnFront.setAlpha(0.5f);
        btnBack.setAlpha(1f);
    }



    private void save() {
        String name = editName.getText().toString().trim();
        if (name.isEmpty()) return;

        muscle.setName(name);

        String label = spinnerCategory.getSelectedItem().toString();
        muscle.category = CATEGORY_FROM_LABEL.get(label);

        muscle.exerciseIds.clear();
        muscle.exerciseIds.addAll(adapter.getSelected());

        List<MuscleBodyView.Marker> mks = bodyView.getMarkers();

        muscle.posXList.clear();
        muscle.posYList.clear();
        muscle.sideList.clear();

        for (MuscleBodyView.Marker m : mks) {
            muscle.posXList.add(m.xNorm);
            muscle.posYList.add(m.yNorm);
            muscle.sideList.add(m.side);
        }

        db.syncMuscle(muscle);

        onSaveCallback.run();
        dismiss();
    }
}
