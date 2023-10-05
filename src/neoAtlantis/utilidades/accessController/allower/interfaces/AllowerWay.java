package neoAtlantis.utilidades.accessController.allower.interfaces;

import java.util.List;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.objects.Permission;
import neoAtlantis.utilidades.accessController.objects.User;

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
    public abstract List<Permission> obtienePermisos(User user, Object... params) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para asignar un permisos a un usuario.
     * @param user Usuario al que se desea asignar el permiso
     * @param permiso Permiso a asignar
     * @return true si se logro asignar, o en caso contrario false
     * @throws WayAccessException 
     */
    public abstract boolean asignaPermiso(User user, Permission permiso) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para remover un permisos a un usuario.
     * @param user Usuario al que se desea remover el permiso
     * @param permiso Permiso a remover
     * @return true si se logro remover, o en caso contrario false
     * @throws WayAccessException 
     */
    public abstract boolean remuevePermiso(User user, Permission permiso) throws WayAccessException;

}
