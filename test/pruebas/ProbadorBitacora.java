/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pruebas;

import org.junit.Test;
import neoAtlantis.utilidades.ctrlAcceso.recursoBitacora.*;
import neoAtlantis.utilidades.ctrlAcceso.recursoBitacora.interfaces.EventoBitacora;

/**
 *
 * @author HP
 */
public class ProbadorBitacora {
    public static String parseaArreglo(String[] a){
        StringBuffer sb=new StringBuffer("");

        for(int i=0; a!=null&&i<a.length; i++){
            sb.append("<<").append(a[i]).append(">>");
        }

        return sb.toString();
    }


    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @Test
    public void pruebaBitacora() throws Exception{
        BitacoraBD d=new BitacoraBD("c:/tmp/mysqlFenix.xml");

        //System.out.println("Genera tabla: "+ d.generaTablaBitacora() );
        d.escribeBitacora("Hiryu", "local", "localhost", EventoBitacora.ACCESO, "Prueba de bitacora BD.");

        String[][]dat=d.generaReporte(1, 5, null);
        for(int i=0; dat!=null&&i<dat.length; i++){
            System.out.println(ProbadorBitacora.parseaArreglo(dat[i]));
        }
        System.out.println("Pos: "+d.getPosicionActual());
    }
}