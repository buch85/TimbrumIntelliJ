package it.buch85.timbrum;


import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class RecordTimbratura {
	private String time;
	private String dir;
	public RecordTimbratura(String[] strings,String[] headers) {
		int timeIndex =getIndexFor(headers,"TIMETIMBR");
		int dirIndex =getIndexFor(headers,"DIRTIMBR");
		time= strings[timeIndex];
		dir= strings[dirIndex];
	}
	
	//DAYSTAMP, TIMETIMBR, DIRTIMBR, CAUSETIMBR, TYPETIMBR, IPTIMBR
	@Override
	public String toString() {
		
		String message= time + " "+ (isEntry()?MainActivity.instance.getString(R.string.entry):MainActivity.instance.getString(R.string.exit));
		return message;
	}

	private int getIndexFor(String[] headers,String string) {
		for(int i=0;i<=headers.length;i++){
			if(headers[i].equals(string)){
				return i;
			}
		}
		return -1;
	}
	
	public String getTime() {
		return time;
	}

    public VersoTimbratura getDirection() {
        if (isEntry()) {
            return VersoTimbratura.ENTRATA;
        } else {
            return VersoTimbratura.USCITA;
        }
    }
	
	public boolean isEntry(){
        return dir.equals(VersoTimbratura.ENTRATA.getCode());
    }
	public boolean isExit(){
		return !isEntry();
	}
	
	public Date getTimeFor(Date date){
		String time=getTime();
		String[] tokens=time.split(":");
		String hour=tokens[0];
		String minutes=tokens[1];
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GTM+00"));
        c.setTime(date);
		c.set(Calendar.AM_PM,Calendar.AM);
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        c.set(Calendar.MINUTE,Integer.parseInt(minutes));
		c.set(Calendar.SECOND,0);
		return c.getTime();
	}
}
