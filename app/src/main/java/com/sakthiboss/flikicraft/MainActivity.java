package com.sakthiboss.flikicraft;

import android.app.Activity;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText script;
    private Spinner language;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        return button;
    }

    private void addGap(LinearLayout parent, int height) {
        TextView gap = new TextView(this);
        parent.addView(gap, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(height)));
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(28), dp(20), dp(24));
        scroll.addView(page);
        scroll.setBackgroundColor(0xFFF5F7FB);

        TextView title = new TextView(this);
        title.setText("FlikiCraft");
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(0xFF172554);
        page.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Tamil & English Script Studio");
        subtitle.setTextSize(16);
        subtitle.setTextColor(0xFF475569);
        page.addView(subtitle);
        addGap(page, 22);

        TextView languageLabel = new TextView(this);
        languageLabel.setText("Script language");
        languageLabel.setTextSize(16);
        page.addView(languageLabel);
        addGap(page, 6);

        language = new Spinner(this);
        String[] languages = {"Tamil", "English", "Tamil + English"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item);
        language.setAdapter(adapter);
        page.addView(language);
        addGap(page, 14);

        TextView scriptLabel = new TextView(this);
        scriptLabel.setText("Enter your video script");
        scriptLabel.setTextSize(16);
        page.addView(scriptLabel);
        addGap(page, 6);

        script = new EditText(this);
        script.setGravity( android.view.Gravity.TOP | android.view.Gravity.START);
        script.setHint("உங்கள் கதையை இங்கே எழுதுங்கள்...\nWrite your script here...");
        script.setTextSize(16);
        script.setMinLines(10);
        script.setPadding(dp(12), dp(12), dp(12), dp(12));
        script.setBackgroundColor(0xFFFFFFFF);
        page.addView(script, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(230)));
        addGap(page, 14);

        Button copy = makeButton("Copy Script");
        page.addView(copy, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        copy.setOnClickListener(v -> {
            String text = script.getText().toString();
            if (text.trim().isEmpty()) {
                toast("முதலில் script எழுதுங்கள்");
                return;
            }
            ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("FlikiCraft script", text));
            toast("Script copied");
        });

        Button share = makeButton("Share Script");
        page.addView(share, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        share.setOnClickListener(v -> {
            String text = script.getText().toString().trim();
            if (text.isEmpty()) {
                toast("முதலில் script எழுதுங்கள்");
                return;
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "FlikiCraft Script");
            intent.putExtra(Intent.EXTRA_TEXT,
                "Language: " + language.getSelectedItem() + "\n\n" + text);
            startActivity(Intent.createChooser(intent, "Share script"));
        });

        addGap(page, 16);
        TextView note = new TextView(this);
        note.setText("Starter version: script writing, copy and share. Video rendering and voice generation will be integrated in a later version.");
        note.setTextSize(13);
        note.setTextColor(0xFF64748B);
        page.addView(note);

        setContentView(scroll);
    }
}
