package it.buch85.timbrum;

import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import it.buch85.timbrum.request.LoginRequest;
import it.buch85.timbrum.request.LoginRequest.LoginResult;
import it.buch85.timbrum.request.ReportRequest;
import it.buch85.timbrum.request.ServerTimeRequest;
import it.buch85.timbrum.request.TimbraturaRequest;


/**
 * Created by mbacer on 23/04/14.
 */
public class Timbrum {


    public static String LOGIN_URL             = "/servlet/cp_login";
    public static String TIMBRUS_URL           = "/servlet/ushp_ftimbrus";
    public static String SQL_DATA_PROVIDER_URL = "/servlet/SQLDataProviderServer";
    public static String SERVER_TIME_URL = "/servlet/ushp_fservertime";

    private final String              username;
    private final String              password;
	private BasicHttpContext context;
	private HttpClient httpClient;
	private String host;

    public Timbrum(String host, String username, String password) {
        this.host = host;
		this.username = username;
        this.password = password;
        httpClient = new DefaultHttpClient();
        context=new BasicHttpContext();
        BasicCookieStore cookieStore=new BasicCookieStore();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    public Report getReport(Date date) throws Exception {
        ReportRequest report = new ReportRequest( httpClient,context);
        report.setUrl(host+SQL_DATA_PROVIDER_URL);
        return new Report(report.getTimbrature(date));
    }

    public Date now() throws IOException, ParseException {
    	ServerTimeRequest serverTime=new ServerTimeRequest(httpClient,context);
    	serverTime.setUrl(host + SERVER_TIME_URL);
    	return serverTime.now();
    }
    
    
    public LoginResult login() throws IOException {
        LoginRequest login = new LoginRequest( httpClient,context);
        login.setUrl(host+LOGIN_URL);
        login.setUsername(username);
        login.setPassword(password);
        return login.submit();
    }

    public void timbra(VersoTimbratura verso) throws IOException {
        TimbraturaRequest timbratura = new TimbraturaRequest(httpClient,context);
        timbratura.setUrl(host+TIMBRUS_URL);
        if (VersoTimbratura.ENTRATA.equals(verso)) {
           timbratura.entrata();
        } else {
           timbratura.uscita();
        }
    }

}
