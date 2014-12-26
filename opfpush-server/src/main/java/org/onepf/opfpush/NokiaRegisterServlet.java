package org.onepf.opfpush;

import com.google.appengine.api.datastore.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class NokiaRegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger _log = Logger.getLogger(NokiaRegisterServlet.class.getName());
    private static DatastoreService _datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("/nokiaregister.jsp");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String txtRegId = req.getParameter("txtRegId");

        // Creates device token entity and saves it in the database
        Entity regId = new Entity("NokiaDeviceIds", txtRegId);
        regId.setProperty("regid", txtRegId);
        if (!isReqIdExist(txtRegId)) {
            saveToDB(regId);
            _log.info("RegId inserted into DB: " + txtRegId);
        }

        resp.sendRedirect("/nokiaregister.jsp");
    }

    // Save device token in the database
    private void saveToDB(Entity regId) {
        _datastore.put(regId);
    }

    // Checks if the device token already exist in the database
    private boolean isReqIdExist(String regId) {
        Key keyRegId = KeyFactory.createKey("NokiaDeviceIds", regId);
        Entity entity = null;
        try {
            entity = _datastore.get(keyRegId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        if (entity != null) {
            return true;
        }
        return false;
    }
}
