package neoAtlantis.utilidades.accessController.audit;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay;
import neoAtlantis.utilidades.accessController.audit.interfaces.EventAudit;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.exceptions.WayConfigurationException;
import neoAtlantis.utilidades.accessController.objects.EnvironmentType;
import neoAtlantis.utilidades.bd.ConfigurationDB;
import neoAtlantis.utilidades.objects.Event;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
  */
public class DbParamsAudit extends AuditWay {
    /**
     * Loggeador de la clase
     */
    static final Logger logger = Logger.getLogger(DbParamsAudit.class);

    /**
     * Configuración de conexi&oacute;n a la BD
     */
    protected Properties config;

    /**
     * Genera un AuditWay por BD.
     * @param xml Flujo de bits que contienen la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbParamsAudit(InputStream xml, Properties mapeos) throws WayConfigurationException{
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(xml);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuditWay por BD.
     * @param xml Archivo que contienen la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbParamsAudit(File xml, Properties mapeos) throws WayConfigurationException {
        try{
            this.config=ConfigurationDB.parseConfiguracionXML(new FileInputStream(xml));
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
    }

    /**
     * Genera un AuditWay por BD.
     * @param xml Ruta del archivo que contienen la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbParamsAudit(String xml, Properties mapeos) throws WayConfigurationException {
        this(new File(xml), mapeos);
    }

    /**
     * Genera un AuditWay por BD.
     * @param configBD Propiedades con la onfiguraci&oacute;n de acceso a la BD
     * @throws WayConfigurationException 
     */
    public DbParamsAudit(Properties configBD, Properties mapeos) throws WayConfigurationException {
        try{
            ConfigurationDB.validaConfigProperties(configBD);
        }catch(Exception ex){
            throw new WayConfigurationException(ex);
        }
        this.config=configBD;
    }

    /**
     * Genera un AuditWay por BD.
     * @param driver Driver a utilizar para la conexi&oacute;n a la BD
     * @param url URL de conexi&oacute;n a la BD
     * @param user Usuario a utilizar para la conexi&oacute;n a la BD
     * @param pass Contrase&ntilde;a a utilizar para la conexi&oacute;n a la BD
     * @throws WayConfigurationException 
     */
    public DbParamsAudit(String driver, String url, String user, String pass, Properties mapeos) throws WayConfigurationException {
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
    public List<Event> recuperaBitacora(Map<String, Object> param, int regs, int offset) throws WayAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void escribeBitacora(String usuario, EnvironmentType tipoTerminal, String origen, String terminal, EventAudit evento, String detalle) throws WayAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
