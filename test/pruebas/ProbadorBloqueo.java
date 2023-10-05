/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pruebas;

import java.util.ArrayList;
import neoAtlantis.utilidades.ctrlAcceso.Usuario;
import neoAtlantis.utilidades.ctrlAcceso.recursoBloqueo.BloqueoBD;
import neoAtlantis.utilidades.debuger.ConsoleDebuger;
import neoAtlantis.utilidades.debuger.interfaces.Debuger;
import neoAtlantis.utilidades.logger.ConsoleLogger;
import neoAtlantis.utilidades.logger.interfaces.Logger;
import org.junit.Test;

/**
 *
 * @author HP
 */
public class ProbadorBloqueo {
    public static Debuger generaDebuger(){
        return new ConsoleDebuger("TEST");
    }

    public static Logger generaLoger(){
        return new ConsoleLogger("TEST");
    }

    public static Usuario generaUsuario(){
        Usuario u=new Usuario("hiryu");

        u.setNombre("Alberto");
        u.setOrigen("172.16.20.3");
        u.setTerminal("web");

        return u;
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @Test
    public void pruebaBloqueo() throws Exception{
        BloqueoBD d=new BloqueoBD("c:/tmp/mysqlFenix.xml");
        d.setMDebug( generaDebuger() );
        d.setMLog( generaLoger() );

        //System.out.println("Genera tabla b: "+ d.generaTablaBloqueos() );
        //System.out.println("Genera tabla c: "+ d.generaTablaConexion() );

        d.agregaBloqueo(generaUsuario());
    }

    @Test
    public void pruebaVerBloqueo() throws Exception{
        BloqueoBD d=new BloqueoBD("c:/tmp/mysqlFenix.xml");
        d.setMDebug( generaDebuger() );
        d.setMLog( generaLoger() );

        System.out.println("Bloq? "+d.verificaBloqueo(generaUsuario()));
    }

    @Test
    public void pruebaBloqueosTerm() throws Exception{
        BloqueoBD d=new BloqueoBD("c:/tmp/mysqlFenix.xml");
        d.setMDebug( generaDebuger() );
        d.setMLog( generaLoger() );
        d.setTiempoBloqueo(50000);

        ArrayList a=d.revisaBloqueosTerminados();

        for(int i=0; a!=null&&i<a.size(); i++){
            System.out.println("-> "+a.get(i));
        }
    }

    @Test
    public void pruebaConexion() throws Exception{
        BloqueoBD d=new BloqueoBD("c:/tmp/mysqlFenix.xml");
        d.setMDebug( generaDebuger() );
        d.setMLog( generaLoger() );

        d.agregaConexion(generaUsuario());
    }

    @Test
    public void pruebaVerConexion() throws Exception{
        BloqueoBD d=new BloqueoBD("c:/tmp/mysqlFenix.xml");
        d.setMDebug( generaDebuger() );
        d.setMLog( generaLoger() );

        System.out.println("Conx? "+d.verificaConexion(generaUsuario()));
    }

    @Test
    public void pruebaRemConexion() throws Exception{
        BloqueoBD d=new BloqueoBD("c:/tmp/mysqlFenix.xml");
        d.setMDebug( generaDebuger() );
        d.setMLog( generaLoger() );

        //d.remueveConexion(generaUsuario());
    }
}