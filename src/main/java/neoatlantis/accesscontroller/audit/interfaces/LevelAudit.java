package neoAtlantis.utils.accessController.audit.interfaces;

/**
 * Enumeraci&oacute;n que define los niveles de bitacorizaci&oacute;n.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public enum LevelAudit {
    /**
     * Constante que define el nulo registro en la bitacora.
     */
    NULL,
    /**
     * Constante que define el registro unicamente de los accesos en la bitacora.
     */
    ACCESS,
    /**
     * Constante que define el registro, unicamente de los accesos y errores de acceso en la bitacora.
     */
    BASIC,
    /**
     * Constante que define el registro, unicamente de movimientos administrativos.
     */
    ADMIN,
    /**
     * Constante que define el registro de todos los eventos de acceso y de negocio personalizado en la bitacora.
     */
    BUSINESS,
    /**
     * Constante que define el registro de todos los eventos en la bitacora.
     */
    FULL
}
