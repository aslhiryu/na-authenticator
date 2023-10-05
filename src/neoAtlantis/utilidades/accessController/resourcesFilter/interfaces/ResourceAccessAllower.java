package neoAtlantis.utilidades.accessController.resourcesFilter.interfaces;

import java.util.*;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.objects.Resource;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.resourcesFilter.AccessEstatus;

/**
 * Interface que define el comportamiento con el que debe contar un Validador de 
 * Accesos a Recursos.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.2
 */
public abstract class ResourceAccessAllower {
    /**
     * Variable que determina si el acceso a los recurso va a ser restrictivo
     */
    protected boolean restrictivo=false;
    /**
     * Lista de recursos
     */
    protected List<Resource> recursos;

    /**
     * Constructor base de ResourceAccessAllower
     * @param restrictivo true si desea que el acceso a los recursos sea restrictivo
     */
    public ResourceAccessAllower(boolean restrictivo){
        this.restrictivo=restrictivo;
        this.recursos=new ArrayList<Resource>();
    }

    /**
     * Recupera el valor de restrictivo
     * @return true si es restrictivo
     */
    public boolean isRestrictivo(){
        return this.restrictivo;
    }

    /**
     * Definici&oacute; del metodo que valida el acceso a un recurso.
     * @param user Usuario que intenta acceder al recurso
     * @param recurso Recurso a validar
     * @return Estatus del acceso
     * @throws WayAccessException 
     */
    public abstract AccessEstatus validaAcceso(User user, String recurso) throws WayAccessException;
}
