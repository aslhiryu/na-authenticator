package neoAtlantis.utilidades.accessController.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import neoAtlantis.utilidades.accessController.audit.interfaces.EventAudit;
import neoAtlantis.utilidades.accessController.audit.interfaces.AuditWay;
import neoAtlantis.utilidades.accessController.exceptions.WayAccessException;
import neoAtlantis.utilidades.accessController.objects.EnvironmentType;
import neoAtlantis.utilidades.objects.Event;

/**
 * Medio Bitacorizador nulo, utilizado para nulificar las operiones desencadenadas al 
 * medio por el {@link neoAtlantis.utilidades.accessController.AccessController Control de Autenticaci&oacute;n}
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.0
 */
public class NullAudit extends AuditWay {

    /**
     * Escribe un registro en la bitacora.
     * @param usuario Usuario del que se desea registrar el evento
     * @param terminal Terminal desde donde se realiza el evento
     * @param origen Equipo desde donde se realiza el evento
     * @param evento Evento que se registra:
     * @param detalle Detalle del evento
     * @throws java.lang.Exception
     */
    @Override
    public void escribeBitacora(String usuario, EnvironmentType tipoTerminal, String origen, String terminal, EventAudit evento, String detalle) throws WayAccessException {

    }

    /**
     * Obtiene registros de la bitacora.
     * @param param Filtros que se desean aplicar para obtener los registros
     * @param regs Numero de registro que se desean obtener
     * @param offset Registro desde donde se desea obtener la informaci&oacute;n
     * @return Registros obtenidos
     * @throws java.lang.Exception
     */
    @Override
    public List<Event> recuperaBitacora(Map<String,Object> param, int regs, int offset) throws WayAccessException{
        return new ArrayList<Event>();
    }

}
