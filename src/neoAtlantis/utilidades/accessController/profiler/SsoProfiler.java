package neoAtlantis.utilidades.accessController.profiler;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utilidades.accessController.objects.Role;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.profiler.interfaces.ProfilerWay;
import neoAtlantis.utilidades.bd.ConfigurationDB;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class SsoProfiler extends ProfilerWay {
    static final Logger logger = Logger.getLogger(SsoProfiler.class);

    protected StringBuffer sql;
    protected Properties config;

    /**
     * 
     * @param xml
     * @throws WayConfigurationException 
     */
    public SsoProfiler(InputStream xml) throws WayConfigurationException {        
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(xml);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un Autenticador por Base de Datos.
     * @param xml Archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException
     */
    public SsoProfiler(File xml) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(new FileInputStream(xml));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un Autenticador por Base de Datos.
     * @param xml Ruta completa del archivo que contiene la configuraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException
     */
    public SsoProfiler(String xml) throws WayConfigurationException {
        this(new File(xml));
    }

    /**
     * Genera un Autenticador por Base de Datos, tomando los valores por default para los campos en la BD.
     * @param configBD Configuraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException
     */
    public SsoProfiler(Properties configBD) throws WayConfigurationException {
        try{
            ConfigurationDB.validaConfigProperties(configBD);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.config=configBD;
    }

    public SsoProfiler(String driver, String url, String user, String pass) throws WayConfigurationException {
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

    @Override
    public List<Role> obtieneRoles(User user, Object... param) throws WayAccessException {
        ArrayList<Role> roles=new ArrayList<Role>();
        Role r;

        try{
            this.sql = new StringBuffer("SELECT roles.id_rol, roles.rol FROM roles LEFT JOIN rol_usuario ON roles.id_rol = rol_usuario.rol LEFT JOIN sistema ON roles.sistema=sistema.id_sistema WHERE rol_usuario.usuario =? AND sistema.clave =?");
            logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");

            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, user.getUser());
            ps.setString(2, (String)param[0]);

            ResultSet res = ps.executeQuery();
            while(res.next()){
                r=new Role(res.getString("id_rol"), res.getString("rol"));
                roles.add(r);
                user.agregaRol(r);
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

    @Override
    public boolean asignaRol(User user, Role rol, Object... param) throws WayAccessException {
        int r=0;

        try{
            this.sql = new StringBuffer("INSERT INTO rol_usuario (rol, usuario) VALUES(?, ?)");
            logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");

            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, user.getUser());
            ps.setString(2, rol.getId());

            r=ps.executeUpdate();

            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro agregar el rol a '" + user + "'.", ex);
            throw new WayAccessException(ex);
        }

        return (r==0? false: true);
    }

    @Override
    public boolean remueveRol(User user, Role rol, Object... param) throws WayAccessException {
        int r=0;

        try{
            this.sql = new StringBuffer("DELETE FROM rol_usuario WHERE rol=? AND usuario=?");
            logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");

            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, user.getUser());
            ps.setString(2, rol.getId());

            r=ps.executeUpdate();

            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro eleminar el rol de '" + user + "'.", ex);
            throw new WayAccessException(ex);
        }

        return (r==0? false: true);
    }

}
