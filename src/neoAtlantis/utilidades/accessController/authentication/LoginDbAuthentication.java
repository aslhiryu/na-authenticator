package neoAtlantis.utilidades.accessController.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import neoAtlantis.utilidades.accessController.authentication.interfaces.AuthenticationWay;
import neoAtlantis.utilidades.accessController.authentication.interfaces.ValidationResult;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utilidades.accessController.objects.EnvironmentType;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.bd.ConfigurationDB;
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
 * @version 5.2
 */
public class LoginDbAuthentication extends AuthenticationWay {
    /**
     * Loggeador de la clase
     */
    static final Logger logger = Logger.getLogger(LoginDbAuthentication.class);

    /**
     * Cadena en donde se almacenan los querys a ejecutarse.
     */
    protected StringBuffer sql;
    /**
     * Configuración de conexi&oacute;n a la BD
     */
    protected Properties config;

    /**
     * Genera un AuthenticationWay por Base de Datos.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public LoginDbAuthentication(InputStream xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(xml);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por Base de Datos.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public LoginDbAuthentication(File xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(new FileInputStream(xml));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuthenticationWay por Base de Datos.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public LoginDbAuthentication(String xml) throws WayConfigurationException {
        this(new File(xml));
    }

    /**
     * Genera un AuthenticationWay por Base de Datos.
     * @param configBD Configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public LoginDbAuthentication(Properties configBD) throws WayConfigurationException {
        try{
            ConfigurationDB.validaConfigProperties(configBD);
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
    public LoginDbAuthentication(String driver, String url, String user, String pass) throws WayConfigurationException {
        this.config.setProperty("driver", driver);
        this.config.setProperty("url", url);
        this.config.setProperty("user", user);
        this.config.setProperty("pass", pass);
        try{
            ConfigurationDB.validaConfigProperties(this.config);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    //---------------------------------------------------------------------------------

    /**
     * Genera el query utilizado para la validación d eunusuario.
     * @return Cadena con el query
     */
    protected String getQuerySelectUser(){
        //                  1        2      3     4      5       6
        return "SELECT id_usuario, nombre, pass, mail, estado, expira FROM usuario_na WHERE login=?";
    }

    /**
     * Genera el query utilizado para la busqueda de usuarios.
     * @return Cadena con el query
     */
    protected String getQuerySearchUsers(){
        //                  1        2      3       4    
        return "SELECT id_usuario, login, nombre, mail FROM usuario_na WHERE login=? OR nombre=?";
    }
    
    /**
     * Carga información adicional del usuario a partir de campos en la BD.
     * @param res ResultSet del registro encontrado
     * @param user Usuario al que se va asigna los datos
     */
    protected void cargaDatosExtras(ResultSet res, User user){
        //nada
    }

    //---------------------------------------------------------------------------------

    /**
     * Valida un usuario, para tal fin requiere los datos <b>user</b> y <b>pass</b>
     * sean suministrados.
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
        
        this.sql = new StringBuffer(this.getQuerySelectUser());
        logger.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, (String)datos.get("user"));

            ResultSet res = ps.executeQuery();

            //si existe el usuario
            if (res.next()) {
                logger.debug("Intenta la validacion de '" + datos.get("user") + "' : '"+this.cifrador.cifra((String)datos.get("pass"))+"'.");

                //cargo la informacion del usuario
                user=new User(res.getString(1), (String)datos.get("user"), "127.0.0.1", "localhost", EnvironmentType.WEB);
                user.setNombre(res.getString(2));
                user.setMail(res.getString(4));
                cargaDatosExtras(res, user);

                //valida la contraseña
                if (res.getString(3)==null || !res.getString(3).equals(this.cifrador.cifra((String)datos.get("pass")))) {
                    user.setEstado(ValidationResult.DENEGADO);
                }
                else{
                    //valida si la cuenta es temporal
                    if( res.getString(5)!=null && res.getString(5).equals("T") ){
                        user.setEstado(ValidationResult.VALIDADO_TEMPORAL);
                    }
                    //valida si esta activo
                    else if( res.getString(5)!=null && 
                            (res.getString(5).toUpperCase().equalsIgnoreCase("I") || res.getString(5).equals("0") || 
                            res.getString(5).toUpperCase().equalsIgnoreCase("F")) || res.getString(5).toUpperCase().equalsIgnoreCase("FALSE")){
                        user.setEstado(ValidationResult.INACTIVO);
                    }
                    //valida si esta vigente la cuenta
                    else if( res.getString(6)!=null && (new java.util.Date()).getTime() > res.getTimestamp(6).getTime() ){
                        user.setEstado(ValidationResult.CADUCADO);
                    }
                    else{
                        user.setEstado(ValidationResult.VALIDADO);
                    }
                }

                logger.debug("Con resultado '" + user + "'.");
            }

            res.close();
            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro autenticar la cuenta '" + datos.get("user") + "'.", ex);
            throw new WayAccessException(ex);
        }

        return user;
    }

    @Override
    public boolean agregaCuenta(User user, String pass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean agregaCuentaTemporal(User user){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean modificaContrasena(User user, String pass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean restauraContrasena(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Validaci&oacute;n posterior a la validaci&oacute;n de credenciales.
     * @param usuario Usuario al que se le realiza la evaluación adicional
     * @return nuul si no existe problema, o la cadena que describe el problema de validación encontrado
     */
    @Override
    public String validacionAdicional(User usuario){
        return null;
    }
    
    
    /**
     * Contruye la tabla de usuarios en la BD configurada.
     * @return <i>true</i> si logro generarla
     * @throws java.lang.Exception
     */
    public boolean generaTablaUsuario() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("usuario.sql");

