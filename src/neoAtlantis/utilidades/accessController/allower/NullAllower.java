package neoAtlantis.utilidades.accessController.allower;

import java.util.ArrayList;
import java.util.List;
import neoAtlantis.utilidades.accessController.allower.interfaces.AllowerWay;
import neoAtlantis.utilidades.accessController.objects.Permission;
import neoAtlantis.utilidades.accessController.objects.User;

/**
 * Medio Permisor nulo, utilizado para nulificar las operiones desencadenadas al 
 * medio por el {@link neoAtlantis.utilidades.accessController.AccessController Control de Autenticaci&oacute;n}
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 3.0
 */
public class NullAllower extends AllowerWay {

    /**
     * Obtiene los permisos de un usuario.
     * @param user Usuario del que se desean los permisos
     * @param params Informaci&oacute;n para obtener los permisos
     * @return Lista de permisos
     * @throws WayAccessException 
     */
    @Override
    public List<Permission> obtienePermisos(User user, Object... params) {
        return new ArrayList();
    }

    /**
     * Asigna un permisos a un usuario.
     * @param user Usuario al que se desea asignar el permiso
     * @param permiso Permiso a asignar
     * @return true si se logro asignar, o en caso contrario false
     * @throws WayAccessException 
     */
    @Override
    public boolean asignaPermiso(User user, Permission permiso) {
        return true;
    }

    /**
     * Remueve un permisos a un usuario.
     * @param user Usuario al que se desea remover el permiso
     * @param permiso Permiso a remover
     * @return true si se logro remover, o en caso contrario false
     * @throws WayAccessException 
     */
    @Override
    public boolean remuevePermiso(User user, Permission permiso) {
        return true;
    }

}
