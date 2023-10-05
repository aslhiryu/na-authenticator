package neoAtlantis.utilidades.accessController.objects;

import neoAtlantis.utilidades.entity.SimpleEntity;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class PeticionCaptcha extends SimpleEntity {
    private String ip;
    private String terminal;
    private String codigo;

    public PeticionCaptcha(String ip, String terminal, String codigo){
        this.ip=ip;
        this.terminal=terminal;
        this.codigo=codigo;
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
     * @return the codigo
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * @param codigo the codigo to set
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

}
