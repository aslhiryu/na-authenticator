package neoAtlantis.utilidades.ctrlAcceso.recursoBitacora.interfaces;

/**
 *
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public enum EventoBitacora {
    /**
     * Constante que indica un evento de logeo.
     */
    LOGEO{@Override public String toString(){return "L";}},
    /**
     * Constante que indica un evento de rechazo.
     */
    RECHAZADO{@Override public String toString(){return "R";}},
    /**
     * Constante que indica un evento de bloqueo.
     */
    BLOQUEADO{@Override public String toString(){return "B";}},
    /**
     * Constante que indica un evento de acceso.
     */
    ACCESO{@Override public String toString(){return "A";}},
    /**
     * Constante que indica un evento de validaci&oacute;n.
     */
    VALIDADO{@Override public String toString(){return "V";}}
}
