package neoAtlantis.utilidades.accessController.profiler.interfaces;

import java.util.List;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.objects.Role;
import neoAtlantis.utilidades.accessController.objects.User;

/**
 * Interface que define el comportamiento con el que debe contar un Medio Perfilador
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 3.0
 */
public abstract class ProfilerWay {
    /**
     * Definici&oacute;n del metodo que recupera los roles de un usuario.
     * @param user Usuario del que se desean los roles
     * @param param Paramatros para obtener los roles
     * @return Lista de roles
     * @throws WayAccessException 
     */
    public abstract List<Role> obtieneRoles(User user, Object... param) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para asignar un rol a un usuario.
     * @param user Usuario al que se desea asignar el rol
     * @param rol Rol a asignar
     * @param param Parametors para asignar los roles
     * @return true si se logro asignar 
     * @throws WayAccessException 
     */
    public abstract boolean asignaRol(User user, Role rol, Object... param) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para remover un rol a un usuario.
     * @param user Usuario al que se desea remover el rol
     * @param rol Rol a remover
     * @param param Parametors para remover los roles
     * @return true si se logro remover 
     * @throws WayAccessException 
     */    
    public abstract boolean remueveRol(User user, Role rol, Object... param) throws WayAccessException;

}
