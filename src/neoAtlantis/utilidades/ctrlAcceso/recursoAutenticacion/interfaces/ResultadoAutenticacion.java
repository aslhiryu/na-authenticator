package neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.interfaces;

/**
 *
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public enum ResultadoAutenticacion {
    /**
     * Constante que indica un acceso denegado
     */
    ACCESO_DENEGADO,
    /**
     * Constante que indica un acceso autorizado
     */
    ACCESO_AUTORIZADO,
    /**
     * Constante que indica un acceso autorizado, pero para cambio de contraseña
     */
    ACCESO_AUTORIZADO_TEMPORAL,
    /**
     * Constante que indica un acceso autorizado, pero con contrase&tilde;a caducada
     */
    //ACCESO_AUTORIZADO_CADUCADO,
    /**
     * Constante que indica un limite revasado
     */
    LIMITE_REVASADO,
    /**
     * Constante que indica un usuario bloqueado
     */
    USUARIO_BLOQUEADO,
    /**
     * Constante que indica un usuario conectado
     */
    USUARIO_CONECTADO,
    /**
     * Constante que indica un acceso fuera de tiempo (en base a los Calendarios de Disponibilidad)
     * @see neoAtlantis.utilidades.ctrlAcceso.disponibilidad.interfaces.Disponibilidad
     */
    ACCESO_FUERA_DE_TIEMPO,
    /**
     * Constante que indica un usuario no existente
     */
    USUARIO_NO_ENCONTRADO,
    /**
     * Constante que indica un usuario no activo
     */
    USUARIO_NO_ACTIVO
}
