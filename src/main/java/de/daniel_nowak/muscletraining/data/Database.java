package de.daniel_nowak.muscletraining.data;

import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import de.daniel_nowak.muscletraining.model.Exercise;
import de.daniel_nowak.muscletraining.model.Muscle;
import de.daniel_nowak.muscletraining.model.Training;

public class Database {

    public final MuscleDatabase muscles;
    public final ExerciseDatabase exercises;
    public final TrainingDatabase trainings;
    public final PlanDatabase plan;

    public Database(Context context) {
        muscles = new MuscleDatabase(context);
        exercises = new ExerciseDatabase(context);
        trainings = new TrainingDatabase(context);
        plan = new PlanDatabase(context);

        muscles.load();
        exercises.load();
        trainings.load();
        plan.load();
    }

    public Training addTraining(Exercise ex, int sets, int reps, float weight, int difficulty) {

        String id = UUID.randomUUID().toString();
        long time = System.currentTimeMillis();

        Training t = new Training(id, time, sets, reps, weight, ex.getId());
        t.setDifficulty(difficulty);

        t.muscleIds.clear();
        t.muscleIds.addAll(ex.muscleIds);

        trainings.trainings.put(id, t);
        trainings.save();

        ex.setLastTraining(time);
        ex.setLastSets(sets);
        ex.setLastReps(reps);
        ex.setLastWeight(weight);
        ex.setLastDifficulty(difficulty);
        exercises.save();

        for (String mId:ex.muscleIds) {
            Muscle m = muscles.muscles.get(mId);
            if (null!=m) {
            m.setLastTraining(time);
            m.setLastSets(sets);
            m.setLastReps(reps);
            m.setLastWeight(weight);
            }
        }
        muscles.save();

        return t;
    }

    public float getMaxVolumeForCategory(Muscle.Category cat) {

        float max = 0f;

        for (Training t : trainings.trainings.values()) {

            Exercise ex = exercises.exercises.get(t.getExerciseId());
            if (ex == null) continue;

            Set<Muscle.Category> cats = ex.muscleIds.stream()
                    .map(id -> muscles.muscles.get(id))
                    .filter(Objects::nonNull)
                    .map(m -> m.category)
                    .collect(Collectors.toSet());

            if (!cats.contains(cat)) continue;

            float vol = t.getSets() * t.getReps() * t.getWeight();
            if (vol > max) max = vol;
        }

        return max;
    }


    public void addDemoData() {
        muscles.addDemoData();
        exercises.addDemoData();
        syncAllRelations();
    }

    public String exportToXml() {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<MuscleTraining version=\"1.0\">\n");

        sb.append("  <Muscles>\n");
        for (Muscle m : muscles.muscles.values()) {
            sb.append("    <Muscle id=\"").append(m.getId()).append("\">\n");
            sb.append("      <Name>").append(escapeXml(m.getName())).append("</Name>\n");
            sb.append("      <LastTraining>").append(m.getLastTraining()).append("</LastTraining>\n");
            sb.append("      <LastSets>").append(m.getLastSets()).append("</LastSets>\n");
            sb.append("      <LastReps>").append(m.getLastReps()).append("</LastReps>\n");
            sb.append("      <LastWeight>").append(m.getLastWeight()).append("</LastWeight>\n");

            sb.append("      <ExerciseIds>");
            sb.append(String.join(",", m.exerciseIds));
            sb.append("</ExerciseIds>\n");

            sb.append("      <PosX>").append(joinFloatList(m.posXList)).append("</PosX>\n");
            sb.append("      <PosY>").append(joinFloatList(m.posYList)).append("</PosY>\n");
            sb.append("      <Side>").append(String.join(",", m.sideList)).append("</Side>\n");

            sb.append("      <Category>").append(m.category.name()).append("</Category>\n");
            sb.append("    </Muscle>\n");
        }
        sb.append("  </Muscles>\n");

