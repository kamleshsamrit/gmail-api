package com.gmailapi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.ssl.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.gmailapi.GmailBodyParser;
import com.gmailapi.GmailBodyParserJson;
import com.gmailapi.GmailEmailParser;


 /**
  * 
  * @author Kamlesh
  *
  */

public class GmailInboxReader {
	
	public static final String GOOGLE_CLIENT_ID = "PASTE YOUR GOOGLE API KEY HERE";
	public static final String GOOGLE_CLIENT_SECRET = "PASTE YOUR GOOGLE API SECRETE KEY HERE";
	public static final String GOOGLE_REDIRECT_URL = "PASTE YOUR REDIRECT URL HERE";
	private static final String SCOPE = "https://mail.google.com/ https://www.googleapis.com/auth/gmail.send  https://www.googleapis.com/auth/userinfo.email";
	private static final Token EMPTY_TOKEN = null;
 
	/** Create OAuthService for Google OAuth 2.0 <br>
	 * This method Builds authorization url 
	 * */
    private static OAuthService getOAuthService(){
         return new ServiceBuilder()
      		  .provider(Google2Api.class)
              .apiKey(GOOGLE_CLIENT_ID).apiSecret(GOOGLE_CLIENT_SECRET)
              .callback(GOOGLE_REDIRECT_URL)
              .scope(SCOPE)
              .build();
      }
    
