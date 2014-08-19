package it.buch85.timbrum;

import java.util.ArrayList;
import java.util.Date;

public class ReportUtils {

	private ArrayList<RecordTimbratura> result;
    private Date now;

    public ReportUtils(ArrayList<RecordTimbratura> result, Date now) {
        this.result = result;
        this.now = now;
    }

	public boolean validate() {
        VersoTimbratura check = VersoTimbratura.ENTRATA;
        for (RecordTimbratura record : result) {
			if (!check.equals(record.getDirection())) {
				return false;
			}
            check = check == VersoTimbratura.ENTRATA ? VersoTimbratura.USCITA : VersoTimbratura.ENTRATA;
        }
		return true;
	}
	
	private long getWorkedTime(ArrayList<RecordTimbratura> result, Date now, Date latestExit) {
		long worked = latestExit.getTime() - result.get(0).getTimeFor(now).getTime();
		for (int i = 1; i < result.size(); i++) {
			RecordTimbratura recordTimbratura = result.get(i);
            if (recordTimbratura.getDirection().equals(VersoTimbratura.ENTRATA)) {
                RecordTimbratura prev = result.get(i - 1);
				long pausa = recordTimbratura.getTimeFor(now).getTime() - prev.getTimeFor(now).getTime();
				worked -= pausa;
			}
		}
		return worked;
	}


    public long getWorkedTime() {
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
