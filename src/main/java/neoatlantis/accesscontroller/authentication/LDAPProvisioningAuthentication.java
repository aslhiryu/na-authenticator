package neoatlantis.accesscontroller.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
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
import neoatlantis.accesscontroller.blocker.interfaces.BlockerWay;
import neoatlantis.accesscontroller.exceptions.WayAccessException;
import neoatlantis.accesscontroller.exceptions.WayConfigurationException;
import neoatlantis.accesscontroller.objects.EnvironmentType;
import neoatlantis.accesscontroller.objects.User;
import neoatlantis.accesscontroller.printer.interfaces.UserAdministratorPrinter;
import neoatlantis.utils.catalogs.objects.MemoryColumn;
import neoatlantis.utils.catalogs.objects.MemoryTable;
import neoatlantis.utils.catalogs.objects.OrderMode;
import neoatlantis.utils.data.DataUtils;
import neoatlantis.utils.dataBase.ConfigurationDB;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class LDAPProvisioningAuthentication extends LDAPAuthentication {
    private static final Logger DEBUGER = Logger.getLogger(LDAPProvisioningAuthentication.class);
    
    /**
     * Cadena en donde se almacenan los querys a ejecutarse.
     */
    protected StringBuffer sql;
    /**
     * Configuración de conexi&oacute;n a la BD
     */
    protected Properties configDB;
    
    protected static Properties campos;
    
    static{
        campos=new Properties();
        campos.setProperty(AuthenticationWay.ID_FIELD, "id_usuario");
        campos.setProperty(AuthenticationWay.NAME_FIELD, "nombre");
        campos.setProperty(AuthenticationWay.LOGIN_FIELD, "login");
        campos.setProperty(AuthenticationWay.PASSWORD_FIELD, "pass");
        campos.setProperty(AuthenticationWay.MAIL_FIELD, "mail");
        campos.setProperty(AuthenticationWay.STATUS_FIELD, "estado");
        campos.setProperty(AuthenticationWay.LAST_ACCESS_FIELD, "ult_acceso");
        campos.setProperty(DbAuthentication.TABLE_NAME, "aprovisionamiento_na");
    }

    
    
    // Contructores ------------------------------------------------------------

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LDAPProvisioningAuthentication(InputStream xml, InputStream xmlDB) throws WayConfigurationException {
        super(xml);
        this.createDefaultPrincipal();

        try{
            this.config=ConfigurationDB.parseXMLConfiguration(xmlDB);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    public LDAPProvisioningAuthentication(InputStream xml, InputStream xmlDB, Properties fields) throws WayConfigurationException {
        super(xml);
        this.createDefaultPrincipal();

        try{
            this.config=ConfigurationDB.parseXMLConfiguration(xmlDB);
            this.assignFields(fields);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LDAPProvisioningAuthentication(File xml, File xmlDB) throws WayConfigurationException {
        super(xml);
        this.createDefaultPrincipal();

        try{
            this.config=ConfigurationDB.parseXMLConfiguration(new FileInputStream(xmlDB));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    public LDAPProvisioningAuthentication(File xml, File xmlDB, Properties fields) throws WayConfigurationException {
        super(xml);
        this.createDefaultPrincipal();

        try{
            this.config=ConfigurationDB.parseXMLConfiguration(new FileInputStream(xmlDB));
            this.assignFields(fields);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LDAPProvisioningAuthentication(String xml, String xmlDB) throws WayConfigurationException {
        this(new File(xml), new File(xmlDB));
    }

    public LDAPProvisioningAuthentication(String xml, String xmlDB, Properties fields) throws WayConfigurationException {
        this(new File(xml), new File(xmlDB), fields);
    }

    /**
     * Genera un AuthenticationWay por LDAP.
     * @param props Configuraci&oacute;n de acceso al LDAP
     * @throws java.lang.Exception
     */
    public LDAPProvisioningAuthentication(Properties props, Properties propsDB) throws WayConfigurationException{
        super(props);
        this.createDefaultPrincipal();

        try{
            ConfigurationDB.validateConfiguration(propsDB);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.configDB=propsDB;
    }

    public LDAPProvisioningAuthentication(Properties props, Properties propsDB, Properties fields) throws WayConfigurationException{
        super(props);
        this.createDefaultPrincipal();

        try{
            ConfigurationDB.validateConfiguration(propsDB);
            this.assignFields(fields);
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
        
        String dn=getDNString(this.config.getProperty("bindType"), this.config.getProperty("base"), (String)datos.get(AuthenticationWay.USER_PARAM));
        String filtro=this.config.getProperty("dataUser")+"="+datos.get(AuthenticationWay.USER_PARAM);
        SearchResult sr;
        Attributes attrs=null;
        Attribute at;
        
        auth.put(Context.SECURITY_PRINCIPAL, dn);
        auth.put(Context.SECURITY_CREDENTIALS, datos.get(AuthenticationWay.PASS_PARAM));
        
        //si es un usuario normal
        DEBUGER.debug("Comunicando con el servidor '" + this.config.getProperty("url") + "'.");
        DEBUGER.debug("Busca la rama '" + dn + "'.");
        
        try {
            user=new User("-1", (String)datos.get(AuthenticationWay.USER_PARAM), "127.0.0.1", "localhost", EnvironmentType.WEB, false);
            user.setState(ValidationResult.VALIDATE);

            //reviso que exista el aprovisionamiento de la cuenta
            user=this.validateProvisionedUser(user);
            if( user!=null ){
                ctx = new InitialDirContext(auth);   
                DEBUGER.debug("Se encontro ("+this+") el usuario: "+datos.get(AuthenticationWay.USER_PARAM));

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
                    }                                        
                }

                this.loadExtraData(attrs, user);
                
                //actualizo los datos almacenados en la BD
                this.updateProvisionedUser(user, datos.get(AuthenticationWay.PASS_PARAM).toString());
            }
            //si no existe el aprovisionamiento mando error
            else{
                DEBUGER.debug("Se deniega el acceso por no estar aprovisionado.");
                user=new User("-1", (String)datos.get(AuthenticationWay.USER_PARAM), "127.0.0.1", "localhost", EnvironmentType.WEB, false);
                user.setState(ValidationResult.NOT_PROVISIONED);
            }
        } 
        catch (AuthenticationException authEx) {
            DEBUGER.debug("Problema al autenticar al usuario en LDAP '"+datos.get(AuthenticationWay.USER_PARAM)+"': "+authEx);
            this.writeAuditEvent(user, LDAPAuthentication.getDetailError(authEx.getMessage()));
            /*uTmp=this.validateProvisionedUser(user);
            if(uTmp!=null){
                user=uTmp;
            }*/
            user.setState(ValidationResult.DENIED);
        } 
        catch (CommunicationException comEx) {
            DEBUGER.debug("No se logro contactar con el LDAP, autentico con el aprovisionamiento: "+comEx);
            this.getProvisionedUser(user, datos.get(AuthenticationWay.PASS_PARAM).toString());
        }
        catch (NamingException namEx) {
            DEBUGER.debug("Existe un inconveniente con la cuenta del usuario '"+datos.get(AuthenticationWay.USER_PARAM)+"': "+namEx);
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
    public List<User> getRegisteredUserList(String order, OrderMode orderType) throws WayAccessException{
        ArrayList<User> lTmp=new ArrayList<User>();
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        User u;
        
        this.sql = new StringBuffer(this.getQuerySelectUsers());
        this.sql.append(" ORDER BY ").append(DbAuthentication.getDataOrder(order, campos)).append(" ").append(orderType);
        try{
            DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
            con = ConfigurationDB.createConection(this.configDB);
            ps = con.prepareStatement(sql.toString());

            res = ps.executeQuery();

            //si existe el usuario
            while(res.next()) {
                u=new User(res.getString(1), res.getString(3), "0.0.0.0", "localhost", EnvironmentType.WEB);
                u.setName(res.getString(2));
                u.setMail(res.getString(4));
                u.setActive( DataUtils.validateTrueBoolean(res.getString(5)) );
                
                lTmp.add(u);
            }
        }
        catch(Exception ex){
            DEBUGER.fatal("No se logro obtener la lista de usuarios.", ex);
            throw new WayAccessException(ex);
        }
        finally{
            try{
                res.close();
            }catch(Exception ex1){}
            try{
                ps.close();
            }catch(Exception ex1){}
            try{
                con.close();
            }catch(Exception ex1){}
        }        

        return lTmp;
    }
    
    @Override
    public Map<String, Object> createUser(Map<String, Object> datos) throws WayAccessException{
        HashMap<String, Object> data=new HashMap<String, Object>();
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        String id;
        boolean existe=false;
        
        //verifico que se cuente con los campos necesarios
        if( datos==null ){
            DEBUGER.debug("No existen datos");
            data.put(UserAdministratorPrinter.ERROR_DATA, new String[]{"No se proporcionaron los datos necesarios"});
        }
        else if(datos!=null && (datos.get(AuthenticationWay.USER_DATA)==null || datos.get(AuthenticationWay.USER_DATA).equals("")) ){
            data.put(UserAdministratorPrinter.ERROR_DATA, new String[]{"No se proporciono la cuenta a aprovisionar"});
        }
        else{
            DEBUGER.debug("Intento agregar al usuario '" + datos.get(AuthenticationWay.USER_DATA)); 
            
            //verifico que no existe el usuario
            this.sql = new StringBuffer(this.getQuerySelectUser());
            try{
                DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
                con = ConfigurationDB.createConection(this.configDB);
                ps = con.prepareStatement(sql.toString());
                ps.setString(1, (String)datos.get(AuthenticationWay.USER_DATA));
                
                res = ps.executeQuery();

                //si existe el usuario
                if (res.next()) {
                    DEBUGER.debug("Ya existe el usuario");
                    existe=true;
                }
            }
            catch(Exception ex){
                DEBUGER.fatal("No se logro consultar la existencia del usuario de '" + (String)datos.get(AuthenticationWay.USER_DATA) + "'.", ex);
                throw new WayAccessException(ex);
            }
            finally{
                try{
                    res.close();
                }catch(Exception ex1){}
                try{
                    ps.close();
                }catch(Exception ex1){}
                try{
                    con.close();
                }catch(Exception ex1){}
            }        
            
            //si no existe el usuario
            if( !existe ){
                //genero la llave del usuario
                id=UUID.randomUUID().toString();

                //genero el usuario
                this.sql = new StringBuffer(this.getQueryInsertUser());

                try{
                    DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
                    con = ConfigurationDB.createConection(this.configDB);
                    ps = con.prepareStatement(sql.toString());
                    ps.setString(1, id);
                    ps.setString(2, (String)datos.get(AuthenticationWay.USER_DATA));
                    ps.setString(3, "");
                    ps.setString(4, "");
                    ps.setString(5, "1");
                    
                    ps.executeUpdate();
                    
                    data.put(UserAdministratorPrinter.ID_DATA, id);
                }
                catch(Exception ex){
                    DEBUGER.fatal("No se logro agregar al usuario '" + (String)datos.get(AuthenticationWay.USER_DATA) + "'.", ex);
                    throw new WayAccessException(ex);
                }
                finally{
                    try{
                        ps.close();
                    }catch(Exception ex1){}
                    try{
                        con.close();
                    }catch(Exception ex1){}
                }       
            }
            //si existe el usuario
            else{
                data.put(UserAdministratorPrinter.ERROR_DATA, new String[]{"El usuario ya existe"});
            }
        }
        
        return data;
    }
    
    @Override
    public User getUserData(String id) throws WayAccessException{
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        User uTmp=null;

        this.sql = new StringBuffer(this.getQuerySelectUserById());
        
        try{
            DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
            con = ConfigurationDB.createConection(this.configDB);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, id);
        
            res = ps.executeQuery();

            //si existe el usuario
            if (res.next()) {
                uTmp=new User(id, res.getString(2), "0.0.0.0", "localhost", EnvironmentType.WEB);
                uTmp.setName(res.getString(1));
                uTmp.setMail(res.getString(3));
                uTmp.setActive( DataUtils.validateTrueBoolean(res.getString(4)) );
            }
        }
        catch(Exception ex){
            DEBUGER.fatal("No se logro recuperar el usuario con id '" + id + "'.", ex);
            throw new WayAccessException(ex);
        }
        finally{
            try{
                res.close();
            }catch(Exception ex1){}
            try{
                ps.close();
            }catch(Exception ex1){}
            try{
                con.close();
            }catch(Exception ex1){}
        }        
        
        //cargo la informacion adicional
        this.loadExtraData(null, uTmp);
        
        return uTmp;
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
        return true;
    }

    @Override
    public boolean activeUser(String id, boolean active) throws WayAccessException{
        Connection con=null;
        PreparedStatement ps=null;
        boolean realizado=false;

        this.sql = new StringBuffer(this.getQueryUpdateState());
        
        try{
            DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
            con = ConfigurationDB.createConection(this.configDB);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, active? "1": "0");
            ps.setString(2, id);
        
            if( ps.executeUpdate()>0 ){
                realizado=true;
            }
            
            DEBUGER.debug("Se realiza el cambio al estado de '"+id+"' a "+active+", con resultado: "+realizado);
        }
        catch(Exception ex){
            DEBUGER.fatal("No se logro actualizar el estao del con id '" + id + "'.", ex);
            throw new WayAccessException(ex);
        }
        finally{
            try{
                ps.close();
            }catch(Exception ex1){}
            try{
                con.close();
            }catch(Exception ex1){}
        }        
        
        return realizado;
    }

    @Override
    public Map<String, Object> updateUser(Map<String, Object> datos) throws WayAccessException{
        HashMap<String, Object> data=new HashMap<String, Object>();
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        String id;
        boolean existe=false;
        
        //verifico que se cuente con los campos necesarios
        if( datos==null ){
            DEBUGER.debug("No existen datos");
            data.put(UserAdministratorPrinter.ERROR_DATA, new String[]{"No se proporcionaron los datos necesarios"});
        }
        else if(datos!=null && (datos.get(UserAdministratorPrinter.ID_DATA)==null || datos.get(UserAdministratorPrinter.ID_DATA).equals("")) ){
            data.put(UserAdministratorPrinter.ERROR_DATA, new String[]{"No se proporciono el ID del usuario a actualizar"});
        }
        else{
            //actualizo el usuario
            this.sql = new StringBuffer(this.getQueryUpdateUserData());

            try{
                DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
                con = ConfigurationDB.createConection(this.configDB);
                ps = con.prepareStatement(sql.toString());
                ps.setString(1, (String)datos.get(AuthenticationWay.NAME_PARAM));
                ps.setString(2, (String)datos.get(AuthenticationWay.MAIL_PARAM));
                ps.setString(3, (String)datos.get(UserAdministratorPrinter.ID_DATA));

                ps.executeUpdate();
                
                data.put(UserAdministratorPrinter.ID_DATA, datos.get(UserAdministratorPrinter.ID_DATA));
            }
            catch(Exception ex){
                DEBUGER.fatal("No se logro actualizar al usuario '" + (String)datos.get(AuthenticationWay.USER_DATA) + "'.", ex);
                throw new WayAccessException(ex);
            }
            finally{
                try{
                    ps.close();
                }catch(Exception ex1){}
                try{
                    con.close();
                }catch(Exception ex1){}
            }       
        }
        
        return data;
    }
    
    @Override
    public List<User> validateLoginLifes() throws WayAccessException{
        ArrayList<User> lTmp=new ArrayList<User>();
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        User u;
        Date fechaPivote;
        
        this.sql = new StringBuffer(this.getQuerySelectUserForInactivity());
        try{
            //preparo la fecha
            fechaPivote=new Date( (new Date()).getTime() -(this.loginLife*BlockerWay.DAY_IN_MILLIS));
            
            DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
            DEBUGER.debug("Con fecha en: "+fechaPivote);
            con = ConfigurationDB.createConection(this.configDB);
            ps = con.prepareStatement(sql.toString());
            ps.setTimestamp(1, new Timestamp(fechaPivote.getTime()));

            res = ps.executeQuery();

            //si existen usuarios los ihabilito
            while(res.next()) {
                u=new User(res.getString(1), res.getString(3), "0.0.0.0", "localhost", EnvironmentType.WEB);
                u.setName(res.getString(2));
                u.setMail(res.getString(4));
                
                lTmp.add(u);
                
                this.activeUser(u.getId(), false);
            }
        }
        catch(Exception ex){
            DEBUGER.fatal("No se logro obtener la inhabilitar los usuarios sinactividad.", ex);
            throw new WayAccessException(ex);
        }
        finally{
            try{
                res.close();
            }catch(Exception ex1){}
            try{
                ps.close();
            }catch(Exception ex1){}
            try{
                con.close();
            }catch(Exception ex1){}
        }        

        return lTmp;
        
    }

    
    

    
    // Metodos protegidos-------------------------------------------------------

    protected void createDefaultPrincipal(){
        MemoryTable t=new MemoryTable(campos.getProperty(DbAuthentication.TABLE_NAME));
        
        MemoryColumn c;
        
        c=new MemoryColumn(campos.getProperty(ID_FIELD));
        c.setKey(true);
        c.setUnique(true);
        c.setId(AuthenticationWay.ID_PARAM);
        t.addColumn(c);
        c=new MemoryColumn(campos.getProperty(NAME_FIELD));
        c.setCapture(false);
        c.setId(AuthenticationWay.NAME_PARAM);
        t.addColumn(c);
        c=new MemoryColumn(campos.getProperty(LOGIN_FIELD));
        c.setUnique(true);
        c.setId(AuthenticationWay.USER_PARAM);
        t.addColumn(c);
        c=new MemoryColumn(campos.getProperty(PASSWORD_FIELD));
        c.setCapture(false);
        c.setId(AuthenticationWay.PASS_PARAM);
        t.addColumn(c);
        c=new MemoryColumn(campos.getProperty(MAIL_FIELD));
        c.setCapture(false);
        c.setId(AuthenticationWay.MAIL_PARAM);
        t.addColumn(c);
        c=new MemoryColumn(campos.getProperty(STATUS_FIELD));
        c.setCapture(false);
        c.setId(AuthenticationWay.STATUS_PARAM);
        t.addColumn(c);
        c=new MemoryColumn(campos.getProperty(LAST_ACCESS_FIELD));
        c.setCapture(false);
        c.setId(AuthenticationWay.LAST_ACCESS_FIELD);
        t.addColumn(c);
    
        DEBUGER.debug("Datos de tabla de usuario: "+t);
        this.principalEntity=t;
        super.setPrincipalEntity(t);
    }
    
    /**
     * Genera el query utilizado para la validación de un usuario.
     * @return Cadena con el query
     */
    protected String getQuerySelectUser(){
        StringBuilder sb=new StringBuilder("SELECT ");
        //                                                                   1                                                                   2                                                                        3                                                                     4                                                                       5
        /*sb.append(campos.getProperty(AuthenticationWay.ID_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.NAME_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.PASSWORD_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.MAIL_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.STATUS_FIELD))
                .append(" FROM ").append(campos.getProperty("TABLE"))
                .append(" WHERE ").append(campos.getProperty(AuthenticationWay.LOGIN_FIELD)).append("=?");*/
        
        for(int i=0; i<this.principalEntity.getColumns().size(); i++){
            if( i>0 ){
                sb.append(", ");
            }
            sb.append(this.principalEntity.getColumns().get(i).getName());
        }
        sb.append(" FROM ").append(this.principalEntity.getName());
        sb.append(" WHERE ").append(campos.getProperty(AuthenticationWay.LOGIN_FIELD)).append("=?");
        
        return sb.toString();
    }
    
    /**
     * Genera el query utilizado para la validaci&oacute;n de un usuario.
     * @return Cadena con el query
     */
    protected String getQueryUpdateUser(){
        StringBuilder sb=new StringBuilder("");
        //                                                                                                                       1                                                                           2                                                                        3                
        sb.append("UPDATE ").append(campos.getProperty("TABLE")).append(" SET ").append(campos.getProperty(AuthenticationWay.NAME_FIELD)).append("=?, ").append(campos.getProperty(AuthenticationWay.PASSWORD_FIELD)).append("=?, ").append(campos.getProperty(AuthenticationWay.MAIL_FIELD)).append("=?, ").append(campos.getProperty(AuthenticationWay.LAST_ACCESS_FIELD)).append("=? WHERE ")
                .append(campos.getProperty(AuthenticationWay.LOGIN_FIELD)).append("=?");
        return sb.toString();
    }
    
    /**
     * Genera el query utilizado paraactualizar la informaci&oacute;n de un usuario.
     * @return Cadena con el query
     */
    protected String getQueryUpdateUserData(){
        StringBuilder sb=new StringBuilder("");
        //                                                                                                                       1                                                                           2                                                                        3                
        sb.append("UPDATE ").append(campos.getProperty("TABLE")).append(" SET ").append(campos.getProperty(AuthenticationWay.NAME_FIELD)).append("=?, ").append(campos.getProperty(AuthenticationWay.MAIL_FIELD)).append("=? WHERE ")
                .append(campos.getProperty(AuthenticationWay.ID_FIELD)).append("=?");
        return sb.toString();
    }

    /**
     * Genera el query utilizado para obtener la lista de usuarios
     * @return Cadena con el query
     */
    protected String getQuerySelectUsers(){
        StringBuilder sb=new StringBuilder("");
        //                                               1                                              2                                              3                                                4                                                5
        sb.append("SELECT ").append(campos.getProperty(AuthenticationWay.ID_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.NAME_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.LOGIN_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.MAIL_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.STATUS_FIELD))
                .append(" FROM ").append(campos.getProperty("TABLE"))
                .append(" WHERE ").append(campos.getProperty(AuthenticationWay.ID_FIELD)).append("<>'-1'");
        return sb.toString();
    }

    protected String getQueryInsertUser(){
        StringBuilder sb=new StringBuilder("");
        //                                                                                                    1                                               2                                               3                                               4                                                 5                                               6
        sb.append("INSERT INTO ").append(campos.getProperty("TABLE")).append(" (").append(campos.getProperty(AuthenticationWay.ID_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.LOGIN_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.NAME_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.MAIL_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.STATUS_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.PASSWORD_FIELD)).append(") VALUES (?, ?, ?, ?, ?, '')");
        return sb.toString();
    }

    protected String getQuerySelectUserById(){
        StringBuilder sb=new StringBuilder("");
        //                                               1                                                2                                               3                                                4     
        sb.append("SELECT ").append(campos.getProperty(AuthenticationWay.NAME_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.LOGIN_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.MAIL_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.STATUS_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.LAST_ACCESS_FIELD))
                .append(" FROM ").append(campos.getProperty("TABLE"))
                .append(" WHERE ").append(campos.getProperty(AuthenticationWay.ID_FIELD)).append("=?");
        return sb.toString();
    }

    protected String getQueryUpdateState(){
        StringBuilder sb=new StringBuilder("");
        //                                                                                                      1                                                       2
        sb.append("UPDATE ").append(campos.getProperty("TABLE")).append(" SET  ").append(campos.getProperty(AuthenticationWay.STATUS_FIELD)).append("=?  WHERE ").append(campos.getProperty(AuthenticationWay.ID_FIELD)).append("=?");
        return sb.toString();
    }
    
    protected String getQuerySelectUserForInactivity(){
        StringBuilder sb=new StringBuilder("");
        //                                                                                                                                                                        1                                                                                                                                                                                    2                                                                                                                                                                    3                                                                                                                                                                                               4                                                                                                                                                                                5
        sb.append("SELECT ").append(campos.getProperty(AuthenticationWay.ID_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.NAME_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.LOGIN_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.MAIL_FIELD)).append(", ").append(campos.getProperty(AuthenticationWay.STATUS_FIELD))
                .append(" FROM ").append(campos.getProperty("TABLE"))
                .append(" WHERE ").append(campos.getProperty(AuthenticationWay.LAST_ACCESS_FIELD)).append("<? AND ").append(campos.getProperty(AuthenticationWay.STATUS_FIELD)).append("<>0");
        return sb.toString();
    }
    
    
    
    
    
    // Metodos protegidos---------------------------------------------------------
    
    protected void assignFields(Properties p){
        boolean cambio=false;
        
        if( p.getProperty(AuthenticationWay.ID_FIELD)!=null && !p.getProperty(AuthenticationWay.ID_FIELD).isEmpty() ){
            campos.setProperty(AuthenticationWay.ID_FIELD, p.getProperty(AuthenticationWay.ID_FIELD));
            cambio=true;
        }
        if( p.getProperty(AuthenticationWay.NAME_FIELD)!=null && !p.getProperty(AuthenticationWay.NAME_FIELD).isEmpty() ){
            campos.setProperty(AuthenticationWay.NAME_FIELD, p.getProperty(AuthenticationWay.NAME_FIELD));
            cambio=true;
        }
        if( p.getProperty(AuthenticationWay.LOGIN_FIELD)!=null && !p.getProperty(AuthenticationWay.LOGIN_FIELD).isEmpty() ){
            campos.setProperty(AuthenticationWay.LOGIN_FIELD, p.getProperty(AuthenticationWay.LOGIN_FIELD));
            cambio=true;
        }
        if( p.getProperty(AuthenticationWay.PASSWORD_FIELD)!=null && !p.getProperty(AuthenticationWay.PASSWORD_FIELD).isEmpty() ){
            campos.setProperty(AuthenticationWay.PASSWORD_FIELD, p.getProperty(AuthenticationWay.PASSWORD_FIELD));
            cambio=true;
        }
        if( p.getProperty(AuthenticationWay.MAIL_FIELD)!=null && !p.getProperty(AuthenticationWay.MAIL_FIELD).isEmpty() ){
            campos.setProperty(AuthenticationWay.MAIL_FIELD, p.getProperty(AuthenticationWay.MAIL_FIELD));
            cambio=true;
        }
        if( p.getProperty(AuthenticationWay.STATUS_FIELD)!=null && !p.getProperty(AuthenticationWay.STATUS_FIELD).isEmpty() ){
            campos.setProperty(AuthenticationWay.STATUS_FIELD, p.getProperty(AuthenticationWay.STATUS_FIELD));
            cambio=true;
        }
        if( p.getProperty(AuthenticationWay.LAST_ACCESS_FIELD)!=null && !p.getProperty(AuthenticationWay.LAST_ACCESS_FIELD).isEmpty() ){
            campos.setProperty(AuthenticationWay.LAST_ACCESS_FIELD, p.getProperty(AuthenticationWay.LAST_ACCESS_FIELD));
            cambio=true;
        }
        if( p.getProperty("TABLE")!=null && !p.getProperty("TABLE").isEmpty() ){
            campos.setProperty("TABLE", p.getProperty("TABLE"));
            cambio=true;
        }

        if( cambio ){
            this.createDefaultPrincipal();
        }
    }
    
    /**
     * Revisa si existe el aprovisionamiento de una cuenta
     * @param u Usuario a validar
     * @return true si esta aprovisionada
     */
    protected User validateProvisionedUser(User u){
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        User uTmp=null;
        
        this.sql = new StringBuffer(this.getQuerySelectUser());
        
        try{
            DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
            con = ConfigurationDB.createConection(this.configDB);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, u.getUser());

            res = ps.executeQuery();

            //si existe el usuario
            if (res.next()) {
                uTmp=new User(res.getString(1), u.getUser(), "0.0.0.0", "localhost", EnvironmentType.WEB);
                uTmp.setName(res.getString(2));
                uTmp.setMail(res.getString(5));
                if( DataUtils.validateTrueBoolean(res.getString(6)) ){
                    uTmp.setState(ValidationResult.VALIDATE);
                }
                else{
                    uTmp.setState(ValidationResult.INACTIVE);
                }
                if( res.getTimestamp(7)!=null ){
                    uTmp.setLastAccessDate(new java.util.Date(res.getTimestamp(7).getTime()));
                }
            }
        }
        catch(Exception ex){
            DEBUGER.fatal("No se logro validar el aprovicionamiento de '" + u.getUser() + "'.", ex);
            throw new WayAccessException(ex);
        }
        finally{
            try{
                res.close();
            }catch(Exception ex1){}
            try{
                ps.close();
            }catch(Exception ex1){}
            try{
                con.close();
            }catch(Exception ex1){}
        }        
        
        return uTmp;
    }
    
    /**
     * Realiza la autenticación del usuario aprovisionado con la información de BD
     * @param u Usuario a autenticar
     * @param pass Contraseña del usuario
     * @return 
     */
    protected User getProvisionedUser(User u, String pass){
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        
        this.sql = new StringBuffer(this.getQuerySelectUser());
        
        try{
            DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
            con = ConfigurationDB.createConection(this.configDB);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, u.getUser());

            res = ps.executeQuery();

            //si existe el usuario
            if (res.next()) {
                DEBUGER.debug("Intenta la validacion de '" + u.getUser() + "' : '"+this.cifrador.cipher(pass)+"'.");

                //cargo la informacion del usuario
                u=new User(res.getString(1), u.getUser(), "127.0.0.1", "localhost", EnvironmentType.WEB);
                u.setName(res.getString(2));
                u.setMail(res.getString(5));

                //valida la contraseña
                if (res.getString(4)==null || !res.getString(4).equals(this.cifrador.cipher(pass))) {
                    u.setState(ValidationResult.DENIED);
                }
                else{
                    //valida si esta activo
                    if( res.getString(6)!=null &&  DataUtils.validateFalseBoolean(res.getString(6))){
                        u.setState(ValidationResult.INACTIVE);
                    }                    
                    else{
                        u.setState(ValidationResult.VALIDATE);
                    }
                }
                
                DEBUGER.debug("Usuario recuperado: " + u);
            }
        }
        catch(Exception ex){
            DEBUGER.fatal("No se logro autenticar la cuenta aprovisionada '" + u.getUser() + "'.", ex);
            throw new WayAccessException(ex);
        }
        finally{
            try{
                res.close();
            }catch(Exception ex1){}
            try{
                ps.close();
            }catch(Exception ex1){}
            try{
                con.close();
            }catch(Exception ex1){}
        }        
        
        return u;
    }
    
    /**
     * Actualiza información del usuario aprovisionado
     * @param u Usuario a aprovisionar
     * @param pass Contraseña del usuario
     */
    protected void updateProvisionedUser(User u, String pass){
        Connection con=null;
        PreparedStatement ps=null;
        
        this.sql = new StringBuffer(this.getQueryUpdateUser());
        
        try{
            DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");            
            con = ConfigurationDB.createConection(this.configDB);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, u.getName());
            ps.setString(2, this.cifrador.cipher(pass) );
            ps.setString(3, u.getMail());
            ps.setTimestamp(4, new Timestamp((new java.util.Date()).getTime()));
            ps.setString(5, u.getUser());
            
            ps.executeUpdate();
        }
        catch(Exception ex){
            DEBUGER.fatal("No se logro actualizar la información de la cuenta '" + u.getUser() + "'.", ex);
            throw new WayAccessException(ex);
        }
        finally{
            try{
                ps.close();
            }catch(Exception ex1){}
            try{
                con.close();
            }catch(Exception ex1){}
        }        
    }
}
