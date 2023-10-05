package neoAtlantis.utilidades.accessController.objects;

import neoAtlantis.utilidades.entity.ContainerEntities;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class PeticionesCaptcha extends ContainerEntities<PeticionCaptcha> {

    public void agregaPeticion(String ip, String terminal, String codigo){
        boolean existe=false;

        for(PeticionCaptcha e: this.data){
            if( e.getIp().equals(ip) && e.getTerminal().equals(terminal) ){
                e.setCodigo(codigo);
                existe=true;
            }
        }

        if( !existe ){
            this.data.add(new PeticionCaptcha(ip, terminal, codigo));
        }
    }

    public PeticionCaptcha recuperaPeticion(String ip, String terminal){
        for(PeticionCaptcha e: this.data){
            if( e.getIp().equals(ip) && e.getTerminal().equals(terminal) ){
                this.data.remove(e);
                
                return e;
            }
        }

        return null;
    }
}
