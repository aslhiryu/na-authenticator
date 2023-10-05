package neoAtlantis.utilidades.accessController.utils;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import java.io.File;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.resourcesFilter.DenialType;
import neoAtlantis.utilidades.accessController.resourcesFilter.FilterException;
import neoAtlantis.utilidades.accessController.resourcesFilter.TypeException;
import neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class SimpleValidatorInterceptor implements Interceptor {
    static final Logger logger = Logger.getLogger(SimpleValidatorInterceptor.class);

    private String homeWeb;
    private String homeWebInf;
    private String homeClass;
    protected ArrayList<FilterException> excs = null;
    protected String redir;
    protected String home;
    protected String oriRedir;
    protected String oriHome;
    protected String mens;
    protected String mensPer;
    protected ResourceAccessAllower val;
    protected User user;
    protected String extStruts="sso";

    private HashMap<String, Object> entorno=new HashMap<String, Object>();

    public void init() {
        System.out.println("Inicializando Validador de Session por Interceptor");
    }

    public String intercept(ActionInvocation actionInvocation) throws Exception {
        if( redir==null ){
            this.cargaEntorno(actionInvocation);
        }
        /*Map<String, Object> m=actionInvocation.getInvocationContext().getContextMap();
        Iterator i=m.keySet().iterator();
        String c;

        while( i.hasNext() ){
            c=(String)i.next();
            System.out.println("--> "+c+"="+m.get(c));
        }

        HttpServletRequestWrapper req=(HttpServletRequestWrapper)actionInvocation.getInvocationContext().getContextMap().get("com.opensymphony.xwork2.dispatcher.HttpServletRequest");
        System.out.println("==> "+req.getRealPath("/"));*/

        DenialType validacion=ValidatorConfigurator.validaAccesoRecuros(this.excs, this.oriRedir, this.oriHome, this.val, ((HttpServletRequestWrapper)actionInvocation.getInvocationContext().getContextMap().get("com.opensymphony.xwork2.dispatcher.HttpServletRequest")), ((HttpServletResponseWrapper)actionInvocation.getInvocationContext().getContextMap().get("com.opensymphony.xwork2.dispatcher.HttpServletResponse")));

        if (validacion==DenialType.NO_CONECTADO) {
            return this.sessionCaducada(((HttpServletRequestWrapper)actionInvocation.getInvocationContext().getContextMap().get("com.opensymphony.xwork2.dispatcher.HttpServletRequest")));
        }
        else if(validacion==DenialType.NO_PERMITIDO) {
            return this.permisosNoValidos(((HttpServletRequestWrapper)actionInvocation.getInvocationContext().getContextMap().get("com.opensymphony.xwork2.dispatcher.HttpServletRequest")));
        }
        else if(validacion==DenialType.RESTRICTIVO) {
            logger.debug("No se puede acceder al recurso, por estar en modo restrictivo.");
            return this.permisosNoValidos(((HttpServletRequestWrapper)actionInvocation.getInvocationContext().getContextMap().get("com.opensymphony.xwork2.dispatcher.HttpServletRequest")));
        } else {
            return actionInvocation.invoke();
        }
    }

    public void destroy() {
    }

    public void setExtension(String ext){
        this.extStruts=ext;
    }

    public String getExtension(){
        return this.extStruts;
    }

    /**
     * Prepara todo el entorno para que trabaje el validador.
     */
    protected void cargaEntorno(ActionInvocation actionInvocation) throws Exception{
        //configuro los home
        this.homeWeb=((HttpServletRequestWrapper)actionInvocation.getInvocationContext().getContextMap().get("com.opensymphony.xwork2.dispatcher.HttpServletRequest")).getRealPath("/").replace('\\', '/')+"/";
        this.homeWebInf=homeWeb+"WEB-INF/";
        this.homeClass=homeWebInf+"classes/";

        String home=this.homeWebInf+"config/configAccess.xml";

        //genero el entorno
        this.entorno.put("homeWeb", this.homeWeb);

        //reviso si existe una configuracion personalizada
        if( ((ServletContext)actionInvocation.getInvocationContext().getContextMap().get("com.opensymphony.xwork2.dispatcher.ServletContext")).getInitParameter("configAccessNA")!=null &&
                ((ServletContext)actionInvocation.getInvocationContext().getContextMap().get("com.opensymphony.xwork2.dispatcher.ServletContext")).getInitParameter("configAccessNA").length()>2 ){
            home=((ServletContext)actionInvocation.getInvocationContext().getContextMap().get("com.opensymphony.xwork2.dispatcher.ServletContext")).getInitParameter("configAccessNA");
        }

        //validamos la existencia del archivo de configuracion
        File fTmp=new File(home);
        if( fTmp.exists() ){
            try{
                //parseo el archivo XML
                Document doc=(new SAXBuilder(false)).build(home);
                this.excs = ValidatorConfigurator.obtieneExcepciones(doc);
                this.oriHome=ValidatorConfigurator.obtieneHome(doc);
                //adecua el home
                if( this.oriHome.indexOf('.'+this.extStruts)>0 ){
                    this.home=this.oriHome.substring(0, this.oriHome.indexOf('.'));
                }
                else{
                    this.home=this.oriHome;
                }
                this.oriRedir=ValidatorConfigurator.obtieneRedirecionamiento(doc);
                //adecua el redir
                if( this.oriRedir.indexOf('.'+this.extStruts)>0 ){
                    this.redir=this.oriRedir.substring(0, this.oriRedir.indexOf('.'));
                }
                else{
                    this.redir=this.oriRedir;
                }
                this.mens=ValidatorConfigurator.obtieneMensajeSession(doc);
                this.mensPer=ValidatorConfigurator.obtieneMensajePermiso(doc);
                this.val=ValidatorConfigurator.obtieneValidador(doc, this.entorno);

                StringBuffer sb=new StringBuffer("[");
                for(int i=0; i<this.excs.size(); i++){
                    if( i>0 ){
                        sb.append(", ");
                    }
                    sb.append((this.excs.get(i).getTipo()==TypeException.PAGE? "P": "R")).append(":").append(this.excs.get(i).getRecurso());
                }
                sb.append("]");
                logger.debug("Excepciones: "+sb);
                logger.debug("Redicionamiento: "+this.redir);
                logger.debug("Home: "+this.home);
                logger.debug("Mensaje: "+this.mens);
                logger.debug("Mensaje 2: "+this.mensPer);
                logger.debug("Validador:"+this.val.getClass());
                logger.debug("Inicia la validación de sesion.");
            }
            catch(Exception ex){
                logger.error("Error al cargar el archivo de configuración de validator '"+home+"'.", ex);
            }
        }
        else{
            throw new RuntimeException("No se ha definido la configuración para el Validador de Sesión.");
        }
    }

    /**
     * Redireciona el proceso en caso de no ser valida la sesion.
     * @param request Resquest de la peticion web
     * @return Forward
     * @throws java.lang.Exception
     */
    protected String sessionCaducada(ServletRequest request){
        request.setAttribute(AccessControllerPublisher.CLAVE_MENSAJE, this.mens);

        logger.debug("Session caducada, redirecionando ...");

        return this.redir;
    }

    protected String permisosNoValidos(ServletRequest request){
        request.setAttribute(AccessControllerPublisher.CLAVE_MENSAJE, this.mensPer);

        logger.debug("Acceso no autorizado, redirecionando ...");

        return this.home;
    }
}
