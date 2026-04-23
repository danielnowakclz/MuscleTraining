package de.daniel_nowak.muscletraining;

import android.os.Bundle;
import android.widget.*;

import java.util.*;

import de.daniel_nowak.muscletraining.data.Database;
import de.daniel_nowak.muscletraining.model.Exercise;

public class ExerciseActivity extends BaseActivity {

    private ListView listView;
    private Button btnAdd, btnDelete, btnEdit;

    private ArrayAdapter<Exercise> adapter;
    private List<Exercise> exerciseList = new ArrayList<>();
    private Exercise selected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        setupToolbar(R.id.toolbar_ex);
        applyEdgeToEdge();

        listView = findViewById(R.id.list_exercises);
        btnAdd = findViewById(R.id.btn_add);
        btnDelete = findViewById(R.id.btn_delete);
        btnEdit = findViewById(R.id.btn_edit);

        loadExercises();

        listView.setOnItemClickListener((parent, view, position, id) ->
                selected = exerciseList.get(position));

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            openEditor(exerciseList.get(position));
            return true;
        });

        btnAdd.setOnClickListener(v -> {
            Exercise ex = db.exercises.add(UUID.randomUUID().toString(), "Neue Übung");
            openEditor(ex);
        });

        btnEdit.setOnClickListener(v -> {
            if (selected != null) openEditor(selected);
        });

        btnDelete.setOnClickListener(v -> {
            if (selected != null) {
                db.exercises.delete(selected.getId());
                loadExercises();
                selected = null;
            }
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
    }

    @Override
    protected void onMenuRefresh() {
        loadExercises();
    }
}

