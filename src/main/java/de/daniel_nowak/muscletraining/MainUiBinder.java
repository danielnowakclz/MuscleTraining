package de.daniel_nowak.muscletraining;

public class MainUiBinder {

    private final MainActivity a;

    public MainUiBinder(MainActivity activity) {
        this.a = activity;
    }

    public void bind() {
        a.spinner = a.findViewById(R.id.spinner_exercise);

        a.txtLast = a.findViewById(R.id.txt_last_training);
        a.txtRec = a.findViewById(R.id.txt_recommendation);
        a.txtIntensity = a.findViewById(R.id.txt_intensity);
        a.txtMuscleWarning = a.findViewById(R.id.txt_muscle_warning);
        a.txtRegen = a.findViewById(R.id.txt_regen);

        a.hintSets = a.findViewById(R.id.hint_sets);
        a.hintReps = a.findViewById(R.id.hint_reps);
        a.hintWeight = a.findViewById(R.id.hint_weight);

        a.inputSets = a.findViewById(R.id.input_sets);
        a.inputReps = a.findViewById(R.id.input_reps);
        a.inputWeight = a.findViewById(R.id.input_weight);

        a.btnSave = a.findViewById(R.id.btn_save_training);

        a.btnSetsMinus = a.findViewById(R.id.btn_sets_minus);
        a.btnSetsPlus = a.findViewById(R.id.btn_sets_plus);
        a.btnRepsMinus = a.findViewById(R.id.btn_reps_minus);
        a.btnRepsPlus = a.findViewById(R.id.btn_reps_plus);
        a.btnWeightMinus = a.findViewById(R.id.btn_weight_minus);
        a.btnWeightPlus = a.findViewById(R.id.btn_weight_plus);

        a.regenView = a.findViewById(R.id.view_regen);
        a.seekETL = a.findViewById(R.id.seek_etl);

        a.seekDifficulty = a.findViewById(R.id.seek_difficulty);
        a.txtDifficultyLabel = a.findViewById(R.id.txt_difficulty_label);

    }
}
