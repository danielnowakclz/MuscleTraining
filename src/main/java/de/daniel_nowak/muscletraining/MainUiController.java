package de.daniel_nowak.muscletraining;

import android.graphics.drawable.GradientDrawable;
import android.widget.AdapterView;
import android.widget.SeekBar;

import de.daniel_nowak.muscletraining.logic.RecommendationService;
import de.daniel_nowak.muscletraining.ui.MuscleRegenView;

public class MainUiController {

    private final MainActivity a;

    public MainUiController(MainActivity activity) {
        this.a = activity;
    }

    public void setupButtons() {

        a.findViewById(R.id.btn_front).setOnClickListener(v -> {
            a.regenView.setSide(MuscleRegenView.Side.FRONT);
            a.highlightFront();
            a.updater.update();
        });

        a.findViewById(R.id.btn_back).setOnClickListener(v -> {
            a.regenView.setSide(MuscleRegenView.Side.BACK);
            a.highlightBack();
            a.updater.update();
        });

        a.btnSetsMinus.setOnClickListener(v -> {
            a.adjustSets(false);
            a.updater.update();
        });

        a.btnSetsPlus.setOnClickListener(v -> {
            a.adjustSets(true);
            a.updater.update();
        });

        a.btnRepsMinus.setOnClickListener(v -> {
            a.adjustReps(false);
            a.updater.update();
        });

        a.btnRepsPlus.setOnClickListener(v -> {
            a.adjustReps(true);
            a.updater.update();
        });

        a.btnWeightMinus.setOnClickListener(v -> {
            a.adjustWeight(false);
            a.updater.update();
        });

        a.btnWeightPlus.setOnClickListener(v -> {
            a.adjustWeight(true);
            a.updater.update();
        });

        a.btnSave.setOnClickListener(v -> a.saveTraining());
    }

    public void setupDifficultySlider() {

        a.seekDifficulty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {

                a.selectedDifficulty = value;

                switch (value) {
                    case 0:
                        a.txtDifficultyLabel.setText("Leicht");
                        break;
                    case 1:
                        a.txtDifficultyLabel.setText("Angenehm");
                        break;
                    case 2:
                        a.txtDifficultyLabel.setText("Schwer");
                        break;
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    public void setupSeekbar() {

        // Farbverlauf für die ETL-Skala
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF4CAF50, 0xFFFFEB3B, 0xFFF44336}
        );
        gradient.setCornerRadius(20f);
        a.seekETL.setProgressDrawable(gradient);

        a.seekETL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // Sicherheit: keine Daten → nichts tun
                if (a.etlCombos == null || a.etlCombos.isEmpty()) return;
                if (progress < 0 || progress >= a.etlCombos.size()) return;

                RecommendationService.Recommendation c = a.etlCombos.get(progress);

                // Eingabefelder NUR aktualisieren, wenn der User bewegt
                if (fromUser) {
                    a.inputSets.setText(String.valueOf(c.sets));
                    a.inputReps.setText(String.valueOf(c.reps));
                    a.inputWeight.setText(a.formatWeight(c.weight));
                }

                // Intensität IMMER aktualisieren
                a.updateIntensityLabel(c.etl);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    public void setupSpinner() {
        a.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                a.updater.update();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
