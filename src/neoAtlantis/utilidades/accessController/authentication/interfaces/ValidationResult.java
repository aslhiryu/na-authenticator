package neoAtlantis.utilidades.accessController.authentication.interfaces;

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
    DENEGADO,
    /**
     * Constante que indica un acceso denegado
     */
    DENEGADO_PROPIO,
    /**
     * Constante que indica un acceso autorizado
     */
    VALIDADO,
    /**
     * Constante que indica un acceso autorizado, pero para cambio de contraseña
     */
    VALIDADO_TEMPORAL,
    /**
     * Constante que indica un acceso autorizado, pero con contrase&tilde;a caducada
     */
    CADUCADO,
    /**
     * Constante que indica un limite revasado
     */
    LIMITE_REBASADO,
    /**
     * Constante que indica un usuario bloqueado
     */
    BLOQUEADO,
    /**
     * Constante que indica un usuario conectado
     */
    EN_USO,
    /**
     * Constante que indica un acceso fuera de tiempo (en base a los Calendarios de Disponibilidad)
     * @see neoAtlantis.utilidades.accessController.scheduler.interfaces.SchedulerWay
     */
    FUERA_DE_TIEMPO,
    /**
     * Constante que indica un usuario no existente
     */
    NO_ENCONTRADO,
    /**
     * Constante que indica un usuario no activo
     */
    INACTIVO,
    /**
     * Constante que indica un error en la clave de confirmaci&oacute;n
     */
    ERROR_CODIGO,
    /**
     * Constante que indica un por falta de usuario
     */
    SIN_USUARIO
}
