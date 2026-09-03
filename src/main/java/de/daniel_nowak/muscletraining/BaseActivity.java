package de.daniel_nowak.muscletraining;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.daniel_nowak.muscletraining.data.Database;
import de.daniel_nowak.muscletraining.model.Exercise;
import de.daniel_nowak.muscletraining.model.Muscle;
import de.daniel_nowak.muscletraining.ui.MuscleRegenView;

public abstract class BaseActivity extends AppCompatActivity {

    protected Database db;

    protected Set<String> selectedExercises = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this);

        selectedExercises.addAll(db.plan.plan);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // WICHTIG: Trainingsplan NICHT erzeugen, wenn wir in der GroupActivity sind
        if (this instanceof GroupActivity) {
            return;
        }

        float ageHours = hoursSince(db.plan.lastPlanTime);

        if (db.plan.plan.isEmpty() || ageHours > 6f) {
            createTrainingPlan();
            db.plan.setPlan(selectedExercises);
        }
    }


    protected void setupToolbar(int toolbarId) {
        MaterialToolbar toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);

        if (toolbar != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                toolbar.setOutlineAmbientShadowColor(Color.WHITE);
                toolbar.setOutlineSpotShadowColor(Color.WHITE);
            }

        }

        // automatisch richtigen Subtitle setzen
        applyAutoSubtitle();
    }

    protected void applyAutoSubtitle() {
        String name = this.getClass().getSimpleName();

        switch (name) {
            case "MainActivity":
                setToolbarSubtitle(getString(R.string.subtitle_training));
                break;
            case "ExerciseActivity":
                setToolbarSubtitle(getString(R.string.subtitle_exercises));
                break;
            case "MuscleActivity":
                setToolbarSubtitle(getString(R.string.subtitle_muscles));
                break;
            case "GroupActivity":
                setToolbarSubtitle(getString(R.string.subtitle_groups));
                break;
            default:
                setToolbarSubtitle("");
                break;
        }
    }

    protected void setToolbarSubtitle(String text) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) return;

        toolbar.setSubtitle(text);

        toolbar.setSubtitleTextAppearance(this, R.style.ToolbarSubtitleStyle);

        try {
            toolbar.setSubtitleCentered(true);
        } catch (Exception ignored) {
        }
    }

    private boolean isLandscape() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = getWindowManager().getCurrentWindowMetrics();
            int width = metrics.getBounds().width();
            int height = metrics.getBounds().height();
            return width > height;
        } else {
            return getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE;
        }
    }


    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyOrientationLayout();
    }


    protected void applyOrientationLayout() {
        View container = findViewById(R.id.container);
        if (container == null) return;

        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) container.getLayoutParams();

        if (isLandscape()) {
            params.leftMargin = dp(48);
            params.rightMargin = dp(48);
        } else {
            params.leftMargin = dp(12);
            params.rightMargin = dp(12);
        }

        container.setLayoutParams(params);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_training) {
            navigateTo(MainActivity.class);
            return true;
        }

        if (id == R.id.menu_exercises) {
            navigateTo(ExerciseActivity.class);
            return true;
        }

        if (id == R.id.menu_muscles) {
            navigateTo(MuscleActivity.class);
            return true;
        }

        if (id == R.id.menu_create_training) {
            createTrainingPlan();
            db.plan.setPlan(selectedExercises);
            Toast.makeText(this, R.string.toast_training_created, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.menu_grouping) {
            navigateTo(GroupActivity.class);
            return true;
        }

        if (id == R.id.menu_delete_training) {
            selectedExercises.clear();
            db.plan.clear();
            onMenuRefresh();
            Toast.makeText(this, R.string.toast_training_deleted, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.menu_export_xml) {
            exportXmlDialog();
            return true;
        }

        if (id == R.id.menu_import_xml) {
            importXmlDialog();
            return true;
        }


        if (id == R.id.menu_add_demo) {
            addDemoData();
            onMenuRefresh();
            return true;
        }

        if (id == R.id.menu_delete_all) {
            deleteAllData();
            onMenuRefresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportXmlDialog() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/xml");
        intent.putExtra(Intent.EXTRA_TITLE, "muscletraining_backup.xml");
        startActivityForResult(intent, 1001);
    }

    private void importXmlDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/xml");
        startActivityForResult(intent, 1002);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == 1001) {
            exportXmlToUri(data.getData());
        }

        if (requestCode == 1002) {
            importXmlFromUri(data.getData());
        }
    }

    private void exportXmlToUri(android.net.Uri uri) {
        try {
            String xml = db.exportToXml();
            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                os.write(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            Toast.makeText(this, R.string.toast_export_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_export_error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void navigateTo(Class<?> target) {
        if (!this.getClass().equals(target)) {
            startActivity(new Intent(this, target));
        } else {
            onMenuRefresh();
        }
    }

    private void importXmlFromUri(android.net.Uri uri) {
        try {
            try (InputStream is = getContentResolver().openInputStream(uri)) {
                db.importFromXml(is);
            }
            Toast.makeText(this, R.string.toast_import_success, Toast.LENGTH_SHORT).show();
            onMenuRefresh();
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_import_error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    public String formatWeight(float w) {
        String s = String.format(java.util.Locale.US, "%.1f", w);

        if (s.endsWith(".0")) {
            s = s.substring(0, s.length() - 2);
        }

        return s;
    }

    public void addDemoData() {
        db.addDemoData();
        db.muscles.load();
        db.exercises.load();
        db.trainings.load();

        onMenuRefresh();
        Toast.makeText(this, R.string.toast_demo_data_added, Toast.LENGTH_SHORT).show();
    }

    public void deleteAllData() {
        File dir = getFilesDir();

        new File(dir, "muscles.db").delete();
        new File(dir, "exercises.db").delete();
        new File(dir, "trainings.db").delete();
        new File(dir, "plan.db").delete();

        db.muscles.load();
        db.exercises.load();
        db.trainings.load();
        db.plan.load();

        selectedExercises.clear();

        if (this instanceof MainActivity) {
            MuscleRegenView regenView = findViewById(R.id.view_regen);
            if (regenView != null) {
                regenView.setMuscles(new HashMap<>());
                regenView.setRegenData(new HashMap<>());
                regenView.invalidate();
            }
        }


        onMenuRefresh();
        Toast.makeText(this, R.string.toast_all_data_deleted, Toast.LENGTH_SHORT).show();
    }

    protected void onMenuRefresh() {
    }

    protected void createTrainingPlan() {

        // ---------------------------------------------------------
        // 0) Gruppenfilter anwenden
        // ---------------------------------------------------------
        Set<String> allowedGroups = new HashSet<>(db.plan.selectedGroupIds);

        // Wenn Gruppen gewählt wurden → nur Übungen aus diesen Gruppen behalten
        if (!allowedGroups.isEmpty()) {

            db.exercises.exercises.values().removeIf(ex -> {

                // Übung hat keine Gruppen → raus
                if (ex.groupIds == null || ex.groupIds.isEmpty()) {
                    return true;
                }

                // Übung hat Gruppen → mindestens eine muss erlaubt sein
                for (String gId : ex.groupIds) {
                    if (allowedGroups.contains(gId)) {
                        return false; // behalten
                    }
                }

                return true; // keine erlaubte Gruppe → raus
            });
        }

        // ---------------------------------------------------------
        // 1) ExercisePool aufbauen
        // ---------------------------------------------------------
        List<Exercise> exercisePool = new ArrayList<>();
        Set<String> poolIds = new HashSet<>();

        // 1) Alle Muskeln nach Regeneration sortieren (älteste zuerst)
        List<Muscle> sortedMuscles = new ArrayList<>(db.muscles.muscles.values());
        sortedMuscles.sort((a, b) -> Long.compare(a.getFullRegeneratedTime(), b.getFullRegeneratedTime()));

        // 2) Für jeden Muskel Übungen sammeln
        for (Muscle muscle : sortedMuscles) {

            List<Exercise> tempPool = new ArrayList<>();

            for (String exId : muscle.exerciseIds) {
                Exercise ex = db.exercises.exercises.get(exId);
                if (ex != null) {
                    tempPool.add(ex);
                }
            }

            // Übungen nach Rotation sortieren
            tempPool.sort((a, b) -> Long.compare(a.getLastTraining(), b.getLastTraining()));

            // In globalen Pool übernehmen (ohne Duplikate)
            for (Exercise ex : tempPool) {
                if (!poolIds.contains(ex.getId())) {
                    poolIds.add(ex.getId());
                    exercisePool.add(ex);
                }
            }
        }

        // Wenn keine Übungen verfügbar sind → Plan bleibt leer
        if (exercisePool.isEmpty()) {
            selectedExercises.clear();
            db.plan.setPlan(selectedExercises);
            onMenuRefresh();
            return;
        }

        // ---------------------------------------------------------
        // 3) Finalen Trainingsplan erzeugen
        // ---------------------------------------------------------
        List<Exercise> finalPlan = new ArrayList<>();
        Set<String> usedMuscles = new HashSet<>();

        while (!exercisePool.isEmpty()) {

            // 3.1) Älteste Übung auswählen
            Exercise oldest = exercisePool.get(0);
            finalPlan.add(oldest);

            // 3.2) Muskeln dieser Übung merken
            for (String muscleId : oldest.muscleIds) {
                usedMuscles.add(muscleId);
            }

            // 3.3) Alle Übungen entfernen, die einen dieser Muskeln trainieren
            exercisePool.removeIf(ex -> {
                for (String mId : ex.muscleIds) {
                    if (usedMuscles.contains(mId)) {
                        return true;
                    }
                }
                return false;
            });
        }

        // ---------------------------------------------------------
        // 4) Finalen Plan speichern
        // ---------------------------------------------------------
        selectedExercises.clear();
        for (Exercise ex : finalPlan) {
            selectedExercises.add(ex.getId());
        }

        db.plan.setPlan(selectedExercises);
        onMenuRefresh();
    }
    private float hoursSince(long time) {
        if (time <= 0L) return 9999f;
        long diff = System.currentTimeMillis() - time;
        return diff / 1000f / 60f / 60f;
    }


}
