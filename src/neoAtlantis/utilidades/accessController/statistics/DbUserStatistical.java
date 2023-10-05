package neoAtlantis.utilidades.accessController.statistics;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletContext;
import neoAtlantis.utilidades.accessController.utils.AccessControllerPublisher;
import neoAtlantis.utilidades.bd.ConfigurationDB;
import neoAtlantis.utilidades.statistics.exceptions.StatisticException;
import neoAtlantis.utilidades.statistics.exceptions.StatisticsConfigurationException;
import neoAtlantis.utilidades.statistics.objects.DbStatistical;
import org.apache.log4j.Logger;

/**
 * Interface que define el comportamiento con el que debe contar un Estadistico de usuario.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class DbUserStatistical extends DbStatistical {
    static final Logger DEBUGGER = Logger.getLogger(DbUserStatistical.class);
    
    protected ServletContext context;

    public DbUserStatistical(InputStream xml, ServletContext context) throws StatisticsConfigurationException {
        super(xml, "statistics_user_NA");
        this.context=context;
    }

    public DbUserStatistical(File xml, ServletContext context) throws StatisticsConfigurationException {
        super(xml, "statistics_user_NA");
        this.context=context;
    }

    public DbUserStatistical(String xml, ServletContext context) throws StatisticsConfigurationException {
        super(xml, "statistics_user_NA");
        this.context=context;
    }

    public DbUserStatistical(Properties configBD, ServletContext context) throws StatisticsConfigurationException {
        super(configBD, "statistics_user_NA");
        this.context=context;
    }

    public DbUserStatistical(String driver, String url, String user, String pass, ServletContext context) throws StatisticsConfigurationException {
        super(driver, url, user, pass, "statistics_user_NA");
        this.context=context;
    }

    // -------------------------------------------------------------------------
    
    protected String getQueryInsert(){
        return "INSERT INTO statistics_user_NA (users, period) VALUES (?, ?)";
    }
    
    // -------------------------------------------------------------------------
    
    public void generate() throws StatisticException {                
        this.sql = new StringBuffer(this.getQueryInsert());
        DEBUGGER.debug("Intenta ejecutar la sentencia '" + this.sql.toString() + "'.");

        try{
            Connection con = ConfigurationDB.generaConexion(this.config);
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setInt(1, ((List)this.context.getAttribute(AccessControllerPublisher.CLAVE_CONECTIONS)).size());
            ps.setTimestamp(2, new Timestamp((new Date()).getTime()));
            
            ps.executeUpdate();
            
            ps.close();
            con.close();            
        }catch(Exception ex){
            DEBUGGER.fatal("No se almacenar el valor en la estadistica de usuario.", ex);
            throw new StatisticException(ex);
        }
    }
    
}
