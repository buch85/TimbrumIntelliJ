package it.buch85.timbrum.request;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginRequest {

    private static final String USERNAME_FIELD = "m_cUserName";
    private static final String PASSWORD_FIELD = "m_cPassword";
    private static final String ACTION_FIELD = "m_cAction";
    private static final String ACTION_FIELD_VALUE = "login";

    private static final String REDIRECT_OK_URL = "/jsp/home.jsp";

    private final OkHttpClient client;
    private final String url;
    private final String username;
    private final String password;

    public LoginRequest(OkHttpClient client, String url, String username, String password) {
        this.client = client;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public LoginResult submit() throws IOException {

        RequestBody formBody = new FormBody.Builder()
                .add(USERNAME_FIELD, username)
                .add(PASSWORD_FIELD, password)
                .add(ACTION_FIELD, ACTION_FIELD_VALUE)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        String message = "";

        if (response.code() == 302) {
            String location = response.header("Location");
            if (location != null && location.endsWith(REDIRECT_OK_URL)) {
                return new LoginResult(true, message);
            }
        } else {
            message = response.header("JSURL-Message");
        }
        return new LoginResult(false, message);
    }
}
