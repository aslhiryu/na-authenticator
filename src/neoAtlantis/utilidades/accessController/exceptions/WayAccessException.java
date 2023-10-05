package neoAtlantis.utilidades.accessController.exceptions;

/**
 * Excepci&oacute;n generada los diferentes medios ({@link neoAtlantis.utilidades.accessController.allower.interfaces.AllowerWay Permisor}, 
 * {@link neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay Auditor},
 * {@link neoAtlantis.utilidades.accessController.authentication.interfaces.AuthenticationWay Autenticador},
 * {@link neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay Bloqueador},
 * {@link neoAtlantis.utilidades.accessController.profiler.interfaces.ProfilerWay Perfilador},
 * {@link neoAtlantis.utilidades.accessController.scheduler.interfaces.SchedulerWay Calendarizador}) 
 * al momento de intentar acceder a su informaci&oacute;n.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class WayAccessException extends RuntimeException {
    /**
     * Constructor base.
     * @param ex Excepci&oacute;n padre
     */
    public WayAccessException(Exception ex){
        super(ex);
    }    
}
