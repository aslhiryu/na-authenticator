package neoAtlantis.utilidades.accessController.profiler;

import java.util.ArrayList;
import java.util.List;
import neoAtlantis.utilidades.accessController.objects.Role;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.profiler.interfaces.ProfilerWay;

/**
 * Medio Perfilador nulo, utilizado para nulificar las operiones desencadenadas al 
 * medio por el {@link neoAtlantis.utilidades.accessController.AccessController Control de Autenticaci&oacute;n}
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class NullProfiler extends ProfilerWay {

    /**
     * Recupera los roles de un usuario.
     * @param user Usuario del que se desean los roles
     * @param param Paramatros para obtener los roles
     * @return Lista de roles
     */
    @Override
    public List<Role> obtieneRoles(User user, Object... param) {
        return new ArrayList();
    }

    /**
     * Asigna un rol a un usuario.
     * @param user Usuario al que se desea asignar el rol
     * @param rol Rol a asignar
     * @param param Parametors para asignar los roles
     * @return true si se logro asignar 
     */
    @Override
    public boolean asignaRol(User user, Role rol, Object... param) {
        return true;
    }

    /**
     * Remueve un rol a un usuario.
     * @param user Usuario al que se desea remover el rol
     * @param rol Rol a remover
     * @param param Parametors para remover los roles
     * @return true si se logro remover 
     */    
    @Override
    public boolean remueveRol(User user, Role rol, Object... param) {
        return true;
    }

}
