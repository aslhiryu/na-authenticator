package neoAtlantis.utilidades.accessController;

import java.util.List;
import java.util.Map;
import neoAtlantis.utilidades.accessController.allower.interfaces.AllowerWay;
import neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay;
import neoAtlantis.utilidades.accessController.audit.interfaces.EventAudit;
import neoAtlantis.utilidades.accessController.authentication.interfaces.ValidationResult;
import neoAtlantis.utilidades.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay;
import neoAtlantis.utilidades.accessController.cipher.interfaces.DataCipher;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.objects.*;
import neoAtlantis.utilidades.accessController.profiler.interfaces.ProfilerWay;
import neoAtlantis.utilidades.accessController.scheduler.interfaces.SchedulerWay;
import neoAtlantis.utilidades.accessController.utils.SimpleAuthenticationServlet;
import neoAtlantis.utilidades.notifier.interfaces.Notifier;
import neoAtlantis.utilidades.objects.Event;
import org.apache.log4j.Logger;

/**
 * Control que autentica cuentas, valida permisos y registra los eventos de los 
 * mismos.<br>
 * <br>
 * Este objeto es el responsable de autenticar cuentas de usuario para acceder a 
 * una aplicacion.
 * Para hacerlo utiliza un {@link neoAtlantis.utilidades.accessController.authentication.interfaces.AuthenticationWay Medio de Autenticacion}
 * (archivo de texto, BD, etc.) el cual valida que sea un usuario permitido el que 
 * esta intentando acceder.<br>
 * <br>
 * Restringe el acceso mediante lo estipulado en un {@link neoAtlantis.utilidades.accessController.scheduler.interfaces.SchedulerWay Medio Calendarizador}
 * el cual define los tiempos de acceso permitidos a la aplicacion.<br>
 * <br>
 * Para llevar el control de quienes estan conectados y quienes estan bloqueados 
 * por mal uso de la cuenta, utiliza un
 * {@link neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay Medio de Bloqueo} 
 * (archivo de texto, BD, etc.)<br>
 * <br>
 * Finalmente para registrar los eventos que ocurren durante la autenticacion de 
 * cuentas y validacion de permisos, utiliza una
 * {@link neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay Medio Bitacoreador}
 * en donde se registran todos los eventos ocurridos.<br>
 * <br>
 * Para generar y confirgurar el objeto dentro de una aplicaci&oacute;n web, se 
 * puede apoyar con la utileria {@link neoAtlantis.utilidades.accessController.utils.AccessControllerPublisher}
 * @version 4.1
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class AccessController {
    static final Logger logger = Logger.getLogger(AccessController.class);

    /**
     * Versi&oacute;n de la clase.
     */
    public static final String version = "4.1";

    protected int maxIntentos = 3;
    protected AuthenticationWay autenticador;
    protected ProfilerWay rolador;
    protected AllowerWay permisor;
    protected BlockerWay bloqueador;
    protected AuditWay auditor;
    protected SchedulerWay calendarizador;
    protected DataCipher cifra;

    private Notifier notificador;
    private int intentos = 0;
    private boolean usuarioMultiple = false;
    private String confirmacion;
    
    private static AccessController access;

    /**
     * Genera un Control de acceso
     * @param aw Medio de autenticación
     * @param dc Cifrador de datos
     * @param pw Medio perfilador
     * @param alw Medio permisor
     * @param bw Medio bloqueador
     * @param auw Medio auditor
     * @param sw Medio temporizador
     * @throws Exception 
     */
    private AccessController(AuthenticationWay aw, DataCipher dc, ProfilerWay pw, AllowerWay alw, BlockerWay bw, AuditWay auw, SchedulerWay sw) throws Exception{
        this.autenticador=aw;
        this.cifra=dc;
        this.rolador=pw;
        this.permisor=alw;
        this.bloqueador=bw;
        this.auditor=auw;
        this.calendarizador=sw;

        logger.debug("Inicia la configuración del control de accesos con:");
        logger.debug("Authentication: '" + autenticador.getClass() + "'");
        logger.debug("Cipher: '" + cifra.getClass() + "'");
        logger.debug("Profiler: '" + rolador.getClass() + "'");
        logger.debug("Allower: '" + permisor.getClass() + "'");
        logger.debug("Blocker: '" + bloqueador.getClass() + "'");
        logger.debug("Audit: '" + auditor.getClass() + "'");
        logger.debug("Scheduler: '" + calendarizador.getClass() + "'");

        this.autenticador.setCifradorDatos(dc);
        this.maxIntentos=3;
    }

    /*public AccessController(AuthenticationWay aw, DataCipher dc, ProfilerWay pw, AllowerWay alw, BlockerWay bw, AuditWay auw) throws Exception{
        this(aw, dc, pw, alw, bw, auw, new GeneralScheduler());
    }

    public AccessController(AuthenticationWay aw, DataCipher dc, ProfilerWay pw, AllowerWay alw, BlockerWay bw) throws Exception{
        this(aw, dc, pw, alw, bw, new NullAudit());
    }

    public AccessController(AuthenticationWay aw, DataCipher dc, AllowerWay alw, BlockerWay bw) throws Exception{
        this(aw, dc, new NullProfiler(), alw, bw);
    }

    public AccessController(AuthenticationWay aw, DataCipher dc, BlockerWay bw) throws Exception{
        this(aw, dc, new NullAllower(), bw);
    }

    public AccessController(AuthenticationWay aw, DataCipher dc) throws Exception{
        this(aw, dc, new NullAllower(), new NullBlocker());
    }

    public AccessController(AuthenticationWay aw) throws Exception{
        this(aw, new CipherMd5Des("dataCipher"), new NullAllower(), new NullBlocker());
    }*/

    public static AccessController getInstance(AuthenticationWay aw, DataCipher dc, ProfilerWay pw, AllowerWay alw, BlockerWay bw, AuditWay auw, SchedulerWay sw) throws Exception{
        if( access==null ){
            access=new AccessController(aw, dc, pw, alw, bw, auw, sw);
        }
        
        return access;
    }
    
     public static AccessController getInstance(){
         /*if( access==null ){
             throw new NullPointerException("No existe instancia.");
         }*/
         
         return access;
     }
    
     @Override
     public String toString(){
         StringBuilder sb=new StringBuilder();
         
         sb.append("/*********************  AccessController  ********************//").append(System.getProperty("line.separator"));
         sb.append("Authentication: '").append(autenticador.getClass()).append(System.getProperty("line.separator"));
         sb.append("Cipher: '").append(cifra.getClass()).append(System.getProperty("line.separator"));
         sb.append("Profiler: '").append(rolador.getClass()).append(System.getProperty("line.separator"));
         sb.append("Allower: '").append(permisor.getClass()).append(System.getProperty("line.separator"));
         sb.append("Blocker: '").append(bloqueador.getClass()).append(System.getProperty("line.separator"));
         sb.append("Audit: '").append(auditor.getClass()).append(System.getProperty("line.separator"));
         sb.append("Scheduler: '").append(calendarizador.getClass()).append(System.getProperty("line.separator"));
         sb.append("/*************************************************************//").append(System.getProperty("line.separator"));
         
         return sb.toString();
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
    public User autenticaPersona(String origen, String terminal, EnvironmentType tipoTerminal, Map<String, Object> datos) throws WayAccessException {
        User user=null;

        logger.debug("Inicia Validación.");

         if (datos.isEmpty()) {
            throw new NullPointerException("No existe datos suficientes para realizar la autenticación.");
        }

        //termina bloqueos anteriores
        this.revisaBloqueosTerminados();

        //realiza verificación
        logger.debug("Realiza la autenticación del usuario.");
        try {
            user = this.autenticador.autenticaUsuario(datos);
            this.auditor.escribeBitacora(user, EventAudit.LOGEO, "Intenta acceder al sistema.");
        } catch (WayAccessException ex) {
            logger.error("Error al autenticar al usuario.", ex);
            throw ex;
        }
        user.setOrigen(origen);
        user.setTerminal(terminal);
        user.setTipoTerminal(tipoTerminal);

        //revisa si ya esta conectado
        if (this.usuarioMultiple == false) {
            logger.debug("Revisa si el usuario esta conectado.");
            if (this.bloqueador.verificaConexion(user)) {
                logger.debug("El usuario ya se encuentra conectado.");
                this.auditor.escribeBitacora(user, EventAudit.RECHAZO, "Es rechazado debido a que ya esta conectado");
                user.setEstado(ValidationResult.EN_USO);
            }
        }

        //revisa si esta bloqueado
        logger.debug("Revisa si el usuario esta bloqueado.");
        if (this.bloqueador.verificaBloqueo(user)) {
            //revisa si esta bloqueado
            logger.debug("Finaliza el accedo debido a que esta bloqueado.");
            this.auditor.escribeBitacora(user, EventAudit.RECHAZO, "Es rechazado debido a que se encuentra bloqueado");
            user.setEstado(ValidationResult.BLOQUEADO);
        }

        //revisa si esta dentro de los tiempos de acceso
        logger.debug("Revisa si el usuario esta accediento dentro de los tiempos establecidos.");
        if ( !this.calendarizador.existeDisponibilidad() ) {
            logger.debug("No se esta dentro de los horarios permitidos.");
            this.auditor.escribeBitacora(user, EventAudit.RECHAZO, "Es rechazado debido a que se esta fuera de los tiempos de acceso");

            user.setEstado(ValidationResult.FUERA_DE_TIEMPO);
            if( this.notificador!=null ){
                this.notificador.enviaNotificacion("SSO", "El usuario '"+user.getUser()+"', esta intentando acceder desde '"+user.getOrigen()+"' en un horario indevido.");
            }
        }
        
        //realiza la validación extra
        String valExtra=this.autenticador.validacionAdicional(user);
        if( valExtra!=null ){
            logger.debug("No se permitio el acceso por la validación extra '"+valExtra+"'.");
            this.auditor.escribeBitacora(user, EventAudit.RECHAZO, valExtra);

            user.setEstado(ValidationResult.DENEGADO_PROPIO);
        }

        if (user.getEstado()==ValidationResult.VALIDADO || user.getEstado()==ValidationResult.VALIDADO_TEMPORAL ) {
            //carga los roles asignados
            user.agregaRoles(this.rolador.obtieneRoles(user, new Object()));
            
            //reviso que tenga roles asignados
            if( user.getRoles().isEmpty() ){
                user.setEstado(ValidationResult.DENEGADO);
                this.auditor.escribeBitacora(user, EventAudit.INGRESO, "Es rechazado debido a que no tiene roles asignados.");
                logger.debug("Usuario rechazado por falta de roles.");
            }
            else{
                intentos = 0;
                this.bloqueador.agregaConexion(user);
                this.auditor.escribeBitacora(user, EventAudit.INGRESO, "Inicio de sesión");
                logger.debug("Usuario autenticado.");
                
                //asigno el permiso de logeado
                user.agregarPermiso(new Permission("logeado"));
                user.agregaRol(new Role("USUARIOS"));
            }
        }
        else if (user.getEstado()==ValidationResult.DENEGADO) {
            logger.debug("Contraseña no valida.");
            this.intentos++;
            this.auditor.escribeBitacora(user, EventAudit.RECHAZO, "El usuario no logro acceder por contraseña invalida, " + this.intentos + " intentos");
            logger.debug("Incrementa #intentos a " + intentos + ".");

            logger.debug("Revisa si el numero de intentos ha sido revasado.");
            if (this.intentos >= this.maxIntentos) {
                this.bloqueador.agregaBloqueo(user);

                //inicializa los intentos
                this.intentos = 0;
                logger.debug("El numero de intentos  ha sido revasado.");
                this.auditor.escribeBitacora(user, EventAudit.BLOQUEO, "Es bloqueado debido a que se ha revasado el numero de intentos");

                user.setEstado(ValidationResult.LIMITE_REBASADO);
                if( this.notificador!=null ){
                    this.notificador.enviaNotificacion("SSO", "El usuario '"+user.getUser()+"', ha exedido su numero de intentos.");
                }
            }
        }
        else if (user.getEstado()==ValidationResult.INACTIVO) {
            logger.debug("Usuario no activo.");
            this.auditor.escribeBitacora(user, EventAudit.RECHAZO, "El usuario no logro accesar por no estar activo.");
        }
        else if (user.getEstado()==ValidationResult.NO_ENCONTRADO) {
            logger.debug("Usuario no encontrado.");
            this.auditor.escribeBitacora(user, EventAudit.RECHAZO, "El usuario no logro accesar por no existir.");
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
    public User autenticaPersona(String origen, String terminal, Map<String, Object> datos) throws WayAccessException {
        return autenticaPersona(origen, terminal, EnvironmentType.STANDALONE, datos);
    }

    /**
     * Valida una cuenta
     * @param datos Información a validar, dependiendo del tipo de AuthenticationWay son el numero y tipo de datos a validar.
     * @return Evento obtenido por la validacion.
     * @throws WayAccessException
     */
    public User autenticaPersona(Map<String, Object> datos) throws WayAccessException {
        return autenticaPersona("127.0.0.1", "localhost", datos);
    }

    // -------------------- funciones del blocker -----------------------------
    
    /**
     * Termina la conexion de un usuario
     * @param user Usuario del que se desea terminar la conexion
     * @throws WayAccessException
     */
    public void finaliza(User user) throws WayAccessException {
        this.bloqueador.remueveConexion(user);

        this.intentos = 0;
        this.auditor.escribeBitacora(user, EventAudit.EGRESO, "Fin de sesión");
    }

    /**
     * Revisa los bloqueos y si han finalizado los remueve.
     * @throws WayAccessException
     */
    public void revisaBloqueosTerminados() throws WayAccessException {
        logger.debug("Busca bloqueos finalizados.");

        List<User> usrs = this.bloqueador.revisaBloqueosTerminados();
        for(User u: usrs){
            logger.debug("Finaliza el bloqueo de '" + u.getUser() + "'.");
        }
    }

    /**
     * Revisa las sesiones inactivas y si han finalizado las remueve.
     * @throws WayAccessException
     */
    public List<User> revisaSesionesInactivas() throws WayAccessException {
        logger.debug("Busca sesiones inactivas.");

        List<User> usrs = this.bloqueador.revisaSesionesInactivas();
        for(User u: usrs){
            logger.debug("Finaliza la session de '" + u.getUser() + "'.");            
        }
        
        return usrs;
    }

    /**
     * Recupera un usuario autenticado a partir de su id.
     * @param id Id del usuario a encontrar
     * @return Usuario encontrado o nullo si no encontro ninguno
     * @throws WayAccessException 
     */
    public User recuperaUsuarioConectado(String id) throws WayAccessException{
        return this.bloqueador.getUsuarioConectado(id);
    }
    
    /**
     * Recupera un usuario autenticado a partir de su sesi&oacute;n.
     * @param ses Sesi&oacute;n del usuario a encontrar
     * @return Usuario encontrado o nullo si no encontro ninguno
     * @throws WayAccessException 
     */
    public User recuperaUsuarioConectadoSession(String ses) throws WayAccessException{
        return this.bloqueador.getUsuarioConectadoSesion(ses);
    }
    
    /**
     * Recupera un usuario bloqueado por id.
     * @param id Idendificador del usuario a recuperar
     * @return Usuario encontrado o nullo si no encontro ninguno
     * @throws WayAccessException 
     */
    public User recuperaUsuarioBloqueado(String id) throws WayAccessException{
        return this.bloqueador.getUsuarioBloqueado(id);
    }
    
    /**
     * Recupera las sesiones que se encuentran activas.
     * @return Lista de usuarios con sesi&oacute;n activa
     * @throws WayAccessException 
     */
    public List<User> recuperaSesionesActivas() throws WayAccessException{
        return this.bloqueador.getConexiones();
    }
    
    /**
     * Elimina el bloqueo de un usuario
     * @param u Usuario del que se desea remover el bloqueo
     * @return true si lo logro remover
     * @throws WayAccessException 
     */
    public boolean eliminaBloqueo(User u) throws WayAccessException{
        return this.bloqueador.remueveBloqueo(u);
    }
    
    /**
     * Actualiza la fehca y hora de conexi&oacute;n.
     * @param user Usuario del que se desea actualizar
     * @throws WayAccessException 
     */
    public void actualizaConexionUsuario(User user) throws WayAccessException{
        this.bloqueador.actualizaActividadConexion(user);
    }

    /**
     * Recupera el tiempo de vida de una sesi&oacute;n.
     * @return Tiempo en segundos
     */
    public long getTiempoVidaSesion(){
        return this.bloqueador.getTiempoSesion();
    }

    // -------------------- funciones del autenticador -----------------------------

    /**
     * Realiza una busqueda de usuarios
     * @param param Parametros de busqueda
     * @return Lista de Usuarios encontrados
     */
    public List<User> buscaUsuarios(Map<String,Object> param) throws WayAccessException{
        return this.autenticador.buscaUsuarios(param);
    }
    
    /**
     * Recupera el codigo HTML para generar un login.
     * @param action Recurso a direcionar cuando se pida la solicitud de autenticaci&oacute;n
     * @param captchaService Recurso que brinda el servicio de captcha
     * @return Cadena con el HTML
     */
    public String getEntornoLogin(String action, String captchaService){
        return this.autenticador.generaEntornoAutenticacionWeb(action, captchaService);
    }

    /**
     * Recupera el codigo HTML para generar un login.
     * @param action Recurso a direcionar cuando se pida la solicitud de autenticaci&oacute;n
     * @return  Cadena con el HTML
     */
    public String getEntornoLogin(String action){
        return this.autenticador.generaEntornoAutenticacionWeb(action, null);
    }

    // -------------------- funciones del auditor -----------------------------
    
    /**
     * Genera una entrada de ingreso en la bitacora
     * @param user Usuario que genera la entrada
     * @param sistema Sistema al que ingresa
     * @throws WayAccessException 
     */
    public void escribeEventoIngreso(User user, String sistema) throws WayAccessException{
        this.auditor.escribeBitacora(user, EventAudit.INGRESO, "Ingresa al sistema '"+sistema+"'.");
    }
    
    /**
     * Genera una entrada en la bitacora
     * @param user Usuario que genera la entrada
     * @param evento Evento que se registra
     * @throws WayAccessException 
     */
    public void escribeEvento(User user, String evento) throws WayAccessException{
        escribeEvento(user, EventAudit.NEGOCIO, evento);
    }

    /**
     * Genera una entrada en la bitacora
     * @param user Usuario que genera la entrada
     * @param tipo Tipo de evento que se registra
     * @param evento Evento que se registra
     * @throws WayAccessException 
     */
    public void escribeEvento(User user, EventAudit tipo, String evento) throws WayAccessException{
        this.auditor.escribeBitacora(user, tipo, evento);
    }
    
    /**
     * Recupera información de la bitacora configurada.
     * @param param Parametros con los que realiza la busqueda
     * @param regs N&uacute;mero de registros a obtener
     * @param offset Posici&oacute;n desde la que toma los registros
     * @return Lista de registros
     */
    public List<Event> recuperaDatosBitacora(Map<String,Object> param, int regs, int offset){
        return this.auditor.recuperaBitacora(param, regs, offset);
    }

    // ---------------------------------------------------------------------------

    /**
     * Elimina la conexi&oacute;n de un usuario
     * @param idUser Idendificador del usuario a eliminar
     * @param admin Usuario que realiza el termino de la conexi&oacute;n
     * @return true si la logro eliminar
     */
    public boolean eliminaConexion(String idUser, User admin){
        boolean ev=false;
        User u=null;

        try{
            u=this.recuperaUsuarioConectado(idUser);
//            SimpleAuthenticationServlet.terminaSesion((HttpSession)u.getSesion().getAtributo(SimpleAuthenticationServlet.HTTP_SESSION));
            if( u!=null ){
                try{
                    finaliza(u);
                }catch(Exception ex){
                    logger.fatal("No se logro terminar la conexión", ex);
                }
            }

            if( u.getSesion()!=null ){
                SimpleAuthenticationServlet.terminaSesion(u.getSesion().getHttpSession());
            }

            logger.debug("Termina la conexion del usuario: "+u.getUser());
            escribeEvento(admin, EventAudit.EGRESO, admin.getUser()+" finaliza la sesión de "+u.getUser());
        }catch(Exception ex){
            logger.error("No se logro destruir la sesion de: "+u, ex);
        }
        
        return ev;
    }
    
    /**
     * Asigna un notificador para avisar de los eventos ocurridos durante la validacion
     * @param n Notificador
     */
    public void setNotificador(Notifier n){
        this.notificador=n;
    }

    /**
     * Asigna el numero maximo de intentos para poder autenticar una cuenta. Por default tiene 3.
     * @param maxIntentos the maxIntentos to set
     */
    public void setMaxIntentos(int maxIntentos) {
        this.maxIntentos = maxIntentos;
    }

    /**
     * Asigna la bandera, para permitir que varios usuarios con la misma cuenta puedan acceder. Por default tiene true
     * @param usuarioMultiple true si se desea activar
     */
    public void setUsuarioMultiple(boolean usuarioMultiple) {
        this.usuarioMultiple = usuarioMultiple;
    }

}
