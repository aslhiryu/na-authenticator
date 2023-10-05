package neoAtlantis.utilidades.ctrlAcceso.recursoBloqueo.interfaces;

/**
 *
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public enum TipoBloqueo {
    /**
     * Constante de indica un bloqueo por usuario
     */
    USUARIO{@Override public String toString(){return "U";}},
    /**
     * Constante de indica un bloqueo por ip
     */
    IP{@Override public String toString(){return "I";}},
    /**
     * Constante de indica un bloqueo por terminal
     */
    TERMINAL{@Override public String toString(){return "T";}}
}
