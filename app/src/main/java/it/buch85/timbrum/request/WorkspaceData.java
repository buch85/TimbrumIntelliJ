package it.buch85.timbrum.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WorkspaceData {

    public static final String C_ID_KEY = "this.splinker10.m_cID='";
    public static final String GETTIMBRUS_CMD = "rows:ushp_fgettimbrus";

    private final OkHttpClient client;
    private final String url;

    private Map<String, String> workspaceData = new HashMap<>();
    private String TIMBRATURA_ID = "TIMBRATURA_ID";

    public WorkspaceData(OkHttpClient client, String url) throws RequestException {
        this.client = client;
        this.url = url;
        this.initWorkspace();
    }

    private void initWorkspace() throws RequestException {
        Call call = createCall();

        try {
            Response response = call.execute();
            ResponseBody body = response.body();

            if (body == null) {
                throw new RequestException("Empty body in workspace request");
            }

            loadVariables(body.charStream());
        } catch (IOException e) {
            throw new RequestException("Unable to load workspace", e);
        }
    }

    public String getTimbraturaId() {
        return workspaceData.get(TIMBRATURA_ID);
    }

    public String getCmdHash(String cmd) {
        return workspaceData.get(cmd);
    }

    private Call createCall() {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return client.newCall(request);
    }

    private void loadVariables(Reader reader) throws RequestException {
        BufferedReader linesReader = new BufferedReader(reader);
        try {
            for (String line = linesReader.readLine(); line != null; line = linesReader.readLine()) {
                if (line.startsWith(C_ID_KEY)) {
                    this.workspaceData.put(TIMBRATURA_ID, getId(line));
                }
                if (line.contains(GETTIMBRUS_CMD)) {
                    this.workspaceData.put(GETTIMBRUS_CMD, getCmdHash(line, GETTIMBRUS_CMD));
                }
            }
        } catch (IOException e) {
            throw new RequestException("Unable to read Workspace data", e);
        }
    }

    private String getCmdHash(String line, String cmd) {
        String value = line.substring(line.indexOf(cmd) + cmd.length() + 13);
        value = value.substring(0, value.indexOf("\""));
        return value;
    }

    private String getId(String line) {
        String[] split = line.split(C_ID_KEY);
        return split[1].replace("';", "");
    }
}
