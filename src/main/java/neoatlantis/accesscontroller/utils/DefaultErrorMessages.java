package neoatlantis.accesscontroller.utils;

/**
 *
 * @author desarrollo.alberto
 */
public class DefaultErrorMessages {
    public static String defineMethodGetMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "No esta permitido el metodo 'get' para autenticar.";
        }
    }

    public static String defineErrorMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "Error al autenticar.";
        }
    }

    public static String defineBlockedMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "El usuario esta baneado.";
        }
    }

    public static String defineExpiresMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "El usuario esta caduco.";
        }
    }

    public static String defineDeniedMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "Acceso denegado.";
        }
    }

    public static String defineConnectedMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "El usuario ya esta conectado.";
        }
    }

    public static String defineCaptchaMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "La clave de confirmación no es correcta.";
        }
    }

    public static String defineOuttimeMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "No esta permitido el acceso en este horario.";
        }
    }

    public static String defineInactiveMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "El usuario esta deshabilitado.";
        }
    }

    public static String defineExcededMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "Se ha revasado el numero de intentos permitidos, la cuenta sera baneada.";
        }
    }

    public static String defineNotFoundMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "El usuario no es valido.";
        }
    }

    public static String defineTemporalMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "Acceso temporal, se requiere modificar la contraseña.";
        }
    }

    public static String defineProvisioningMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "La cuenta no esta aprovisionada.";
        }
    }

    public static String defineNotUserMessage(String cad){
        if( cad!=null && !cad.isEmpty() ){
            return cad;
        }
        else{
            return "No se proporciono el usuario.";
        }
    }
}
