package neoAtlantis.utilidades.ctrlAcceso.recursoBitacora.interfaces;

import neoAtlantis.utilidades.interfaces.EventRegister;


/**
 * Interface que define el comportamiento que debe de tener una <i>RecursoBitacorador</i>.
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public abstract class RecursoBitacorador extends EventRegister {
    /**
     * Versi&oacute;n de la clase.
     */
    public static final String VERSION="1.0";

    /**
     * Definici&oacute;n del metodo para escribir en la bitacora.
     * @param usuario Usuario del que se desea registrar el evento
     * @param terminal Terminal desde donde se realiza el evento
     * @param origen Equipo desde donde se realiza el evento
     * @param evento Evento que se registra:
     * @param detalle Detalle del evento
     * @throws java.lang.Exception
     */
    public abstract void escribeBitacora(String usuario, String terminal, String origen, EventoBitacora evento, String detalle) throws Exception;
}
