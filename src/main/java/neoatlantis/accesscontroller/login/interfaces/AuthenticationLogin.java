package neoatlantis.accesscontroller.login.interfaces;

import java.util.Map;
import neoatlantis.accesscontroller.objects.User;

/**
 *Interface que define el comportamiento que debe considerar un login
 * @author Hiryu (aslhiryu@gmail.com)
 */
public interface AuthenticationLogin {
    public static final String REQUEST_WEB_KEY="na.authentication.elements.request";
    public static final String RESPONSE_WEB_KEY="na.authentication.elements.response";
    
    public User tryAccess(Map<String,Object> params);
    public void loadDataPostvalidation(User user, Map<String,Object> data);
}
