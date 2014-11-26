

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * Servlet implementation class process
 */
public class process extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public process() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
        try {

            // Parse the work to be done from the POST request body.
        	TweetRequest tweetRequest = TweetRequest.fromJson(request.getInputStream());

        	System.out.println(getSentiment(tweetRequest.getText()));
        	
        	
        	
        	
        	
            
            // Signal to beanstalk that processing was successful so this work
            // item should not be retried.
            response.setStatus(200);

        } catch (RuntimeException exception) {
            
            // Signal to beanstalk that something went wrong while processing
            // the request. The work request will be retried several times in
            // case the failure was transient (eg a temporary network issue
            // when writing to Amazon S3).
            
            response.setStatus(500);
            try (PrintWriter writer =
                 new PrintWriter(response.getOutputStream())) {
                exception.printStackTrace(writer);
            }
        }
	}
	
	private String getSentiment(String text) throws ClientProtocolException, IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://access.alchemyapi.com/calls/text/TextGetTextSentiment");

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(3);
        String apiKey = getApiKey();
        params.add(new BasicNameValuePair("apikey", apiKey));
        params.add(new BasicNameValuePair("text", text));
        params.add(new BasicNameValuePair("outputMode", "json"));

        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        String ret = null;
        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                // do something useful
                ret = EntityUtils.toString(entity);
            } finally {
                instream.close();
            }
        }
        return ret;
	}
	
	private String getApiKey() {
		InputStream password = Thread.currentThread().getContextClassLoader().getResourceAsStream("api_key.ini");
        String pass = null;
        pass = new Scanner(password).next();
        return pass;
	}

}