package neoAtlantis.utils.accessController.web;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neoAtlantis.utils.accessController.AccessController;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.accessController.web.listeners.AccessControllerPublisher;
import neoAtlantis.utils.apps.web.utils.ParameterCleaner;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class AdminUsersServlet extends HttpServlet {
    private static final Logger DEBUGER=Logger.getLogger(AdminUsersServlet.class);
    
    public static final String OPERATION_PARAM="NA_Operation";
    public static final String SESSION_PARAM="NA_Session";
    public static final String ID_USER_PARAM="NA_IdUser";
    public static final String ID_ROLE_PARAM="NA_IdRole";
    public static final String ID_PERMISSION_PARAM="NA_IdPermission";
    public static final String PATH_SERVICE="/neoAtlantis/resources/web/adminUser.service";


    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String salida;
        User uTmp;
        
        DEBUGER.debug("Se realiza una solicitud al servlet de administración de usuarios, con operación '"+request.getParameter(OPERATION_PARAM)+"' y solicitante '"+request.getParameter(SESSION_PARAM)+"'");
        
        Map<String,String[]>params=ParameterCleaner.cleanParameter(request.getParameterMap());
        
        
        //valido que exista el control
        AccessController ctrl=(AccessController)request.getServletContext().getAttribute(AccessControllerPublisher.ACCESS_CTRL_KEY);
        if( ctrl!=null ){
            //cvalida que sea una sesion autorizada
            uTmp=ctrl.getConnectedUserBySession(request.getParameter(SESSION_PARAM));
            if( uTmp!=null ){
                //reviso que operacion se debe realizar
                if( params.get(OPERATION_PARAM)!=null && params.get(OPERATION_PARAM)[0].equalsIgnoreCase("activeUser") ){
                    salida="DATA:"+ctrl.activeUser(uTmp.getId(), params.get(ID_USER_PARAM)[0], true);
                }
                else if( params.get(OPERATION_PARAM)!=null && params.get(OPERATION_PARAM)[0].equalsIgnoreCase("inactiveUser") ){
                    salida="DATA:"+ctrl.activeUser(uTmp.getId(), params.get(ID_USER_PARAM)[0], false);
                }
                else if( params.get(OPERATION_PARAM)!=null && params.get(OPERATION_PARAM)[0].equalsIgnoreCase("addRole") ){
                    salida="DATA:"+ctrl.assignRole(uTmp.getId(), params.get(ID_USER_PARAM)[0], params.get(ID_ROLE_PARAM)[0]);
                }
                else if( params.get(OPERATION_PARAM)!=null && params.get(OPERATION_PARAM)[0].equalsIgnoreCase("removeRole") ){
                    salida="DATA:"+ctrl.removeRole(uTmp.getId(), params.get(ID_USER_PARAM)[0], params.get(ID_ROLE_PARAM)[0]);
                }
                else if( params.get(OPERATION_PARAM)!=null && params.get(OPERATION_PARAM)[0].equalsIgnoreCase("removeSession") ){
                    salida="DATA:"+ctrl.removeConnection(params.get(ID_USER_PARAM)[0], uTmp);
                }
                else if( params.get(OPERATION_PARAM)!=null && params.get(OPERATION_PARAM)[0].equalsIgnoreCase("removeBlock") ){
                    salida="DATA:"+ctrl.removeBlock(uTmp.getId(), params.get(ID_USER_PARAM)[0]);
                }
                else if( params.get(OPERATION_PARAM)!=null && params.get(OPERATION_PARAM)[0].equalsIgnoreCase("addPermissionRole") ){
                    salida="DATA:"+ctrl.assignPermissionToRole(uTmp.getId(), params.get(ID_ROLE_PARAM)[0], params.get(ID_PERMISSION_PARAM)[0]);
                }
                else if( params.get(OPERATION_PARAM)!=null && params.get(OPERATION_PARAM)[0].equalsIgnoreCase("removePermissionRole") ){
                    salida="DATA:"+ctrl.removePermissionFromRole(uTmp.getId(), params.get(ID_ROLE_PARAM)[0], params.get(ID_PERMISSION_PARAM)[0]);
                }
                else{
                    salida="ERROR:Operacion no valida";
                }
            }
            else{
                salida="ERROR:La session no es valida";
            }
        }
        else{
            salida="ERROR:No existe el servicio de administración de usuarios";
        }
        
        response.getOutputStream().print(salida);
        response.flushBuffer();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        this.doGet(request, response);
    }
}
