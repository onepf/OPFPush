package org.onepf.opfpush;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.appengine.api.datastore.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Logger;

public class GcmServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(GcmServlet.class.getName());
    // API_KEY is sender_auth_token (server key previously generated in GCM)
    private static final String API_KEY = "AIzaSyCue1O-_9pFmAovLklyKEukfxuMp9q3bxY";
    private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


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
            MulticastResult result = sender.send(message, devices, 5);
            log.info("Message posted: " + utf8txtInput);
            resp.sendRedirect("/gcm.jsp?message=" + utf8txtInput);
        } else {
            log.info("No devices registered.");
            resp.sendRedirect("/gcm.jsp?message=warning-no-devices");
        }
    }

    // Reads all previously stored device tokens from the database
    private ArrayList<String> getAllRegIds() {
        ArrayList<String> regIds = new ArrayList<String>();
        Query gaeQuery = new Query("GCMDeviceIds");
        PreparedQuery pq = datastore.prepare(gaeQuery);
        for (Entity result : pq.asIterable()) {
            String id = (String) result.getProperty("regid");
            regIds.add(id);
        }

        return regIds;
    }
}
