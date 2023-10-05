package neoAtlantis.utilidades.accessController.utils;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neoAtlantis.utilidades.accessController.AccessController;
import neoAtlantis.utilidades.accessController.audit.interfaces.EventAudit;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.resourcesFilter.*;
import neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower;
import neoAtlantis.utilidades.configFiles.ClassGenerator;
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
    static final Logger logger = Logger.getLogger(ValidatorConfigurator.class);

    /**
     * Obtiene las recursos a omitirse de validaci&oacute;n.
     * @param config Documento XML con la configurac´&oacute;n
     * @return Lista de recursos a omitirse
     */
    public static ArrayList<FilterException> obtieneExcepciones(Document config){
        Element e, raiz = config.getRootElement();
        ArrayList<FilterException> pags=new ArrayList<FilterException>();
        List<Element> lTmp;

        e=raiz.getChild("authentication");
        //si no existe autenticacion termino
        if( e==null || e.getChild("validation")==null || e.getChild("validation").getChild("exceptions")==null ){
            return new ArrayList();
        }

        //recupero las paginas
        lTmp=e.getChild("validation").getChild("exceptions").getChildren();
        for(Element eTmp: lTmp){
            if(eTmp.getName().equalsIgnoreCase("page") && eTmp.getText().length()>0 ){
                pags.add(new FilterException(eTmp.getText().trim()));
            }
            else if(eTmp.getName().equalsIgnoreCase("resource")  && eTmp.getText().length()>0 ){
                pags.add(new FilterException(eTmp.getText().trim().toLowerCase(), TypeException.RESOURCE));
            }
            else if(eTmp.getName().equalsIgnoreCase("directory")  && eTmp.getText().length()>0 ){
                pags.add(new FilterException(eTmp.getText(), TypeException.DIRECTORY));
            }
        }

        return pags;
    }

    /**
     * Obtiene el recurso al que se redirecccionara en caso de vencimiento de la 
     * sesi&oacute;n.
     * @param config Documento XML con la configurac´&oacute;n
     * @return Cadena con el recurso
     */
    public static String obtieneRedirecionamiento(Document config){
        Element e, raiz = config.getRootElement();

        e=raiz.getChild("authentication");
        //si no existen autenticacion termino
        if( e==null || e.getAttribute("login")==null || e.getAttributeValue("login").length()==0 ){
            return "login.html";
        }

        return e.getAttributeValue("login");
    }

    /**
     * Obtiene el recurso al que se redirecccionara en caso de no contar con los 
     * privilegios suficientes.
     * @param config Documento XML con la configurac´&oacute;n
     * @return Cadena con el recurso
     */
    public static String obtieneHome(Document config){
        Element e, raiz = config.getRootElement();

        e=raiz.getChild("authentication");
        //si no existen autenticacion termino
        if( e==null || e.getChild("home")==null || e.getChild("home").getText().length()==0 ){
            return "index.html";
        }

        return e.getChild("home").getText();
    }

    /**
     * Obtiene el tiempo de espera en segundos para validar las sesiones activas..
     * @param config Documento XML con la configurac´&oacute;n
     * @return Tiempo de espera
     */
    public static long obtieneTiempoPing(Document config){
        Element e, raiz = config.getRootElement();
        long t;

        e=raiz.getChild("authentication");
        //si no existen autenticacion termino
        if( e==null || e.getChild("sessionPing")==null || e.getChild("sessionPing").getText().length()==0 ){
            return -1;
        }

        try{
            t=Math.abs(Integer.parseInt(e.getAttributeValue("sessionPing"))*BlockerWay.SEGUNDO_EN_MILIS);
            if( t<5*BlockerWay.SEGUNDO_EN_MILIS ){
                t=5*BlockerWay.SEGUNDO_EN_MILIS;
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
    public static String obtieneMensajeSession(Document config){
        Element e, raiz = config.getRootElement();

        e=raiz.getChild("messages");
        //si no existe mensajes termino
        if( e==null || e.getChild("session")==null ||  e.getChild("session").getText().length()==0 ){
            return "Su sesión ha expirado.";
        }
        

        return e.getChild("session").getText();
    }

    /**
     * Obtiene el texto que se manejar&aacute; en caso de no contar con los privilegios 
     * suficientes para acceder a un recurso.
     * @param config Documento XML con la configurac´&oacute;n
     * @return Cadena con el mensaje
     */
    public static String obtieneMensajePermiso(Document config){
        Element e, raiz = config.getRootElement();

        e=raiz.getChild("messages");
        //si no existe mensajes termino
        if( e==null || e.getChild("permission")==null ||  e.getChild("permission").getText().length()==0 ){
            return "Sin privilegios suficientes.";
        }


        return e.getChild("permission").getText();
    }

    /**
     * Obtiene el Validador de Acceso a Recursos utilizado para el control de acceso.
     * @param config Documento XML con la configurac´&oacute;n
     * @param entorno Objetos del entorno actual
     * @return Validador de Acceso a Recursos
     */
    public static ResourceAccessAllower obtieneValidador(Document config, Map<String,Object> entorno){
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

        obj=ClassGenerator.generaInstancia(e1, params, ClassGenerator.generaComodinesHomeWeb((String)entorno.get("homeWeb")), entorno);

        if( obj==null ){
            res=new NullResourceAccess();
        }
        else{
            res=(ResourceAccessAllower)obj;
        }

        return res;
    }
    
    /**
     * Genera un evento en el {@link neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay Medio Bitacorizador}
     * configurado en el Control de Acceso.
     * @param ctrl Control de Acceso
     * @param user Usuario que genera el evento
     * @param request Petici´&oacute;n de la p&aacute;gina
     */
    public static void escribeBitacora(AccessController ctrl, User user, HttpServletRequest request){
        try{
            ctrl.escribeEvento(user, EventAudit.ACCESO, "El usuario '"+user.getUser()+"', accede al recurso: "+request.getServletPath());
        }catch(Exception ex){
            logger.error("No se logro escribir en bitacora.", ex);
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
    public static DenialType validaAccesoRecuros(ArrayList<FilterException> excs, String redir, String home, ResourceAccessAllower val, HttpServletRequest request, HttpServletResponse response){
        AccessController ctrl=AccessController.getInstance(); //(AccessController)request.getSession().getAttribute(AccessControllerPublisher.CLAVE_CTRL);

        //recupero al usuario
        User u = (User)request.getSession().getAttribute(AccessControllerPublisher.CLAVE_USER);
        logger.debug("Recurso a validar: "+request.getServletPath());        

        //valida la pagina de redirecionamiento
        if( request.getServletPath().equals("/"+redir) ){
            //valida si tiene sesion activa
            if( u!=null ){
                logger.debug("Esta activa la sesión.");
                try{
                    ctrl.actualizaConexionUsuario(u);
                    SimpleAuthenticationServlet.generaCookieSesion(request, response, u);
                }catch(Exception ex){
                    logger.error("No se logro actualizar la actividad del usuario.", ex);
                }
                escribeBitacora(ctrl, u, request);
                
                return DenialType.PERMITIDO_LOGIN;
            }
            
            logger.debug("Omite la pagina por ser el redirecionamiento.");
            return DenialType.PERMITIDO;
        }

        //valida la pagina de home
        if( request.getServletPath().equals("/"+home) ){
            if( u!=null ){
                logger.debug("Omite la pagina por ser el home.");
                escribeBitacora(ctrl, u, request);
                
                return DenialType.PERMITIDO;
            }
            else{
                return DenialType.NO_CONECTADO;
            }
        }

        //valida las paginas de excepcion
        for (FilterException e: excs) {
            if ( e.getTipo()==TypeException.RESOURCE && request.getServletPath().toLowerCase().endsWith("."+e.getRecurso()) ) {
                logger.debug("Omite el recurso por ser una excepcion (Recurso).");
                
                return DenialType.PERMITIDO;
            }
            else if ( e.getTipo()==TypeException.DIRECTORY && request.getServletPath().startsWith("/"+e.getRecurso()) ) {
                logger.debug("Omite la pagina por ser una excepcion (Directorio).");
                
                return DenialType.PERMITIDO;
            }
            else if ( e.getTipo()==TypeException.PAGE && request.getServletPath().equals("/"+e.getRecurso()) ) {
                logger.debug("Omite la pagina por ser una excepcion (Pagina).");
                
                return DenialType.PERMITIDO;
            }
        }

        //valida la existencia del usuario conectado
        if( u!=null ){
            logger.debug("El usuario esta conectado");
            try{
                ctrl.actualizaConexionUsuario(u);
                SimpleAuthenticationServlet.generaCookieSesion(request, response, u);
            }catch(Exception ex){
                logger.error("No se logro actualizar la actividad del usuario.", ex);
            }
            AccessEstatus valido=val.validaAcceso(u, request.getServletPath());
            logger.debug("Valida los permisos del recurso: "+valido);

            if( valido==AccessEstatus.NO_ENCONTRADO && val.isRestrictivo() ){
                logger.debug("No se puede acceder al recurso, por estar en modo restrictivo.");
                return DenialType.RESTRICTIVO;
            }
            else if( valido==AccessEstatus.DENEGADO ){
                logger.debug("No tiene permisos para acceder al recurso.");
                return DenialType.NO_PERMITIDO;
            }
            else{
                escribeBitacora(ctrl, u, request);
                
                return DenialType.PERMITIDO;
            }
        }

        return DenialType.NO_CONECTADO;
    }
}
