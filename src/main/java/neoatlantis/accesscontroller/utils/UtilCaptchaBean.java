package neoatlantis.accesscontroller.utils;

import javax.servlet.http.HttpServletRequest;
import neoatlantis.utils.data.interfaces.ConfirmationCode;
import org.apache.log4j.Logger;

/**
 *
 * @author hiryu
 */
public class UtilCaptchaBean {
    private static final Logger DEBUGER = Logger.getLogger(UtilCaptchaBean.class);    
    
    public static final String CODE_CONFIRMATION_KEY="na.util.app.codeConfimation";
    public static final String CODE_GENERATOR_KEY="na.util.app.generatorCode";
    public static final String CAPTCHA_PARAM="NA:Captcha";
    
    public static  boolean validateConfirmationCode(HttpServletRequest request, String code){
        String actual=(String)request.getSession().getAttribute(CODE_CONFIRMATION_KEY);
        ConfirmationCode codigo=(ConfirmationCode)request.getServletContext().getAttribute(CODE_GENERATOR_KEY);
        
        if( codigo==null ){
            throw new RuntimeException("No se tiene deinido un ''codeGenerator.");
        }
        
        DEBUGER.debug("Codido original: "+actual);
        DEBUGER.debug("Codido a validar: "+code);
        
        if( actual!=null && actual.equalsIgnoreCase(code) ){
            return true;
        }
        else{
            request.getSession().setAttribute(CODE_CONFIRMATION_KEY, codigo.create());
            
            return false;
        }
    }
}
