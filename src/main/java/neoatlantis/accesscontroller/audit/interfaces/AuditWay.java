package neoAtlantis.utils.accessController.audit.interfaces;

import java.util.Map;
import neoAtlantis.utils.accessController.exceptions.WayAccessException;
import neoAtlantis.utils.accessController.objects.EnvironmentType;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.objects.interfaces.EventRegister;

/**
 * Interface que define el comportamiento con el que debe contar un Medio Auditor
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.0
 */
public abstract class AuditWay extends EventRegister {
    private LevelAudit nivelBitacora=LevelAudit.ACCESS;

    public static final String ID_FIELD="ID";
    public static final String USER_FIELD="USER";
    public static final String ORIGIN_FIELD="ORIGIN";
    public static final String DATE_FIELD="DATE";
    public static final String EVENT_FIELD="EVENT";
    public static final String DETAIL_FIELD="DETAIL";
    public static final String TERMINAL_FIELD="TERMINAL";
    public static final String TERMINAL_TYPE_FIELD="TERMINAL_TYPE";
    

    public static final String INITIAL_DATE_PARAM="fecIni";
    public static final String END_DATE_PARAM="fecFin";
    public static final String ORDER_PARAM="orden";
    public static final String ORDER_TYPE_PARAM="tipoOrden";
    
    /**
     * Definici&oacute;n del metodo para escribir en la bitacora.
     * @param usuario Usuario del que se desea registrar el evento
     * @param terminal Terminal desde donde se realiza el evento
     * @param origen Equipo desde donde se realiza el evento
     * @param evento Evento que se registra:
     * @param detalle Detalle del evento
     * @throws java.lang.Exception
     */
    public abstract void writeEvent(String usuario, EnvironmentType tipoTerminal, String origen, String terminal, EventAudit evento, String detalle,Map<String,Object> data ) throws WayAccessException;

    public abstract int getRegistries(Map<String, Object> param) throws WayAccessException;
    
    
    
    // metodos publicos --------------------------------------------------------
    
    /**
     * Escribe un registro en la bitacora.
     * @param user Usuario que genera el evento
     * @param evento Evento acontecido
     * @param detalle Detalle del evento
     * @throws WayAccessException 
     */
    public void writeEvent(User user, EventAudit evento, String detalle) throws WayAccessException{
        this.writeEvent(user, evento, detalle, null);
    }
    
    /**
     * Escribe un registro en la bitacora.
     * @param user Usuario que genera el evento
     * @param evento Evento acontecido
     * @param detalle Detalle del evento
     * @param  data Informacion adicional
     * @throws WayAccessException 
     */
    public void writeEvent(User user, EventAudit evento, String detalle, Map<String,Object> data) throws WayAccessException{
        if( evento==EventAudit.ENTRY || evento==EventAudit.EXIT ){
            switch(getLevelAudit()){
                case FULL:
                case ADMIN:
                case BUSINESS:
                case BASIC:
                case ACCESS:{
                    this.writeEvent(user.getId(), user.getEnvironmentType(), user.getOrigin(), user.getTerminal(), evento, detalle, data);
                }
            }
        }                
        else if( evento==EventAudit.DENIED || evento==EventAudit.BLOCKED || evento==EventAudit.LOGGED ){
            switch(getLevelAudit()){
                case FULL:
                case ADMIN:
                case BUSINESS:
                case BASIC:{
                    this.writeEvent(user.getId(), user.getEnvironmentType(), user.getOrigin(), user.getTerminal(), evento, detalle, data);
                }
            }
        }                
        else if( evento==EventAudit.BUSSINESS ){
            switch(getLevelAudit()){
                case FULL:
                case ADMIN:
                case BUSINESS:{
                    this.writeEvent(user.getId(), user.getEnvironmentType(), user.getOrigin(), user.getTerminal(), evento, detalle, data);
                }
            }
        }                
        else if( evento==EventAudit.ADMIN ){
            switch(getLevelAudit()){
                case ADMIN:
                case FULL:{
                    this.writeEvent(user.getId(), user.getEnvironmentType(), user.getOrigin(), user.getTerminal(), evento, detalle, data);
                }
            }
        }                
        else if(  evento==EventAudit.RESOURCE_ACCESS ){
            switch(getLevelAudit()){
                case FULL:{
                    this.writeEvent(user.getId(), user.getEnvironmentType(), user.getOrigin(), user.getTerminal(), evento, detalle, data);
                }
            }
        }                
    }

    /**
     * Recupera el nivel de bitacoreo asignado
     * @return the nivelBitacora
     */
    public LevelAudit getLevelAudit() {
        return nivelBitacora;
    }

    /**
     * Asigna el nivel de bitacoreo
     * @param nivelBitacora the nivelBitacora to set
     */
    public void setLevelAudit(LevelAudit nivelBitacora) {
        this.nivelBitacora = nivelBitacora;
    }
    

    
    
    
    // metodos estaticos publicos ----------------------------------------------
    
    public static EventAudit getEventAuditFromString(String cad){
        if( cad!=null && cad.equalsIgnoreCase("r")){
            return EventAudit.DENIED;
        }
        else if( cad!=null && cad.equalsIgnoreCase("a")){
            return EventAudit.RESOURCE_ACCESS;
        }
        else if( cad!=null && cad.equalsIgnoreCase("b")){
            return EventAudit.BLOCKED;
        }
        else if( cad!=null && cad.equalsIgnoreCase("o")){
            return EventAudit.EXIT;
        }
        else if( cad!=null && cad.equalsIgnoreCase("i")){
            return EventAudit.ENTRY;
        }
        else if( cad!=null && cad.equalsIgnoreCase("n")){
            return EventAudit.BUSSINESS;
        }
        else if( cad!=null && cad.equalsIgnoreCase("d")){
            return EventAudit.ADMIN;
        }
        else{
            return EventAudit.LOGGED;
        }                        
    }
    
    public static EnvironmentType getEnvironmentTypeFromString(String cad){
        if( cad!=null && cad.equalsIgnoreCase("web")){
            return EnvironmentType.WEB;
        }
        else if( cad!=null && cad.equalsIgnoreCase("movil")){
            return EnvironmentType.MOVIL;
        }
        else{
            return EnvironmentType.STANDALONE;
        }                
    }

}
