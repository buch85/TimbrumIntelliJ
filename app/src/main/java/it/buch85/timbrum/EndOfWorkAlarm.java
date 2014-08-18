package it.buch85.timbrum;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class EndOfWorkAlarm {
	public static final String NOTIFY_END_OF_WORK = "notify.end.of.work";
	private Context context;

	public EndOfWorkAlarm(Context applicationContext) {
		this.context = applicationContext;
	}

	public void set(long within) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setAction(NOTIFY_END_OF_WORK);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_ONE_SHOT);
	    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(pendingIntent); // cancel any existing alarms
	    alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+within, pendingIntent);
	}

}
