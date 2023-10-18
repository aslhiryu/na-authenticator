package neoAtlantis.utils.accessController.printer.interfaces;

import java.util.Map;
import neoAtlantis.utils.apps.printer.exceptions.FormatterException;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public interface RoleAdministratorPrinter {
    public static final String ADD_OPERATION="addRole";
    public static final String CREATE_OPERATION="createRole";
    public static final String EDIT_OPERATION="editRole";
    public static final String UPDATE_OPERATION="updateRole";
    public static final String PERMISSIONS_OPERATION="editPermissions";
    
    public Object printRolesList(Map<String,Object> params) throws FormatterException;
    public Object printAddRole(Map<String,Object> params) throws FormatterException;
    public Object printEditRole(Map<String,Object> params) throws FormatterException;
    public Object printEditPermissions(Map<String,Object> params) throws FormatterException;
}
