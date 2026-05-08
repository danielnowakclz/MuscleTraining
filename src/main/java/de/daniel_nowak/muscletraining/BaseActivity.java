package de.daniel_nowak.muscletraining;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowInsets;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;


import de.daniel_nowak.muscletraining.data.Database;
import de.daniel_nowak.muscletraining.model.Exercise;
import de.daniel_nowak.muscletraining.model.Muscle;
import de.daniel_nowak.muscletraining.model.Training;
import de.daniel_nowak.muscletraining.ui.MuscleRegenView;

public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    protected Database db;

    protected Set<String> selectedExercises = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this);

        // NICHTS mehr mit Trainingsplan hier machen!
        selectedExercises.clear();
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
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setSubtitleTextColor(Color.WHITE);

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

        if (name.equals("MainActivity")) {
            setToolbarSubtitle("Training");
        }
        else if (name.equals("ExerciseActivity")) {
            setToolbarSubtitle("Übungen");
        }
        else if (name.equals("MuscleActivity")) {
            setToolbarSubtitle("Muskeln");
        } else {
            setToolbarSubtitle("");
        }
    }

    protected void setToolbarSubtitle(String text) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) return;

        toolbar.setSubtitle(text);

        // Subtitle kleiner + transparent
        toolbar.setSubtitleTextAppearance(this, R.style.ToolbarSubtitleStyle);

        try {
            toolbar.setSubtitleCentered(true);
        } catch (Exception ignored) {
        }
    }




    protected void applyEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }

        View root = findViewById(android.R.id.content);

        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int top = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    top = insets.getInsets(WindowInsets.Type.statusBars()).top;
                }
            }
            int bottom = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
                }
            }

            v.setPadding(0, top, 0, bottom);
            return insets;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // Training
        if (id == R.id.menu_training) {
            navigateTo(MainActivity.class);
            return true;
        }

        // Übungen
        if (id == R.id.menu_exercises) {
            navigateTo(ExerciseActivity.class);
            return true;
        }

        // Muskeln
        if (id == R.id.menu_muscles) {
            navigateTo(MuscleActivity.class);
            return true;
        }

        // Training erstellen
        if (id == R.id.menu_create_training) {
            createTrainingPlan();
            db.plan.setPlan(selectedExercises);
            Toast.makeText(this, "Training erstellt", Toast.LENGTH_SHORT).show();
            return true;
        }

        // Training löschen
        if (id == R.id.menu_delete_training) {
            selectedExercises.clear();
            db.plan.clear();
            onMenuRefresh();
            Toast.makeText(this, "Training gelöscht", Toast.LENGTH_SHORT).show();
            return true;
        }


        // Demo-Daten
        if (id == R.id.menu_add_demo) {
            addDemoData();
            onMenuRefresh();
            return true;
        }

        // Alles löschen
        if (id == R.id.menu_delete_all) {
            deleteAllData();
            onMenuRefresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateTo(Class<?> target) {
        if (!this.getClass().equals(target)) {
            startActivity(new Intent(this, target));
        } else {
            onMenuRefresh();
        }
    }

    public String formatWeight(float w) {
        // Immer Punkt statt Komma
        String s = String.format(java.util.Locale.US, "%.1f", w);

        // ".0" entfernen
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
        Toast.makeText(this, "Demo-Daten hinzugefügt", Toast.LENGTH_SHORT).show();
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

        // Heatmap leeren (falls MainActivity aktiv ist)
        if (this instanceof MainActivity) {
            MuscleRegenView regenView = findViewById(R.id.view_regen);
            if (regenView != null) {
                regenView.setMuscles(new HashMap<>());
                regenView.setRegenData(new HashMap<>());
                regenView.invalidate();
            }
        }


        onMenuRefresh();
        Toast.makeText(this, "Alle Daten gelöscht", Toast.LENGTH_SHORT).show();
    }

    // Jede Activity kann das überschreiben
    protected void onMenuRefresh() {
        // Default: nichts
    }

    protected void createTrainingPlan() {

        selectedExercises.clear();

        // 1. Pool aller Übungen
        List<Exercise> pool = new ArrayList<>(db.exercises.exercises.values());

        // 2. Zielkategorien
        Muscle.Category[] targetCats = {
                Muscle.Category.ARM,
                Muscle.Category.SHOULDER,
                Muscle.Category.CHEST,
                Muscle.Category.BACK,
                Muscle.Category.CORE,
                Muscle.Category.LEG
        };

        // 3. Kategorien, die wir schon haben
        Set<Muscle.Category> usedCategories = new HashSet<>();

        // 4. Für jede Kategorie eine Übung wählen
        for (Muscle.Category targetCat : targetCats) {

            Exercise best = null;
            float bestScore = -1f;

            for (Exercise ex : pool) {

                // ---------------------------------------------------------
                // Kategorie bestimmen (aus allen Muskeln der Übung)
                // ---------------------------------------------------------
                Set<Muscle.Category> cats = ex.muscleIds.stream()
                        .map(id -> db.muscles.muscles.get(id))
                        .filter(Objects::nonNull)
                        .map(m -> m.category)
                        .collect(Collectors.toSet());

                // passt die Übung zur Zielkategorie?
                if (!cats.contains(targetCat)) continue;

                // Kategorie schon benutzt?
                if (cats.stream().anyMatch(usedCategories::contains)) continue;

                // ---------------------------------------------------------
                // A) Regeneration (Minimum aller Muskeln)
                // ---------------------------------------------------------
                float regen = ex.muscleIds.stream()
                        .map(id -> db.calculateRegeneration(id) * 100f)
                        .min(Float::compare)
                        .orElse(100f);

                // ---------------------------------------------------------
                // B) Muskelrotation (schlechtester Muskel)
                // ---------------------------------------------------------
                long muscleLast = ex.muscleIds.stream()
                        .map(id -> db.muscles.muscles.get(id))
                        .filter(Objects::nonNull)
                        .mapToLong(Muscle::getLastTraining)
                        .min()
                        .orElse(0L);

                float hoursMuscle = hoursSince(muscleLast);
                float muscleRotationScore = Math.min(100f, (hoursMuscle / 72f) * 100f);

                // ---------------------------------------------------------
                // C) Volumen (letzte Belastung)
                // ---------------------------------------------------------
                float lastVolume = ex.getLastVolume(); // sets * reps * weight
                float maxVolume = db.getMaxVolumeForCategory(targetCat);

                float volumeScore = (maxVolume <= 0f)
                        ? 100f
                        : Math.max(0f, 100f - (lastVolume / maxVolume * 100f));

                // ---------------------------------------------------------
                // D) Übungsrotation (Hard Rotation)
                // ---------------------------------------------------------
                long exerciseLast = ex.getLastTraining();
                float hoursEx = hoursSince(exerciseLast);
                float exerciseRotationScore = Math.min(100f, (hoursEx / 168f) * 100f); // 7 Tage

                // ---------------------------------------------------------
                // Gesamtscore
                // ---------------------------------------------------------
                float score =
                        regen * 0.40f +
                                muscleRotationScore * 0.20f +
                                volumeScore * 0.20f +
                                exerciseRotationScore * 0.20f;

                // ---------------------------------------------------------
                // Beste Übung wählen
                // ---------------------------------------------------------
                if (score > bestScore) {
                    bestScore = score;
                    best = ex;
                }
            }

            if (best != null) {

                selectedExercises.add(best.getId());

                // Kategorien merken
                best.muscleIds.stream()
                        .map(id -> db.muscles.muscles.get(id))
                        .filter(Objects::nonNull)
                        .map(m -> m.category)
                        .forEach(usedCategories::add);

                // Primärmuskeln extrahieren
                Set<String> primaryMuscles = new HashSet<>(best.muscleIds);

                // Übungen entfernen, die Primärmuskeln teilen
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

    // ---------------------------------------------------------
// Hilfsfunktion
// ---------------------------------------------------------
    private float hoursSince(long time) {
        if (time <= 0L) return 9999f;
        long diff = System.currentTimeMillis() - time;
        return diff / 1000f / 60f / 60f;
    }


}
