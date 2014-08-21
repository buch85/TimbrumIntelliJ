package it.buch85.timbrum.request;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ServerTimeRequest extends AbstractRequest {

    public static final String FUNCTION_RETURN_VALUE = "Function return value:";

    public ServerTimeRequest(HttpClient httpclient, HttpContext context) {
		super(httpclient, context);
	}

	public Date now() throws IOException, ParseException {
		request = new HttpPost(URI.create(url));
		HttpResponse response = httpclient.execute(request,context);
        HttpEntity entity = response.getEntity();
        String responseString=EntityUtils.toString(entity);
        entity.consumeContent();
        int start=responseString.lastIndexOf(FUNCTION_RETURN_VALUE)+ FUNCTION_RETURN_VALUE.length();
        int end=responseString.indexOf('\n',start);
        String date=responseString.substring(start,end);
        SimpleDateFormat parserSDF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        parserSDF.setTimeZone(TimeZone.getTimeZone("GMT+00"));
        return parserSDF.parse(date);
	}
	
	

}
