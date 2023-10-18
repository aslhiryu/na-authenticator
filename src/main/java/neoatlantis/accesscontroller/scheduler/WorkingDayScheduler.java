package neoAtlantis.utils.accessController.scheduler;

import java.util.Calendar;
import neoAtlantis.utils.accessController.scheduler.interfaces.SchedulerWay;

/**
 * Calendario de disponibilidad que se basa en el horario habil
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class WorkingDayScheduler extends SchedulerWay {
    private int inicio;
    private int fin;

    public WorkingDayScheduler(){
        this(9, 18);
    }
    
    public WorkingDayScheduler(int inicio, int fin){
        this.inicio=inicio;
        this.fin=fin;
    }
    
    /**
     * Revisa la disponibilidad.
     * @return true si es disponible el acceso
     */
    @Override
    public boolean availability() {
        //valida los dias habiles
        if( Calendar.getInstance().get(Calendar.DAY_OF_WEEK)>=Calendar.MONDAY && Calendar.getInstance().get(Calendar.DAY_OF_WEEK)<=Calendar.FRIDAY ){
            //revisa los horarios habiles
            if( Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>=this.inicio && Calendar.getInstance().get(Calendar.HOUR_OF_DAY)<=this.fin ){
                return true;
            }
        }

        return false;
    }

}
