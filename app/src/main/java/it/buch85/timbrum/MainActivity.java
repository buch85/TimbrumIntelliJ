package it.buch85.timbrum;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import it.buch85.timbrum.prefs.SettingsActivity;
import it.buch85.timbrum.prefs.TimbrumPreferences;
import it.buch85.timbrum.request.LoginRequest.LoginResult;
import it.buch85.timbrum.request.RecordTimbratura;
import it.buch85.timbrum.request.TimbraturaRequest;

public class MainActivity extends Activity {
	private TimbrumPreferences timbrumPreferences;
	private ListView listView;
	private Button buttonRefresh;

	/** The view to show the ad. */
	private AdView adView;
	private TextView workedText;
	private TextView remainingText;
	private TextView remainingLabel;
	private SeekBar seekBar;
	public static MainActivity instance;
    private TextView serverTimeText;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
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
        serverTimeText= (TextView) findViewById(R.id.textServerTime);
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
		enableDisableButtons();
	}

	protected void refresh() {
		new TimbrumTask().execute();
	}

	protected void exit() {
		new TimbrumTask(TimbraturaRequest.VERSO_USCITA).execute();

	}

	protected void enter() {
		new TimbrumTask(TimbraturaRequest.VERSO_ENTRATA).execute();
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
			new TimbrumTask().execute();
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

	/** Called before the activity is destroyed. */
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

	
	private final class TimbrumTask extends AsyncTask<String, String, ArrayList<RecordTimbratura>> {
        String versoTimbratura = null;
		private ProgressDialog progressDialog;

		String message = "";
		private Timbrum timbrum;
		protected boolean isConfirmed=true;
        private Date now;

        public TimbrumTask() {
			this(null);
		}

		public TimbrumTask(String timbratura) {
			this.versoTimbratura = timbratura;
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setTitle(getString(R.string.loading));
			progressDialog.setMessage(getString(R.string.please_wait));
			timbrum = new Timbrum(timbrumPreferences.getHost(), timbrumPreferences.getUsername(), timbrumPreferences.getPassword());
		}

		@Override
		protected void onPreExecute() {
			listView.setAdapter(null);
			progressDialog.show();
		}

		@Override
		protected ArrayList<RecordTimbratura> doInBackground(String... params) {
			try {
				publishProgress( getString(R.string.logging_in));
				LoginResult loginResult = timbrum.login();
				if (loginResult.isSuccess()) {
					now = timbrum.now();
					if (versoTimbratura != null) {
						publishProgress(getString(R.string.loading_logs));
						ArrayList<RecordTimbratura> report = timbrum.getReport(now);
						if (exitAsFirstTimbrum(report) || doubleTimbrum(report) ) {
							isConfirmed=false;
							final CountDownLatch latch=new CountDownLatch(1);
							final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
							    public void onClick(DialogInterface dialog, int whichButton) {
                                    isConfirmed = whichButton == DialogInterface.BUTTON_POSITIVE;
							    	dialog.dismiss();
							    	latch.countDown();
							    }};
							MainActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									createConfirmationDialog(versoTimbratura,onClickListener).show();
								}
							});
							latch.await();
						}
						if(isConfirmed){
							publishProgress(getString(R.string.timbrum_in_progress));
							timbrum.timbra(versoTimbratura);
						}else{
							return report;
						}
					}
					publishProgress(getString(R.string.loading_logs));
					return timbrum.getReport(now);
				} else {
					message = getString(R.string.login_error) + loginResult.getMessage();
				}
			} catch (Exception e) {
				message = getString(R.string.error) + e.getMessage();
			}
			return null;
		}

		private AlertDialog createConfirmationDialog(String direction,DialogInterface.OnClickListener onClickListener) {
			String title= direction.equals(TimbraturaRequest.VERSO_ENTRATA)?getString(R.string.confirm_entry_title):getString(R.string.confirm_exit_title);
			String message= direction.equals(TimbraturaRequest.VERSO_ENTRATA)?getString(R.string.confirm_entry_message):getString(R.string.confirm_exit_message);
			AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
			.setTitle(title)
			.setMessage(message)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.yes, onClickListener)
			.setNegativeButton(android.R.string.no, onClickListener).create();
			return dialog;
		}

		private boolean doubleTimbrum(ArrayList<RecordTimbratura> report) {
			if(report.isEmpty()){
				return false;
			}else{
				RecordTimbratura recordTimbratura = report.get(report.size()-1);
				return recordTimbratura.getDirection().equals(versoTimbratura);
			}
		}

		private boolean exitAsFirstTimbrum(ArrayList<RecordTimbratura> report) {
			return report.isEmpty() && TimbraturaRequest.VERSO_USCITA.equals(versoTimbratura);
		}

		@Override
		protected void onProgressUpdate(String... values) {
			for (String value : values) {
				progressDialog.setMessage(value);
			}
		}

		@Override
		protected void onPostExecute(ArrayList<RecordTimbratura> result) {
			progressDialog.dismiss();
			remainingLabel.setText(getString(R.string.remaining));
            serverTimeText.setText(new SimpleDateFormat("HH:mm").format(now));
			if (result == null) {
				Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
				return;
			}
			updateView(result);
		}

		private void updateView(ArrayList<RecordTimbratura> result) {
			if (result.size() == 0) {
				workedText.setText(getString(R.string.n_a));
				remainingText.setText(getString(R.string.n_a));
			} else {
				listView.setAdapter(new ArrayAdapter<RecordTimbratura>(MainActivity.this, R.layout.row, R.id.textViewList, result));

				ReportUtils logRecords = new ReportUtils(result);
				if (logRecords.validate()) {
					long worked = logRecords.getWorkedTime();
					long remaining = logRecords.getRemainingTime(timbrumPreferences.getTimeToWork());
					workedText.setText(formatTime(worked));
					if (remaining < 0) {
						remainingLabel.setText(getString(R.string.exceeding));
						remainingText.setText(formatTime(-remaining));
					} else {
						remainingText.setText(formatTime(remaining));
					}
					new EndOfWorkAlarm(getApplicationContext()).set(remaining<0?0:remaining);
				} else {
					workedText.setText(getString(R.string.n_a));
					remainingText.setText(getString(R.string.n_a));
				}
			}
		}

		@Override
		protected void onCancelled() {
			cancel(true);
			progressDialog.dismiss();
		}
	}
}
