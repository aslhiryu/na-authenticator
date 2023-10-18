package neoAtlantis.utils.accessController.resourceAccessAllower;

import java.io.*;
import java.util.*;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neoAtlantis.utils.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.accessController.resourceAccessAllower.interfaces.AccessEstatus;
import neoAtlantis.utils.accessController.resourceAccessAllower.interfaces.DenialType;
import neoAtlantis.utils.accessController.resourceAccessAllower.interfaces.FilterException;
import neoAtlantis.utils.accessController.resourceAccessAllower.interfaces.ResourceAccessAllower;
import neoAtlantis.utils.accessController.resourceAccessAllower.utils.ValidatorConfigurator;
import neoAtlantis.utils.accessController.web.listeners.AccessControllerPublisher;
import neoAtlantis.utils.accessController.web.listeners.BlockerSessionListener;
import neoAtlantis.utils.apps.web.listeners.SessionListener;
import neoAtlantis.utils.apps.web.objects.UserRequest;
import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * Validador de Acceso a Recursos operado a traves de un XML, cual debe apegarse 
 * a la siguiente estructura:
 * <br>
 * <pre>
 * &lt;pagesAccess&gt;
 *   &lt;directory name="<i>nombre_del_directorio</i>"&gt;
 *     &lt;roles&gt;
 *       &lt;rol&gt;<i>rol_permitido_para_el_directorio</i>&lt;/rol&gt;
 *     &lt;/roles&gt;
 *     &lt;permissions&gt;
 *       &lt;permission&gt;<i>permiso_permitido_para_el_directorio</i>&lt;/permission&gt;
 *     &lt;/permissions&gt;
 *   &lt;/directory&gt;
 *   &lt;page name="<i>nombre_de_la_p&aacute;gina</i>"&gt;
 *     &lt;roles&gt;
 *       &lt;rol&gt;<i>rol_permitido_para_la_p&aacute;gina</i>&lt;/rol&gt;
 *     &lt;/roles&gt;
 *     &lt;permissions&gt;
 *       &lt;permission&gt;<i>permiso_permitido_para_la p&aacute;gina</i>&lt;/permission&gt;
 *     &lt;/permissions&gt;
 *   &lt;/page&gt;
 *   &lt;resource name="<i>recurso</i>"&gt;
 *     &lt;roles&gt;
 *       &lt;rol&gt;<i>rol_permitido_para_el_recurso</i>&lt;/rol&gt;
 *     &lt;/roles&gt;
 *     &lt;permissions&gt;
 *       &lt;permission&gt;<i>permiso_permitido_para_el_recurso</i>&lt;/permission&gt;
 *     &lt;/permissions&gt;
 *   &lt;/resource&gt;
 * &lt;/pagesAccess&gt;
 * </pre>
 * Donde se pueden agregar tantos elementos de <i>directory</i>, <i>page</i> y <i>
 * resource</i> sean necesarios.<br><br>
 * Por default a los recursos de <i>directory</i> y <i>page</i> se les incorpora 
 * una / al inicio, para denotar busque el elemento a partir del contexto de la 
 * aplicaci&oacute;n.<br><br>
 * Por default a los recursos de <i>resource</i> se les incorpora un * al inicio, 
 * para denotar busque todas las coincidencias del elemento.<br><br>
 * Se pueden incorporar tantos elementos <i>rol</i> y <i>permission</i> sean necesarios,
 * en el momento que el validador encuentre al menos un rol y permiso asignado al 
 * usuario permitir&aacute; el acceso al recurso.<br><br>
 * Se pueden utilizar de comodin el rol <b>USUARIO</b> y el permiso <b>LOGEADO</b>
 * dado que estos valores los asigna el {@link neoAtlantis.utilidades.accessController.AccessController Control de Autenticaci&oacute;n} 
 * a todos los usuarios al momento de iniciar sesi&oacute;n.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.0
 */
public class XmlSessionResourceAccess extends ResourceAccessAllower implements Filter {
    private static final Logger DEBUGER = Logger.getLogger(XmlSessionResourceAccess.class);

