package org.onepf.opfpush;

import com.google.appengine.api.datastore.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NokiaServlet extends HttpServlet {

    final static String NOKIA_URL = "https://nnapi.ovi.com/nnapi/2.0/send";
    final static String SENDER_ID = "pushsample";
    final static String AUTH_KEY = "key=cHVzaHNhbXBsZTpqdkIzZkxHQWV4N1RTZzlBRDRZRWcwNExLanhSbGtJa1hEaTg1VXBzbW1VPQ==";

    private static final Logger _log = Logger.getLogger(NokiaServlet.class.getName());
    private static DatastoreService _datastore = DatastoreServiceFactory.getDatastoreService();

    // Handles HTTP GET request from the gcm.jsp
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("/nokia.jsp");
    }

    // Handles HTTP POST request - submit message from the adm.jsp
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String txtInput = req.getParameter("txtInput");

        // Creating a message
        String message = URLEncoder.encode(txtInput, "UTF-8");
        String response = "";

        ArrayList<String> regIds = getAllRegIds();
        String[] regIdsArr = new String[regIds.size()];
        for (int i = 0; i < regIds.size(); ++i) {
            regIdsArr[i] = regIds.get(i);
        }
        if (!regIds.isEmpty()) {
            try {
                response = sendMessageToDevice(message, regIdsArr, 3600);
            } catch (Exception e) {
                _log.info(e.getMessage());
                resp.sendRedirect("/nokia.jsp?error=" + e.getMessage());
                return;
            }
            _log.info("Message posted: " + message + "; Response: " + response);
            resp.sendRedirect("/nokia.jsp?response=" + response + "?message=" + message);
        } else {
            _log.info("No devices registered.");
            resp.sendRedirect("/nokia.jsp?message=warning-no-devices");
        }
    }

    private String parseResponse(InputStream in) throws Exception {
        InputStreamReader inputStream = new InputStreamReader(in, "UTF-8");
        BufferedReader buff = new BufferedReader(inputStream);

        StringBuilder sb = new StringBuilder();
        String line = buff.readLine();
        while (line != null) {
            sb.append(line);
            line = buff.readLine();
        }

        return sb.toString();
    }

    private String sendMessageToDevice(String message, String[] registrationIds, int timeToLive) throws Exception {

        URL nokiaUrl = new URL(NOKIA_URL);

        // Generate the HTTPS connection for the POST request. You cannot make a connection over HTTP.
        HttpURLConnection conn = (HttpURLConnection) nokiaUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        // Set the content type and accept headers.
        conn.setRequestProperty("content-type", "application/json");

        // Add the authorization key as a header.
        conn.setRequestProperty("Authorization", AUTH_KEY);

        JSONObject json = new JSONObject();

        JSONArray regIds = new JSONArray();
        for (String id : registrationIds) {
            regIds.add(id);
        }
        json.put("registration_ids", regIds);
        json.put("time_to_live", timeToLive);

        JSONObject data = new JSONObject();
        data.put("payload", message);
        json.put("data", data);

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
        if (responseCode != 200) {
            String errorContent = parseResponse(conn.getErrorStream());
            throw new RuntimeException(String.format("ERROR: The enqueue request failed with a " +
                            "%d response code, with the following message: %s; JSON: %s",
                    responseCode, errorContent, json.toJSONString()));
        } else {
            // The request was successful. The response contains the canonical Registration ID for the specific instance of your
            // app, which may be different that the one used for the request.

            String responseContent = parseResponse(conn.getInputStream());
            JSONObject parsedObject = (JSONObject) JSONValue.parse(responseContent);
            return parsedObject.toJSONString();
        }
    }

    // Reads all previously stored device tokens from the database
    private ArrayList<String> getAllRegIds() {
        ArrayList<String> regIds = new ArrayList<String>();
        Query gaeQuery = new Query("NokiaDeviceIds");
        PreparedQuery pq = _datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()) {
            String id = (String) result.getProperty("regid");
            regIds.add(id);
        }

        return regIds;
    }
}
