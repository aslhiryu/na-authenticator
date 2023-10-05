package neoAtlantis.utilidades.accessController.blocker;

import java.util.List;
import java.util.ArrayList;
import neoAtlantis.utilidades.accessController.blocker.interfaces.BlockerWay;
import neoAtlantis.utilidades.accessController.objects.User;

/**
 * Medio Bloqueador nulo, utilizado para nulificar las operiones desencadenadas al 
 * medio por el {@link neoAtlantis.utilidades.accessController.AccessController Control de Autenticaci&oacute;n}
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class NullBlocker extends BlockerWay {

    @Override
    public void agregaBloqueo(User user) {
    }

    @Override
    public List<User> revisaBloqueosTerminados() {
        return (new ArrayList<User>());
    }

    @Override
    public boolean verificaBloqueo(User user) {
        return false;
    }

    @Override
    public boolean remueveBloqueo(User user) {
        return true;
    }

    @Override
    public void agregaConexion(User user) {
    }

    @Override
    public boolean remueveConexion(User user) {
        return true;
    }

    @Override
    public boolean verificaConexion(User user) {
        return false;
    }

    @Override
    public void actualizaActividadConexion(User user) {
    }

    @Override
    public User getUsuarioConectado(String id) {
        return null;
    }

    @Override
    public List<User> revisaSesionesInactivas() {
        return (new ArrayList<User>());
    }

    @Override
    public User getUsuarioBloqueado(String id) {
        return null;
    }

    @Override
    public List<User> getConexiones() {
        return (new ArrayList<User>());
    }

    @Override
    public User getUsuarioConectadoSesion(String ses) {
        return null;
    }

}
