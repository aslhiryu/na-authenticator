package neoAtlantis.utilidades.ctrlAcceso.disponibilidad;

import java.util.Calendar;
import neoAtlantis.utilidades.ctrlAcceso.disponibilidad.interfaces.Disponibilidad;

/**
 * Calendario de disponibilidad que se basa en el horario habil
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class DisponibilidadDiaHabil extends Disponibilidad {

    /**
     * Revisa la disponibilidad.
     * @return true si es disponible el acceso
     */
    public boolean existeDisponibilidad() {
        //valida los dias habiles
        if( Calendar.getInstance().get(Calendar.DAY_OF_WEEK)>=Calendar.MONDAY && Calendar.getInstance().get(Calendar.DAY_OF_WEEK)<=Calendar.FRIDAY ){
            //revisa los horarios habiles
            if( Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>=9 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY)<=17 ){
                return true;
            }
        }

        return false;
    }

}
