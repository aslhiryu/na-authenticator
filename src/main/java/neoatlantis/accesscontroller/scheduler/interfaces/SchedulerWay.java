package neoatlantis.accesscontroller.scheduler.interfaces;

import neoatlantis.accesscontroller.exceptions.WayAccessException;

/**
 * Interface que define el comportamiento con el que debe contar un Medio Calendarizador.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public abstract class SchedulerWay {
    /**
     * Definici&oacute;n del metodo para revisar la disponibilidad.
     * @return true si es disponible el acceso
     */
    public abstract boolean availability() throws WayAccessException;

}
