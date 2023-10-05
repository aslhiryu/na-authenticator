package neoAtlantis.utilidades.accessController.objects;

/**
 * Enumeraci&oacute;n que define los tipos de terminales.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public enum EnvironmentType {
    /**
     * Constante que indica una terminal WEB.
     */
    WEB{@Override public String toString(){return "WEB";}},
    /**
     * Constante que indica una terminal StandAlone.
     */
    STANDALONE{@Override public String toString(){return "STANDALONE";}},
    /**
     * Constante que indica una terminal StandAlone.
     */
    MOVIL{@Override public String toString(){return "MOVIL";}}
}
