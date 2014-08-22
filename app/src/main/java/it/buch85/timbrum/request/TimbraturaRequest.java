package it.buch85.timbrum.request;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import it.buch85.timbrum.VersoTimbratura;

/**
 * Created by mbacer on 11/04/14.
 */
public class TimbraturaRequest extends AbstractRequest {

    public static final String VERSO_FIELD   = "verso";

    public TimbraturaRequest(HttpClient httpclient, HttpContext context) {
        super(httpclient, context);
    }

    private void timbraVerso(VersoTimbratura verso) throws IOException {
        request = new HttpPost(URI.create(url));
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair(VERSO_FIELD, verso.getCode()));
        request.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
        HttpResponse response = httpclient.execute(request,context);
        response.getEntity().consumeContent();
        System.out.println("Login form get: " + response.getStatusLine());
    }

    public void entrata() throws IOException {
        timbraVerso(VersoTimbratura.ENTRATA);
    }

    public void uscita() throws IOException {
        timbraVerso(VersoTimbratura.USCITA);
    }
}