        sb.append("  <Exercises>\n");
        for (Exercise ex : exercises.exercises.values()) {
            sb.append("    <Exercise id=\"").append(ex.getId()).append("\">\n");
            sb.append("      <Name>").append(escapeXml(ex.getName())).append("</Name>\n");

            sb.append("      <WeightMin>").append(ex.getWeightMin()).append("</WeightMin>\n");
            sb.append("      <WeightMax>").append(ex.getWeightMax()).append("</WeightMax>\n");
            sb.append("      <WeightStep>").append(ex.getWeightStep()).append("</WeightStep>\n");

            sb.append("      <SetsMin>").append(ex.getSetsMin()).append("</SetsMin>\n");
            sb.append("      <SetsMax>").append(ex.getSetsMax()).append("</SetsMax>\n");

            sb.append("      <RepsMin>").append(ex.getRepsMin()).append("</RepsMin>\n");
            sb.append("      <RepsMax>").append(ex.getRepsMax()).append("</RepsMax>\n");
            sb.append("      <RepsStep>").append(ex.getRepsStep()).append("</RepsStep>\n");

            sb.append("      <LastTraining>").append(ex.getLastTraining()).append("</LastTraining>\n");
            sb.append("      <LastSets>").append(ex.getLastSets()).append("</LastSets>\n");
            sb.append("      <LastReps>").append(ex.getLastReps()).append("</LastReps>\n");
            sb.append("      <LastWeight>").append(ex.getLastWeight()).append("</LastWeight>\n");
            sb.append("      <LastDifficulty>").append(ex.getLastDifficulty()).append("</LastDifficulty>\n");

            sb.append("      <MuscleIds>");
            sb.append(String.join(",", ex.muscleIds));
            sb.append("</MuscleIds>\n");

            sb.append("    </Exercise>\n");
        }
        sb.append("  </Exercises>\n");

        sb.append("  <Trainings>\n");
        for (Training t : trainings.trainings.values()) {
            sb.append("    <Training id=\"").append(t.getId()).append("\">\n");

            sb.append("      <Time>").append(t.getTime()).append("</Time>\n");
            sb.append("      <Sets>").append(t.getSets()).append("</Sets>\n");
            sb.append("      <Reps>").append(t.getReps()).append("</Reps>\n");
            sb.append("      <Weight>").append(t.getWeight()).append("</Weight>\n");

            sb.append("      <ExerciseId>").append(t.getExerciseId()).append("</ExerciseId>\n");

            sb.append("      <MuscleIds>");
            sb.append(String.join(",", t.muscleIds));
            sb.append("</MuscleIds>\n");

            sb.append("      <Difficulty>").append(t.getDifficulty()).append("</Difficulty>\n");

            sb.append("    </Training>\n");
        }
        sb.append("  </Trainings>\n");

        sb.append("</MuscleTraining>\n");

