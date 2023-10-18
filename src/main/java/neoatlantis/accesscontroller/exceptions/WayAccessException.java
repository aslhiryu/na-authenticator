package neoatlantis.accesscontroller.exceptions;

/**
 * Excepci&oacute;n generada los diferentes medios ({@link neoatlantis.utilidades.accesscontroller.allower.interfaces.AllowerWay Permisor}, 
 * {@link neoatlantis.utilidades.accesscontroller.audit.interfaces.AuditWay Auditor},
 * {@link neoatlantis.utilidades.accesscontroller.authentication.interfaces.AuthenticationWay Autenticador},
 * {@link neoatlantis.utilidades.accesscontroller.blocker.interfaces.BlockerWay Bloqueador},
 * {@link neoatlantis.utilidades.accesscontroller.profiler.interfaces.ProfilerWay Perfilador},
 * {@link neoatlantis.utilidades.accesscontroller.scheduler.interfaces.SchedulerWay Calendarizador}) 
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

    public WayAccessException(String ex){
        super(ex);
    }    
}
