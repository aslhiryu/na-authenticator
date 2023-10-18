package neoAtlantis.utils.accessController.printer;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import neoAtlantis.utils.accessController.AccessController;
import neoAtlantis.utils.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utils.accessController.objects.Role;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.accessController.printer.interfaces.UserAdministratorPrinter;
import neoAtlantis.utils.accessController.web.UtilsAuthenticatorBean;
import neoAtlantis.utils.accessController.web.listeners.AccessControllerPublisher;
import neoAtlantis.utils.accessController.web.utils.AuthenticationResourcesLoader;
import neoAtlantis.utils.apps.catalogs.objetcs.OrderType;
import neoAtlantis.utils.apps.printer.exceptions.FormatterException;
import neoAtlantis.utils.apps.utils.UtilsPagination;
import neoAtlantis.utils.apps.web.utils.ParameterCleaner;
import neoAtlantis.utils.apps.web.utils.ResourcesLoader;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class SimpleHtmlUserAdministratorPrinter implements UserAdministratorPrinter {
    public static final String NAME_DATA="NA:UserName";
    public static final String PASSWORD_DATA="NA:UserPass";
    public static final String EMAIL_DATA="NA:UserMail";
    
    @Override
    public Object printUsersList(Map<String,Object> params) {
        StringBuilder sb = new StringBuilder("");
        HttpServletRequest request;
        String order="1";
        OrderType ordTipo=OrderType.ASC;
        
        if( params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)==null ){
            throw new FormatterException("No se proporciono el request para generar el objeto HTML.");
        }
        //obtengo el request
        request=((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY));
        if( request.getParameter(ORDER_PARAM)!=null ){
            order=request.getParameter(ORDER_PARAM);
        }
        if( request.getParameter(MODE_ORDER_PARAM)!=null && request.getParameter(MODE_ORDER_PARAM).equalsIgnoreCase("DESC") ){
            ordTipo=OrderType.DESC;
        }
        
        //recupero el control de acceso
        AccessController ctrl=(AccessController)request.getServletContext().getAttribute(AccessControllerPublisher.ACCESS_CTRL_KEY);
        User uCon=(User)request.getSession().getAttribute(AccessControllerPublisher.USER_KEY);
        //recupero la lista de usuarios registrados
        List<User> lTmp=ctrl.getRegisteredUsers(order, ordTipo);
        User uTmp, uTmp2;
        int pags=UtilsPagination.calculatePages( (lTmp!=null? lTmp.size(): 0) );
        int pagActual=1;        
        try{
            pagActual=Integer.parseInt( request.getParameter(PAGE_PARAM) );
        }catch(Exception ex){}
        
        sb.append("<script src=\"").append( request.getContextPath() ).append(ResourcesLoader.PATH_UTILS_JS).append("\"></script>\n");
        sb.append("<script src=\"").append( request.getContextPath() ).append(AuthenticationResourcesLoader.PATH_ADMIN_USERS_JS).append("\"></script>\n");
        sb.append("<form name=\"NA:ChangedDataList\" action=\"").append(request.getContextPath() ).append("/").append( ParameterCleaner.getRelativeURL(request)).append("\" id=\"NA:ChangedDataList\"  method=\"post\">\n");
        sb.append("<input type=\"hidden\" name=\"").append(PAGE_PARAM).append("\" id=\"").append(PAGE_PARAM).append("\"  value=\"").append(pagActual).append("\">\n");
        sb.append("<input type=\"hidden\" name=\"").append(ORDER_PARAM).append("\" id=\"").append(ORDER_PARAM).append("\"  value=\"").append(order).append("\">\n");
        sb.append("<input type=\"hidden\" name=\"").append(MODE_ORDER_PARAM).append("\" id=\"").append(MODE_ORDER_PARAM).append("\"  value=\"").append(ordTipo).append("\">\n");
        sb.append("<input type=\"hidden\" name=\"").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("\" id=\"").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("\"  value=\"\">\n");
        sb.append("<input type=\"hidden\" name=\"").append(UtilsAuthenticatorBean.ID_PARAM).append("\" id=\"").append(UtilsAuthenticatorBean.ID_PARAM).append("\"  value=\"\">\n");
        sb.append("<div class=\"NA_DataList_list\">\n");
        sb.append("<table>\n");
        sb.append("<tr>\n");
        //sb.append("<th>\n");
        //sb.append("<a href=\"javaScript:NADataChangeOrder('").append(AuthenticationWay.ID_FIELD).append("', '").append(AuthenticationWay.ID_FIELD.equals(order)&&ordTipo==OrderType.ASC? "DESC": "ASC").append("')\" />Id</a>\n");
        //sb.append(AuthenticationWay.ID_FIELD.toString().equalsIgnoreCase(order)? "<div class=\"NA_data_order"+ordTipo+"\" />": "");
        //sb.append("</th>\n");
        sb.append("<th>\n");
        sb.append(AuthenticationWay.LOGIN_FIELD.toString().equalsIgnoreCase(order)? "<div class=\"NA_data_order"+ordTipo+"\"></div>": "");
        sb.append("<a href=\"javaScript:NADefaultDataChangeOrder('").append(AuthenticationWay.LOGIN_FIELD).append("', '").append(AuthenticationWay.LOGIN_FIELD.equals(order)&&ordTipo==OrderType.ASC? "DESC": "ASC").append("')\" />Usuario</a>\n");
        sb.append("</th>\n");
        sb.append("<th>\n");
        sb.append(AuthenticationWay.NAME_FIELD.toString().equalsIgnoreCase(order)? "<div class=\"NA_data_order"+ordTipo+"\"></div>": "");
        sb.append("<a href=\"javaScript:NADefaultDataChangeOrder('").append(AuthenticationWay.NAME_FIELD).append("', '").append(AuthenticationWay.NAME_FIELD.equals(order)&&ordTipo==OrderType.ASC? "DESC": "ASC").append("')\" />Nombre</a>\n");
        sb.append("</th>\n");
        sb.append("<th>\n");
        sb.append(AuthenticationWay.MAIL_FIELD.toString().equalsIgnoreCase(order)? "<div class=\"NA_data_order"+ordTipo+"\"></div>": "");
        sb.append("<a href=\"javaScript:NADefaultDataChangeOrder('").append(AuthenticationWay.MAIL_FIELD).append("', '").append(AuthenticationWay.MAIL_FIELD.equals(order)&&ordTipo==OrderType.ASC? "DESC": "ASC").append("')\" />Mail</a>\n");
        sb.append("</th>\n");
        sb.append("<th>Acciones</th>\n");
        sb.append("</tr>\n");
        for(int i=0; lTmp!=null&&i<lTmp.size(); i++){
            //valida si los registros estan dentro de la pagina
            if( i<(pagActual-1)*UtilsPagination.getPageSise() ){
                continue;
            }
            if( i>=pagActual*UtilsPagination.getPageSise() ){
                break;
            }
            
            sb.append("<tr>\n");
            //sb.append("<td>").append(lTmp.get(i).getId()).append("</td>\n");
            sb.append("<td>").append(lTmp.get(i).getUser()).append("</td>\n");
            sb.append("<td>").append(lTmp.get(i).getName()!=null? lTmp.get(i).getName(): "").append("</td>\n");
            sb.append("<td>").append(lTmp.get(i).getMail()!=null? lTmp.get(i).getMail(): "").append("</td>\n");
            sb.append("<td>\n");
            //recupero si el usuario esta conectado
            uTmp=ctrl.getConnectedUser(lTmp.get(i).getId());
            //recupero si el usuario esta bloqueado
            uTmp2=ctrl.getBlockedUser(lTmp.get(i).getId());
            
            //si el usuario esta bloquedo
            if( uTmp2!=null ){
                sb.append("<a href=\"#\" id=\"NA:StatusUserButton:").append(lTmp.get(i).getId()).append("\" class=\"NA_UserList_blockedUserButton\" title=\"El usuario esta bloqueado\" onclick=\"NAAdminUsersRemoveBlock('").append(lTmp.get(i).getId()).append("', '").append(uCon.getSession().getId()).append("')\">&nbsp;</a>\n");                
            }
            //reviso si es monousuario
            if( !ctrl.isMultiUser() && uTmp2==null ){
                sb.append("<a href=\"#\" id=\"NA:StatusUserButton:").append(lTmp.get(i).getId()).append("\" class=\"NA_UserList_").append(uTmp!=null? "on": "off").append("lineButton\" title=\"").append(uTmp!=null? "En": "Fuera de").append(" Linea ").append(uTmp!=null? "&#13;Origen:"+uTmp.getOrigin()+"&#13;SO:"+uTmp.getSession().getOs()+"&#13;Browser:"+uTmp.getSession().getBrowser(): "").append("\" onclick=\"").append(uTmp!=null&&uCon!=null && !uCon.getId().equals(lTmp.get(i).getId())? "NAAdminUsersRemoveConnection('"+lTmp.get(i).getId()+"', '"+uCon.getSession().getId()+"')": "").append("\">&nbsp;</a>\n");
            }

            //valido que no sea el mismo
            if( uCon!=null && !uCon.getId().equals(lTmp.get(i).getId()) ){
                sb.append("<a href=\"#\" id=\"NA:ActiveUserButton:").append(lTmp.get(i).getId()).append("\" class=\"NA_UserList_").append(lTmp.get(i).isActive()? "inactive": "active").append("Button\" title=\"").append(lTmp.get(i).isActive()? "Inhabilita": "Habilita").append(" al usuario\" onclick=\"NAAdminUsers").append(lTmp.get(i).isActive()? "Inactive": "Active").append("User('").append(lTmp.get(i).getId()).append("', '").append(uCon.getSession().getId()).append("')\" >&nbsp;</a>\n");
            }
            //reviso que se puedan modificar los usuarios
            if(ctrl.allowsUpdateUsers()){
                sb.append("<a id=\"NA:ModifyUserButton:").append(lTmp.get(i).getId()).append("\"  class=\"").append(!lTmp.get(i).isActive()? "NA_null": "NA_UserList_editButton").append("\" title=\"Editar Usuario\" href=\"javaScript:NAAdminUsersExecuteOperation('").append(UserAdministratorPrinter.EDIT_OPERATION).append("', '").append(lTmp.get(i).getId()).append("');\">&nbsp;</a>\n");
            }
            //valido que se puedan cambiar las contraseñas
            if(ctrl.allowsUpdatePasswords()){
                sb.append("<a id=\"NA:ChangePassButton:").append(lTmp.get(i).getId()).append("\" href=\"#\" class=\"").append(!lTmp.get(i).isActive()? "NA_null": "NA_UserList_passwordButton").append("\" title=\"Modificar Contrase&ntilde;a\">&nbsp;</a>\n");
            }
            //valido que se puedan editar los roles
            if(ctrl.canAsignRoles()){
                sb.append("<a id=\"NA:ChangeRolesButton:").append(lTmp.get(i).getId()).append("\" class=\"").append(!lTmp.get(i).isActive()? "NA_null": "NA_UserList_rolesButton").append("\" title=\"Modificar Roles Asignados\" href=\"javaScript:NAAdminUsersExecuteOperation('").append(UserAdministratorPrinter.ROLES_OPERATION).append("', '").append(lTmp.get(i).getId()).append("');\">&nbsp;</a>\n");
            }
            sb.append("</td>\n");
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
        
        sb.append(UtilsPagination.printHtmlPagination(pags, pagActual)).append("\n");
        sb.append("<div id=\"NA_ExtraData\">\n");
        sb.append("<p>Usuarios Conectados: ").append(ctrl.getConnections().size()).append("</p><p>Usuarios Registrados: ").append(lTmp!=null? lTmp.size(): 0).append("</p>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"NA_Controls\">\n");
        if( ctrl.allowsCreateUser()){
        sb.append("<button type=\"button\" class=\"NA_Controls_newUser\" onclick=\"location.href='").append(request.getContextPath() ).append("/").append( ParameterCleaner.getRelativeURL(request)).append("?").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("=").append(UserAdministratorPrinter.ADD_OPERATION).append("'\">Nuevo Usuario</button>\n");
        }
        sb.append("</div>\n");
        sb.append("</div>\n");
        sb.append("</form>\n");
        
        return sb.toString();
    }
    
    @Override
    public Object printAddUser(Map<String,Object> params) throws FormatterException{
        StringBuilder sb = new StringBuilder("");
        
        if( params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)==null ){
            throw new FormatterException("No se proporciono el request para generar el objeto HTML.");
        }
        HttpServletRequest request=(HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY);
        
        sb.append("<div class=\"NA_DataForm_form\">\n");
        sb.append("<form name=\"NA:AddUserForm\" action=\"").append(request.getContextPath() ).append("/").append( ParameterCleaner.getRelativeURL(request)).append("\" method=\"post\" >\n");
        sb.append("<h1>Nuevo Usuario</h1>\n");
        sb.append("<dl>\n");
        sb.append("<dt >Nombre</dt>\n");
        sb.append("<dd><input name=\"").append(NAME_DATA).append("\" id=\"").append(NAME_DATA).append("\" value=\"").append(request.getParameter(NAME_DATA)!=null? request.getParameter(NAME_DATA): "").append("\" /></dd>\n");
        sb.append("<dt>Usuario</dt>\n");
        sb.append("<dd><input name=\"").append(AuthenticationWay.USER_DATA).append("\" id=\"").append(AuthenticationWay.USER_DATA).append("\" value=\"").append(request.getParameter(AuthenticationWay.USER_DATA)!=null? request.getParameter(AuthenticationWay.USER_DATA): "").append("\" /></dd>\n");
        sb.append("<dt>Contrase&ntilde;a</dt>\n");
        sb.append("<dd><input name=\"").append(PASSWORD_DATA).append("\" id=\"").append(PASSWORD_DATA).append("\" value=\"").append(request.getParameter(PASSWORD_DATA)!=null? request.getParameter(PASSWORD_DATA): "").append("\" /></dd>\n");
        sb.append("<dt>Correo</dt>\n");
        sb.append("<dd><input name=\"").append(EMAIL_DATA).append("\" id=\"").append(EMAIL_DATA).append("\" value=\"").append(request.getParameter(EMAIL_DATA)!=null? request.getParameter(EMAIL_DATA): "").append("\" /></dd>\n");
        sb.append("</dl>\n");
        sb.append("<div class=\"NA_Controls\">\n");
        sb.append("<button id=\"NA:CreateButton\" class=\"NA_Controls_addUser\"  type=\"submit\">Crear</button>\n");
        sb.append("<button id=\"NA:CancelButton\" class=\"NA_Ccontrols_cancel\"  type=\"button\" onclick=\"this.form.").append(UtilsAuthenticatorBean.OPERATION_PARAM).append(".value='';this.form.submit();\">Cancelar</button>\n");
        sb.append("</div>\n");
        sb.append("<input type=\"hidden\" name=\"").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("\" id=\"").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("\" value=\"").append(UserAdministratorPrinter.CREATE_OPERATION).append("\" />\n");
        sb.append("</form>\n");
        sb.append("</div>\n");
        
        return sb.toString();
    }
    
    @Override
    public Object printEditUser(Map<String,Object> params) throws FormatterException{
        StringBuilder sb = new StringBuilder("");
        
        if( params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)==null ){
            throw new FormatterException("No se proporciono el request para generar el objeto HTML.");
        }
        if( params.get(UtilsAuthenticatorBean.USER_PARAM)==null ){
            throw new FormatterException("No se proporciono el usuario para generar el objeto HTML.");
        }
        HttpServletRequest request=(HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY);
        
        sb.append("<div class=\"NA_DataForm_form\">\n");
        sb.append("<form name=\"NA:EditUserForm\" action=\"").append(request.getContextPath() ).append("/").append( ParameterCleaner.getRelativeURL(request)).append("\" method=\"post\">\n");
        sb.append("<h1>Modifica Usuario</h1>\n");
        sb.append("<dl>\n");
        sb.append("<dt>Id</dt>\n");
        sb.append("<dd>").append(((User)params.get(UtilsAuthenticatorBean.USER_PARAM)).getId()).append("</dd>\n");
        sb.append("<dt>Usuario</dt>\n");
        sb.append("<dd>").append(((User)params.get(UtilsAuthenticatorBean.USER_PARAM)).getUser()).append("</dd>\n");
        sb.append("<dt>Nombre</dt>\n");
        sb.append("<dd><input name=\"").append(NAME_DATA).append("\" id=\"").append(NAME_DATA).append("\" value=\"").append(((User)params.get(UtilsAuthenticatorBean.USER_PARAM)).getName()).append("\" /></dd>\n");
        sb.append("<dt>Correo</dt>\n");
        sb.append("<dd><input name=\"").append(EMAIL_DATA).append("\" id=\"").append(EMAIL_DATA).append("\" value=\"").append(((User)params.get(UtilsAuthenticatorBean.USER_PARAM)).getMail()).append("\" /></dd>\n");
        sb.append("</dl>\n");
        sb.append("<div class=\"NA_Controls\">\n");
        sb.append("<button id=\"NA:UpdateButton\" class=\"NA_Ccontrols_setUser\"  type=\"submit\"><div />Modificar</button>\n");
        sb.append("<button id=\"NA:CancelButton\" class=\"NA_Ccontrols_cancel\"  type=\"button\" onclick=\"this.form.").append(UtilsAuthenticatorBean.OPERATION_PARAM).append(".value='';this.form.submit();\"><div />Cancelar</button>\n");
        sb.append("</div>\n");
        sb.append("<input type=\"hidden\" name=\"").append(ID_DATA).append("\" id=\"").append(ID_DATA).append("\" value=\"").append(((User)params.get(UtilsAuthenticatorBean.USER_PARAM)).getId()).append("\" />\n");
        sb.append("<input type=\"hidden\" name=\"").append(UtilsAuthenticatorBean.ID_PARAM).append("\" id=\"").append(UtilsAuthenticatorBean.ID_PARAM).append("\" value=\"").append(((User)params.get(UtilsAuthenticatorBean.USER_PARAM)).getId()).append("\" />\n");
        sb.append("<input type=\"hidden\" name=\"").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("\" id=\"").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("\" value=\"").append(UserAdministratorPrinter.UPDATE_OPERATION).append("\" />\n");
        sb.append("</form>\n");
        sb.append("</div>\n");
        
        return sb.toString();
    }
    
    @Override
    public Object printEditRoles(Map<String,Object> params) throws FormatterException{
        StringBuilder sb = new StringBuilder("");
        HttpServletRequest request;
        List<Role> disponibles, asignados;
        boolean selecionado;
        
        if( params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)==null ){
            throw new FormatterException("No se proporciono el request para generar el objeto HTML.");
        }
        if( params.get(UtilsAuthenticatorBean.USER_PARAM)==null ){
            throw new FormatterException("No se proporciono el usuario para generar el objeto HTML.");
        }
        //obtengo el request
        request=((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY));
        
        //recupero el control de acceso
        AccessController ctrl=(AccessController)request.getServletContext().getAttribute(AccessControllerPublisher.ACCESS_CTRL_KEY);
        User uCon=(User)request.getSession().getAttribute(AccessControllerPublisher.USER_KEY);

        sb.append("<script src=\"").append( request.getContextPath() ).append(ResourcesLoader.PATH_UTILS_JS).append("\"></script>\n");
        sb.append("<script src=\"").append( request.getContextPath() ).append(AuthenticationResourcesLoader.PATH_ADMIN_USERS_JS).append("\"></script>\n");
        sb.append("<div class=\"NA_DataForm_form\">\n");
        sb.append("<form name=\"NA:RolesUserForm\" action=\"").append(request.getContextPath() ).append("/").append( ParameterCleaner.getRelativeURL(request)).append("\">\n");
        sb.append("<h1>Modifica Permisos</h1>\n");
        sb.append("<dl>\n");
        sb.append("<dt>Usuario</dt>\n");
        sb.append("<dd>").append(((User)params.get(UtilsAuthenticatorBean.USER_PARAM)).getUser()).append("</dd>\n");
        sb.append("<dt>Roles</dt>\n");
        sb.append("<dd>\n");
        
        disponibles=ctrl.getActivedRoles();
        asignados=ctrl.getAssignedRoles(((User)params.get(UtilsAuthenticatorBean.USER_PARAM)), params);
        for(int i=0; disponibles!=null&&i<disponibles.size(); i++){
            sb.append("<a id=\"NA:AssigRoleButton:").append(disponibles.get(i).getId()).append("\" href=\"#\" class=\"NA_EditRole_");
            selecionado=false;
            
            for(int j=0; asignados!=null&&j<asignados.size(); j++){
                if( asignados.get(j).getId().equals(disponibles.get(i).getId()) ){
                    sb.append("selectedRole");
                    selecionado=true;
                    break;
                }
            }
            
            if(!selecionado){
                sb.append("unselectedRole");
            }
            sb.append("\" title=\"").append(selecionado? "Remueve": "Asigna").append(" Rol\" onclick=\"NAAdminUsers").append(selecionado? "Remove": "Add").append("Role('").append(((User)params.get(UtilsAuthenticatorBean.USER_PARAM)).getId()).append("', '").append(disponibles.get(i).getId()).append("', '").append(uCon.getSession().getId()).append("')\">&nbsp;</a>").append(disponibles.get(i).getName()).append("<br />\n");
        }
        sb.append("</dd>\n");
        sb.append("</dl>\n");
        sb.append("<div class=\"NA_Controls\">\n");
        sb.append("<button id=\"NA:ReturnButton\" class=\"NA_Return_button\"  type=\"button\" onclick=\"this.form.submit();\"><div />Aplicar</button>\n");
        sb.append("</div>\n");
        sb.append("</form>\n");
        sb.append("</div>\n");
        
        return sb.toString();
    }
}
