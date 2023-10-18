package neoatlantis.accesscontroller.web.listeners;

import java.util.Date;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import neoatlantis.accesscontroller.AccessController;
import neoatlantis.accesscontroller.objects.User;
import neoatlantis.applications.web.listeners.ApplicationListener;
import neoatlantis.applications.web.objects.ApplicationSession;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class BlockerSessionListener  implements HttpSessionListener{
    private static final Logger DEBUGER = Logger.getLogger(BlockerSessionListener.class);

    public static final String HTTP_SESSION_COOKIE="na.utils.aut.cookie.sesion";

    
    // Metodos publicos---------------------------------------------------------

    @Override
    public void sessionCreated(HttpSessionEvent hse) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent hse) {
        User user=(User)hse.getSession().getAttribute(AccessControllerPublisher.USER_KEY);
        AccessController access=(AccessController)hse.getSession().getServletContext().getAttribute(AccessControllerPublisher.ACCESS_CTRL_KEY);

        if( user!=null ){
            try{
                access.updateStatisticsLifeConnection( ((new Date()).getTime()-user.getSession().getCreated().getTime())/1000/60 );
                access.ends(user);
            }
            catch(Exception ex){
                DEBUGER.error("Error al finalizar al usuario '"+user+"'.", ex);
            }
        }
    }
    

    
    // Metodos publicos estaticos-----------------------------------------------

    /**
     * Destruye el contecto y sesi&oacute;n del usuario que se encuentra conectado.
     * @param req Petici´&oacute;n de la p&aacute;gina
     * @param res  Respuesta de la  p&aacute;gina
     */
    public static void endSession(HttpServletRequest req, HttpServletResponse res){
        if( req==null || res==null ){
            return;
        }

        User u=(User)req.getSession().getAttribute(AccessControllerPublisher.USER_KEY);
        AccessController ctrl=AccessController.getInstance(); 
        
        //remuevo cookie
        if( req.getCookies()!=null ){
            for(Cookie c: req.getCookies()){
                if( u!=null && c.getName().equals(HTTP_SESSION_COOKIE) ){
                    c.setMaxAge(0);
                    res.addCookie(c);
                    DEBUGER.debug("Termina la cookie de la sesion del usuario: "+u.getUser());
                    break;
                }
            }
        }
        
        req.getSession().removeAttribute(AccessControllerPublisher.USER_KEY);
        req.getSession().invalidate();
        if( u!=null ){
            try{
                ctrl.ends(u);
            }catch(Exception ex){
                DEBUGER.fatal("No se logro terminar la conexión", ex);
            }
        }
        
        DEBUGER.debug("Termina la conexion del usuario: "+(u==null? u: u.getUser()));
    }

    /**
     * Destruye el contecto y sesi&oacute;n del usuario que se encuentra conectado.
     * @param ses Sesi&oacute;n web activa
     */
    public static void endSession(HttpSession ses){
        if( ses==null ){
            return;
        }

        try{
            ses.removeAttribute(AccessControllerPublisher.USER_KEY);
            ses.invalidate();
        }
        catch(Exception ex){}
    }

    /**
     * Redireciona la petici&oacute;n ejecutada hacia alg&uacute; otro recurso.
     * @param request Petici´&oacute;n de la p&aacute;gina
     * @param response  Respuesta de la  p&aacute;gina
     * @param pagina Recurso al que se desea redirecionar
     * @param mensaje Mensaje que se deba agragar a la lista de mensajes a desplegar 
     * en el nuevo recurso
     */
    public static void redirect(ServletRequest request, ServletResponse response, String pagina, String mensaje){
        RequestDispatcher dispatcher;

        if( mensaje!=null ){
            request.setAttribute(AccessControllerPublisher.MESSAGE_ERROR_KEY, mensaje);
        }

        try{
            DEBUGER.debug("OK: "+response.isCommitted()+", "+mensaje);
//            response.reset();
            dispatcher = request.getRequestDispatcher("/"+pagina);
            dispatcher.forward(request, response);
        }
        catch(Exception ex){
            DEBUGER.error("Error al redirecionar a '"+pagina+"'", ex);
        }
    }

    /**
     * Genera y carga el contexto de la sesi&oacute;n para un usuario conectado.
     * @param req Petici´&oacute;n de la p&aacute;gina
     * @param res Respuesta de la  p&aacute;gina
     * @param u Usuario del que se desea generar su sesi&oacute;n
     */
    public static void initSession(HttpServletRequest req, HttpServletResponse res, User u){
        boolean existe=false;
        
        if( req==null || res==null ){
            return;
        }

        req.getSession().setAttribute(AccessControllerPublisher.USER_KEY, u);
        
        //revisa si existe sesion cargada por el api de NaApp
        List<ApplicationSession> sesiones=(List<ApplicationSession>)req.getSession().getServletContext().getAttribute(ApplicationListener.SESSIONS_KEY);
        
        DEBUGER.debug("Existen "+(sesiones!=null? sesiones.size(): -1)+" sesiones activas.");
        for(int i=0; sesiones!=null&&i<sesiones.size(); i++){
            if( sesiones.get(i).getHttpSession().getId().equals(req.getSession().getId()) ){
                DEBUGER.debug("Vincula sesion con usuario: "+sesiones.get(i).getId());
                u.newSession(sesiones.get(i));
                existe=true;
                break;
            }
        }

        if( !existe ){
            u.newSession(new ApplicationSession(req.getSession(), req.getRemoteAddr()));
        }
        
        createSessionCookie(req, res, u);

        DEBUGER.debug("Inicia la conexion del usuario: "+u.getUser());
    }

    
    /**
     * Genera la cokoie que almacena la sesi&oacute;n del usuario conectado.
     * @param req Petici´&oacute;n de la p&aacute;gina
     * @param res Respuesta de la  p&aacute;gina
     * @param u Usuario del que se desea generar su cookie
     */
    public static void createSessionCookie(HttpServletRequest req, HttpServletResponse res, User u){
        //genero cookie de conexion
        DEBUGER.debug("Genero la cookie de la sesion del  usuario: "+u.getUser());
        Cookie cook=new Cookie(HTTP_SESSION_COOKIE, u.getSession().getId());
        cook.setPath("/");
        cook.setMaxAge(u.getSession().getHttpSession().getMaxInactiveInterval());        
        cook.setSecure(true);
        res.addCookie(cook);        
    }

    public static String getSessionFromCookie(HttpServletRequest req){
        DEBUGER.debug("Verifico si existe una cookie de sesion del usuario");
        if( req.getCookies()!=null ){
            for(Cookie c: req.getCookies()){                
                DEBUGER.debug("["+c.getName()+"]: "+c.getValue());
                if( c.getName().equals(HTTP_SESSION_COOKIE) ){
                    return c.getValue();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Revisa si existe sesiones de usuario que ya no estan activas y las elimina.
     * @param request  Resquest de la peticion web
     */
    public static void endInactiveSessions(HttpServletRequest request, long tiempoPing){
        AccessController ctrl=AccessController.getInstance();
        
        if( ctrl!=null ){
            //valida termino por tiempo de vida
            try{
                for(User uTmp: ctrl.validateInactiveSessions()){
                    ctrl.ends(uTmp);
                    endSession(uTmp.getSession().getHttpSession());
                    DEBUGER.debug("Destruyo la sesion para "+uTmp.getUser()+" por tiempo de vida");
                }
            }
            catch(Exception ex){
                DEBUGER.error("No se logro recuperar las sesiones inactivas", ex);
            }

            //valida termino por timeout en ping
            if( tiempoPing>-1 && ctrl!=null ){
                try{
                     for(User uTmp: ctrl.getConnections()){
                        DEBUGER.debug("Valida sesion para "+uTmp.getUser()+" : "+uTmp.getActivityDate());
                        
                        if( uTmp.getActivityDate()!=null && (new Date()).getTime()>uTmp.getActivityDate().getTime()+tiempoPing ){
                            ctrl.ends(uTmp);
                            endSession(uTmp.getSession().getHttpSession());
                            DEBUGER.debug("Destruyo la sesion para "+uTmp.getUser()+" por ping excedido");
                        }
                    }
                }catch(Exception ex){
                    DEBUGER.error("No se logro recuperar las sesiones activas.", ex);
                }
            }
        }
    }
}
