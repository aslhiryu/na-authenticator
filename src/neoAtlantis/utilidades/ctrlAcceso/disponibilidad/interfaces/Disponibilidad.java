package neoAtlantis.utilidades.ctrlAcceso.disponibilidad.interfaces;

import neoAtlantis.utilidades.debuger.interfaces.Debuger;
import neoAtlantis.utilidades.logger.interfaces.Logger;

/**
 * Interface que define el comportamiento que debe de tener un <i>Calendario de Disponibilidad para los Accesos</i>.
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public abstract class Disponibilidad {
    /**
     * Versi&oacute;n de la clase.
     */
    public static final String VERSION="1.0";
    /**
     * Debuger que da seguimiento a los procesos que realiza la clase.
     */
    protected Debuger mDebug;
    /**
     * Loger que registra los errores que se probocan en la clase.
     */
    protected Logger mLog;

    /**
     * Definici&oacute;n del metodo para revisar la disponibilidad.
     * @return true si es disponible el acceso
     */
    public abstract boolean existeDisponibilidad();

    /**
     * Asigna un Debuger a la clase para poder dar seguimiento a los procesos que realiza la clase.
     * @param mDebug the mDebug to set
     */
    public void setMDebug(Debuger mDebug) {
        this.mDebug = mDebug;
    }

    /**
     * Asigna un Loger a la clase para poder registrar los errores que se proboquen en la clase.
     * @param mLog the mLog to set
     */
    public void setMLog(Logger mLog) {
        this.mLog = mLog;
    }

}