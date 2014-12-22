package org.onepf.opfpush;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.repackaged.org.joda.time.DateTime;
import org.json.simple.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Logger;

public class AdmServlet extends HttpServlet {

    // Client ID received from ADM.
    final static String PROD_CLIENT_ID = "amzn1.application-oa2-client.9ccf96d2987848438df279ccdcb235b0";

    // Client secret received from ADM.
    final static String PROD_CLIENT_SECRET = "66f69e71ada44843208a08f9ae3e40814d1c4383ef0486daa67ff00ff0517587";

    final static String REGISTRATION_ID = "amzn1.adm-registration.v3.Y29tLmFtYXpvbi5EZXZpY2VNZXNzYWdpbmcuUmVnaXN0cmF0aW9uSWRFbmNyeXB0aW9uS2V5ITEha2p1eGFHVDNhNzNGQ3I4WUViVjFjVURNTjZmTlU1QTBUc0tCZTlKSWFFOHQrQ2lPSXhXQ2xRR1ltaUd4Ly94aVByTFBYOGRxRUV2SnphdHNtQkdwdVZZR0cybzVFYjJ3S3Y5Zks2ZzlKVXpodHd4TlFhWHJzMHEweUttVVVEYklTWjBPL0pTdWhOMEtVY3BKM0s1cGFUd0d5Ri9iTlNiMTZ4YTFLcGt0UjJ1VXRyNnU0dlR4T3dLaGU3cHFjbi95dnk0dk1mWElrTEduZmhuQVZaRUhXL1hzM05UeEJkQjMrTmQxdUN1Z0ZEd3NGcHZYWlNGbi9sS3FVZnI3NFl1TEZ6Q1d5MXdxZXJkdVhPa0FxSXEzN0JXMWJyNGw1OXNmc3NUVDRKYkFHR2JqYVE3NTVNMFJXRnU0aXEra0NYSWR4eW5YSmk1M2RHRWNUZW1QVUlQTTBzS01qVnIvSUVaMXB0QkRCZzM0ZUhzPSF2VUgxb2pPeWVySzVuNko0MmtBUzJ3PT0";

    // Oauth2.0 token endpoint. This endpoint is used to request authorization tokens.
    final static String AMAZON_TOKEN_URL = "https://api.amazon.com/auth/O2/token";

    // ADM services endpoint. This endpoint is used to perform ADM requests.
    final static String AMAZON_ADM_URL = "https://api.amazon.com/messaging/registrations/";

    private static final Logger _log = Logger.getLogger(AdmServlet.class.getName());
    private static DatastoreService _datastore = DatastoreServiceFactory.getDatastoreService();

