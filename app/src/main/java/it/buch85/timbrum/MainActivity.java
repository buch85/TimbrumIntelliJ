package it.buch85.timbrum;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

import it.buch85.timbrum.prefs.SettingsActivity;
import it.buch85.timbrum.prefs.TimbrumPreferences;
import it.maverick.workday.Workday;

public class MainActivity extends AppCompatActivity {
    private TimbrumPreferences timbrumPreferences;
    private ListView listView;
    private Button buttonRefresh;

    /**
     * The view to show the ad.
     */
    private AdView adView;
    private TextView workedText;
    private TextView remainingText;
    private TextView remainingLabel;
    private SeekBar seekBar;
    private TextView serverTimeText;
    private TimbrumTaskView timbrumView;
    private TextView exitTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+00"));
        setContentView(R.layout.activity_main);

        timbrumPreferences = new TimbrumPreferences(PreferenceManager.getDefaultSharedPreferences(this));
        // getSupportFragmentManager().beginTransaction().add(R.id.container,
        // new PlaceholderFragment()).commit();
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
        listView = (ListView) findViewById(R.id.listView1);
        workedText = (TextView) findViewById(R.id.textWorked);
        remainingText = (TextView) findViewById(R.id.textRemaining);
        remainingLabel = (TextView) findViewById(R.id.textRemainingLabel);
        serverTimeText = (TextView) findViewById(R.id.textServerTime);
        exitTimeText = (TextView) findViewById(R.id.textExitTime);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0) {
                    exit();
                } else if (progress == seekBar.getMax()) {
                    enter();
                }
                seekBar.setProgress(seekBar.getMax() / 2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });

        buttonRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) {
            // only for gingerbread and newer versions

            // Look up the AdView as a resource and load a request.
            AdView adView = (AdView) this.findViewById(R.id.adMobadView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
        timbrumView = new TimbrumTaskView();
        enableDisableButtons();
    }

    protected void refresh() {
        new TimbrumAsynchTask(timbrumPreferences, timbrumView).refresh();
    }

    protected void exit() {
        new TimbrumAsynchTask(timbrumPreferences, timbrumView).exit();
    }

    protected void enter() {
        new TimbrumAsynchTask(timbrumPreferences, timbrumView).enter();
    }

    private void enableDisableButtons() {
        boolean arePreferencesValid = timbrumPreferences.arePreferencesValid();
        buttonRefresh.setEnabled(arePreferencesValid);
        seekBar.setEnabled(arePreferencesValid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        enableDisableButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!paused && timbrumPreferences.arePreferencesValid()) {
            new TimbrumAsynchTask(timbrumPreferences, timbrumView).refresh();
        }
    }

    boolean paused = false;

    @Override
    protected void onPause() {
        paused = true;
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    /**
     * Called before the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        // Destroy the AdView.
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String formatTime(long millis) {
        long minute = (long) ((millis / (1000d * 60)) % 60);
        long hour = (long) ((millis / (1000d * 60 * 60)) % 24);

        String time = String.format(Locale.ENGLISH, "%02d:%02d", hour, minute);
        return time;
    }

    private final class TimbrumTaskView implements TimbrumView {
        private Date now;
        private final ProgressDialog progressDialog;
        private String errorMessage;
        private SimpleDateFormat simpleDateFormat;

        private TimbrumTaskView() {
            simpleDateFormat = new SimpleDateFormat("HH:mm");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00"));
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setTitle(getString(R.string.loading));
            progressDialog.setMessage(getString(R.string.please_wait));
        }

        @Override
        public boolean askTimbrumConfirmation(final VersoTimbratura direction) {
            final ConfirmationResult confirmationResult = new ConfirmationResult();
            final CountDownLatch latch = new CountDownLatch(1);
            final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    confirmationResult.setConfirmed(whichButton == DialogInterface.BUTTON_POSITIVE);
                    dialog.dismiss();
                    latch.countDown();
                }
            };
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    createConfirmationDialog(direction, onClickListener).show();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                return false;
            }
            return confirmationResult.isConfirmed();
        }

        private class ConfirmationResult {
            boolean confirmed = false;

            public void setConfirmed(boolean confirmed) {
                this.confirmed = confirmed;
            }

            public boolean isConfirmed() {
                return confirmed;
            }

        }

        @Override
        public void initProgress() {
            progressDialog.show();
        }

        @Override
        public void updateProgress(Integer resourceId) {
            progressDialog.setMessage(getString(resourceId));
        }

        @Override
        public void dismissProgress() {
            progressDialog.dismiss();
        }

        @Override
        public void resetView() {
            now = null;
            errorMessage = null;
            listView.setAdapter(null);
            remainingLabel.setText(getString(R.string.remaining));
            serverTimeText.setText(getString(R.string.n_a));
            workedText.setText(getString(R.string.n_a));
            remainingText.setText(getString(R.string.n_a));
            exitTimeText.setText(getString(R.string.n_a));
        }

        @Override
        public void updateView(Report result) {
            serverTimeText.setText(simpleDateFormat.format(now));
            if (result.getTimbrature().size() != 0) {
                listView.setAdapter(new RecordListAdapter(result.getTimbrature()));
            }
        }

        @Override
        public void updateView(Report result, Workday workday) {
            updateView(result);
            if (result.getTimbrature().size() == 0) {
                return;
            }
            long worked = workday.getWorkedTime();
            long remaining = workday.getRemainingTime();
            Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("GTM+00"));
            instance.setTime(now);
            instance.setTimeInMillis(instance.getTimeInMillis() + remaining);
            workedText.setText(formatTime(worked));
            exitTimeText.setText(simpleDateFormat.format(instance.getTime()));
            if (remaining < 0) {
                remainingLabel.setText(getString(R.string.exceeding));
                remainingText.setText(formatTime(-remaining));
            } else {
                remainingText.setText(formatTime(remaining));
                new EndOfWorkAlarm(getApplicationContext()).set(remaining);
            }
        }

        @Override
        public void setErrorMessage(String message) {
            this.errorMessage = message;
        }

        @Override
        public void showErrorMessage() {
            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        }

        @Override
        public void setLoginError(String loginErrorMessage) {
            setErrorMessage(getString(R.string.login_error) + loginErrorMessage);
        }

        private AlertDialog createConfirmationDialog(VersoTimbratura direction, DialogInterface.OnClickListener onClickListener) {
            String title = VersoTimbratura.ENTRATA.equals(direction) ? getString(R.string.confirm_entry_title) : getString(R.string.confirm_exit_title);
            String message = VersoTimbratura.ENTRATA.equals(direction) ? getString(R.string.confirm_entry_message) : getString(R.string.confirm_exit_message);
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, onClickListener)
                    .setNegativeButton(android.R.string.no, onClickListener).create();
            return dialog;
        }

        @Override
        public void setNow(Date now) {
            this.now = now;
        }
    }

    private class RecordListAdapter extends ArrayAdapter<RecordTimbratura> {

        public RecordListAdapter(List<RecordTimbratura> objects) {
            super(MainActivity.this, R.layout.row, R.id.textViewList, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            RecordTimbratura item = getItem(position);
            if (item != null) {
                ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
                TextView textView = (TextView) view.findViewById(R.id.textViewList);
                if (item.isEntry()) {
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.e));
                    textView.setText(item.getTime() + " " + getString(R.string.entry));
                } else {
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.u));
                    textView.setText(item.getTime() + " " + getString(R.string.exit));
                }
            }
            return view;
        }
    }
}
