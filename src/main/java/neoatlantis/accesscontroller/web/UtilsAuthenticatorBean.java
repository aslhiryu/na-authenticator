package neoAtlantis.utils.accessController.web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import neoAtlantis.utils.accessController.AccessController;
import neoAtlantis.utils.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utils.accessController.objects.Role;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.accessController.printer.SimpleHtmlLoginPrinter;
import neoAtlantis.utils.accessController.printer.interfaces.AuditPrinter;
import neoAtlantis.utils.accessController.printer.interfaces.LoginPrinter;
import neoAtlantis.utils.accessController.printer.interfaces.RoleAdministratorPrinter;
import neoAtlantis.utils.accessController.printer.interfaces.UserAdministratorPrinter;
import neoAtlantis.utils.accessController.profiler.interfaces.ProfilerWay;
import neoAtlantis.utils.accessController.web.listeners.AccessControllerPublisher;
import neoAtlantis.utils.accessController.web.utils.AuthenticationResourcesLoader;
import org.apache.log4j.Logger;

/**
 * Clase de apoyo que permite el acceso a los objetos del contexto de NA para autenticar
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class UtilsAuthenticatorBean {
    private static final Logger DEBUGER = Logger.getLogger(UtilsAuthenticatorBean.class);    
    
    public static final String OPERATION_PARAM="NA_Operation";
    public static final String ID_PARAM="NA_id";
    public static final String USER_PARAM="NA_user";
    public static final String ROLE_PARAM="NA_role";
    /**
     * Clave para el parametro que alberga el id del administrador que realizo la operacion
     */
    public static final String ID_ADMIN_PARAM="NA_idAdmin";
    /**
     * Clave para el parametro que alberga el nombre del administrador que realizo la operacion
     */
    public static final String NAME_ADMIN_PARAM="NA_nameAdmin";
    
    
    
    
    /**
     * Recupera la estructurta HTML para la auditoria
     * @param request Petición web 
     * @return Cadena con la estructura
     */
    public static String getAudit(HttpServletRequest request){
        AuditPrinter printer=(AuditPrinter)request.getServletContext().getAttribute(AccessControllerPublisher.AUDIT_PRINTER_KEY);        
        
        //valida que exista el printer
        if( request==null ){
            return "<div class=\"NA_General_textoError\">No se proporciono el request Web</div>\n";
        }
        else if( printer!=null ){
            HashMap<String,Object> params=new HashMap<String,Object>();
            params.put(SimpleHtmlLoginPrinter.REQUEST_KEY, request);
            DEBUGER.debug("Pinto la auditoria.");
            
            return (String)printer.printAudit(params);
        }
        else{
            DEBUGER.error("No existe Printer para desplegar la Auditoria");
            
            return "<div class=\"NA_General_textoError\">No existe un Printer para la Auditoria en el contexto</div>\n";
        }
    }
    
    /**
     * Recupera la estructurta HTML para el login
     * @param request Petición web 
     * @return Cadena con la estructura
     */
    public static String getLogin(HttpServletRequest request){
        LoginPrinter printer=(LoginPrinter)request.getServletContext().getAttribute(AccessControllerPublisher.LOGIN_PRINTER_KEY);        
        
        //valida que exista el printer
        if( request==null ){
            return "<div class=\"NA_General_textoError\">No se proporciono el request Web</div>\n";
        }
        else if( printer!=null ){
            HashMap<String,Object> params=new HashMap<String,Object>();
            params.put(SimpleHtmlLoginPrinter.REQUEST_KEY, request);
            params.put(AuthenticationWay.USER_PARAM, request.getParameter(AuthenticationWay.USER_PARAM));
            DEBUGER.debug("Pinto el Login.");
            
            return (String)printer.printLogin(params);
        }
        else{
            DEBUGER.error("No existe Printer para desplegar el Login");
            
            return "<div class=\"NA_General_textoError\">No existe un Printer para el Login en el contexto</div>\n";
        }
    }
    
    /**
     * Recupera el actual usuario que se encuentra en sesion
     * @param request Petición web
     * @return Usuario conectado
     */
    public static User getConnectedUser(HttpServletRequest request){
        User u=(User)request.getSession().getAttribute(AccessControllerPublisher.USER_KEY); 
        
        return u;
    }
    
    /**
     * Recupera la estructurta HTML para los detalles del usuario
     * @param request Petición web 
     * @return Cadena con la estructura
     */
    public static String getUserDetails(HttpServletRequest request){
        LoginPrinter printer=(LoginPrinter)request.getServletContext().getAttribute(AccessControllerPublisher.LOGIN_PRINTER_KEY);        
        
        //valida que exista el printer
        if( request==null ){
            return "<div class=\"NA_General_textoError\">No se proporciono el request Web</div>\n";
        }
        else if( printer!=null ){
            HashMap<String,Object> params=new HashMap<String,Object>();
            params.put(SimpleHtmlLoginPrinter.REQUEST_KEY, request);
            DEBUGER.debug("Pinto los detalles del usuario.");
            
            return (String)printer.printUserDetails(params);
        }
        else{
            DEBUGER.error("No existe Printer para desplegar los detalles del usuario");
            
            return "<div class=\"NA_General_textoError\">No existe un Printer para el Login en el contexto</div>\n";
        }
    }
    
    /**
     * Recupera la estructurta HTML para la administracion de roles
     * @param request Petición web 
     * @return Cadena con la estructura
     */
    public static String getRolesAdministration(HttpServletRequest request){
        RoleAdministratorPrinter printer=(RoleAdministratorPrinter)request.getServletContext().getAttribute(AccessControllerPublisher.ROLES_ADMIN_PRINTER_KEY);      
        User admin=(User)request.getSession().getAttribute(AccessControllerPublisher.USER_KEY);
        Map<String,Object> res;
        StringBuilder sb;
        Role rTmp;

        //valida que exista el printer
        if( request==null ){
            return "<div class=\"NA_General_textoError\">No se proporciono el request Web</div>\n";
        }
        else if( admin==null ){
            return "<div class=\"NA_General_textoError\">No se logro ubicar al usuario que gestiona la operación</div>\n";
        }
        else if( printer!=null ){
            //recupero el control de acceso
            AccessController ctrl=(AccessController)request.getServletContext().getAttribute(AccessControllerPublisher.ACCESS_CTRL_KEY);

            HashMap<String,Object> params=new HashMap<String,Object>();
            params.put(SimpleHtmlLoginPrinter.REQUEST_KEY, request);
            DEBUGER.debug("Operación solicitada: "+request.getParameter(OPERATION_PARAM));

            //si se va a agregar un rol
            if( request.getParameter(OPERATION_PARAM)!=null && request.getParameter(OPERATION_PARAM).equalsIgnoreCase(RoleAdministratorPrinter.ADD_OPERATION) ){
                DEBUGER.debug("Pinto el agregado de rol.");
                return (String)printer.printAddRole(params);
            }
            //si se va crea el rol
            else if( request.getParameter(OPERATION_PARAM)!=null && request.getParameter(OPERATION_PARAM).equalsIgnoreCase(RoleAdministratorPrinter.CREATE_OPERATION) ){
                DEBUGER.debug("Intento agregar un nuevo rol.");
                res=ctrl.registerRole(admin.getId(), request.getParameter(ProfilerWay.ROLE_DATA), parseRequestParameters(request) );
                
                //valida si existe error
                if( res.get(UserAdministratorPrinter.ERROR_DATA)!=null ){
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoError\">").append(proccessErrors(res)).append("</div>\n");
                    sb.append(printer.printAddRole(params));
                    
                    return sb.toString();
                }
                //si no existio error
                else{
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoOK\">Se agrego el rol con id: ").append(res.get(UserAdministratorPrinter.ID_DATA)).append("</div>\n");
                    sb.append(printer.printRolesList(params));

                    return sb.toString();
                }                
            }
            //si se edita el rol
            else if( request.getParameter(OPERATION_PARAM)!=null && request.getParameter(OPERATION_PARAM).equalsIgnoreCase(RoleAdministratorPrinter.EDIT_OPERATION) ){
                DEBUGER.debug("Intento editar un rol.");
                
                //reviso si existe el rol que se proporciono
                rTmp=ctrl.getRegisteredRole(request.getParameter(ID_PARAM));
                //si existe el rol
                if(rTmp!=null){
                    params.put(ROLE_PARAM, rTmp);
                    return (String)printer.printEditRole(params);
                }
                //si no existe el rol
                else{
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoError\">No se encontró el rol</div>\n");
                    sb.append(printer.printRolesList(params));

                    return sb.toString();
                }
            }
            //si se modifica el rol
            else if( request.getParameter(OPERATION_PARAM)!=null && request.getParameter(OPERATION_PARAM).equalsIgnoreCase(RoleAdministratorPrinter.UPDATE_OPERATION) ){
                DEBUGER.debug("Intento actualizar un el rol: "+request.getParameter(ID_PARAM));
                rTmp=ctrl.getRegisteredRole(request.getParameter(ID_PARAM));
                res=ctrl.modifyRole(admin.getId(), rTmp.getId(), parseRequestParameters(request) );
                
                //valida si existe error
                if( res.get(UserAdministratorPrinter.ERROR_DATA)!=null ){
                    params.put(ROLE_PARAM, rTmp);
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoError\">").append(proccessErrors(res)).append("</div>\n");
                    sb.append(printer.printEditRole(params));
                    
                    return sb.toString();
                }
                //si no existio error
                else{
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoOK\">Se modificó el rol con id: ").append(res.get(UserAdministratorPrinter.ID_DATA)).append("</div>\n");
                    sb.append(printer.printRolesList(params));

                    return sb.toString();
                }                
            }
            //si se editan los permisos
            else if( request.getParameter(OPERATION_PARAM)!=null && request.getParameter(OPERATION_PARAM).equalsIgnoreCase(RoleAdministratorPrinter.PERMISSIONS_OPERATION) ){
                DEBUGER.debug("Intento editar los permisos.");
                //reviso si se proporciono existe el usuario
                rTmp=ctrl.getRegisteredRole(request.getParameter(ID_PARAM));
                //si existe el rol
                if(rTmp!=null){
                    params.put(ROLE_PARAM, rTmp);
                    return (String)printer.printEditPermissions(params);
                }
                //si no existe el rol
                else{
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoError\">No se encontró el rol</div>\n");
                    sb.append(printer.printRolesList(params));

                    return sb.toString();
                }
            }
            //por default muestra la lista de roles
            else{
                DEBUGER.debug("Pinto el listado de usuarios.");
                return (String)printer.printRolesList(params);
            }
        }
        else{
            DEBUGER.error("No existe Printer para desplegar la Administración de roles");
            
            return "<div class=\"NA_General_textoError\">No existe un Printer para la Administración de Usuarios en el contexto</div>\n";
        }
        
    }
    
    /**
     * Recupera la estructurta HTML para la administracion de usuarios
     * @param request Peticion web
     * @return Cadena con la estructura
     */
    public static String getUsersAdministration(HttpServletRequest request){
        UserAdministratorPrinter printer=(UserAdministratorPrinter)request.getServletContext().getAttribute(AccessControllerPublisher.USERS_ADMIN_PRINTER_KEY);      
        User admin=(User)request.getSession().getAttribute(AccessControllerPublisher.USER_KEY);
        Map<String,Object> res;
        StringBuilder sb;
        User uTmp;
        
        //valida que exista el printer
        if( request==null ){
            return "<div class=\"NA_General_textoError\">No se proporciono el request Web</div>\n";
        }
        else if( admin==null ){
            return "<div class=\"NA_General_textoError\">No se logro ubicar al usuario que gestiona la operación</div>\n";
        }
        else if( printer!=null ){
            //recupero el control de acceso
            AccessController ctrl=(AccessController)request.getServletContext().getAttribute(AccessControllerPublisher.ACCESS_CTRL_KEY);

            HashMap<String,Object> params=new HashMap<String,Object>();
            params.put(SimpleHtmlLoginPrinter.REQUEST_KEY, request);
            DEBUGER.debug("Operación solicitada: "+request.getParameter(OPERATION_PARAM));
            
            //si se va a agregar un usuario
            if( request.getParameter(OPERATION_PARAM)!=null && request.getParameter(OPERATION_PARAM).equalsIgnoreCase(UserAdministratorPrinter.ADD_OPERATION) ){
                DEBUGER.debug("Pinto el agregado de usuario.");
                return (String)printer.printAddUser(params);
            }
            //si se va crea el usuario
            else if( request.getParameter(OPERATION_PARAM)!=null && request.getParameter(OPERATION_PARAM).equalsIgnoreCase(UserAdministratorPrinter.CREATE_OPERATION) ){
                DEBUGER.debug("Intento agregar un nuevo usuario.");
                res=ctrl.registerUser(admin.getId(), request.getParameter(AuthenticationWay.USER_DATA), parseRequestParameters(request) );
                
                //valida si existe error
                if( res.get(UserAdministratorPrinter.ERROR_DATA)!=null ){
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoError\">").append(proccessErrors(res)).append("</div>\n");
                    sb.append(printer.printAddUser(params));
                    
                    return sb.toString();
                }
                //si no existio error
                else{
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoOK\">Se agrego el usuario con id: ").append(res.get(UserAdministratorPrinter.ID_DATA)).append("</div>\n");
                    sb.append(printer.printUsersList(params));

                    return sb.toString();
                }                
            }
            //si se edita el usuario
            else if( request.getParameter(OPERATION_PARAM)!=null && request.getParameter(OPERATION_PARAM).equalsIgnoreCase(UserAdministratorPrinter.EDIT_OPERATION) ){
                DEBUGER.debug("Intento editar un usuario.");
                
                //reviso si  existe el usuario
                uTmp=ctrl.getRegisteredUser(request.getParameter(ID_PARAM));
                //si existe el usuario
                if(uTmp!=null){
                    params.put(USER_PARAM, uTmp);
                    return (String)printer.printEditUser(params);
                }
                //si no existe el usuario
                else{
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoError\">No se encontró el usuario</div>\n");
                    sb.append(printer.printUsersList(params));

                    return sb.toString();
                }
            }
            //si se modifica el usuario
            else if( request.getParameter(OPERATION_PARAM)!=null && request.getParameter(OPERATION_PARAM).equalsIgnoreCase(UserAdministratorPrinter.UPDATE_OPERATION) ){
                DEBUGER.debug("Intento actualizar un usuario.");
                uTmp=ctrl.getRegisteredUser(request.getParameter(ID_PARAM));
                res=ctrl.modifyUser(admin.getId(), uTmp.getId(), parseRequestParameters(request));
                
                //valida si existe error
                if( res.get(UserAdministratorPrinter.ERROR_DATA)!=null ){
                    params.put(USER_PARAM, uTmp);
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoError\">").append(proccessErrors(res)).append("</div>\n");
                    sb.append(printer.printEditUser(params));
                    
                    return sb.toString();
                }
                //si no existio error
                else{
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoOK\">Se modificó el usuario con id: ").append(res.get(UserAdministratorPrinter.ID_DATA)).append("</div>\n");
                    sb.append(printer.printUsersList(params));

                    return sb.toString();
                }                
            }
            //si se editan los roles
            else if( request.getParameter(OPERATION_PARAM)!=null && request.getParameter(OPERATION_PARAM).equalsIgnoreCase(UserAdministratorPrinter.ROLES_OPERATION) ){
                DEBUGER.debug("Intento editar los roles.");
                //reviso si se proporciono existe el usuario
                uTmp=ctrl.getRegisteredUser(request.getParameter(ID_PARAM));
                //si existe el usuario
                if(uTmp!=null){
                    params.put(USER_PARAM, uTmp);
                    return (String)printer.printEditRoles(params);
                }
                //si no existe el usuario
                else{
                    sb=new StringBuilder("");
                    sb.append("<div class=\"NA_General_textoError\">No se encontró el usuario</div>\n");
                    sb.append(printer.printUsersList(params));

                    return sb.toString();
                }
            }
            //por default muestra la lista de usuarios
            else{
                DEBUGER.debug("Pinto el listado de usuarios.");
                return (String)printer.printUsersList(params);
            }
        }
        else{
            DEBUGER.error("No existe Printer para desplegar la Administración de usuarios");
            
            return "<div class=\"NA_General_textoError\">No existe un Printer para la Administración de Usuarios en el contexto</div>\n";
        }
    }
    
    public static String getScriptAdminUserPath(HttpServletRequest request){
        return request.getContextPath()+AuthenticationResourcesLoader.PATH_ADMIN_USERS_JS;
    }

    /**
     * Recupera la ruta donde se publico el login
     * @param request Peticion Web
     * @return Cadena con la ruta
     */
    public static String getLoginServletPath(HttpServletRequest request){
        return request.getContextPath()+AccessControllerPublisher.PATH_LOGIN;
    }

    
    
    
    
    //metodos privados--------------------------------------------------------
    
    private static Map<String,Object> parseRequestParameters(HttpServletRequest request){
        HashMap<String,Object> data=new HashMap<String,Object>();
        Enumeration<String> names;
        String cTmp;
        
        if( request!=null ){
            names=request.getParameterNames();
            while(names.hasMoreElements()){
                cTmp=names.nextElement();
                
                data.put(cTmp, request.getParameter(cTmp));
            }
        }
        
        DEBUGER.debug("Parametros recuperado: "+data);
        
        return data;
    }
    
    private static String proccessErrors(Map<String,Object> errors){
        StringBuilder sb=new StringBuilder();
        
        if( errors!=null && errors.get(UserAdministratorPrinter.ERROR_DATA)!=null && errors.get(UserAdministratorPrinter.ERROR_DATA) instanceof String[] ){
            for(String e: (String[])errors.get(UserAdministratorPrinter.ERROR_DATA)){
                sb.append(e).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    public String x(){
        return null;
    }
}
