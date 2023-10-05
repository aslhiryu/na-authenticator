package neoAtlantis.utilidades.accessController.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
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
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SortControl;
import neoAtlantis.utilidades.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utilidades.accessController.authentication.interfaces.ValidationResult;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utilidades.accessController.objects.EnvironmentType;
import neoAtlantis.utilidades.accessController.objects.Role;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.ldap.ConfigurationLDAP;
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
public class LoginLDAPAutentication extends AuthenticationWay {
    static final String  SORT_CONTROL_OID = "1.2.840.113556.1.4.473";
    
    /**
     * Loggeador de la clase
     */
    static final Logger logger = Logger.getLogger(LoginLDAPAutentication.class);

    /**
     * Configuración de conexi&oacute;n al LDAP.
     */
    protected Properties config;

    private Hashtable auth = new Hashtable();

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LoginLDAPAutentication(InputStream xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationLDAP.parseConfiguracionXML(xml);
            
            this.generaContexto();
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LoginLDAPAutentication(File xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationLDAP.parseConfiguracionXML(new FileInputStream(xml));
            
            this.generaContexto();
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LoginLDAPAutentication(String xml) throws WayConfigurationException {
        this(new File(xml));
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param props Configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LoginLDAPAutentication(Properties props) throws WayConfigurationException{
        try{
            ConfigurationLDAP.validaConfigProperties(props);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.config=props;
        this.generaContexto();
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
    public LoginLDAPAutentication(String url, String base, String bindType, String dataUser, String dataMail, String dataName, String order) throws WayConfigurationException {
        this.config.setProperty("url", url);
        this.config.setProperty("base", base);
        this.config.setProperty("bindType", bindType);
        this.config.setProperty("dataUser", dataUser);
        this.config.setProperty("dataMail", dataMail);
        this.config.setProperty("dataName", dataName);
        this.config.setProperty("order", order);
        try{
            ConfigurationLDAP.validaConfigProperties(this.config);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        
        this.generaContexto();
    }

    //---------------------------------------------------------------------------------

    /**
     * Valida un usuario.
     * @param datos Variables que se deben de utilizar para realizar la autenticación
     * @return Usuario que se encontro al realizar la autenticacion:
     * @throws java.lang.Exception
     */
    @Override
    public User autenticaUsuario(Map<String, Object> datos) throws WayAccessException {
        User user=User.getNadie();

        if( datos.get("user")==null ){
            throw new NullPointerException("No se el valor de 'user', para el autenticador.");
        }
        else if( datos.get("pass")==null ){
            throw new NullPointerException("No se el valor de 'pass', para el autenticador.");
        }
        
        //si no viene el usuario
        if( datos.get("user").toString().isEmpty() ){
            user=new User("Nobody", "Desconocido", "127.0.0.1", "localhost", EnvironmentType.WEB);
            user.setEstado(ValidationResult.SIN_USUARIO);
            return user;
        }
        
        String dn=getCadenaDN(this.config.getProperty("bindType"), this.config.getProperty("base"), (String)datos.get("user"));
        String filtro=this.config.getProperty("dataUser")+"="+datos.get("user");
        SearchResult sr;
        Attributes attrs;
        Attribute at;
        
        //revisa si es el administrador
        if( ((String)datos.get("user")).equalsIgnoreCase("admin") ){
            Properties pTmp=new Properties();
            
            try{
                pTmp.load(new FileInputStream(this.config.getProperty("filePass")));

                if( pTmp.getProperty("admin")!=null && !pTmp.getProperty("admin").isEmpty() ){
                    if( this.cifrador.cifra((String)datos.get("pass")).equals(pTmp.getProperty("admin")) ){
                        logger.debug("Coincide la contraseña del 'admin', carga info.");
                        user=new User("0", "admin", "127.0.0.1", "localhost", EnvironmentType.WEB);
                        user.setEstado(ValidationResult.VALIDADO);
                        user.setNombre("Administrador del Sistema");
                        user.setMail(pTmp.getProperty("adminmail"));
                        user.agregaRol(new Role("administrador"));
                        
                        cargaDatosExtras(null, user);
                        return user;
                    }
                    else{
                        logger.debug("No coincide la contraseña del 'admin' '"+datos.get("pass")+"'.");
                        user.setEstado(ValidationResult.DENEGADO);
                        return user;
                    }
                }
                else{
                    logger.fatal("No se ubico la cuenta del 'admin'");
                    user.setEstado(ValidationResult.NO_ENCONTRADO);
                    return user;
                }
            }
            catch(Exception ex){
                logger.fatal("No se logro cargar el archivo de passwords: "+this.config.getProperty("filePass"), ex);
                throw new WayAccessException(ex);
            }
        }
        
        auth.put(Context.SECURITY_PRINCIPAL, dn);
        auth.put(Context.SECURITY_CREDENTIALS, datos.get("pass"));
        
        //si es un usuario normal
        logger.debug("Comunicando con el servidor '" + this.config.getProperty("url") + "'.");
        logger.debug("Busca la rama '" + dn + "'.");
        
        try {
            user=new User((String)datos.get("user"), (String)datos.get("user"), "127.0.0.1", "localhost", EnvironmentType.WEB, false);
            user.setEstado(ValidationResult.VALIDADO);
            cargaDatosExtras(null, user);

            DirContext ctx = new InitialDirContext(auth);   
            logger.debug("Se encontro ("+this+") el usuario: "+datos.get("user"));
            
            //recupero información del usuario
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration res = ctx.search(this.config.getProperty("base"), filtro, searchCtls);
            logger.debug("Realiza la busqueda del usuario con: "+filtro);
            if( res.hasMore() ){
                logger.debug("Encontro al usuario.");
                
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
                        user.setNombre((String)at.get());
                    }
                }
                cargaDatosExtras(attrs, user);
                   
            }
            
            ctx.close();
        } catch (AuthenticationException authEx) {
            logger.debug("No existe el usuario '"+datos.get("user")+"': "+authEx);
            user.setEstado(ValidationResult.DENEGADO);
        } catch (NamingException namEx) {
            logger.error("No se logro contactar con el LDAP.", namEx);
            throw new WayAccessException(namEx);
        }
        
        return user;
    }
    
    @Override
    public boolean agregaCuenta(User user, String pass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean agregaCuentaTemporal(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Modifica la contrase&ntilde;a de un usuario.
     * @param user Nickname del usuario
     * @param pass Nueva contrase&ntilde;a para el usuario
     * @return true, si logro modificar la cuenta
     * @throws java.lang.Exception
     */
    @Override
    public boolean modificaContrasena(User user, String pass) throws WayAccessException {
        logger.debug("Intento modificar la contraseña de :"+user);
        
        //revisa si es el administrador
        if( user!=null && user.getUser().equalsIgnoreCase("admin") ){
            if( user.getEstado()!=ValidationResult.VALIDADO  && user.getEstado()==ValidationResult.VALIDADO_TEMPORAL ){
                logger.debug("El usuario '"+user+"' no esta autenticado.");
                return false;
            }
            
            Properties pTmp=new Properties();
            
            try{
                pTmp.load(new FileInputStream(this.config.getProperty("filePass")));
            }
            catch(Exception ex){
                logger.fatal("No se logro cargar el archivo de passwords: "+this.config.getProperty("filePass"), ex);
                throw new WayAccessException(ex);
            }
         
            //cambia la contraseña
            pTmp.setProperty("admin", pass);
            try{
                pTmp.store(new FileOutputStream(this.config.getProperty("filePass")), "");
            }
            catch(Exception ex){
                logger.fatal("No se logro cargar el archivo de passwords: "+this.config.getProperty("filePass"), ex);
                throw new WayAccessException(ex);
            }
            
            logger.debug("Se modifico la contraseña de :"+user);
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public boolean restauraContrasena(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String generaEntornoAutenticacionWeb(String action, String captchaService) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String validacionAdicional(User usuario) {
        return null;
    }
    
    /**
     * Realiza la busqueda de usuarios en el LDAP.
     * @param param Datos del usuario a buscar.
     * @return Lista de usuarios encontrados
     * @throws WayAccessException 
     */
    @Override
    public List<User> buscaUsuarios(Map<String, Object> param) throws WayAccessException{
        ArrayList<User> lTmp=new ArrayList<User>();
        String filtro="(| ("+this.config.getProperty("dataUser")+"=*"+param.get("cadenaBusqueda")+"*)("+this.config.getProperty("dataName")+"=*"+param.get("cadenaBusqueda")+"*))";
        SearchResult sr;
        User u;
        Attributes attrs;
        Attribute at;
        String c;
        
        logger.debug("Busca personas ("+this+") con contexto: "+auth);
        
        try{
            LdapContext ctx=new InitialLdapContext(auth, null);

            //para ordernar
            if(!isSortControlSupported(ctx)){
                logger.info("El servidor no soporta ordenamiento por (SortControl).");
            }
            else{
                ctx.setRequestControls(new Control[]{new SortControl(this.config.getProperty("order"), Control.CRITICAL)});
                logger.debug("Ordena resultados por: "+this.config.getProperty("order"));
            }
            
            logger.debug("Realiza la busqueda para: "+filtro);
            //para buscar
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration res = ctx.search(this.config.getProperty("base"), filtro, searchCtls);
            
            while( res.hasMore() ){
                sr = (SearchResult)res.next();
                
                attrs = sr.getAttributes();
                c=null;

                if (attrs != null) {            
                    //recupera el usuario
                    at=attrs.get(this.config.getProperty("dataUser"));
                    if( at!=null ){
                        c=(String)at.get();
                    }


                    if( c==null || c.isEmpty() ){
                        continue;
                    }
                    u=new User(c, "127.0.0.1", "localhost");

                    //recupera el correo
                    at=attrs.get(this.config.getProperty("dataMail"));
                    if( at!=null ){
                        u.setMail((String)at.get());
                    }
                    //recupera el nombre
                    at=attrs.get(this.config.getProperty("dataName"));
                    if( at!=null ){
                        u.setNombre((String)at.get());
                    }                

                    lTmp.add(u);
                }
            }
            
            res.close();
            ctx.close();
            logger.debug("Cerrando conexion LDAP");
        }catch(Exception ex){
            throw new WayAccessException(ex);
        }
        
        return lTmp;
    }
    
    //---------------------------------------------------------------------------------

    /**
     * Carga información adicional del usuario a partir de la información en el LDAP.
     * @param res Attributes del registro encontrado
     * @param user Usuario al que se va asigna los datos
     */
    protected void cargaDatosExtras(Attributes res, User user){
        //nada
    }

    //---------------------------------------------------------------------------------
    
    private boolean isSortControlSupported(LdapContext ctx)
            throws NamingException {
        SearchControls ctl = new SearchControls();
        ctl.setReturningAttributes(new String[]{"supportedControl"});
        ctl.setSearchScope(SearchControls.OBJECT_SCOPE);

        /* search for the rootDSE object */
        NamingEnumeration results = ctx.search("", "(objectClass=*)", ctl);

        while (results.hasMore()) {
            SearchResult entry = (SearchResult) results.next();
            NamingEnumeration attrs = entry.getAttributes().getAll();
            while (attrs.hasMore()) {
                Attribute attr = (Attribute) attrs.next();
                NamingEnumeration vals = attr.getAll();
                while (vals.hasMore()) {
                    String value = (String) vals.next();
                    if (value.equals(SORT_CONTROL_OID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void generaContexto() throws WayConfigurationException{
        logger.debug("Genera contexto de LDAP con: "+this.config);
        
        try{
            auth.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
            auth.put(Context.PROVIDER_URL, this.config.getProperty("url"));
            auth.put(Context.SECURITY_AUTHENTICATION, "simple");
            auth.put(Context.REFERRAL, "follow");
            auth.put("com.sun.jndi.ldap.read.timeout", ""+Integer.parseInt(this.config.getProperty("timeout"))*1000);
        }catch(Exception ex){
            logger.error("No se logro validar la configuración de LDAP.", ex);
            throw new WayConfigurationException(ex);
        }
        
        logger.debug("Genera configuración de conexión al LDAP ("+this+")");
    }

    private String getCadenaDN(String tipo, String base, String user){
        StringBuilder sb=new StringBuilder();
        
        if(tipo!=null && tipo.equalsIgnoreCase("activedirectory")){
            logger.debug("Autenticación por Active Directory.");

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
            logger.debug("Autenticación por LDAP.");

            sb.append("cn=").append(user).append(",").append(base);
        }
        
        return sb.toString();
    }

}
