package neoatlantis.accesscontroller.profiler.interfaces;

import java.util.List;
import java.util.Map;
import neoatlantis.accesscontroller.exceptions.WayAccessException;
import neoatlantis.accesscontroller.objects.Role;
import neoatlantis.accesscontroller.objects.User;

/**
 * Interface que define el comportamiento con el que debe contar un Medio Perfilador
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 3.0
 */
public abstract class ProfilerWay {
    public static final String ROLE_DATA="NA:RoleName";
    public static final String ID_DATA="NA:RoleId";

    
    /**
     * Definici&oacute;n del metodo que recupera los roles de un usuario.
     * @param user Usuario del que se desean los roles
     * @param param Paramatros para obtener los roles
     * @return Lista de roles
     * @throws WayAccessException 
     */
    public abstract List<Role> getRolesFromUser(User user, Object... param) throws WayAccessException;
    
    public abstract boolean canAsignRoles() throws WayAccessException;
    
    public abstract List<Role> getRegisteredRoles() throws WayAccessException;
    
    public abstract List<Role> getActivedRoles() throws WayAccessException;
    
    public abstract boolean canEditRoles() throws WayAccessException;
    
    public abstract Map<String, Object> createRole(Map<String, Object> datos) throws WayAccessException;
    
    public abstract Map<String, Object> updateRole(Map<String, Object> datos) throws WayAccessException;
    
    public abstract boolean asignRoleToUser(String idUser, String idRole) throws WayAccessException;
    
    public abstract boolean removeRoleFromUser(String idUser, String idRole) throws WayAccessException;
    
    public abstract boolean allowsUpdateRoles() throws WayAccessException;
    
    public abstract boolean allowsAsignPermissions() throws WayAccessException;
    
    public abstract Role getRoleData(String id) throws WayAccessException;
}
