package neoAtlantis.utilidades.accessController.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import neoAtlantis.utilidades.accessController.AccessController;
import neoAtlantis.utilidades.accessController.allower.NullAllower;
import neoAtlantis.utilidades.accessController.allower.interfaces.AllowerWay;
import neoAtlantis.utilidades.accessController.audit.NullAudit;
import neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay;
import neoAtlantis.utilidades.accessController.audit.interfaces.LevelAudit;
import neoAtlantis.utilidades.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utilidades.accessController.blocker.MemoryBlocker;
import neoAtlantis.utilidades.accessController.blocker.NullBlocker;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockType;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay;
import neoAtlantis.utilidades.accessController.captcha.*;
import neoAtlantis.utilidades.accessController.captcha.interfaces.CaptchaPainter;
import neoAtlantis.utilidades.accessController.captcha.interfaces.ConfirmationCode;
import neoAtlantis.utilidades.accessController.cipher.*;
import neoAtlantis.utilidades.accessController.cipher.interfaces.DataCipher;
import neoAtlantis.utilidades.accessController.objects.PeticionesCaptcha;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.profiler.NullProfiler;
import neoAtlantis.utilidades.accessController.profiler.interfaces.ProfilerWay;
import neoAtlantis.utilidades.accessController.scheduler.GeneralScheduler;
import neoAtlantis.utilidades.accessController.scheduler.interfaces.SchedulerWay;
import neoAtlantis.utilidades.configFiles.ClassGenerator;
import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class AccessControlerPublisher implements HttpSessionListener{
    static final Logger logger = Logger.getLogger(AccessControlerPublisher.class);

    public static String CLAVE_CTRL="na.util.access.AccessCtrl";
    public static String CLAVE_USER="na.util.access.User";
    public static String CLAVE_CAPTCHA="na.util.access.Captcha";
    public static String CLAVE_CODE="na.util.access.CodeConfimation";
    public static String CLAVE_MENSAJE="na.util.access.MessageText";
    public static String CLAVE_PETITIONS="na.util.access.Captchas";
    public static String CLAVE_CONECTIONS="na.util.access.Conections";
    public static String CLAVE_BLOCKS="na.util.access.Blocks";

    private String homeWeb;
    private String homeWebInf;
    private User user;
    private AccessController access;
    private HashMap<String, Object> entorno=new HashMap<String, Object>();
    private ArrayList<User> conexiones;
    private ArrayList<User> bloqueos;

    public void sessionCreated(HttpSessionEvent hse) {        
        //configuro los home
        this.homeWeb=hse.getSession().getServletContext().getRealPath("/").replace('\\', '/')+"/";
        this.homeWebInf=homeWeb+"WEB-INF/";

        String home=this.homeWebInf+"config/configAccess.xml";

        //genero la coleccion de peticiones
        if( hse.getSession().getServletContext().getAttribute(CLAVE_PETITIONS)==null ){
            logger.debug("Inicializo las peticiones de Catpcha");
            hse.getSession().getServletContext().setAttribute(CLAVE_PETITIONS, new PeticionesCaptcha());
        }
        //genero los elemento del MemoryBlocker
        if( hse.getSession().getServletContext().getAttribute(CLAVE_CONECTIONS)==null ){
            logger.debug("Inicializo las sesiones del MemoryBlocker");
            this.conexiones=new ArrayList<User>();
            hse.getSession().getServletContext().setAttribute(CLAVE_CONECTIONS, this.conexiones);
        }
        else{
            this.conexiones=(ArrayList<User>)hse.getSession().getServletContext().getAttribute(CLAVE_CONECTIONS);
        }
        if( hse.getSession().getServletContext().getAttribute(CLAVE_BLOCKS)==null ){
            logger.debug("Inicializo los bloqueos del MemoryBlocker");
            this.bloqueos=new ArrayList<User>();
            hse.getSession().getServletContext().setAttribute(CLAVE_BLOCKS, this.bloqueos);
        }
        else{
            this.bloqueos=(ArrayList<User>)hse.getSession().getServletContext().getAttribute(CLAVE_BLOCKS);
        }

        //reviso si existe una configuracion personalizada
        if( hse.getSession().getServletContext().getInitParameter("configAccessNA")!=null && hse.getSession().getServletContext().getInitParameter("configAccessNA").length()>2 ){
            home=hse.getSession().getServletContext().getInitParameter("configAccessNA");
        }

        //genero el entorno
        this.entorno.put("appContext", hse.getSession().getServletContext());
        this.entorno.put("sessionContext", hse.getSession());
        this.entorno.put("homeWeb", this.homeWeb);

        //validamos la existencia del archivo de configuracion
        File fTmp=new File(home);
        if( fTmp.exists() ){
            try{
                //parseo el archivo XML
                Document doc=(new SAXBuilder(false)).build(home);
                AuthenticationWay aut=creaAutenticador(doc);
                DataCipher cip=creaCifrador(doc);
                ProfilerWay per=creaPerfilador(doc);
                AllowerWay perm=creaPermisor(doc);
                BlockerWay blo=creaBloqueador(doc);
                AuditWay bit=creaBitacora(doc);
                SchedulerWay cal=creaCalendario(doc);
                boolean multiple=obtieneMultiple(doc);
                int intentos=obtieneIntentos(doc);
                LevelAudit nivel=obtieneNivelBitacora(doc);

                this.access=new AccessController(aut, cip, per, perm,  blo, bit, cal);
                this.access.setUsuarioMultiple(multiple);
                this.access.setMaxIntentos(intentos);
                this.access.setNivelBitacora(nivel);

                //cargo las variables de aplicacion
                hse.getSession().setAttribute(CLAVE_CTRL, this.access);
                if( hse.getSession().getServletContext().getAttribute(CLAVE_CAPTCHA)==null ){
                    hse.getSession().getServletContext().setAttribute(CLAVE_CAPTCHA, creaCaptcha(doc));
                }
                if( hse.getSession().getServletContext().getAttribute(CLAVE_CODE)==null ){
                    hse.getSession().getServletContext().setAttribute(CLAVE_CODE, creaCodigo(doc));
                }
                if( hse.getSession().getServletContext().getAttribute(CLAVE_MENSAJE)==null ){
                    hse.getSession().getServletContext().setAttribute(CLAVE_MENSAJE, this.obtieneMensajesError(doc));
                }
            }
            catch(Exception ex){
                logger.error("Error al cargar el archivo de configuración de access '"+home+"'.", ex);
            }
        }
        else{
            throw new RuntimeException("No se ha definido la configuración para el Control de Acceso.");
        }
    }

    public void sessionDestroyed(HttpSessionEvent hse) {
        this.user=(User)hse.getSession().getAttribute(CLAVE_USER);

        if( this.user!=null ){
            try{
                this.access.finaliza(this.user);
            }
            catch(Exception ex){
                logger.error("Error al finalizar al usuario '"+this.user+"'.", ex);
            }
        }
    }

    //--------------------------------------------------------------------------

    private AuthenticationWay creaAutenticador(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("authenticationWay");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        if(obj!=null){
            return (AuthenticationWay)obj;
        }
        else{
            throw new RuntimeException("No se logro definir la clase del autentcador.");
        }
    }

    private ProfilerWay creaPerfilador(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("profilerWay");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        return (obj==null? new NullProfiler(): (ProfilerWay)obj);
    }

    private AllowerWay creaPermisor(Document config){
        return new NullAllower();
    }

    private BlockerWay creaBloqueador(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;
        BlockerWay blo;

        e=raiz.getChild("blockerWay");

        //revisa si utiliza el MemoryBlocker
        if( e==null || e.getAttribute("class")==null || e.getAttributeValue("class").equals("neoAtlantis.utilidades.accessController.blocker.MemoryBlocker") ){
            logger.debug("Se genera el MemoryBlocker");
            blo=new MemoryBlocker(this.conexiones, this.bloqueos);

            //recupero el tipo
            if( e.getAttribute("type")!=null ){
                if( e.getAttributeValue("type").equalsIgnoreCase("ip") ){
                    blo.setModoBloqueo(BlockType.IP);
                }
                else if( e.getAttributeValue("type").equalsIgnoreCase("user") ){
                    blo.setModoBloqueo(BlockType.USUARIO);
                }
                else if( e.getAttributeValue("type").equalsIgnoreCase("dual") ){
                    blo.setModoBloqueo(BlockType.IP_USUARIO);
                }
                else{
                    throw new RuntimeException("El valor de 'type' en 'blockerWay' solo puede ser 'ip', 'user' o 'dual'.");
                }
            }

            //recupero el tiempo
            if( e.getAttribute("time")!=null ){
                try{
                    blo.setTiempoBloqueo(Math.abs(Integer.parseInt(e.getAttributeValue("time"))*BlockerWay.MINUTO_EN_MILIS));
                }
                catch(Exception ex1){
                    throw new RuntimeException("El valor de 'time' en 'blockerWay' solo entero positivo.");
                }
            }

            return blo;
        }

        //si no utiliza el MemoryBlocker genero el especificado
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        if(obj==null){
            blo=new NullBlocker();
        }
        else{
            blo=(BlockerWay)obj;

            //recupero el tipo
            if( e.getAttribute("type")!=null ){
                if( e.getAttributeValue("type").equalsIgnoreCase("ip") ){
                    blo.setModoBloqueo(BlockType.IP);
                }
                else if( e.getAttributeValue("type").equalsIgnoreCase("user") ){
                    blo.setModoBloqueo(BlockType.USUARIO);
                }
                else{
                    throw new RuntimeException("El valor de 'type' en 'blockerWay' solo puede ser 'ip' o 'user'.");
                }
            }

            //recupero el tiempo
            if( e.getAttribute("time")!=null ){
                try{
                    blo.setTiempoBloqueo(Math.abs(Integer.parseInt(e.getAttributeValue("time"))*BlockerWay.MINUTO_EN_MILIS));
                }
                catch(Exception ex1){
                    throw new RuntimeException("El valor de 'time' en 'blockerWay' solo entero positivo.");
                }
            }
        }

        return blo;
    }

    private AuditWay creaBitacora(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("auditWay");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        return (obj==null? new NullAudit(): (AuditWay)obj);
    }

    private SchedulerWay creaCalendario(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("schedulerWay");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        return (obj==null? new GeneralScheduler(): (SchedulerWay)obj);
    }

    private ConfirmationCode creaCodigo(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("confirmationCode");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        return (obj==null? new BasicConfirmationCode(): (ConfirmationCode)obj);
}

    private CaptchaPainter creaCaptcha(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("captcha");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        return (obj==null? new PointLineCaptcha(): (CaptchaPainter)obj);
    }

    private DataCipher creaCifrador(Document config){
        Element e, raiz = config.getRootElement();
        Object obj;

        e=raiz.getChild("cipher");
        obj=ClassGenerator.generaInstancia(e, new ArrayList(), ClassGenerator.generaComodinesHomeWeb(this.homeWeb), this.entorno);

        return (obj==null? new CipherMd5Des("default"): (DataCipher)obj);
    }

    private boolean obtieneMultiple(Document config){
        boolean b=false;
        Element raiz = config.getRootElement();

        if(raiz.getAttribute("multiple")==null){
            return b;
        }

        try{
            b=(new Boolean(raiz.getAttributeValue("multiple"))).booleanValue();
        }
        catch(Exception ex){
            throw new RuntimeException("El valor de 'multiple' en 'accessController' solo puede ser 'true' o 'false'.");
        }

        return b;
    }

    private int obtieneIntentos(Document config){
        int i=3;
        Element raiz = config.getRootElement();

        if(raiz.getAttribute("attempts")==null){
            return i;
        }

        try{
            i=Integer.parseInt(raiz.getAttributeValue("attempts"));
            i=Math.abs(i);
        }
        catch(Exception ex){
            throw new RuntimeException("El valor de 'attempts' en 'accessController' solo puede ser entero positivo.");
        }

        return i;
    }

    private LevelAudit obtieneNivelBitacora(Document config){
        int i=3;
        Element raiz = config.getRootElement();

        if(raiz.getAttribute("levelAudit")==null){
            return LevelAudit.ACCESO;
        }

        if( raiz.getAttributeValue("levelAudit").equalsIgnoreCase("all") ){
            return LevelAudit.COMPLETA;
        }
        else if( raiz.getAttributeValue("levelAudit").equalsIgnoreCase("access") ){
            return LevelAudit.ACCESO;
        }
        else if( raiz.getAttributeValue("levelAudit").equalsIgnoreCase("null") ){
            return LevelAudit.NULA;
        }
        else{
            throw new RuntimeException("El valor de 'levelAudit' en 'accessController' solo puede ser 'all', 'access' o 'null'.");
        }
    }

    private Map<String, String> obtieneMensajesError(Document config){
        Element e, e2,raiz = config.getRootElement();
        HashMap<String,String> m=new HashMap<String,String>();

        //defino los mensajes
        e=raiz.getChild("messages");
        e2=e.getChild("methodGet");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("methodGet", e2.getText());
        }
        else{
            m.put("methodGet", "No esta permitido el metodo 'get' para autenticar.");
        }
        e2=e.getChild("error");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("error", e2.getText());
        }
        else{
            m.put("error", "Error al autenticar.");
        }
        e2=e.getChild("blocked");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("blocked", e2.getText());
        }
        else{
            m.put("blocked", "El usuario esta baneado.");
        }
        e2=e.getChild("expires");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("expires", e2.getText());
        }
        else{
            m.put("expires", "El usuario esta caduco.");
        }
        e2=e.getChild("denied");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("denied", e2.getText());
        }
        else{
            m.put("denied", "Acceso denegado.");
        }
        e2=e.getChild("connected");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("connected", e2.getText());
        }
        else{
            m.put("connected", "El usuario ya esta conectado.");
        }
        e2=e.getChild("captcha");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("captcha", e2.getText());
        }
        else{
            m.put("captcha", "La clave de confirmación no es correcta.");
        }
        e2=e.getChild("outTime");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("outTime", e2.getText());
        }
        else{
            m.put("outTime", "No esta permitido el acceso en este horario.");
        }
        e2=e.getChild("inactive");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("inactive", e2.getText());
        }
        else{
            m.put("inactive", "El usuario esta deshabilitado.");
        }
        e2=e.getChild("exceeded");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("exceeded", e2.getText());
        }
        else{
            m.put("exceeded", "Se ha revasado el numero de intentos permitidos, la cuenta sera baneada.");
        }
        e2=e.getChild("noFound");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("noFound", e2.getText());
        }
        else{
            m.put("noFound", "El usuario no es valido.");
        }
        e2=e.getChild("temporal");
        if( e2!=null && e2.getText().length()>0 ){
            m.put("temporal", e2.getText());
        }
        else{
            m.put("temporal", "Acceso temporal, se requiere modificar la contraseña.");
        }

        return m;
    }
}
