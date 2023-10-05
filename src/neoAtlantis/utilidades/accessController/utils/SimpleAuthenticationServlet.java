package neoAtlantis.utilidades.accessController.utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import neoAtlantis.utilidades.accessController.AccessController;
import neoAtlantis.utilidades.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utilidades.accessController.authentication.interfaces.ValidationResult;
import neoAtlantis.utilidades.accessController.objects.EnvironmentType;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.apps.escuchadores.AppListener;
import neoAtlantis.utilidades.apps.objects.SesionApp;
import neoAtlantis.utilidades.configFiles.ClassGenerator;
import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * Servlet que genera un punto de autenticaci&oacute; para un aplicativo web. Realiza
 * el proceso de acuerdo a lo configurado en el {@link neoAtlantis.utilidades.accessController.AccessController Control de Autenticaci&oacute;n}
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class SimpleAuthenticationServlet extends HttpServlet {
    static final Logger logger = Logger.getLogger(SimpleAuthenticationServlet.class);
    
    public static final String HTTP_SESSION="httpSession";
    public static final String HTTP_SO="httpSo";
    public static final String HTTP_NAVEGADOR="httpBrowser";
    public static final String HTTP_COOKIE_SESION="na.utils.aut.cookie.sesion";
    public static final String HTTP_PATH_COOKIES="na.utils.aut.cookies";

    private AccessController access;
    private String homeWeb;
    private String homeWebInf;
    private String homeClass;

    /**
     * P&aacute;gina en donde solicita las credenciales.
     */
    protected String login;
    /**
     * P&aacute;gina inicial que se utiliza despues de la autenticaci&oacute;n. 
     */
    protected String home;
    /**
     * P&aacute;gina que se despliega en caso de tener que modificarse la contrase&ntilde;a.
     */
    protected String temp;
    /**
     * Lista de mensajes que puede manda el proceso de autenticaci&oacute;n.
     */
    protected Map<String, String> mens;

    /**
     * M&eacuet;todo que inicializa al servlet, dentro del cual se realiz&aacute; 
     * la configuraci&oacute;n del servlet con base a lo que haya cargado el 
     * {@link neoAtlantis.utilidades.accessController.utils.AccessControllerPublisher Configurador del Control de Acceso}
     */
    @Override
    public void init(){
        //configuro los home
        this.homeWeb=this.getServletContext().getRealPath("/").replace('\\', '/')+"/";
        this.homeWebInf=homeWeb+"WEB-INF/";
        this.homeClass=homeWebInf+"classes/";
        Properties com=new Properties();
        com.setProperty("homeWeb", homeWeb);
        com.setProperty("homeWebInf", homeWebInf);
        com.setProperty("homeClass", homeClass);

        String home=this.homeWebInf+"config/configAccess.xml";

        if( this.getServletContext().getAttribute(AccessControllerPublisher.CLAVE_MENSAJE)!=null ){
            this.mens=(Map<String, String>)this.getServletContext().getAttribute(AccessControllerPublisher.CLAVE_MENSAJE);
        }

        //reviso si existe una configuracion personalizada
        if( this.getServletContext().getInitParameter("configAccessNA")!=null && this.getServletContext().getInitParameter("configAccessNA").length()>2 ){
            home=ClassGenerator.parseaComodinesConfig(this.getServletContext().getInitParameter("configAccessNA"), com);
        }

        //valida la existencia de los mensajes
        if( this.mens==null ){
            throw new RuntimeException("No se logro acceder a los mensajes ['"+AccessControllerPublisher.CLAVE_MENSAJE+"'] en memoria.");
        }
        
        logger.info("Configuracion cargada de :"+home);
        //validamos la existencia del archivo de configuracion
        File fTmp=new File(home);
        if( fTmp.exists() ){
            try{
                //parseo el archivo XML
                Document doc=(new SAXBuilder(false)).build(home);
                cargaEntorno(doc);
            }
            catch(Exception ex){
                logger.error("Error al cargar el archivo de configuración de access '"+home+"'.", ex);
            }
        }
        else{
            throw new RuntimeException("No se ha definido la configuración para el Control de Acceso.");
        }

        logger.debug("Servlet de autenticación configurado.");
        logger.debug("Home: "+this.home);
        logger.debug("Login: "+this.login);
        logger.debug("Temporal: "+this.temp);
        logger.debug("Mensajes: "+this.mens);
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
        if( request.getParameter("scriptSession")!=null ){
            StringBuilder sb=new StringBuilder("");
            sb.append("function ajaxObject(){\n")
                .append("\tvar xmlhttp=false;\n")
                .append("\ttry{\n")
                .append("\t\txmlhttp = new ActiveXObject('Msxml2.XMLHTTP');\n")
                .append("\t}catch(e){\n")
                .append("\t\ttry{\n")
                .append("\t\t\txmlhttp = new ActiveXObject('Microsoft.XMLHTTP');\n")
                .append("\t\t}catch(E){\n")
                .append("\t\t\txmlhttp = false;\n")
                .append("\t\t}\n")
                .append("\t}\n")
                .append("\n")
                .append("\tif(!xmlhttp && typeof XMLHttpRequest!='undefined'){\n")
                .append("\t\txmlhttp = new XMLHttpRequest();\n")
                .append("\t}\n")
                .append("\treturn xmlhttp;\n")
                .append("}\n");
                sb.append("\n");
                sb.append("function generaPing (url){\n")
                .append("\tajax=ajaxObject();\n")
                .append("\tajax.open('GET', url, true); \n")
                .append("\tajax.onreadystatechange=function(){\n")
                .append("\t\tif(ajax.readyState==1){\n")
                .append("\t\t\t//Sucede cuando se esta cargando la pagina\n")
                .append("\t\t}else if(ajax.readyState==4){\n")
                .append("\t\t\t//Sucede cuando la pagina se cargó\n")
                .append("\t\t\tif(ajax.status==200){\n")
                .append("\t\t\t\t//Todo OK\n")
                .append("\t\t\t}else if(ajax.status==404){\n")
                .append("\t\t\t\t//La pagina no existe\n")
                .append("\t\t\t\talert('No se localizo el servicio de autenticación: '+url);\n")
                .append("\t\t\t\tlocation.href='").append(request.getContextPath()).append("';\n")
                .append("\t\t\t}else{\n")
                .append("\t\t\t\t//Mostramos el posible error\n")
                .append("\t\t\t\talert('Sesión invalida: '+ajax.status);\n")
                .append("\t\t\t\tlocation.href='").append(request.getContextPath()).append("';\n")
                .append("\t\t\t}\n")
                .append("\t\t}\n")
                .append("\t\t}\n")
                .append("\tajax.send(null);\n")
                .append("}\n");
                sb.append("\n");
                sb.append("function programaPing(url, espera){\n")
                .append("\tgeneraPing(url);\n")
                .append("\tif(espera==null) espera=5;\n")
                .append("\tsetTimeout('programaPing(\"'+url+'\")', espera*1000);\n")
                .append("}\n");
            
            response.setContentType("text/javascript");
            response.setHeader("Content-Disposition", "filename=sessionScripts.js");
            response.getOutputStream().write(sb.toString().getBytes());
            return;
        }
        else if( request.getParameter("sessionId")!=null ){
            response.setHeader("Cache-Control","no-cache"); 
            response.setHeader("Pragma","no-cache");
            response.setDateHeader ("Expires", 0);

            User uTmp=(User)request.getSession().getAttribute(AccessControllerPublisher.CLAVE_USER);
            if( uTmp!=null ){
//                uTmp.getSesion().setActividad(new Date());
                if( uTmp.getSesion()!=null ){
                    uTmp.getSesion().setActividad(new Date());
                }
                logger.info("Da ping el usuario "+uTmp.getUser()+".");
                response.getOutputStream().write((uTmp.getUser()+"-"+uTmp.getSesion().getActividad()).getBytes());
            }

            return;
        }
        else if( request.getParameter(AuthenticationWay.PARAM_LOGIN)==null ){
            //termina session
            SimpleAuthenticationServlet.terminaSesion(request, response);
            redireciona(request, response, this.login, this.mens.get("exit"));
        }
        else{
            redireciona(request, response, this.login, this.mens.get("methodGet"));
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
        HashMap<String, Object> datos=new HashMap<String, Object>();
        User u;
        boolean autenticar=false;

        //intenta recuperar el control de acceso
/*        if( request.getSession().getAttribute(AccessControllerPublisher.CLAVE_CTRL)!=null ){
            this.access=AccessControllerBean.getAccessController(request);
        }*/
        this.access=AccessController.getInstance();

        //recupero los parametros enviados
        while(pe.hasMoreElements()){
            par=(String)pe.nextElement();

            if( par.equals(AuthenticationWay.PARAM_LOGIN) ){
                autenticar=true;
            }
            else{
                datos.put(par, request.getParameter(par));
            }
        }
        
        u=validaExistenciaSesion(request, response);
        if( u!=null ){
            //existe cookie de sesion
            autenticar=false;
            request.getSession().setAttribute(AccessControllerPublisher.CLAVE_USER, u);
            logger.debug("Inicia la session del usuario por cookie");
        }

        if( autenticar ){
            //realizo la autenticación
            logger.debug("Inicia la validación del usuario");
            
            try{
                u=this.access.autenticaPersona(request.getRemoteAddr(), request.getRemoteHost(), EnvironmentType.WEB, datos);

                if( u.getEstado()==ValidationResult.BLOQUEADO ){
                    redireciona(request, response, this.login, this.mens.get("blocked"));
                }
                else if( u.getEstado()==ValidationResult.CADUCADO ){
                    redireciona(request, response, this.login, this.mens.get("expires"));
                }
                else if( u.getEstado()==ValidationResult.DENEGADO ){
                    redireciona(request, response, this.login, this.mens.get("denied"));
                }
                else if( u.getEstado()==ValidationResult.DENEGADO_PROPIO ){
                    redireciona(request, response, this.login, this.mens.get("deniedOwner"));
                }
                else if( u.getEstado()==ValidationResult.EN_USO ){
                    redireciona(request, response, this.login, this.mens.get("connected"));
                }
                else if( u.getEstado()==ValidationResult.ERROR_CODIGO ){
                    redireciona(request, response, this.login, this.mens.get("captcha"));
                }
                else if( u.getEstado()==ValidationResult.FUERA_DE_TIEMPO ){
                    redireciona(request, response, this.login, this.mens.get("outTime"));
                }
                else if( u.getEstado()==ValidationResult.INACTIVO ){
                    redireciona(request, response, this.login, this.mens.get("inactive"));
                }
                else if( u.getEstado()==ValidationResult.LIMITE_REBASADO ){
                    redireciona(request, response, this.login, this.mens.get("exceeded"));
                }
                else if( u.getEstado()==ValidationResult.NO_ENCONTRADO ){
                    redireciona(request, response, this.login, this.mens.get("noFound"));
                }
                else if( u.getEstado()==ValidationResult.SIN_USUARIO ){
                    redireciona(request, response, this.login, this.mens.get("noUser"));
                }
                else if( u.getEstado()==ValidationResult.VALIDADO ){
                    SimpleAuthenticationServlet.iniciaSesion(request, response, u);
                    this.cargaInfoPosValidacion(request, response);
                    response.sendRedirect(this.home);
                }
                else if( u.getEstado()==ValidationResult.VALIDADO_TEMPORAL ){
                    request.setAttribute(AccessControllerPublisher.CLAVE_USER, u);
                    redireciona(request, response, this.temp, this.mens.get("temporal"));
                }
            }
            catch(Exception ex){
                logger.error("No se logro realizar la autenticación", ex);
                redireciona(request, response, this.login, this.mens.get("error"));
            }
        }
        //cierra
        else{
            //cierro la sesion
            SimpleAuthenticationServlet.terminaSesion(request, response);
            try{
                response.sendRedirect(this.login);
            }catch(Exception ex){
                logger.error("No puedo redirecionar a login", ex);
            }
        }
    }
    

    /**
     * Redireciona la petici&oacute;n ejecutada hacia alg&uacute; otro recurso.
     * @param request Petici´&oacute;n de la p&aacute;gina
     * @param response  Respuesta de la  p&aacute;gina
     * @param pagina Recurso al que se desea redirecionar
     * @param mensaje Mensaje que se deba agragar a la lista de mensajes a desplegar 
     * en el nuevo recurso
     */
    public void redireciona(ServletRequest request, ServletResponse response, String pagina, String mensaje){
        RequestDispatcher dispatcher;

        if( mensaje!=null ){
            request.setAttribute(AccessControllerPublisher.CLAVE_MENSAJE, mensaje);
        }

        try{
            logger.debug("OK: "+response.isCommitted()+", "+mensaje);
            response.reset();
            //response.resetBuffer();
            dispatcher = request.getRequestDispatcher("/"+pagina);
            dispatcher.forward(request, response);
        }
        catch(Exception ex){
            logger.error("Error al redirecionar a '"+pagina+"'", ex);
        }
    }

    //------------------------------------------------------------------------

    /**
     * Revisa si la sesi&oacute;n de un usuario se quedo sin cerrar, para tal fin 
     * valida la cookie de sesi&oacute;n.
     * @param req Petici´&oacute;n de la p&aacute;gina
     * @param res Respuesta de la  p&aacute;gina
     * @return El usuario si es que exite sesi&oacute;n
     */
    public static User validaExistenciaSesion(HttpServletRequest req, HttpServletResponse res){
        AccessController ctrl=AccessController.getInstance();//(AccessController)req.getSession().getAttribute(AccessControllerPublisher.CLAVE_CTRL);
        User u=null;
        
        if( req==null || res==null || ctrl==null ){
            return null;
        }
        
        //revisa si existe la cookie
        for(Cookie c: req.getCookies()){
            if( c.getName().equals(HTTP_COOKIE_SESION) ){
                logger.info("Existe cooki de sesion activa: "+c.getValue());
                
                try{
                    u=ctrl.recuperaUsuarioConectadoSession(c.getValue());
                }
                catch(Exception ex){
                    logger.error("No logre recuperar el usuario por sesion.", ex);
                }

                if( u==null ){
                    c.setMaxAge(0);
                }
            }
        }
        
        return u;
    }
    
    /**
     * Genera la cokkie que almacena la sesi&oacute;n del usuario conectado.
     * @param req Petici´&oacute;n de la p&aacute;gina
     * @param res Respuesta de la  p&aacute;gina
     * @param u Usuario del que se desea generar su cookie
     */
    public static void generaCookieSesion(HttpServletRequest req, HttpServletResponse res, User u){
        //genero cookie de conexion
        logger.debug("Genero la cookie del usuario: "+u.getUser());
        AccessController ctrl=AccessController.getInstance();//(AccessController)req.getSession().getAttribute(AccessControllerPublisher.CLAVE_CTRL);
        Cookie cook=new Cookie(HTTP_COOKIE_SESION, u.getSesion().getId());
        cook.setPath(HTTP_PATH_COOKIES);
        cook.setMaxAge((int)ctrl.getTiempoVidaSesion());        
        res.addCookie(cook);
        
    }
    
    /**
     * Genera y carga el contexto de la sesi&oacute;n para un usuario conectado.
     * @param req Petici´&oacute;n de la p&aacute;gina
     * @param res Respuesta de la  p&aacute;gina
     * @param u Usuario del que se desea generar su sesi&oacute;n
     */
    public static void iniciaSesion(HttpServletRequest req, HttpServletResponse res, User u){
        boolean existe=false;;
        
        if( req==null || res==null ){
            return;
        }

        req.getSession().setAttribute(AccessControllerPublisher.CLAVE_USER, u);
        
        //revisa si existe sesion cargada por el api de NaApp
        List<SesionApp> sesiones=(List<SesionApp>)req.getSession().getServletContext().getAttribute(AppListener.APP_SESIONES);
        for(int i=0; sesiones!=null&&i<sesiones.size(); i++){
            if( sesiones.get(i).getId().equals(req.getSession().getId()) ){
                logger.debug("Vincula sesion con usuario: "+sesiones.get(i).getId());
                u.newSesion( sesiones.get(i) );
                existe=true;
                break;
            }
        }

//        u.getSesion().setAtributo(HTTP_SESSION, req.getSession());
        if( u.getSesion()!=null ){
            u.getSesion().setActividad(new Date());
        }
        generaCookieSesion(req, res, u);

        /*if( sesiones!=null ){
            for(SesionApp s: sesiones){
                if( s.getId().equals(req.getSession().getId()) ){
                    u.getSesion().setAtributo(HTTP_SO, s.getOs());
                    u.getSesion().setAtributo(HTTP_NAVEGADOR, s.getBrowser());
                    break;
                }
            }
        }*/
        
        if(  !existe ){
            u.newSesion(new SesionApp(req.getSession(), req.getSession().getId(), req.getRemoteAddr()));
        }
        
        logger.debug("Inicia la conexion del usuario: "+u.getUser());
    }
    
    /**
     * Destruye el contecto y sesi&oacute;n del usuario que se encuentra conectado.
     * @param req Petici´&oacute;n de la p&aacute;gina
     * @param res  Respuesta de la  p&aacute;gina
     */
    public static void terminaSesion(HttpServletRequest req, HttpServletResponse res){
        if( req==null || res==null ){
            return;
        }

        User u=(User)req.getSession().getAttribute(AccessControllerPublisher.CLAVE_USER);
        AccessController ctrl=AccessController.getInstance(); //(AccessController)req.getSession().getAttribute(AccessControllerPublisher.CLAVE_CTRL);
        
        //remuevo cookie
        if( req.getCookies()!=null ){
            for(Cookie c: req.getCookies()){
                if( u!=null && c.getName().equals(HTTP_COOKIE_SESION) ){
                    logger.debug("Termina la cookie del usuario: "+u.getUser());
                    c.setMaxAge(0);
                }
            }
        }
        
        req.getSession().removeAttribute(AccessControllerPublisher.CLAVE_USER);
        req.getSession().invalidate();
        if( u!=null ){
            try{
                ctrl.finaliza(u);
            }catch(Exception ex){
                logger.fatal("No se logro terminar la conexión", ex);
            }
        }
        
        logger.debug("Termina la conexion del usuario: "+(u==null? u: u.getUser()));
    }
    
    /**
     * Destruye el contecto y sesi&oacute;n del usuario que se encuentra conectado.
     * @param ses Sesi&oacute;n web activa
     */
    public static void terminaSesion(HttpSession ses){
        if( ses==null ){
            return;
        }

//        User u=(User)ses.getAttribute(AccessControllerPublisher.CLAVE_USER);
//        AccessController ctrl=AccessController.getInstance(); //(AccessController)ses.getAttribute(AccessControllerPublisher.CLAVE_CTRL);
        try{
            ses.removeAttribute(AccessControllerPublisher.CLAVE_USER);
            ses.invalidate();
        }
        catch(Exception ex){}
    }

    //------------------------------------------------------------------------

    /**
     * M&eacute;todo que se ejecuta para cargar informaci&oacute;n adicional posterior 
     * a la validaci&oacute;n del usuario.
     * @param request Petici´&oacute;n de la p&aacute;gina
     * @param response Respuesta de la  p&aacute;gina
     */
    protected void cargaInfoPosValidacion(HttpServletRequest request, HttpServletResponse response){
        //nada
    }
    
    //------------------------------------------------------------------------

    private void cargaEntorno(Document config){
        Element e, e2, raiz = config.getRootElement();

        e=raiz.getChild("authentication");
        if(e==null ){
            logger.debug("No se definio adecuadamenete la opcion de autenticación.");
            throw new RuntimeException("No se logro definir las opciones de autenticación.");
        }

        //defino el input
        if( e.getAttributeValue("login")!=null && e.getAttributeValue("login").length()>0 ){
            this.login=e.getAttributeValue("login");
        }
        else{
            this.login="login.html";
        }

        //defino el home
        e2=e.getChild("home");
        if( e2!=null && e2.getText().length()>0 ){
            this.home=e2.getText();
        }
        else{
            this.home="index.html";
        }

        //defino el temporal
        e2=e.getChild("temp");
        if( e2!=null && e2.getText().length()>0 ){
            this.temp=e2.getText();
        }
        else{
            this.temp="password.html";
        }

        e2=e.getChild("exit");
        if( e2!=null && e2.getText().length()>0 ){
            this.mens.put("exit", e2.getText());
        }
        else{
            this.mens.put("exit", "Sesión finalizada.");
        }
    }
}
