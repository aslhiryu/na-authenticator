package neoAtlantis.utils.accessController.printer;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import neoAtlantis.utils.accessController.AccessController;
import neoAtlantis.utils.accessController.objects.Permission;
import neoAtlantis.utils.accessController.objects.Role;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.accessController.printer.interfaces.HtmlRoleAdministratorPrinter;
import neoAtlantis.utils.accessController.printer.interfaces.RoleAdministratorPrinter;
import neoAtlantis.utils.accessController.profiler.interfaces.ProfilerWay;
import neoAtlantis.utils.accessController.web.UtilsAuthenticatorBean;
import neoAtlantis.utils.accessController.web.listeners.AccessControllerPublisher;
import neoAtlantis.utils.apps.printer.exceptions.FormatterException;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class SimpleHtmlRoleAdministratorPrinter extends HtmlRoleAdministratorPrinter {

    @Override
    public Object printRolesList(Map<String, Object> params) throws FormatterException {
        StringBuilder sb = new StringBuilder("");
        HttpServletRequest request;
        
        if( params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)==null ){
            throw new FormatterException("No se proporciono el request para generar el objeto HTML.");
        }
        //obtengo el request
        request=((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY));
        //recupero el control de acceso
        AccessController ctrl=(AccessController)request.getServletContext().getAttribute(AccessControllerPublisher.ACCESS_CTRL_KEY);

        List<Role> lTmp=ctrl.getRegisteredRoles();

        sb.append("<table border=0 class=\"NA_RoleList_table\">\n");
        sb.append("<tr class=\"NA_RoleList_title\">\n");
        sb.append("<td>Id</td>\n");
        sb.append("<td>Nombre</td>\n");
        sb.append("<td>Acciones</td>\n");
        sb.append("</tr>\n");
        for(int i=0; lTmp!=null&&i<lTmp.size(); i++){
            sb.append("<tr class=\"NA_RoleList_row").append((i%2)+1).append("\">\n");
            sb.append("<td>").append(lTmp.get(i).getId()).append("</td>\n");
            sb.append("<td>").append(lTmp.get(i).getName()).append("</td>\n");
            sb.append("<td>\n");
            //reviso que se puedan modificar los roles
            if(ctrl.allowsUpdateRoles()){
                sb.append("<a id=\"NA:ModifyRoleButton:").append(lTmp.get(i).getId()).append("\" href=\"#\" class=\"").append(!lTmp.get(i).isActive()? "NA_null": "NA_RoleList_editButton").append("\" title=\"Editar Rol\" onclick=\"location.href='").append(request.getContextPath()).append(request.getServletPath()).append("?").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("=").append(RoleAdministratorPrinter.EDIT_OPERATION).append("&").append(UtilsAuthenticatorBean.ID_PARAM).append("=").append(lTmp.get(i).getId()).append("';\">&nbsp;</a>\n");
            }
            //valido que se puedan editar los permisos
            if(ctrl.canAsignPermissions()){
                sb.append("<a id=\"NA:ChangePermissionsButton:").append(lTmp.get(i).getId()).append("\" href=\"#\" class=\"").append(!lTmp.get(i).isActive()? "NA_null": "NA_RoleList_permissionsButton").append("\" title=\"Modificar Permisos Asignados\" onclick=\"location.href='").append(request.getContextPath()).append(request.getServletPath()).append("?").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("=editPermissions&").append(UtilsAuthenticatorBean.ID_PARAM).append("=").append(lTmp.get(i).getId()).append("';\">&nbsp;</a>\n");
            }
            sb.append("</td>\n");
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
        
        sb.append("<div style=\"height:30px;\" />\n");
        
        sb.append("<table border=0 class=\"NA_RoleUtils_table\">\n");
        sb.append("<tr>\n");
        sb.append("<td class=\"NA_RoleUtils_info\">Roles Registrados: ").append(lTmp!=null? lTmp.size(): 0).append("</td>\n");
        sb.append("<td><button id=\"NA:CreateRoleButton\" class=\"NA_CreateRole_button\" onclick=\"location.href='").append(request.getContextPath()).append(request.getServletPath()).append("?").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("=").append(RoleAdministratorPrinter.ADD_OPERATION).append("'\"><div />Crear Rol</button></td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");

        return sb.toString();
    }

    @Override
    public String contentAddRole(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder("");

        sb.append("<table border=0 class=\"NA_AddRole_table\">\n");
        sb.append("<tr class=\"NA_AddRole_title\">\n");
        sb.append("<td colspan=2>Nuevo Rol</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr class=\"NA_AddRole_information\">\n");
        sb.append("<td class=\"NA_AddRole_data\">Nombre</td>\n");
        sb.append("<td class=\"NA_AddRole_field\"><input name=\"").append(ProfilerWay.ROLE_DATA).append("\" id=\"").append(ProfilerWay.ROLE_DATA).append("\" /></td>\n");
        sb.append("</tr>\n");
        sb.append("<tr class=\"NA_AddRole_options\">\n");
        sb.append("<td colspan=2>\n");
        sb.append("<button id=\"NA:CreateButton\" class=\"NA_Create_button\"  type=\"submit\"><div />Crear</button>\n");
        sb.append("<button id=\"NA:CancelButton\" class=\"NA_Cancel_button\"  type=\"button\" onclick=\"location.href='").append(((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)).getContextPath()).append(((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)).getServletPath()).append("'\"><div />Cancelar</button>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");
        
        return sb.toString();
    }

    @Override
    public String contentEditRole(Map<String, Object> params) throws FormatterException {
        StringBuilder sb = new StringBuilder("");
        
        sb.append("<table border=0 class=\"NA_EditRole_table\">\n");
        sb.append("<tr class=\"NA_EditRole_title\">\n");
        sb.append("<td colspan=2>Modifica Rol</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr class=\"NA_EditRole_information\">\n");
        sb.append("<td class=\"NA_EditRole_data\">Id</td>\n");
        sb.append("<td class=\"NA_EditRole_field\">").append(((Role)params.get(UtilsAuthenticatorBean.ROLE_PARAM)).getId()).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr class=\"NA_EditRole_information\">\n");
        sb.append("<td class=\"NA_EditRole_data\">Nombre</td>\n");
        sb.append("<td class=\"NA_EditRole_field\"><input name=\"NA:RoleName\" id=\"NA:RoleName\" value=\"").append(((Role)params.get(UtilsAuthenticatorBean.ROLE_PARAM)).getName()).append("\" /></td>\n");
        sb.append("</tr>\n");
        sb.append("<tr class=\"NA_EditRole_options\">\n");
        sb.append("<td colspan=2>\n");
        sb.append("<button id=\"NA:UpdateButton\" class=\"NA_Update_button\"  type=\"submit\"><div />Modificar</button>\n");
        sb.append("<button id=\"NA:CancelButton\" class=\"NA_Cancel_button\"  type=\"button\" onclick=\"location.href='").append(((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)).getContextPath()).append(((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)).getServletPath()).append("'\"><div />Cancelar</button>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");
        
        return sb.toString();
    }
    
    @Override
    public String contentEditPermissions(Map<String,Object> params) throws FormatterException{
        StringBuilder sb = new StringBuilder("");
        HttpServletRequest request;
        List<Permission> disponibles, asignados;
        boolean selecionado;
        
        //obtengo el request
        request=((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY));
        
        //recupero el control de acceso
        AccessController ctrl=(AccessController)request.getServletContext().getAttribute(AccessControllerPublisher.ACCESS_CTRL_KEY);
        User uCon=(User)request.getSession().getAttribute(AccessControllerPublisher.USER_KEY);

        sb.append("<table border=0 class=\"NA_EditPermissions_table\">\n");
        sb.append("<tr class=\"NA_EditPermissions_title\">\n");
        sb.append("<td colspan=2>Permisos Asignados</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr class=\"NA_EditPermissions_information\">\n");
        sb.append("<td class=\"NA_EditPermissions_data\">Rol</td>\n");
        sb.append("<td class=\"NA_EditPermissions_field\">").append(((Role)params.get(UtilsAuthenticatorBean.ROLE_PARAM)).getName()).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr class=\"NA_EditPermissions_information\">\n");
        sb.append("<td class=\"NA_EditPermissions_data\">Permisos</td>\n");
        sb.append("<td class=\"NA_EditPermissions_field\">\n");
        
        disponibles=ctrl.getActivedPermissions();
        asignados=ctrl.getAssignedPermissions(((Role)params.get(UtilsAuthenticatorBean.ROLE_PARAM)), params);
        for(int i=0; disponibles!=null&&i<disponibles.size(); i++){
            sb.append("<a id=\"NA:AssigPermissionButton:").append(disponibles.get(i).getId()).append("\" href=\"#\" class=\"NA_EditPermission_");
            selecionado=false;
            
            for(int j=0; asignados!=null&&j<asignados.size(); j++){
                if( asignados.get(j).getId().equals(disponibles.get(i).getId()) ){
                    sb.append("selectedPermission");
                    selecionado=true;
                    break;
                }
            }
            
            if(!selecionado){
                sb.append("unselectedPermission");
            }
            sb.append("\" title=\"").append(selecionado? "Remueve": "Asigna").append(" Permiso\" onclick=\"NAAdminUsers").append(selecionado? "Remove": "Add").append("PermissionRole('").append(((Role)params.get(UtilsAuthenticatorBean.ROLE_PARAM)).getId()).append("', '").append(disponibles.get(i).getId()).append("', '").append(uCon.getSession().getId()).append("')\">&nbsp;</a>").append(disponibles.get(i).getName()).append("<br />\n");
        }
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr class=\"NA_EditPermissions_options\">\n");
        sb.append("<td colspan=2>\n");
        sb.append("<button id=\"NA:ReturnButton\" class=\"NA_Return_button\"  type=\"button\" onclick=\"location.href='").append(((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)).getContextPath()).append(((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)).getServletPath()).append("'\"><div />Regresar</button>\n");
        sb.append("</td>\n");
        sb.append("</table>\n");
        
        return sb.toString();
    }
}
