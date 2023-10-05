package neoAtlantis.utilidades.accessController.audit.interfaces;

/**
 * Enumeraci&oacute;n que define los niveles de bitacorizaci&oacute;n.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public enum LevelAudit {
    /**
     * Constante que define el nulo registro en la bitacora.
     */
    NULA,
    /**
     * Constante que define el registro unicamente de los accesos en la bitacora.
     */
    ACCESO,
    /**
     * Constante que define el registro, unicamente, de los accesos y errores de acceso en la bitacora.
     */
    BASICA,
    /**
     * Constante que define el registro de todos los eventos de acceso y de negocio personalizado en la bitacora.
     */
    NEGOCIO,
    /**
     * Constante que define el registro de todos los eventos en la bitacora.
     */
    COMPLETA
}
