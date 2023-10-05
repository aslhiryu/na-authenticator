package neoAtlantis.utilidades.accessController.audit.interfaces;

/**
 * Enumeraci&oacute;n que define los eventos que se pueden generar por la bitacora
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public enum EventAudit {
    /**
     * Constante que indica un evento de intento de logeo.
     */
    LOGEO{@Override public String toString(){return "L";}},
    /**
     * Constante que indica un evento de rechazo de logeo.
     */
    RECHAZO{@Override public String toString(){return "R";}},
    /**
     * Constante que indica un evento de bloqueo de logeo.
     */
    BLOQUEO{@Override public String toString(){return "B";}},
    /**
     * Constante que indica un evento de acceso a recusos.
     */
    ACCESO{@Override public String toString(){return "A";}},
    /**
     * Constante que indica un evento de ingreso a un aplicativo.
     */
    INGRESO{@Override public String toString(){return "I";}},
    /**
     * Constante que indica un evento de egreso de un aplicativo.
     */
    EGRESO{@Override public String toString(){return "O";}},
    /**
     * Constante que indica un evento de negocio.
     */
    NEGOCIO{@Override public String toString(){return "N";}}
}
