package neoatlantis.accesscontroller.blocker.interfaces;

/**
 * Enumeraci&oacute;n que define los tipos de bloqueo que se pueden realizar.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public enum BlockType {
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
    TERMINAL{@Override public String toString(){return "T";}},
    /**
     * Constante de indica un bloqueo por ip e usuario
     */
    IP_USUARIO{@Override public String toString(){return "D";}}
}
