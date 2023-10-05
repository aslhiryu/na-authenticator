package neoAtlantis.utilidades.accessController.utils;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import neoAtlantis.utilidades.accessController.allower.SsoAllower;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.profiler.SsoProfiler;
import neoAtlantis.utilidades.sso.clienteWS.SsoService;
import neoAtlantis.utilidades.sso.clienteWS.SsoServiceService;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class SsoValidatorFilter implements Filter {
    static final Logger logger = Logger.getLogger(SsoValidatorFilter.class);

    protected FilterConfig filterConfig;

    private String url;
    private String app;
    private SsoService port;
    private HashMap<String, String> params;

    public void init(FilterConfig fc) throws ServletException {
        this.filterConfig = fc;

        //recupero la ruta del servicio del SSO
        this.url=fc.getInitParameter("serviceSSO");
        if( this.url==null || this.url.isEmpty() ){
            throw new ServletException("No se cuenta con la ruta al servicio del SSO.");
        }
        logger.debug("Inicia el servicio del SSO en la ruta '"+this.url+"'.");

        //recupero  la aplication
        this.app=fc.getInitParameter("application");
        if( this.app==null || this.app.isEmpty() ){
            throw new ServletException("No se cuenta con la clave del aplicativo a conectarse al servicio del SSO.");
        }
        logger.debug("Para el aplicativo '"+this.app+"'.");
    } 

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        User u;

        logger.debug("Intento validar el acceso a '"+((HttpServletRequest)request).getServletPath()+"';");
        this.preparaEnlace();

        //valido que existe un usuario en sesion
        u=(User)((HttpServletRequest)request).getSession().getAttribute(AccessControllerPublisher.CLAVE_USER);
        //si aun no existe usuario se debe de validar la conexion
        if( u==null){
            //realizo la carga de parametros
            this.parseaParams();

            logger.debug("No existe usuario en sesion valido la conexion.");
            u=this.recuperaUsuario(request.getRemoteAddr(), request.getRemoteHost());
            //si no existe conexion redirigo a otra pagina
            if( u==null ){
                logger.debug("No existe conexion del usuario, redirecciono al login en '"+this.params.get("urlSSO")+"/login.sso'.");
                ((HttpServletResponse)response).sendRedirect(this.params.get("urlSSO")+"/login.sso?system="+this.app);
            }
            //existe la conexion del usuario
            else{
                logger.debug("Existe conexion del usuario, lo cargo en sesion.");
                try{
                    SsoAllower all=new SsoAllower(this.filterConfig.getServletContext().getRealPath("/")+"/WEB-INF/config/dataBase.xml");
                    List perms=all.obtienePermisos(u, this.app);
                    logger.debug("Permisos recuperados: "+perms.size());
                }
                catch(Exception ex){
                    logger.error("No se logro recuperar los permisos del usuario", ex);
                }
                try{
                    SsoProfiler prof=new SsoProfiler(this.filterConfig.getServletContext().getRealPath("/")+"/WEB-INF/config/dataBase.xml");
                    List rols=prof.obtieneRoles(u, this.app);
                    logger.debug("Roles recuperados: "+rols.size());
                }
                catch(Exception ex){
                    logger.error("No se logro recuperar los roles del usuario", ex);
                }
                ((HttpServletRequest)request).getSession().setAttribute(AccessControllerPublisher.CLAVE_USER, u);
            }
        }
        if( u!=null ){
            logger.debug("Existe usuario en sesion valido el acceso al recurso.");
            chain.doFilter(request, response);
        }        
    }

    public void destroy() {
        this.filterConfig = null;
    }

    //-----------------------------------

    private void preparaEnlace() throws ServletException{
        if( this.port==null ){
            //genero la conexion al WS del SSO
            try{
                SsoServiceService s=new SsoServiceService(new URL(SsoServiceService.class.getResource("."), this.url+"?wsdl"), new QName("http://services.web.sso.entornos.neoAtlantis/", "SsoServiceService"));
                this.port=s.getSsoServicePort();
            }
            catch(Exception ex){
                throw new ServletException("No se logro conectar al servicio del SSO.");
            }
            //reviso que sea un sistema valido
            try{
                if( !this.port.validaSistema(this.app) ){
                    throw new ServletException("El sistema no esta autorizado para utilizar el servicio del SSO.");
                }
            }
            catch(Exception ex){
                throw new ServletException("No se logro autorizar el sistema en el servicio del SSO.");
            }
        }
    }

    private User  recuperaUsuario(String ip, String terminal){
        User u=null;
        
        logger.debug("Intento validar la sesion de "+ip+" - "+terminal+".");

        String xml=this.port.validaConexion(ip, terminal, this.app);
        logger.debug("Recupero el usuario: "+xml);
        if(xml!=null){
            try{
                return User.parseaXml(xml);
            }
            catch(Exception ex){
                logger.error("No se logro recuperar el usuario. ", ex);
                return null;
            }
        }

        return u;
    }

    private void parseaParams(){
        HashMap<String, String> p=new HashMap<String, String>();
        Element r, e;

        logger.debug("Intento recuperar los parametros del SSO.");
        String params=this.port.recuperaParametrosSso();

        try{
            Document doc=(new SAXBuilder(false)).build(new StringReader(params));
            r=doc.getRootElement();
            List<Element>  l=r.getChildren("param");
            for(Element eTmp: l){
                p.put(eTmp.getAttributeValue("name"), eTmp.getText());
            }
        }
        catch(Exception ex){

        }

        logger.debug("Parametros recuperados del SSO: "+p);

        this.params=p;
    }

}
