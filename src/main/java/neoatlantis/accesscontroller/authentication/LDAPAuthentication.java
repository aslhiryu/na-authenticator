package neoAtlantis.utils.accessController.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import neoAtlantis.utils.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utils.accessController.authentication.interfaces.ValidationResult;
import neoAtlantis.utils.accessController.exceptions.WayAccessException;
import neoAtlantis.utils.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utils.accessController.objects.EnvironmentType;
import neoAtlantis.utils.accessController.objects.Role;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.apps.catalogs.objetcs.OrderType;
import neoAtlantis.utils.ldap.ConfigurationLDAP;
import org.apache.log4j.Logger;

/**
 * Medio Autenticador operado a traves de LDAP.<br>
* Para poder generar el objeto se hace uso de un documento XML con la siguiente 
 * estructura:<br>
 * <pre>
 * &lt;ldap&gt;
 *   &lt;url&gt;<i>url_de_conexi&oacute;n_al_LDAP</i>&lt;/url&gt;
 *   &lt;base&gt;<i>contexto_inicial_de_busqueda_de_usuarios</i>&lt;/base&gt;
 *   &lt;bindType&gt;<i>tipo_de_conexi&oacute;n</i>&lt;/bindType&gt;
 *   &lt;dataUser&gt;<i>campo_que_contiene_la_clave_del_usuario</i>&lt;/dataUser&gt;
 *   &lt;dataMail&gt;<i>campo_que_contiene_el_mail_del_usuario</i>&lt;/dataMail&gt;
 *   &lt;dataName&gt;<i>campo_que_contiene_el_nombre_del_usuario</i>&lt;/dataName&gt;
 *   &lt;order&gt;<i>campos_utilizados_para_ordenar_resultados</i>&lt;/order&gt;
 *   &lt;timeout&gt;<i>tiempo_en_segundos_en_que_espera_respuesta_del_servidor</i>&lt;/timeout&gt;
 * &lt;/ldap&gt;
 * </pre>
 * Otra opci&oacute;n para generar el objeto es con un <b>java.util.Properties</b>, para lo cual 
 * deber&aacute; contar con los siguientes datos:
 * <pre>
 * url=<i>url_de_conexi&oacute;n_al_LDAP</i>
 * base=<i>contexto_inicial_de_busqueda_de_usuarios</i>
 * bindType=<i>tipo_de_conexi&oacute;n</i>
 * dataUser=<i>campo_que_contiene_la_clave_del_usuario</i>
 * dataMail=<i>campo_que_contiene_el_mail_del_usuario</i>
 * dataName=<i>campo_que_contiene_el_nombre_del_usuario</i>
 * order=<i>campos_utilizados_para_ordenar_resultados</i>
 * timeout=<i>tiempo_en_segundos_en_que_espera_respuesta_del_servidor</i>
 * </pre>
 * Los datos obligatorios son <i>url</i> y <i>base</i>.<br>
 * <i>bindType</i> solo acepta los valores de 'ldap' y 'activedirectory'.<br>
 * En caso de no definir <i>bindType</i> se asigna por default el valor de 'ldap'.<br>
 * En caso de no definir <i>dataUser</i> se asigna por default el valor de 'sAMAccountName'.<br>
 * En caso de no definir <i>dataMail</i> se asigna por default el valor de 'mail'.<br>
 * En caso de no definir <i>dataName</i> se asigna por default el valor de 'displayName'.<br>
 * En caso de no definir <i>order</i> se asigna por default el valor de 'sAMAccountName'.<br>
 * En caso de no definir <i>timeout</i> se asigna por default el valor de '10'.<br>
 * <br><br>
 * Para trabajar adecuamente este objeto requiere la libreria de <b>NA_Utils</b>.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.0
 */
public class LDAPAuthentication extends AuthenticationWay {
    private static final Logger DEBUGER = Logger.getLogger(LDAPAuthentication.class);
    
