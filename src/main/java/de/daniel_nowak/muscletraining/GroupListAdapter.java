package de.daniel_nowak.muscletraining;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.daniel_nowak.muscletraining.data.Database;
import de.daniel_nowak.muscletraining.model.Group;

public class GroupListAdapter extends BaseAdapter {

    private final Context ctx;
    private final Database db;
    private final List<Group> groups;
    private final List<String> selectedGroupIds;

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Group getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public GroupListAdapter(Context ctx, Database db, List<Group> groups, List<String> selectedGroupIds) {
        this.ctx = ctx;
        this.db = db;
        this.groups = groups;
        this.selectedGroupIds = selectedGroupIds;
    }

    @Override
    public View getView(int pos, View convert, ViewGroup parent) {
        if (convert == null) {
            convert = LayoutInflater.from(ctx).inflate(R.layout.item_group, parent, false);
        }

        Group g = groups.get(pos);

        TextView name = convert.findViewById(R.id.text_group_name);
        CheckBox check = convert.findViewById(R.id.check_group);

        name.setText(g.getName());

        // 1) Listener entfernen, damit setChecked() keinen falschen Zustand speichert
        check.setOnCheckedChangeListener(null);

        // 2) Checked-State korrekt setzen
        boolean isSelected = selectedGroupIds.contains(g.getId());
        check.setChecked(isSelected);

        // 3) Listener wieder setzen
        check.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                if (!selectedGroupIds.contains(g.getId()))
                    selectedGroupIds.add(g.getId());
            } else {
                selectedGroupIds.remove(g.getId());
            }

            // Sofort speichern
            db.plan.selectedGroupIds.clear();
            db.plan.selectedGroupIds.addAll(selectedGroupIds);
            db.plan.save();
        });

        return convert;
    }

}
