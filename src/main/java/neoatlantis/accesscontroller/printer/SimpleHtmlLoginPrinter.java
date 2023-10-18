package neoatlantis.accesscontroller.printer;

import java.text.SimpleDateFormat;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import neoatlantis.accesscontroller.AccessController;
import neoatlantis.accesscontroller.authentication.interfaces.AuthenticationWay;
import neoatlantis.accesscontroller.login.HtmlBasicLoginServlet;
import neoatlantis.accesscontroller.objects.User;
import neoatlantis.accesscontroller.printer.interfaces.LoginPrinter;
import neoatlantis.accesscontroller.web.UtilsAuthenticatorBean;
import neoatlantis.accesscontroller.web.listeners.AccessControllerPublisher;
import neoatlantis.accesscontroller.web.utils.PhotoUserServlet;
import neoatlantis.applications.printer.exceptions.FormatterException;
import neoatlantis.applications.web.UtilsApplicationBean;

/**
 * 
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class SimpleHtmlLoginPrinter implements LoginPrinter {
    public static final String REQUEST_KEY="HttpRequest";
    
    private static final SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yy");
    private static final SimpleDateFormat sdf2=new SimpleDateFormat("dd/MM/yy HH:mm");

    @Override
    public Object printLogin(Map<String,Object> params) {
        StringBuilder sb = new StringBuilder("");
        HttpServletRequest request;
        String username;
        
        if( params.get(REQUEST_KEY)==null ){
            throw new FormatterException("No se proporciono el request para generar el objeto HTML.");
        }
        //obtengo el request
        request=((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY));
        AccessController access=AccessController.getInstance();
        
        //si existe un error lo pinto
        if( request.getAttribute(AccessControllerPublisher.MESSAGE_ERROR_KEY)!=null ){
            sb.append("<span class='NA_Login_textoError'>").append(request.getAttribute(AccessControllerPublisher.MESSAGE_ERROR_KEY)).append("</span>\n");
        }
        
        //valida si existe la cookie de usuario
        username=HtmlBasicLoginServlet.getUserFromCookie(request);
        sb.append("<script src=\"").append( UtilsApplicationBean.getScriptUtilsPath(request)).append("\"></script>\n");
        sb.append("<script src=\"").append( UtilsAuthenticatorBean.getScriptAdminUserPath(request) ).append("\"></script>\n");
        sb.append("<script>\n");
        if( (params.get(AuthenticationWay.USER_PARAM)!=null && !((String)params.get(AuthenticationWay.USER_PARAM)).isEmpty()) || (username!=null && !username.isEmpty()) ){
            sb.append("addEvent(window, \"load\", function(){ document.getElementById(\"").append(AuthenticationWay.PASS_PARAM).append("\").focus(); });\n");
        }
        else{
            sb.append("addEvent(window, \"load\", function(){ document.getElementById(\"").append(AuthenticationWay.USER_PARAM).append("\").focus(); });\n");
        }
        sb.append("</script>\n");
        sb.append("<div class=\"NA_Login_form\">\n");
        sb.append("<form name=\"NA:LogginAccessForm\" action=\"").append(UtilsAuthenticatorBean.getLoginServletPath(request)).append("\" method=\"post\">\n");
        sb.append("<h1>Acceso</h1>\n");
        sb.append("<dl>\n");
        sb.append("<dt>Usuario:</dt>\n");
        if( username!=null && !username.isEmpty() ){
            sb.append("<dd><p>").append(username).append("</p><input type=\"hidden\" name=\"").append(AuthenticationWay.USER_PARAM).append("\" id=\"").append(AuthenticationWay.USER_PARAM).append("\" value=\"").append(username).append("\" /></dd>\n");
        }
        else{
            sb.append("<dd><input type=\"input\" name=\"").append(AuthenticationWay.USER_PARAM).append("\" id=\"").append(AuthenticationWay.USER_PARAM).append("\" value=\"").append(params.get(AuthenticationWay.USER_PARAM)!=null? params.get(AuthenticationWay.USER_PARAM): "").append("\" placeholder=\"Ingresa tu usuario\" /></dd>\n");
        }
        sb.append("<dt>Contrase&ntilde;a:</dt>\n");
        sb.append("<dd><input type=\"password\" name=\"").append(AuthenticationWay.PASS_PARAM).append("\" id=\"").append(AuthenticationWay.PASS_PARAM).append("\"  autocomplete=\"off\" placeholder=\"Ingresa tu contrase&ntilde;a\" /></dd>\n");
        if( access.isUseCaptcha()  && access.execedAttempt() ){
            sb.append( UtilsApplicationBean.printHtmlCaptcha(request) );
        }
        sb.append("</dl>\n");
        sb.append("<div class=\"NA_Controls\">\n");
        sb.append("<button id=\"NA:LogginAccessButton\"  type=\"submit\">Ingresar</button>\n");
        if( username!=null && !username.isEmpty() ){
            sb.append("<a id=\"NAAnotherUserButton\"  href=\"#\" onclick=\'location.href=\"").append(UtilsAuthenticatorBean.getLoginServletPath(request)).append("?").append(HtmlBasicLoginServlet.ANOTHER_USER_PARAM).append("\";\'>Cambiar de usuario</a>\n");
        }
        sb.append("</div>\n");
        sb.append("<input type=\"hidden\" name=\"").append(AuthenticationWay.LOGIN_PARAM).append("\" />\n");
        sb.append("</form>\n");
        sb.append("</div>\n");
        
        return sb.toString();
    }
    
    @Override
    public Object printUserDetails(Map<String,Object> params) {
        StringBuilder sb = new StringBuilder("");
        HttpServletRequest request;
        User user=null;
        
        if( params.get(REQUEST_KEY)==null ){
            throw new FormatterException("No se proporciono el request para generar el objeto HTML.");
        }
        //obtengo el request
        request=((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY));
        user=(User)request.getSession().getAttribute(AccessControllerPublisher.USER_KEY);

        if(user!=null){
            sb.append("<span class=\"NA_UserDetails_user\" onmouseover=\"document.getElementById('NA:userDetails').style.display='inline';\" onmouseout=\"document.getElementById('NA:userDetails').style.display='none';\">").append(user.getName()).append("</span>\n");
            sb.append("<div id=\"NA:userDetails\" style=\"display:none;\" class=\"NA_UserDetails_detail\" >\n");            
            sb.append("<img src=\"").append(request.getContextPath()).append(PhotoUserServlet.PATH_SERVICE).append("\" alt=\"Foto\" />\n");
            
            sb.append("<dl>\n");
            sb.append("<dt>Usuario</dt>\n");
            sb.append("<dd>").append(user.getUser()).append("</dd>\n");
            sb.append("<dt>ID</dt>\n");
            sb.append("<dd>").append(user.getId()).append("</dd>\n");
            sb.append("<dt>Correo</dt>\n");
            sb.append("<dd>").append(user.getMail()).append("</dd>\n");
            sb.append("<dt>Origen</dt>\n");
            sb.append("<dd>").append(user.getOrigin()).append("(").append(user.getTerminal()).append(")</dd>\n");
            sb.append("<dt>Alta</dt>\n");
            sb.append("<dd>").append(user.getCreatedDate()!=null? sdf.format(user.getCreatedDate()): "").append("</dd>\n");
            sb.append("<dt>&Uacute;ltima conexi&oacute;n</dt>\n");
            sb.append("<dd>").append(user.getLastAccessDate()!=null? sdf2.format(user.getLastAccessDate()): "").append("</dd>\n");
            for(int i=0; user.getProperties()!=null&&i<user.getProperties().length; i++){
                sb.append("<dt>").append(user.getProperties()[i]).append("</dt>\n");
                sb.append("<dd>");
                for(int j=0; user.getProperty(user.getProperties()[i])!=null&&j<user.getProperty(user.getProperties()[i]).length; j++){
                    if( j>0 ){
                        sb.append(", ");
                    }                    
                    if( user.getProperty(user.getProperties()[i])[j]!=null ){
                        sb.append( user.getProperty(user.getProperties()[i])[j] );
                    }
                }
                sb.append("</dd>");
            }
            sb.append("</dl>\n");
            sb.append("</div>\n");
        }
        
        return sb.toString();
    }
}
