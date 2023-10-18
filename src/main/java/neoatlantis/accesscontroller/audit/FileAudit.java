package neoatlantis.accesscontroller.audit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import neoatlantis.accesscontroller.audit.interfaces.AuditWay;
import neoatlantis.accesscontroller.audit.interfaces.EventAudit;
import neoatlantis.accesscontroller.exceptions.WayAccessException;
import neoatlantis.accesscontroller.objects.EnvironmentType;
import neoatlantis.entity.Event;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class FileAudit extends AuditWay {
    /**
     * Loggeador de la clase
     */
    private static final Logger DEBUGER = Logger.getLogger(FileAudit.class);
    private OutputStream out;
    
    public FileAudit(OutputStream file){
        this.out=file;
    }
    
    public FileAudit(File file) throws FileNotFoundException{
        this.out=new FileOutputStream(file);
    }

    public FileAudit(String file) throws FileNotFoundException {
        this(new File(file));
    }
    
    
    
    
    
    // metodos publicos --------------------------------------------------------
    
    @Override
    public void writeEvent(String usuario, EnvironmentType tipoTerminal, String origen, String terminal, EventAudit evento, String detalle, Map<String, Object> data) throws WayAccessException {
        StringBuilder sb=new StringBuilder("");
        Set<String> llaves;
        
        sb.append("Usuario=").append(usuario);
        sb.append(", TipoTerminal=").append(tipoTerminal);
        sb.append(", IP=").append(origen);
        sb.append(", Terminal=").append(terminal);
        sb.append(", Evento=").append(evento);
        sb.append(", Detalle=").append(detalle);
        
        if(data!=null){
            llaves=data.keySet();
            
            for(String llave: llaves){
                sb.append(", ").append(llave).append("=").append(data.get(llave));
            }
        }
        
        try{
            this.out.write( sb.toString().getBytes() );
            this.out.flush();
        }
        catch(Exception ex){
            DEBUGER.error("No se logro escribir en la bitacora", ex);
        }
    }

    @Override
    public int getRegistries(Map<String, Object> param) throws WayAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Event> getEvents(Map<String, Object> map, int i, int i1) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
