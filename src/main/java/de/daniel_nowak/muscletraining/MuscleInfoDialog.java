package de.daniel_nowak.muscletraining;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import de.daniel_nowak.muscletraining.data.Database;
import de.daniel_nowak.muscletraining.model.Exercise;
import de.daniel_nowak.muscletraining.model.Muscle;
import de.daniel_nowak.muscletraining.model.Training;

public class MuscleInfoDialog extends Dialog {

    private final Database db;
    private final Muscle muscle;

    public MuscleInfoDialog(Context ctx, Database db, Muscle muscle) {
        super(ctx);
        this.db = db;
        this.muscle = muscle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_muscle_info);

        TextView txtName = findViewById(R.id.txt_muscle_name);
        TextView txtLast = findViewById(R.id.txt_last_trained);
        TextView txtWeekCount = findViewById(R.id.txt_week_count);
        TextView txtWarning = findViewById(R.id.txt_warning);

        ListView listIntensity = findViewById(R.id.list_intensity);
        ListView listTrainings = findViewById(R.id.list_trainings);

        LinearLayout containerVolume = findViewById(R.id.container_volume);

        Button btnOk = findViewById(R.id.btn_ok);

        txtName.setText(muscle.getName());

        // ---------------------------------------------------------
        // Trainings für diesen Muskel
        // ---------------------------------------------------------
        List<Training> trainings = db.trainings.trainings.values().stream()
                .filter(t -> t.muscleIds.contains(muscle.getId()))
                .sorted(Comparator.comparingLong(Training::getTime).reversed())
                .collect(Collectors.toList());

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat sdfShort = new SimpleDateFormat("dd.MM.", Locale.getDefault());

        // ---------------------------------------------------------
        // LETZTES TRAINING (optimiert)
        // ---------------------------------------------------------
        long last = muscle.getLastTraining();

        if (last == 0L) {
            txtLast.setText(R.string.placeholder_dash);
        } else {
            txtLast.setText(sdf.format(new Date(last)));
        }

        // ---------------------------------------------------------
        // TRAININGS DER LETZTEN 7 TAGE
        // ---------------------------------------------------------
        long weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;
        long weekCount = trainings.stream()
                .filter(t -> t.getTime() >= weekAgo)
                .count();
        txtWeekCount.setText(String.valueOf(weekCount));

        // ---------------------------------------------------------
        // WARNUNG / REGENERATION 2.1
        // ---------------------------------------------------------
        if (last == 0L) {
            txtWarning.setText(R.string.placeholder_dash);
            txtWarning.setTextColor(0xFF777777);
        } else {

            float regen = db.calculateRegeneration(muscle.getId()) * 100f;
            int regenInt = Math.round(regen);

            if (regenInt < 40) {
                txtWarning.setText(getContext().getString(R.string.warning_too_early_percent, regenInt));
                txtWarning.setTextColor(0xFFFF4444);
            }
            else if (regenInt < 70) {
                txtWarning.setText(getContext().getString(R.string.warning_caution_percent, regenInt));
                txtWarning.setTextColor(0xFFFFBB33);
            }
            else {
                txtWarning.setText(getContext().getString(R.string.warning_ok_percent, regenInt));
                txtWarning.setTextColor(0xFF99CC00);
            }
        }

        // ---------------------------------------------------------
        // AUTOMATISCHE INTENSITÄTSBERECHNUNG
        // ---------------------------------------------------------
        List<Exercise> exercises = db.exercises.exercises.values().stream()
                .filter(ex -> ex.muscleIds.contains(muscle.getId()))
                .collect(Collectors.toList());

        Map<String, Float> volumeMap = new HashMap<>();

        for (Exercise ex : exercises) {
            float totalVolume = 0f;

            for (Training t : trainings) {
                if (t.getExerciseId().equals(ex.getId())) {
                    totalVolume += t.getSets() * t.getReps() * t.getWeight();
                }
            }

            volumeMap.put(ex.getId(), totalVolume);
        }

        float maxVolume = volumeMap.values().stream()
                .max(Float::compare)
                .orElse(0f);

        exercises.sort((a, b) -> Float.compare(volumeMap.get(b.getId()), volumeMap.get(a.getId())));

        // ---------------------------------------------------------
        // INTENSITÄTSLISTE MIT FARBEN
        // ---------------------------------------------------------
        List<SpannableString> intensityColored = new ArrayList<>();

        for (Exercise ex : exercises) {
            float v = volumeMap.get(ex.getId());
            int percent = (maxVolume == 0f) ? 0 : Math.round((v / maxVolume) * 100f);

            String text = ex.getName() + " [" + percent + "%]";
            SpannableString span = new SpannableString(text);

            int color;
            if (percent >= 70) color = 0xFFFF4444;
            else if (percent >= 40) color = 0xFFFFBB33;
            else color = 0xFF99CC00;

            span.setSpan(new ForegroundColorSpan(color),
                    text.indexOf("["), text.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            intensityColored.add(span);
        }

        ArrayAdapter<SpannableString> intAdapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, intensityColored);
        listIntensity.setAdapter(intAdapter);

        // ---------------------------------------------------------
        // TRAININGSLISTE (kurzform)
        // ---------------------------------------------------------
        List<String> trainingStrings = trainings.stream()
                .map(t -> sdf.format(new Date(t.getTime())) + " — " +
                        t.getSets() + "×" + t.getReps() + " @ " + t.getWeight() + " kg")
                .collect(Collectors.toList());

        ArrayAdapter<String> trAdapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, trainingStrings);
        listTrainings.setAdapter(trAdapter);

        // ---------------------------------------------------------
        // GRAFISCHER VOLUMENVERLAUF (BALKEN)
        // ---------------------------------------------------------
        float maxVol = trainings.stream()
                .map(t -> t.getSets() * t.getReps() * t.getWeight())
                .max(Float::compare)
                .orElse(1f);

        for (Training t : trainings) {

            float vol = t.getSets() * t.getReps() * t.getWeight();
            float ratio = vol / maxVol;

            float density = getContext().getResources().getDisplayMetrics().density;
            int barWidth = (int) (ratio * 200 * density);

            int color;
            if (ratio >= 0.7f) color = 0xFFFF4444;
            else if (ratio >= 0.4f) color = 0xFFFFBB33;
            else color = 0xFF99CC00;

            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView txt = new TextView(getContext());
            txt.setText(sdfShort.format(new Date(t.getTime())));
            txt.setWidth(150);

            View bar = new View(getContext());
            bar.setBackgroundColor(color);
            bar.setLayoutParams(new LinearLayout.LayoutParams(barWidth, 20));

            row.addView(txt);
            row.addView(bar);

            containerVolume.addView(row);
        }

        btnOk.setOnClickListener(v -> dismiss());
    }
}