    /**
     * This method returns Authorization Url
     */
      public static String getAuthorizationUrl(){
      	OAuthService au=getOAuthService();
          System.out.println("Fetching the Authorization URL...");
          String authorizationUrl = au.getAuthorizationUrl(null);
          authorizationUrl=authorizationUrl+"&approval_prompt=force&access_type=offline";
          System.out.println("Got the Authorization URL: "+authorizationUrl);        
         	return authorizationUrl;
     }
      /**
       * To get AccessToken pass Authorization code to this method
       * It returns AccessToken
       */
      public static String getToken(String code){
    	  OAuthService au=getOAuthService();
    	  Token accessToken = null;
       	  Verifier verifier = new Verifier(code);
	      System.out.println();
	      // Trade the Request Token and Verfier for the Access Token
	      System.out.println("Trading the Request Token for an Access Token...");
	      accessToken = au.getAccessToken(EMPTY_TOKEN, verifier);
	      System.out.println("Got the Access Token!");
	      System.out.println("AccessToken: " + accessToken.getToken());					// It gives only access token
	      System.out.println("AccessToken Raw response: ");
	      System.out.println(accessToken.getRawResponse());   							// Parse Json response and save both AccessToken and Refresh Token
	      System.out.println();
	      String token=accessToken.getRawResponse();
	      System.out.println("Final Token:  "+token);
	     
	      return token;
 }
      /**
       * To validate token for a long time this method will be use<br>
       * we have to provide refreshToken to validate existing short life accessToken
       */
      public static String exchangeRefreshToken(String refreshtoken) throws Exception {
  		
  		
  		System.out.println("************Exchange Token Method*****************");
  		
  		String url = "https://www.googleapis.com/oauth2/v4/token";					// request URL for refersh token

  		HttpClient client = new DefaultHttpClient();
  		HttpPost post = new HttpPost(url);
  		// add header
  		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
  		
  		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
  		urlParameters.add(new BasicNameValuePair("client_id", GOOGLE_CLIENT_ID));
  		urlParameters.add(new BasicNameValuePair("client_secret", GOOGLE_CLIENT_SECRET));
  		urlParameters.add(new BasicNameValuePair("redirect_uri", GOOGLE_REDIRECT_URL));
  		urlParameters.add(new BasicNameValuePair("refresh_token", refreshtoken));				// replace refresh token here
  		urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
  		  		
  		post.setEntity(new UrlEncodedFormEntity(urlParameters));
  		HttpResponse response = client.execute(post);
  		System.out.println("\n Sending 'POST' request to URL : " + url);
  		System.out.println("Post parameters : " + post.getEntity());
  		System.out.println("Response Code : " +
                                   response.getStatusLine().getStatusCode());
  		
  		BufferedReader rd = new BufferedReader(
                       new InputStreamReader(response.getEntity().getContent()));

  		StringBuffer result = new StringBuffer();
  		String line = "";
  		while ((line = rd.readLine()) != null) {
  			result.append(line);
  		}

  		System.out.println(result.toString());						// here result comes in json format, it gives long lived Offline AccessToken 
  		return result.toString();

  	}
      //---------------------------------------------------------------------------------------------------------------------------------------------------
      /**
       * To send email through Gmail it must be in Base64Url format 
       * Pass all the parameter needed for email to this method it returns encoded mail in Base64Url format 
       */
      public static  String createEmail(String to, String from, String subject, String bodymsg) throws MessagingException, IOException {
	
	    Properties props = new Properties();
	    Session session = Session.getDefaultInstance(props, null);
	
	    MimeMessage email = new MimeMessage(session);
	    email.setFrom(new InternetAddress(from));												// set from address here
	    email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));		// set To address here
	    email.setSubject(subject);																// set subject here
	    //email.setText(bodyText);																// set body in text format
	    email.setContent(bodymsg, "text/html");
	    // set body in HTML format
	    email.reply(true);
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    email.writeTo(buffer);
	    byte[] bytes = buffer.toByteArray();
	    String encodedEmail = Base64.encodeBase64URLSafeString(bytes);							// Encode all mail data to Base64Url format
	  
	    System.out.println("Encoded mail:"+encodedEmail);
	    /*Message message = new Message();
	    message.setRaw(encodedEmail);
	    return message;*/
	    return encodedEmail;																	// Returns Encoded mail formats
}

      /**
       *  To send mail use this method 
       *  To send mail using this method we have to pass token and encoded (BAse64Url format) email structure
       */

		public static String sendEmail(String token, String encodedEmail) throws Exception {		
			
			String url = "https://www.googleapis.com/gmail/v1/users/me/messages/send";				// Request Url for sending mail 
		
			token="Bearer "+token;																	// set bearer token_type before token
			URL serverUrl = new URL(url);
			HttpURLConnection urlConnection = (HttpURLConnection)serverUrl.openConnection();
			
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Authorization", token);								// replace token here
			urlConnection.setRequestProperty("Content-Type", "application/json");
			
			String d = "{\r\n\"raw\": \""+encodedEmail+"\"\r\n}";									// make request in JSON format i.e, put encoded Bse64Url format into JSON
					BufferedWriter httpRequestBodyWriter = 
					new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
					//System.out.println("printing d: "+d);
					httpRequestBodyWriter.write(d);
					httpRequestBodyWriter.close();
					  
					BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
					 StringBuilder results = new StringBuilder();
				        String line;
				        while ((line = reader.readLine()) != null) {
				            results.append(line);
				        }
					//httpResponseScanner.close();
					urlConnection.disconnect();
					System.out.println("Response for Email Sending: "+results);
					return results.toString();
		}