    protected boolean configurado=false;
    
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

    
    
    
    // Contructores ------------------------------------------------------------
    
    public XmlSessionResourceAccess(String xml) throws WayConfigurationException{
        this(false, xml);
    }
    
    public XmlSessionResourceAccess(boolean restrictivo, String xml) throws WayConfigurationException{
        super(restrictivo);

        try{
            this.createEnvironment(new FileInputStream(xml));
        }
        catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }
    
    
    
    // Metodos publicos---------------------------------------------------------
    
    /**
     * Valida el acceso a un recurso.
     * @param user Usuario que intenta acceder al recurso
     * @param recurso Recurso a validar
     * @return Estus del acceso
     */
    @Override
    public AccessEstatus validateAccess(User user, String recurso) {
        return AccessEstatus.VALIDATE;
    }

    /**
     * M&eacute;todo que se ejecuta al generar el filtro. Durante el cual se realiza
     * la configuraci&oacute;n de las reglas de acceso a recursos.
     * @param filterConfig
     * @throws ServletException 
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    /**
     * M&eacute;todo que se ejecuta cada vez que se solicita un recurso. Durante 
     * este realiza la validación de acceso y en caso de no contar con los privilegios 
     * suficiontes redireciona al recurso mapeado en <b>home</b>. De igual manera
     * valida la existencia del usuario y en caso de no existir redirecciona al 
     * recurso redir.
     * @param request Petici´&oacute;n de la p&aacute;gina
     * @param response Respuesta de la  p&aacute;gina
     * @param chain Flujo de seguimiento del filtro
     * @throws IOException
     * @throws ServletException 
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        UserRequest peticion;
        DEBUGER.debug("Valida el acceso al recurso.");
        
        BlockerSessionListener.endInactiveSessions((HttpServletRequest)request, -1);
        
        DenialType validacion=ValidatorConfigurator.validateResourceAccess(this.excs, this.redir, this.home, this, (HttpServletRequest)request, (HttpServletResponse)response);

        if (validacion==DenialType.NOT_CONNECTED) {
            this.sessionExpired(request, response);
        }
        else {
            //valido si existe una pagina en cookie para redirecionar
            peticion=SessionListener.getLastRequest((HttpServletRequest)request);
            if( peticion!=null ){
                SessionListener.clearLastRequest((HttpServletRequest)request);
                if( !peticion.isPost() ){
                    DEBUGER.debug("Existe la ultima visita, direciona hacia esta");
                    this.goToLastAccess(peticion, request, response);
                }
                else{
                    chain.doFilter(request, response);
                }
            }
            else{
                chain.doFilter(request, response);
            }
        }
    }

    /**
     * M&eacute;todo que se ejecuta al destruir el filtro.
     */
    @Override
    public void destroy() {
        this.filterConfig = null;
    }





    //Metodos protegidos--------------------------------------------------------

    /**
     * Prepara todo el entorno para que trabaje el validador.
     * @param xml Configuración del ReosurceAccess
     * @throws java.lang.Exception
     */
    protected void createEnvironment(InputStream xml) throws Exception{
        //si no se ha configurado realizo la misma
        if(!configurado){
            try{
                //parseo el archivo XML
                Document doc=(new SAXBuilder(false)).build(xml);
                this.excs = ValidatorConfigurator.getExceptions(doc.getRootElement());
                this.redir=ValidatorConfigurator.getRedirect(doc.getRootElement());
                this.home=ValidatorConfigurator.getHome(doc.getRootElement());
                this.mens=ValidatorConfigurator.getSessionMessage(doc.getRootElement());
                this.mensPer=ValidatorConfigurator.getPermissionMessage(doc.getRootElement());
    //            this.pingTime=ValidatorConfigurator.getPingTime(doc);

                DEBUGER.debug("Validador configurado como restrictivo: "+this.isRestrictive());
                //DEBUGER.debug("Ping: "+this.tiempoPing);
            }
            catch(Exception ex){
                DEBUGER.error("Error al cargar el archivo de configuración de validator '"+home+"'.", ex);
            }
            
            
            this.configurado=true;
        }                
    }
    
