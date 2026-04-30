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

import de.daniel_nowak.muscletraining.model.Muscle;

public class MuscleActivity extends BaseActivity {

    private ListView listView;
    private Button btnAdd;

    private ArrayAdapter<Muscle> adapter;
    private List<Muscle> muscleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muscles);

        // ⭐ Einheitliche Toolbar-ID
        setupToolbar(R.id.toolbar);
        applyEdgeToEdge();

        listView = findViewById(R.id.list_muscles);
        btnAdd = findViewById(R.id.btn_add);

        loadMuscles();

        // ⭐ Editor öffnen
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Muscle m = muscleList.get(position);
            openEditor(m);
        });

        // ⭐ Info-Dialog per Long-Click
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Muscle m = muscleList.get(position);
            new MuscleInfoDialog(this, db, m).show();
            return true;
        });

        // ⭐ Neuer Muskel
        btnAdd.setOnClickListener(v -> {
            String id = UUID.randomUUID().toString();
            db.muscles.add(id, "Neuer Muskel");
            Muscle m = db.muscles.muscles.get(id);
            openEditor(m);
        });
    }

    private void openEditor(Muscle m) {
        new MuscleEditorDialog(this, db, m, this::loadMuscles).show();
    }

    public void loadMuscles() {
        muscleList = new ArrayList<>(db.muscles.muscles.values());
        Collections.sort(muscleList, Comparator.comparing(Muscle::getName));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, muscleList);
        listView.setAdapter(adapter);

        // ⭐ Optional: Scroll nach oben
        listView.post(() -> listView.setSelection(0));
    }

    @Override
    protected void onMenuRefresh() {
        loadMuscles();
    }
}