//=========================================================================================================================================================================================
		/**
		 * Read User inbox to get emails
		 * @param token
		 * @return
		 * @throws IOException
		 */
		/**
		 * To get list of mails from gmail inbox use this method
		 * To get list make GET api call to request url with specific query parameters
		 */
		public static List getEmailList(String token) throws IOException{ 
			
			token="Bearer "+token;
			
			String url = "https://www.googleapis.com/gmail/v1/users/me/messages?maxResults=5"; // it reutrns only 5 recent emails

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("Authorization", token);
				
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			 StringBuilder results = new StringBuilder();
		        String line;
		        while ((line = reader.readLine()) != null) {
		            results.append(line);
		        }
			//httpResponseScanner.close();
			con.disconnect();
			String jsonString= results.toString();
			System.out.println("Response for Email List: "+jsonString);
			List msgidlist= GmailEmailParser.parse(jsonString);		// call parser
			return msgidlist;
		}
		
		/**
		 * To get body of particular message-id use getEmailBody() method with bellow parameters
		 * @param token
		 * @param id
		 * @return
		 * @throws IOException
		 */
		public static List getEmailBody(String token, String id) throws IOException{
		
			token="Bearer "+token;
			
			String url = "https://www.googleapis.com/gmail/v1/users/me/messages/"+id; // add ids

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("Authorization", token);
			con.setRequestProperty("format", "full");
			List emailBodyList = new ArrayList();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			 StringBuilder results = new StringBuilder();
		        String line;
		        while ((line = reader.readLine()) != null) {
		            results.append(line);
		            emailBodyList.add(results);
		        }
			con.disconnect();
			System.out.println("Response for Email Body List: "+results);
			return emailBodyList;
		}
		
		/**
		 * This method gets list from  both of above methods 
		 * @param token
		 * @throws IOException
		 */
		public static List getEmails(String token) throws IOException {
			
			// calls getEmailList() method to get List of emails
			List msgIdList= getEmailList(token);
			 System.out.println("msgIdList Size:  "+msgIdList.size());
			//System.out.println(msgIdList.get(0));.
			List bodyList = new ArrayList();
			for (int i = 0; i < msgIdList.size(); i++) {
				String id= (String) msgIdList.get(i);
				
				List emailBodyList = getEmailBody(token, id);
				System.out.println("emailBodyList Size:  "+emailBodyList.size());
				System.out.println(emailBodyList.get(i));
				bodyList.add(emailBodyList.get(i));
			}
			// now calls Gmail Body parser to get needed data(Email)
			List finallist = GmailBodyParser.getList(bodyList);
			System.out.println("FinalList Size: "+finallist.size());
			// Parse final list to get email 
			for (int i = 0; i < finallist.size(); i++) {
				//String id = (String) finallist.get(i);
				GmailBodyParserJson gm = (GmailBodyParserJson) finallist.get(i);
				String snippet = gm.getSnippet();
				System.out.println("id: "+id);
				System.out.println("snippet: "+snippet);
				List emailList = StrUtil.extractEmails(snippet);
				if(emailList.size() > 0){
				String email= (String) emailList.get(0);
				System.out.println("Email: "+email);
				}
			}
			
			return finallist;
		}
		
		public static void gmailBodyDecoder() throws Exception{
			  byte[] decodedmail = Base64.decodeBase64(PASTE YOUR GMAIL BODU IN BASE64URL FORMAT);
			  String s = new String(decodedmail);
			  System.out.println("decodedmail: "+s.toString());
			  List emailList = StrUtil.extractEmails(s);
			  
			  if(emailList.size()>=0){
				
			  String email= (String) emailList.get(emailList.size()-1);
			  //System.out.println("Emailfrom list: "+email);
			  //System.out.println("emailList: "+emailList);
			  
			  String domainname = email.substring(email.indexOf("@")+1, email.indexOf("."));
			 // System.out.println("domainname : "+domainname);
			  }else{
				  System.out.println("Emaillist empty");
			  }
		
			  }
//==========================================================================================================================================================================================
		
	public static void main(String[] args) throws Exception
		{
			String to="TO EMAIL ADDRESS";
			String from = "USER EMAIL ADDRESS";
			String subject = "This is subject for test mail";
			String bodyText = "Hi, this is body part of mail.......";
			String code="PASTE AUTHENTICATION CODE HERE";
			String refreshtoken="PASTE REFRESH TOKEN HERE";  		
			String token="PASTE ACCESSTOKEN HERE";
			String htmlbody="PASTE YOUR GMAIL HTML BODY";
			/**
			 * Call following methods for send email one bye one and comment other when one is in use
			 */
			//String authorizationUrl= getAuthorizationUrl();						// call this method to get authorization token				
			//System.out.println(authorizationUrl);	
			//String token = getToken(code);										// call this method to get access token from google
			//exchangeRefreshToken(refreshtoken);
			//String encodedEmail= createEmail(to, from, subject, htmlbody);		// call this method to convert plain text mail into Base64Url format
			//sendEmail(token, encodedEmail);										// call this method to send mail
			//getEmails(token);
			//gmailBodyDecoder();
			//getEmailBody(token, MSG_ID);
		}
}