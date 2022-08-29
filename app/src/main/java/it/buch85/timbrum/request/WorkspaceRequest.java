package it.buch85.timbrum.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WorkspaceRequest {

    public static final String C_ID_KEY = "this.splinker10.m_cID='";

    private final OkHttpClient client;
    private final String url;

    public WorkspaceRequest(OkHttpClient client, String url) {
        this.client = client;
        this.url = url;
    }

    public String loadTimbraturaId() throws RequestException {
        Call call = createCall();

        try {
            Response response = call.execute();
            ResponseBody body = response.body();

            if (body == null) {
                throw new RequestException("Empty body in workspace request");
            }

            return getTimbraturaId(body.charStream());
        } catch (IOException e) {
            throw new RequestException("Unable to load workspace", e);
        }

    }

    private Call createCall() {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return client.newCall(request);
    }

    private String getTimbraturaId(Reader reader) throws RequestException {

        BufferedReader linesReader = new BufferedReader(reader);
        try {
            for (String line = linesReader.readLine(); line != null; line = linesReader.readLine()) {
                if (line.startsWith(C_ID_KEY)) {
                    return getId(line);
                }
            }
        } catch (IOException e) {
            throw new RequestException("Unable to read the timbratura ID", e);
        }
        throw new RequestException("Timbratura ID not found");
    }

    private String getId(String line) {
        String[] split = line.split(C_ID_KEY);
        return split[1].replace("';", "");
    }

}
