package neoAtlantis.utilidades.accessController.captcha.interfaces;

import java.awt.Image;

/**
 * Interface que define el comportamiento con el que debe contar un Dibujador de 
 * Captchas.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public interface CaptchaPainter {
    /**
     * Definici&oacute;n del metodo que pinta un captcha.
     * @param cadena Cadena con la que se genera el captcha
     * @return Imagen con el captcha
     */
    public Image dibuja(String cadena);
}
