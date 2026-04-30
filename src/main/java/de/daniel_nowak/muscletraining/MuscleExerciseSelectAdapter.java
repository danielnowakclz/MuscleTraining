package de.daniel_nowak.muscletraining;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.daniel_nowak.muscletraining.model.Exercise;

public class MuscleExerciseSelectAdapter extends RecyclerView.Adapter<MuscleExerciseSelectAdapter.ViewHolder> {

    private final List<Exercise> originalList;   // unverändert
    private final List<Exercise> displayList;    // gefiltert
    private final List<String> selected;

    public MuscleExerciseSelectAdapter(List<Exercise> exercises, List<String> selected) {
        this.originalList = new ArrayList<>(exercises);
        this.displayList = new ArrayList<>(exercises);
        this.selected = new ArrayList<>(selected);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CheckBox check;

        public ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.text_name);
            check = v.findViewById(R.id.checkbox);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_muscle_exercise, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Exercise ex = displayList.get(pos);

        h.name.setText(ex.getName());
        h.check.setChecked(selected.contains(ex.getId()));

        h.itemView.setOnClickListener(v -> {
            if (selected.contains(ex.getId()))
                selected.remove(ex.getId());
            else
                selected.add(ex.getId());

            notifyItemChanged(pos);
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public List<String> getSelected() {
        return selected;
    }

    // ---------------------------------------------------------
    // FILTER-FUNKTION
    // ---------------------------------------------------------
    public void filter(String query) {
        displayList.clear();

        if (query == null || query.trim().isEmpty()) {
            displayList.addAll(originalList);
        } else {
            String q = query.toLowerCase();
            for (Exercise e : originalList) {
                if (e.getName().toLowerCase().contains(q)) {
                    displayList.add(e);
                }
            }
        }

        notifyDataSetChanged();
    }
}

