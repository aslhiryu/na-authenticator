package neoAtlantis.utilidades.ctrlAcceso.utils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Generador de imagenes de claves de confirmaci&oacute;n, las cuales se utilizan para prevenir los robots de descubrimiento de contrase&ntilde;as.
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class ClaveConfirmacion {
  /**
   * Metodo para genera una imagen de una clave de confirmación
   * @param clave Clave de confirmación
   * @return Imagen con la clave
   */
  public static BufferedImage generaImagenConfirmacion(String clave){
      return generaImagenConfirmacion(clave, 40, 35, new Color(120, 120, 120), new Color(255, 255, 204), Color.RED);
  }

  /**
   * Metodo para genera una imagen de una clave de confirmación
   * @param clave Clave de confirmación
   * @param sepCaracter Separación entre caracteres
   * @param tamCaracter Tamaño de los caracteres
   * @param colFuente Color para la fuente
   * @param fondo Color de fondo
   * @param frente Color de los objetos del fondo
   * @return Imagen con la clave
   */
  public static BufferedImage generaImagenConfirmacion(String clave, int sepCaracter, int tamCaracter, Color colFuente, Color fondo, Color frente){
    BufferedImage image=new BufferedImage(clave.length()*sepCaracter, tamCaracter+5, BufferedImage.TYPE_INT_RGB);
    Graphics grafico=image.getGraphics();
    Color[] colores=new Color[]{Color.BLUE, Color.BLACK, Color.RED, Color.DARK_GRAY, Color.GREEN, Color.MAGENTA, Color.ORANGE, new Color(50, 153, 10)};
    Font fuente=new Font("Dialog", 1, tamCaracter);
    AffineTransform trans=new AffineTransform();
    int numRayas=10;

    //modifico el fondo de la imagen
    grafico.setColor( fondo );
    grafico.fillRect(0, 0, image.getWidth(), image.getHeight());
    //genero puntos en el fondo
    grafico.setColor( frente );
    for(int i=0; i<image.getWidth(); i+=5){
      for(int j=0; j<image.getHeight(); j+=5){
            grafico.fillRect(i, j, 1, 1);
      }
    }
    //genero lineas en el fondo
    int x, y, r, ai, af, c;
    for(int i=0; i<(clave.length()*numRayas); i++){
      x=(int)(Math.random()*image.getWidth());
      y=(int)(Math.random()*image.getHeight());
      r=(int)(Math.random()*image.getHeight());
      ai=(int)(Math.random()*360);
      af=(int)(Math.random()*360);
      c=(int)(Math.random()*8);

      grafico.setColor( colores[c] );
      grafico.drawArc(x, y, r, r, ai, af);
    }

    grafico.setColor( colFuente );
    double rad=0;
    int ang, neg;
    for(int i=0; i<clave.length(); i++){
      //calculo el angulo de deformacion del caracter
      ang=(int)(Math.random()*45+1);
      neg=(int)(Math.random()*100+1);
      rad=ang*Math.PI/180.0;
      if( neg%2==0 )
        rad=rad*-1;

      //dibujo los caracteres
      trans.setToRotation( rad );
      grafico.setFont( fuente.deriveFont(trans) );
      grafico.drawString(""+clave.charAt(i), (i*sepCaracter)+10, tamCaracter-5);
    }

    return image;
  }

}
