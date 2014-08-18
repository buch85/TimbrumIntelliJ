package it.buch85.timbrum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import it.buch85.timbrum.request.RecordTimbratura;
import it.buch85.timbrum.request.TimbraturaRequest;

public class ReportUtils {

	private ArrayList<RecordTimbratura> result;

	public ReportUtils(ArrayList<RecordTimbratura> result) {
		this.result = result;
	}

	public boolean validate() {
		String check = TimbraturaRequest.VERSO_ENTRATA;
		for (RecordTimbratura record : result) {
			if (!check.equals(record.getDirection())) {
				return false;
			}
			check = check == TimbraturaRequest.VERSO_ENTRATA ? TimbraturaRequest.VERSO_USCITA : TimbraturaRequest.VERSO_ENTRATA;
		}
		return true;
	}
	
	private long getWorkedTime(ArrayList<RecordTimbratura> result, Date now, Date latestExit) {
		long worked = latestExit.getTime() - result.get(0).getTimeFor(now).getTime();
		for (int i = 1; i < result.size(); i++) {
			RecordTimbratura recordTimbratura = result.get(i);
			if (recordTimbratura.getDirection().equals(TimbraturaRequest.VERSO_ENTRATA)) {
				RecordTimbratura prev = result.get(i - 1);
				long pausa = recordTimbratura.getTimeFor(now).getTime() - prev.getTimeFor(now).getTime();
				worked -= pausa;
			}
		}
		return worked;
	}

	private Date now() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.SECOND, 0);
		Date now = c.getTime();
		return now;
	}

	
	public long getWorkedTime() {
		Date now = now();
		Date latestExit = now;
		if (result.get(result.size() - 1).isExit()) {
			latestExit = result.get(result.size() - 1).getTimeFor(now);
		}
		return getWorkedTime(result, now, latestExit);
	}

	public long getRemainingTime(long timeToWork) {
		return timeToWork-getWorkedTime();
	}
	
	public boolean stillHaveToExit(){
		if(result.size()>0){
			return result.get(result.size()-1).isEntry();
		}else{
			return false;
		}
		
	}

}
