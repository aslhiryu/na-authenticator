package neoatlantis.accesscontroller.printer.interfaces;

import java.util.Map;
import neoatlantis.applications.printer.exceptions.FormatterException;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public interface UserAdministratorPrinter {
    public static final String ID_DATA="NA:ID";
    public static final String ERROR_DATA="NA:ERROR";
    
    public static final String ADD_OPERATION="addUser";
    public static final String CREATE_OPERATION="createUser";
    public static final String EDIT_OPERATION="editUser";
    public static final String UPDATE_OPERATION="updateUser";
    public static final String ROLES_OPERATION="editRoles";

    public static final String PAGE_PARAM="NA_DataPage";
    public static final String ORDER_PARAM="NA_DataOrder";
    public static final String MODE_ORDER_PARAM="NA_DataModeOrder";

    public Object printUsersList(Map<String,Object> params) throws FormatterException;
    public Object printAddUser(Map<String,Object> params) throws FormatterException;
    public Object printEditUser(Map<String,Object> params) throws FormatterException;
    public Object printEditRoles(Map<String,Object> params) throws FormatterException;
}
