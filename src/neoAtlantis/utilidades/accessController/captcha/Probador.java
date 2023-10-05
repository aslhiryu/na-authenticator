/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package neoAtlantis.utilidades.accessController.captcha;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.util.Iterator;
import javax.imageio.*;
import javax.swing.*;

/**
 *
 * @author ECONOMIA
 */
public class Probador {

    public static void main(String[] a){
        JFrame f=new JFrame("Captcha");
        JLabel l=new JLabel();
        PointLineCaptcha c=new PointLineCaptcha();
        BasicConfirmationCode cc=new BasicConfirmationCode();
        ImageIcon im=new ImageIcon(c.dibuja(cc.genera()));

        l.setIcon(im);
        f.setSize(640, 480);
        f.getContentPane().add(l);

        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*try{
            Image im=c.dibuja("ngfweriu");
            BufferedImage i=new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g2d = i.createGraphics();
            g2d.drawImage(im, new AffineTransform(), null);
            g2d.dispose();

            Iterator writers = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = (ImageWriter)writers.next();
            writer.setOutput(ImageIO.createImageOutputStream(new File("d:/tmp.jpg")));
            writer.write(i);
        }catch(Exception ex){
            ex.printStackTrace();
        }*/
    }
}
