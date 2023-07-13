package com.example.tp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_TEXT = "text";
    private static final String KEY_BUTTON_ENABLED = "buttonEnabled";
    private static final String KEY_PAUSE_START_TIME = "pauseStartTime";
    private static final String KEY_PROGRESS = "progress";

    private TextView textView;
    private Button button;
    private ProgressBar progressBar;
    private Handler handler;
    private Runnable runnable;

    private long pauseStartTime;
    private boolean buttonEnabled;
    private int progress;

    private SleepTask sleepTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);
        handler = new Handler();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false); // Désactive le bouton pendant l'attente
                progressBar.setProgress(0); // Réinitialise la barre de progression

                // Génère un délai aléatoire entre 1 et 5 secondes
                Random random = new Random();
                int delay = 1000 * (1 + random.nextInt(5));

                pauseStartTime = SystemClock.elapsedRealtime();
                textView.setText("En attente...");
                sleepTask = new SleepTask();
                sleepTask.execute(delay);
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                textView.setText("L'application est réveillée !");

                long pauseDuration = SystemClock.elapsedRealtime() - pauseStartTime;
                String pauseDurationText = "Durée de pause : " + (pauseDuration / 1000) + " secondes";
                textView.append("\n" + pauseDurationText);

                button.setEnabled(true); // Réactive le bouton après l'attente
            }
        };

        if (savedInstanceState != null) {
            // Restaure l'état de l'activité
            String savedText = savedInstanceState.getString(KEY_TEXT);
            textView.setText(savedText);

            buttonEnabled = savedInstanceState.getBoolean(KEY_BUTTON_ENABLED);
            button.setEnabled(buttonEnabled);

            pauseStartTime = savedInstanceState.getLong(KEY_PAUSE_START_TIME);

            progress = savedInstanceState.getInt(KEY_PROGRESS);
            progressBar.setProgress(progress);

            if (progress < 100) {
                sleepTask = new SleepTask();
                sleepTask.execute();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Sauvegarde l'état de l'activité
        outState.putString(KEY_TEXT, textView.getText().toString());
        outState.putBoolean(KEY_BUTTON_ENABLED, button.isEnabled());
        outState.putLong(KEY_PAUSE_START_TIME, pauseStartTime);
        outState.putInt(KEY_PROGRESS, progress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sleepTask != null && !sleepTask.isCancelled()) {
            sleepTask.cancel(true);
        }
    }

    private class SleepTask extends AsyncTask<Integer, Integer, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            int delay = params[0];
            int sleepDuration = delay / 10; // Divise le temps de sommeil total en 10 tranches

            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int progress = (i + 1) * 10;
                publishProgress(progress, progress); // Met à jour la barre de progression et affiche le pourcentage
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            int percentage = values[1];
            progressBar.setProgress(progress);
            progressBar.setSecondaryProgress(percentage); // Affiche le pourcentage dans la barre de progression
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            textView.setText("L'application est réveillée !");

            long pauseDuration = SystemClock.elapsedRealtime() - pauseStartTime;
            String pauseDurationText = "Durée de pause : " + (pauseDuration / 1000) + " secondes";
            textView.append("\n" + pauseDurationText);

            button.setEnabled(true); // Réactive le bouton après l'attente
        }
    }
}
