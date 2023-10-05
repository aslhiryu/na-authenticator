package neoAtlantis.utilidades.ctrlAcceso.utils;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
import neoAtlantis.utilidades.ctrlAcceso.ControlAcceso;
import neoAtlantis.utilidades.ctrlAcceso.Recurso;
import neoAtlantis.utilidades.ctrlAcceso.Usuario;
import neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.interfaces.RecusoAutenticador;
import neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.interfaces.ResultadoAutenticacion;
import org.jdom.input.SAXBuilder;

/**
 * Validador de session Web que valida la existencia del usuario en sesi&oacute;n y de permisos para acceder a los recursos.
 * <br><br>
 * Para realizar la validac&oacute;n revisa que en sesi&oacute;n exista una instancia de <i>neoAtlantis.utilidades.ctrlAcceso.Usuario</i>
 * con el nombre de 'usuario' y una instancia de <i>neoAtlantis.utilidades.ctrlAcceso.ControlAcceso</i> con el nomnre de 'autenticacion'.
 * En caso de no existe redirecionar&aacute; hacia donde se tenga configurado.
 * <br><br>
 * Para realizar la validac&oacute;n de permisos del recurso se apoya en un archivo xml que tiene la siguiente estructura:
 * <pre>
 * <b>&lt;recursos&gt;</b>
 *     <b>&lt;recurso nombre="nombre_del_recurso"&gt;</b>
 *         <b>&lt;permisos&gt;</b>
 *             <b>&lt;permiso&gt;</b><i>permiso_permitido_para_el_recurso</i><b>&lt;/permiso&gt;</b>
 *         <b>&lt;/permisos&gt;</b>
 *         <b>&lt;roles&gt;</b>
 *             <b>&lt;rol&gt;</b><i>rol_permitido_para_el_recurso</i><b>&lt;/rol&gt;</b>
 *         <b>&lt;/roles&gt;</b>
 *     <b>&lt;/recurso&gt;</b>
 * <b>&lt;/recursos&gt;</b>
 * </pre>
 * En esta rchivo se mapean los roles y permisos que se deben de tener para poder acceder al recurso.
 * En caso de no existe redirecionar&aacute; hacia donde se tenga configurado.
 * <br><br>
 * la designacion de roles y permisos se realiza (por default) cargnado los roles y permisos definidos para cada recurso
 * en el archivo XML adem&aacute;s de asignarles el rol '<i>USUARIO</i>' y el permiso '<i>LOGEADO</i> a todos estos y los que no
 * esten definidos en el archivo. O de manera restrictiva en donde forsozamente deben de tener todos los recursos por lo menos un
 * rol y un permiso definido para poder acceder a este; y en caso de no existe alguno y tratar de acceder al recurso, se enviar&aacute;
 * un mensaje de error en la salida estandar del servidor de aplicaciones.
 * <br><br>
 * Para lograr la validadci&oacute;n el filtro comprobar&aacute; que la clase alojada en session como
 * 'usuario' pertenesca a alguno de los roles definidos para el recurso y que ademas dicho usuario tenga asignado por lo menos uno de
 * los permisos definidos para el recurso, para tal .
 * <br><br>
 * Para poder utilizar este validador, se debe de configurar el <i>Filter</i> en el '<i>web.xml</i>' de la aplicaci&oacute;n web.
 * Las lineas a agregar al archivo serian las siguientes:
 * <pre>
 * <b>&lt;filter&gt;</b>
 *     <b>&lt;filter-name>Filtro de sesion<b>&lt;/filter-name&gt;</b>
 *     <b>&lt;filter-class>neoAtlantis.utilidades.ctrlAcceso.utils.ValidadorSesionSimple<b>&lt;/filter-class&gt;</b>
 *     <b>&lt;init-param&gt;</b>
 *         <b>&lt;param-name&gt;</b>excepciones<b>&lt;/param-name&gt;</b>
 *         <b>&lt;param-value&gt;</b><i>paginas_que_no_ser&aacute;n_validadas(estas deben de estar separadas por coma)</i><b>&lt;/param-value&gt;</b>
 *     <b>&lt;/init-param&gt;</b>
 *     <b>&lt;init-param&gt;</b>
 *         <b>&lt;param-name&gt;</b>redireccion<b>&lt;/param-name&gt;</b>
 *         <b>&lt;param-value&gt;</b><i>pagina_a_la_que_se_redereccionar&aacute;_si_se_termino_la_sesi&oacute;n(default index.html)</i><b>&lt;/param-value&gt;</b>
 *     <b>&lt;/init-param&gt;</b>
 *     <b>&lt;init-param&gt;</b>
 *         <b>&lt;param-name&gt;</b>inicio<b>&lt;/param-name&gt;</b>
 *         <b>&lt;param-value&gt;</b><i>pagina_a_la_que_se_redereccionar&aacute;_si_no_se_tienen_permisos(default index.html)</i><b>&lt;/param-value&gt;</b>
 *     <b>&lt;/init-param&gt;</b>
 *     <b>&lt;init-param&gt;</b>
 *         <b>&lt;param-name&gt;</b>errorNombre<b>&lt;/param-name&gt;</b>
 *         <b>&lt;param-value&gt;</b><i>nombre_del_atributo_de_request_donde_se_alojara_el_mensaje_de_error(default errorGeneral)</i><b>&lt;/param-value&gt;</b>
 *     <b>&lt;/init-param&gt;</b>
 *     <b>&lt;init-param&gt;</b>
 *         <b>&lt;param-name&gt;</b>errorTexto<b>&lt;/param-name&gt;</b>
 *         <b>&lt;param-value&gt;</b><i>mensaje_de_error_para_cuando_no_existe_sesi&oacute;n</i><b>&lt;/param-value&gt;</b>
 *     <b>&lt;/init-param&gt;</b>
 *     <b>&lt;init-param&gt;</b>
 *         <b>&lt;param-name&gt;</b>errorPermiso<b>&lt;/param-name&gt;</b>
 *         <b>&lt;param-value&gt;</b><i>mensaje_de_error_para_cuando_no_se_tienen_permisos</i><b>&lt;/param-value&gt;</b>
 *     <b>&lt;/init-param&gt;</b>
 *     <b>&lt;init-param&gt;</b>
 *         <b>&lt;param-name&gt;</b>mapeoPermisos<b>&lt;/param-name&gt;</b>
 *         <b>&lt;param-value&gt;</b><i>archivo_xml_con_los_permisos_por_recurso</i><b>&lt;/param-value&gt;</b>
 *     <b>&lt;/init-param&gt;</b>
 *     <b>&lt;init-param&gt;</b>
 *         <b>&lt;param-name&gt;</b>restrictivo<b>&lt;/param-name&gt;</b>
 *         <b>&lt;param-value&gt;</b><i>true_si_se_desea_los_permisos_restrictivos(default true)</i><b>&lt;/param-value&gt;</b>
 *     <b>&lt;/init-param&gt;</b>
 *     <b>&lt;init-param&gt;</b>
 *         <b>&lt;param-name&gt;</b>debug<b>&lt;/param-name&gt;</b>
 *         <b>&lt;param-value&gt;</b><i>true_si_se_desea_activar_el_debug(default true)</i><b>&lt;/param-value&gt;</b>
 *     <b>&lt;/init-param&gt;</b>
 * <b>&lt;/filter&gt;</b>
 * <b>&lt;filter-mapping&gt;</b>
 *     <b>&lt;filter-name&gt;</b>Filtro de sesion<b>&lt;/filter-name&gt;</b>
 *     <b>&lt;url-pattern&gt;</b><i>patron_de_los_recursos_a_validar(ej: *.jsp)</i><b>&lt;/url-pattern&gt;</b>
 * <b>&lt;/filter-mapping&gt;</b>
 * </pre>
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class ValidadorSesionPermisos extends ValidadorSesionSimple {

    protected String home;
    protected String errorPermiso;
    protected ArrayList<Recurso> recursos;
    protected boolean restrictivo=true;

    /**
     * Intercepta la llamada a un recurso Web.
     * @param request Request de la petici&oacute;n
     * @param response Response de la petici&oacute;n
     * @param chain Chain del <i>Filter</i>
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean conectado = false;
        boolean valida=this.validaRecurso(((HttpServletRequest)request).getServletPath());
        boolean sinPermisos=true;
        boolean permitida=false;
        Usuario user=null;
        ControlAcceso ctrl=null;
        //RequestDispatcher dispatcher;

        try {
            user=(Usuario)((HttpServletRequest) request).getSession().getAttribute("usuario");
            ctrl=(ControlAcceso)((HttpServletRequest) request).getSession().getAttribute("autenticacion");
            conectado = (user!=null&&ctrl!=null);
        } catch (Exception ex) {
        }

        if( !valida ){
            permitida=true;
            sinPermisos=false;
        }
        else{
            //revisa que el recurso tenga permisos
            boolean[] bTmp=this.validaPermisosRecuso(((HttpServletRequest)request).getServletPath(), user, ctrl);
            sinPermisos=bTmp[0];
            permitida=bTmp[1];
        }
        
        if ( valida && !conectado ) {
            this.sessionCaducada(request, response);
        }
        else if( sinPermisos && conectado ){
            System.out.println("ERROR: "+this.nombreVal+": El recurso no tiene permisos o roles asignados.");
            this.permisosNoValidos(request, response);
        }
        else if( !permitida && conectado ){
            this.permisosNoValidos(request, response);
        }
        else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Obtiene si el validador es de tipo restrictivo.
     * @return true si es restrictivo
     */
    protected boolean obtieneRestrictivo(){
        try {
            if( this.filterConfig.getInitParameter("restrictivo")!=null ){
                return (new Boolean(this.filterConfig.getInitParameter("restrictivo"))).booleanValue();
            }
        } catch (Exception ex) {
        }
        
        return true;
    }

    /**
     * Obtiene la pagina a la que se redireciona en caso de no tener permisos para acceder al recurso.
     * @return Pagina de redirecionamiento
     */
    protected String obtieneHome(){
        try {
            return (this.filterConfig.getInitParameter("inicio") != null ? this.filterConfig.getInitParameter("inicio") : "index.html").trim();
        } catch (Exception ex) {
            return "index.html";
        }
    }

    /**
     * Obtiene el mensaje de error que se mostrar&aacute; al momento de no tener permisos sobre el recurso.
     * @return
     */
    protected String obtieneErrorPermiso(){
        try {
            return (this.filterConfig.getInitParameter("errorPermiso") != null ? this.filterConfig.getInitParameter("errorPermiso") : "Permisos insuficientes para el recurso.").trim();
        } catch (Exception ex) {
            return "Permisos insuficientes para el recurso.";
        }
    }

    /**
     * Obtiene el mapeo de recursos consus respectivos permisos.
     * @return Coleccion de recursos con permisos
     * @throws javax.servlet.ServletException
     */
    protected ArrayList<Recurso> obtieneRecursos() throws ServletException{
        String ruta=null;

        try{
            ruta=this.filterConfig.getServletContext().getRealPath("/")+"/"+this.filterConfig.getInitParameter("mapeoPermisos");
            SAXBuilder builder = new SAXBuilder(false);
            return Recurso.parseaArchivoRecursos(builder.build(ruta));
        }
        catch(Exception ex){
            throw new ServletException("No se logro cargar el mapeo de recursos '"+ruta+"': "+ex.getMessage());
        }
    }


    /**
     * Prepara todo el entorno para que trabaje el validador.
     */
    @Override
    protected void cargaEntorno() throws Exception{
        this.nombreVal="ValidadorSesionPermisos";
        super.cargaEntorno();

        this.restrictivo = this.obtieneRestrictivo();
        this.home = this.obtieneHome();
        this.errorPermiso = this.obtieneErrorPermiso();
        this.recursos = this.obtieneRecursos();

        if( this.debug ){
            System.out.println("INFO: "+this.nombreVal+": inicio -> "+this.home);
            System.out.println("INFO: "+this.nombreVal+": errorPermiso -> "+this.errorPermiso);
            System.out.println("INFO: "+this.nombreVal+": restrictivo -> "+this.restrictivo);
            System.out.println("INFO: "+this.nombreVal+": mapeo -> ("+(this.recursos!=null? this.recursos.size(): -1)+")\n"+this.recursos);
            System.out.println("INFO: "+this.nombreVal+": Inicia la validación de sesion y permisos.");
        }
    }

    /**
     * Revisa si el recurso se debe de validar.
     * @param request Resquest de la peticion web
     * @return true si se debe de validar
     */
    @Override
    protected boolean validaRecurso(String recurso){
        boolean b;

        if( b=super.validaRecurso(recurso) ){
            //revisa si es la pagina de inicio para omitir la revision de permisos
            if( recurso.equals(this.home) ){
                if( this.debug ){
                    System.out.println("INFO: "+this.nombreVal+": Omite el recurso por ser el home.");
                }

                return false;
            }

            return b;
        }
        else{
            return false;
        }
    }

    protected boolean[] validaPermisosRecuso(String recurso, Usuario user, ControlAcceso ctrl){
        boolean rolPer=false;
        ResultadoAutenticacion permisoPer=ResultadoAutenticacion.ACCESO_DENEGADO;

        //revisa que el recurso tenga permisos
        for(int i=0; i<this.recursos.size(); i++){

            if( recurso.equals(this.recursos.get(i).getNombre()) ){
                if( this.debug ){
                    System.out.println("INFO: "+this.nombreVal+": Recurso encontrado.");
                }

                if( this.restrictivo && (this.recursos.get(i).getPermisosPermitidos().length==0 || this.recursos.get(i).getRolesPermitidos().length==0) ){
                    if( this.debug ){
                        System.out.println("INFO: "+this.nombreVal+": El recurso "+this.recursos.get(i).getNombre()+", no tiene definidos permisos.");
                    }
                    //el recurso no tiene permisos por lo que se finaliza
                    break;
                }
                //valida que se tenga el rol y el permiso
                try{
                    rolPer=user.perteneceAlgunRol(this.recursos.get(i).getRolesPermitidos());
                    permisoPer=ctrl.validaAlgunPermiso(this.recursos.get(i).getPermisosPermitidos(), user, this.recursos.get(i).getNombre());
                    if( this.debug ){
                        System.out.println("INFO: "+this.nombreVal+": Permiso: "+permisoPer+", Rol: "+rolPer+".");
                    }
                    if( permisoPer==ResultadoAutenticacion.ACCESO_AUTORIZADO && rolPer ){
                        if( this.debug ){
                            System.out.println("INFO: "+this.nombreVal+": Se cumplen con los permisos para acceder al recurso.");
                        }

                        return new boolean[]{false, true};
                    }
                }
                catch(Exception ex){
                }
            }
        }

        if( this.debug ){
            System.out.println("INFO: "+this.nombreVal+": Omite el recurso por no tener permisos y no ser restrictivo.");
        }
        return new boolean[]{true, !this.restrictivo};
    }

    /**
     * Redireciona el proceso en caso de no ser valida la sesion.
     * @param request Resquest de la peticion web
     * @param response Response de la peticion web
     * @throws java.lang.Exception
     */
    protected void permisosNoValidos(ServletRequest request, ServletResponse response) throws IOException, ServletException{
        RequestDispatcher dispatcher;
        request.setAttribute(this.errorNombre, this.errorPermiso);

        if (this.debug) {
            System.out.println("INFO: " + this.nombreVal + ": Acceso no autorizado, redirecionando ...");
            System.out.println("INFO: " + this.nombreVal + ": " + this.errorNombre + "=" + request.getAttribute(this.errorNombre));
        }

        dispatcher = this.filterConfig.getServletContext().getRequestDispatcher(this.home);
        dispatcher.forward(request, response);
    }
}