    public static final String  SORT_CONTROL_OID = "1.2.840.113556.1.4.473";
    public static final String  ACTIVE_DIRECTORY = "activedirectory";

    protected Properties config;    
    protected Hashtable auth = new Hashtable();
    
    

    // Contructores ------------------------------------------------------------

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LDAPAuthentication(InputStream xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationLDAP.parseXmlConfiguration(xml);
            
            this.createContext();
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LDAPAuthentication(File xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationLDAP.parseXmlConfiguration(new FileInputStream(xml));
            
            this.createContext();
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LDAPAuthentication(String xml) throws WayConfigurationException {
        this(new File(xml));
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param props Configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LDAPAuthentication(Properties props) throws WayConfigurationException{
        try{
            ConfigurationLDAP.validateConfig(props);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.config=props;
        this.createContext();
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param url URL del LDAP
     * @param base Contexto base para la busqueda de usuarios
     * @param bindType Tipo de conexion [ LDAP | ACTIVEDIRECTORY [
     * @param dataUser Campo que contiene la clave del usuario
     * @param dataMail Campo que contiene el mail del usuario
     * @param dataName Campo que contiene el nombre del usuario
     * @param order Campos utilizados para ordenar resultados
     * @throws WayConfigurationException 
     */
    public LDAPAuthentication(String url, String base, String bindType, String dataUser, String dataMail, String dataName, String order) throws WayConfigurationException {
        this.config.setProperty("url", url);
        this.config.setProperty("base", base);
        this.config.setProperty("bindType", bindType);
        this.config.setProperty("dataUser", dataUser);
        this.config.setProperty("dataMail", dataMail);
        this.config.setProperty("dataName", dataName);
        this.config.setProperty("order", order);
        try{
            ConfigurationLDAP.validateConfig(this.config);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        
        this.createContext();
    }



    // Metodos publicos---------------------------------------------------------
    
    @Override
    public User authenticateUser(Map<String, Object> datos) throws WayAccessException {
        User user=User.getNobody();

        if( datos.get(AuthenticationWay.USER_PARAM)==null ){
            throw new NullPointerException("No se el valor de 'user', para el autenticador.");
        }
        else if( datos.get(AuthenticationWay.PASS_PARAM)==null ){
            throw new NullPointerException("No se el valor de 'pass', para el autenticador.");
        }
        
        //si no viene el usuario
        if( datos.get(USER_PARAM).toString().isEmpty() ){
            user.setState(ValidationResult.NOT_USER);
            return user;
        }
        
        String dn=getDNString(this.config.getProperty("bindType"), this.config.getProperty("base"), (String)datos.get(AuthenticationWay.USER_PARAM));
        String filtro=this.config.getProperty("dataUser")+"="+datos.get(AuthenticationWay.USER_PARAM);
        SearchResult sr;
        Attributes attrs;
        Attribute at;
        
        //revisa si es el administrador
        if( ((String)datos.get(AuthenticationWay.USER_PARAM)).equalsIgnoreCase("admin") ){
            Properties pTmp=new Properties();
            
            try{
                pTmp.load(new FileInputStream(this.config.getProperty("filePass")));

                if( pTmp.getProperty("admin")!=null && !pTmp.getProperty("admin").isEmpty() ){
                    if( this.cifrador.cipher((String)datos.get(AuthenticationWay.PASS_PARAM)).equals(pTmp.getProperty("admin")) ){
                        DEBUGER.debug("Coincide la contraseña del 'admin', carga info.");
                        user=new User("0", "admin", "127.0.0.1", "localhost", EnvironmentType.WEB);
                        user.setState(ValidationResult.VALIDATE);
                        user.setName("Administrador del Sistema");
                        user.setMail(pTmp.getProperty("adminmail"));
                        user.addRole(new Role("administrador"));
                        
                        this.loadExtraData(null, user);
                        return user;
                    }
                    else{
                        DEBUGER.debug("No coincide la contraseña del 'admin' '"+datos.get(AuthenticationWay.PASS_PARAM)+"'.");
                        user.setState(ValidationResult.DENIED);
                        return user;
                    }
                }
                else{
                    DEBUGER.fatal("No se ubico la cuenta del 'admin'");
                    user.setState(ValidationResult.NOT_FOUND);
                    return user;
                }
            }
            catch(Exception ex){
                DEBUGER.fatal("No se logro cargar el archivo de passwords: "+this.config.getProperty("filePass"), ex);
                throw new WayAccessException(ex);
            }
        }
        
        auth.put(Context.SECURITY_PRINCIPAL, dn);
        auth.put(Context.SECURITY_CREDENTIALS, datos.get(AuthenticationWay.PASS_PARAM));
        
        //si es un usuario normal
        DEBUGER.debug("Comunicando con el servidor '" + this.config.getProperty("url") + "'.");
        DEBUGER.debug("Busca la rama '" + dn + "'.");
        
        try {
            user=new User((String)datos.get(AuthenticationWay.USER_PARAM), (String)datos.get(AuthenticationWay.USER_PARAM), "127.0.0.1", "localhost", EnvironmentType.WEB, false);
            user.setState(ValidationResult.VALIDATE);
            this.loadExtraData(null, user);

            DirContext ctx = new InitialDirContext(auth);   
            DEBUGER.debug("Se encontro ("+this+") el usuario: "+datos.get(AuthenticationWay.USER_PARAM));
            
            //recupero información del usuario
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration res = ctx.search(this.config.getProperty("base"), filtro, searchCtls);
            DEBUGER.debug("Realiza la busqueda del usuario con: "+filtro);
            if( res.hasMore() ){
                DEBUGER.debug("Encontro al usuario.");
                
                sr = (SearchResult)res.next();
                attrs = sr.getAttributes();
                
                if (attrs != null) {                    
                    //recupera el correo
                    at=attrs.get(this.config.getProperty("dataMail"));
                    if( at!=null ){
                        user.setMail((String)at.get());
                    }
                    //recupera el nombre
                    at=attrs.get(this.config.getProperty("dataName"));
                    if( at!=null ){
                        user.setName((String)at.get());
                    }
                }
                this.loadExtraData(attrs, user);
                   
            }
            
            ctx.close();
        } catch (AuthenticationException authEx) {
            DEBUGER.debug("No existe el usuario '"+datos.get(AuthenticationWay.USER_PARAM)+"': "+authEx);
            user.setState(ValidationResult.DENIED);
        } catch (NamingException namEx) {
            DEBUGER.error("No se logro contactar con el LDAP.", namEx);
            throw new WayAccessException(namEx);
        }
        
        return user;
    }

    @Override
    public String additionalValidation(User usuario) throws WayAccessException {
        return null;
    }
    
    @Override
    public List<User> getRegisteredUserList(String order, OrderType orderType) throws WayAccessException{
        return new ArrayList();
    }
    
    @Override
    public Map<String, Object> createUser(Map<String, Object> datos) throws WayAccessException {
        return new HashMap<String, Object> ();
    }

    @Override
    public boolean allowsUpdatePassword() throws WayAccessException{
        return false;
    }
    
    @Override
    public boolean allowsUpdateUser() throws WayAccessException{
        return false;
    }
    
    @Override
    public boolean allowsCreateUser() throws WayAccessException{
        return false;
    }
    
    @Override
    public  List<User> validateLoginLifes() throws WayAccessException{
        return new ArrayList<User>();
    }
    
    @Override
    public User getUserData(String id) throws WayAccessException {
        return null;
    }
    
    @Override
    public Map<String, Object> updateUser(Map<String, Object> datos) throws WayAccessException{
        return new HashMap<String, Object> ();
    }

    @Override
    public boolean activeUser(String id, boolean active) throws WayAccessException{
        return true;
    }
    
    @Override
    public boolean updateLastAccess(String id) throws WayAccessException{
        return true;
    }
    
    
    
    
    
    

    // Metodos protegidos-------------------------------------------------------

    /**
     * Prepara el contexto para conectarse al LDAP
     * @throws WayConfigurationException 
     */
    protected void createContext() throws WayConfigurationException{
        DEBUGER.debug("Genera contexto de LDAP con: "+this.config);
        
        try{
            auth.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
            auth.put(Context.PROVIDER_URL, this.config.getProperty("url"));
            auth.put(Context.SECURITY_AUTHENTICATION, "simple");
            auth.put(Context.REFERRAL, "follow");
            auth.put("com.sun.jndi.ldap.read.timeout", ""+Integer.parseInt(this.config.getProperty("timeout"))*1000);
        }catch(Exception ex){
            DEBUGER.error("No se logro validar la configuración de LDAP.", ex);
            throw new WayConfigurationException(ex);
        }
        
        DEBUGER.debug("Genera configuración de conexión al LDAP ("+this+")");
    }

    /**
     * Carga información adicional del usuario a partir de la información en el LDAP.
     * @param res Attributes del registro encontrado
     * @param user Usuario al que se va asigna los datos
     */
    protected void loadExtraData(Attributes res, User user){
        //nada
    }
    
    
    
    // Metodos publicos y estaticos---------------------------------------------

    /**
     * Recupera la cadena de conexión al LDAP
     * @param tipo Tipo de LDAP 'activedirectory' o 'normal'
     * @param base Base para la conexión
     * @param user Usuario con el que se conecta
     * @return 
     */
    public static String getDNString(String tipo, String base, String user){
        StringBuilder sb=new StringBuilder();
        
        if(tipo!=null && tipo.equalsIgnoreCase(ACTIVE_DIRECTORY)){
            DEBUGER.debug("Autenticación por Active Directory.");

            sb.append(user).append("@");
            if( base!=null ){
                String[] cTmp=base.split(",");
                
                for(int i=0; cTmp!=null&&i<cTmp.length; i++){
                    if( cTmp[i].toLowerCase().indexOf("dc=")==0 ){
                        if(i>0){
                            sb.append(".");
                        }
                        sb.append(cTmp[i].substring(3));
                    }
                }                
            }
        }
        else{
            DEBUGER.debug("Autenticación por LDAP.");

            sb.append("cn=").append(user).append(",").append(base);
        }
        
        return sb.toString();
    }
    
    public static String getDetailError(String error){
        DEBUGER.debug("Error  a evaluar: "+error);
        
        if( error!=null && error.trim().startsWith("[LDAP: error code 49") && error.indexOf(" error, data ")>0 ){
            int posIni=error.indexOf(" error, data ")+13;
            int posFin=error.indexOf(",", posIni+1);
            String errorCode="";
            
            DEBUGER.debug("Posicional Inicial: "+posIni+", Posicion final: "+posFin);
            errorCode=error.substring(posIni, posFin);
            DEBUGER.debug("Codigo de error: "+errorCode);
            
            if(errorCode.equals("525")){
                return "User not found";
            }
            else if(errorCode.equals("52e​")){
                return "Invalid credentials";
            }
            else if(errorCode.equals("530​")){
                return "Not permitted to logon at this time​";
            }
            else if(errorCode.equals("531​")){
                return "Not permitted to logon at this workstation​";
            }
            else if(errorCode.equals("532​")){
                return "Password expired";
            }
            else if(errorCode.equals("533")){
                return "Account disabled";
            }
            else if(errorCode.equals("701​")){
                return "Account expired";
            }
            else if(errorCode.equals("773​")){
                return "User must reset password";
            }
            else if(errorCode.equals("775​")){
                return "User account locked";
            }
            else{
                return "Unknow error";
            }            
        }
        else{
            return "Unknow detail";
        }
    }
}
