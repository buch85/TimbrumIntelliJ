package it.buch85.timbrum;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import it.buch85.timbrum.request.LoginRequest;
import it.buch85.timbrum.request.LoginResult;
import it.buch85.timbrum.request.ReportRequest;
import it.buch85.timbrum.request.RequestException;
import it.buch85.timbrum.request.ServerTimeRequest;
import it.buch85.timbrum.request.TimbraturaRequest;
import it.buch85.timbrum.request.WorkspaceRequest;
import okhttp3.CookieJar;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;


/**
 * Created by mbacer on 23/04/14.
 */
public class Timbrum {

    private static final String LOGIN_URL = "/servlet/cp_login";
    private static final String TIMBRUS_URL = "/servlet/ushp_ftimbrus";
    private static final String SQL_DATA_PROVIDER_URL = "/servlet/SQLDataProviderServer";
    public static final String SERVER_TIME_URL = "/servlet/ushp_fservertime";
    private static final String WORKSPACE_URL = "/jsp/gsmd_container.jsp?containerCode=MYDESK&pTitle=My%20Workspace";

    private final String username;
    private final String password;
    private final String host;

    private final OkHttpClient client = createClient();

    public Timbrum(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    private OkHttpClient createClient() {
        final OkHttpClient client;
        //todo
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieJar cookieJar = new JavaNetCookieJar(cookieManager);

        client = new OkHttpClient.Builder()
                .followRedirects(false)
                .cookieJar(cookieJar)
                .build();
        return client;
    }

    public Report getReport(Date date) throws Exception {
        ReportRequest report = new ReportRequest(client, host + SQL_DATA_PROVIDER_URL);
        return new Report(report.getTimbrature(date));
    }

    public Date now() throws IOException, ParseException {
        ServerTimeRequest serverTime = new ServerTimeRequest(client, host + SERVER_TIME_URL);
        return serverTime.now();
    }

    public LoginResult login() throws IOException {
        LoginRequest login = new LoginRequest(client, host + LOGIN_URL, username, password);
        return login.submit();
    }


    public void timbra(VersoTimbratura verso, String timbraturaId) throws IOException {
        TimbraturaRequest timbratura = new TimbraturaRequest(client, host + TIMBRUS_URL);
        if (VersoTimbratura.ENTRATA == verso) {
            timbratura.entrata(timbraturaId);
        } else {
            timbratura.uscita(timbraturaId);
        }
    }

    public String loadTimbraturaId() throws RequestException {
        WorkspaceRequest workspaceRequest = new WorkspaceRequest(client, host + WORKSPACE_URL);
        return workspaceRequest.loadTimbraturaId();
    }

}
