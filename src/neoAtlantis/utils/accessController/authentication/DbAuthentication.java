package neoAtlantis.utils.accessController.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import neoAtlantis.utils.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utils.accessController.authentication.interfaces.ValidationResult;
import neoAtlantis.utils.accessController.exceptions.WayAccessException;
import neoAtlantis.utils.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utils.accessController.objects.EnvironmentType;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.apps.catalogs.objetcs.OrderType;
import neoAtlantis.utils.data.DataUtils;
import neoAtlantis.utils.dataBase.ConfigurationDB;
import org.apache.log4j.Logger;

/**
 * Medio Autenticador operado a traves de BD, para lo cual utiliza una tabla con
 * la siguiente estructura:<br>
 * <pre>
 * CREATE TABLE usuario_na(
 *     id_usuario  INT NOT NULL,
 *     nombre      VARCHAR(50),
 *     login       VARCHAR(15),
 *     pass        VARCHAR(30),
 *     mail        VARCHAR(50),
 *     estado      CHAR(1),
 *     expira      DATE,
 *     ult_acceso  DATE,
 *     CONSTRAINT id_usuario_na PRIMARY KEY (id_usuario)
 * ); 
 * </pre>
 * Para poder generar el objeto se hace uso de un documento XML con la siguiente 
 * estructura:<br>
 * <pre>
 * &lt;bd&gt;
 *   &lt;driver&gt;<i>clase_driver_a_utilizar</i>&lt;/driver&gt;
 *   &lt;url&gt;<i>url_de_conexion</i>&lt;/url&gt;
 *   &lt;user&gt;<i>usuario_de_bd</i>&lt;/user&gt;
 *   &lt;pass&gt;<i>contrase&ntilde;a_del_usuario</i>&lt;/pass&gt;
 * &lt;/bd&gt;
 * </pre>
 * o tambien puede contar con la siguiente estructura:<br>
 * <pre>
 * &lt;bd&gt;
 *   &lt;jndi&gt;<i>datasource_configurado_en_el_contenedor</i>&lt;/jndi&gt;
 * &lt;/bd&gt;
 * </pre>
 * Otra opci&oacute;n para generar el objeto es con un <b>java.util.Properties</b>, para lo cual 
 * deber&aacute; contar con los siguientes datos:
 * <pre>
 * driver=<i>clase_driver_a_utilizar</i>
 * url=<i>url_de_conexion</i>
 * user=<i>usuario_de_bd</i>
 * pass=<i>contrase&ntilde;a_del_usuario</i>
 * </pre>
 * o en su defecto solo puede contener el siguiente dato:
 * <pre>
 * jndi=<i>datasource_configurado_en_el_contenedor</i>
 * </pre>
 * En ambas configuraciones siempre que encuentre el parametro <i>jndi</i> se har&aacute;
 * caso omiso de los demas parametros.
 * <br><br>
 * Esta clase utiliza los parametros <b>user</b> y <b>pass</b>, para realizar la 
 * autenticaci&oacute;n.
 * <br><br>
 * Para trabajar adecuamente este objeto requiere la libreria de <b>NA_Utils</b>.
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class DbAuthentication extends AuthenticationWay {
    protected static final Logger DEBUGER = Logger.getLogger(DbAuthentication.class);

    public static final String TABLE_NAME="TABLE";

    /**
     * Cadena en donde se almacenan los querys a ejecutarse.
     */
    protected StringBuffer sql;
    /**
     * Configuración de conexi&oacute;n a la BD
     */
    protected Properties config;
    
    private static Properties campos;
    
    static{
        campos=new Properties();
        campos.setProperty(AuthenticationWay.ID_FIELD, "id_usuario");
        campos.setProperty(AuthenticationWay.NAME_FIELD, "nombre");
        campos.setProperty(AuthenticationWay.LOGIN_FIELD, "login");
        campos.setProperty(AuthenticationWay.PASSWORD_FIELD, "pass");
        campos.setProperty(AuthenticationWay.MAIL_FIELD, "mail");
        campos.setProperty(AuthenticationWay.STATUS_FIELD, "estado");
        campos.setProperty(AuthenticationWay.LIFE_FIELD, "expira");
        campos.setProperty(AuthenticationWay.LAST_ACCESS_FIELD, "ult_acceso");
        campos.setProperty(TABLE_NAME, "usuario_na");
    }

    
    
    
    
    
    
    // Contructores ------------------------------------------------------------
    
    /**
     * Genera un AuthenticationWay por Base de Datos.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbAuthentication(InputStream xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseXMLConfiguration(xml);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por Base de Datos.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbAuthentication(File xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseXMLConfiguration(new FileInputStream(xml));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por Base de Datos.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbAuthentication(String xml) throws WayConfigurationException {
        this(new File(xml));
    }

    /**
     * Genera un AuthenticationWay por Base de Datos.
     * @param configBD Configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbAuthentication(Properties configBD) throws WayConfigurationException {
        try{
            ConfigurationDB.validateConfiguration(configBD);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.config=configBD;
    }

    /**
     * Genera un AuthenticationWay por Base de Datos.
     * @param driver Driver a utilizar para la conexi&oacute;n a la BD
     * @param url URL de conexi&oacute;n a la BD
     * @param user Usuario a utilizar para la conexi&oacute;n a la BD
     * @param pass Contrase&ntilde;a a utilizar para la conexi&oacute;n a la BD
     * @throws WayConfigurationException 
     */
    public DbAuthentication(String driver, String url, String user, String pass) throws WayConfigurationException {
        this.config.setProperty("driver", driver);
        this.config.setProperty("url", url);
        this.config.setProperty("user", user);
        this.config.setProperty(AuthenticationWay.PASSWORD_FIELD, pass);
        try{
            ConfigurationDB.validateConfiguration(this.config);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    // Metodos protegidos-------------------------------------------------------

    /**
     * Genera el query utilizado para la validación de un usuario.
     * @return Cadena con el query
     */
    protected String getQuerySelectUser(){
        //                                                        1                                                    2                                                        3                                                      4                                                       5                                                    6                                                           7
        return "SELECT "+campos.getProperty(AuthenticationWay.ID_FIELD)+", "+campos.getProperty(AuthenticationWay.NAME_FIELD)+", "+campos.getProperty(AuthenticationWay.PASSWORD_FIELD)+", "+campos.getProperty(AuthenticationWay.MAIL_FIELD)+", "+campos.getProperty(AuthenticationWay.STATUS_FIELD)+", "+campos.getProperty(AuthenticationWay.LIFE_FIELD)+", "+campos.getProperty(AuthenticationWay.LAST_ACCESS_FIELD)+" FROM "+campos.getProperty("TABLE")+" WHERE "+campos.getProperty(AuthenticationWay.LOGIN_FIELD)+"=?";
    }

    /**
     * Genera el query utilizado para la busqueda de usuarios.
     * @return Cadena con el query
     */
    protected String getQuerySearchUsers(){
        //                                                        1                                                    2                                                      3                                                     4    
        return "SELECT "+campos.getProperty(AuthenticationWay.ID_FIELD)+", "+campos.getProperty(AuthenticationWay.LOGIN_FIELD)+", "+campos.getProperty(AuthenticationWay.NAME_FIELD)+", "+campos.getProperty(AuthenticationWay.MAIL_FIELD)+" FROM "+campos.getProperty("TABLE")+" WHERE "+campos.getProperty(AuthenticationWay.LOGIN_FIELD)+"=? OR "+campos.getProperty(AuthenticationWay.NAME_FIELD)+"=?";
    }
    
    /**
     * Genera el query utilizado para obtener la lista de usuarios
     * @return Cadena con el query
     */
    protected String getQuerySelectUsers(){
        //                                                       1                                                     2                                                     3                                                     4                                                       5                                                     6                                                            7
        return "SELECT "+campos.getProperty(AuthenticationWay.ID_FIELD)+", "+campos.getProperty(AuthenticationWay.NAME_FIELD)+", "+campos.getProperty(AuthenticationWay.LOGIN_FIELD)+", "+campos.getProperty(AuthenticationWay.MAIL_FIELD)+", "+campos.getProperty(AuthenticationWay.STATUS_FIELD)+", "+campos.getProperty(AuthenticationWay.LIFE_FIELD)+", "+campos.getProperty(AuthenticationWay.LAST_ACCESS_FIELD)+" FROM "+campos.getProperty("TABLE");
    }

    /**
     * Genera el querypara actualizar el eltimo acceso
     * @return Cadena con el query
     */
    protected String getQueryUpdateLastAccess(){
        //                                                                                              1                                                                  2
        return "UPDATE "+campos.getProperty("TABLE")+ " SET "+campos.getProperty(AuthenticationWay.LAST_ACCESS_FIELD)+"=? WHERE "+campos.getProperty(AuthenticationWay.ID_FIELD)+"=?";
    }

    /**
     * Carga información adicional del usuario a partir de campos en la BD.
     * @param res ResultSet del registro encontrado
     * @param user Usuario al que se va asigna los datos
     */
    protected void loadAditionalData(ResultSet res, User user){
        //nada
    }

    public static  String getDataOrder(String order, Properties campos){
        if(campos!=null){
            if( campos.getProperty(order)!=null && !campos.getProperty(order).isEmpty() ){
                return campos.getProperty(order);
            }
            else{
                return "1";
            }
        }
        else{
            return "1";
        }
    }
    
    
    
    
    
    
    
    // Metodos publicos---------------------------------------------------------

    /**
     * Valida un usuario, para tal fin requiere los datos <b>user</b> y <b>pass</b>
     * sean suministrados.
     * @param datos Variables que se deben de utilizar para realizar la autenticación
     * @return Usuario que se encontro al realizar la autenticacion:
     * @throws java.lang.Exception
     */
    @Override
    public User authenticateUser(Map<String, Object> datos) throws WayAccessException {
        Connection con=null;
        PreparedStatement ps=null;
        ResultSet res=null;
        User user=User.getNobody();

        if( datos.get(AuthenticationWay.USER_PARAM)==null ){
            throw new NullPointerException("No se el valor de 'user', para el autenticador.");
        }
        else if( datos.get(AuthenticationWay.PASS_PARAM)==null ){
            throw new NullPointerException("No se el valor de 'pass', para el autenticador.");
        }

        //si no viene el usuario
        if( datos.get(AuthenticationWay.USER_PARAM).toString().isEmpty() ){
            user=new User("Nobody", "Desconocido", "127.0.0.1", "localhost", EnvironmentType.WEB);
            user.setState(ValidationResult.NOT_USER);
            return user;
        }
        
        this.sql = new StringBuffer(this.getQuerySelectUser());
        DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            con = ConfigurationDB.createConection(this.config);
            ps = con.prepareStatement(sql.toString());
            ps.setString(1, (String)datos.get(AuthenticationWay.USER_PARAM));

            res = ps.executeQuery();

            //si existe el usuario
            if (res.next()) {
                DEBUGER.debug("Intenta la validacion de '" + datos.get(AuthenticationWay.USER_PARAM) + "' : '"+this.cifrador.cipher((String)datos.get(AuthenticationWay.PASS_PARAM))+"'.");

                //cargo la informacion del usuario
                user=new User(res.getString(1), (String)datos.get(AuthenticationWay.USER_PARAM), "127.0.0.1", "localhost", EnvironmentType.WEB);
                user.setName(res.getString(2));
                user.setMail(res.getString(4));
                user.setLastAccessDate(new java.util.Date(res.getTimestamp(7).getTime()));
                loadAditionalData(res, user);

                //valida la contraseña
                if (res.getString(3)==null || !res.getString(3).equals(this.cifrador.cipher((String)datos.get(AuthenticationWay.PASS_PARAM)))) {
                    user.setState(ValidationResult.DENIED);
                }
                else{
                    //valida si la cuenta es temporal
                    if( res.getString(5)!=null && res.getString(5).equals("T") ){
                        user.setState(ValidationResult.TEMPORAL_VALIDATE);
                    }
                    //valida si esta activo
                    else if( res.getString(5)!=null && DataUtils.validateFalseBoolean(res.getString(5))){
                        user.setState(ValidationResult.INACTIVE);
                    }
                    //valida si esta vigente la cuenta
                    else if( res.getString(6)!=null && (new java.util.Date()).getTime() > res.getTimestamp(6).getTime() ){
                        user.setState(ValidationResult.LAPSED);
                    }
                    else{
                        user.setState(ValidationResult.VALIDATE);
                    }
                }

                DEBUGER.debug("Con resultado '" + user + "'.");
                this.updateLastAccess(user.getId());
            }
        }catch(Exception ex){
            DEBUGER.fatal("No se logro autenticar la cuenta '" + datos.get(AuthenticationWay.USER_PARAM) + "'.", ex);
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

        return user;    
    }
    
    @Override
    public boolean updateLastAccess(String id) throws WayAccessException{
        Connection con=null;
        PreparedStatement ps=null;

        //actualizo el ultimo acceso
        if( campos.containsKey(AuthenticationWay.LAST_ACCESS_FIELD) && campos.getProperty(AuthenticationWay.LAST_ACCESS_FIELD)!=null
                && !campos.getProperty(AuthenticationWay.LAST_ACCESS_FIELD).isEmpty() && !campos.getProperty(AuthenticationWay.LAST_ACCESS_FIELD).equalsIgnoreCase("null")){
            try{
                this.sql = new StringBuffer(this.getQuerySelectUser());
                DEBUGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");
                
                con = ConfigurationDB.createConection(this.config);
                ps = con.prepareStatement(sql.toString());
                ps.setTimestamp(1, new Timestamp((new java.util.Date()).getTime()));
                ps.setString(2, id);
                
                return (ps.executeUpdate()>0);
            }catch(Exception ex){
                DEBUGER.fatal("No se logro actualizar el ultimo acceso del usuario.", ex);
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
        
        return false;
    }

    /**
     * Validaci&oacute;n posterior a la validaci&oacute;n de credenciales.
     * @param usuario Usuario al que se le realiza la evaluación adicional
     * @return nuul si no existe problema, o la cadena que describe el problema de validación encontrado
     */
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean allowsUpdatePassword() throws WayAccessException{
        return true;
    }
    
    @Override
    public boolean allowsUpdateUser() throws WayAccessException{
        return true;
    }
    
    @Override
    public boolean allowsCreateUser() throws WayAccessException{
        return true;
    }
    
    @Override
    public User getUserData(String id) throws WayAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public  List<User> validateLoginLifes() throws WayAccessException{
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Map<String, Object> updateUser(Map<String, Object> datos) throws WayAccessException{
        return null;
    }
    
    @Override
    public boolean activeUser(String id, boolean active) throws WayAccessException{
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    /**
     * Contruye la tabla de usuarios en la BD configurada.
     * @return <i>true</i> si logro generarla
     * @throws java.lang.Exception
     */
    public boolean createTable() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("usuario.sql");

        if (in != null) {
            StringBuilder sb = new StringBuilder("");
            int c;

            while ((c = in.read()) != -1) {
                sb.append((char) c);
            }

            Connection con = ConfigurationDB.createConection(this.config);
            Statement st = con.createStatement();
            st.execute(sb.toString());
            st.close();
            con.close();

            return true;
        }


        return false;
    }
    
    /**
     * Modifica los campos utilizados por default para las consultas
     * @param field Nombre del campo {ID, NAME, LOGIN, PASS, MAIL, STATUS, LIFE, TABLE}
     * @param value Valor a asignar
     */
    public void setField(String field, String value){
        if(field!=null && field.equalsIgnoreCase(AuthenticationWay.ID_FIELD)){
            campos.setProperty(AuthenticationWay.ID_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase(AuthenticationWay.NAME_FIELD)){
            campos.setProperty(AuthenticationWay.NAME_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase(AuthenticationWay.LOGIN_FIELD)){
            campos.setProperty(AuthenticationWay.LOGIN_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase(AuthenticationWay.PASSWORD_FIELD)){
            campos.setProperty(AuthenticationWay.PASSWORD_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase(AuthenticationWay.MAIL_FIELD)){
            campos.setProperty(AuthenticationWay.MAIL_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase(AuthenticationWay.STATUS_FIELD)){
            campos.setProperty(AuthenticationWay.STATUS_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase(AuthenticationWay.LIFE_FIELD)){
            campos.setProperty(AuthenticationWay.LIFE_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase(AuthenticationWay.LAST_ACCESS_FIELD)){
            campos.setProperty(AuthenticationWay.LAST_ACCESS_FIELD, value);
        }
        else if(field!=null && field.equalsIgnoreCase("TABLE")){
            campos.setProperty("TABLE", value);
        }
    }

}
