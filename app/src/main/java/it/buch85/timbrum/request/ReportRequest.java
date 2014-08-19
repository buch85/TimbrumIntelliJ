package it.buch85.timbrum.request;


import android.annotation.SuppressLint;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.buch85.timbrum.RecordTimbratura;

/**
 * Created by mbacer on 16/04/14.
 */
public class ReportRequest extends AbstractRequest {
    @SuppressLint("SimpleDateFormat")
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ReportRequest(HttpClient httpclient, HttpContext context) {
        super(httpclient, context);
    }


    public ArrayList<RecordTimbratura> getTimbrature(Date date) throws IOException, JSONException {
        request = new HttpPost(URI.create(url));
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("rows", "10"));
        formparams.add(new BasicNameValuePair("startrow", "0"));
        formparams.add(new BasicNameValuePair("count", "true"));
        formparams.add(new BasicNameValuePair("sqlcmd", "rows:ushp_fgettimbrus"));
        formparams.add(new BasicNameValuePair("pDATE", dateFormat.format(date)));
        request.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
        HttpResponse response = httpclient.execute(request, context);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity);
        entity.consumeContent();
        JSONObject jsonObject = new JSONObject(responseString);
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
