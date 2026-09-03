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

public class GroupExerciseSelectAdapter extends RecyclerView.Adapter<GroupExerciseSelectAdapter.ViewHolder> {

    private final List<Exercise> allExercises;
    private final List<Exercise> filteredExercises;
    private final List<String> selected;

    public GroupExerciseSelectAdapter(List<Exercise> allExercises, List<String> selectedIds) {
        this.allExercises = new ArrayList<>(allExercises);
        this.filteredExercises = new ArrayList<>(allExercises);
        this.selected = new ArrayList<>(selectedIds);
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
        Exercise ex = filteredExercises.get(pos);

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
        return filteredExercises.size();
    }

    public List<String> getSelected() {
        return selected;
    }

    public void filter(String query) {
        query = query.toLowerCase().trim();
        filteredExercises.clear();

        if (query.isEmpty()) {
            filteredExercises.addAll(allExercises);
        } else {
            for (Exercise ex : allExercises) {
                if (ex.getName().toLowerCase().contains(query)) {
                    filteredExercises.add(ex);
                }
            }
        }

        notifyDataSetChanged();
    }
}
