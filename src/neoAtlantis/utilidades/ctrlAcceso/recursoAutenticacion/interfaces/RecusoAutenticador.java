package neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.interfaces;

import java.util.Calendar;
import java.util.Random;
import neoAtlantis.utilidades.ctrlAcceso.Usuario;
import neoAtlantis.utilidades.ctrlAcceso.cipher.interfaces.CifradorDatos;
import neoAtlantis.utilidades.debuger.interfaces.Debuger;
import neoAtlantis.utilidades.logger.interfaces.Logger;

/**
 * Interface que define el comportamiento que debe de tener un <i>Autenticador</i>.
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public abstract class RecusoAutenticador {

    /**
     * Versi&oacute;n de la clase.
     */
    public static String VERSION="1.0";
    /**
     * Debuger que da seguimiento a los procesos que realiza la clase.
     */
    protected Debuger mDebug;
    /**
     * Loger que registra los errores que se probocan en la clase.
     */
    protected Logger mLog;
    /**
     * Cifrador que se utiliza para encriptar las contrase&ntilde;as (si este esta como null las contrase&ntilde;as no seran encriptadas).
     */
    protected CifradorDatos cifrador;

    /**
     * Define el metodo para validar usuarios.
     * @param user Nickname del usuario
     * @param pass Contrase&ntilde;a del usuario
     * @return Evento resultado de la validaci&oacute;n:
     * @throws java.lang.Exception
     */
    public abstract ResultadoAutenticacion validaUsuario(String user, String pass) throws Exception;

    /**
     * Define el metodo para agregar cuentas.
     * @param user Nickname del usuario
     * @param pass Contrase&ntilde;a del usuario
     * @return true, si logro agregar la cuenta
     * @throws java.lang.Exception
     */
    public abstract boolean agregaCuenta(Usuario user, String pass) throws Exception;

    /**
     * Define el metodo para agregar cuentas temporales.
     * @param user Nickname del usuario
     * @return true, si logro agregar la cuenta
     * @throws java.lang.Exception
     */
    public abstract boolean agregaCuentaTemporal(Usuario user) throws Exception;

    /**
     * Define el metodo para modificar una contrase&ntilde;a.
     * @param user Nickname del usuario
     * @param pass Nueva contrase&ntilde;a para el usuario contrase&ntilde;a
     * @return true, si logro modificar la cuenta
     * @throws java.lang.Exception
     */
    public abstract boolean modificaContrasena(Usuario user, String pass) throws Exception;

    /**
     * Define el metodo para obtener un elemento del usuario.
     * @param user Nickname del usuario
     * @param elemento Nombre del elemento deseado
     * @return Valor del elemento
     * @throws java.lang.Exception
     */
    public abstract String obtieneElemento(String usuario, String elemento) throws Exception;

    /**
     * Define el metodo para obetener el elemento '<i>nombre</i>' del usuario.
     * @param user Nickname del usuario
     * @return Valor del elemento
     * @throws java.lang.Exception
     */
    public abstract String obtieneElementoNombre(String usuario) throws Exception;

    /**
     * Define el metodo para obetener el elemento '<i>id</i>' del usuario.
     * @param user Nickname del usuario
     * @return Valor del elemento
     * @throws java.lang.Exception
     */
    public abstract String obtieneElementoId(String usuario) throws Exception;

    /**
     * Asigna un Debuger a la clase para poder dar seguimiento a los procesos que realiza la clase.
     * @param mDebug the mDebug to set
     */
    public void setMDebug(Debuger mDebug) {
        this.mDebug = mDebug;
    }

    /**
     * Asigna un Loger a la clase para poder registrar los errores que se proboquen en la clase.
     * @param mLog the mLog to set
     */
    public void setMLog(Logger mLog) {
        this.mLog = mLog;
    }

    /**
     * Metodo para validar la esuctrura de una contraseña
     * @param pass Contrasena a validar
     * @param cars Numero de caracteres minimos con los que debe de contar
     * @param nums Numero de digitos minimos con los que debe de contar
     * @param esps Numero de caracteres especiales minimos con los que debe de contar
     * @return true si es valida
     */
    public static boolean validaContrasena(String pass, int cars, int nums, int esps) {
        //valida que no sea nulo
        if (pass == null || cars < 0 || nums < 0 || esps < 0) {
            return false;
        }

        char[] cad = pass.toCharArray();
        int c = 0, n = 0, e = 0;

        for (int i = 0; cad != null && i < cad.length; i++) {
            if (Character.isWhitespace(cad[i])) {
                return false;
            } else if (Character.isDigit(cad[i])) {
                n++;
            } else if (Character.isLetter(cad[i])) {
                c++;
            } else {
                e++;
            }
        }

        if (n >= nums && c >= cars && e >= esps) {
            return true;
        }

        return false;
    }

    /**
     * Metodo para generar una contraseña
     * @param cars Numero de caracteres con los que debe de contar
     * @param nums Numero de digitos con los que debe de contar
     * @param esps Numero de caracteres especiales minimos con los que debe de contar
     * @return Contraseña generada
     */
    public static String generaContrasena(int cars, int nums, int esps) {
        StringBuffer sb = new StringBuffer("");
        int c = 0, n = 0, e = 0, tmp;
        Random r = new Random(Calendar.getInstance().getTimeInMillis());
        char ch;

        while (true) {
            tmp = r.nextInt(126);

            if (tmp < 33) {
                continue;
            }

            ch = (char) tmp;
//System.out.println("- "+tmp+", "+ch);
            if (Character.isWhitespace(ch)) {
                continue;
            } else if (Character.isDigit(ch) && n < nums) {
                n++;
                sb.append(ch);
            } else if (Character.isLetter(ch) && c < cars) {
                c++;
                sb.append(ch);
            } else if (e < esps) {
                e++;
                sb.append(ch);
            }

            if (c == cars && n == nums && e == esps) {
                break;
            }
        }

        return sb.toString();
    }

    /**
     * Metodo para genera una clave de confirmación
     * @param tam Tamaño de la clave
     * @return Clave generada
     */
    public static String generaClaveConfirmacion(int tam) {
        StringBuffer sb = new StringBuffer("");
        int c = 0, tmp;
        Random r = new Random(Calendar.getInstance().getTimeInMillis());
        char ch;

        while (true) {
            tmp = r.nextInt(122);

            if (tmp < 48 || (tmp > 57 && tmp < 65) || (tmp > 91 && tmp < 96)) {
                continue;
            }

            ch = (char) tmp;

            if (Character.isLetterOrDigit(ch)) {
                sb.append(ch);
                c++;
            }

            if (c == tam) {
                break;
            }
        }

        return sb.toString();
    }

    /**
     * Asigna un Cifrador para la encriptaci&oacute;n de las contrase&ntilde;as  (si este esta como null las contrase&ntilde;as no seran encriptadas).
     * @param c Cifrador de Datos
     */
    public void setCifradorDatos(CifradorDatos c){
        this.cifrador=c;
    }
}
