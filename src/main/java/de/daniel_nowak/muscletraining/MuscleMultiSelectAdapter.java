package de.daniel_nowak.muscletraining;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.daniel_nowak.muscletraining.model.Muscle;

public class MuscleMultiSelectAdapter extends RecyclerView.Adapter<MuscleMultiSelectAdapter.VH> {

    private List<Muscle> muscles;
    private final Set<String> selected;

    public MuscleMultiSelectAdapter(List<Muscle> muscles, List<String> selectedIds) {
        this.muscles = muscles;
        this.selected = new HashSet<>(selectedIds);
    }

    public static class VH extends RecyclerView.ViewHolder {
        CheckBox check;
        public VH(View v) {
            super(v);
            check = v.findViewById(R.id.check_muscle);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_muscle_select, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Muscle m = muscles.get(pos);

        h.check.setText(m.getName());
        h.check.setChecked(selected.contains(m.getId()));

        h.check.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) selected.add(m.getId());
            else selected.remove(m.getId());
        });
    }

    @Override
    public int getItemCount() {
        return muscles.size();
    }

    public void updateList(List<Muscle> newList) {
        this.muscles = newList;
        notifyDataSetChanged();
    }

    public List<String> getSelected() {
        return new ArrayList<>(selected);
    }
}
