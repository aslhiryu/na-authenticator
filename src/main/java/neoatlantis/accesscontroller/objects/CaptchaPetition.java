package neoAtlantis.utils.accessController.objects;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class CaptchaPetition {
    private String ip;
    private String terminal;
    private String codigo;

    public CaptchaPetition(String ip, String terminal, String codigo){
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

    /**
     * Genera la informac&oacute;n del usuario.
     * @return Informaci&oacute;n del usuario
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");

        sb.append("/*********************  PETICION CAPTCHA  ********************//").append(System.getProperty("line.separator"));
        sb.append("IP: ").append(this.ip).append(System.getProperty("line.separator"));
        sb.append("Terminal: ").append(this.terminal).append(System.getProperty("line.separator"));
        sb.append("Captcha: ").append(this.codigo).append(System.getProperty("line.separator"));
        sb.append("/*************************************************************//").append(System.getProperty("line.separator"));
        
        return sb.toString();
    }
}
