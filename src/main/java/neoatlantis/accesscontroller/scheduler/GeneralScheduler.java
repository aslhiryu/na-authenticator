package neoatlantis.accesscontroller.scheduler;

import neoatlantis.accesscontroller.scheduler.interfaces.SchedulerWay;

/**
 * Calendario de disponibilidad con disponibilidad total
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class GeneralScheduler extends SchedulerWay {

    /**
     * Revisa la disponibilidad.
     * @return true si es disponible el acceso
     */
    @Override
    public boolean availability() {
        return true;
    }

}
