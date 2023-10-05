package neoAtlantis.utilidades.accessController.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import neoAtlantis.utilidades.accessController.AccessController;
import neoAtlantis.utilidades.accessController.objects.User;
import neoAtlantis.utilidades.accessController.utils.html.AuditHtmlFormatter;
import org.apache.log4j.Logger;

/**
 * Bean que permite la recuepraci&oacute;n de objetos manejados por {@link neoAtlantis.utilidades.accessController.AccessController Control de Autenticaci&oacute;n}
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class AccessControllerBean {
    private static final Logger LOGGER = Logger.getLogger(AccessControllerBean.class);
    private static final SimpleDateFormat sdfFecha=new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Recupera el Control de acceso de la sesi&oacute;n.
     * @param request Request de http
     * @return Control de Acceso
     */
    public static AccessController getAccessController(HttpServletRequest request){
        return AccessController.getInstance();//(AccessController)request.getSession().getAttribute(AccessControllerPublisher.CLAVE_CTRL);
    }

    /**
     * Recupera usuario conectado de la sesi&oacute;n.
     * @param request Request de http
     * @return Usuario conectado
     */
    public static User getUser(HttpServletRequest request){
        return (User)request.getSession().getAttribute(AccessControllerPublisher.CLAVE_USER);
    }
    
    public static String getBitacora(HttpServletRequest request){
        int regs=5;
        int offset=1;
        HashMap<String,Object> data=new HashMap<String,Object>();
        String cTmp;
        
        //recupero los datos de registros y offset
        try{
            regs=Integer.parseInt(request.getParameter("regs"));
        }catch(Exception ex){}
        try{
            offset=((Integer.parseInt(request.getParameter("pag"))-1)*regs)+1;
        }catch(Exception ex){}
        if(request.getParameter("orderType")!=null && !request.getParameter("orderType").isEmpty()){
            data.put("orderType", request.getParameter("orderType"));
        }
        if(request.getParameter("order")!=null && !request.getParameter("order").isEmpty()){
            data.put("order", request.getParameter("order"));
        }
        
        //cargo los parametros        
        if(request.getParameter("user")!=null && !request.getParameter("user").isEmpty()){
            data.put("user", "%"+request.getParameter("user")+"%");
        }
        if(request.getParameter("origin")!=null && !request.getParameter("origin").isEmpty()){
            data.put("origin", "%"+request.getParameter("origin")+"%");
        }
        if(request.getParameter("terminal")!=null && !request.getParameter("terminal").isEmpty()){
            data.put("terminal", "%"+request.getParameter("terminal")+"%");
        }
        if(request.getParameter("date")!=null){
            try{
                data.put("date", sdfFecha.parse(request.getParameter("date")));
            }catch(Exception ex){}
        }
        if(request.getParameter("event")!=null && !request.getParameter("event").isEmpty()){
            data.put("event", request.getParameter("event"));
        }
        if(request.getParameter("detail")!=null && !request.getParameter("detail").isEmpty()){
            data.put("detail", "%"+request.getParameter("detail")+"%");
        }
        data.put("regs", regs);
        LOGGER.debug("Parametros encontrados: "+data);
        
        AccessController ctrl=getAccessController(request);
        if( ctrl==null ){
            LOGGER.debug("No existen Control de Acceso en memoria.");
            return "<font color='red'><b>No se tienen Control de Acceso para trabajar.</b></font>\n";
        }
        else{
            return AuditHtmlFormatter.formateaBitacora(request, getAccessController(request).recuperaDatosBitacora(data, regs, offset), data);
        }

    }
}
