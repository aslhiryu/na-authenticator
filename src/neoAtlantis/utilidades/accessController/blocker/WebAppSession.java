package neoAtlantis.utilidades.accessController.blocker;

import java.util.Date;
import neoAtlantis.utilidades.accessController.cipher.CipherSha1;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.entity.SimpleEntity;
import neoAtlantis.utilidades.entity.annotations.IdEntity;


/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class WebAppSession extends SimpleEntity {
    @IdEntity
    private String id;
    private User usuario;
    private String ip;
    private String terminal;
    private Date generacion;
    private Date actualizacion;

    public WebAppSession(User usuario,String ip, String terminal){
        CipherSha1 ci=new CipherSha1();

        this.usuario=usuario;
        this.ip=ip;
        this.terminal=terminal;
        this.generacion=new Date();
        this.actualizacion=new Date();
        try{
            this.id=ci.cifra(this.toString());
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the usuario
     */
    public User getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the terminal
     */
    public String getTerminal() {
        return terminal;
    }

    /**
     * @param terminal the terminal to set
     */
    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    /**
     * @return the generacion
     */
    public Date getGeneracion() {
        return generacion;
    }

    /**
     * @param generacion the generacion to set
     */
    public void setGeneracion(Date generacion) {
        this.generacion = generacion;
    }

    /**
     * @return the actualizacion
     */
    public Date getActualizacion() {
        return actualizacion;
    }

    /**
     * @param actualizacion the actualizacion to set
     */
    public void setActualizacion(Date actualizacion) {
        this.actualizacion = actualizacion;
    }
}
