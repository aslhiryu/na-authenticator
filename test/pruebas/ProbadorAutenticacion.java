/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pruebas;

import neoAtlantis.utilidades.ctrlAcceso.Usuario;
import neoAtlantis.utilidades.ctrlAcceso.cipher.CifradorMd5Des;
import neoAtlantis.utilidades.ctrlAcceso.recursoAutenticacion.AutenticadorBD;
import neoAtlantis.utilidades.debuger.ConsoleDebuger;
import neoAtlantis.utilidades.debuger.interfaces.Debuger;
import neoAtlantis.utilidades.logger.ConsoleLogger;
import neoAtlantis.utilidades.logger.interfaces.Logger;
import org.junit.Test;

/**
 *
 * @author HP
 */
public class ProbadorAutenticacion {

    public static Debuger generaDebuger(){
        return new ConsoleDebuger("TEST");
    }

    public static Logger generaLoger(){
        return new ConsoleLogger("TEST");
    }

    public static Usuario generaUsuario(){
        Usuario u=new Usuario("hiryu");

        u.setNombre("Alberto");

        return u;
    }

    public static CifradorMd5Des generaCifrador(){
        return new CifradorMd5Des("bluemary");
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}


    @Test
    public void pruebaGenCuenta() throws Exception{
        AutenticadorBD d=new AutenticadorBD("c:/tmp/mysqlFenix.xml");
        d.setMDebug( generaDebuger() );
        d.setMLog( generaLoger() );

        d.agregaCuenta(generaUsuario(), "mercury");
    }

    @Test
    public void pruebaGenCuentaTmp() throws Exception{
        AutenticadorBD d=new AutenticadorBD("c:/tmp/mysqlFenix.xml");
        d.setMDebug( generaDebuger() );
        d.setMLog( generaLoger() );
        Usuario u=generaUsuario();
        u.setUser("userTmp");

        d.agregaCuentaTemporal(u);
    }

    @Test
    public void pruebaModificaPass() throws Exception{
        AutenticadorBD d=new AutenticadorBD("c:/tmp/mysqlFenix.xml");
        d.setMDebug( generaDebuger() );
        d.setMLog( generaLoger() );
        Usuario u=generaUsuario();

        d.modificaContrasena(u, "mercury");
    }

    @Test
    public void pruebaAut() throws Exception{
        AutenticadorBD d=new AutenticadorBD("c:/tmp/mysqlFenix.xml");
        d.setMDebug( generaDebuger() );
        d.setMLog( generaLoger() );

        //System.out.println("Genera tabla: "+ d.generaTablaUsuario() );
        System.out.println("Val: "+ d.validaUsuario("userTmp", "mercury") );
        System.out.println("Val: "+ d.validaUsuario("hiryu", generaCifrador().cifra("mercury")) );

        /*String[][]dat=d.generaReporte(1, 5, null);
        for(int i=0; dat!=null&&i<dat.length; i++){
            System.out.println(ProbadorBitacora.parseaArreglo(dat[i]));
        }
        System.out.println("Pos: "+d.getPosicionActual());*/
    }
}