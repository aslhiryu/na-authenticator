package neoAtlantis.utilidades.accessController.authentication.interfaces;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import neoAtlantis.utilidades.accessController.cipher.interfaces.DataCipher;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.objects.User;
import org.apache.log4j.Logger;

/**
 * Interface que define el comportamiento con el que debe contar un Medio Autenticador.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.0
 */
public abstract class AuthenticationWay {
    /**
     * Loggeador de la clase
     */
    private static Logger debugger=Logger.getLogger(AuthenticationWay.class);
    
    /**
     * Nombre para el parametro de las formas de html para el codigo de confirmacion
     */
    public static String CODE_PARAM="code";
    /**
     * Variable que le indica al servlet de autenticación que debe validar al usuario.
     */
    public static String PARAM_LOGIN="na_auth";
    /**
     * Defina la contraseña por default
     */
    protected String passDefault="Temporal1";
    /**
     * Cifrador que se utiliza para encriptar las contrase&ntilde;as (si este esta como null las contrase&ntilde;as no seran encriptadas).
     */
    protected DataCipher cifrador;

    //--------------------------------------------------------------------------------

    /**
     * Definici&oacute;n del metodo para validar usuarios.
     * @param datos Variables que se deben de utilizar para realizar la autenticación
     * @return Usuario que se encontro al realizar la autenticacion:
     * @throws java.lang.Exception
     */
    public abstract User autenticaUsuario(Map<String, Object> datos) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para agregar cuentas.
     * @param user Nickname del usuario
     * @param pass Contrase&ntilde;a del usuario
     * @return true, si logro agregar la cuenta
     * @throws java.lang.Exception
     */
    public abstract boolean agregaCuenta(User user, String pass) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para agregar cuentas temporales.
     * @param user Nickname del usuario
     * @return true, si logro agregar la cuenta
     * @throws java.lang.Exception
     */
    public abstract boolean agregaCuentaTemporal(User user) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para modificar una contrase&ntilde;a.
     * @param user Nickname del usuario
     * @param pass Nueva contrase&ntilde;a para el usuario
     * @return true, si logro modificar la cuenta
     * @throws java.lang.Exception
     */
    public abstract boolean modificaContrasena(User user, String pass) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo para inicializar una contrase&ntilde;a.
     * @param user Nickname del usuario
     * @return true, si logro modificar la cuenta
     * @throws java.lang.Exception
     */
    public abstract boolean restauraContrasena(User user) throws WayAccessException;

    public abstract String generaEntornoAutenticacionWeb(String action, String captchaService);
    
    /**
     * Definici&oacute;n del metodo que se ejecuta posterior a las validaciones de credenciales.
     * @param usuario Usuario al que se le realiza la evaluación adicional
     * @return nuul si no existe problema, o la cadena que describe el problema de validación encontrado
     */
    public abstract String validacionAdicional(User usuario) throws WayAccessException;
    
    /**
     * Definici&oacute;n del metodo que busca usuarios en el repositorio asignado.
     * @param param Datos del usuario a buscar.
     * @return Lista de usuarios encontrados
     * @throws WayAccessException 
     */
    public abstract List<User> buscaUsuarios(Map<String,Object> param) throws WayAccessException;
    
    //--------------------------------------------------------------------------------

    /**
     * Asigna un Cifrador para la encriptaci&oacute;n de las contrase&ntilde;as  (si este esta como null las contrase&ntilde;as no seran encriptadas).
     * @param c Cifrador de Datos
     */
    public void setCifradorDatos(DataCipher c){
        this.cifrador=c;
    }

    //--------------------------------------------------------------------------------

    /**
     * Metodo para validar la esuctrura de una contraseña
     * @param pass Contrasena a validar
     * @param tam Numero de caracteres de la que debe de estar conformada la contraseña
     * @param mays Numero de letras mayusculas minimas con los que debe de contar
     * @param nums Numero de digitos minimos con los que debe de contar
     * @param esps Numero de caracteres especiales minimos con los que debe de contar
     * @return true si es valida
     */
    public static boolean validaConstitucionContrasena(String pass, int tam, int mays, int nums, int esps) {
        //valida que no sea nulo
        if (pass == null) {
            throw new RuntimeException("La contraseña es nula.");
        }
        
        if (tam < 0 || mays < 0 || nums < 0 || esps < 0) {
            throw new RuntimeException("Alguno de los parametros es negativo.");
        }
        
        if (mays+nums+esps>tam) {
            throw new RuntimeException("La suma de los caracteres es mayor al tamaño de la contraseña");
        }
        
        if( pass.length()<tam ){
            debugger.debug("El password no cumple con el tamaño.");
            return false;
        }

        char[] cad = pass.toCharArray();
        int M = 0, m = 0, n = 0, e = 0;

        for (int i = 0; cad != null && i < cad.length; i++) {
            if (Character.isWhitespace(cad[i])) {
                debugger.debug("El password tiene espacios.");
                return false;
            } else if (Character.isDigit(cad[i])) {
                n++;
            } else if (Character.isUpperCase(cad[i])) {
                M++;
            } else if (Character.isLowerCase(cad[i])) {
                m++;
            } else {
                e++;
            }
        }

        debugger.debug("El password esta compuesto por M:"+M+", m:"+m+", n:"+n+", e:"+e+".");
        if (n >= nums && M >= mays && e >= esps) {
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
        StringBuilder sb = new StringBuilder("");
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
        StringBuilder sb = new StringBuilder("");
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
}
