package neoAtlantis.utilidades.accessController.utils;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay;
import neoAtlantis.utilidades.accessController.resourcesFilter.DenialType;
import neoAtlantis.utilidades.accessController.resourcesFilter.FilterException;
import neoAtlantis.utilidades.accessController.resourcesFilter.TypeException;
import neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower;
import neoAtlantis.utilidades.configFiles.ClassGenerator;
import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * Filtro que se utiliza para validar el acceso a los recursos del sistema. Toma 
 * como base lo configurado en el {@link neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower Validador de Acceso a Recursos}
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.3
 */
public class SimpleRulesValidatorFilter implements Filter {
    private static final Logger logger = Logger.getLogger(SimpleRulesValidatorFilter.class);
    /**
     * Nombre clave del validador.
     */
    public static String CLAVE_VAL="na.util.access.ResourceValidator";

    protected FilterConfig filterConfig;
    /**
     * Lista de recursos a omitirse en la validaci&oacute;n.
     */
    protected ArrayList<FilterException> excs = null;
    /**
     * Recurso al que se redireciona en caso de ya no existir una sesi&oacute;n 
     * activa del usuario.
     */
    protected String redir;
    /**
     * Recurso al que se redireciona en caso de no tener permisos para acceder a 
     * alg&uacute;n recurso.
     */
    protected String home;
    /**
     * Mensaje al que se hace referencia en caso de ya no existir una sesi&oacute;n 
     * activa del usuario.
     */
    protected String mens;
    /**
     * Mensaje al que se hace referencia en caso de no tener permisos para acceder a 
     * alg&uacute;n recurso.
     */
    protected String mensPer;
    /**
     * {@link neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower Validador de Acceso a Recursos}
     */
    protected ResourceAccessAllower val;
//    protected User user;
    /**
     * Tiempo en que se realiza el ping, si esta activado el m&eacuet;todo de  mantenimiento 
     * de sesi&oacute;n mediante ping.
     */
    protected long tiempoPing=BlockerWay.SEGUNDO_EN_MILIS*10;

    private String homeWeb;
    private String homeWebInf;
    private HashMap<String, Object> entorno=new HashMap<String, Object>();

    /**
     * M&eacute;todo que se ejecuta al generar el filtro. Durante el cual se realiza
     * la configuraci&oacute;n de las reglas de acceso a recursos.
     * @param filterConfig
     * @throws ServletException 
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;

        try{
            this.cargaEntorno();
        }
        catch(Exception ex){
            throw new ServletException(ex);
        }

        System.out.println("Inicializando Validador de Session");
    }

    /**
     * M&eacute;todo que se ejecuta cada vez que se solicita un recurso. Durante 
     * este realiza la validación de acceso y en caso de no contar con los privilegios 
     * suficiontes redireciona al recurso mapeado en <b>home</b>.
     * @param request Petici´&oacute;n de la p&aacute;gina
     * @param response Respuesta de la  p&aacute;gina
     * @param chain Flujo de seguimiento del filtro
     * @throws IOException
     * @throws ServletException 
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //finalizaSesionesInactivas((HttpServletRequest)request);
        
        DenialType validacion=ValidatorConfigurator.validaAccesoRecuros(this.excs, this.redir, this.home, this.val, (HttpServletRequest)request, (HttpServletResponse)response);

        /*if (validacion==DenialType.NO_CONECTADO) {
            this.sessionCaducada(request, response);
        }*/
        if(validacion==DenialType.NO_PERMITIDO) {
            this.permisosNoValidos(request, response);
        }
        /*else if(validacion==DenialType.PERMITIDO_LOGIN) {
            this.redireccionLogin(request, response);
        }*/
        else if(validacion==DenialType.RESTRICTIVO) {
            this.permisosNoValidos(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * M&eacute;todo que se ejecuta al destruir el filtro.
     */
    public void destroy() {
        this.filterConfig = null;
    }

    //-------------------------------------------------------------------------

    /**
     * Prepara todo el entorno para que trabaje el validador.
     */
    protected void cargaEntorno() throws Exception{
        //configuro los home

        this.homeWeb=this.filterConfig.getServletContext().getRealPath("/").replace('\\', '/')+"/";
        this.homeWebInf=homeWeb+"WEB-INF/";
        Properties com=new Properties();
        com.setProperty("homeWeb", homeWeb);
        com.setProperty("homeWebInf", homeWebInf);
        com.setProperty("homeClass", homeWebInf+"classes/");

        String home=this.homeWebInf+"config/configAccess.xml";

        //reviso si existe una configuracion personalizada
        if( this.filterConfig.getServletContext().getInitParameter("configAccessNA")!=null && this.filterConfig.getServletContext().getInitParameter("configAccessNA").length()>2 ){
            home=ClassGenerator.parseaComodinesConfig(this.filterConfig.getServletContext().getInitParameter("configAccessNA"), com);
        }

        //genero el entorno
        this.entorno.put("appContext",this.filterConfig.getServletContext());
        this.entorno.put("homeWeb", this.homeWeb);

        logger.info("Configuracion cargada de :"+home);
        //validamos la existencia del archivo de configuracion
        File fTmp=new File(home);
        if( fTmp.exists() ){
            try{
                //parseo el archivo XML
                Document doc=(new SAXBuilder(false)).build(home);
                this.excs = ValidatorConfigurator.obtieneExcepciones(doc);
                this.redir=ValidatorConfigurator.obtieneRedirecionamiento(doc);
                this.home=ValidatorConfigurator.obtieneHome(doc);
                this.mens=ValidatorConfigurator.obtieneMensajeSession(doc);
                this.mensPer=ValidatorConfigurator.obtieneMensajePermiso(doc);
                this.val=ValidatorConfigurator.obtieneValidador(doc, this.entorno);
                this.tiempoPing=ValidatorConfigurator.obtieneTiempoPing(doc);

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
                logger.debug("Restrictivo:"+this.val.isRestrictivo());
                logger.debug("Inicia la validación de sesion.");
                logger.debug("Ping: "+this.tiempoPing);
            }
            catch(Exception ex){
                logger.error("Error al cargar el archivo de configuración de validator '"+home+"'.", ex);
            }
        }
        else{
            throw new RuntimeException("No se ha definido la configuración para el Validador de Sesión '"+home+"'.");
        }
    }


    /**
     * Redireciona al recurso vinculado a <b>home</b> dado que la permisos
     * del usuario no son suficientes.
     * @param request Resquest de la peticion web
     * @param response  Response de la peticion web
     */
    protected void permisosNoValidos(ServletRequest request, ServletResponse response){
        try{
            RequestDispatcher dispatcher;
            request.setAttribute(AccessControllerPublisher.CLAVE_MENSAJE, this.mensPer);

            logger.debug("Acceso no autorizado, redirecionando a /"+this.home+"...");

            dispatcher = this.filterConfig.getServletContext().getRequestDispatcher("/"+this.home);
            dispatcher.forward(request, response);
        }
        catch(Exception ex){
            logger.error("No se logro redireccionar la sesion sin permisos.", ex);
        }
    }

    
}
