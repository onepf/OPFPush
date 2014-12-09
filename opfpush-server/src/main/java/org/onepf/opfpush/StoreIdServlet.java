package org.onepf.opfpush;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class StoreIdServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MainActivityServlet.class.getName());
	// Datastore is database where all device tokens get stored
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	// Handles HTTP GET request from the storeid.jsp
	@Override
	protected void doGet(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		resp.sendRedirect("/storeid.jsp"); 
	}
	
	// Handles HTTP POST request - submit message from the storeid.jsp
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException { 
    	String txtRegId = req.getParameter("txtRegId");
    	
    	// Creates device token entity and saves it in the database
    	Entity regId = new Entity("GCMDeviceIds",txtRegId);
    	regId.setProperty("regid", txtRegId);
    	if(!isReqIdExist(txtRegId)){
    		saveToDB(regId);
    		log.info("RegId inserted into DB: " + txtRegId);
    	}
    }
    
    // Save device token in the database
    private void saveToDB(Entity regId){
    	datastore.put(regId);
    }
    
    // Checks if the device token already exist in the database
    private boolean isReqIdExist(String regId){
    	Key keyRegId = KeyFactory.createKey("GCMDeviceIds", regId);
    	Entity entity = null;
    	try {
			entity = datastore.get(keyRegId);
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if(entity!=null){
    		return true;
    	}
    	return false;
    }
}
