package neoAtlantis.utilidades.accessController.captcha;

import java.util.Calendar;
import java.util.Random;
import neoAtlantis.utilidades.accessController.captcha.interfaces.ConfirmationCode;

/**
 * Generador de Codigos de Confirmaci&oacute;n base.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class BasicConfirmationCode implements ConfirmationCode {
    private int limit=3;

    /**
     * Constructoir base
     */
    public BasicConfirmationCode(){
    }

    /**
     * Constructor que permite definir el tama&ntilde;o del c&oacute;digo
     * @param limit 
     */
    public BasicConfirmationCode(int limit){
        if( limit>3){
            this.limit=limit;
        }
    }

    /**
     * Genera un c&oacute;digo de confirmaci&oacute;n.
     * @return Cadena con el c&oacute;digo
     */
    @Override
    public String genera() {
        char[] cadena=new char[this.limit];
        Random r=new Random(Calendar.getInstance().getTimeInMillis());
        int i = 0;
        char c;

        while ( i < this.limit){
            c=(char)r.nextInt(255);
            if ( (c >= '0' && c <='9') || (c >='A' && c <='Z') ){
                cadena[i]=c;
                i ++;
            }
        }

        return new String(cadena);
    }

}
