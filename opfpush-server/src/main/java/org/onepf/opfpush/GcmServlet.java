package org.onepf.opfpush;

import com.google.android.gcm.server.*;
import com.google.appengine.api.datastore.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GcmServlet extends HttpServlet {

    // API_KEY is sender_auth_token (server key previously generated in GCM)
    private static final String API_KEY = "AIzaSyCue1O-_9pFmAovLklyKEukfxuMp9q3bxY";

    private static final Logger _log = Logger.getLogger(GcmServlet.class.getName());
    private static DatastoreService _datastore = DatastoreServiceFactory.getDatastoreService();


    // Handles HTTP GET request from the gcm.jsp
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("/gcm.jsp");
    }

    // Handles HTTP POST request - submit message from the gcm.jsp
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String txtInput = req.getParameter("txtInput");

        // Instantiating sender for dispatching message to GCM
        Sender sender = new Sender(API_KEY);
        // Creating a message for GCM
        String utf8txtInput = URLEncoder.encode(txtInput, "UTF-8");
        Message message = new Message.Builder().addData("message", utf8txtInput).build();

        ArrayList<String> devices = getAllRegIds();
        if (!devices.isEmpty()) {
            // Sending multicast message to GCM specifying all targeting devices
            MulticastResult multicastResult = sender.send(message, devices, 5);
            List<Result> results = multicastResult.getResults();
            // analyze the results
            for (int i = 0; i < devices.size(); i++) {
                String regId = devices.get(i);
                Result result = results.get(i);
                String messageId = result.getMessageId();
                if (messageId != null) {
                    _log.fine("Succesfully sent message to device: " + regId + "; messageId = " + messageId);
                    String canonicalRegId = result.getCanonicalRegistrationId();
                    if (canonicalRegId != null) {
                        // same device has more than on registration id: update it
                        _log.info("canonicalRegId " + canonicalRegId);
                        Entity entity = null;
                        try {
                            entity = _datastore.get(KeyFactory.createKey("GCMDeviceIds", regId));
                        } catch (EntityNotFoundException e) {
                            _log.info("ID not found: " + regId);
                        }
                        if (entity != null) {
                            entity.setProperty("regid", regId);
                            _datastore.put(entity);
                        }
                    }
                } else {
                    String error = result.getErrorCodeName();
                    if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                        // application has been removed from device - unregister it
                        _log.info("Unregistered device: " + regId);
                        _datastore.delete(KeyFactory.createKey("GCMDeviceIds", regId));
                    } else {
                        _log.severe("Error sending message to " + regId + ": " + error);
                    }
                }
            }
            _log.info("Message posted: " + utf8txtInput);
            resp.sendRedirect("/gcm.jsp?message=" + utf8txtInput);
        } else {
            _log.info("No devices registered.");
            resp.sendRedirect("/gcm.jsp?message=warning-no-devices");
        }
    }

    // Reads all previously stored device tokens from the database
    private ArrayList<String> getAllRegIds() {
        ArrayList<String> regIds = new ArrayList<String>();
        Query gaeQuery = new Query("GCMDeviceIds");
        PreparedQuery pq = _datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()) {
            String id = (String) result.getProperty("regid");
            regIds.add(id);
        }

        return regIds;
    }
}
