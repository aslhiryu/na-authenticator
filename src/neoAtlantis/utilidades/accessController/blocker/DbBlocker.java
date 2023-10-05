package neoAtlantis.utilidades.accessController.blocker;

import java.io.*;
import java.sql.*;
import java.util.*;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockType;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.bd.ConfigurationDB;
import org.apache.log4j.Logger;

/**
 * Medio Bloqueador operado a traves de BD, para lo cual utiliza dos tablas con
 * las siguientes estructuras:<br>
 * <pre>
 * CREATE TABLE bloqueo_na(
 *     usuario     VARCHAR(30),
 *     origen      VARCHAR(30),
 *     fecha       DATETIME,
 *     tipo        VARCHAR(1)
 * ); 
 * CREATE TABLE conexion_na(
 *     usuario     VARCHAR(30),
 *     origen      VARCHAR(30),
 *     fecha       DATETIME
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
 * @version 2.0
 */
public class DbBlocker extends BlockerWay {
    /**
     * Loggeador de la clase
     */
    static final Logger logger = Logger.getLogger(DbBlocker.class);
    /**
     * Cadena en donde se almacenan los querys a ejecutarse.
     */
    protected StringBuffer sql;
    /**
     * Configuración de conexi&oacute;n a la BD
     */
    protected Properties config;

