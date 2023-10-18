package neoatlantis.accesscontroller.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import static neoatlantis.accesscontroller.authentication.LDAPAuthentication.getDNString;
import neoatlantis.accesscontroller.authentication.interfaces.AuthenticationWay;
import neoatlantis.accesscontroller.authentication.interfaces.ValidationResult;
import neoatlantis.accesscontroller.exceptions.WayAccessException;
import neoatlantis.accesscontroller.exceptions.WayConfigurationException;
import neoatlantis.accesscontroller.objects.EnvironmentType;
import neoatlantis.accesscontroller.objects.User;
import neoatlantis.utils.ldap.ConfigurationAD;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class ADProvisioningAuthentication extends LDAPProvisioningAuthentication {
    private static final Logger DEBUGER = Logger.getLogger(ADProvisioningAuthentication.class);
    
    public static final String BUSSINESS_UNIT="unit";
    public static final String EMPLOYMENT="employment";
    public static final String BOSS="boss";
    public static final String PHONE="telephone";

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws WayConfigurationException
     */
    public ADProvisioningAuthentication(InputStream xml, InputStream xmlDB) throws WayConfigurationException {
        super(xml, xmlDB);
        
        try{
            this.config=ConfigurationAD.parseXmlConfiguration(xml);            
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    public ADProvisioningAuthentication(InputStream xml, InputStream xmlDB, Properties fields) throws WayConfigurationException {
        super(xml, xmlDB, fields);
        
        try{
            this.config=ConfigurationAD.parseXmlConfiguration(xml);            
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws WayConfigurationException
     */
    public ADProvisioningAuthentication(File xml, File xmlDB) throws WayConfigurationException {
        super(xml, xmlDB);
        
        try{
            this.config=ConfigurationAD.parseXmlConfiguration(new FileInputStream(xml));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    public ADProvisioningAuthentication(File xml, File xmlDB, Properties fields) throws WayConfigurationException {
        super(xml, xmlDB, fields);

        try{
            this.config=ConfigurationAD.parseXmlConfiguration(new FileInputStream(xml));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws WayConfigurationException
     */
    public ADProvisioningAuthentication(String xml, String xmlDB) throws WayConfigurationException {
        this(new File(xml), new File(xmlDB));
    }

    public ADProvisioningAuthentication(String xml, String xmlDB, Properties fields) throws WayConfigurationException {
        this(new File(xml), new File(xmlDB), fields);
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param props Configuraci&oacute;n de acceso al LDAP
     * @throws WayConfigurationException
     */
    public ADProvisioningAuthentication(Properties props, Properties propsDB) throws WayConfigurationException{
        super(props, propsDB);

        try{
            ConfigurationAD.validateConfig(props);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.configDB=propsDB;
    }

    public ADProvisioningAuthentication(Properties props, Properties propsDB, Properties fields) throws WayConfigurationException{
        super(props, propsDB, fields);

        try{
            ConfigurationAD.validateConfig(props);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.configDB=propsDB;
    }
    



    // Metodos publicos---------------------------------------------------------
    
    @Override
    public User authenticateUser(Map<String, Object> datos) throws WayAccessException {
        DirContext ctx=null;
        User user=User.getNobody();
        User uTmp;
        String cTmp;
        String username;

        if( datos.get(AuthenticationWay.USER_PARAM)==null ){
            throw new NullPointerException("No se el valor de 'user', para el autenticador.");
        }
        else if( datos.get(AuthenticationWay.PASS_PARAM)==null ){
            throw new NullPointerException("No se el valor de 'pass', para el autenticador.");
        }
        
        //si no viene el usuario
        if( datos.get(AuthenticationWay.USER_PARAM).toString().isEmpty() ){
            user.setState(ValidationResult.NOT_USER);
            return user;
        }
        
        username=""+datos.get(AuthenticationWay.USER_PARAM);
        //valida si el usuario se proporciono con dominio, ej. usuario@domino.com
        if( username.indexOf('@')>0 ){
            username=username.substring(0, username.indexOf('@'));
        }
        //valida si el usuario se proporciono con dominio, ej. dominio\\usuario
        if(username.indexOf('\\')>0){
            username=username.substring(username.indexOf('\\')+1, username.length());
        }
        String dn=getDNString(ACTIVE_DIRECTORY, this.config.getProperty("base"), username);
        String filtro=this.config.getProperty("dataUser")+"="+username;
        SearchResult sr;
        Attributes attrs=null;
        Attribute at;
        
        auth.put(Context.SECURITY_PRINCIPAL, dn);
        auth.put(Context.SECURITY_CREDENTIALS, datos.get(AuthenticationWay.PASS_PARAM));
        
        //si es un usuario normal
        DEBUGER.debug("Comunicando con el servidor '" + this.config.getProperty("url") + "'.");
        DEBUGER.debug("Busca la rama '" + dn + "'.");
        
        try {
            user=new User("-1", username, "127.0.0.1", "localhost", EnvironmentType.WEB, false);
            user.setState(ValidationResult.VALIDATE);

            //reviso que exista el aprovisionamiento de la cuenta
            user=this.validateProvisionedUser(user);
            if( user!=null ){
                ctx = new InitialDirContext(auth);   
                DEBUGER.debug("Se encontro ("+this+") el usuario: "+username);

                //recupero información del usuario
                SearchControls searchCtls = new SearchControls();
                searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                NamingEnumeration res = ctx.search(this.config.getProperty("base"), filtro, searchCtls);  //------------ si tiene password manda error aqui
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
                        //recupera la foto
                        at=attrs.get(this.config.getProperty("dataPhoto"));
                        if( at!=null ){
                            try{
                                user.setPhoto((byte[])at.get());
                            }
                            catch(Exception ex){
                                DEBUGER.error("La foto no es de un tipo valido ", ex);
                            }
                        }
                        //recupera el area
                        at=attrs.get(this.config.getProperty("dataUnit"));
                        if( at!=null ){
                            user.setProperty(BUSSINESS_UNIT, new String[]{(String)at.get()});
                        }
                        //recupera el puesto
                        at=attrs.get(this.config.getProperty("dataEmployment"));
                        if( at!=null ){
                            user.setProperty(EMPLOYMENT, new String[]{(String)at.get()});
                        }
                        //recupera el jefe
                        at=attrs.get(this.config.getProperty("dataBoss"));
                        if( at!=null ){
                            cTmp=(String)at.get();
                            if(cTmp!=null  && cTmp.indexOf(',')>0){
                                cTmp=cTmp.substring(0, cTmp.indexOf(',')).replaceAll("CN=", "");
                            }
                            user.setProperty(BOSS,new String[]{cTmp});
                        }
                        //recupera el telefono
                        at=attrs.get(this.config.getProperty("dataPhone"));
                        if( at!=null ){
                            user.setProperty(PHONE,new String[]{ (String)at.get()});
                        }
                    }                                        
                }

                this.loadExtraData(attrs, user);
                
                //actualizo los datos almacenados en la BD
                this.updateProvisionedUser(user, datos.get(AuthenticationWay.PASS_PARAM).toString());
            }
            //si no existe el aprovisionamiento mando error
            else{
                DEBUGER.debug("Se deniega el acceso por no estar aprovisionado.");
                user=new User("-1", username, "127.0.0.1", "localhost", EnvironmentType.WEB, false);
                user.setState(ValidationResult.NOT_PROVISIONED);
            }
        } 
        catch (AuthenticationException authEx) {
            DEBUGER.debug("Problema al autenticar al usuario en AD '"+username+"': "+authEx);
            this.writeAuditEvent(user, LDAPAuthentication.getDetailError(authEx.getMessage()));
            /*uTmp=this.validateProvisionedUser(user);
            if(uTmp!=null){
                user=uTmp;
            }*/
            user.setState(ValidationResult.DENIED);
        } 
        catch (CommunicationException comEx) {
            DEBUGER.debug("No se logro contactar con el AD, autentico con el aprovisionamiento: "+comEx);
            this.getProvisionedUser(user, datos.get(AuthenticationWay.PASS_PARAM).toString());
        }
        catch (NamingException namEx) {
            DEBUGER.debug("Existe un inconveniente con la cuenta del usuario '"+username+"': "+namEx);
            uTmp=this.validateProvisionedUser(user);
            if(uTmp!=null){
                user=uTmp;
            }
            user.setState(ValidationResult.DENIED);
        } 
        finally{
            try{
                ctx.close();
            }catch(Exception ex1){}
        }        
        
        return user;
    }

    @Override
    public Map<String, Object> createUser(Map<String, Object> datos) throws WayAccessException{
        //reviso que venga el dato de usuario
        if( datos.containsKey(AuthenticationWay.USER_DATA) ){
            String username=""+datos.get(AuthenticationWay.USER_DATA);
            //valida si el usuario se proporciono con dominio, ej. usuario@domino.com
            if( username.indexOf('@')>0 ){
                username=username.substring(0, username.indexOf('@'));
            }
            //valida si el usuario se proporciono con dominio, ej. dominio\\usuario
            if(username.indexOf('\\')>0){
                username=username.substring(username.indexOf('\\')+1, username.length());
            }

            datos.put(AuthenticationWay.USER_DATA, username);
        }
        
        return super.createUser(datos);
    }
    
}
