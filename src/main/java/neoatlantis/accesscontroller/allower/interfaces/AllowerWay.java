package neoAtlantis.utils.accessController.allower.interfaces;

import java.util.List;
import java.util.Map;
import neoAtlantis.utils.accessController.exceptions.WayAccessException;
import neoAtlantis.utils.accessController.objects.Permission;
import neoAtlantis.utils.accessController.objects.Role;
import neoAtlantis.utils.accessController.objects.User;

/**
 * Interface que define el comportamiento con el que debe contar un Medio Permisor
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 3.0
 */
public abstract class AllowerWay {
    /**
     * Definici&oacute;n del metodo para obtener los permisos de un usuario.
     * @param user Usuario del que se desean los permisos
     * @param params Informaci&oacute;n para obtener los permisos
     * @return Lista de permisos
     * @throws WayAccessException 
     */
    public abstract List<Permission> getRegisteredPermissions() throws WayAccessException;
    
    public abstract List<Permission> getActivedPermissions() throws WayAccessException;
    
    public abstract List<Permission> getAssignedPermissions(Role role, Map<String,Object> param) throws WayAccessException;
    
    public abstract List<Permission> getAssignedPermissions(User user, Map<String,Object> param) throws WayAccessException;
    
    public abstract boolean canEditPermissions() throws WayAccessException;
    
    public abstract boolean asignPermissionToRole(String idRole, String idPermission) throws WayAccessException;
    
    public abstract boolean removePermissionFromRole(String idRole, String idPermission) throws WayAccessException;
    
    public abstract Permission getPermissionData(String id) throws WayAccessException;
}
