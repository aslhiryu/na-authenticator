package neoAtlantis.utilidades.accessController.objects;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import neoAtlantis.utilidades.accessController.cipher.CipherSha1;

/**
 * Representaci�n de un sesi�n de acceso
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class AccessSession {
    private String id;
    private Date actividad;
    private Map atributos;
    
    /**
     * Genera una sesi�n de acceso
     * @param u Usuario del que se genera la sesi�n
     */
    public AccessSession(User u){
        this.atributos=new HashMap<String,Object>();
        this.actividad=new Date();
        generaId(u);
    }
    
    /**
     * Recupera el identificador de la sesi�n
     * @return Identificador de la sesi�n
     */
    public String getId(){
        return this.id;
    }
    
    /**
     * Agrega un atributo a la sesi�n
     * @param clave Identificador del atributo
     * @param valor Atributo
     */
    public void setAtributo(String clave, Object valor){
        this.atributos.put(clave, valor);
    }
    
    /**
     * Recupera un atributo de la sesi�n
     * @param clave Identificador del atributo
     * @return 
     */
    public Object getAtributo(String clave){
        return this.atributos.get(clave);
    }
    
    /**
     * Genera la informac&oacute;n de la sesi�n.
     * @return Informaci&oacute;n de la sesi�n
     */
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();

        sb.append("/*********************  AccessSession  ********************//").append(System.getProperty("line.separator"));
        sb.append("Session: ").append(this.id).append(System.getProperty("line.separator"));
        sb.append("Actividad: ").append(this.actividad).append(System.getProperty("line.separator"));
        Set<String> cls=this.atributos.keySet();
        sb.append("Atributos: ");
        for(String c: cls){
            sb.append("[").append(this.atributos.get(c).getClass().getName()).append("]").append(c).append(":").append(this.atributos.get(c)).append(",");
        }
        sb.append(System.getProperty("line.separator"));
        sb.append("/****************************************************//").append(System.getProperty("line.separator"));

        return sb.toString();
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * Genera la clave d ela sesi�n
     * @param u Usuario del que se genera la sesi�n
     */
    protected void generaId(User u){
        CipherSha1 c=new CipherSha1();
        StringBuilder sb=new StringBuilder(u.getId());
        
        sb.append("|").append(u.getNombre()).append("|").append(u.getOrigen()).append("|").
                append(u.getUser()).append("|").append(u.getGeneracion());
        
        try{
            this.id=c.cifra(sb.toString());
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return the actividad
     */
    public Date getActividad() {
        return actividad;
    }

    /**
     * @param actividad the actividad to set
     */
    public void setActividad(Date actividad) {
        this.actividad = actividad;
    }
}
