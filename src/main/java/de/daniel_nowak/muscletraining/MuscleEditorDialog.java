package de.daniel_nowak.muscletraining;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.daniel_nowak.muscletraining.data.Database;
import de.daniel_nowak.muscletraining.model.Exercise;
import de.daniel_nowak.muscletraining.model.Muscle;

public class MuscleEditorDialog extends Dialog {

    private final Database db;
    private final Muscle muscle;
    private final Runnable onSaveCallback;

    private EditText editName;
    private RecyclerView recycler;
    private MuscleExerciseSelectAdapter adapter;

    private List<Exercise> allExercises;

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
        recycler = findViewById(R.id.recycler_exercises);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        allExercises = new ArrayList<>(db.exercises.exercises.values());
        Collections.sort(allExercises, Comparator.comparing(Exercise::getName));

        adapter = new MuscleExerciseSelectAdapter(allExercises, muscle.exerciseIds);
        recycler.setAdapter(adapter);

        editName.setText(muscle.getName());

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> save());

        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dismiss());

        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            db.delMuscle(muscle.getId());
            onSaveCallback.run();
            dismiss();
        });
    }

    private void save() {
        String name = editName.getText().toString().trim();
        if (name.isEmpty()) return;

        muscle.setName(name);

        muscle.exerciseIds.clear();
        muscle.exerciseIds.addAll(adapter.getSelected());

        db.syncMuscle(muscle);

        onSaveCallback.run();
        dismiss();
    }
}
