package neoAtlantis.utilidades.accessController.utils;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neoAtlantis.utilidades.accessController.AccessController;
import neoAtlantis.utilidades.accessController.utils.html.AuditHtmlFormatter;
import org.apache.log4j.Logger;

/**
 * Servlet que permite visualizar el listado de eventos de la bitacora.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class AuditViewerServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AuditViewerServlet.class);
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        OutputStream out=response.getOutputStream();
        AccessController ctrl=AccessController.getInstance();//(AccessController)request.getSession().getAttribute(AccessControllerPublisher.CLAVE_CTRL);

        if( ctrl==null ){
            out.write(("<font color='red'><b>No se tienen Control de Acceso para trabajar.</b></font>\n").getBytes());
            LOGGER.debug("No existen Control de Acceso en memoria.");
        }
        else{
            LOGGER.debug("Accion generada: "+request.getParameter("accion"));
            AuditHtmlFormatter.formateaBitacora(request, ctrl.recuperaDatosBitacora(null, 5, 1), null);
        }

        out.flush();
        out.close();
        out=null;
    }
    
    
}
