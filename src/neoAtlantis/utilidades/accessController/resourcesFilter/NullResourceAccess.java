package neoAtlantis.utilidades.accessController.resourcesFilter;

import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower;

/**
 * Validador de Accesos a Recursos nulo, utilizado para nulificar las operiones 
 * desencadenadas al validador por el {@link neoAtlantis.utilidades.accessController.utils.SimpleValidatorFilter Validador de Acceso recursos Web}
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class NullResourceAccess extends ResourceAccessAllower {

    /**
     * Constructuor
     */
    public NullResourceAccess(){
        super(false);
    }

    /**
     * Valida el acceso a un recurso.
     * @param user Usuario que intenta acceder al recurso
     * @param recurso Recurso a validar
     * @return Estatus del acceso
     * @throws WayAccessException 
     */
    @Override
    public AccessEstatus validaAcceso(User user, String recurso) {
        return AccessEstatus.VALIDO;
    }

}
