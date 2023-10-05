package neoAtlantis.utilidades.accessController.blocker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockType;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay;
import neoAtlantis.utilidades.accessController.objects.User;
import org.apache.log4j.Logger;

/**
 * Medio Bloqueador operado a traves de objetos en memoria.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class MemoryBlocker extends BlockerWay {
    /**
     * Loggeador de la clase
     */
    static final Logger logger = Logger.getLogger(MemoryBlocker.class);

    private List<User> sesiones;
    private List<User> bloqueos;

    /**
     * Genera un BlockerWay por memoria
     * @param sesiones
     * @param bloqueos 
     */
    public MemoryBlocker(List<User> sesiones, List<User> bloqueos){
        this.sesiones=sesiones;
        this.bloqueos=bloqueos;
        
        logger.debug("Genero instancia de blocker: "+this.sesiones+", "+this.bloqueos);
    }

    //--------------------------------------------------------------------------
    
    
    /**
     * Agregar bloqueos de usuario en memoria.
     * @param user Usuario a bloquear
     */
    @Override
    public void agregaBloqueo(User user) {
        logger.debug("Bloqueos activos: "+this.bloqueos.size());
        if( user==null ){
            return;
        }

       this.bloqueos.add(user);
       logger.debug("Agrego el bloqueo para el usuario: "+user);
    }

    /**
     * Revisa lo bloqueos que ya hayan finalizado.
     * @return Colecci&oacute;n con los nombres de los usuarios de los cuales termino su bloqueo.
     */
    @Override
    public List<User> revisaBloqueosTerminados() {
        ArrayList<User> l=new ArrayList<User>();
        User u;

        logger.debug("Bloqueos activos: "+this.bloqueos.size());
        
        for(int i=this.bloqueos.size()-1; i>=0; i--){
            u=this.bloqueos.get(i);
            if( Calendar.getInstance().getTimeInMillis()>(u.getActividad().getTime()+this.tiempoBloqueo) ){
                l.add(u);
                this.bloqueos.remove(i);
                logger.debug("Finaliza el bloqueo por tiempo ("+this.tiempoBloqueo+") de: "+u);
            }
        }

       return l;
    }

    /**
     * Revisa si un usuario esta bloqueado.
     * @param user Usuario del que se desea verificar su bloqueo
     * @return true si existe bloqueo
     */
    @Override
    public boolean verificaBloqueo(User user) {
        logger.debug("Bloqueos activos: "+this.bloqueos.size());

        for(User u: this.bloqueos){
            if( this.modoBloqueo==BlockType.IP && u.getOrigen().equals(user.getOrigen()) ){
                logger.debug("El usuario '"+u.getUser()+"' esta bloqueado por IP.");
                return true;
            }
            else if(this.modoBloqueo == BlockType.USUARIO && u.getUser().equals(user.getUser())){
                logger.debug("El usuario '"+u.getUser()+"' esta bloqueado por usuario.");
                return true;
            }
            else if(this.modoBloqueo == BlockType.TERMINAL && u.getUser().equals(user.getUser()) && u.getTipoTerminal()==user.getTipoTerminal()){
                logger.debug("El usuario '"+u.getUser()+"' esta bloqueado por terminal.");
                return true;
            }
            else if(this.modoBloqueo == BlockType.IP_USUARIO && (u.getUser().equals(user.getUser()) || u.getOrigen().equals(user.getOrigen()))){
                logger.debug("El usuario '"+u.getUser()+"' esta bloqueado por usuario o ip.");
                return true;
            }
        }

        logger.debug("El usuario '"+user.getUser()+"' no esta bloqueado.");
       return false;
    }

    /**
     * Remueve el bloqueos de un usuario de memoria.
     * @param user Usuario del que se desea terminar el bloqueo
     * @return true si se logro remover el bloqueo
     */
    @Override
    public boolean remueveBloqueo(User user) {
        boolean rem=false;
        User u;

        logger.debug("Bloqueos activos: "+this.bloqueos.size());

        for(int i=this.bloqueos.size()-1; i>=0; i--){
            u=this.bloqueos.get(i);
            if( this.modoBloqueo==BlockType.IP && u.getOrigen().equals(user.getOrigen()) ){
                this.bloqueos.remove(i);
                rem=true;
                logger.debug("Remuevo el bloqueo de: "+u);
            }
            else if(this.modoBloqueo == BlockType.USUARIO && u.getUser().equals(user.getUser())){
                this.bloqueos.remove(i);
                rem=true;
                logger.debug("Remuevo el bloqueo de: "+u);
            }
            else if(this.modoBloqueo == BlockType.TERMINAL && u.getUser().equals(user.getUser()) && u.getTipoTerminal()==user.getTipoTerminal()){
                this.bloqueos.remove(i);
                rem=true;
                logger.debug("Remuevo el bloqueo de: "+u);
            }
            else if(this.modoBloqueo == BlockType.IP_USUARIO && (u.getUser().equals(user.getUser()) || u.getOrigen().equals(user.getOrigen()))){
                this.bloqueos.remove(i);
                rem=true;
                logger.debug("Remuevo el bloqueo de: "+u);
            }
        }

       return rem;
    }

    /**
     * Agrega la conexi&oacute;n de un usuario en memoria.
     * @param user Usuario del que se desea registrar su conexi&oacute;n
     */
    @Override
    public void agregaConexion(User user) {
        logger.debug("Conexiones activas: "+this.sesiones.size());
        if( user==null ){
            return;
        }

       this.sesiones.add(user);
        logger.debug("Agrego la conexion del usuario: "+user);
    }

    /**
     * Remueve la conexi&oacute;n de un usuario de memoria.
     * @param user Usuario del que se desea remover su conexi&oacute;n
     * @return true si se logro remover la conexi&oacute;n
     */
    @Override
    public boolean remueveConexion(User user) {
        User u;

        logger.debug("Conexiones activas: "+this.sesiones.size());
        for(int i=this.sesiones.size()-1; i>=0; i--){
            u=this.sesiones.get(i);
            if( u.getUser().equals(user.getUser()) && u.getOrigen().equals(user.getOrigen()) && u.getTerminal().equals(user.getTerminal()) ){
                this.sesiones.remove(i);
                logger.debug("********************** Remuevo la conexion de: "+u);

                return true;
            }
        }

       return false;
    }

    /**
     * Revisa si un usuario esta conectado.
     * @param user Usuario del que se desea revisar su conexi&oacute;n
     * @return true si tiene una conexi&oacute;n activa
     */
    @Override
    public boolean verificaConexion(User user) {
        for(User u: this.sesiones){
            if( u.getUser().equals(user.getUser()) ){
                logger.debug("Se encuentra conectado el usuario: "+u);

                return true;
            }
        }

       return false;
    }

    /**
     * Actualiza la actividad de la conexion existente
     * @param user  Usuario del que se desea actualizar el estado de la conexi&oacute;n
     */
    @Override
    public void actualizaActividadConexion(User user) {
        logger.debug("Intento actualizar el estado de la sesión de: "+user);

        for(User u: this.sesiones){
            if( u.getUser().equals(user.getUser()) && u.getOrigen().equals(user.getOrigen()) && u.getTerminal().equals(user.getTerminal()) ){
                u.setActividad(new Date());
                logger.debug("Estado de la sesión actualizada:"+user);
            }
        }
    }

    /**
     * Recupera los datos del usuario conectado solicitado
     * @param id Identificador del usuario
     */
    @Override
    public User getUsuarioConectado(String id) {
        for(User u: this.sesiones){
            if(u.getId().equals(id)){
                return u;
            }
        }
        
        return null;
    }

    /**
     * Recupera los datos del usuario conectado por su sesión
     * @param session Sesion del usuario
     */
    @Override
    public User getUsuarioConectadoSesion(String session) {
        for(User u: this.sesiones){
            if(u.getSesion()!=null && u.getSesion().getId().equals(session)){
                return u;
            }
        }
        
        return null;
    }

    /**
     * Revisa las sesiones que ya hayan finalizado.
     * @return Colecci&oacute;n con los usuarios que expiraron sus sesiones.
     */
    @Override
    public List<User> revisaSesionesInactivas() {
        ArrayList<User> l=new ArrayList<User>();
        User u;

        logger.debug("Sesiones activas: "+this.sesiones.size());
        
        for(int i=this.sesiones.size()-1; i>=0; i--){
            u=this.sesiones.get(i);
            if( Calendar.getInstance().getTimeInMillis()>(u.getActividad().getTime()+this.tiempoSesion) ){
                l.add(u);
                this.sesiones.remove(i);
                logger.debug("*************************** Finaliza la session por tiempo ("+this.tiempoSesion+") de: "+u);
            }
        }

       return l;
    }

    /**
     * Recupera los datos del usuario bloqueado solicitado
     * @param id Identificador del usuario
     */
    @Override
    public User getUsuarioBloqueado(String id) {
        for(User u: this.bloqueos){
            if(u.getId().equals(id)){
                return u;
            }
        }
        
        return null;
    }

    /**
     * Recupera la lista de conecionex activas
     * @return Lista de conexiones
     */
    @Override
    public List<User> getConexiones() {
        return this.sesiones;
    }

}
