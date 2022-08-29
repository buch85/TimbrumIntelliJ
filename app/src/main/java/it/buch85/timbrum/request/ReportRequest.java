package it.buch85.timbrum.request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import it.buch85.timbrum.RecordTimbratura;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReportRequest {

    private final OkHttpClient client;
    private final String url;

    public ReportRequest(OkHttpClient client, String url) {
        this.client = client;
        this.url = url;
    }

    public ArrayList<RecordTimbratura> getTimbrature(Date date) throws IOException, JSONException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        RequestBody formBody = new FormBody.Builder()
                .add("rows", "10")
                .add("startrow", "0")
                .add("count", "true")
                .add("sqlcmd", "rows:ushp_fgettimbrus")
                .add("pDATE", dateFormat.format(date))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        String string = response.body().string();
        JSONObject jsonObject = new JSONObject(string);

        JSONArray fields = jsonObject.getJSONArray("Fields");
        String[] headers = new String[fields.length()];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = fields.getString(i);
        }
        JSONArray data = jsonObject.getJSONArray("Data");
        ArrayList<String[]> records = new ArrayList<String[]>();
        for (int d = 0; d < data.length() - 1; d++) {
            JSONArray realData = data.getJSONArray(d);
            String[] values = new String[realData.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = realData.getString(i);
            }
            records.add(values);
        }

        ArrayList<RecordTimbratura> timbrature = new ArrayList<RecordTimbratura>();
        for (int i = 0; i < records.size(); i++) {
            RecordTimbratura r = new RecordTimbratura(records.get(i), headers);
            timbrature.add(r);
        }
        return timbrature;
    }

}
