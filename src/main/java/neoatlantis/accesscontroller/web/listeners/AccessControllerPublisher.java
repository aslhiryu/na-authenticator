package neoatlantis.accesscontroller.web.listeners;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;
import neoatlantis.accesscontroller.AccessController;
import neoatlantis.accesscontroller.allower.interfaces.AllowerWay;
import neoatlantis.accesscontroller.audit.interfaces.AuditWay;
import neoatlantis.accesscontroller.audit.interfaces.LevelAudit;
import neoatlantis.accesscontroller.authentication.interfaces.AuthenticationWay;
import neoatlantis.accesscontroller.blocker.MemoryBlocker;
import neoatlantis.accesscontroller.blocker.interfaces.BlockType;
import neoatlantis.accesscontroller.blocker.interfaces.BlockerWay;
import neoatlantis.accesscontroller.printer.interfaces.LoginPrinter;
import neoatlantis.accesscontroller.login.interfaces.AuthenticationLogin;
import neoatlantis.accesscontroller.printer.interfaces.AuditPrinter;
import neoatlantis.accesscontroller.printer.interfaces.RoleAdministratorPrinter;
import neoatlantis.accesscontroller.printer.interfaces.UserAdministratorPrinter;
import neoatlantis.accesscontroller.profiler.GenericProfiler;
import neoatlantis.accesscontroller.profiler.interfaces.ProfilerWay;
import neoatlantis.accesscontroller.resourceAccessAllower.interfaces.ResourceAccessAllower;
import neoatlantis.accesscontroller.scheduler.GeneralScheduler;
import neoatlantis.accesscontroller.scheduler.interfaces.SchedulerWay;
import neoatlantis.accesscontroller.utils.DefaultErrorMessages;
import neoatlantis.accesscontroller.web.AdminUsersServlet;
import neoatlantis.accesscontroller.web.utils.AuthenticationResourcesLoader;
import neoatlantis.accesscontroller.web.utils.PhotoUserServlet;
import neoatlantis.applications.web.listeners.ApplicationListener;
import neoatlantis.utils.cipher.CipherMd5Des;
import neoatlantis.utils.cipher.interfaces.DataCipher;
import neoatlantis.utils.configurations.ClassGenerator;
import neoatlantis.utils.configurations.ConfigurationUtils;
import neoatlantis.utils.data.DataUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Clase de apoyo para carga la configuración de acceso a recursos al iniciar cualquier sesion web
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class AccessControllerPublisher implements ServletContextListener{
    private static final Logger DEBUGER = Logger.getLogger(AccessControllerPublisher.class);

    /**
     * Clave con la que se ubica el Acces Controller en los contextos
     */
    public static String ACCESS_CTRL_KEY="na.util.access.AccessCtrl";
    /**
     * Clave con la que se ubica el Usuario en los contextos
     */
    public static String USER_KEY="na.util.access.User";
    /**
     * Clave con la que se ubica los mensajes de error en los contextos
     */
    public static String MESSAGES_KEY="na.util.access.MessagesText";
    /**
     * Clave en donde se localiza el error de autenticación
     */
    public static String MESSAGE_ERROR_KEY="na.util.access.MessageError";
    /**
     * Clave en donde se localiza el printer de logeo
     */
    public static String LOGIN_PRINTER_KEY="na.util.access.printers.Login";
    /**
     * Clave en donde se localiza el printer de auditoria
     */
    public static String AUDIT_PRINTER_KEY="na.util.access.printers.Audit";
    /**
     * Clave en donde se localiza el printer de administración de usuarios
     */
    public static String USERS_ADMIN_PRINTER_KEY="na.util.access.printers.UsersAdministration";
    public static String ROLES_ADMIN_PRINTER_KEY="na.util.access.printers.RolesAdministration";
    /**
     * Clave con la que se localiza la ruta del login para el acceso
     */
    public static String PATH_LOGIN="/neoAtlantis/resources/web/login.jsp";

    private boolean configurado=false;
    private HashMap<String, Object> entorno=new HashMap<String, Object>();

    
    
    // Contructores ------------------------------------------------------------
    
    public AccessControllerPublisher(){
    }
    
    public AccessControllerPublisher(Element root, ServletContext context){
        this.createEnvironmentContext(context);
        this.initEnvironment(context, root);
    }
    
    
    
    // Metodos publicos---------------------------------------------------------

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        createEnvironmentContext(sce.getServletContext());
        
        String configFile=this.entorno.get("homeWebInf")+"config/NA_autenthication.xml";

        //reviso si existe una configuracion personalizada
        if( sce.getServletContext().getInitParameter("configAccessNA")!=null && sce.getServletContext().getInitParameter("configAccessNA").length()>2 ){
            configFile = ConfigurationUtils.parseWindcars(sce.getServletContext().getInitParameter("configAccessNA"), entorno);
        }
        DEBUGER.debug("Archivo de configuración: "+configFile);

        //validamos la existencia del archivo de configuracion y si aun no se ha configurado
        File fTmp=new File(configFile);
        if( fTmp.exists() && !configurado ){
            try{
                //parseo el archivo XML
                Document doc=(new SAXBuilder(false)).build(configFile);
                initEnvironment(sce.getServletContext(), doc.getRootElement());
            }
            catch(Exception ex){
                DEBUGER.error("Error al cargar el archivo de configuración de access '"+configFile+"'.", ex);
                throw new RuntimeException("Error al cargar el archivo de configuración de access '"+configFile+"'.");
            }            
        }
        else if( !fTmp.exists() && !configurado ){
            throw new RuntimeException("No se ha definido la configuración para el Control de Acceso.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {        
        this.finalizeEnvironment(sce.getServletContext());
    }

    
    
    //  Metodos Privados--------------------------------------------------------

    private void finalizeEnvironment(ServletContext context){
        //remueve el registro del MBean de sesiones
        String app=(String)context.getAttribute(ApplicationListener.APP_NAME_KEY);
        ApplicationListener.unregisterMBean("neoatlantis.app."+(app!=null? DataUtils.cleanSpecialCharacters(app): "undefined")+".users.jmx:type=InfoStatusUsers");
    }
    

    /**
     * Metodo que prepara las variables del entorno
     * @param context Contexto de la aplicación web
     */
    private void createEnvironmentContext(ServletContext context){
        //configuro los home
        String homeWeb = context.getRealPath("/").replace('\\', '/') + "/";
        String homeWebInf = homeWeb + "WEB-INF/";
        
        //genero el entorno
        this.entorno.put("appContext", context);
        this.entorno.put("homeWeb", homeWeb);
        this.entorno.put("homeWebInf", homeWebInf);
        this.entorno.put("homeClass", homeWebInf + "classes/");
    }
    
    /**
     * Prepara todo el entorno para el Access Controller
     * @param root Nodo que contiene la configuración del Access Controller
     */
    private void initEnvironment(ServletContext context, final Element root){
        AccessController access=null;
        javax.servlet.ServletRegistration.Dynamic dimRequest;
        DEBUGER.debug("Configuro el entorno del Control de Acceso");
                
        try {
            AuthenticationWay aut = makeAuthentication(root);
            DataCipher cip = makeCipher(root);
            ProfilerWay per = makeProfiler(root);
            AllowerWay perm = makeAllower(root);
            BlockerWay blo = makeBlocker(context, root);
            AuditWay bit = makeAudit(root);
            SchedulerWay cal = makeScheduler(root);
            boolean multiple = validateMultiple(root);
            boolean useCaptcha = validateCaptcha(root);
            int intentos = readAttempts(root);
            LevelAudit nivel = readLevelAudit(root);
            LoginPrinter logFor=makeLoginPrinter(root);
            UserAdministratorPrinter uaFor=makeUserAdministratorPrinter(root);
            RoleAdministratorPrinter raFor=makeRoleAdministratorPrinter(root);
            AuditPrinter autFor=makeAuditPrinter(root);
            String app=(String)context.getAttribute(ApplicationListener.APP_NAME_KEY);

            if( bit!=null ){
                bit.setLevelAudit(nivel);
            }
            if(app==null){
                app="undefined";
            }
            
            access = AccessController.getInstance(app, aut, cip, per, perm, blo, bit, cal);
            access.setMultipleUser(multiple);
            access.setUseCaptcha(useCaptcha);
            access.setMaximiumAttempt(intentos);

            //cargo las variables de aplicacion
            context.setAttribute(ACCESS_CTRL_KEY, access);
            context.setAttribute(LOGIN_PRINTER_KEY, logFor);
            context.setAttribute(USERS_ADMIN_PRINTER_KEY, uaFor);
            context.setAttribute(ROLES_ADMIN_PRINTER_KEY, raFor);
            context.setAttribute(AUDIT_PRINTER_KEY, autFor);
            if (context.getAttribute(MESSAGES_KEY) == null) {
                context.setAttribute(MESSAGES_KEY, this.loadErrorMessages(root));
            }
            
            //publico el login
            AuthenticationLogin log=makeLogin(root);
            if( log!=null ){
                if( !(log instanceof HttpServlet) ){
                    throw new RuntimeException("El Login proporcionado no es valido, debe ser un Servlet.");
                }
                dimRequest=context.addServlet("LoginNA", (HttpServlet)log);
                dimRequest.addMapping(PATH_LOGIN);
                dimRequest.setAsyncSupported(true);
                DEBUGER.debug("Publica el Login en: "+PATH_LOGIN);
            }
            
            //publico los servicios de administracion de usuarios
            dimRequest=context.addServlet("UserAdministrationNA", new AdminUsersServlet());
            dimRequest.addMapping(AdminUsersServlet.PATH_SERVICE);
            dimRequest.setAsyncSupported(true);
            DEBUGER.debug("Publica el Servicio de Administración de usuarios en: "+AdminUsersServlet.PATH_SERVICE);
            
            //publico los servicios de visualizacion de fotos del usuario
            dimRequest=context.addServlet("PhotoUserViewNA", new PhotoUserServlet());
            dimRequest.addMapping(PhotoUserServlet.PATH_SERVICE);
            dimRequest.setAsyncSupported(true);
            DEBUGER.debug("Publica el Servicio de Visualización de foto de usuarios en: "+PhotoUserServlet.PATH_SERVICE);
            
            //publico el filtro
            ResourceAccessAllower raa=makeResourceAccessAllower(root);
            if( raa!=null ){
                if( !(raa instanceof Filter) ){
                    throw new RuntimeException("El Control de acceso a recursos proporcionado no es valido, debe ser un Filter.");
                }
                javax.servlet.FilterRegistration.Dynamic dim=context.addFilter("AccessResourceNA", (Filter)raa);
                dim.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");                
            }
            
            //publico los elementos necesarios
            AuthenticationResourcesLoader.loadResorces(context);
        } catch (Exception ex) {
            DEBUGER.error("Error al realizar la configuración del Access Controller'.", ex);
            throw new RuntimeException("No se logro realizar la configuración del Access Controller: "+ex);
        }

        this.configurado=true;
    }

    /**
     * Genera un Autenticador
     * @param root Nodo que contiene la configuración del Access Controller
     * @return Autenticador generado
     */
    private AuthenticationWay makeAuthentication(final Element root){
        Element e;
        Object obj;
        AuthenticationWay aut;

        e=root.getChild("authenticationWay");
        obj=ClassGenerator.createInstance(e, this.entorno);

        if(obj!=null){
            aut= (AuthenticationWay)obj;
            
            //recupero el tiempo de vida del login
            if( e!=null && e.getAttribute("loginLife")!=null ){
                try{
                    aut.setLoginLife(Math.abs(Integer.parseInt(e.getAttributeValue("loginLife"))));
                }
                catch(Exception ex1){
                    throw new RuntimeException("El valor de 'loginLife' en 'authenticationWay' solo entero positivo.");
                }
            }        

            DEBUGER.debug("Authentication generada: "+aut.getClass());
            DEBUGER.debug("Tiempo de vida del usuario: "+aut.getLoginLife());
            return aut;
        }
        else{
            throw new RuntimeException("No se logro definir el autenticador.");
        }
    }

    /**
     * Genera un Cifrador de datos
     * @param root Nodo que contiene la configuración del Access Controller
     * @return Cifrador de datos generado
     */
    private DataCipher makeCipher(final Element root){
        Element e;
        Object obj;

        e=root.getChild("cipher");
        obj=ClassGenerator.createInstance(e, this.entorno);

        DEBUGER.debug("Cifrador generado: "+(obj==null? CipherMd5Des.class: obj.getClass()));
        return (obj==null? new CipherMd5Des("default"): (DataCipher)obj);
    }
    
    /**
     * Genera un Perfilador
     * @param root Nodo que contiene la configuración del Access Controller
     * @return Perfilador generado
     */
    private ProfilerWay makeProfiler(final Element root){
        Element e;
        Object obj;

        e=root.getChild("profilerWay");
        obj=ClassGenerator.createInstance(e, this.entorno);
        
        if( obj==null ){
            obj=new GenericProfiler();
        }

        DEBUGER.debug("Perfilador generado: "+obj);
        return (ProfilerWay)obj;
    }

    /**
     * Genera un Permisor
     * @param root Nodo que contiene la configuración del Access Controller
     * @return Permisor generado
     */
    private AllowerWay makeAllower(final Element root){
        Element e;
        Object obj;

        e=root.getChild("allowerWay");
        obj=ClassGenerator.createInstance(e, this.entorno);

        DEBUGER.debug("Permisor generado: "+(obj==null? "": obj.getClass()));
        return (AllowerWay)obj;
    }

    /**
     * Genera un Bloqueador
     * @param context  Contexto de la aplicación web
     * @param root Nodo que contiene la configuración del Access Controller
     * @return Bloqueador generado
     */
    private BlockerWay makeBlocker(ServletContext context, final Element root){
        Element e;
        BlockerWay blo;

        e=root.getChild("blockerWay");
        blo=(BlockerWay)ClassGenerator.createInstance(e, this.entorno);
        
        //si no se definio bloqueador defino el de memoria
        if( blo==null ){
            blo=new MemoryBlocker(context);
        }
        
        //recupero el tipo
        if( e!=null && e.getAttribute("type")!=null ){
            if( e.getAttributeValue("type").equalsIgnoreCase("ip") ){
                blo.setBlockType(BlockType.IP);
            }
            else if( e.getAttributeValue("type").equalsIgnoreCase("user") ){
                blo.setBlockType(BlockType.USUARIO);
            }
            else{
                throw new RuntimeException("El valor de 'type' en 'blockerWay' solo puede ser 'ip' o 'user'.");
            }
        }

        //recupero el tiempo
        if( e!=null && e.getAttribute("timeBlocked")!=null ){
            try{
                blo.setBlockTime(Math.abs(Integer.parseInt(e.getAttributeValue("timeBlocked"))*BlockerWay.MINUTE_IN_MILLIS));
            }
            catch(Exception ex1){
                throw new RuntimeException("El valor de 'timeBlocked' en 'blockerWay' solo entero positivo.");
            }
        }        

        //recupero el tiempo de sesion
        if( e!=null && e.getAttribute("sessionLife")!=null ){
            try{
                blo.setSessionTime(Math.abs(Integer.parseInt(e.getAttributeValue("sessionLife"))*BlockerWay.MINUTE_IN_MILLIS));
            }
            catch(Exception ex1){
                throw new RuntimeException("El valor de 'sessionLife' en 'blockerWay' solo entero positivo.");
            }
        }

        //igualo el tiempo de sesion
        //blo.setSessionTime(sesion.getMaxInactiveInterval());
        DEBUGER.debug("Tiempo de sesion: "+blo.getSessionTime());
        DEBUGER.debug("Tiempo de bloqueo: "+blo.getBlockTime());

        DEBUGER.debug("Bloqueador generado: "+blo.getClass());
        return blo;
    }

    /**
     * Genera un auditor
     * @param root Nodo que contiene la configuración del Access Controller
     * @return Auditor generado
     */
    private AuditWay makeAudit(final Element root){
        Element e;
        Object obj;

        e=root.getChild("auditWay");
        obj=ClassGenerator.createInstance(e, this.entorno);

        DEBUGER.debug("Auditor generado: "+(obj==null? "": obj.getClass()));
        return (AuditWay)obj;
    }

    /**
     * Genera un Calendario
     * @param root Nodo que contiene la configuración del Access Controller
     * @return Calendario generado
     */
    private SchedulerWay makeScheduler(final Element root){
        Element e;
        Object obj;

        e=root.getChild("schedulerWay");
        obj=ClassGenerator.createInstance(e, this.entorno);

        DEBUGER.debug("Calendarizador generado: "+(obj==null? GeneralScheduler.class: obj.getClass()));
        return (obj==null? new GeneralScheduler(): (SchedulerWay)obj);
    }
    
    private AuditPrinter makeAuditPrinter(final Element root){
        Element e;
        Object obj;

        e=root.getChild("auditPrinter");
        obj=ClassGenerator.createInstance(e, this.entorno);

        DEBUGER.debug("Impresor de Auditoria generado: "+(obj==null? "": obj.getClass()));
        return (AuditPrinter)obj;
    }

    /**
     * Genera un formateador de Login
     * @param root Nodo que contiene la configuración del Access Controller
     * @return Formateador generado
     */
    private LoginPrinter makeLoginPrinter(final Element root){
        Element e;
        Object obj;

        e=root.getChild("loginPrinter");
        obj=ClassGenerator.createInstance(e, this.entorno);

        DEBUGER.debug("Impresor de Login generado: "+(obj==null? "": obj.getClass()));
        return (LoginPrinter)obj;
    }

    private UserAdministratorPrinter makeUserAdministratorPrinter(final Element root){
        Element e;
        Object obj;

        e=root.getChild("usersAdministrationPrinter");
        obj=ClassGenerator.createInstance(e, this.entorno);

        DEBUGER.debug("Impresor de administracion de usuarios generado: "+(obj==null? "": obj.getClass()));
        return (UserAdministratorPrinter)obj;
    }

    private RoleAdministratorPrinter makeRoleAdministratorPrinter(final Element root){
        Element e;
        Object obj;

        e=root.getChild("rolesAdministrationPrinter");
        obj=ClassGenerator.createInstance(e, this.entorno);

        DEBUGER.debug("Impresor de administracion de roles generado: "+(obj==null? "": obj.getClass()));
        return (RoleAdministratorPrinter)obj;
    }

    /**
     * Genera un login
     * @param root Nodo que contiene la configuración del Access Controller
     * @return Login generado
     */
    private AuthenticationLogin makeLogin(final Element root){
        Element e;
        Object obj;

        e=root.getChild("authenticationLogin");
        obj=ClassGenerator.createInstance(e, this.entorno);

        DEBUGER.debug("Login generado: "+obj);
        return (obj==null? null: (AuthenticationLogin)obj);
    }
    
    private ResourceAccessAllower makeResourceAccessAllower(final Element root){
        Element e;
        Object obj;

        e=root.getChild("resourceAccessAllower");
        obj=ClassGenerator.createInstance(e, this.entorno);

        DEBUGER.debug("Contorl de acceso a recursos generado: "+obj);
        return (obj==null? null: (ResourceAccessAllower)obj);
    }

    /**
     * Revisa si se permite la conectividad de multiples usuarios
     * @param root
     * @return 
     */
    private boolean validateMultiple(final Element root){
        boolean m=false;
        
        if(root.getAttribute("multiple")!=null){
            m=DataUtils.validateTrueBoolean(root.getAttributeValue("multiple"));
        }

        DEBUGER.debug("Definición de Multiple: "+m);
        return m;
    }

    /**
     * Revisa si se utiliza el captcha
     * @param root
     * @return 
     */
    private boolean validateCaptcha(final Element root){
        boolean m=false;
        
        if(root.getAttribute("useCaptcha")!=null){
            m=DataUtils.validateTrueBoolean(root.getAttributeValue("useCaptcha"));
        }

        DEBUGER.debug("Definición de UseCaptcha: "+m);
        return m;
    }

    /**
     * Obtiene el numero de intentos permitidos
     * @param root Nodo que contiene la configuración del Access Controller
     * @return el Número de intentos permitidos
     */
    private int readAttempts(final Element root){
        int i=3;

        if(root.getAttribute("attempts")!=null){
            try{
                i=Integer.parseInt(root.getAttributeValue("attempts"));
                i=Math.abs(i);
            }
            catch(Exception ex){
                throw new RuntimeException("El valor de 'attempts' en 'accessController' solo puede ser entero positivo.");
            }
        }

        DEBUGER.debug("Definición de Intentos: "+i);
        return i;
    }

    /**
     * Obtiene el nivel de auditoria a utilizar
     * @param root Nodo que contiene la configuración del Access Controller
     * @return Nivel de auditoria configurado
     */
    private LevelAudit readLevelAudit(final Element root){
        LevelAudit l=LevelAudit.BASIC;
        
        if( root.getAttribute("levelAudit")!=null && root.getAttributeValue("levelAudit").equalsIgnoreCase("full") ){
            l=LevelAudit.FULL;
        }
        else if( root.getAttribute("levelAudit")!=null && root.getAttributeValue("levelAudit").equalsIgnoreCase("access") ){
            l=LevelAudit.ACCESS;
        }
        else if( root.getAttribute("levelAudit")!=null && root.getAttributeValue("levelAudit").equalsIgnoreCase("admin") ){
            l=LevelAudit.ADMIN;
        }
        else if( root.getAttribute("levelAudit")!=null && root.getAttributeValue("levelAudit").equalsIgnoreCase("business") ){
            l=LevelAudit.BUSINESS;
        }
        else if( root.getAttribute("levelAudit")!=null && root.getAttributeValue("levelAudit").equalsIgnoreCase("null") ){
            l=LevelAudit.NULL;
        }
        else if( root.getAttribute("levelAudit")!=null && root.getAttributeValue("levelAudit").equalsIgnoreCase("basic") ){
            l=LevelAudit.BASIC;
        }
        else if( root.getAttribute("levelAudit")!=null ){
            throw new RuntimeException("El valor de 'levelAudit' en 'accessController' solo puede ser {"+Arrays.toString(LevelAudit.values())+"}.");
        }
        
        DEBUGER.debug("Definición de Nivel de Auditoria: "+l);
        return l;
    }
    
    /**
     * Carga los mensajes de error
     * @param root Nodo con la configuración 
     * @return Mapa con los mensajes cargados
     */
    private Map<String, String> loadErrorMessages(Element root){
        Element e, e2;
        HashMap<String,String> m=new HashMap<String,String>();

        //defino los mensajes
        e=root.getChild("messages");
        if(e!=null){
            e2=e.getChild("methodGet");
            m.put("methodGet", DefaultErrorMessages.defineMethodGetMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("error");
            m.put("error", DefaultErrorMessages.defineErrorMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("blocked");
            m.put("blocked", DefaultErrorMessages.defineBlockedMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("expires");
            m.put("expires", DefaultErrorMessages.defineExpiresMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("denied");
            m.put("denied", DefaultErrorMessages.defineDeniedMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("connected");
            m.put("connected", DefaultErrorMessages.defineConnectedMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("captcha");
            m.put("captcha", DefaultErrorMessages.defineCaptchaMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("outTime");
            m.put("outTime", DefaultErrorMessages.defineOuttimeMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("inactive");
            m.put("inactive", DefaultErrorMessages.defineInactiveMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("exceeded");
            m.put("exceeded", DefaultErrorMessages.defineExcededMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("notFound");
            m.put("notFound", DefaultErrorMessages.defineNotFoundMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("temporal");
            m.put("temporal", DefaultErrorMessages.defineTemporalMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("notProvisioned");
            m.put("notProvisioned", DefaultErrorMessages.defineProvisioningMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("notUser");
            m.put("notUser", DefaultErrorMessages.defineNotUserMessage(e2!=null? e2.getText(): null));
            e2=e.getChild("invalidCaptcha");
            m.put("invalidCaptcha", DefaultErrorMessages.defineNotUserMessage(e2!=null? e2.getText(): null));
        }
        else{
            m.put("methodGet", DefaultErrorMessages.defineMethodGetMessage(null));
            m.put("error", DefaultErrorMessages.defineErrorMessage(null));
            m.put("blocked", DefaultErrorMessages.defineBlockedMessage(null));
            m.put("expires", DefaultErrorMessages.defineExpiresMessage(null));
            m.put("denied", DefaultErrorMessages.defineDeniedMessage(null));
            m.put("connected", DefaultErrorMessages.defineConnectedMessage(null));
            m.put("captcha", DefaultErrorMessages.defineCaptchaMessage(null));
            m.put("outTime", DefaultErrorMessages.defineOuttimeMessage(null));
            m.put("inactive", DefaultErrorMessages.defineInactiveMessage(null));
            m.put("exceeded", DefaultErrorMessages.defineExcededMessage(null));
            m.put("notFound", DefaultErrorMessages.defineNotFoundMessage(null));
            m.put("temporal", DefaultErrorMessages.defineTemporalMessage(null));
            m.put("notProvisioned", DefaultErrorMessages.defineProvisioningMessage(null));
            m.put("notUser", DefaultErrorMessages.defineNotUserMessage(null));
        }

        return m;
    }
}
