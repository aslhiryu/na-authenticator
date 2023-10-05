package pruebas;

import java.util.Date;
import neoAtlantis.utils.accessController.blocker.interfaces.BlockerWay;

/**
 *
 * @author desarrollo.alberto
 */
public class Pruebas {
    
    
    public static void main(String[] args) {
        int loginLife=30;
        long time=loginLife*BlockerWay.DAY_IN_MILLIS;
        
        Date ini=new Date();
        Date fechaPivote=new Date( ini.getTime() -time);
        
        System.out.println("I  "+ini);
        System.out.println("T  "+time);
        System.out.println("L  "+loginLife);
        System.out.println("F: "+fechaPivote);
    }
}
