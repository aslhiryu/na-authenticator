package neoAtlantis.utilidades.accessController.blocker.interfaces;

import java.util.List;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.objects.User;

/**
 * Interface que define el comportamiento con el que debe contar un Medio Bloqueador
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.0
 */
public abstract class BlockerWay {
    /**
     * Constante de indica un segundo en milisegundos
     */
    public static final long SEGUNDO_EN_MILIS = 1000;
    /**
     * Constante de indica un minuto en milisegundos
     */
    public static final long MINUTO_EN_MILIS = SEGUNDO_EN_MILIS * 60;
    /**
     * Constante de indica una hora en milisegundos
     */
    public static final long HORA_EN_MILIS = MINUTO_EN_MILIS * 60;
    /**
     * Constante de indica und&iacute;a en milisegundos
     */
    public static final long DIA_EN_MILIS = HORA_EN_MILIS * 24;
    /**
     * Constante de indica una semana en milisegundos
     */
    public static final long SEMANA_EN_MILIS = DIA_EN_MILIS * 7;
    /**
     * Variable que indica el modo de bloqueo utilizado.
     */
    protected BlockType modoBloqueo = BlockType.USUARIO;
    /**
     * Variable que indica el tiempo de bloqueo a utilizar.
     */
    protected long tiempoBloqueo = DIA_EN_MILIS;
    /**
     * Variable que indica el tiempo de sesion activa.
     */
    protected long tiempoSesion = MINUTO_EN_MILIS*10;

    /**
     * Definici&oacute;n del metodo para agregar bloqueos de usuario.
     * @param user Usuario a bloquear
     * @throws java.lang.WayAccessException
     */
    public abstract void agregaBloqueo(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para revisar los bloqueos que ya hayan finalizado.
     * @return Colecci&oacute;n con los nombres de los usuarios de los cuales termino su bloqueo.
     * @throws java.lang.WayAccessException
     */
    public abstract List<User> revisaBloqueosTerminados() throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para revisar las sesiones que ya hayan finalizado.
     * @return Colecci&oacute;n con los usuarios que expiraron sus sesiones.
     * @throws java.lang.WayAccessException
     */
    public abstract List<User> revisaSesionesInactivas() throws WayAccessException;

    /**
     * Definici&oacute;n del metodo que revisa si un usuario esta bloqueado.
     * @param user Usuario del que se desea verificar su bloqueo
     * @return true si existe bloqueo
     * @throws java.lang.WayAccessException
     */
    public abstract boolean verificaBloqueo(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para remover bloqueos de usuario.
     * @param user Usuario del que se desea terminar el bloqueo
     * @return true si se logro remover el bloqueo
     * @throws java.lang.WayAccessException
     */
    public abstract boolean remueveBloqueo(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para agregar la conexi&oacute;n de un usuario.
     * @param user Usuario del que se desea registrar su conexi&oacute;n
     * @throws java.lang.WayAccessException
     */
    public abstract void agregaConexion(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para remover la conexi&oacute;n de un usuario.
     * @param user Usuario del que se desea remover su conexi&oacute;n
     * @return true si se logro remover la conexi&oacute;n
     * @throws java.lang.WayAccessException
     */
    public abstract boolean remueveConexion(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo que revisa si un usuario esta conectado.
     * @param user Usuario del que se desea revisar su conexi&oacute;n
     * @return true si tiene una conexi&oacute;n activa
     * @throws java.lang.WayAccessException
     */
    public abstract boolean verificaConexion(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para recuperar los datos del usuario conectado solicitado
     * @param id Identificador del usuario
     * @throws java.lang.WayAccessException
     */
    public abstract User getUsuarioConectado(String id) throws WayAccessException;
    
    /**
     * Definici&oacute;n del metodo para recuperar los datos del usuario conectado por su sesi&oacute;n
     * @param session Sesi&oacute;n del usuario
     * @throws java.lang.WayAccessException
     */
    public abstract User getUsuarioConectadoSesion(String session) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para recuperar los datos del usuario bloqueado solicitado
     * @param id Identificador del usuario
     * @throws java.lang.WayAccessException
     */
    public abstract User getUsuarioBloqueado(String id) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para actualizar la actividad de la conexion existente
     * @param user  Usuario del que se desea actualizar el estado de la conexi&oacute;n
     * @throws java.lang.WayAccessException
     */
    public abstract void actualizaActividadConexion(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para recuperar la lista de conecionex activas
     * @return Lista de conexiones
     * @throws WayAccessException 
     */
    public abstract List<User> getConexiones() throws WayAccessException;
    
    //------------------------------------------------------------------------

    /**
     * Asigna el tipo de bloqueo a utilizar
     * @param modoBloqueo Tipo de bloqueo:
     */
    public void setModoBloqueo(BlockType modoBloqueo) {
        this.modoBloqueo=modoBloqueo;
    }

    /**
     * Asigna el tiempo de duración de un bloqueo.
     * @param tiempoBloqueo Tiempo en milisegundos que durar&aacute; un bloqueo
     */
    public void setTiempoBloqueo(long tiempoBloqueo) {
        this.tiempoBloqueo = tiempoBloqueo;
    }

    /**
     * Asigna el tiempo de duración de un bloqueo.
     * @param tiempoSesion Tiempo en milisegundos que durar&aacute; un bloqueo
     */
    public void setTiempoSesion(long tiempoSesion) {
        this.tiempoSesion = tiempoSesion;
    }
    
    public long getTiempoSesion(){
        return this.tiempoSesion;
    }
}