    private String _authToken = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            _authToken = getAuthToken(PROD_CLIENT_ID, PROD_CLIENT_SECRET);
            _log.info("Auth token: " + _authToken);
        } catch (Exception e) {
            _log.info(e.getMessage());
        }
    }

    // Handles HTTP GET request from the main.jsp
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("/adm.jsp");
    }

    // Handles HTTP POST request - submit message from the adm.jsp
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String txtInput = req.getParameter("txtInput");

        // Creating a message
        String message = URLEncoder.encode(txtInput, "UTF-8");
        String response = null;

        try {
            response = sendMessageToDevice(message, REGISTRATION_ID, "msg", 3600);
        } catch (Exception e) {
            _log.info(e.getMessage());
            resp.sendRedirect("/adm.jsp?exception=" + e.getMessage());
            return;
        }

        _log.info("Message posted: " + message);
        resp.sendRedirect("/adm.jsp?response=" + response + "?message=" + message);
    }

    private String parseResponse(InputStream in) throws Exception
    {
        InputStreamReader inputStream = new InputStreamReader(in, "UTF-8");
        BufferedReader buff = new BufferedReader(inputStream);

        StringBuilder sb = new StringBuilder();
        String line = buff.readLine();
        while (line != null )
        {
            sb.append(line);
            line = buff.readLine();
        }

        return sb.toString();
    }

    /**
     * HTTPS request to Amazon to obtain an access token
     */
    private String getAuthToken(String clientId, String clientSecret) throws Exception
    {
        // Encode the body of your request, including your clientID and clientSecret values.
        String body = "grant_type="    + URLEncoder.encode("client_credentials", "UTF-8") + "&" +
                      "scope="         + URLEncoder.encode("messaging:push", "UTF-8")     + "&" +
                      "client_id="     + URLEncoder.encode(clientId, "UTF-8")             + "&" +
                      "client_secret=" + URLEncoder.encode(clientSecret, "UTF-8");

        // Create a new URL object with the base URL for the access token request.
        URL authUrl = new URL(AMAZON_TOKEN_URL);

        // Generate the HTTPS connection. You cannot make a connection over HTTP.
        HttpURLConnection con = (HttpURLConnection) authUrl.openConnection();
        con.setDoOutput( true );
        con.setRequestMethod( "POST" );

        // Set the Content-Type header.
        con.setRequestProperty( "Content-Type" , "application/x-www-form-urlencoded" );
        con.setRequestProperty( "Charset" , "UTF-8" );
        // Send the encoded parameters on the connection.
        OutputStream os = con.getOutputStream();
        os.write(body.getBytes( "UTF-8" ));
        os.flush();
        con.connect();

        // Convert the response into a String object.
        String responseContent = parseResponse(con.getInputStream());

        // Create a new JSONObject to hold the access token and extract
        // the token from the response.
        JSONObject parsedObject = (JSONObject) JSONValue.parse(responseContent);
        return (String) parsedObject.get("access_token");
    }

    // Constructs and sends a request to ADM Servers to enqueue a message for delivery to a specific app instance.
    // Updates registration id if a newer one is received with the ADM server response.
    private String sendMessageToDevice(String message, String device, String consolidationKey, int expiresAfter) throws Exception {

        URL admUrl = new URL(AMAZON_ADM_URL + device + "/messages");

        // Generate the HTTPS connection for the POST request. You cannot make a connection
        // over HTTP.
        HttpURLConnection conn = (HttpURLConnection) admUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        // Set the content type and accept headers.
        conn.setRequestProperty("content-type", "application/json");
        conn.setRequestProperty("accept", "application/json");
        conn.setRequestProperty("X-Amzn-Type-Version ", "com.amazon.device.messaging.ADMMessage@1.0");
        conn.setRequestProperty("X-Amzn-Accept-Type", "com.amazon.device.messaging.ADMSendResult@1.0");

        // Add the authorization token as a header.
        conn.setRequestProperty("Authorization", "Bearer " + _authToken);

        JSONObject json = new JSONObject();

        JSONObject data = new JSONObject();
        String timeStamp = DateTime.now().toDateTimeISO().toString();
        data.put("message", message);
        data.put("timeStamp", timeStamp);

        json.put("data", data);
        json.put("consolidationKey", consolidationKey);
        json.put("expiresAfter", expiresAfter);
        //json.put("md5", ""); // TODO: implement calculate checksum

        _log.info("Request data: " + json.toJSONString());

        byte[] jsonString = json.toJSONString().getBytes("UTF-8");

        // Send the encoded parameters on the connection.
        OutputStream os = conn.getOutputStream();
        os.write(jsonString, 0, jsonString.length);
        os.flush();
        conn.connect();

        // Obtain the response code from the connection.
        int responseCode = conn.getResponseCode();
        _log.info("Response code: " + responseCode);

        // Check if we received a failure response, and if so, get the reason for the failure.
        if( responseCode != 200)
        {
            if( responseCode == 401 )
            {
                // If a 401 response code was received, the access token has expired. The token should be refreshed
                // and this request may be retried.
            }

            String errorContent = parseResponse(conn.getErrorStream());
            throw new RuntimeException(String.format("ERROR: The enqueue request failed with a " +
                            "%d response code, with the following message: %s",
                    responseCode, errorContent));
        }
        else
        {
            // The request was successful. The response contains the canonical Registration ID for the specific instance of your
            // app, which may be different that the one used for the request.

            String responseContent = parseResponse(conn.getInputStream());
            JSONObject parsedObject = (JSONObject) JSONValue.parse(responseContent);

            String canonicalRegistrationId = (String) parsedObject.get("registrationID");

            // Check if the two Registration IDs are different.
            if(!canonicalRegistrationId.equals(device))
            {
                // At this point the data structure that stores the Registration ID values should be updated
                // with the correct Registration ID for this particular app instance.
            }

            return parsedObject.toJSONString();
        }
    }
}
