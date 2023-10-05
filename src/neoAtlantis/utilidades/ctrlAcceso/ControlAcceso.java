package neoAtlantis.utilidades.ctrlAcceso;

import java.util.ArrayList;
import neoAtlantis.utilidades.ctrlAcceso.cipher.CifradorMd5Des;
import neoAtlantis.utilidades.ctrlAcceso.cipher.interfaces.CifradorDatos;
import neoAtlantis.utilidades.ctrlAcceso.recursoBitacora.interfaces.RecursoBitacorador;
import neoAtlantis.utilidades.ctrlAcceso.disponibilidad.*;
import neoAtlantis.utilidades.ctrlAcceso.disponibilidad.interfaces.Disponibilidad;
import neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.interfaces.RecusoAutenticador;
import neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.interfaces.ResultadoAutenticacion;
import neoAtlantis.utilidades.ctrlAcceso.recursoBitacora.interfaces.EventoBitacora;
import neoAtlantis.utilidades.ctrlAcceso.recursoBitacora.interfaces.TipoBitacora;
import neoAtlantis.utilidades.ctrlAcceso.recursoBloqueo.interfaces.RecursoBloqueador;
import neoAtlantis.utilidades.debuger.NullDebuger;
import neoAtlantis.utilidades.debuger.interfaces.Debuger;
import neoAtlantis.utilidades.logger.NullLogger;
import neoAtlantis.utilidades.logger.interfaces.Logger;
import neoAtlantis.utilidades.notifier.interfaces.Notifier;

