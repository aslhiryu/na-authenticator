package neoatlantis.accesscontroller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import neoatlantis.accesscontroller.allower.interfaces.AllowerWay;
import neoatlantis.accesscontroller.audit.interfaces.AuditWay;
import neoatlantis.accesscontroller.audit.interfaces.EventAudit;
import neoatlantis.accesscontroller.authentication.interfaces.AuthenticationWay;
import neoatlantis.accesscontroller.authentication.interfaces.ValidationResult;
import neoatlantis.accesscontroller.blocker.interfaces.BlockerWay;
import neoatlantis.accesscontroller.exceptions.WayAccessException;
import neoatlantis.accesscontroller.jmx.InfoStatusUsers;
import neoatlantis.accesscontroller.jmx.InfoStatusUsersMBean;
import neoatlantis.accesscontroller.login.interfaces.AuthenticationLogin;
import neoatlantis.accesscontroller.objects.EnvironmentType;
import neoatlantis.accesscontroller.objects.LogEvent;
import neoatlantis.accesscontroller.objects.Permission;
import neoatlantis.accesscontroller.objects.Role;
import neoatlantis.accesscontroller.objects.User;
import neoatlantis.accesscontroller.profiler.interfaces.ProfilerWay;
import neoatlantis.accesscontroller.scheduler.interfaces.SchedulerWay;
import neoatlantis.accesscontroller.web.UtilsAuthenticatorBean;
import neoatlantis.applications.catalogs.objetcs.OrderType;
import neoatlantis.applications.utils.UtilsPagination;
import neoatlantis.applications.web.UtilsApplicationBean;
import neoatlantis.applications.web.listeners.ApplicationListener;
import neoatlantis.applications.web.listeners.PageListener;
import neoatlantis.utils.cipher.interfaces.DataCipher;
import neoatlantis.utils.data.DataUtils;
import neoatlantis.utils.objects.Event;
import org.apache.log4j.Logger;

