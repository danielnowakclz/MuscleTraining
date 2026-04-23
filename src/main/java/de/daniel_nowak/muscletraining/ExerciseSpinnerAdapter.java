package de.daniel_nowak.muscletraining;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import de.daniel_nowak.muscletraining.model.Exercise;

public class ExerciseSpinnerAdapter extends ArrayAdapter<Exercise> {

    private final Set<String> selected;

    public ExerciseSpinnerAdapter(Context ctx, List<Exercise> list, Set<String> selected) {
        super(ctx, android.R.layout.simple_spinner_item, list);
        this.selected = selected;
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getDropDownView(int pos, View convertView, ViewGroup parent) {
        TextView v = (TextView) super.getDropDownView(pos, convertView, parent);
        Exercise ex = getItem(pos);

        if (selected.contains(ex.getId())) {
            v.setTextColor(0xFF008000); // Grün
        } else {
            v.setTextColor(0xFF000000); // Schwarz
        }

        return v;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        TextView v = (TextView) super.getView(pos, convertView, parent);
        Exercise ex = getItem(pos);

        if (selected.contains(ex.getId())) {
            v.setTextColor(0xFF008000);
        } else {
            v.setTextColor(0xFF000000);
        }

        return v;
    }
}