/**
 * Control que autentica cuentas, valida permisos y registra los eventos de los mismos.
 * <br><br>
 * Este objeto es el responsable de autenticar cuentas de usuario para acceder a una aplicacion.
 * Para hacerlo utiliza un {@link neoAtlantis.utilidades.ctrlAcceso.medioAutenticacion.interfaces.RecusoAutenticador Medio de Autenticacion}
 * (archivo de texto, BD, etc.) el cual valida que sea un usuario permitido el que esta intentando acceder.
 * <br><br>
 * Restringe el acceso mediante lo estipulado en un {@link neoAtlantis.utilidades.ctrlAcceso.disponibilidad.interfaces.Disponibilidad Calendario de Disponibilidad}
 * el cual define los tiempos de acceso permitidos a la aplicacion.
 * <br><br>
 * Para llevar el control de quienes estan conectados y quienes estan bloqueados por mal uso de la cuenta, utiliza un
 * {@link neoAtlantis.utilidades.ctrlAcceso.medioBloqueo.interfaces.RecursoBloqueador Medio de Bloqueo} (archivo de texto, BD, etc.)
 * <br><br>
 * Finalmente para registrar los eventos que ocurren durante la autenticacion de cuentas y validacion de permisos, utiliza una
 * {@link neoAtlantis.utilidades.ctrlAcceso.bitacora.interfaces.RecursoBitacorador RecursoBitacorador}
 * en donde se registran todos los eventos ocurridos.
 * @version 3.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class ControlAcceso {

    /**
     * Versi&oacute;n de la clase.
     */
    public static final String version = "3.0";
    //public static final int PARAMETROS_DE_USUARIO_ERRONEOS = -1;

    private Debuger mDebug;
    private Logger mLog;
    private Notifier notificador;
    private boolean configurado = false;
    private int maxIntentos = 3;
    private int intentos = 0;
    private CifradorDatos cifra;
    private RecusoAutenticador medio;
    private RecursoBloqueador bloqueo;
    private RecursoBitacorador bitacora;
    private Disponibilidad disponibilidad;
    private boolean usuarioMultiple = false;
    private TipoBitacora nivelBitacora = TipoBitacora.ACCESO;

    /**
     * Genera un Control de Acceso
     * @param ma Autenticador que se utilizara para validar las cuentas
     * @param mb Bloqueador de Usuarios que se utilizara para getionar los bloqueos y conexiones de los usuarios
     * @param dis Calendario de Disponibilidad del acceso a la aplicacion
     * @param bit RecursoBitacorador que registrara los eventos que se efectuen
     * @throws java.lang.Exception
     * @see neoAtlantis.utilidades.ctrlAcceso.disponibilidad.DisponibilidadTotal
     * @see neoAtlantis.utilidades.ctrlAcceso.disponibilidad.DisponibilidadDiaHabil
     */
    public ControlAcceso(RecusoAutenticador ma, RecursoBloqueador mb, Disponibilidad dis, RecursoBitacorador bit) throws Exception {
        mDebug = new NullDebuger();
        mLog = new NullLogger();
        cifra = new CifradorMd5Des("llave");

        //valido la existencia del manipulador de acceso
        if (ma == null) {
            throw new Exception("El medio de Autenticación no es valido.");
        }
        if (mb == null) {
            throw new Exception("El medio de Bloqueos no es valido.");
        }
        if (bit == null) {
            throw new Exception("La bitacora no es valida.");
        }

        this.mDebug.escribeDebug(this.getClass(), "Configuro el Medio de Autenticacion '" + ma.getClass() + "'");

        this.medio = ma;
        this.bloqueo = mb;
        this.disponibilidad = dis;
        this.bitacora = bit;

        this.medio.setMDebug(this.mDebug);
        this.medio.setMLog(this.mLog);
        this.medio.setCifradorDatos(cifra);
        this.bloqueo.setMDebug(this.mDebug);
        this.bloqueo.setMLog(this.mLog);
        this.disponibilidad.setMDebug(this.mDebug);
        this.disponibilidad.setMLog(this.mLog);

        this.setMaxIntentos(3);
        this.revisaBloqueosTerminados();

        this.configurado = true;
    }

    /**
     * Genera un Control de Acceso, con un Calendario de Disponibilidad Total
     * @param ma Autenticador que se utilizara para validar las cuentas
     * @param mb Bloqueador de Usuarios que se utilizara para getionar los bloqueos y conexiones de los usuarios
     * @param bit RecursoBitacorador que registrara los eventos que se efectuen
     * @throws java.lang.Exception
     * @see neoAtlantis.utilidades.ctrlAcceso.disponibilidad.DisponibilidadTotal
     */
    public ControlAcceso(RecusoAutenticador ma, RecursoBloqueador mb, RecursoBitacorador bit) throws Exception {
        this(ma, mb, new DisponibilidadTotal(), bit);
    }

    /**
     * Valida una cuenta
     * @param user Usuario a validar.
     * @param pass Contrase&ntilde;a del usuario.
     * @return Evento obtenido por la validacion:
     * @throws Exception
     */
    public ResultadoAutenticacion validaPersona(Usuario user, String pass) throws Exception {
        if (user == null) {
            throw new NullPointerException("Sin usuario para validar");
        }

        this.mDebug.escribeDebug(this.getClass(), "Inicia Validación.");
        if( this.nivelBitacora==TipoBitacora.COMPLETA ){
            this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.LOGEO, "Intenta acceder al sistema.");
        }

        //termina bloqueos anteriores
        this.revisaBloqueosTerminados();

        //revisa si esta conectado
        if (this.usuarioMultiple == false) {
            this.mDebug.escribeDebug(this.getClass(), "Revisa si el usuario esta conectado.");
            if (this.bloqueo.verificaConexion(user)) {
                this.mDebug.escribeDebug(this.getClass(), "El usuario ya se encuentra conectado.");
                if( this.nivelBitacora==TipoBitacora.COMPLETA || this.nivelBitacora==TipoBitacora.ACCESO ){
                    this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.RECHAZADO, "Es rechazado debido a que ya esta conectado");
                }
                return ResultadoAutenticacion.USUARIO_CONECTADO;
            }
        }

        //revisa si esta bloqueado
        this.mDebug.escribeDebug(this.getClass(), "Revisa si el usuario esta bloqueado.");
        boolean userBlock = this.bloqueo.verificaBloqueo(user);
        if (userBlock) {
            //revisa si esta bloqueado
            this.mDebug.escribeDebug(this.getClass(), "Finaliza el accedo debido a que esta bloqueado.");
            if( this.nivelBitacora==TipoBitacora.COMPLETA || this.nivelBitacora==TipoBitacora.ACCESO ){
                this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.RECHAZADO, "Es rechazado debido a que se encuentra bloqueado");
            }
            return ResultadoAutenticacion.USUARIO_BLOQUEADO;
        }

        //revisa si esta dentro de los tiempos de acceso
        this.mDebug.escribeDebug(this.getClass(), "Revisa si el usuario esta accediento dentro de los tiempos establecidos.");

        if ( !this.disponibilidad.existeDisponibilidad() ) {
            this.mDebug.escribeDebug(this.getClass(), "No se esta dentro de los horarios permitidos.");
            if( this.nivelBitacora==TipoBitacora.COMPLETA || this.nivelBitacora==TipoBitacora.ACCESO ){
                this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.RECHAZADO, "Es rechazado debido a que se esta fuera de los tiempos de acceso");
            }

            return ResultadoAutenticacion.ACCESO_FUERA_DE_TIEMPO;
        }

        //revisa su numero de intentos erroneos
        this.mDebug.escribeDebug(this.getClass(), "Revisa si el numero de intentos no ha sido revasado.");
        if (this.intentos >= this.maxIntentos) {
            this.bloqueo.agregaBloqueo(user);

            //inicializa los intentos
            this.intentos = 0;
            this.mDebug.escribeDebug(this.getClass(), "El numero de intentos  ha sido revasado.");
            if( this.nivelBitacora==TipoBitacora.COMPLETA ){
                this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.BLOQUEADO, "Es bloqueado debido a que se ha revasado el numero de intentos");
            }

            return ResultadoAutenticacion.LIMITE_REVASADO;
        }

        //realiza verificación
        this.mDebug.escribeDebug(this.getClass(), "Compara las cuentas.");

        ResultadoAutenticacion opc = ResultadoAutenticacion.ACCESO_DENEGADO;
        try {
            opc = this.medio.validaUsuario(user.getUser(), pass);
        } catch (Exception ex) {
            if (this.mLog != null) {
                this.mLog.escribeLog(this.getClass(), "Error al decifrar la contraseña.", ex);
            }

            throw new Exception("Error al descifrar la contraseña. " + ex.getMessage());
        }

        if (opc==ResultadoAutenticacion.ACCESO_AUTORIZADO || opc==ResultadoAutenticacion.ACCESO_AUTORIZADO_TEMPORAL ) {
            intentos = 0;
            this.bloqueo.agregaConexion(user);
            if( this.nivelBitacora==TipoBitacora.COMPLETA || this.nivelBitacora==TipoBitacora.ACCESO ){
                this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.VALIDADO, "El usuario es autenticado satisfactoriamente");
            }
            this.mDebug.escribeDebug(this.getClass(), "Usuario autenticado.");

            //cargo los datos del usuario
            user.setNombre(this.medio.obtieneElementoNombre(user.getUser()));
            user.setId(this.medio.obtieneElementoId(user.getUser()));
            user.agregarPermiso("logeado");
            user.agregaRol(new Rol("usuarios"));
            return opc;
        }

        else if (opc == ResultadoAutenticacion.ACCESO_DENEGADO) {
            this.mDebug.escribeDebug(this.getClass(), "Contraseña no valida.");
            if( this.nivelBitacora==TipoBitacora.COMPLETA || this.nivelBitacora==TipoBitacora.ACCESO ){
                this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.RECHAZADO, "El usuario no logro accesar por contraseña invalida, " + this.intentos + " intentos");
            }
            this.intentos++;
            this.mDebug.escribeDebug(this.getClass(), "Incrementa #intentos a " + intentos + ".");
        } else if (opc == ResultadoAutenticacion.USUARIO_NO_ACTIVO) {
            this.mDebug.escribeDebug(this.getClass(), "Usuario no activo.");
                if( this.nivelBitacora==TipoBitacora.COMPLETA || this.nivelBitacora==TipoBitacora.ACCESO ){
            this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.RECHAZADO, "El usuario no logro accesar por no estar activo.");
            }
        } else if (opc == ResultadoAutenticacion.USUARIO_NO_ENCONTRADO) {
            this.mDebug.escribeDebug(this.getClass(), "Usuario no encontrado.");
            if( this.nivelBitacora==TipoBitacora.COMPLETA || this.nivelBitacora==TipoBitacora.ACCESO ){
                this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.RECHAZADO, "El usuario no logro accesar por no existir.");
            }
        /*} else if (opc == ResultadoAutenticacion.ACCESO_AUTORIZADO_CADUCADO) {
            this.mDebug.escribeDebug(this.getClass(), "Usuario no esta caducado.");
            if( this.nivelBitacora==BITACORA_COMPLETA || this.nivelBitacora==BITACORA_ACCESO ){
                this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), RecursoBitacorador.RECHAZADO, "El usuario no logro accesar por estar caducado.");
            }*/
        } else {
            if (this.mLog != null) {
                this.mLog.escribeLog(this.getClass(), "No se encontro el valor "+opc+".", new Exception());
            }
            throw new Exception("Se genero un evento desconocido al intentar validar la cuenta.");
        }

        return opc;
    }

    /**
     * Valida un permiso de un usuario.
     * @param permiso Permiso a validar
     * @param user Usuario que se desea validar
     * @param recurso Recurso al que se esta intentando acceder
     * @return Evento obtenido por la validacion:
     * @throws java.lang.Exception
     */
    public ResultadoAutenticacion validaPermiso(String permiso, Usuario user, String recurso) throws Exception {
        this.mDebug.escribeDebug(this.getClass(), "Intenta validar el permiso '" + permiso + "'.");

        //revisa si esta bloqueado
        this.mDebug.escribeDebug(this.getClass(), "Revisa si existe bloqueo del usuario.");

        boolean userBlock = this.bloqueo.verificaBloqueo(user);

        if (userBlock) {
            this.mDebug.escribeDebug(this.getClass(), "Usuario bloqueado.");
            if( this.nivelBitacora==TipoBitacora.COMPLETA ){
                this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.BLOQUEADO, "Intenta la validación del permiso '" + permiso + "' para acceder a '"+recurso+"', con resultado denegado");
            }
            return ResultadoAutenticacion.USUARIO_BLOQUEADO;
        }

        if (user.validaPermiso(permiso)) {
            this.mDebug.escribeDebug(this.getClass(), "Permiso valido.");
            if( this.nivelBitacora==TipoBitacora.COMPLETA ){
                this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.VALIDADO, "Intenta la validación del permiso '" + permiso + "' para acceder a '"+recurso+"', con resultado autorizado");
            }
            this.bloqueo.agregaConexion(user);

            return ResultadoAutenticacion.ACCESO_AUTORIZADO;
        }

        this.mDebug.escribeDebug(this.getClass(), "Permiso no valido.");
        if( this.nivelBitacora==TipoBitacora.COMPLETA ){
            this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.RECHAZADO, "Intenta la validación del permiso '" + permiso + "' para acceder a '"+recurso+"', con resultado denegado");
        }

        return ResultadoAutenticacion.ACCESO_DENEGADO;
    }

    /**
     * Valida que se cumplan todo un grupo de permisos.
     * @param permisos Arreglo con los permisos
     * @param user Usuario que se desea validar
     * @param recurso Recurso al que se esta intentando acceder
     * @return Evento obtenido por la validacion:
     * @throws java.lang.Exception
     */
    public ResultadoAutenticacion validaTodosPermisos(String[] permisos, Usuario user, String recurso) throws Exception {
        if (permisos == null || permisos.length == 0) {
            return ResultadoAutenticacion.ACCESO_DENEGADO;
        }

        for (int i = 0; i < permisos.length; i++) {
            if (this.validaPermiso(permisos[i], user, recurso) != ResultadoAutenticacion.ACCESO_AUTORIZADO) {
                return ResultadoAutenticacion.ACCESO_DENEGADO;
            }
        }

        return ResultadoAutenticacion.ACCESO_AUTORIZADO;
    }

    /**
     * Valida que se cumplan cualquier permiso de un grupo de permisos.
     * @param permisos Arreglo con los permisos
     * @param user Usuario que se desea validar
     * @param recurso Recurso al que se esta intentando acceder
     * @return Evento obtenido por la validacion:
     * @throws java.lang.Exception
     */
    public ResultadoAutenticacion validaAlgunPermiso(String[] permisos, Usuario user, String recurso) throws Exception {
        if (permisos == null || permisos.length == 0) {
            return ResultadoAutenticacion.ACCESO_DENEGADO;
        }

        for (int i = 0; i < permisos.length; i++) {
            if (this.validaPermiso(permisos[i], user, recurso) == ResultadoAutenticacion.ACCESO_AUTORIZADO) {
                return ResultadoAutenticacion.ACCESO_AUTORIZADO;
            }
        }

        return ResultadoAutenticacion.ACCESO_DENEGADO;
    }


    /**
     * Valida que se cumplan todo un grupo de permisos.
     * @param permisos Coleccion con los permisos
     * @param user Usuario que se desea validar
     * @param recurso Recurso al que se esta intentando acceder
     * @return Evento obtenido por la validacion:
     * @throws java.lang.Exception
     */
    public ResultadoAutenticacion validaTodosPermisos(ArrayList permisos, Usuario user, String recurso) throws Exception {
        if (permisos == null) {
            return ResultadoAutenticacion.ACCESO_DENEGADO;
        }

        String[] per = new String[permisos.size()];
        for (int i = 0; i < permisos.size(); i++) {
            per[i] = (String) permisos.get(i);
        }
        return validaTodosPermisos(per, user, recurso);
    }

    /**
     * Valida que se cumplan cualquier permiso de un grupo de permisos.
     * @param permisos Coleccion con los permisos
     * @param user Usuario que se desea validar
     * @param recurso Recurso al que se esta intentando acceder
     * @return Evento obtenido por la validacion:
     * @throws java.lang.Exception
     */
    public ResultadoAutenticacion validaAlgunPermiso(ArrayList permisos, Usuario user, String recurso) throws Exception {
        if (permisos == null) {
            return ResultadoAutenticacion.ACCESO_DENEGADO;
        }

        String[] per = new String[permisos.size()];
        for (int i = 0; i < permisos.size(); i++) {
            per[i] = (String) permisos.get(i);
        }
        return validaAlgunPermiso(per, user, recurso);
    }

    /**
     * Termina la conexion de un usuario
     * @param user Usuario del que se desea terminar la conexion
     * @throws Exception
     */
    public void finaliza(Usuario user) throws Exception {
        this.bloqueo.remueveConexion(user);

        this.intentos = 0;
        if( this.nivelBitacora==TipoBitacora.COMPLETA || this.nivelBitacora==TipoBitacora.ACCESO ){
            this.bitacora.escribeBitacora(user.getUser(), user.getTerminal(), user.getOrigen(), EventoBitacora.LOGEO, "Finaliza la sessión del usuario");
        }
    }

    /**
     * Recupera un elemento de los atributos disponibles por el Medio de autenticacion.
     * @param user Usuario del que se desea el elemento
     * @param elemento Elemento a recuperar
     * @return Valor del elemento
     * @see neoAtlantis.utilidades.ctrlAcceso.medioBloqueo.interfaces.RecursoBloqueador
     */
    public String obtieneElemento(Usuario user, String elemento) throws Exception {
        return this.medio.obtieneElemento(user.getUser(), elemento);
    }

    /**
     * Revisa los bloqueos y si han finalizado los remueve.
     * @throws Exception
     */
    public void revisaBloqueosTerminados() throws Exception {
        this.mDebug.escribeDebug(this.getClass(), "Busca bloqueos finalizados.");

        ArrayList objs = this.bloqueo.revisaBloqueosTerminados();
        for (int i = 0; objs != null && i < objs.size(); i++) {
            this.mDebug.escribeDebug(this.getClass(), "Finaliza el bloqueo de '" + objs.get(i) + "'.");
        }
    }

    /**
     * Asigna un notificador para avisar de los eventos ocurridos durante la validacion
     * @param n Notificador
     */
    public void setNotificador(Notifier n){
        this.notificador=n;
    }

    /**
     * Asigna un Debuger a la clase para poder dar seguimiento a los procesos que realiza la clase.
     * @param mDebug the mDebug to set
     */
    public void setMDebug(Debuger mDebug) {
        if( mDebug==null ){
            return;
        }

        this.mDebug = mDebug;
        if( this.medio!=null ){
            this.medio.setMDebug(mDebug);
        }
        if( this.bloqueo!=null ){
            this.bloqueo.setMDebug(mDebug);
        }
        if( this.disponibilidad!=null ){
            this.disponibilidad.setMDebug(mDebug);
        }
    }

    /**
     * Asigna un Loger a la clase para poder registrar los errores que se proboquen en la clase.
     * @param mLog the mLog to set
     */
    public void setMLog(Logger mLog) {
        if( mLog==null ){
            return;
        }

        this.mLog = mLog;
        if( this.medio!=null ){
            this.medio.setMLog(mLog);
        }
        if( this.bloqueo!=null ){
            this.bloqueo.setMLog(mLog);
        }
        if( this.disponibilidad!=null ){
            this.disponibilidad.setMLog(mLog);
        }
    }

    /**
     * Obtiene si el Control esta debidamente configurado
     * @return true si esta configurado
     */
    public boolean isConfigurado() {
        return configurado;
    }

    /**
     * Asigna el numero maximo de intentos para poder autenticar una cuenta. Por default tiene 3.
     * @param maxIntentos the maxIntentos to set
     */
    public void setMaxIntentos(int maxIntentos) {
        this.maxIntentos = maxIntentos;
    }

    /**
     * Recupera el Cifrador de datos utilizado para encriptar las contrase&ntilde;as.
     * @return the cifra
     */
    public CifradorDatos getCifra() {
        return cifra;
    }

    /**
     * Asigna un Cifrador de datos para encriptar las contrase&ntilde;as.
     * Si se asigana null, las contrase&ntilde;as no seran encriptadas. Por default tiene un CifradorMd5Des
     * @param cifra Cifrador de datos
     */
    public void setCifra(CifradorDatos cifra) {
        this.cifra = cifra;
        if( this.medio!=null ){
            this.medio.setCifradorDatos(cifra);
        }
    }

    /**
     * Asigna la bandera, para permitir que varios usuarios con la misma cuenta puedan acceder. Por default tiene true
     * @param true si se desea activar
     */
    public void setUsuarioMultiple(boolean usuarioMultiple) {
        this.usuarioMultiple = usuarioMultiple;
    }

    /**
     * Asigna el nivel de captura de eventos que registrara la bitacora
     * @param nivelBitacora Nivel de captura:
     * {@link #BITACORA_ACCESO},
     * {@link #BITACORA_COMPLETA},
     * {@link #BITACORA_NULA}
     */
    public void setNivelBitacora(TipoBitacora nivelBitacora) {
        this.nivelBitacora = nivelBitacora;
    }

    /*
    public void setOrigen(String origen){
        this.origen=origen;
        if( this.bitacora!=null ){
            this.bitacora.setOrigen(origen);
        }
        if( this.bloqueo!=null ){
            this.bloqueo.setOrigen(origen);
        }
        if( this.disponibilidad!=null ){
            this.disponibilidad.setOrigen(origen);
        }
        if( this.mDebug!=null ){
            this.mDebug.setOri(origen);
        }
        if( this.mLog!=null ){
            this.mLog.setOri(origen);
        }
        if( this.medio!=null ){
            this.medio.setOrigen(origen);
        }
    }*/
}
