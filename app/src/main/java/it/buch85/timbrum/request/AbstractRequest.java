package it.buch85.timbrum.request;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * Created by mbacer on 23/04/14.
 */
public class AbstractRequest {

    protected HttpClient httpclient;
    protected String              url;
    protected HttpContext context;
    protected HttpPost request;

    public AbstractRequest(HttpClient httpclient, HttpContext context) {
        this.httpclient = httpclient;
        HttpParams httpParameters = httpclient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
        HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		this.context = context;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
