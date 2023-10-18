package neoAtlantis.utils.accessController.resourceAccessAllower.utils;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neoAtlantis.utils.accessController.AccessController;
import neoAtlantis.utils.accessController.audit.interfaces.EventAudit;
import neoAtlantis.utils.accessController.blocker.interfaces.BlockerWay;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.accessController.resourceAccessAllower.interfaces.AccessEstatus;
import neoAtlantis.utils.accessController.resourceAccessAllower.interfaces.DenialType;
import neoAtlantis.utils.accessController.resourceAccessAllower.interfaces.FilterException;
import neoAtlantis.utils.accessController.resourceAccessAllower.interfaces.ResourceAccessAllower;
import neoAtlantis.utils.accessController.resourceAccessAllower.interfaces.TypeException;
import neoAtlantis.utils.accessController.web.listeners.AccessControllerPublisher;
import neoAtlantis.utils.accessController.web.listeners.BlockerSessionListener;
import neoAtlantis.utils.apps.web.listeners.SessionListener;
import neoAtlantis.utils.apps.web.objects.ApplicationSession;
import neoAtlantis.utils.configurations.ClassGenerator;
import org.apache.log4j.Logger;
import org.jdom.*;

/**
 * Permite la carga de la configuraci&oacute;n de un Validador de Acceso a Recursos
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class ValidatorConfigurator {
    /**
     * Loggeador de la clase
     */
    private static final Logger DEBUGER = Logger.getLogger(ValidatorConfigurator.class);

    /**
     * Obtiene las recursos a omitirse de validaci&oacute;n.
     * @param config Documento XML con la configurac´&oacute;n
     * @return Lista de recursos a omitirse
     */
    public static ArrayList<FilterException> getExceptions(Element root){
        Element e;
        ArrayList<FilterException> pags=new ArrayList<FilterException>();
        List<Element> lTmp;

        //genera una excepcion para los recursos de la propia libreria
        pags.add(new FilterException("neoAtlantis", TypeException.DIRECTORY));
        
        //valido que existean excepciones
        if( root.getChild("exceptions")!=null ){
            //recupero las paginas
            for(int i=0; root.getChild("exceptions").getChildren()!=null&&i<root.getChild("exceptions").getChildren().size(); i++){
                e=(Element)root.getChild("exceptions").getChildren().get(i);
                if(e.getName().equalsIgnoreCase("page") && e.getText().length()>0 ){
                    pags.add(new FilterException(e.getText().trim()));
                }
                else if(e.getName().equalsIgnoreCase("resource")  && e.getText().length()>0 ){
                    pags.add(new FilterException(e.getText().trim().toLowerCase(), TypeException.RESOURCE));
                }
                else if(e.getName().equalsIgnoreCase("directory")  && e.getText().length()>0 ){
                    pags.add(new FilterException(e.getText(), TypeException.DIRECTORY));
                }
            }
        }

        StringBuffer sb=new StringBuffer("[");
        for(int i=0; i<pags.size(); i++){
            if( i>0 ){
                sb.append(", ");
            }
            sb.append(pags.get(i).getType()).append(":").append(pags.get(i).getResource());
        }
        sb.append("]");
        DEBUGER.debug("Excepciones configuradas: "+sb);
            
        return pags;
    }

    /**
     * Obtiene el recurso al que se redirecccionara en caso de vencimiento de la 
     * sesi&oacute;n.
     * @param config Documento XML con la configurac´&oacute;n
     * @return Cadena con el recurso
     */
    public static String getRedirect(Element root){
        String red="login.html";

        //valida si existe la definicion
        if( root!=null && root.getAttribute("login")!=null && !root.getAttributeValue("login").isEmpty() ){
            red= root.getAttributeValue("login");
        }

        DEBUGER.debug("Redicionamiento configurado: "+red);
        return red;
    }

    /**
     * Obtiene el recurso al que se redirecccionara en caso de no contar con los 
     * privilegios suficientes.
     * @param config Documento XML con la configurac´&oacute;n
     * @return Cadena con el recurso
     */
    public static String getHome(Element root){
        String home="index.html";

        //valida si existe la definicion
        if( root!=null && root.getAttribute("home")!=null && !root.getAttributeValue("home").isEmpty() ){
            home= root.getAttributeValue("home");
        }

        DEBUGER.debug("Home configurado: "+home);
        return home;
    }

    /**
     * Obtiene el tiempo de espera en segundos para validar las sesiones activas..
     * @param config Documento XML con la configurac´&oacute;n
     * @return Tiempo de espera
     */
    public static long getPingTime(Document config){
        Element e, raiz = config.getRootElement();
        long t;

        e=raiz.getChild("authentication");
        //si no existen autenticacion termino
        if( e==null || e.getChild("sessionPing")==null || e.getChild("sessionPing").getText().length()==0 ){
            return -1;
        }

        try{
            t=Math.abs(Integer.parseInt(e.getAttributeValue("sessionPing"))*BlockerWay.SECOND_IN_MILLIS);
            if( t<5*BlockerWay.SECOND_IN_MILLIS ){
                t=5*BlockerWay.SECOND_IN_MILLIS;
            }
            
            return t;
        }catch(Exception ex){
            return -1;
        }
    }
        
    /**
     * Obtiene el texto que se manejar&aacute; en caso del vencimiento de la 
     * sesi&oacute;n.
     * @param config Documento XML con la configurac´&oacute;n
     * @return Cadena con el mensaje
     */
    public static String getSessionMessage(Element root){
        String mens="Su sesión ha expirado.";

        //valida si existe la definicion
        if( root!=null && root.getAttribute("sessionMessage")!=null && !root.getAttributeValue("sessionMessage").isEmpty() ){
            mens= root.getAttributeValue("sessionMessage");
        }

        DEBUGER.debug("Mensaje: "+mens);
        return mens;
    }

    /**
     * Obtiene el texto que se manejar&aacute; en caso de no contar con los privilegios 
     * suficientes para acceder a un recurso.
     * @param config Documento XML con la configurac´&oacute;n
     * @return Cadena con el mensaje
     */
    public static String getPermissionMessage(Element root){
        String mens="Sin privilegios suficientes.";

        //valida si existe la definicion
        if( root!=null && root.getAttribute("deniedMessage")!=null && !root.getAttributeValue("deniedMessage").isEmpty() ){
            mens= root.getAttributeValue("deniedMessage");
        }

        DEBUGER.debug("Mensaje 2: "+mens);
        return mens;
    }

    /**
     * Obtiene el Validador de Acceso a Recursos utilizado para el control de acceso.
     * @param config Documento XML con la configurac´&oacute;n
     * @param entorno Objetos del entorno actual
     * @return Validador de Acceso a Recursos
     */
    public static ResourceAccessAllower getValidator(Document config, Map<String,Object> entorno){
        Element e, e1, raiz = config.getRootElement();
        Object obj;
        ResourceAccessAllower res;
        boolean restrictivo=true;
        ArrayList params=new ArrayList();

        //obtengo los datos
        e=raiz.getChild("authentication");
        e1=e.getChild("validation").getChild("validator");

        //recupera la restriccion
        try{
            restrictivo=(new Boolean(e.getChild("validation").getAttributeValue("restrictive"))).booleanValue();
        }
        catch(Exception ex2){}
        params.add(restrictivo);

        obj=ClassGenerator.createInstance(e1, entorno);

        return (obj!=null? (ResourceAccessAllower)obj: null);
    }
    
    /**
     * Genera un evento en el {@link neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay Medio Bitacorizador}
     * configurado en el Control de Acceso.
     * @param ctrl Control de Acceso
     * @param user Usuario que genera el evento
     * @param request Petici´&oacute;n de la p&aacute;gina
     */
    public static void writeEvent(AccessController ctrl, User user, HttpServletRequest request){
        try{
            ctrl.writeEvent(user, EventAudit.RESOURCE_ACCESS, "El usuario '"+user.getUser()+"', accede al recurso: "+request.getServletPath());
        }catch(Exception ex){
            DEBUGER.error("No se logro escribir en bitacora.", ex);
        }
    }

    /**
     * Realiza la validaci&oacute;n del acceso al recurso especificado.
     * @param excs Lista de recursos a omitirse
     * @param redir Recurso al que se debe redirecionar en caso de sesi&oacute;n invalida
     * @param home Recurso al que se debe redirecionar en caso de privilegios insuficientes
     * @param val Validador de Acceso a Recursos
     * @param request Petici´&oacute;n de la p&aacute;gina
     * @param response Respuesta de la  p&aacute;gina
     * @return Resultado de la validaci&oacute;n
     */
    public static DenialType validateResourceAccess(ArrayList<FilterException> excs, String redir, String home, ResourceAccessAllower val, HttpServletRequest request, HttpServletResponse response){
        AccessController ctrl=AccessController.getInstance(); //(AccessController)request.getSession().getAttribute(AccessControllerPublisher.CLAVE_CTRL);
        String sess;

        //valida las paginas de excepcion
        for (FilterException e: excs) {
            if ( e.getType()==TypeException.RESOURCE && request.getServletPath().toLowerCase().endsWith("."+e.getResource()) ) {
                DEBUGER.debug("Omite el recurso por ser una excepcion (Recurso).");
                
                return DenialType.ALLOWED;
            }
            else if ( e.getType()==TypeException.DIRECTORY && request.getServletPath().startsWith("/"+e.getResource()) ) {
                DEBUGER.debug("Omite la pagina por ser una excepcion (Directorio).");
                
                return DenialType.ALLOWED;
            }
            else if ( e.getType()==TypeException.PAGE && request.getServletPath().equals("/"+e.getResource()) ) {
                DEBUGER.debug("Omite la pagina por ser una excepcion (Pagina).");
                
                return DenialType.ALLOWED;
            }
        }

        //recupero al usuario
        User u = (User)request.getSession().getAttribute(AccessControllerPublisher.USER_KEY);
        DEBUGER.debug("Recurso a validar: "+request.getServletPath());        

        //valida la pagina de redirecionamiento
        if( request.getServletPath().equals("/"+redir) ){
            //valida si tiene sesion activa
            if( u!=null ){
                DEBUGER.debug("Esta activa la sesión.");
                try{
                    ctrl.updateActivity(u);
                    BlockerSessionListener.createSessionCookie(request, response, u);
                }catch(Exception ex){
                    DEBUGER.error("No se logro actualizar la actividad del usuario.", ex);
                }
                writeEvent(ctrl, u, request);
                
                return DenialType.LOGIN_ALLOWED;
            }
            
            //valido si existe la sesion activa aun
            sess=BlockerSessionListener.getSessionFromCookie(request);
            if( sess!=null ){
                DEBUGER.debug("Existe sesion: "+sess);
                u=ctrl.getConnectedUserBySession(sess);
                if( u!=null ){
                    DEBUGER.debug("El usuario aun esta conectado: "+u);
                    try{
                        ctrl.updateActivity(u);
                        BlockerSessionListener.createSessionCookie(request, response, u);
                    }catch(Exception ex){
                        DEBUGER.error("No se logro actualizar la actividad del usuario.", ex);
                    }
                    writeEvent(ctrl, u, request);

                    return DenialType.LOGIN_ALLOWED;
                }
            }
          
            DEBUGER.debug("Omite la pagina por ser el redirecionamiento.");
            return DenialType.ALLOWED;
        }

        //valida la pagina de home
        if( request.getServletPath().equals("/"+home) ){
            if( u!=null ){
                DEBUGER.debug("Omite la pagina por ser el home.");
                writeEvent(ctrl, u, request);
                
                return DenialType.ALLOWED;
            }
            else{
                return DenialType.NOT_CONNECTED;
            }
        }

        //valida la existencia del usuario conectado
        if( u!=null ){
            DEBUGER.debug("El usuario esta conectado");
            try{
                ctrl.updateActivity(u);
                BlockerSessionListener.createSessionCookie(request, response, u);
            }catch(Exception ex){
                DEBUGER.error("No se logro actualizar la actividad del usuario.", ex);
            }
            AccessEstatus valido=val.validateAccess(u, request.getServletPath());
            DEBUGER.debug("Valida los permisos del recurso: "+valido);

            if( valido==AccessEstatus.NOT_FOUND && val.isRestrictive()){
                DEBUGER.debug("No se puede acceder al recurso, por estar en modo restrictivo.");
                return DenialType.RESTRICTIVE;
            }
            else if( valido==AccessEstatus.DENIED ){
                DEBUGER.debug("No tiene permisos para acceder al recurso.");
                return DenialType.NOT_ALLOWED;
            }
            else{
                writeEvent(ctrl, u, request);
                
                return DenialType.ALLOWED;
            }
        }

        //guardo la peticion para posteriormente redirecionar, cualdo se haya validad el usuario
        SessionListener.saveLastRequest(request);

        //retorno el erro de no conectado
        return DenialType.NOT_CONNECTED;
    }
}
