/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pruebas;

import neoAtlantis.utilidades.ctrlAcceso.disponibilidad.DisponibilidadDiaHabil;
import org.junit.Test;

/**
 *
 * @author HP
 */
public class ProbadorDisponibilidad {

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @Test
    public void pruebaDisponibilidadHabil(){
        DisponibilidadDiaHabil d=new DisponibilidadDiaHabil();

        System.out.println("Disponible: "+d.existeDisponibilidad());
    }

}