/**
 * Control que autentica cuentas, valida permisos y registra los eventos de los 
 * mismos.<br>
 * <br>
 * Este objeto es el responsable de autenticar cuentas de usuario para acceder a 
 * una aplicacion.
 * Para hacerlo utiliza un {@link neoatlantis.utilidades.accesscontroller.authentication.interfaces.AuthenticationWay Medio de Autenticacion}
 * (archivo de texto, BD, etc.) el cual valida que sea un usuario permitido el que 
 * esta intentando acceder.<br>
 * <br>
 * Restringe el acceso mediante lo estipulado en un {@link neoatlantis.utilidades.accesscontroller.scheduler.interfaces.SchedulerWay Medio Calendarizador}
 * el cual define los tiempos de acceso permitidos a la aplicacion.<br>
 * <br>
 * Para llevar el control de quienes estan conectados y quienes estan bloqueados 
 * por mal uso de la cuenta, utiliza un
 * {@link neoatlantis.utilidades.accesscontroller.blocker.interfaces.BlockerWay Medio de Bloqueo} 
 * (archivo de texto, BD, etc.)<br>
 * <br>
 * Finalmente para registrar los eventos que ocurren durante la autenticacion de 
 * cuentas y validacion de permisos, utiliza una
 * {@link neoatlantis.utilidades.accesscontroller.audit.interfaces.AuditWay Medio Bitacoreador}
 * en donde se registran todos los eventos ocurridos.<br>
 * <br>
 * Para generar y confirgurar el objeto dentro de una aplicaci&oacute;n web, se 
 * puede apoyar con la utileria {@link neoatlantis.utilidades.accesscontroller.AccessControllerPublisher}
 * @version 4.1
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class AccessController {
    private static final Logger DEBUGER = Logger.getLogger(AccessController.class);

    protected int maxIntentos = 3;
    protected AuthenticationWay autenticador;
    protected ProfilerWay rolador;
    protected AllowerWay permisor;
    protected BlockerWay bloqueador;
    protected AuditWay auditor;
    protected SchedulerWay calendarizador;
    protected DataCipher cifra;
    
    private int intentos = 0;
    private boolean usuarioMultiple = false;
    private boolean useCaptcha = false;
    private String nomApp="undefined";
    private InfoStatusUsersMBean mBean;

    private static AccessController access;


    
    // Contructores ------------------------------------------------------------
    
    /**
     * Genera un Control de acceso
     * @param na Nombre del aplicativo
     * @param aw Medio de autenticación
     * @param dc Cifrador de datos
     * @param pw Medio perfilador
     * @param alw Medio permisor
     * @param bw Medio bloqueador
     * @param auw Medio auditor
     * @param sw Medio temporizador
     * @throws Exception 
     */
    private AccessController(String na, AuthenticationWay aw, DataCipher dc, ProfilerWay pw, AllowerWay alw, BlockerWay bw, AuditWay auw, SchedulerWay sw) throws Exception{
        this.nomApp=na;
        this.autenticador=aw;
        this.cifra=dc;
        this.rolador=pw;
        this.permisor=alw;
        this.bloqueador=bw;
        this.auditor=auw;
        this.calendarizador=sw;

        this.autenticador.setDataCipher(dc);
        this.autenticador.setAuditWay(auw);
        this.maxIntentos=3;
        
        //genero el mBean de la informacion de usuarios
        this.mBean=new InfoStatusUsersMBean(bw);
        ApplicationListener.registerMBean(this.mBean, InfoStatusUsers.class, "neoatlantis.app."+(DataUtils.cleanSpecialCharacters(this.nomApp))+".users.jmx:type=InfoStatusUsers");        
        
        DEBUGER.debug("Inicia la configuración del control de accesos: "+this);
    }


    
    // Metodos publicos estaticos-----------------------------------------------
    
    /**
     * Obtiene la instancia del control de acceso, en caso de no existir genera uno
     * @param na Nombre del aplicativo
     * @param aw Medio de autenticación
     * @param dc Cifrador de datos
     * @param pw Medio perfilador
     * @param alw Medio permisor
     * @param bw Medio bloqueador
     * @param auw Medio auditor
     * @param sw Medio temporizador
     * @return Instancia del Control de acceso
     * @throws Exception 
     */
    public static AccessController getInstance(String na, AuthenticationWay aw, DataCipher dc, ProfilerWay pw, AllowerWay alw, BlockerWay bw, AuditWay auw, SchedulerWay sw) throws Exception{
        if( access==null ){
            access=new AccessController(na, aw, dc, pw, alw, bw, auw, sw);
        }
        
        return access;
    }
    
    /**
     * Obtiene la instancia del control de acceso
     * @return Instancia del Control de acceso
     */
     public static AccessController getInstance(){
         return access;
     }


     
    // Metodos publicos --------------------------------------------------------
    
     @Override
     public String toString(){
         StringBuilder sb=new StringBuilder();
         
         sb.append("/*********************  AccessController  ********************//").append(System.getProperty("line.separator"));
         sb.append("Nombre del Aplicativo: ").append(this.nomApp).append(System.getProperty("line.separator"));
         sb.append("Authentication: ").append(autenticador.getClass()).append(System.getProperty("line.separator"));
         sb.append("Cipher: ").append(cifra.getClass()).append(System.getProperty("line.separator"));
         sb.append("Profiler: ").append(rolador!=null? rolador.getClass(): "").append(System.getProperty("line.separator"));
         sb.append("Allower: ").append(permisor!=null? permisor.getClass(): "").append(System.getProperty("line.separator"));
         sb.append("Blocker: ").append(bloqueador!=null? bloqueador.getClass(): "").append(System.getProperty("line.separator"));
         sb.append("Audit: ").append(auditor!=null? auditor.getClass(): "").append(System.getProperty("line.separator"));
         sb.append("Scheduler: ").append(calendarizador.getClass()).append(System.getProperty("line.separator"));
         sb.append("MultipleUser: ").append(this.usuarioMultiple).append(System.getProperty("line.separator"));
         sb.append("UseCaptcha: ").append(this.useCaptcha).append(System.getProperty("line.separator"));
         sb.append("MaxIntent: ").append(this.maxIntentos).append(System.getProperty("line.separator"));
         sb.append("/*************************************************************//").append(System.getProperty("line.separator"));
         
         return sb.toString();
     }
     
     public void updateStatisticsLifeConnection(double lifeTime){
         this.mBean.updateLifeConnection(lifeTime);
     }
     
    /**
     * Valida una cuenta
     * @param origen IP desde donde se origina la peticion.
     * @param terminal Nombre de la terminal desde donde se origina la peticion.
     * @param tipoTerminal Tipo de terminal desde donde se origina la peticion.
     * @param datos Información a validar, dependiendo del tipo de AuthenticationWay son el numero y tipo de datos a validar.
     * @return Evento obtenido por la validacion.
     * @throws WayAccessException
     */
    public User authenticatePerson(String origen, String terminal, EnvironmentType tipoTerminal, Map<String, Object> datos) throws WayAccessException {
        User user=null;
        String oriCod=null;

        DEBUGER.debug("Inicia Validación.");

         if (datos.isEmpty()) {
            throw new NullPointerException("No existe datos suficientes para realizar la autenticación.");
        }

        //termina bloqueos anteriores
        this.validateEndedBlocks();
        
        //inabilita los usuarios que tienen tiempo sin actividad
        this.validateLoginLifes();

        //valida si es necesario validar el captcha
        if( this.useCaptcha  && this.intentos>=this.maxIntentos && datos.get(AuthenticationLogin.REQUEST_WEB_KEY)!=null ){
            DEBUGER.debug("Realiza la validación del captcha.");
            //valida que sean iguales los codigos, si no genera un error
            if( !UtilsApplicationBean.validateConfirmationCode((HttpServletRequest)datos.get(AuthenticationLogin.REQUEST_WEB_KEY), (String)datos.get(PageListener.CAPTCHA_PARAM)) ){
                DEBUGER.debug("Captcha invalido.");
                user=User.getNobody();
                user.setOrigin(origen);
                user.setTerminal(terminal);
                user.setEnvironmentType(tipoTerminal);
                user.setState(ValidationResult.CODE_ERROR);
                
                return user;
            }            
        }
        
        //realiza la validacion normal
        //realiza verificación
        DEBUGER.debug("Realiza la autenticación del usuario.");
        try {
            user = this.autenticador.authenticateUser(datos);
            this.writeEvent(user, EventAudit.LOGGED, "Intenta acceder al sistema con usuario: "+user.getUser());
        } catch (WayAccessException ex) {
            DEBUGER.error("Error al autenticar al usuario.", ex);
            throw ex;
        }
        user.setOrigin(origen);
        user.setTerminal(terminal);
        user.setEnvironmentType(tipoTerminal);
        DEBUGER.debug("Usuario encontrado: "+user);

        //revisa si ya esta conectado
        if (this.usuarioMultiple == false) {
            DEBUGER.debug("Revisa si el usuario esta conectado.");
            if (this.bloqueador.validateConnection(user)) {
                DEBUGER.debug("El usuario ya se encuentra conectado.");
                this.writeEvent(user, EventAudit.DENIED, "Es rechazado debido a que ya esta conectado");
                user.setState(ValidationResult.IN_USE);
            }
        }

        //revisa si esta bloqueado
        DEBUGER.debug("Revisa si el usuario esta bloqueado.");
        if (this.bloqueador.validateBlock(user)) {
            //revisa si esta bloqueado
            DEBUGER.debug("Finaliza el accedo debido a que esta bloqueado.");
            this.writeEvent(user, EventAudit.DENIED, "Es rechazado debido a que se encuentra bloqueado");
            user.setState(ValidationResult.BLOCKED);
        }

        //revisa si esta dentro de los tiempos de acceso
        DEBUGER.debug("Revisa si el usuario esta accediento dentro de los tiempos establecidos.");
        if ( !this.calendarizador.availability()) {
            DEBUGER.debug("No se esta dentro de los horarios permitidos.");
            this.writeEvent(user, EventAudit.DENIED, "Es rechazado debido a que se esta fuera de los tiempos de acceso");

            user.setState(ValidationResult.OUTTIME);
            /*if( this.notificador!=null ){
                this.notificador.enviaNotificacion("SSO", "El usuario '"+user.getUser()+"', esta intentando acceder desde '"+user.getOrigen()+"' en un horario indevido.");
            }*/
        }

        //realiza la validación extra
        String valExtra=this.autenticador.additionalValidation(user);
        if( valExtra!=null ){
            DEBUGER.debug("No se permitio el acceso por la validación extra '"+valExtra+"'.");
            this.writeEvent(user, EventAudit.DENIED, valExtra);

            user.setState(ValidationResult.ALTER_DENIED);
        }

        if (user.getState()==ValidationResult.VALIDATE || user.getState()==ValidationResult.TEMPORAL_VALIDATE ) {
            //carga los roles asignados
            if( this.rolador!=null ){
                user.addRoles(this.rolador.getRolesFromUser(user, new Object()));
            }

            //reviso que tenga roles asignados
            if( user.getRoles().isEmpty() ){
                user.setState(ValidationResult.DENIED);
                this.writeEvent(user, EventAudit.ENTRY, "Es rechazado debido a que no tiene roles asignados.");
                DEBUGER.debug("Usuario rechazado por falta de roles.");
            }
            else{
                intentos = 0;
                this.bloqueador.addConnection(user);
                this.writeEvent(user, EventAudit.ENTRY, "Inicio de sesión");
                DEBUGER.debug("Usuario autenticado.");

                //asigno el permiso de logeado
                user.agregarPermiso(new Permission("LOGGED"));
                user.addRole(new Role("USERS"));
            }
        }
        else if (user.getState()==ValidationResult.DENIED) {
            DEBUGER.debug("Contraseña no valida.");
            this.intentos++;
            this.writeEvent(user, EventAudit.DENIED, "El usuario no logro acceder por contraseña invalida, " + this.intentos + " intentos");
            DEBUGER.debug("Incrementa #intentos a " + intentos + ".");

            DEBUGER.debug("Revisa si el numero de intentos ha sido revasado.");
            if (this.intentos >= this.maxIntentos*(this.useCaptcha? 2: 1)) {
                this.bloqueador.addBlock(user);

                //inicializa los intentos
                this.intentos = 0;
                DEBUGER.debug("El numero de intentos  ha sido revasado.");
                this.writeEvent(user, EventAudit.BLOCKED, "Es bloqueado debido a que se ha revasado el numero de intentos");

                user.setState(ValidationResult.EXCEED_LIMIT);
                /*if( this.notificador!=null ){
                    this.notificador.enviaNotificacion("SSO", "El usuario '"+user.getUser()+"', ha exedido su numero de intentos.");
                }*/
            }
        }
        else if (user.getState()==ValidationResult.INACTIVE) {
            DEBUGER.debug("Usuario no activo.");
            this.writeEvent(user, EventAudit.DENIED, "El usuario no logro acceder por no estar activo.");
        }
        else if (user.getState()==ValidationResult.NOT_FOUND) {
            DEBUGER.debug("Usuario no encontrado.");
            this.writeEvent(user, EventAudit.DENIED, "El usuario no logro acceder por no existir.");
        }
        else if (user.getState()==ValidationResult.NOT_PROVISIONED) {
            DEBUGER.debug("Usuario no aprovisionado.");
            this.writeEvent(user, EventAudit.DENIED, "El usuario no logro acceder por no estar aprovisionado.");
        }
        else if (user.getState()==ValidationResult.NOT_USER) {
            DEBUGER.debug("Usuario no proporcionado.");
        }
        else if (user.getState()==ValidationResult.BLOCKED) {
            DEBUGER.debug("Usuario bloqueado por "+(this.bloqueador.getSessionTime()/1000/60)+" minutos.");
            this.writeEvent(user, EventAudit.DENIED, "El usuario no logro acceder por estar bloqueado.");
        }
        else if (user.getState()==ValidationResult.IN_USE) {
            DEBUGER.debug("Usuario autenticado en otra sesion.");
            this.writeEvent(user, EventAudit.DENIED, "El usuario no logro acceder por estar autenticado en otra sesion.");
        }
        else{
            DEBUGER.debug("No se encontro la opción.");
            throw new WayAccessException("No se localizo el resultado proporcionado: "+user.getState());
        }

        return user;
    }

    /**
     * Valida una cuenta
     * @param origen IP desde donde se origina la peticion.
     * @param terminal Nombre de la terminal desde donde se origina la peticion.
     * @param datos Información a validar, dependiendo del tipo de AuthenticationWay son el numero y tipo de datos a validar.
     * @return Evento obtenido por la validacion.
     * @throws WayAccessException
     */
    public User authenticatePerson(String origen, String terminal, Map<String, Object> datos) throws WayAccessException {
        return authenticatePerson(origen, terminal, EnvironmentType.STANDALONE, datos);
    }

    /**
     * Valida una cuenta
     * @param datos Información a validar, dependiendo del tipo de AuthenticationWay son el numero y tipo de datos a validar.
     * @return Evento obtenido por la validacion.
     * @throws WayAccessException
     */
    public User authenticatePerson(Map<String, Object> datos) throws WayAccessException {
        return authenticatePerson("127.0.0.1", "localhost", datos);
    }

    /**
     * Asigna el numero maximo de intentos para poder autenticar una cuenta. Por default tiene 3.
     * @param maxIntentos the maxIntentos to set
     */
    public void setMaximiumAttempt(int maxIntentos) {
        this.maxIntentos = maxIntentos;
    }

    /**
     * Asigna la bandera, para permitir que varios usuarios con la misma cuenta puedan acceder. Por default tiene true
     * @param usuarioMultiple true si se desea activar
     */
    public void setMultipleUser(boolean usuarioMultiple) {
        this.usuarioMultiple = usuarioMultiple;
    }

    public boolean isMultiUser(){
        return this.usuarioMultiple;
    }
    
    public boolean isUseCaptcha(){
        return this.useCaptcha;
    }
    
    public void setUseCaptcha(boolean use){
        this.useCaptcha=use;
    }
    
    public boolean execedAttempt(){
        return (this.intentos>=this.maxIntentos);
    }
    
    
    
    
    //-------- authenticator    
    
    /**
     * Recupera la lista de usuarios registrados
     * @return Lista de usuarios
     */
    public List<User> getRegisteredUsers(String order, OrderType orderType){
        return this.autenticador.getRegisteredUserList(order, orderType);
    }
    
    public Map<String,Object> registerUser(String idAdmin, String newUser, Map<String,Object> data){
        User admin=this.getConnectedUser(idAdmin);

        if(admin!=null){
            this.writeEvent(admin, EventAudit.ADMIN, "Registra al usuario '"+newUser+"'.");
            data.put(UtilsAuthenticatorBean.ID_ADMIN_PARAM, admin.getId());
            data.put(UtilsAuthenticatorBean.NAME_ADMIN_PARAM, admin.getUser());
            
            return this.autenticador.createUser(data);
        }
        else{
            DEBUGER.debug("No se logro ubicar a algun elemento para agregar al usuario.");
            return null;
        }
    }
    
    public Map<String,Object> modifyUser(String idAdmin, String idUser, Map<String,Object> data){
        User admin=this.getConnectedUser(idAdmin);
        User user=this.autenticador.getUserData(idUser);

        if(admin!=null && user!=null){
            this.writeEvent(admin, EventAudit.ADMIN, "Modifica al usuario '"+user.getUser()+"'.");
            data.put(UtilsAuthenticatorBean.ID_ADMIN_PARAM, admin.getId());
            data.put(UtilsAuthenticatorBean.NAME_ADMIN_PARAM, admin.getUser());
            
            return this.autenticador.updateUser(data);
        }
        else{
            DEBUGER.debug("No se logro ubicar a algun elemento para modificar al usuario.");
            return null;
        }
    }
    
    public boolean activeUser(String idAdmin, String idUser, boolean active){
        User admin=this.getConnectedUser(idAdmin);
        User user=this.autenticador.getUserData(idUser);

        if(admin!=null && user!=null){
            this.writeEvent(admin, EventAudit.ADMIN, (active? "Habilita": "Inhabilita")+" al usuario '"+user.getUser()+"'.");
            return this.autenticador.activeUser(user.getId(), active);
        }
        else{
            DEBUGER.debug("No se logro ubicar a algun elemento para modificar al usuario.");
            return false;
        }
    }
    
    /**
     * Revisa que usuarios no han tenido actividad en algun tiempo y los ihnabilita
     * @throws WayAccessException 
     */
    public void validateLoginLifes() throws WayAccessException {
        DEBUGER.debug("Busca usuarios sin actividad.");

        //reviso que se tenga que validar la actividad d elos usuarios
        if( this.autenticador.getLoginLife()>0){
            List<User> usrs = this.autenticador.validateLoginLifes();
            for(User u: usrs){
                DEBUGER.debug("Inhabilita por inactividad a " + u.getUser() + "'.");
            }
        }
    }
    
    public User getRegisteredUser(String id){
        DEBUGER.debug("Intento recuperar el usuario con id="+id);
        
        return this.autenticador.getUserData(id);
    }
    
    public boolean allowsUpdatePasswords(){
        return this.autenticador.allowsUpdatePassword();
    }

    public boolean allowsUpdateUsers(){
        return this.autenticador.allowsUpdateUser();
    }

    public boolean allowsCreateUser(){
        return this.autenticador.allowsCreateUser();
    }
    
    
    
    
    //-------- blocker    
    
    /**
     * Revisa los bloqueos y si han finalizado los remueve.
     * @throws WayAccessException
     */
    public void validateEndedBlocks() throws WayAccessException {
        DEBUGER.debug("Busca bloqueos finalizados.");

        List<User> usrs = this.bloqueador.validateEndedBlocks();
        for(User u: usrs){
            DEBUGER.debug("Finaliza el bloqueo de '" + u.getUser() + "'.");
        }
    }

    public void ends(String idAdmin, String idUser) throws WayAccessException {
        User admin=this.getConnectedUser(idAdmin);
        User user=this.autenticador.getUserData(idUser);

        if(admin!=null && user!=null){
            this.writeEvent(admin, EventAudit.ADMIN, "Forza para finalizar la sesión de '"+user.getUser()+"'.");
            ends(user);
        }
        else{
            DEBUGER.debug("No se logro ubicar a algun elemento para remover finalizar la sesion.");
        }
    }

    /**
     * Termina la conexion de un usuario
     * @param user Usuario del que se desea terminar la conexion
     * @throws WayAccessException
     */
    public void ends(User user) throws WayAccessException {
        this.bloqueador.removeConnection(user);

        this.intentos = 0;
        this.writeEvent(user, EventAudit.EXIT, "Finalización de sesión forzada");
    }

    /**
     * Revisa las sesiones inactivas y si han finalizado las remueve.
     * @throws WayAccessException
     */
    public List<User> validateInactiveSessions() throws WayAccessException {
        DEBUGER.debug("Busca sesiones inactivas.");

        List<User> usrs = this.bloqueador.validateInactiveSessions();
        for(User u: usrs){
            DEBUGER.debug("Finaliza la session de '" + u.getUser() + "'.");            
        }
        
        return usrs;
    }

    /**
     * Recupera un usuario autenticado a partir de su id.
     * @param id Id del usuario a encontrar
     * @return Usuario encontrado o nullo si no encontro ninguno
     * @throws WayAccessException 
     */
    public User getConnectedUser(String id) throws WayAccessException{
        return this.bloqueador.getConnectedUser(id);
    }
    
    /**
     * Recupera un usuario autenticado a partir de su sesi&oacute;n.
     * @param ses Sesi&oacute;n del usuario a encontrar
     * @return Usuario encontrado o nullo si no encontro ninguno
     * @throws WayAccessException 
     */
    public User getConnectedUserBySession(String ses) throws WayAccessException{
        return this.bloqueador.getConnectedUserBySession(ses);
    }
    
    /**
     * Recupera un usuario bloqueado por id.
     * @param id Idendificador del usuario a recuperar
     * @return Usuario encontrado o nullo si no encontro ninguno
     * @throws WayAccessException 
     */
    public User getBlockedUser(String id) throws WayAccessException{
        return this.bloqueador.getBlockedUser(id);
    }
    
    /**
     * Recupera las sesiones que se encuentran activas.
     * @return Lista de usuarios con sesi&oacute;n activa
     * @throws WayAccessException 
     */
    public List<User> getConnections() throws WayAccessException{
        return this.bloqueador.getConnections();
    }
    
    /**
     * Elimina el bloqueo de un usuario
     * @param u Usuario del que se desea remover el bloqueo
     * @return true si lo logro remover
     * @throws WayAccessException 
     */
    public boolean removeBlock(String idAdmin, String idUser) throws WayAccessException{
        User admin=this.getConnectedUser(idAdmin);
        User user=this.autenticador.getUserData(idUser);

        if(admin!=null && user!=null){
            this.writeEvent(admin, EventAudit.ADMIN, "Remueve el bloqueo del usuario '"+user.getUser()+"'.");
            return this.bloqueador.removeBlock(user);
        }
        else{
            DEBUGER.debug("No se logro ubicar a algun elemento para remover el bloqueo.");
            return false;
        }
    }
    
    /**
     * Actualiza la fehca y hora de conexi&oacute;n.
     * @param user Usuario del que se desea actualizar
     * @throws WayAccessException 
     */
    public void updateActivity(User user) throws WayAccessException{
        this.bloqueador.updateActivity(user);
    }

    /**
     * Elimina la conexi&oacute;n de un usuario
     * @param idUser Idendificador del usuario a eliminar
     * @param admin Usuario que realiza el termino de la conexi&oacute;n
     * @return true si la logro eliminar
     */
    public boolean removeConnection(String idUser, User admin){
        boolean ev=false;
        User u=null;

        try{
            u=this.getConnectedUser(idUser);
            if( u!=null ){
                try{
                    ends(u);                    
                    ev=true;
                }catch(Exception ex){
                    DEBUGER.fatal("No se logro terminar la conexión", ex);
                }

                if( u.getSession()!=null && u.getSession().getHttpSession()!=null ){
                    u.getSession().getHttpSession().invalidate();
                }
                
                DEBUGER.debug("Termina la conexion del usuario: "+u.getUser());
                this.writeEvent(admin, EventAudit.ADMIN, "Finaliza la sesión de "+u.getUser());
            }
        }
        catch(Exception ex){
            DEBUGER.error("No se logro destruir la sesion de: "+u, ex);
        }
        
        return ev;
    }

    
    
    
    
    
    
    //-------- audit            
    
    /**
     * Genera una entrada de ingreso en la bitacora
     * @param user Usuario que genera la entrada
     * @param sistema Sistema al que ingresa
     * @throws WayAccessException 
     */
    public void writeEntryEvent(User user, String sistema) throws WayAccessException{
        writeEvent(user, EventAudit.ENTRY, "Ingresa al sistema '"+sistema+"'.");
    }
    
    /**
     * Genera una entrada en la bitacora
     * @param user Usuario que genera la entrada
     * @param evento Evento que se registra
     * @throws WayAccessException 
     */
    public void whiteEvent(User user, String evento) throws WayAccessException{
        writeEvent(user, EventAudit.BUSSINESS, evento);
    }

    /**
     * Genera una entrada en la bitacora
     * @param user Usuario que genera la entrada
     * @param tipo Tipo de evento que se registra
     * @param evento Evento que se registra
     * @throws WayAccessException 
     */
    public void writeEvent(User user, EventAudit tipo, String evento) throws WayAccessException{
        if( this.auditor!=null ){
            this.auditor.writeEvent(user, tipo, evento);
        }
    }
    
    /**
     * Genera una entrada en la bitacora
     * @param user Usuario que genera la entrada
     * @param tipo Tipo de evento que se registra
     * @param evento Evento que se registra
     * @param  data Informacion adicional
     * @throws WayAccessException 
     */
    public void writeEvent(User user, EventAudit tipo, String evento, Map<String,Object> data) throws WayAccessException{
        if( this.auditor!=null ){
            this.auditor.writeEvent(user, tipo, evento, data);
        }
    }

    /**
     * Recupera la lista de eventos registrados
     * @return Lista de usuarios
     */
    public List<LogEvent> getEventList(Date ini, Date fin, String order, OrderType orderType, int page) throws Exception{
        HashMap<String,Object> p=new HashMap<String,Object>();
        List<LogEvent> res=new ArrayList<LogEvent>();;
        List<Event> rTmp;
        
        p.put(AuditWay.INITIAL_DATE_PARAM, ini);
        p.put(AuditWay.END_DATE_PARAM, fin);
        p.put(AuditWay.ORDER_PARAM, order);
        p.put(AuditWay.ORDER_TYPE_PARAM, orderType);
        
        rTmp=this.auditor.getEvents(p, UtilsPagination.getPageSise(), (page-1)*UtilsPagination.getPageSise());
        
        for(int i=0; rTmp!=null&&i<rTmp.size(); i++){
            res.add( (LogEvent)rTmp.get(i) );
        }
        
        return res;
    }
    
    public int getTotalEvents(Date ini, Date fin) throws Exception{
        HashMap<String,Object> p=new HashMap<String,Object>();

        p.put(AuditWay.INITIAL_DATE_PARAM, ini);
        p.put(AuditWay.END_DATE_PARAM, fin);
        
        return this.auditor.getRegistries(p);
    }

    
    
    
    
    //-------- perfilador
    
    public boolean canAsignRoles(){
        return this.rolador.canAsignRoles();
    }
    
    public boolean canEditRoles(){
        return this.rolador.canEditRoles();
    }
    
    public List<Role> getRegisteredRoles(){
        return this.rolador.getRegisteredRoles();
    }

    public List<Role> getActivedRoles(){
        return this.rolador.getActivedRoles();
    }

    public List<Role> getAssignedRoles(User user, Map<String,Object> param){
        return this.rolador.getRolesFromUser(user, param);
    }
    
    public boolean assignRole(String idAdmin, String idUser, String idRole){
        User admin=this.getConnectedUser(idAdmin);
        User user=this.autenticador.getUserData(idUser);
        Role role=this.rolador.getRoleData(idRole);
        
        if(admin!=null && user!=null && role!=null){
            this.writeEvent(admin, EventAudit.ADMIN, "Asigna el rol de '"+role.getName()+"' a '"+user.getUser()+"'.");
            return this.rolador.asignRoleToUser(idUser, idRole);
        }
        else{
            DEBUGER.debug("No se logro ubicar a algun elemento para asignar el rol.");
            return false;
        }
    }

    public boolean removeRole(String idAdmin, String idUser, String idRole){
        User admin=this.getConnectedUser(idAdmin);
        User user=this.autenticador.getUserData(idUser);
        Role role=this.rolador.getRoleData(idRole);
        
        if(admin!=null && user!=null && role!=null){
            this.writeEvent(admin, EventAudit.ADMIN, "Remueve el rol de '"+role.getName()+"' a '"+user.getUser()+"'.");
            return this.rolador.removeRoleFromUser(idUser, idRole);
        }
        else{
            DEBUGER.debug("No se logro ubicar a algun elemento para remover el rol.");
            return false;
        }
    }
    
    public boolean allowsUpdateRoles(){
        return this.rolador.allowsUpdateRoles();
    }

    public boolean canAsignPermissions(){
        return this.rolador.allowsAsignPermissions();
    }

    public Map<String,Object> registerRole(String idAdmin, String nameRole, Map<String,Object> data){
        User admin=this.getConnectedUser(idAdmin);

        if(admin!=null){
            this.writeEvent(admin, EventAudit.ADMIN, "Registra al rol '"+nameRole+"'.");
            return this.rolador.createRole(data);
        }
        else{
            DEBUGER.debug("No se logro ubicar a algun elemento para registrar el rol.");
            return null;
        }
    }
    
    public Map<String,Object> modifyRole(String idAdmin, String idRole, Map<String,Object> data){
        User admin=this.getConnectedUser(idAdmin);
        Role role=this.getRegisteredRole(idRole);

        if(admin!=null && role!=null){
            this.writeEvent(admin, EventAudit.ADMIN, "Modifica el rol '"+role.getName()+"'.");
            return this.rolador.updateRole(data);
        }
        else{
            DEBUGER.debug("No se logro ubicar a algun elemento para modificar el rol.");
            return null;
        }
    }

    public Role getRegisteredRole(String id){
        DEBUGER.debug("Intento recuperar el role con id="+id);
        
        return this.rolador.getRoleData(id);
    }
    
    
    
    

    
    //----------------- permisor

    public List<Permission> getActivedPermissions(){
        return this.permisor.getActivedPermissions();
    }

    public List<Permission> getAssignedPermissions(Role role, Map<String,Object> param){
        return this.permisor.getAssignedPermissions(role, param);
    }
    
    public boolean assignPermissionToRole(String idAdmin, String idRole, String idPermission){
        User admin=this.getConnectedUser(idAdmin);
        Role role=this.rolador.getRoleData(idRole);
        Permission perm=this.permisor.getPermissionData(idPermission);
        
        if(admin!=null && perm!=null && role!=null){
            this.writeEvent(admin, EventAudit.ADMIN, "Agrega el permiso de '"+perm.getName()+"' al rol '"+role.getName()+"'.");
            return this.permisor.asignPermissionToRole(idRole, idPermission);
        }
        else{
            DEBUGER.debug("No se logro ubicar a algun elemento para asignar el permiso ("+idAdmin+", "+idRole+", "+idPermission+").");
            return false;
        }
    }

    public boolean removePermissionFromRole(String idAdmin, String idRole, String idPermission){
        User admin=this.getConnectedUser(idAdmin);
        Role role=this.rolador.getRoleData(idRole);
        Permission perm=this.permisor.getPermissionData(idPermission);
        
        if(admin!=null && perm!=null && role!=null){
            this.writeEvent(admin, EventAudit.ADMIN, "Remueve el permiso de '"+role.getName()+"' del rol '"+role.getName()+"'.");
            return this.permisor.removePermissionFromRole(idRole, idPermission);
        }
        else{
            DEBUGER.debug("No se logro ubicar a algun elemento para remover el permiso ("+idAdmin+", "+idRole+", "+idPermission+").");
            return false;
        }
    }
    
    
                          
}
