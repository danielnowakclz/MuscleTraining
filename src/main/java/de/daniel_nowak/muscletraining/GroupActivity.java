package de.daniel_nowak.muscletraining;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.daniel_nowak.muscletraining.data.Database;
import de.daniel_nowak.muscletraining.model.Group;

public class GroupActivity extends BaseActivity {

    private ListView listGroups;
    private GroupListAdapter adapter;

    private List<String> selectedGroupIds;
    private List<Group> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        setupToolbar(R.id.toolbar);
        applyOrientationLayout();

        listGroups = findViewById(R.id.list_groups);

        // Auswahl laden
        selectedGroupIds = new ArrayList<>(db.plan.selectedGroupIds);

        // Gruppen laden
        groups = new ArrayList<>(db.groups.groups.values());

        adapter = new GroupListAdapter(this, db, groups, selectedGroupIds);
        listGroups.setAdapter(adapter);

        // Klick auf Gruppe → Editor öffnen
        listGroups.setOnItemClickListener((parent, view, pos, id) -> {
            Group g = groups.get(pos);

            GroupEditorDialog dlg = new GroupEditorDialog(
                    this,
                    db,
                    g,
                    () -> {
                        db.groups.save();
                        reload();
                    }
            );
            dlg.show();
        });

        // Neue Gruppe
        Button btnAdd = findViewById(R.id.btn_add_group);
        btnAdd.setOnClickListener(v -> {
            Group g = new Group(UUID.randomUUID().toString(), "Neue Gruppe");

            db.groups.groups.put(g.getId(), g);
            db.groups.save();

            GroupEditorDialog dlg = new GroupEditorDialog(
                    this,
                    db,
                    g,
                    () -> {
                        db.groups.save();
                        reload();
                    }
            );
            dlg.show();
        });
    }

    private void reload() {
        groups = new ArrayList<>(db.groups.groups.values());
        selectedGroupIds = new ArrayList<>(db.plan.selectedGroupIds);

        adapter = new GroupListAdapter(this, db, groups, selectedGroupIds);
        listGroups.setAdapter(adapter);
    }
}
