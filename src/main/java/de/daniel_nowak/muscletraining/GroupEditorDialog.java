package de.daniel_nowak.muscletraining;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import de.daniel_nowak.muscletraining.model.Group;

public class GroupEditorDialog extends Dialog {

    private final Database db;
    private final Group group;
    private final Runnable onSaveCallback;

    private EditText editName;
    private EditText editSearchEx;
    private RecyclerView recycler;

    private GroupExerciseSelectAdapter adapter;
    private List<Exercise> allExercises;

    public GroupEditorDialog(Context ctx, Database db, Group group, Runnable onSaveCallback) {
        super(ctx);
        this.db = db;
        this.group = group;
        this.onSaveCallback = onSaveCallback;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.dialog_group_editor);

        getWindow().setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        editName = findViewById(R.id.edit_name);
        editSearchEx = findViewById(R.id.edit_search_ex);
        recycler = findViewById(R.id.recycler_exercises);

        // Name setzen
        editName.setText(group.getName());

        // Übungen laden
        allExercises = new ArrayList<>(db.exercises.exercises.values());
        Collections.sort(allExercises, Comparator.comparing(Exercise::getName));

        // Adapter: bekommt alle Übungen + die IDs der Übungen, die in dieser Gruppe sind
        adapter = new GroupExerciseSelectAdapter(allExercises, group.exerciseIds);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        // Buttons
        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> save());

        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> {
            onSaveCallback.run();
            dismiss();
        });

        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            db.groups.groups.remove(group.getId());
            db.groups.save();
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

    private void save() {
        String name = editName.getText().toString().trim();
        if (name.isEmpty()) return;

        // Name speichern
        group.name = name;

        // Übungen speichern
        group.exerciseIds.clear();
        group.exerciseIds.addAll(adapter.getSelected());

        // alte Zuordnung entfernen
        for (Exercise ex : db.exercises.exercises.values()) {
            ex.groupIds.remove(group.id);
        }

        // neue Zuordnung setzen
        for (String exId : group.exerciseIds) {
            Exercise ex = db.exercises.exercises.get(exId);
            if (ex != null && !ex.groupIds.contains(group.id)) {
                ex.groupIds.add(group.id);
            }
        }

        db.exercises.save();
        db.groups.save();

        onSaveCallback.run();
        dismiss();
    }
}
