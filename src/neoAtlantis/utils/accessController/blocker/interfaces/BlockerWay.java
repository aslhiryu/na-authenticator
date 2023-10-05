package neoAtlantis.utils.accessController.blocker.interfaces;

import java.util.List;
import neoAtlantis.utils.accessController.exceptions.WayAccessException;
import neoAtlantis.utils.accessController.objects.User;

/**
 * Interface que define el comportamiento con el que debe contar un Medio Bloqueador
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.0
 */
public abstract class BlockerWay {
    /**
     * Constante de indica un segundo en milisegundos
     */
    public static final long SECOND_IN_MILLIS = 1000;
    /**
     * Constante de indica un minuto en milisegundos
     */
    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    /**
     * Constante de indica una hora en milisegundos
     */
    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    /**
     * Constante de indica und&iacute;a en milisegundos
     */
    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
    /**
     * Constante de indica una semana en milisegundos
     */
    public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;
    /**
     * Variable que indica el modo de bloqueo utilizado.
     */
    protected BlockType modoBloqueo = BlockType.USUARIO;
    /**
     * Variable que indica el tiempo de bloqueo a utilizar.
     */
    protected long tiempoBloqueo = DAY_IN_MILLIS;
    /**
     * Variable que indica el tiempo de sesion activa.
     */
    protected long tiempoSesion = MINUTE_IN_MILLIS*10;

    /**
     * Definici&oacute;n del metodo para agregar bloqueos de usuario.
     * @param user Usuario a bloquear
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract void addBlock(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para revisar los bloqueos que ya hayan finalizado.
     * @return Colecci&oacute;n con los nombres de los usuarios de los cuales termino su bloqueo.
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract List<User> validateEndedBlocks() throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para revisar las sesiones que ya hayan finalizado.
     * @return Colecci&oacute;n con los usuarios que expiraron sus sesiones.
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract List<User> validateInactiveSessions() throws WayAccessException;

    /**
     * Definici&oacute;n del metodo que revisa si un usuario esta bloqueado.
     * @param user Usuario del que se desea verificar su bloqueo
     * @return true si existe bloqueo
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract boolean validateBlock(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para remover bloqueos de usuario.
     * @param user Usuario del que se desea terminar el bloqueo
     * @return true si se logro remover el bloqueo
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract boolean removeBlock(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para agregar la conexi&oacute;n de un usuario.
     * @param user Usuario del que se desea registrar su conexi&oacute;n
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract void addConnection(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para remover la conexi&oacute;n de un usuario.
     * @param user Usuario del que se desea remover su conexi&oacute;n
     * @return true si se logro remover la conexi&oacute;n
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract boolean removeConnection(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo que revisa si un usuario esta conectado.
     * @param user Usuario del que se desea revisar su conexi&oacute;n
     * @return true si tiene una conexi&oacute;n activa
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract boolean validateConnection(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para recuperar los datos del usuario conectado solicitado
     * @param id Identificador del usuario
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract User getConnectedUser(String id) throws WayAccessException;
    
    /**
     * Definici&oacute;n del metodo para recuperar los datos del usuario conectado por su sesi&oacute;n
     * @param session Sesi&oacute;n del usuario
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract User getConnectedUserBySession(String session) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para recuperar los datos del usuario bloqueado solicitado
     * @param id Identificador del usuario
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract User getBlockedUser(String id) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para actualizar la actividad de la conexion existente
     * @param user  Usuario del que se desea actualizar el estado de la conexi&oacute;n
     * @throws neoAtlantis.utils.accessController.exceptions.WayAccessException
     */
    public abstract void updateActivity(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para recuperar la lista de conecionex activas
     * @return Lista de conexiones
     * @throws WayAccessException 
     */
    public abstract List<User> getConnections() throws WayAccessException;
    
    //------------------------------------------------------------------------

    /**
     * Asigna el tipo de bloqueo a utilizar
     * @param modoBloqueo Tipo de bloqueo:
     */
    public void setBlockType(BlockType modoBloqueo) {
        this.modoBloqueo=modoBloqueo;
    }

    /**
     * Asigna el tiempo de duración de un bloqueo.
     * @param tiempoBloqueo Tiempo en milisegundos que durar&aacute; un bloqueo
     */
    public void setBlockTime(long tiempoBloqueo) {
        this.tiempoBloqueo = tiempoBloqueo;
    }

    /**
     * Asigna el tiempo de duración de un bloqueo.
     * @param tiempoSesion Tiempo en milisegundos que durar&aacute; un bloqueo
     */
    public void setSessionTime(long tiempoSesion) {
        this.tiempoSesion = tiempoSesion;
    }
    
    /**
     * Definici&oacute;n del metodo para recuperar la lista de usuarios bloqueados
     * @return Lista de usuarios
     * @throws WayAccessException 
     */
    public abstract List<User> getBlocked() throws WayAccessException;

    public long getSessionTime(){
        return this.tiempoSesion;
    }

    public long getBlockTime(){
        return this.tiempoBloqueo;
    }
}
