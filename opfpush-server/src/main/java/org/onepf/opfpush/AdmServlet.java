package org.onepf.opfpush;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.google.cloud.sql.jdbc.Connection;
import org.json.simple.*;
import javax.net.ssl.HttpsURLConnection;
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

        try {
            sendMessageToDevice(message, REGISTRATION_ID, "msg", 3600);
        } catch (Exception e) {
            _log.info(e.getMessage());
            resp.sendRedirect("/asp.jsp?message=exception=" + e.getMessage());
            return;
        }

        _log.info("Message posted: " + message);
        resp.sendRedirect("/asp.jsp?message=" + message);
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
        URL url = new URL(AMAZON_ADM_URL + device + "/messages");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setDoOutput(true);
        request.setRequestMethod("POST");
        request.setRequestProperty("Content-Type", "application/json");
        request.setRequestProperty("Accept", "application/json");
        request.setRequestProperty("x-amzn-type-version", "com.amazon.device.messaging.ADMMessage@1.0");
        request.setRequestProperty("x-amzn-accept-type", "com.amazon.device.messaging.ADMSendResult@1.0");
        request.setRequestProperty("Authorization", "Bearer " + _authToken);

        String timeStamp = DateTime.now().toDateTimeISO().toString();

        JSONObject json = new JSONObject();

        JSONObject data = new JSONObject();
        data.put("message", message);
        data.put("timeStamp", timeStamp);

        json.put("data", data);
        json.put("consolidationKey", consolidationKey);
        json.put("expiresAfter", expiresAfter);
        json.put("md5", ""); // TODO: implement calculate checksum

        _log.info("Request data: " + json.toJSONString());

        // Send the encoded parameters on the connection.
        OutputStream os = request.getOutputStream();
        os.write(json.toJSONString().getBytes("UTF-8"));
        os.flush();
        request.connect();

        // Convert the response into a String object.
        String responseContent = parseResponse(request.getInputStream());
        //JSONObject parsedObject = (JSONObject) JSONValue.parse(responseContent);
        _log.info("Response data: " + responseContent);

        return responseContent;

        /*
        try:
        // POST EnqueueMessage request to AMD Servers.
        response = urllib2.urlopen(req, json.dumps(JSON_MSG_REQUEST))

        // Retreiving Amazon ADM request ID.Include this with troubleshooting reports.
                X_Amzn_RequestId = response.info().get('x-amzn-RequestId')

        // Retreiving the MD5 value computed by ADM servers.
        MD5_from_ADM = response.info().get('x-amzn-data-md5')
        print "ADM server md5_checksum " + MD5_from_ADM

        // Checking if the app 's registration ID needs to be updated.
        response_data = json.load(response)
        canonical_reg_id = response_data['registrationID']
        if device != canonical_reg_id:
        print "Updating registration Id"
        if self.devices.has_key(device):
        self.devices.pop(device)
        self.devices[canonical_reg_id] = canonical_reg_id
        return 'Message sent.'
        except urllib2.HTTPError as e:
        error_reason = json.load(e)['reason']
        if e.code == 400:
        return 'Handle ' + str(e) + '. invalid input. Reason: ' + error_reason
        elif e.code == 401:
        return self.handle_invalid_token_error(e)
        elif e.code == 403:
        return 'Handle ' + str(e) + '. max rate exceeded. Reason: ' + error_reason
        elif e.code == 413:
        return 'Handle ' + str(e) + '. message greater than 6KB. Reason: ' + error_reason
        elif e.code == 500:
        return 'Handle ' + str(e) + '.  internal server error'
        elif e.code == 503:
        return self.handle_server_temporarily_unavailable_error(e)
        else:
        return 'Message was not sent',str(e)
        except urllib2.URLError as e:
        return 'Message was not sent','URLError: ' + str(e.reason)
        except urllib2.HTTPException as e:
        return 'Message was not sent','HTTPException: ' + str(e)
        except Exception as e:
        return 'Message was not sent','Exception: ' + str(e)
        */
    }
}
