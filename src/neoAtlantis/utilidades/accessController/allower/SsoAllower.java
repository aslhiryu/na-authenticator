    package neoAtlantis.utilidades.accessController.allower;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import neoAtlantis.utilidades.accessController.allower.interfaces.AllowerWay;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utilidades.accessController.objects.Permission;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.bd.ConfigurationDB;
import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class SsoAllower extends AllowerWay {
    static final Logger logger = Logger.getLogger(SsoAllower.class);

    protected StringBuffer sql;
    protected Properties config;

    public SsoAllower(InputStream xml) throws WayConfigurationException {
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
    public SsoAllower(File xml) throws WayConfigurationException {
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
    public SsoAllower(String xml) throws WayConfigurationException {
        this(new File(xml));
    }

    /**
     * Genera un Autenticador por Base de Datos, tomando los valores por default para los campos en la BD.
     * @param configBD Configuraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException
     */
    public SsoAllower(Properties configBD) throws WayConfigurationException {
        try{
            ConfigurationDB.validaConfigProperties(configBD);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.config=configBD;
    }

    public SsoAllower(String driver, String url, String user, String pass) throws WayConfigurationException {
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
    
    @Override
    public List<Permission> obtienePermisos(User user, Object... params) throws WayAccessException {
        ArrayList<Permission> perms=new ArrayList<Permission>();
        Permission p;

        try{
            this.sql = new StringBuffer("SELECT permisos.id_permiso, permisos.permiso FROM permisos LEFT JOIN permiso_usuario ON permisos.id_permiso=permiso_usuario.permiso LEFT JOIN sistema ON permisos.sistema=sistema.id_sistema WHERE permiso_usuario.usuario =? AND sistema.clave =?");
            logger.debug("Intenta ejecutar la sentencia '" + sql.toString() + "'.");

            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setString(1, user.getUser());
            ps.setString(2, (String)params[0]);

            ResultSet res = ps.executeQuery();
            while(res.next()){
                p=new Permission(res.getString("permiso"));
                perms.add(p);
                user.agregarPermiso(p);
            }

            res.close();
            ps.close();
            con.close();
        }catch(Exception ex){
            logger.fatal("No se logro obtener los permisos de '" + user + "'.", ex);
            throw new WayAccessException(ex);
        }

        return perms;
    }

    @Override
    public boolean asignaPermiso(User user, Permission permiso) throws WayAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean remuevePermiso(User user, Permission permiso) throws WayAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
