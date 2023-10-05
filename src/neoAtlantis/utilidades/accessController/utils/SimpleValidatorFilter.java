package neoAtlantis.utilidades.accessController.utils;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import neoAtlantis.utilidades.accessController.AccessController;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.resourcesFilter.DenialType;
import neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower;
import org.apache.log4j.Logger;

/**
 * Filtro que se utiliza para validar el acceso a los recursos del sistema y la 
 * vigencia del usuario. Toma como base lo configurado en el {@link neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower Validador de Acceso a Recursos}
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.3
 */
public class SimpleValidatorFilter extends SimpleRulesValidatorFilter {
    private static final Logger logger = Logger.getLogger(SimpleValidatorFilter.class);

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
        finalizaSesionesInactivas((HttpServletRequest)request);
        
        DenialType validacion=ValidatorConfigurator.validaAccesoRecuros(this.excs, this.redir, this.home, this.val, (HttpServletRequest)request, (HttpServletResponse)response);

        if (validacion==DenialType.NO_CONECTADO) {
            this.sessionCaducada(request, response);
        }
        else if(validacion==DenialType.NO_PERMITIDO) {
            this.permisosNoValidos(request, response);
        }
        else if(validacion==DenialType.PERMITIDO_LOGIN) {
            this.redireccionLogin(request, response);
        }
        else if(validacion==DenialType.RESTRICTIVO) {
            this.permisosNoValidos(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Redireciona al recurso vinculado a <b>redir</b> dado que la sesi&oacute;n 
     * del usuario no es valida.
     * @param request Resquest de la peticion web
     * @param response Response de la peticion web
     * @throws java.lang.Exception
     */
    protected void sessionCaducada(ServletRequest request, ServletResponse response){
        try{
            RequestDispatcher dispatcher;
            request.setAttribute(AccessControllerPublisher.CLAVE_MENSAJE, this.mens);

            logger.debug("Session caducada, redirecionando a /"+this.redir+"...");

            dispatcher = this.filterConfig.getServletContext().getRequestDispatcher("/"+this.redir);
            if( dispatcher!=null ){
                dispatcher.forward(request, response);
            }
            else{
                ((HttpServletResponse)response).sendRedirect( ((HttpServletRequest)request).getContextPath()+this.redir );
            }
        }
        catch(Exception ex){
            logger.error("No se logro redireccionar la sesion caduca.", ex);
        }
    }

    /**
     * Redireciona al recurso vinculado a <b>home</b> dado que existe una sesi&oacute;n 
     * activa del usuario.
     * @param request Resquest de la peticion web
     * @param response Response de la peticion web
     */
    protected void redireccionLogin(ServletRequest request, ServletResponse response){
        try{
            RequestDispatcher dispatcher;

            logger.debug("Existe sesión activa, redirecionando a /"+this.home+"...");

            dispatcher = this.filterConfig.getServletContext().getRequestDispatcher("/"+this.home);
            dispatcher.forward(request, response);
        }
        catch(Exception ex){
            logger.error("No se logro redireccionar la sesion activa.", ex);
        }
    }

    /**
     * Revisa si existe sesiones de usuario que ya no estan activas y las elimina.
     * @param request  Resquest de la peticion web
     */
    protected void finalizaSesionesInactivas(HttpServletRequest request){
        AccessController ctrl=AccessController.getInstance(); //(AccessController)request.getSession().getAttribute(AccessControllerPublisher.CLAVE_CTRL);
        
        if( ctrl!=null ){
            //valida termino por tiempo de vida
            try{
                for(User uTmp: ctrl.revisaSesionesInactivas()){
                    ctrl.finaliza(uTmp);
//                    SimpleAuthenticationServlet.terminaSesion((HttpSession)uTmp.getSesion().getAtributo(SimpleAuthenticationServlet.HTTP_SESSION));
                    SimpleAuthenticationServlet.terminaSesion(uTmp.getSesion().getHttpSession());
                    logger.debug("Destruyo la sesion para "+uTmp.getUser()+" por tiempo de vida");
                }
            }
            catch(Exception ex){
                logger.error("No se logro recuperar las sesiones inactivas", ex);
            }

            //valida termino por timeout en ping
            if( this.tiempoPing>-1 && ctrl!=null ){
                try{
                     for(User uTmp: ctrl.recuperaSesionesActivas()){
                        logger.debug("Valida sesion para "+uTmp.getUser()+" : "+uTmp.getSesion().getActividad());
                        
                        if( uTmp.getSesion().getActividad()!=null && (new Date()).getTime()>uTmp.getSesion().getActividad().getTime()+this.tiempoPing ){
                            ctrl.finaliza(uTmp);
//                            SimpleAuthenticationServlet.terminaSesion((HttpSession)uTmp.getSesion().getAtributo(SimpleAuthenticationServlet.HTTP_SESSION));
                            SimpleAuthenticationServlet.terminaSesion(uTmp.getSesion().getHttpSession());
                            logger.debug("Destruyo la sesion para "+uTmp.getUser()+" por ping excedido");
                        }
                    }
                }catch(Exception ex){
                    logger.error("No se logro recuperar las sesiones activas.", ex);
                }
            }
        }
    }
    
}