    /**
     * Redireciona al ultimo recurso que se quedo pendiente
     * @param access Ultimo acceso registrado
     * @param request Resquest de la peticion web
     * @param response  Response de la peticion web
     */
    protected void goToLastAccess(UserRequest access, ServletRequest request, ServletResponse response){
        try{
            RequestDispatcher dispatcher;
            DEBUGER.debug("Redirecciono al ultimo acceso entontrado"+access.getUrl()+"...");

            dispatcher = this.filterConfig.getServletContext().getRequestDispatcher(access.getUrl());
            dispatcher.forward(request, response);
        }
        catch(Exception ex){
            DEBUGER.error("No se logro redireccionar a la ultima solicitud.", ex);
        }
    }
    
    /**
     * Redireciona al recurso vinculado a <b>home</b> dado que la permisos
     * del usuario no son suficientes.
     * @param request Resquest de la peticion web
     * @param response  Response de la peticion web
     */
    protected void accessDenied(ServletRequest request, ServletResponse response){
        try{
            RequestDispatcher dispatcher;
            request.setAttribute(AccessControllerPublisher.MESSAGE_ERROR_KEY, this.mensPer);

            DEBUGER.debug("Acceso no autorizado, redirecionando a /"+this.home+"...");

            dispatcher = this.filterConfig.getServletContext().getRequestDispatcher("/"+this.home);
            dispatcher.forward(request, response);
        }
        catch(Exception ex){
            DEBUGER.error("No se logro redireccionar la sesion sin permisos.", ex);
        }
    }

    /**
     * Redireciona al recurso vinculado a <b>redir</b> dado que la sesi&oacute;n 
     * del usuario no es valida.
     * @param request Resquest de la peticion web
     * @param response Response de la peticion web
     */
    protected void sessionExpired(ServletRequest request, ServletResponse response){
        try{
            RequestDispatcher dispatcher;
            request.setAttribute(AccessControllerPublisher.MESSAGE_ERROR_KEY, this.mens);

            DEBUGER.debug("Session caducada, redirecionando a /"+this.redir+"...");

            dispatcher = this.filterConfig.getServletContext().getRequestDispatcher("/"+this.redir);
            if( dispatcher!=null ){
                dispatcher.forward(request, response);
            }
            else{
                ((HttpServletResponse)response).sendRedirect( ((HttpServletRequest)request).getContextPath()+this.redir );
            }
        }
        catch(Exception ex){
            DEBUGER.error("No se logro redireccionar la sesion caduca.", ex);
        }
    }

    /**
     * Redireciona al recurso vinculado a <b>home</b> dado que existe una sesi&oacute;n 
     * activa del usuario.
     * @param request Resquest de la peticion web
     * @param response Response de la peticion web
     */
    protected void existActiveSession(ServletRequest request, ServletResponse response){
        try{
            RequestDispatcher dispatcher;
            DEBUGER.debug("Existe sesión activa, redirecionando a /"+this.home+"...");

            dispatcher = this.filterConfig.getServletContext().getRequestDispatcher("/"+this.home);
            dispatcher.forward(request, response);
        }
        catch(Exception ex){
            DEBUGER.error("No se logro redireccionar la sesion activa.", ex);
        }
    }

    /**
     * Redireciona al recurso solicitado originalmente
     * activa del usuario.
     * @param request Resquest de la peticion web
     * @param response Response de la peticion web
     */
    protected void existLastVisit(ServletRequest request, ServletResponse response, String url){
        try{
            RequestDispatcher dispatcher;
            DEBUGER.debug("Existe pagina de ultima visita, redirecionando a /"+url+"...");

            dispatcher = this.filterConfig.getServletContext().getRequestDispatcher("/"+url);
            dispatcher.forward(request, response);
        }
        catch(Exception ex){
            DEBUGER.error("No se logro redireccionar a la ultima visita.", ex);
        }
    }

}
