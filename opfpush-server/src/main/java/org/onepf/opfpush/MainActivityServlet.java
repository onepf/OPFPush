package org.onepf.opfpush;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class MainActivityServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MainActivityServlet.class.getName());
	// API_KEY is sender_auth_token (server key previously generated in GCM)
	private static final String API_KEY = "AIzaSyCue1O-_9pFmAovLklyKEukfxuMp9q3bxY";
	// Datastore is database where all device tokens get stored
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	
	// Handles HTTP GET request from the main.jsp 
	@Override
	protected void doGet(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		resp.sendRedirect("/main.jsp"); 
	}
	
	// Handles HTTP POST request - submit message from the main.jsp
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException { 
    	String txtInput = req.getParameter("txtInput");
    	
    	// Instantiating sender for dispatching message to GCM
    	Sender sender = new Sender(API_KEY);
    	// Creating a message for GCM
    	Message message = new Message
    			.Builder()
				.addData("message", txtInput)
    			.build();
    	
    	ArrayList<String> devices = getAllRegIds();
    	if(!devices.isEmpty()){
    		// Sending multicast message to GCM specifying all targeting devices
	    	MulticastResult result = sender.send(message, devices, 5);
	    	log.info("Message posted: " + txtInput);
			resp.sendRedirect("/main.jsp?message="+txtInput);
    	}else{
    		log.info("No devices registered.");
			resp.sendRedirect("/main.jsp?message=warning-no-devices");
    	}
    }
    
    // Reads all previously stored device tokens from the database
    private ArrayList<String> getAllRegIds(){
    	ArrayList<String> regIds = new ArrayList<String>();
    	Query gaeQuery = new Query("GCMDeviceIds");
    	PreparedQuery pq = datastore.prepare(gaeQuery);
    	for (Entity result : pq.asIterable()){
    		String id = (String) result.getProperty("regid");
    		regIds.add(id);
    	}
    	
    	return regIds;
    }
}
