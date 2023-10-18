package neoatlantis.accesscontroller.resourceAccessAllower;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import neoatlantis.accesscontroller.exceptions.WayConfigurationException;
import org.apache.log4j.Logger;

/**
 * Validador de Acceso a Recursos operado a traves de un XML, cual debe apegarse 
 * funciona igual que el XmlRoleResourceAccess, solo que si no se tienen los permisos
 * genera un error 503, en lugar de re-direcionar al home
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class XmlRole403ResourceAccess extends XmlRoleResourceAccess {
    private static final Logger DEBUGER = Logger.getLogger(XmlRole403ResourceAccess.class);

    
    // Contructores ------------------------------------------------------------
    
    public XmlRole403ResourceAccess(String xml) throws WayConfigurationException{
        super(false, xml);
    }
    
    public XmlRole403ResourceAccess(boolean restrictivo, String xml) throws WayConfigurationException{
        super(restrictivo, xml);
    }
    
    /**
     * Genera un error 503 dado que los permisos
     * del usuario no son suficientes.
     * @param request Resquest de la peticion web
     * @param response  Response de la peticion web
     */
    @Override
    protected void accessDenied(ServletRequest request, ServletResponse response){
        try{
            DEBUGER.debug("Acceso no autorizado, genero error 403");

            ((HttpServletResponse)response).sendError(403);
        }
        catch(Exception ex){
            DEBUGER.error("No se logro generar el error 403.", ex);
        }
    }

}