    /**
     * Genera un BlockerWay por BD.
     * @param xml Flujo de bits que contienen la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbBlocker(InputStream xml) throws WayConfigurationException{
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(xml);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un BlockerWay por BD.
     * @param xml Archivo que contienen la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbBlocker(File xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(new FileInputStream(xml));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un BlockerWay por BD.
     * @param xml Ruta del archivo que contienen la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbBlocker(String xml) throws WayConfigurationException {
        this(new File(xml));
    }

    /**
     * Genera un BlockerWay por BD.
     * @param configBD Propiedades con la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbBlocker(Properties configBD) throws WayConfigurationException {
        try{
            ConfigurationDB.validaConfigProperties(configBD);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.config=configBD;
    }

    /**
     * Genera un BlockerWay por BD.
     * @param driver Driver a utilizar para la conexi&oacute;n a la BD
     * @param url URL de conexi&oacute;n a la BD
     * @param user Usuario a utilizar para la conexi&oacute;n a la BD
     * @param pass Contrase&ntilde;a a utilizar para la conexi&oacute;n a la BD
     * @throws WayConfigurationException 
     */
    public DbBlocker(String driver, String url, String user, String pass) throws WayConfigurationException {
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

    //--------------------------------------------------------------------------

    /**
     * Genera el query utilizado para la inserci&ocute;n de registros de bloqueo.
     * @return Cadena con el query
     */
    protected String getQueryInsertBloqueo(){
        //                                 1        2      3     4
        return "INSERT INTO bloqueo_na (usuario, origen, fecha, tipo)  VALUES(?, ?, ?, ?)";
    }

    /**
     * Genera el query utilizado para la recuperar los registros de bloqueo.
     * @return Cadena con el query
     */
    protected String getQuerySelectBloqueo(){
        //               1        2      3   
        return "SELECT fecha, usuario, origen FROM bloqueo_na ORDER BY fecha DESC";
    }

    /**
     * Genera el query utilizado para recuperar un bloqueo.
     * @return Cadena con el query
     */
    protected String getQuerySimpleSelectBloqueo(){
        //                 1        2      3   
        String c="SELECT fecha, usuario, origen FROM bloqueo_na WHERE ";
        if( this.modoBloqueo==BlockType.IP ){
            this.sql.append("origen=?");
        }
        else{
            this.sql.append("usuario=?");
        }
        
        return c;
    }

    /**
     * Genera el query utilizado para la eliminar registros de bloqueo.
     * @return Cadena con el query
     */
    protected String getQueryDeleteBloqueo(){
        //                                       1          2     
        return "DELETE FROM bloqueo_na WHERE usuario=? OR origen=?";
    }

    /**
     * Genera el query utilizado para eliminar un bloqueo.
     * @return Cadena con el query
     */
    protected String getQuerySimpleDeleteBloqueo(){
        //                                     
        String c="DELETE FROM bloqueo_na WHERE ";
        if( this.modoBloqueo==BlockType.IP ){
            this.sql.append("origen=?");
        }
        else{
            this.sql.append("usuario=?");
        }
        
        return c;
    }
    
    /**
     * Genera el query utilizado para la inserci&ocute;n de registros de conexi&oacute;n.
     * @return Cadena con el query
     */
    protected String getQueryInsertConexion(){
        //                                 1        2      3   
        return "INSERT INTO conexion_na (usuario, origen, fecha)  VALUES(?, ?, ?)";
    }
    
    /**
     * Genera el query utilizado para eliminar una conexi&oacute;n.
     * @return Cadena con el query
     */
    protected String getQuerySimpleDeleteConexion(){
        //                                       1             2
        return "DELETE FROM conexion_na WHERE usuario=? AND origen=?";
    }
    
    /**
     * Genera el query utilizado para recuperar una conexi&oacute;n.
     * @return Cadena con el query
     */
    protected String getQuerySimpleSelectConexion(){
        //                 1        2      3   
        return "SELECT usuario, origen, fecha FROM conexion_na WHERE usuario=?";
    }
    
    
    /**
     * Genera el query utilizado para la recuperar los registros de conexi&oacute;n.
     * @return Cadena con el query
     */
    protected String getQuerySelectConexion(){
        //               1        2      3   
        return "SELECT fecha, usuario, origen FROM conexion_na ORDER BY fecha DESC";
    }
    
    /**
     * Genera el query utilizado para la eliminar registros de conexi&oacute;n.
     * @return Cadena con el query
     */
    protected String getQueryDeleteConexion(){
        //                                       1          2     
        return "DELETE FROM conexion_na WHERE usuario=? OR origen=?";
    }
    
    //---------------------------------------------------------------------------------

    /**
     * Agregar el bloqueo de un usuario.
     * @param user Usuario a bloquear
     * @throws java.lang.WayAccessException
     */
    @Override
    public void agregaBloqueo(User user) throws WayAccessException {
        this.sql = new StringBuffer(this.getQueryInsertBloqueo());
        logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");

        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, user.getUser());
            ps.setString(2, user.getOrigen());
            ps.setTimestamp(3, new Timestamp((new java.util.Date()).getTime()));
            ps.setString(4, this.modoBloqueo.toString());

            ps.executeUpdate();

            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro generar la conexion de '" + user + "'.", ex);
            throw new WayAccessException(ex);
        }
    }

    /**
     * Revisa los bloqueos que ya hayan finalizado.
     * @return Colecci&oacute;n con los nombres de los usuarios de los cuales termino su bloqueo.
     * @throws java.lang.WayAccessException
     */
    @Override
    public List<User> revisaBloqueosTerminados() throws WayAccessException {
        ArrayList<User> objs = new ArrayList<User>();
        int i=0;
        long fecha;
        String obj, ori, ter;

        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            Statement st = con.createStatement();

            this.sql = new StringBuffer(this.getQuerySelectBloqueo());
            logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");
            ResultSet res = st.executeQuery(this.sql.toString());

            this.sql = new StringBuffer(this.getQueryDeleteBloqueo());
            logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");
            PreparedStatement ps = con.prepareStatement(sql.toString());

            while (res.next()) {
                fecha = res.getTimestamp("fecha").getTime();
                obj = res.getString("usuario");
                ori = res.getString("origen");
                ter = res.getString("terminal");
                if (fecha + this.tiempoBloqueo <= (new java.util.Date()).getTime()) {
                    ps.setString(1, obj);
                    ps.setString(2, ori);
                    ps.execute();
                    logger.debug("Remueve el objeto '" + obj + "' bloqueado.");
                    objs.add(new User(obj, ori, ter));
                }

            }


            st.close();
            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro finalizar los bloqueos terminados.", ex);
            throw new WayAccessException(ex);
        }

        return objs;
    }

    /**
     * Revisa si un usuario esta bloqueado.
     * @param user Usuario del que se desea verificar su bloqueo
     * @return true si existe bloqueo
     * @throws java.lang.WayAccessException
     */
    @Override
    public boolean verificaBloqueo(User user) throws WayAccessException {
        boolean existe=false;

        this.sql = new StringBuffer(this.getQuerySimpleSelectBloqueo());
        logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");

        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            if( this.modoBloqueo==BlockType.IP ){
                ps.setString(1, user.getOrigen());
            }
            else{
                ps.setString(1, user.getUser());
            }

            ResultSet res = ps.executeQuery();
            if( res.next() ){
                existe=true;
            }

            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro verificar el bloqueo de '" + user + "'.", ex);
            throw new WayAccessException(ex);
        }

        return existe;
    }

    /**
     * Remueve el bloqueo de un usuario.
     * @param user Usuario del que se desea terminar el bloqueo
     * @return true si se logro remover el bloqueo
     * @throws java.lang.WayAccessException
     */
    @Override
    public boolean remueveBloqueo(User user) throws WayAccessException {
        int i=0;

        this.sql = new StringBuffer(this.getQuerySimpleDeleteBloqueo());
        logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");

        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            if( this.modoBloqueo==BlockType.IP ){
                ps.setString(1, user.getOrigen());
            }
            else{
                ps.setString(1, user.getUser());
            }

            i=ps.executeUpdate();

            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro remover el bloqueo de '" + user + "'.", ex);
            throw new WayAccessException(ex);
        }

        return (i>0);
    }

    /**
     * Agrega la conexi&oacute;n de un usuario.
     * @param user Usuario del que se desea registrar su conexi&oacute;n
     * @throws java.lang.WayAccessException
     */
    @Override
    public void agregaConexion(User user) throws WayAccessException {
        this.sql = new StringBuffer(this.getQueryInsertConexion());
        logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");

        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, user.getUser());
            ps.setString(2, user.getOrigen());
            ps.setTimestamp(3, new Timestamp((new java.util.Date()).getTime()));

            ps.executeUpdate();

            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro generar la conexion de '" + user + "'.", ex);
            throw new WayAccessException(ex);
        }
    }

    /**
     * Remueve la conexi&oacute;n de un usuario.
     * @param user Usuario del que se desea remover su conexi&oacute;n
     * @return true si se logro remover la conexi&oacute;n
     * @throws java.lang.WayAccessException
     */
    @Override
    public boolean remueveConexion(User user) throws WayAccessException {
        int i=0;

        this.sql = new StringBuffer(this.getQuerySimpleDeleteConexion());
        logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");

        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, user.getUser());
            ps.setString(2, user.getOrigen());

            i=ps.executeUpdate();

            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro eliminar la conexion de '" + user + "'.", ex);
            throw new WayAccessException(ex);
        }

        return (i>0);
    }

    /**
     * Revisa si un usuario esta conectado.
     * @param user Usuario del que se desea revisar su conexi&oacute;n
     * @return true si tiene una conexi&oacute;n activa
     * @throws java.lang.WayAccessException
     */
    @Override
    public boolean verificaConexion(User user) throws WayAccessException {
        boolean existe=false;

        this.sql = new StringBuffer(this.getQuerySimpleSelectConexion());
        logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");

        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, user.getUser());

            ResultSet res = ps.executeQuery();
            if( res.next() ){
                existe=true;
            }

            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro generar la conexion de '" + user + "'.", ex);
            throw new WayAccessException(ex);
        }

        return existe;
    }

    @Override
    public void actualizaActividadConexion(User user) {
        
    }

    @Override
    public User getUsuarioConectado(String id) {
        return null;
    }

    /**
     * Revisa las sesiones que ya hayan finalizado.
     * @return Colecci&oacute;n con los usuarios que expiraron sus sesiones.
     * @throws java.lang.WayAccessException
     */
    @Override
    public List<User> revisaSesionesInactivas() throws WayAccessException {
        ArrayList<User> objs = new ArrayList<User>();
        int i=0;
        long fecha;
        String obj, ori, ter;

        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            Statement st = con.createStatement();

            this.sql = new StringBuffer(this.getQuerySelectConexion());
            logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");
            ResultSet res = st.executeQuery(this.sql.toString());

            this.sql = new StringBuffer(this.getQueryDeleteConexion());
            logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");
            PreparedStatement ps = con.prepareStatement(sql.toString());

            while (res.next()) {
                fecha = res.getTimestamp("fecha").getTime();
                obj = res.getString("usuario");
                ori = res.getString("origen");
                ter = res.getString("terminal");
                if (fecha + this.tiempoSesion <= (new java.util.Date()).getTime()) {
                    ps.setString(1, obj);
                    ps.setString(2, ori);
                    ps.execute();
                    logger.debug("Remueve el objeto '" + obj + "' de sesion.");
                    objs.add(new User(obj, ori, ter));
                }

            }


            st.close();
            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro remover las conexiones caducadas.", ex);
            throw new WayAccessException(ex);
        }

        return objs;
    }

    @Override
    public User getUsuarioBloqueado(String id){
        return null;
    }

    @Override
    public List<User> getConexiones() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User getUsuarioConectadoSesion(String ses) {
        return null;
    }
}
