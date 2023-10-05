package neoAtlantis.utilidades.accessController.audit;

import neoAtlantis.utilidades.accessController.audit.interfaces.EventAudit;
import neoAtlantis.utilidades.accessController.objects.EnvironmentType;
import neoAtlantis.utilidades.objects.Event;

/**
 * Definici&oacute;n de un evento en la bitacora.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class AuditEvent extends Event {
    private String usuario;
    private EnvironmentType tipoTerminal;
    private String terminal;
    private EventAudit evento;

    /**
     * Recupera el usuario.
     * @return Usuario del evento
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Asigna el usuario.
     * @param usuario Usuario que gener&oacute; el evento
     */
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    /**
     * Recupera el tipo de terminal.
     * @return Tipo de terminal del evento
     */
    public EnvironmentType getTipoTerminal() {
        return tipoTerminal;
    }

    /**
     * Asigna el tipo de terminal.
     * @param tipoTerminal Tipo de terminal desde donde se gener&oacute; el evento
     */
    public void setTipoTerminal(EnvironmentType tipoTerminal) {
        this.tipoTerminal = tipoTerminal;
    }

    /**
     * Recupera la terminal.
     * @return Terminal del evento
     */
    public String getTerminal() {
        return terminal;
    }

    /**
     * Asigna la terminal.
     * @param terminal Terminal desde donde se gener&oacute; el evento
     */
    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    /**
     * Recupera el tipo de evento.
     * @return Tipo de evento registrado
     */
    public EventAudit getEvento() {
        return evento;
    }

    /**
     * Asigna el tipo de evento.
     * @param evento Tipo de evento generado
     */
    public void setEvento(EventAudit evento) {
        this.evento = evento;
    }

}
