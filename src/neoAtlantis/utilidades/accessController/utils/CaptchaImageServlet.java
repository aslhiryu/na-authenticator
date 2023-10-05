package neoAtlantis.utilidades.accessController.utils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import javax.imageio.*;
import javax.servlet.http.*;
import neoAtlantis.utilidades.accessController.captcha.interfaces.CaptchaPainter;
import neoAtlantis.utilidades.accessController.captcha.interfaces.ConfirmationCode;
import neoAtlantis.utilidades.accessController.objects.PeticionesCaptcha;
import org.apache.log4j.Logger;

/**
 * Servlet que apoya para obtener un archivo de imagen JPEG, apoyendose en el 
 * {@link neoAtlantis.utilidades.accessController.captcha.interfaces.CaptchaPainter Dibujardor de Captchas}
 * que se tenga configurado a partir del {@link neoAtlantis.utilidades.accessController.utils.AccessControllerPublisher}.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class CaptchaImageServlet extends HttpServlet {
    /**
     * Logeador de la clase
     */
    static final Logger logger = Logger.getLogger(CaptchaImageServlet.class);

    private ConfirmationCode codigo;
    private CaptchaPainter captcha;
    private PeticionesCaptcha peticiones;

    /**
     * M&eacuet;todo que inicializa al servlet, dentro del cual se genera el entorno para 
     * poder desplegar los captchas.
     */
    @Override
    public void init(){
        if( this.getServletContext().getAttribute(AccessControllerPublisher.CLAVE_CAPTCHA)!=null ){
            this.captcha=(CaptchaPainter)this.getServletContext().getAttribute(AccessControllerPublisher.CLAVE_CAPTCHA);
        }
        else{
            throw new RuntimeException("No se tiene definido un 'CaptchaPainter'.");
        }
        if( this.getServletContext().getAttribute(AccessControllerPublisher.CLAVE_CODE)!=null ){
            this.codigo=(ConfirmationCode)this.getServletContext().getAttribute(AccessControllerPublisher.CLAVE_CODE);
        }
        else{
            throw new RuntimeException("No se tiene definido un 'ConfirmationCode'.");
        }
        if( this.getServletContext().getAttribute(AccessControllerPublisher.CLAVE_PETITIONS)!=null ){
            this.peticiones=(PeticionesCaptcha)this.getServletContext().getAttribute(AccessControllerPublisher.CLAVE_PETITIONS);
        }
        else{
            throw new RuntimeException("No se ha inicializado el componente de peticiones de captcha.");
        }
    }

    /**
     * M&eacuet;todo que se ejecuta cuando se invoca al servlet de la forma GET.
     * @param request Petici´&oacute;n de la p&aacute;gina
     * @param response Respuesta de la  p&aacute;gina
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        this.doPost(request, response);
    }

    /**
     * M&eacuet;todo que se ejecuta cuando se invoca al servlet de la forma POST.
     * @param request Petici´&oacute;n de la p&aacute;gina
     * @param response  Respuesta de la  p&aacute;gina
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response){
        String cod=this.codigo.genera();
        Image im=this.captcha.dibuja(cod);
        BufferedImage i=new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = i.createGraphics();

        g2d.drawImage(im, new AffineTransform(), null);
        g2d.dispose();
        response.setContentType("image/jpeg");

        try{
            Iterator writers = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = (ImageWriter)writers.next();

            writer.setOutput(ImageIO.createImageOutputStream(response.getOutputStream()));
            writer.write(i);
            response.getOutputStream().flush();
        }
        catch(Exception ex){
            logger.error("Error al generar la imagen", ex);
        }

        //genero la peticion
        logger.debug("Almaceno la petición del captcha.");
        this.peticiones.agregaPeticion(request.getRemoteAddr(), request.getRemoteHost(), cod);
    }
}
