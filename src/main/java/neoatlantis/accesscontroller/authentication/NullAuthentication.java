package neoatlantis.accesscontroller.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import neoatlantis.accesscontroller.authentication.interfaces.AuthenticationWay;
import neoatlantis.accesscontroller.exceptions.WayAccessException;
import neoatlantis.accesscontroller.objects.EnvironmentType;
import neoatlantis.accesscontroller.objects.User;
import neoatlantis.utils.catalogs.objects.OrderMode;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class NullAuthentication extends AuthenticationWay {
    public static final String ID="idUser";
    public static final String USER="loginUser";
    

    @Override
    public User authenticateUser(Map<String, Object> datos) throws WayAccessException {
        User u=new User((String)datos.get(ID), (String)datos.get(USER), "127.0.0.1", "localhost", EnvironmentType.WEB);
        
        return u;
    }

    @Override
    public String additionalValidation(User usuario) throws WayAccessException {
        return null;
    }

    @Override
    public List<User> getRegisteredUserList(String order, OrderMode orderType) throws WayAccessException {
        return new ArrayList<User>();
    }

    @Override
    public Map<String, Object> createUser(Map<String, Object> datos) throws WayAccessException {
        return new HashMap<String, Object>();
    }

    @Override
    public Map<String, Object> updateUser(Map<String, Object> datos) throws WayAccessException {
        return new HashMap<String, Object>();
    }

    @Override
    public User getUserData(String id) throws WayAccessException {
        return null;
    }

    @Override
    public boolean allowsUpdatePassword() throws WayAccessException {
        return false;
    }

    @Override
    public boolean allowsUpdateUser() throws WayAccessException {
        return false;
    }

    @Override
    public boolean allowsCreateUser() throws WayAccessException {
        return false;
    }

    @Override
    public boolean activeUser(String id, boolean active) throws WayAccessException {
        return true;
    }

    @Override
    public boolean updateLastAccess(String id) throws WayAccessException {
        return true;
    }

    @Override
    public List<User> validateLoginLifes() throws WayAccessException {
        return new ArrayList<User>();
    }
    
}
