package org.onepf.opfpush;

import com.google.appengine.api.datastore.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class AdmRegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger _log = Logger.getLogger(AdmRegisterServlet.class.getName());
    private static DatastoreService _datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("/admregister.jsp");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Text txtRegId = new Text(req.getParameter("txtRegId"));

        // Creates device token entity and saves it in the database
        Entity regId = new Entity("ADMDeviceIds");
        regId.setProperty("regid", txtRegId);

        if (!isReqIdExist(txtRegId)) {
            _datastore.put(regId);
            _log.info("RegId inserted into DB: " + txtRegId);
        }

        resp.sendRedirect("/admregister.jsp");
    }

    // Checks if the device token already exist in the database
    private boolean isReqIdExist(Text regId) {
        Query gaeQuery = new Query("ADMDeviceIds");
        PreparedQuery pq = _datastore.prepare(gaeQuery);
        for (Entity entity : pq.asIterable()) {
            Text id = (Text) entity.getProperty("regid");
            if (id != null && id.equals(regId))
                return true;
        }
        return false;
    }
}
