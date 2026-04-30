package de.daniel_nowak.muscletraining;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import de.daniel_nowak.muscletraining.model.Exercise;

public class ExerciseActivity extends BaseActivity {

    private ListView listView;
    private Button btnAdd;

    private ArrayAdapter<Exercise> adapter;
    private List<Exercise> exerciseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        // ⭐ Einheitliche Toolbar-ID
        setupToolbar(R.id.toolbar);
        applyEdgeToEdge();

        listView = findViewById(R.id.list_exercises);
        btnAdd = findViewById(R.id.btn_add);

        loadExercises();

        // ⭐ Editor öffnen
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Exercise ex = exerciseList.get(position);
            openEditor(ex);
        });

        // ⭐ Neue Übung
        btnAdd.setOnClickListener(v -> {
            String id = UUID.randomUUID().toString();
            db.exercises.add(id, "Neue Übung");
            Exercise ex = db.exercises.exercises.get(id);
            openEditor(ex);
        });
    }

    private void openEditor(Exercise ex) {
        new ExerciseEditorDialog(this, db, ex, this::loadExercises).show();
    }

    public void loadExercises() {
        exerciseList = new ArrayList<>(db.exercises.exercises.values());
        Collections.sort(exerciseList, Comparator.comparing(Exercise::getName));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, exerciseList);
        listView.setAdapter(adapter);

        // ⭐ Optional: Scroll nach oben
        listView.post(() -> listView.setSelection(0));
    }

    @Override
    protected void onMenuRefresh() {
        loadExercises();
    }
}
