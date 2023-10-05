package neoAtlantis.utilidades.accessController.profiler;

import java.io.*;
import java.sql.*;
import java.util.*;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utilidades.accessController.objects.Role;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.profiler.interfaces.ProfilerWay;
import neoAtlantis.utilidades.bd.ConfigurationDB;
import org.apache.log4j.Logger;

/**
 * Medio Perfilador operado a traves de BD, para lo cual utiliza una tabla con
 * la siguiente estructura:<br>
 * <pre>
 * CREATE TABLE roles_na(
 *     id_rol      INT NOT NULL,
 *     rol         VARCHAR(50),
 *     usuario     VARCHAR(15)
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
 * Para trabajar adecuamente este objeto requiere la libreria de <b>NA_Utils</b>.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.2
 */
public class DbProfiler extends ProfilerWay {
    /**
     * Loggeador de la clase
     */
    static final Logger logger = Logger.getLogger(DbProfiler.class);

    /**
     * Cadena en donde se almacenan los querys a ejecutarse.
     */
    protected StringBuffer sql;
    /**
     * Configuración de conexi&oacute;n a la BD
     */
    protected Properties config;

    /**
     * Genera un ProfilerWay por Base de Datos.
     * @param xml Flujo del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbProfiler(InputStream xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(xml);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un ProfilerWay por Base de Datos.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbProfiler(File xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(new FileInputStream(xml));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un ProfilerWay por Base de Datos.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbProfiler(String xml) throws WayConfigurationException {
        this(new File(xml));
    }

    /**
     * Genera un ProfilerWay por Base de Datos.
     * @param configBD Configuraci&oacute;n de acceso a la BD
     * @throws java.lang.Exception
     */
    public DbProfiler(Properties configBD) throws WayConfigurationException {
        try{
            ConfigurationDB.validaConfigProperties(configBD);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.config=configBD;
    }

    /**
     * Genera un ProfilerWay por BD.
     * @param driver Driver a utilizar para la conexi&oacute;n a la BD
     * @param url URL de conexi&oacute;n a la BD
     * @param user Usuario a utilizar para la conexi&oacute;n a la BD
     * @param pass Contrase&ntilde;a a utilizar para la conexi&oacute;n a la BD
     * @throws WayConfigurationException 
     */
    public DbProfiler(String driver, String url, String user, String pass) throws WayConfigurationException {
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
     * Genera el query utilizado para la obtener los registros de los roles.
     * @return Cadena con el query
     */
    protected String getQuerySelectRol(){
        //               1      2  
        return "SELECT id_rol, rol FROM roles_na WHERE usuario=?";
    }

    //---------------------------------------------------------------------------------
    
    /**
     * Recupera los roles de un usuario.
     * @param user Usuario del que se desean los roles
     * @param param Paramatros para obtener los roles
     * @return Lista de roles
     * @throws WayAccessException 
     */
    @Override
    public List<Role> obtieneRoles(User user, Object... param) throws WayAccessException {
        ArrayList<Role> roles=new ArrayList<Role>();
        Role r;

        try{
            this.sql = new StringBuffer(this.getQuerySelectRol());
            logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");

            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, user.getUser());

            ResultSet res = ps.executeQuery();
            while(res.next()){
                r=new Role(res.getString(1), res.getString(2));
                roles.add(r);
            }
            
            res.close();
            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro obtener los roles de '" + user + "'.", ex);
            throw new WayAccessException(ex);
        }

        return roles;
    }

    /**
     * Asigna un rol a un usuario.
     * @param user Usuario al que se desea asignar el rol
     * @param rol Rol a asignar
     * @param param Parametors para asignar los roles
     * @return true si se logro asignar 
     * @throws WayAccessException 
     */
    @Override
    public boolean asignaRol(User user, Role rol, Object... param) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Remueve un rol a un usuario.
     * @param user Usuario al que se desea remover el rol
     * @param rol Rol a remover
     * @param param Parametors para remover los roles
     * @return true si se logro remover 
     * @throws WayAccessException 
     */    
    @Override
    public boolean remueveRol(User user, Role rol, Object... param) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
