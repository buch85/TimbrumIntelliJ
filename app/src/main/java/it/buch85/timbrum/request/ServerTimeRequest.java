package it.buch85.timbrum.request;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerTimeRequest {

    public static final String FUNCTION_RETURN_VALUE = "Function return value:";

    private final OkHttpClient client;
    private final String url;

    public ServerTimeRequest(OkHttpClient client, String url) {
        this.client = client;
        this.url = url;
    }

    public Date now() throws IOException, ParseException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        String responseString = response.body().string();

        int start = responseString.lastIndexOf(FUNCTION_RETURN_VALUE) + FUNCTION_RETURN_VALUE.length();
        int end = responseString.indexOf('\n', start);
        String date = responseString.substring(start, end);
        SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        parserSDF.setTimeZone(TimeZone.getTimeZone("GMT+00"));
        Date parsed = parserSDF.parse(date);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+00"));
        calendar.setTime(parsed);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
