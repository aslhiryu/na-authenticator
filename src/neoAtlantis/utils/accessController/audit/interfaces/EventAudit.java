package neoAtlantis.utils.accessController.audit.interfaces;

/**
 * Enumeraci&oacute;n que define los eventos que se pueden generar por la bitacora
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public enum EventAudit {
    /**
     * Constante que indica un evento de administración.
     */
    ADMIN("Evento de administración"){@Override public String toString(){return "D";}},
    /**
     * Constante que indica un evento de intento de logeo.
     */
    LOGGED("Evento de intento de ingreso"){@Override public String toString(){return "L";}},
    /**
     * Constante que indica un evento de rechazo de logeo.
     */
    DENIED("Evento de declinado o rechazo"){@Override public String toString(){return "R";}},
    /**
     * Constante que indica un evento de bloqueo de logeo.
     */
    BLOCKED("Evento de bloqueo por intentos fallidos"){@Override public String toString(){return "B";}},
    /**
     * Constante que indica un evento de acceso a recusos.
     */
    RESOURCE_ACCESS("Evento de acceso a recurso"){@Override public String toString(){return "A";}},
    /**
     * Constante que indica un evento de ingreso a un aplicativo.
     */
    ENTRY("Evento de ingreso al aplicativo"){@Override public String toString(){return "I";}},
    /**
     * Constante que indica un evento de egreso de un aplicativo.
     */
    EXIT("Evento de salida del aplicativo"){@Override public String toString(){return "O";}},
    /**
     * Constante que indica un evento de negocio.
     */
    BUSSINESS("Evento de negocio"){@Override public String toString(){return "N";}};
    
    
    
    //personalizacion de la enumeracion
    private final String detail;
    
    private EventAudit(String detalle){
        this.detail=detalle;
    }
    
    public String getDetail(){
        return this.detail;
    }
}
