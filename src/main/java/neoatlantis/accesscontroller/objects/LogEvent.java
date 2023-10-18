package neoatlantis.accesscontroller.objects;

import neoatlantis.accesscontroller.audit.interfaces.EventAudit;
import neoatlantis.entity.Event;

/**
 * Definici&oacute;n de un evento en la bitacora.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class LogEvent extends Event {
    private String usuario;
    private EnvironmentType tipoTerminal;
    private String terminal;
    private EventAudit evento;

    /**
     * Recupera el usuario.
     * @return Usuario del evento
     */
    public String getUserId() {
        return usuario;
    }

    /**
     * Asigna el usuario.
     * @param usuario Usuario que gener&oacute; el evento
     */
    public void setUserId(String usuario) {
        this.usuario = usuario;
    }

    /**
     * Recupera el tipo de terminal.
     * @return Tipo de terminal del evento
     */
    public EnvironmentType getTerminalType() {
        return tipoTerminal;
    }

    /**
     * Asigna el tipo de terminal.
     * @param tipoTerminal Tipo de terminal desde donde se gener&oacute; el evento
     */
    public void setTerminalType(EnvironmentType tipoTerminal) {
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
    public EventAudit getEvent() {
        return evento;
    }

    /**
     * Asigna el tipo de evento.
     * @param evento Tipo de evento generado
     */
    public void setEvent(EventAudit evento) {
        this.evento = evento;
    }

     /**
     * Genera la informac&oacute;n del evento.
     * @return Informaci&oacute;n del evento
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");

        sb.append("/*********************  ").append(this.getClass()).append("  ********************//").append(System.getProperty("line.separator"));
        sb.append("Usuario: ").append(this.getUserId()).append(System.getProperty("line.separator"));
        sb.append("Origen: ").append(this.getOrigin()).append(System.getProperty("line.separator"));
        sb.append("Terminal: ").append(this.getTerminal()).append(System.getProperty("line.separator"));
        sb.append("Tipo de Terminal: ").append(this.getTerminalType()).append(System.getProperty("line.separator"));
        sb.append("Fecha: ").append(this.getEventDate()).append(System.getProperty("line.separator"));
        sb.append("Evento: ").append(this.getEvent()).append(System.getProperty("line.separator"));
        sb.append("Detalle: ").append(this.getDetail()).append(System.getProperty("line.separator"));
        sb.append("/***************************************************//").append(System.getProperty("line.separator"));

        return sb.toString();
    }   
}
