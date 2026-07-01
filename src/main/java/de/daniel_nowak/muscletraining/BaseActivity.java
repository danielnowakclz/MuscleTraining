package de.daniel_nowak.muscletraining;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowMetrics;
import android.widget.LinearLayout;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

        selectedExercises.clear();

        List<Exercise> pool = new ArrayList<>(db.exercises.exercises.values());

        Muscle.Category[] targetCats = {
                Muscle.Category.ARM,
                Muscle.Category.SHOULDER,
                Muscle.Category.CHEST,
                Muscle.Category.BACK,
                Muscle.Category.CORE,
                Muscle.Category.LEG
        };

        Set<Muscle.Category> usedCategories = new HashSet<>();

        for (Muscle.Category targetCat : targetCats) {

            Exercise best = null;
            float bestScore = -1f;

            for (Exercise ex : pool) {

                Set<Muscle.Category> cats = ex.muscleIds.stream()
                        .map(id -> db.muscles.muscles.get(id))
                        .filter(Objects::nonNull)
                        .map(m -> m.category)
                        .collect(Collectors.toSet());

                if (!cats.contains(targetCat)) continue;

                if (cats.stream().anyMatch(usedCategories::contains)) continue;

                float regen = ex.muscleIds.stream()
                        .map(id -> db.calculateRegeneration(id) * 100f)
                        .min(Float::compare)
                        .orElse(100f);

                long muscleLast = ex.muscleIds.stream()
                        .map(id -> db.muscles.muscles.get(id))
                        .filter(Objects::nonNull)
                        .mapToLong(Muscle::getLastTraining)
                        .min()
                        .orElse(0L);

                float hoursMuscle = hoursSince(muscleLast);
                float muscleRotationScore = Math.min(100f, (hoursMuscle / 72f) * 100f);

                float lastVolume = ex.getLastVolume();
                float maxVolume = db.getMaxVolumeForCategory(targetCat);

                float volumeScore = (maxVolume <= 0f)
                        ? 100f
                        : Math.max(0f, 100f - (lastVolume / maxVolume * 100f));

                long exerciseLast = ex.getLastTraining();
                float hoursEx = hoursSince(exerciseLast);
                float exerciseRotationScore = Math.min(100f, (hoursEx / 168f) * 100f);

                float score =
                        regen * 0.40f +
                                muscleRotationScore * 0.20f +
                                volumeScore * 0.20f +
                                exerciseRotationScore * 0.20f;

                if (score > bestScore) {
                    bestScore = score;
                    best = ex;
                }
            }

            if (best != null) {

                selectedExercises.add(best.getId());

                best.muscleIds.stream()
                        .map(id -> db.muscles.muscles.get(id))
                        .filter(Objects::nonNull)
                        .map(m -> m.category)
                        .forEach(usedCategories::add);

                Set<String> primaryMuscles = new HashSet<>(best.muscleIds);

                Exercise finalBest = best;
                pool.removeIf(ex ->
                        ex.getId().equals(finalBest.getId()) ||
                                ex.muscleIds.stream().anyMatch(primaryMuscles::contains)
                );
            }
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
