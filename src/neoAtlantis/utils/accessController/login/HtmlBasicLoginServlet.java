package neoAtlantis.utils.accessController.login;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neoAtlantis.utils.accessController.AccessController;
import neoAtlantis.utils.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utils.accessController.login.interfaces.AuthenticationLogin;
import neoAtlantis.utils.accessController.objects.EnvironmentType;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.accessController.web.listeners.AccessControllerPublisher;
import neoAtlantis.utils.accessController.web.listeners.BlockerSessionListener;
import neoAtlantis.utils.apps.web.listeners.SessionListener;
import neoAtlantis.utils.configurations.ConfigurationUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class HtmlBasicLoginServlet extends HttpServlet implements AuthenticationLogin {
    private static final Logger DEBUGER = Logger.getLogger(HtmlBasicLoginServlet.class);
    
    public static final String USER_COOKIE="na.utils.aut.cookie.username";
    public static final String ANOTHER_USER_PARAM="na_another_user";

    private static boolean configurado=false;
    
    protected static Properties config;
    protected static Map<String, String> mens;
    
    
    
    
    
    // Contructores ------------------------------------------------------------

    public HtmlBasicLoginServlet(String login, String home, String temporal){
        this(login, home, temporal, null);
    }

    public HtmlBasicLoginServlet(String login, String home, String temporal, String message){
        Properties p=new Properties();
        p.setProperty("na.login.login", login);
        p.setProperty("na.login.home", home);
        p.setProperty("na.login.temporal", temporal);
        p.setProperty("na.login.messages.exit", message);
                
        createEnvironment(p);
    }

    public HtmlBasicLoginServlet(Properties config){
        createEnvironment(config);
    }

    
    
    // Metodos publicos---------------------------------------------------------
    
    /**
     * M&eacuet;todo que inicializa al servlet, dentro del cual se realiz&aacute; 
     * la configuraci&oacute;n del servlet con base a lo que haya cargado el 
     * {@link neoAtlantis.utilidades.accessController.utils.AccessControllerPublisher Configurador del Control de Acceso}
     */
    @Override
    public void init(){
        String homeWeb;
        String homeWebInf;
        String homeClass;

        if(!configurado){
            //configuro los home
            homeWeb=this.getServletContext().getRealPath("/").replace('\\', '/')+"/";
            homeWebInf=homeWeb+"WEB-INF/";
            homeClass=homeWebInf+"classes/";
            HashMap<String, Object> com=new HashMap<String, Object>();
            com.put("homeWeb", homeWeb);
            com.put("homeWebInf", homeWebInf);
            com.put("homeClass", homeClass);

            String home=homeWebInf+"config/configAccess.xml";

            //reviso si existe una configuracion personalizada
            if( this.getServletContext().getInitParameter("configAccessNA")!=null && this.getServletContext().getInitParameter("configAccessNA").length()>2 ){
                home=ConfigurationUtils.parseWindcars(this.getServletContext().getInitParameter("configAccessNA"), com);
            }

            DEBUGER.info("Carga configuración de :"+home);
            //validamos la existencia del archivo de configuracion
            File fTmp=new File(home);
            if( fTmp.exists() ){
                try{
                    //parseo el archivo XML
                    Document doc=(new SAXBuilder(false)).build(home);
                    createEnvironment(this.parseXml(doc.detachRootElement()));
                }
                catch(Exception ex){
                    DEBUGER.error("Error al cargar el archivo de configuración de access '"+home+"'.", ex);
                }
            }
            else{
                throw new RuntimeException("No se ha definido la configuración para el Control de Acceso.");
            }
        }

        //valida la existencia de los mensajes
        if( this.getServletContext().getAttribute(AccessControllerPublisher.MESSAGES_KEY)!=null ){
            mens=(Map<String, String>)this.getServletContext().getAttribute(AccessControllerPublisher.MESSAGES_KEY);
        }
        if( mens==null ){
            throw new RuntimeException("No se logro acceder a los mensajes ['"+AccessControllerPublisher.MESSAGES_KEY+"'] en memoria.");
        }
        else{
            //reviso si existe mensaje de salida
            if( config.getProperty("na.login.messages.exit")!=null && !config.getProperty("na.login.messages.exit").isEmpty() ){
                mens.put("exit", config.getProperty("na.login.messages.exit"));
            }
            else{
                mens.put("exit", "Sesión finalizada.");
            }
            
        }

        DEBUGER.debug("Mensaajes cargados: "+config);
    }
    
    @Override
    public User tryAccess(Map<String, Object> datos) {
        User u=null;
        //realizo la autenticación
        DEBUGER.debug("Inicia la validación del usuario");
        HttpServletRequest request=((HttpServletRequest)datos.get(REQUEST_WEB_KEY));
        HttpServletResponse response=((HttpServletResponse)datos.get(RESPONSE_WEB_KEY));

        try{
            AccessController access=AccessController.getInstance();
            u=access.authenticatePerson(request.getRemoteAddr(), request.getRemoteHost(), EnvironmentType.WEB, datos);
            DEBUGER.debug("Se realiza la autenticación con resultado: "+u.getState());

            switch( u.getState() ){
                case BLOCKED: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("blocked"));
                    break;
                }
                case LAPSED: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("expires"));
                    break;
                }
                case DENIED: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("denied"));
                    break;
                }
                case ALTER_DENIED: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("deniedOwner"));
                    break;
                }
                case IN_USE: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("connected"));
                    break;
                }
                case CODE_ERROR: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("captcha"));
                    break;
                }
                case OUTTIME: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("outTime"));
                    break;
                }
                case INACTIVE: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("inactive"));
                    break;
                }
                case EXCEED_LIMIT: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("exceeded"));
                    break;
                }
                case NOT_FOUND: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("notFound"));
                    break;
                }
                case NOT_USER: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("notUser"));
                    break;
                }
                case NOT_PROVISIONED: {
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("notProvisioned"));
                    break;
                }
                case VALIDATE: {
                    BlockerSessionListener.initSession(request, response, u);
                    this.loadDataPostvalidation(u, datos);
                    createUserCookie(u, response);
                    response.sendRedirect(request.getServletContext().getContextPath()+"/"+config.getProperty("na.login.home"));
                    break;
                }
                case TEMPORAL_VALIDATE: {
                    request.setAttribute(AccessControllerPublisher.USER_KEY, u);
                    createUserCookie(u, response);
                    BlockerSessionListener.redirect(request, response, config.getProperty("na.login.temporal"), mens.get("temporal"));
                    break;
                }
            }
        }
        catch(Exception ex){
            DEBUGER.error("No se logro realizar la autenticación", ex);
            BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("error"));
        }
        
        return u;
    }

    @Override
    public void loadDataPostvalidation(User user, Map<String,Object> data){
    }
    
    /**
     * M&eacuet;todo que se ejecuta cuando se invoca al servlet de la forma GET.
     * Mediante esta forma se pueden realizar 3 operaciones, para lo cual se definen
     * los siguientes parametros:<br>
     * <b>scriptSession: </b>Si se incluye este parametro dentro de la petici&oacute;n 
     * se obtentra el script de mantenimiento de sesi&oacute;n mediante ping.<br>
     * <b>sessionId:</b>Si se incluye este parametro en la petici&oacute;n se actualizar&aacute;
     * el tiempo de actividad de la sesi&oacute;n del usuario conectado. Esta funcionalidad 
         * es utilizada couando se implementa el m&eacuet;todo de  mantenimiento de 
         * sesi&oacute;n mediante ping.
     * <b>Sin parametros:</b> Finaliza la sesi&oacute;n y conexi&oacute;n del usuario
     * conectado.
     * @param request Petici´&oacute;n de la p&aacute;gina
     * @param response  Respuesta de la  p&aacute;gina
     * @throws IOException 
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //valido se se debe remover la cookie de usuario
        if( request.getParameter(ANOTHER_USER_PARAM)!=null ){
            removeUserCookie(request, response);
            response.sendRedirect(request.getServletContext().getContextPath()+"/"+config.getProperty("na.login.login"));
        }        
        //valido si no es una peticion de autenticacion
        else if( request.getParameter(AuthenticationWay.LOGIN_PARAM)==null ){
            //termina session
            BlockerSessionListener.endSession(request, response);
            SessionListener.clearLastRequest(request);
            BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("exit"));
        }
        else{
            BlockerSessionListener.redirect(request, response, config.getProperty("na.login.login"), mens.get("methodGet"));
        }
    }    

    /**
     * M&eacuet;todo que se ejecuta cuando se invoca al servlet de la forma POST.
     * Mediante esta forma se puede autenticar un usuario.
     * @param request  Petici´&oacute;n de la p&aacute;gina
     * @param response  Respuesta de la  p&aacute;gina
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response){
        Enumeration pe=request.getParameterNames();
        String par;
        boolean autenticar=false;
        HashMap<String, Object> datos=new HashMap<String, Object>();

        //recupero los parametros enviados
        while(pe.hasMoreElements()){
            par=(String)pe.nextElement();

            if( par.equals(AuthenticationWay.LOGIN_PARAM) ){
                autenticar=true;
            }
            else{
                datos.put(par, request.getParameter(par).replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
            }
        }
        
        if( autenticar ){
            datos.put(REQUEST_WEB_KEY, request);
            datos.put(RESPONSE_WEB_KEY, response);
            this.tryAccess(datos);
        }        
        else{
            //cierro la sesion
            BlockerSessionListener.endSession(request, response);
            try{
                response.sendRedirect(config.getProperty("na.login.login"));
            }catch(Exception ex){
                DEBUGER.error("No puedo redirecionar a login", ex);
            }
        }
    }    
    
    
    
    

    // Metodos estaticos---------------------------------------------------------
    
    /**
     * Metodo que genera la cookie con el username del usuario que se autentico
     * @param usuario Usuario autenticado
     * @param res Repuesta web
     */
    public static void createUserCookie(User usuario, HttpServletResponse res){
        DEBUGER.debug("Genero la cookie del  usuario: "+usuario);
        Cookie cook=new Cookie(USER_COOKIE, usuario.getUser());
        cook.setPath("/");
        cook.setMaxAge(60*60*24*365);        
        res.addCookie(cook);                
    }
    
    /**
     * Metodo que elimina la cookie con el username del usuario que se autentico
     * @param req Peticion web
     * @param res Respuesta web
     */
    public static void removeUserCookie(HttpServletRequest req, HttpServletResponse res){
        DEBUGER.debug("Remuevo la cookie del  usuario");
        Cookie cook=new Cookie(USER_COOKIE, null);
        cook.setPath("/");
        cook.setMaxAge(0);        
        res.addCookie(cook);   
        DEBUGER.debug("Cookie del usuario removida");
    }
    
    /**
     * Metodo que recuperael username del usuario que se autentico a partir de la cookie
     * @param req Peticion web
     * @return Username del usuario
     */
    public static String getUserFromCookie(HttpServletRequest req){
        DEBUGER.debug("Intento recuperar el usuario a partir de su cookie");
        if( req.getCookies()!=null ){
            for(Cookie c: req.getCookies()){
                if( c.getName().equals(USER_COOKIE) ){
                    DEBUGER.debug("Cookie de usuario encontrada, retorno el username");
                    return c.getValue();
                }
            }
        }
        
        return null;
    }
    
    
    
    

    // Metodos privados---------------------------------------------------------
    
    private Properties parseXml(Element root){
        Properties p=new Properties();
        Element e, e2;

        e=root.getChild("authentication");
        if(e==null ){
            DEBUGER.debug("No se definio adecuadamenete la opcion de autenticación.");
            throw new RuntimeException("No se logro definir las opciones de autenticación.");
        }

        //defino el input
        if( e.getAttributeValue("login")!=null && e.getAttributeValue("login").length()>0 ){
            p.setProperty("na.login.login", e.getAttributeValue("login"));
        }
        else{
            p.setProperty("na.login.login", "login.html");
        }

        //defino el home
        e2=e.getChild("home");
        if( e2!=null && e2.getText().length()>0 ){
            p.setProperty("na.login.home", e2.getText());
        }
        else{
            p.setProperty("na.login.home", "index.html");
        }

        //defino el temporal
        e2=e.getChild("temp");
        if( e2!=null && e2.getText().length()>0 ){
            p.setProperty("na.login.temporal", e2.getText());
        }
        else{
            p.setProperty("na.login.temporal", "password.html");
        }

        e2=e.getChild("exit");
        if( e2!=null && e2.getText().length()>0 ){
            p.setProperty("na.login.messages.exit", e2.getText());
        }
        
        return p;
    }
    
    /**
     * Prepara el entorno para su uso
     * @param root Nodo con la configuración
     */
    private void createEnvironment(Properties con){
        //si no se ha configurado realizo la misma
        if(!configurado){
            //verifico que se tenga la adecuada configuración
            if( con==null 
                    || con.getProperty("na.login.login")==null || con.getProperty("na.login.login").isEmpty()
                    || con.getProperty("na.login.home")==null || con.getProperty("na.login.home").isEmpty()
                    || con.getProperty("na.login.temporal")==null || con.getProperty("na.login.temporal").isEmpty()){
                throw new RuntimeException("No proporciono adecuadamente la configuración para el login.");
            }
            else{
                config=con;
            }

            DEBUGER.debug("Configuración cargada: "+config);            
            DEBUGER.debug("Login de autenticación configurado.");
            configurado=true;
        }        
    }
}
