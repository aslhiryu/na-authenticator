package neoAtlantis.utils.accessController.web.utils;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;
import neoAtlantis.utils.apps.web.utils.ElementPublisherServlet;
import org.apache.log4j.Logger;

/**
 * Clase de apoyo que carga todos los elementos web necesarios por las librerias
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class AuthenticationResourcesLoader {
    private static final Logger DEBUGGER = Logger.getLogger(AuthenticationResourcesLoader.class);
    
    /**
     * Clave con la que se localiza la ruta del login para el acceso
     */
    public static String PATH_ADMIN_USERS_JS="/neoAtlantis/resources/scripts/NA_adminUsers.js";
    public static String PATH_NO_PHOTO_PNG="/neoAtlantis/resources/images/NA_noPhoto.png";
    public static String PATH_ADMIN_USERS_CSS="/neoAtlantis/resources/styles/NA_adminUsers.css";

    
    
    
    
    
    // Metodos publicos estaticos-----------------------------------------------

    public static void loadResorces(ServletContext context){        
        //publica la hoja de estilos
        ElementPublisherServlet servlet=new ElementPublisherServlet(AuthenticationResourcesLoader.class.getResourceAsStream("adminUsers.js"), "NA_adminUsers.js", "text/javascript");
        Dynamic dim=context.addServlet("ScriptAdminUsers", servlet);
        dim.addMapping(PATH_ADMIN_USERS_JS);
        dim.setAsyncSupported(true);
        DEBUGGER.debug("Publico el javascript de administracion de usuarios en: "+PATH_ADMIN_USERS_JS);
        
        servlet=new ElementPublisherServlet(AuthenticationResourcesLoader.class.getResourceAsStream("noPhoto.png"), "NA_noPhoto.png", "image/png");
        dim=context.addServlet("ImageNoPhotoUser", servlet);
        dim.addMapping(PATH_NO_PHOTO_PNG);
        dim.setAsyncSupported(true);
        DEBUGGER.debug("Publico la imagen de sin foto en: "+PATH_NO_PHOTO_PNG);

        servlet=new ElementPublisherServlet(AuthenticationResourcesLoader.class.getResourceAsStream("adminUsers.css"), "NA_adminUsers.css", "text/stylesheet");
        dim=context.addServlet("StylesAdminUsers", servlet);
        dim.addMapping(PATH_ADMIN_USERS_CSS);
        dim.setAsyncSupported(true);
        DEBUGGER.debug("Publico los estlos de administracion de usuarios en: "+PATH_ADMIN_USERS_CSS);
    }
}
