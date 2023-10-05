package neoAtlantis.utils.accessController.authentication.interfaces;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import neoAtlantis.utils.accessController.audit.interfaces.AuditWay;
import neoAtlantis.utils.accessController.audit.interfaces.EventAudit;
import neoAtlantis.utils.accessController.exceptions.WayAccessException;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.apps.catalogs.objetcs.DataType;
import neoAtlantis.utils.apps.catalogs.objetcs.MemoryColumn;
import neoAtlantis.utils.apps.catalogs.objetcs.MemoryTable;
import neoAtlantis.utils.apps.catalogs.objetcs.OrderType;
import neoAtlantis.utils.cipher.interfaces.DataCipher;
import org.apache.log4j.Logger;

/**
 * Interface que define el comportamiento que se debe considerar para una via de autenticación
 * @author desarrollo.alberto
 */
public abstract class AuthenticationWay {
    /**
     * Loggeador de la clase
     */
    private static Logger debugger=Logger.getLogger(AuthenticationWay.class);
    
    public static final String USER_DATA="NA:UserUser";
    
    public static final String ID_FIELD="ID";
    public static final String NAME_FIELD="NAME";
    public static final String LOGIN_FIELD="LOGIN";
    public static final String PASSWORD_FIELD="PASS";
    public static final String MAIL_FIELD="MAIL";
    public static final String STATUS_FIELD="STATUS";
    public static final String LIFE_FIELD="LIFE";
    public static final String RESET_PASSWORD_FIELD="RESET_PASS";
    public static final String LAST_ACCESS_FIELD="LAST_ACCESS";

    /**
     * Variable que le indica al servlet de autenticación que debe validar al usuario.
     */
    public static final String LOGIN_PARAM="na_authenticate";
    public static final String ID_PARAM="NA:"+ID_FIELD;
    public static final String NAME_PARAM="NA:"+NAME_FIELD;
    public static final String USER_PARAM="NA:"+LOGIN_FIELD;
    public static final String PASS_PARAM="NA:"+PASSWORD_FIELD;
    public static final String MAIL_PARAM="NA:"+MAIL_FIELD;
    public static final String STATUS_PARAM="NA:"+STATUS_FIELD;
    public static final String LIFE_PARAM="NA:"+LIFE_FIELD;
    public static final String RESET_PASS_PARAM="NA:"+RESET_PASSWORD_FIELD;
    /**
     * Defina la contraseña por default
     */
    protected String defaultPass="Temporal1";
    /**
     * Cifrador que se utiliza para encriptar las contrase&ntilde;as (si este esta como null las contrase&ntilde;as no seran encriptadas).
     */
    protected DataCipher cifrador;
    
    protected AuditWay auditor;

    protected MemoryTable principalEntity;
    
    protected List<MemoryTable> relatedEntities;    
    /**
     * Define el tiempo de vida (en d&iacute;as) que puede permanecer un usuario sin registrar accion alguna, 0 indica que no tiene tiempo
     */
    protected int loginLife =0;
    
    public AuthenticationWay(){
        this.principalEntity=new MemoryTable("user");
        MemoryColumn c;
        
        c=new MemoryColumn(ID_FIELD);
        c.setKey(true);
        c.setUnique(true);
        this.principalEntity.addColumn(c);
        c=new MemoryColumn(NAME_FIELD);
        this.principalEntity.addColumn(c);
        c=new MemoryColumn(LOGIN_FIELD);
        c.setUnique(true);
        this.principalEntity.addColumn(c);
        c=new MemoryColumn(PASSWORD_FIELD);
        this.principalEntity.addColumn(c);
        c=new MemoryColumn(MAIL_FIELD);
        this.principalEntity.addColumn(c);
        c=new MemoryColumn(STATUS_FIELD);
        c.setCapture(false);
        c.setType(DataType.BOOLEAN);
        this.principalEntity.addColumn(c);
        c=new MemoryColumn(LIFE_FIELD);
        c.setType(DataType.DATE);
        this.principalEntity.addColumn(c);
        c=new MemoryColumn(RESET_PASSWORD_FIELD);
        c.setType(DataType.BOOLEAN);
        this.principalEntity.addColumn(c);
        c=new MemoryColumn(LAST_ACCESS_FIELD);
        c.setType(DataType.DATE);
        this.principalEntity.addColumn(c);
    }
    
    
    
    
    
    //--------------------------------------------------------------------------------

    /**
     * Definici&oacute;n del metodo para validar usuarios.
     * @param datos Variables que se deben de utilizar para realizar la autenticación
     * @return Usuario que se encontro al realizar la autenticacion:
     * @throws java.lang.Exception
     */
    public abstract User authenticateUser(final Map<String, Object> datos) throws WayAccessException;

    /**
     * Definici&oacute;n del metodo que se ejecuta posterior a las validaciones de credenciales.
     * @param usuario Usuario al que se le realiza la evaluación adicional
     * @return nuul si no existe problema, o la cadena que describe el problema de validación encontrado
     */
    public abstract String additionalValidation(final User usuario) throws WayAccessException;
    
    /**
     * Regresa la lista de usuarios registrados
     * @return Lista de usuarios
     * @throws WayAccessException 
     */
    public abstract List<User> getRegisteredUserList(final String order,final  OrderType orderType) throws WayAccessException;
    
    public abstract Map<String, Object> createUser(final Map<String, Object> datos) throws WayAccessException;
    
    public abstract Map<String, Object> updateUser(final Map<String, Object> datos) throws WayAccessException;
    
    public abstract User getUserData(final String id) throws WayAccessException;
    
    public abstract boolean allowsUpdatePassword() throws WayAccessException;
    
    public abstract boolean allowsUpdateUser() throws WayAccessException;
    
    public abstract boolean allowsCreateUser() throws WayAccessException;
    
    public abstract boolean activeUser(final String id, final boolean active) throws WayAccessException;
    
    public abstract boolean updateLastAccess(final String id) throws WayAccessException;
    
     public abstract List<User> validateLoginLifes() throws WayAccessException;
    
    
    
    
    
    //--------------------------------------------------------------------------------

    /**
     * Asigna un Cifrador para la encriptaci&oacute;n de las contrase&ntilde;as  (si este esta como null las contrase&ntilde;as no seran encriptadas).
     * @param c Cifrador de Datos
     */
    public void setDataCipher(DataCipher c){
        this.cifrador=c;
    }
    
    public void setAuditWay(AuditWay audit){
        this.auditor=audit;
    }

    public void setPrincipalEntity(MemoryTable principal){
        this.principalEntity=principal;
    }
    
    public void setRelatedEntities(List<MemoryTable> entities){
        this.relatedEntities=entities;
    }
    
    public void writeAuditEvent(User user, String msg){
        if( this.auditor!=null ){
            this.auditor.writeEvent(user, EventAudit.LOGGED, msg);
        }
    }

    /**
     * Asigna el tiempo de vida (en d&iacute;as) que puede permanecer un usuario sin registrar accion alguna, 0 indica que no tiene tiempo
     * @param life numero de d&iacute;as
     */
    public void setLoginLife(int life){
        this.loginLife=life;
    }
    
    /**
     * Recupera el tiempo de vida (en d&iacute;as) que puede permanecer un usuario sin registrar accion alguna,, 0 indica que no tiene tiempo
     * @return 
     */
    public int getLoginLife(){
        return this.loginLife;
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
    public static boolean validatePolityPass(String pass, int tam, int mays, int nums, int esps) {
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
    public static String createPass(int cars, int nums, int esps) {
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
    public static String createConfirmationCode(int tam) {
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
