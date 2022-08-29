package it.buch85.timbrum.request;

import java.io.IOException;

import it.buch85.timbrum.VersoTimbratura;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by mbacer on 11/04/14.
 */
public class TimbraturaRequest {

    public static final String VERSO_FIELD = "verso";
    public static final String TIMBRATURA_ID = "m_cID";
    private final OkHttpClient client;
    private final String url;

    public TimbraturaRequest(OkHttpClient client, String url) {
        this.client = client;
        this.url = url;
    }

    private void timbraVerso(VersoTimbratura verso, String timbraturaId) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add(VERSO_FIELD, verso.getCode())
                .add(TIMBRATURA_ID, timbraturaId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        System.out.println("Login form get: " + response);

        //todo senza mcid c'è errore... da gestire
    }

    public void entrata(String timbraturaId) throws IOException {
        timbraVerso(VersoTimbratura.ENTRATA, timbraturaId);
    }

    public void uscita(String mcId) throws IOException {
        timbraVerso(VersoTimbratura.USCITA, mcId);
    }
}