        return sb.toString();
    }

    public void importFromXml(InputStream in) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(in);

        Element root = doc.getDocumentElement();
        @SuppressWarnings("unused") String version = root.getAttribute("version");

        muscles.muscles.clear();
        NodeList muscleNodes = doc.getElementsByTagName("Muscle");

        for (int i = 0; i < muscleNodes.getLength(); i++) {
            Element e = (Element) muscleNodes.item(i);

            String id = e.getAttribute("id");
            String name = getText(e, "Name");

            Muscle m = new Muscle(id, name);

            m.setLastTraining(parseLong(getText(e, "LastTraining")));
            m.setLastSets(parseInt(getText(e, "LastSets")));
            m.setLastReps(parseInt(getText(e, "LastReps")));
            m.setLastWeight(parseFloat(getText(e, "LastWeight")));

            m.exerciseIds = splitList(getText(e, "ExerciseIds"));
            m.posXList = splitFloatList(getText(e, "PosX"));
            m.posYList = splitFloatList(getText(e, "PosY"));
            m.sideList = splitList(getText(e, "Side"));

            try {
                m.category = Muscle.Category.valueOf(getText(e, "Category"));
            } catch (Exception ignored) {}

            muscles.muscles.put(id, m);
        }

        exercises.exercises.clear();
        NodeList exNodes = doc.getElementsByTagName("Exercise");

        for (int i = 0; i < exNodes.getLength(); i++) {
            Element e = (Element) exNodes.item(i);

            String id = e.getAttribute("id");
            String name = getText(e, "Name");

            Exercise ex = new Exercise(id, name);

            ex.setWeightMin(parseFloat(getText(e, "WeightMin")));
            ex.setWeightMax(parseFloat(getText(e, "WeightMax")));
            ex.setWeightStep(parseFloat(getText(e, "WeightStep")));

            ex.setSetsMin(parseInt(getText(e, "SetsMin")));
            ex.setSetsMax(parseInt(getText(e, "SetsMax")));

            ex.setRepsMin(parseInt(getText(e, "RepsMin")));
            ex.setRepsMax(parseInt(getText(e, "RepsMax")));
            ex.setRepsStep(parseInt(getText(e, "RepsStep")));

            ex.setLastTraining(parseLong(getText(e, "LastTraining")));
            ex.setLastSets(parseInt(getText(e, "LastSets")));
            ex.setLastReps(parseInt(getText(e, "LastReps")));
            ex.setLastWeight(parseFloat(getText(e, "LastWeight")));
            ex.setLastDifficulty(parseInt(getText(e, "LastDifficulty")));

            ex.muscleIds = splitList(getText(e, "MuscleIds"));

            exercises.exercises.put(id, ex);
        }

        trainings.trainings.clear();
        NodeList tNodes = doc.getElementsByTagName("Training");

        for (int i = 0; i < tNodes.getLength(); i++) {
            Element e = (Element) tNodes.item(i);

            String id = e.getAttribute("id");

            Training t = new Training(
                    id,
                    parseLong(getText(e, "Time")),
                    parseInt(getText(e, "Sets")),
                    parseInt(getText(e, "Reps")),
                    parseFloat(getText(e, "Weight")),
                    getText(e, "ExerciseId")
            );

            t.muscleIds = splitList(getText(e, "MuscleIds"));
            t.setDifficulty(parseInt(getText(e, "Difficulty")));

            trainings.trainings.put(id, t);
        }

        syncAllRelations();

        muscles.save();
        exercises.save();
        trainings.save();
    }

    private String getText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) return "";
        return list.item(0).getTextContent();
    }

    private List<String> splitList(String s) {
        if (s == null || s.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(s.split(",")));
    }

    private List<Float> splitFloatList(String s) {
        List<Float> out = new ArrayList<>();
        if (s == null || s.isEmpty()) return out;
        for (String p : s.split(",")) {
            try { out.add(Float.parseFloat(p)); } catch (Exception ignored) {}
        }
        return out;
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return 0L; }
    }

    private float parseFloat(String s) {
        try { return Float.parseFloat(s); } catch (Exception e) { return 0f; }
    }

    private String joinFloatList(List<Float> list) {
        List<String> out = new ArrayList<>();
        for (Float f : list) out.add(String.valueOf(f));
        return String.join(",", out);
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }


    public float calculateRegeneration(String muscleId) {

        Muscle m = muscles.muscles.get(muscleId);
        if (m == null) return 1.0f;

        long last = m.getLastTraining();

        if (last == 0L) return 1.0f;

        long now = System.currentTimeMillis();
        float hours = (now - last) / 1000f / 3600f;

        float regenHours = m.getRegenerationHours();
        if (regenHours <= 0f) return 1.0f;

        float regen = hours / regenHours;

        if (regen > 1f) regen = 1f;
        if (regen < 0f) regen = 0f;

        return regen;
    }

    public void syncAllRelations() {

        for (Muscle m : muscles.muscles.values()) {
            m.exerciseIds.clear();
        }

        for (Exercise ex : exercises.exercises.values()) {
            ex.muscleIds.removeIf(id -> !muscles.muscles.containsKey(id));
        }

        for (Exercise ex : exercises.exercises.values()) {
            for (String mId : ex.muscleIds) {
                Muscle m = muscles.muscles.get(mId);
                if (m != null && !m.exerciseIds.contains(ex.getId())) {
                    m.exerciseIds.add(ex.getId());
                }
            }
        }

        muscles.save();
        exercises.save();
    }


    public void delExercise(String id) {

        exercises.exercises.remove(id);

        for (Muscle m : muscles.muscles.values()) {
            m.exerciseIds.remove(id);
        }

        trainings.markExerciseDeleted(id);

        exercises.save();
        muscles.save();
        trainings.save();
    }

    public void delMuscle(String id) {

        muscles.muscles.remove(id);

        for (Exercise ex : exercises.exercises.values()) {
            ex.muscleIds.remove(id);
        }

        trainings.markMuscleDeleted(id);

        muscles.save();
        exercises.save();
        trainings.save();
    }


    public void syncExercise(Exercise ex) {

        for (Muscle m : muscles.muscles.values()) {
            m.exerciseIds.remove(ex.getId());
        }

        for (String muscleId : ex.muscleIds) {
            Muscle m = muscles.muscles.get(muscleId);
            if (m != null && !m.exerciseIds.contains(ex.getId())) {
                m.exerciseIds.add(ex.getId());
            }
        }

        exercises.save();
        muscles.save();
    }

    public void syncMuscle(Muscle m) {

        for (Exercise ex : exercises.exercises.values()) {
            ex.muscleIds.remove(m.getId());
        }

        for (String exId : m.exerciseIds) {
            Exercise ex = exercises.exercises.get(exId);
            if (ex != null && !ex.muscleIds.contains(m.getId())) {
                ex.muscleIds.add(m.getId());
            }
        }

        exercises.save();
        muscles.save();
    }
}
