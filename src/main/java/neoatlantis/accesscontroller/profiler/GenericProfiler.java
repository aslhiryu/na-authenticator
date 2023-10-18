package neoatlantis.accesscontroller.profiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import neoatlantis.accesscontroller.exceptions.WayAccessException;
import neoatlantis.accesscontroller.objects.Role;
import neoatlantis.accesscontroller.objects.User;
import neoatlantis.accesscontroller.profiler.interfaces.ProfilerWay;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class GenericProfiler extends ProfilerWay {
    protected static ArrayList<Role> roles=new ArrayList<Role>();

    static{
        roles.add(new Role("USERS"));
    }
    
    @Override
    public List<Role> getRolesFromUser(User user, Object... param) throws WayAccessException {
        return roles;
    }

    @Override
    public boolean canAsignRoles() throws WayAccessException {
        return false;
    }

    @Override
    public List<Role> getRegisteredRoles() throws WayAccessException {
        return roles;
    }

    @Override
    public List<Role> getActivedRoles() throws WayAccessException {
        return roles;
    }

    @Override
    public boolean canEditRoles() throws WayAccessException {
        return false;
    }

    @Override
    public Map<String, Object> createRole(Map<String, Object> datos) throws WayAccessException {
        return null;
    }

    @Override
    public Map<String, Object> updateRole(Map<String, Object> datos) throws WayAccessException {
        return null;
    }

    @Override
    public boolean asignRoleToUser(String idUser, String idRole) throws WayAccessException {
        return false;
    }

    @Override
    public boolean removeRoleFromUser(String idUser, String idRole) throws WayAccessException {
        return false;
    }
    
    @Override
    public boolean allowsUpdateRoles() throws WayAccessException{
        return false;
    }
    
    @Override
    public boolean allowsAsignPermissions() throws WayAccessException{
        return false;
    }

    @Override
    public Role getRoleData(String id) throws WayAccessException {
        return new Role("USERS");
    }
}
