package neoatlantis.accesscontroller.blocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletContext;
import neoatlantis.accesscontroller.blocker.interfaces.BlockType;
import neoatlantis.accesscontroller.blocker.interfaces.BlockerWay;
import neoatlantis.accesscontroller.exceptions.WayAccessException;
import neoatlantis.accesscontroller.objects.User;
import neoatlantis.accesscontroller.web.listeners.BlockerSessionListener;
import org.apache.log4j.Logger;

/**
 * Medio Bloqueador operado a traves de objetos en memoria.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class MemoryBlocker extends BlockerWay {
    private static final Logger DEBUGER = Logger.getLogger(MemoryBlocker.class);

    /**
     * Clave con la que se ubica las conexiones activas en los contextos
     */
    public static String CLAVE_CONECTIONS="na.util.access.Conections";
    /**
     * Clave con la que se ubica los bloqueos activos en los contextos
     */
    public static String CLAVE_BLOCKS="na.util.access.Blocks";

    private List<User> sesiones;
    private List<User> bloqueos;
    
    // Contructores ------------------------------------------------------------

    /**
     * Genera un BlockerWay por memoria
     * @param sesiones
     * @param bloqueos 
     */
    public MemoryBlocker(ServletContext context){
        //genero las variables requeridas
        if( context.getAttribute(CLAVE_CONECTIONS)==null ){
            DEBUGER.debug("Inicializo las sesiones del MemoryBlocker");
            this.sesiones=Collections.synchronizedList(new ArrayList<User>());
            context.setAttribute(CLAVE_CONECTIONS, this.sesiones);
        }
        else{
            this.sesiones=(ArrayList<User>)context.getAttribute(CLAVE_CONECTIONS);
        }
        if( context.getAttribute(CLAVE_BLOCKS)==null ){
            DEBUGER.debug("Inicializo los bloqueos del MemoryBlocker");
            this.bloqueos=Collections.synchronizedList(new ArrayList<User>());
            context.setAttribute(CLAVE_BLOCKS, this.bloqueos);
        }
        else{
            this.bloqueos=(ArrayList<User>)context.getAttribute(CLAVE_BLOCKS);
        }
        
        //genero el listener para el termino de las sesiones
        context.addListener(BlockerSessionListener.class);
        
        DEBUGER.debug("Genero instancia de MemoryBlocker");
    }

    
    
    // Metodos publicos---------------------------------------------------------

    /**
     * Agregar bloqueos de usuario en memoria.
     * @param user Usuario a bloquear
     */
    @Override
    public void addBlock(User user) throws WayAccessException {
        DEBUGER.debug("Bloqueos activos: "+this.bloqueos.size());
        if( user==null ){
            return;
        }

       this.bloqueos.add(user);
       DEBUGER.debug("Agrego el bloqueo para el usuario: "+user);
    }

    /**
     * Revisa lo bloqueos que ya hayan finalizado.
     * @return Colecci&oacute;n con los nombres de los usuarios de los cuales termino su bloqueo.
     */
    @Override
    public List<User> validateEndedBlocks() throws WayAccessException {
        ArrayList<User> l=new ArrayList<User>();
        User u;

        DEBUGER.debug("Bloqueos activos (t:"+this.tiempoBloqueo+"): "+this.bloqueos.size());
        
        for(int i=this.bloqueos.size()-1; i>=0; i--){
            u=this.bloqueos.get(i);
            DEBUGER.debug("Valida bloqueo de: "+u);
            DEBUGER.debug("TA: "+(new Date())+", TUA:"+u.getActivityDate());
            if( (new Date()).getTime()>(u.getActivityDate().getTime()+this.tiempoBloqueo) ){
                l.add(u);
                this.bloqueos.remove(i);
                DEBUGER.debug("Finaliza el bloqueo por tiempo ("+this.tiempoBloqueo+") de: "+u);
            }
        }

       return l;
    }

    /**
     * Revisa las sesiones que ya hayan finalizado.
     * @return Colecci&oacute;n con los usuarios que expiraron sus sesiones.
     */
    @Override
    public List<User> validateInactiveSessions() throws WayAccessException {
        ArrayList<User> l=new ArrayList<User>();
        User u;

        DEBUGER.debug("Sesiones activas: "+this.sesiones.size());
        
        for(int i=this.sesiones.size()-1; i>=0; i--){
            u=this.sesiones.get(i);
            if( (new Date()).getTime()>(u.getActivityDate().getTime()+this.tiempoSesion) ){
                l.add(u);
                this.sesiones.remove(i);
                DEBUGER.debug("*************************** Finaliza la session por tiempo ("+this.tiempoSesion+") de: "+u);
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
    public boolean validateBlock(User user) throws WayAccessException {
        DEBUGER.debug("Bloqueos activos: "+this.bloqueos.size());

        for(User u: this.bloqueos){
            if( this.modoBloqueo==BlockType.IP && u.getOrigin().equals(user.getOrigin()) ){
                DEBUGER.debug("El usuario '"+u.getUser()+"' esta bloqueado por IP.");
                return true;
            }
            else if(this.modoBloqueo == BlockType.USUARIO && u.getUser().equals(user.getUser())){
                DEBUGER.debug("El usuario '"+u.getUser()+"' esta bloqueado por usuario.");
                return true;
            }
            else if(this.modoBloqueo == BlockType.TERMINAL && u.getUser().equals(user.getUser()) && u.getEnvironmentType()==user.getEnvironmentType()){
                DEBUGER.debug("El usuario '"+u.getUser()+"' esta bloqueado por terminal.");
                return true;
            }
            else if(this.modoBloqueo == BlockType.IP_USUARIO && (u.getUser().equals(user.getUser()) || u.getOrigin().equals(user.getOrigin()))){
                DEBUGER.debug("El usuario '"+u.getUser()+"' esta bloqueado por usuario o ip.");
                return true;
            }
        }

        DEBUGER.debug("El usuario '"+user.getUser()+"' no esta bloqueado.");
       return false;
    }

    /**
     * Remueve el bloqueos de un usuario de memoria.
     * @param user Usuario del que se desea terminar el bloqueo
     * @return true si se logro remover el bloqueo
     */
    @Override
    public boolean removeBlock(User user) throws WayAccessException {
        boolean rem=false;
        User u;

        DEBUGER.debug("Bloqueos activos: "+this.bloqueos.size());

        for(int i=this.bloqueos.size()-1; i>=0; i--){
            u=this.bloqueos.get(i);
            if( this.modoBloqueo==BlockType.IP && u.getOrigin().equals(user.getOrigin()) ){
                this.bloqueos.remove(i);
                rem=true;
                DEBUGER.debug("Remuevo el bloqueo de: "+u);
            }
            else if(this.modoBloqueo == BlockType.USUARIO && u.getUser().equals(user.getUser())){
                this.bloqueos.remove(i);
                rem=true;
                DEBUGER.debug("Remuevo el bloqueo de: "+u);
            }
            else if(this.modoBloqueo == BlockType.TERMINAL && u.getUser().equals(user.getUser()) && u.getEnvironmentType()==user.getEnvironmentType()){
                this.bloqueos.remove(i);
                rem=true;
                DEBUGER.debug("Remuevo el bloqueo de: "+u);
            }
            else if(this.modoBloqueo == BlockType.IP_USUARIO && (u.getUser().equals(user.getUser()) || u.getOrigin().equals(user.getOrigin()))){
                this.bloqueos.remove(i);
                rem=true;
                DEBUGER.debug("Remuevo el bloqueo de: "+u);
            }
        }

       return rem;
    }

    /**
     * Agrega la conexi&oacute;n de un usuario en memoria.
     * @param user Usuario del que se desea registrar su conexi&oacute;n
     */
    @Override
    public void addConnection(User user) throws WayAccessException {
        DEBUGER.debug("Conexiones activas: "+this.sesiones.size());
        if( user==null ){
            return;
        }

       this.sesiones.add(user);
        DEBUGER.debug("Agrego la conexion del usuario: "+user);
    }

    /**
     * Remueve la conexi&oacute;n de un usuario de memoria.
     * @param user Usuario del que se desea remover su conexi&oacute;n
     * @return true si se logro remover la conexi&oacute;n
     */
    @Override
    public boolean removeConnection(User user) throws WayAccessException {
        User u;

        DEBUGER.debug("Conexiones activas: "+this.sesiones.size());
        for(int i=this.sesiones.size()-1; i>=0; i--){
            u=this.sesiones.get(i);
            if( u.getUser().equals(user.getUser()) && u.getOrigin().equals(user.getOrigin()) && u.getTerminal().equals(user.getTerminal()) ){
                this.sesiones.remove(i);
                DEBUGER.debug("********************** Remuevo la conexion de: "+u);

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
    public boolean validateConnection(User user) throws WayAccessException {
        for(User u: this.sesiones){
            if( u.getUser().equals(user.getUser()) ){
                DEBUGER.debug("Se encuentra conectado el usuario: "+u);

                return true;
            }
        }

       return false;
    }

    /**
     * Recupera los datos del usuario conectado solicitado
     * @param id Identificador del usuario
     */
    @Override
    public User getConnectedUser(String id) throws WayAccessException {
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
    public User getConnectedUserBySession(String session) throws WayAccessException {
        for(User u: this.sesiones){
            if(u.getSession()!=null && u.getSession().getId().equals(session)){
                return u;
            }
        }
        
        return null;
    }

    /**
     * Recupera los datos del usuario bloqueado solicitado
     * @param id Identificador del usuario
     */
    @Override
    public User getBlockedUser(String id) throws WayAccessException {
        for(User u: this.bloqueos){
            if(u.getId().equals(id)){
                return u;
            }
        }
        
        return null;
    }

    /**
     * Actualiza la actividad de la conexion existente
     * @param user  Usuario del que se desea actualizar el estado de la conexi&oacute;n
     */
    @Override
    public void updateActivity(User user) throws WayAccessException {
        DEBUGER.debug("Intento actualizar el estado de la sesión de: "+user);

        for(User u: this.sesiones){
            if( u.getUser().equals(user.getUser()) && u.getOrigin().equals(user.getOrigin()) && u.getTerminal().equals(user.getTerminal()) ){
                u.setActivityDate(new Date());
                DEBUGER.debug("Estado de la sesión actualizada:"+user);
            }
        }
    }

    /**
     * Recupera la lista de conecionex activas
     * @return Lista de conexiones
     */
    @Override
    public List<User> getConnections() throws WayAccessException {
        return cloneList(this.sesiones);
    }
  
    /**
     * Definici&oacute;n del metodo para recuperar la lista de conecionex activas
     * @return Lista de conexiones
     * @throws WayAccessException 
     */
    @Override
    public List<User> getBlocked() throws WayAccessException{
        return cloneList(this.bloqueos);
    }

    
    private List<User> cloneList(List<User> l){
        if( l==null ){
            return null;
        }
        
        List<User> lTmp=new ArrayList();
        
        for(User uTmp: l){
            lTmp.add(  uTmp.clone() );
        }
        
        return lTmp;
    }
}
