package neoatlantis.accesscontroller.exceptions;

/**
 * Excepci&oacute;n generada los diferentes medios ({@link neoatlantis.utilidades.accesscontroller.allower.interfaces.AllowerWay Permisor}, 
 * {@link neoatlantis.utilidades.accesscontroller.audit.interfaces.AuditWay Auditor},
 * {@link neoatlantis.utilidades.accesscontroller.authentication.interfaces.AuthenticationWay Autenticador},
 * {@link neoatlantis.utilidades.accesscontroller.blocker.interfaces.BlockerWay Bloqueador},
 * {@link neoatlantis.utilidades.accesscontroller.profiler.interfaces.ProfilerWay Perfilador},
 * {@link neoatlantis.utilidades.accesscontroller.scheduler.interfaces.SchedulerWay Calendarizador}) 
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
