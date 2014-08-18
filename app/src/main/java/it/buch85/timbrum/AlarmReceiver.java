package it.buch85.timbrum;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Date;

import it.buch85.timbrum.prefs.TimbrumPreferences;
import it.buch85.timbrum.request.RecordTimbratura;



public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (EndOfWorkAlarm.NOTIFY_END_OF_WORK.equals(intent.getAction())) {
			final TimbrumPreferences preferences = new TimbrumPreferences(
					PreferenceManager.getDefaultSharedPreferences(context));
			final Timbrum timbrum = new Timbrum(preferences.getHost(),
					preferences.getUsername(), preferences.getPassword());

			new AsyncTask<Void,Void,Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					try {
						if (timbrum.login().isSuccess()) {
							ArrayList<RecordTimbratura> timbrumRecords = timbrum.getReport(new Date());
							ReportUtils reportUtils = new ReportUtils(timbrumRecords);
							if (reportUtils.validate()&& reportUtils.stillHaveToExit()) {
								long remaining = reportUtils.getRemainingTime(preferences.getTimeToWork());
								if (remaining > 0) {
									new EndOfWorkAlarm(context).set(remaining);
								} else {
									showNotification(context);
								}
							}
						}
					} catch (Exception e) {
						showNotification(context);
					}
					return null;
				}
			}.execute();

		}

	}

	private void showNotification(Context context) {
		// String title=intent.getStringExtra(name)
		Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// Because clicking the notification opens a new ("special") activity,
		// there's
		// no need to create an artificial back stack.
		Uri soundUri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
				0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(context.getString(R.string.end_of_work_title))
				.setContentText(context.getString(R.string.end_of_work_message))
				.setContentIntent(resultPendingIntent).setSound(soundUri)
				.setAutoCancel(true)
				.setVibrate(new long[] { 1000, 1000, 1000 });
		Notification note = mBuilder.build();
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, note);
	}

}