        if (in != null) {
            StringBuilder sb = new StringBuilder("");
            int c;

            while ((c = in.read()) != -1) {
                sb.append((char) c);
            }

            Connection con = ConfigurationDB.generaConexion(this.config);
            Statement st = con.createStatement();
            st.execute(sb.toString());
            st.close();
            con.close();

            return true;
        }


        return false;
    }

    @Override
    public String generaEntornoAutenticacionWeb(String action, String captchaService) {
        StringBuilder sb=new StringBuilder("");

        sb.append("<body onload=\"document.formaLoginNA.user.focus()\">").append(System.getProperty("line.separator"))
                .append("<form name=\"formaLoginNA\" action=\"").append(action).append("\" method=\"post\">").append(System.getProperty("line.separator"))
                .append("<table class=\"tableLoginNA\">").append(System.getProperty("line.separator"))
                .append("<tr>").append(System.getProperty("line.separator"))
                .append("<td class=\"celdaLoginNA\">Usuario:</td>").append(System.getProperty("line.separator"))
                .append("<td class=\"celdaLoginNA\"><input name=\"user\" size=\"10\" class=\"inputLoginNA\" /></td>").append(System.getProperty("line.separator"))
                .append("</tr>").append(System.getProperty("line.separator"))
                .append("<tr>").append(System.getProperty("line.separator"))
                .append("<td class=\"celdaLoginNA\">Contraseña:</td>").append(System.getProperty("line.separator"))
                .append("<td class=\"celdaLoginNA\"><input type=\"password\" name=\"pass\" size=\"10\" class=\"inputLoginNA\" /></td>").append(System.getProperty("line.separator"))
                .append("</tr>").append(System.getProperty("line.separator"));
        if( captchaService!=null && captchaService.length()>0 ){
            logger.debug("Genera entorno con captcha en '"+captchaService+"'.");

            sb.append("<tr>").append(System.getProperty("line.separator"))
                    .append("<td class=\"celdaLoginNA\">Clave inferior:</td>").append(System.getProperty("line.separator"))
                    .append("<td class=\"celdaLoginNA\"><input name=\"").append(CODE_PARAM).append("\" size=\"10\" class=\"inputLoginNA\" /></td>").append(System.getProperty("line.separator"))
                    .append("</tr>").append(System.getProperty("line.separator"))
                    .append("<tr>").append(System.getProperty("line.separator"))
                    .append("<td class=\"celdaLoginNA\" align=\"center\" colspan=\"2\"><img src=\"").append(captchaService).append("\" width=\"200\" height=\"70\" /></td>").append(System.getProperty("line.separator"))
                    .append("</tr>").append(System.getProperty("line.separator"));
        }
        sb.append("<tr>").append(System.getProperty("line.separator"))
                .append("<td class=\"celdaLoginNA\" align=\"center\" colspan=\"2\"><input type=\"submit\" value=\"Acceder\" class=\"botonLoginNA\" /></td>").append(System.getProperty("line.separator"))
                .append("</tr>").append(System.getProperty("line.separator"))
                .append("</table>").append(System.getProperty("line.separator"))
                .append("<input type=\"hidden\" name=\"").append(PARAM_LOGIN).append("\" />").append(System.getProperty("line.separator"))
                .append("</form>").append(System.getProperty("line.separator"));

        return sb.toString();
    }

    /**
     * Busca usuarios en la BD.
     * @param param Datos del usuario a buscar.
     * @return Lista de usuarios encontrados
     * @throws WayAccessException 
     */
    @Override
    public List<User> buscaUsuarios(Map<String, Object> param) throws WayAccessException {
        ArrayList users=new ArrayList<User>();
        User user;
        
        this.sql = new StringBuffer(this.getQuerySelectUser());
        logger.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");
        
        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, "%"+(String)param.get("cadenaBusqueda")+"%");
            ps.setString(2, "%"+(String)param.get("cadenaBusqueda")+"%");
            
            ResultSet res = ps.executeQuery();

            //si existen usuarios
            while (res.next()) {
                user=new User(res.getString(1), res.getString(2), "127.0.0.1", "localhost", EnvironmentType.WEB);
                user.setNombre(res.getString(3));
                user.setMail(res.getString(4));
                users.add(user);
            }
            
            res.close();
            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro recuperar los usuarios con '" + param.get("cadenaBusqueda") + "'.", ex);
            throw new WayAccessException(ex);
        }
            
        return users;
    }

}
