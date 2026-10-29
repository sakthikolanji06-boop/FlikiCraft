package com.sakthiboss.flikicraft;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.graphics.Typeface;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity {
    private EditText script;
    private Spinner language;
    private TextView sceneOutput;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private final ArrayList<String> scenes = new ArrayList<>();
    private static final int EXPORT_SRT = 501;

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + .5f);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private TextView label(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    private void gap(LinearLayout p, int h) {
        p.addView(new TextView(this),
            new LinearLayout.LayoutParams(-1, dp(h)));
    }

    private void button(LinearLayout p, String text, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        p.addView(b, new LinearLayout.LayoutParams(-1, -2));
        b.setOnClickListener(v -> action.run());
    }

    private String getScript() {
        return script.getText().toString().trim();
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);

        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(24), dp(18), dp(28));
        page.setBackgroundColor(0xFFF5F7FB);
        scroll.addView(page);

        TextView title = label("FlikiCraft AI Video Studio", 27, 0xFF172554);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        page.addView(title);
        page.addView(label(
            "Tamil + English • Script • Scenes • Voice • Subtitles",
            14, 0xFF475569));
        gap(page, 16);

        page.addView(label("Script language", 16, 0xFF172554));
        language = new Spinner(this);
        String[] languages = {"Tamil", "English", "Tamil + English"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item);
        language.setAdapter(adapter);
        page.addView(language);
        gap(page, 10);

        page.addView(label("Your video script", 16, 0xFF172554));
        script = new EditText(this);
        script.setGravity(Gravity.TOP | Gravity.START);
        script.setHint("உங்கள் கதையை இங்கே எழுதுங்கள்...");
        script.setTextSize(16);
        script.setMinLines(9);
        script.setPadding(dp(12), dp(12), dp(12), dp(12));
        script.setBackgroundColor(0xFFFFFFFF);
        page.addView(script, new LinearLayout.LayoutParams(-1, dp(220)));
        gap(page, 8);

        button(page, "Save Project", this::saveProject);
        button(page, "Load Project", this::loadProject);
        button(page, "Copy Script", this::copyScript);
        button(page, "Share Script", this::shareScript);
        button(page, "Split Script into Scenes", this::splitScenes);
        button(page, "Speak Script (Device TTS)", this::speak);
        button(page, "Stop Voice", () -> {
            if (tts != null) tts.stop();
            toast("Voice stopped");
        });
        button(page, "Export Subtitles (.srt)", this::exportSrt);

        gap(page, 14);
        TextView heading = label("Scene Plan", 20, 0xFF172554);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        page.addView(heading);
        sceneOutput = label(
            "Write a script and select Split Script into Scenes.",
            14, 0xFF334155);
        sceneOutput.setPadding(dp(12), dp(12), dp(12), dp(12));
        sceneOutput.setBackgroundColor(0xFFFFFFFF);
        page.addView(sceneOutput);

        gap(page, 12);
        TextView status = label(
            "Available in this stage: local project save/load, scene planning, " +
            "device TTS and SRT export.\n\n" +
            "Not yet implemented: AI image generation, animated scene composition, " +
            "MP4 rendering and YouTube upload. This app does not claim those features " +
            "are complete.",
            13, 0xFF7C2D12);
        status.setPadding(dp(12), dp(12), dp(12), dp(12));
        status.setBackgroundColor(0xFFFFF7ED);
        page.addView(status);

        setContentView(scroll);

        tts = new TextToSpeech(this, result -> {
            ttsReady = result == TextToSpeech.SUCCESS;
            if (!ttsReady) toast("Device TTS initialization failed");
        });
    }

    private File projectFile() {
        return new File(getFilesDir(), "flikicraft-project.txt");
    }

    private void saveProject() {
        String value = getScript();
        if (value.isEmpty()) {
            toast("முதலில் script எழுதுங்கள்");
            return;
        }
        try (FileOutputStream out = new FileOutputStream(projectFile())) {
            String data = language.getSelectedItemPosition() + "\n" + value;
            out.write(data.getBytes(StandardCharsets.UTF_8));
            toast("Project saved on this device");
        } catch (Exception e) {
            toast("Save failed: " + e.getMessage());
        }
    }

    private void loadProject() {
        File f = projectFile();
        if (!f.exists()) {
            toast("Saved project இல்லை");
            return;
        }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), StandardCharsets.UTF_8))) {
            int index = Integer.parseInt(r.readLine());
            language.setSelection(Math.max(0, Math.min(2, index)));
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                if (text.length() > 0) text.append('\n');
                text.append(line);
            }
            script.setText(text.toString());
            toast("Project loaded");
        } catch (Exception e) {
            toast("Load failed: " + e.getMessage());
        }
    }

    private void copyScript() {
        String value = getScript();
        if (value.isEmpty()) {
            toast("முதலில் script எழுதுங்கள்");
            return;
        }
        ClipboardManager cb = (ClipboardManager)
            getSystemService(Context.CLIPBOARD_SERVICE);
        cb.setPrimaryClip(ClipData.newPlainText("FlikiCraft", value));
        toast("Script copied");
    }

    private void shareScript() {
        String value = getScript();
        if (value.isEmpty()) {
            toast("முதலில் script எழுதுங்கள்");
            return;
        }
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "FlikiCraft Video Script");
        i.putExtra(Intent.EXTRA_TEXT,
            "Language: " + language.getSelectedItem() + "\n\n" + value);
        startActivity(Intent.createChooser(i, "Share script"));
    }

    private void splitScenes() {
        String value = getScript();
        if (value.isEmpty()) {
            toast("முதலில் script எழுதுங்கள்");
            return;
        }
        scenes.clear();
        String[] chunks = value.split("(?<=[.!?。！？])\\s+|\\n+");
        StringBuilder current = new StringBuilder();
        int words = 0;

        for (String raw : chunks) {
            String part = raw.trim();
            if (part.isEmpty()) continue;
            int count = part.split("\\s+").length;
            if (words >= 35 && current.length() > 0) {
                scenes.add(current.toString().trim());
                current.setLength(0);
                words = 0;
            }
            if (current.length() > 0) current.append(' ');
            current.append(part);
            words += count;
        }
        if (current.length() > 0) scenes.add(current.toString().trim());

        int totalWords = value.split("\\s+").length;
        int minutes = Math.max(1, (int)Math.ceil(totalWords / 130.0));
        StringBuilder result = new StringBuilder();
        result.append("Word count: ").append(totalWords)
            .append("\nEstimated narration: ").append(minutes)
            .append(" minute(s)\n")
            .append("5-minute target: script length may need adjustment.\n\n");

        for (int i = 0; i < scenes.size(); i++) {
            result.append("SCENE ").append(i + 1).append("\n")
                .append(scenes.get(i)).append("\n\n");
        }
        sceneOutput.setText(result.toString());
        toast(scenes.size() + " scenes created");
    }

    private boolean hasTamil(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.UnicodeBlock.of(value.charAt(i)) ==
                Character.UnicodeBlock.TAMIL) return true;
        }
        return false;
    }

    private void speak() {
        String value = getScript();
        if (value.isEmpty()) {
            toast("முதலில் script எழுதுங்கள்");
            return;
        }
        if (!ttsReady || tts == null) {
            toast("Device TTS is not ready yet");
            return;
        }

        String choice = String.valueOf(language.getSelectedItem());
        Locale locale;
        if ("Tamil".equals(choice)) locale = new Locale("ta", "IN");
        else if ("English".equals(choice)) locale = Locale.US;
        else locale = hasTamil(value) ? new Locale("ta", "IN") : Locale.US;

        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED) {
            toast("Voice unavailable. Install the language in Android TTS settings.");
            return;
        }
        tts.setSpeechRate(0.92f);
        tts.speak(value, TextToSpeech.QUEUE_FLUSH, null);
    }

    private String time(long ms) {
        long secTotal = ms / 1000;
        return String.format(Locale.US, "%02d:%02d:%02d,%03d",
            secTotal / 3600, (secTotal / 60) % 60, secTotal % 60, ms % 1000);
    }

    private String makeSrt() {
        StringBuilder out = new StringBuilder();
        long cursor = 0;
        for (int i = 0; i < scenes.size(); i++) {
            String s = scenes.get(i);
            int words = Math.max(1, s.split("\\s+").length);
            long duration = Math.max(2500, (long)(words * 60000.0 / 130.0));
            out.append(i + 1).append('\n')
                .append(time(cursor)).append(" --> ")
                .append(time(cursor + duration)).append('\n')
                .append(s).append("\n\n");
            cursor += duration;
        }
        return out.toString();
    }

    private void exportSrt() {
        if (scenes.isEmpty()) splitScenes();
        if (scenes.isEmpty()) return;
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/x-subrip");
        i.putExtra(Intent.EXTRA_TITLE, "flikicraft-subtitles.srt");
        startActivityForResult(i, EXPORT_SRT);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != EXPORT_SRT || resultCode != RESULT_OK ||
            data == null || data.getData() == null) return;
        try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
            if (out == null) throw new IOException("Unable to open selected file");
            out.write(makeSrt().getBytes(StandardCharsets.UTF_8));
            toast("Subtitle file exported");
        } catch (Exception e) {
            toast("Subtitle export failed: " + e.getMessage());
        }
    }

    @Override protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
