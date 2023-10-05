package neoAtlantis.utilidades.accessController.captcha;

import java.awt.*;
import java.awt.font.*  ;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Calendar;
import java.util.Random;
import neoAtlantis.utilidades.accessController.captcha.interfaces.CaptchaPainter;

/**
 * Dibujador de Captchas que realiza la imegen con lineas y puntos.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class PointLineCaptcha implements CaptchaPainter {
    private float[] rads=new float[]{0, 0.45f, -0.45f, 0.63f, -0.63f};

    /**
     * Pinta el captcha.
     * @param cadena Cadena con la que se desea se genere el captcha
     * @return Imagen con el captcha
     */
    @Override
    public Image dibuja(String cadena) {
        char[] letras=cadena.toCharArray();
        AffineTransform trans;
        Random r=new Random(Calendar.getInstance().getTimeInMillis());

        //define la fuente a utilizar
        Font f=new Font("SansSerif", Font.BOLD, 80);

        //calculo el espacio por letra
        Rectangle2D rec=f.getMaxCharBounds(new FontRenderContext(new AffineTransform(), true, true));
        int ancho=(int)((letras.length+2)*(rec.getWidth()/3));
        int alto=(int)(rec.getHeight()+(rec.getHeight()/2));
//        System.out.println("w:"+rec.getWidth()+", h:"+rec.getHeight()+", al:"+alto+", an:"+ancho);

        //defino la imagen
        BufferedImage image=new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics g=image.createGraphics();

        //dibujo el fondo
        g.setColor(new Color(255, 255, 176));
        g.fillRect(0, 0, ancho, alto);

        //dibuja unas lineas
        g.setColor(new Color(50, 50, 50));
        for(int i=0; i<(letras.length*20); i++){
            g.drawLine(r.nextInt(ancho), r.nextInt(alto), r.nextInt(ancho), r.nextInt(alto));
        }

        //dibujo las letras
        g.setColor(Color.BLACK);
        for(int i=0; i<letras.length; i++){
            trans=new AffineTransform();
            //trans.setToRotation(r.nextFloat());
            trans.setToRotation(this.rads[ Math.abs(r.nextInt()%this.rads.length) ]);
            g.setFont(f.deriveFont(trans));

            //g.drawString(""+letras[i], (int)(rec.getWidth()+(i*rec.getWidth()/2)), (int)rec.getHeight());
            g.drawString(""+letras[i], (int)(ancho/(letras.length+2)*(i+1)), (int)rec.getHeight());
        }

        //finalizo la imagen
        g.dispose();

        return image;
    }

}
