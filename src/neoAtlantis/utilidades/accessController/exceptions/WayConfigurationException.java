package neoAtlantis.utilidades.accessController.exceptions;

/**
 * Excepci&oacute;n generada los diferentes medios ({@link neoAtlantis.utilidades.accessController.allower.interfaces.AllowerWay Permisor}, 
 * {@link neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay Auditor},
 * {@link neoAtlantis.utilidades.accessController.authentication.interfaces.AuthenticationWay Autenticador},
 * {@link neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay Bloqueador},
 * {@link neoAtlantis.utilidades.accessController.profiler.interfaces.ProfilerWay Perfilador},
 * {@link neoAtlantis.utilidades.accessController.scheduler.interfaces.SchedulerWay Calendarizador}) 
 * al cargar su configuraci&oacute;n.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class WayConfigurationException extends Exception {
    /**
     * Constructor base.
     * @param ex Excepci&oacute;n padre
     */
    public WayConfigurationException(Exception ex){
        super(ex);
    }
    
    /**
     * Constructor base.
     * @param msg  Mensage de error
     */
    public WayConfigurationException(String msg){
        super(msg);
    }
}
