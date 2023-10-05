package neoAtlantis.utils.accessController.authentication.interfaces;

/**
 * Enumeraci&oacute;n que define los diferentes resultados que se pueden generar
 * al efectuar una validaci&oacute;n.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public enum ValidationResult {
    /**
     * Constante que indica un acceso denegado
     */
    DENIED,
    /**
     * Constante que indica un acceso denegado, generado explicitamente desde el código
     */
    ALTER_DENIED,
    /**
     * Constante que indica que el usuario no esta aprovisionado
     */
    NOT_PROVISIONED,
    /**
     * Constante que indica un acceso autorizado
     */
    VALIDATE,
    /**
     * Constante que indica un acceso autorizado, pero para cambio de contraseña
     */
    TEMPORAL_VALIDATE,
    /**
     * Constante que indica un acceso autorizado, pero con contrase&tilde;a caducada
     */
    LAPSED,
    /**
     * Constante que indica un limite revasado
     */
    EXCEED_LIMIT,
    /**
     * Constante que indica un usuario bloqueado
     */
    BLOCKED,
    /**
     * Constante que indica un usuario conectado
     */
    IN_USE,
    /**
     * Constante que indica un acceso fuera de tiempo (en base a los Calendarios de Disponibilidad)
     * @see neoAtlantis.utilidades.accessController.scheduler.interfaces.SchedulerWay
     */
    OUTTIME,
    /**
     * Constante que indica un usuario no existente
     */
    NOT_FOUND,
    /**
     * Constante que indica un usuario no activo
     */
    INACTIVE,
    /**
     * Constante que indica un error en la clave de confirmaci&oacute;n
     */
    CODE_ERROR,
    /**
     * Constante que indica un por falta de usuario
     */
    NOT_USER
}
