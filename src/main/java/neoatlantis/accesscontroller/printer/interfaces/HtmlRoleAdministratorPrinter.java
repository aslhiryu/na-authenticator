package neoAtlantis.utils.accessController.printer.interfaces;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import neoAtlantis.utils.accessController.objects.Role;
import neoAtlantis.utils.accessController.printer.SimpleHtmlLoginPrinter;
import neoAtlantis.utils.accessController.web.UtilsAuthenticatorBean;
import neoAtlantis.utils.accessController.web.utils.AuthenticationResourcesLoader;
import neoAtlantis.utils.apps.printer.exceptions.FormatterException;
import neoAtlantis.utils.apps.web.utils.ResourcesLoader;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public abstract class HtmlRoleAdministratorPrinter implements RoleAdministratorPrinter {

    @Override
    public Object printAddRole(Map<String,Object> params) throws FormatterException{
        StringBuilder sb = new StringBuilder("");
        
        if( params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)==null ){
            throw new FormatterException("No se proporciono el request para generar el objeto HTML.");
        }
        
        sb.append("<form name=\"NA:AddRoleForm\" action=\"").append(((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)).getContextPath()).append(((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)).getServletPath()).append("\" method=\"post\" class=\"NA_AddRole_form\">\n");
        sb.append(this.contentAddRole(params));
        sb.append("<input type=\"hidden\" name=\"").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("\" id=\"").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("\" value=\"").append(RoleAdministratorPrinter.CREATE_OPERATION).append("\" />\n");
        sb.append("</form>\n");
        
        return sb.toString();
    }
    
    public abstract String contentAddRole(Map<String,Object> params);
    
    @Override
    public Object printEditRole(Map<String,Object> params) throws FormatterException{
        StringBuilder sb = new StringBuilder("");
        
        if( params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)==null ){
            throw new FormatterException("No se proporciono el request para generar el objeto HTML.");
        }
        if( params.get(UtilsAuthenticatorBean.ROLE_PARAM)==null ){
            throw new FormatterException("No se proporciono el rol para generar el objeto HTML.");
        }
        
        sb.append("<form name=\"NA:EditRoleForm\" action=\"").append(((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)).getContextPath()).append(((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)).getServletPath()).append("\" method=\"post\" class=\"NA_EditRole_form\">\n");
        sb.append(this.contentEditRole(params));
        sb.append("<input type=\"hidden\" name=\"").append(UtilsAuthenticatorBean.ID_PARAM).append("\" id=\"").append(UtilsAuthenticatorBean.ID_PARAM).append("\" value=\"").append(((Role)params.get(UtilsAuthenticatorBean.ROLE_PARAM)).getId()).append("\" />\n");
        sb.append("<input type=\"hidden\" name=\"NA:RoleId\" id=\"NA:RoleId\" value=\"").append(((Role)params.get(UtilsAuthenticatorBean.ROLE_PARAM)).getId()).append("\" />\n");
        sb.append("<input type=\"hidden\" name=\"").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("\" id=\"").append(UtilsAuthenticatorBean.OPERATION_PARAM).append("\" value=\"").append(RoleAdministratorPrinter.UPDATE_OPERATION).append("\" />\n");
        sb.append("</form>\n");
        
        return sb.toString();        
    }
    
    public abstract String contentEditRole(Map<String,Object> params);
    
    @Override
    public Object printEditPermissions(Map<String,Object> params) throws FormatterException{
        StringBuilder sb = new StringBuilder("");
        HttpServletRequest request;
        
        if( params.get(SimpleHtmlLoginPrinter.REQUEST_KEY)==null ){
            throw new FormatterException("No se proporciono el request para generar el objeto HTML.");
        }
        if( params.get(UtilsAuthenticatorBean.ROLE_PARAM)==null ){
            throw new FormatterException("No se proporciono el rol para generar el objeto HTML.");
        }
        //obtengo el request
        request=((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY));
        
        sb.append("<script src=\"").append( request.getContextPath() ).append(ResourcesLoader.PATH_UTILS_JS).append("\"></script>\n");
        sb.append("<script src=\"").append( request.getContextPath() ).append(AuthenticationResourcesLoader.PATH_ADMIN_USERS_JS).append("\"></script>\n");
        sb.append(this.contentEditPermissions(params));
        
        return sb.toString();        
    }

    public abstract String contentEditPermissions(Map<String,Object> params);
    
}
