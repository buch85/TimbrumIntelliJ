package it.buch85.timbrum.request;


import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbacer on 11/04/14.
 */
public class LoginRequest extends AbstractRequest {
    String username;
    String password;
    private static String USERNAME_FIELD = "m_cUserName";
    private static String PASSWORD_FIELD = "m_cPassword";
    private static String ACTION_FIELD = "m_cAction";
    private static String ACTION_FIELD_VALUE = "login";

    private static String REDIRECT_OK_URL = "/jsp/home.jsp";

    public LoginRequest(HttpClient httpclient, HttpContext context) {
        super(httpclient, context);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LoginResult submit() throws IOException {
    	request = new HttpPost(URI.create(url));
    	List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair(USERNAME_FIELD, username));
        formparams.add(new BasicNameValuePair(PASSWORD_FIELD, password));
        formparams.add(new BasicNameValuePair(ACTION_FIELD, ACTION_FIELD_VALUE));
        request.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
        HttpClientParams.setRedirecting(httpclient.getParams(), false);
        HttpResponse response = httpclient.execute(request,context);
        response.getEntity().consumeContent();
        String message="";
        if (response.getStatusLine().getStatusCode() == 302) {
            Header[] location = response.getHeaders("Location");
            if (location.length > 0 && location[0].getValue().endsWith(REDIRECT_OK_URL)) {
                return new LoginResult(true,message);
            }
        } else {
            Header[] jsurlMessage = response.getHeaders("JSURL-Message");
            if (jsurlMessage.length > 0) {
                message = jsurlMessage[0].getValue();
            }
        }
        return new LoginResult(false, message);
    }

    
    public class LoginResult{
    	boolean success=false;
    	String message="";
    	public LoginResult(boolean success,String message) {
    		this.success=success;
    		this.message=message;
		}
    	
    	public boolean isSuccess() {
			return success;
		}
    	public String getMessage() {
			return message;
		}
    }
}
