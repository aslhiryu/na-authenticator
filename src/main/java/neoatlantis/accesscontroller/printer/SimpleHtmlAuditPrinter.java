package neoAtlantis.utils.accessController.printer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import neoAtlantis.utils.accessController.AccessController;
import neoAtlantis.utils.accessController.audit.interfaces.AuditWay;
import neoAtlantis.utils.accessController.audit.interfaces.EventAudit;
import neoAtlantis.utils.accessController.objects.LogEvent;
import static neoAtlantis.utils.accessController.printer.SimpleHtmlLoginPrinter.REQUEST_KEY;
import neoAtlantis.utils.accessController.printer.interfaces.AuditPrinter;
import static neoAtlantis.utils.accessController.printer.interfaces.UserAdministratorPrinter.MODE_ORDER_PARAM;
import static neoAtlantis.utils.accessController.printer.interfaces.UserAdministratorPrinter.ORDER_PARAM;
import static neoAtlantis.utils.accessController.printer.interfaces.UserAdministratorPrinter.PAGE_PARAM;
import neoAtlantis.utils.accessController.web.listeners.AccessControllerPublisher;
import neoAtlantis.utils.apps.catalogs.objetcs.OrderType;
import neoAtlantis.utils.apps.printer.exceptions.FormatterException;
import neoAtlantis.utils.apps.utils.UtilsPagination;
import neoAtlantis.utils.apps.web.utils.ResourcesLoader;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class SimpleHtmlAuditPrinter implements AuditPrinter {
    private static final Logger DEBUGER=Logger.getLogger(SimpleHtmlAuditPrinter.class);
    private SimpleDateFormat formateadorFecha=new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat formateadorFecha2=new SimpleDateFormat("dd/MM/yyyy");
    
    public static final String INITIAL_DATE_PARAM="NA:FilterInitialDate";
    public static final String END_DATE_PARAM="NA:FilterEndDate";

    @Override
    public Object printAudit(Map<String, Object> params) throws FormatterException {
        StringBuilder sb = new StringBuilder("");
        HttpServletRequest request;
        String order="1";
        OrderType ordTipo=OrderType.ASC;
        Date fecIni;
        Date fecFin;
        Calendar cTmp;
        
        if( params.get(REQUEST_KEY)==null ){
            throw new FormatterException("No se proporciono el request para generar el objeto HTML.");
        }
        //obtengo el request
        request=((HttpServletRequest)params.get(SimpleHtmlLoginPrinter.REQUEST_KEY));
        if( request.getParameter(ORDER_PARAM)!=null ){
            order=request.getParameter(ORDER_PARAM);
        }
        if( request.getParameter(MODE_ORDER_PARAM)!=null && request.getParameter(MODE_ORDER_PARAM).equalsIgnoreCase("DESC") ){
            ordTipo=OrderType.DESC;
        }

        //recupero el control de acceso
        AccessController ctrl=(AccessController)request.getServletContext().getAttribute(AccessControllerPublisher.ACCESS_CTRL_KEY);        

        int pagActual=1;        
        try{
            pagActual=Integer.parseInt( request.getParameter(PAGE_PARAM) );
        }catch(Exception ex){}

        cTmp=Calendar.getInstance();
        
        //si existen parametros los intenta asignar
        try{
            fecIni=formateadorFecha2.parse( request.getParameter(INITIAL_DATE_PARAM) );
        }catch(Exception ex){
            cTmp.set(Calendar.HOUR_OF_DAY, 0);
            fecIni=cTmp.getTime();
        }
        try{
            fecFin=formateadorFecha2.parse( request.getParameter(END_DATE_PARAM) );
        }catch(Exception ex){
            fecFin=cTmp.getTime();
        }
        cTmp.setTime(fecFin);
        cTmp.set(Calendar.HOUR_OF_DAY, 23);        
        fecFin=cTmp.getTime();
        
        try{
            //recupero la lista de eventos
            List<LogEvent> lTmp=ctrl.getEventList(fecIni, fecFin, order, ordTipo, pagActual);
            int pags=UtilsPagination.calculatePages( ctrl.getTotalEvents(fecIni, fecFin) );

            sb.append("<script src=\"").append( request.getContextPath() ).append(ResourcesLoader.PATH_UTILS_JS).append("\"></script>\n");
            sb.append("<form name=\"NA:ChangedDataList\" id=\"NA:ChangedDataList\" method=\"post\">\n");
            sb.append("<div id=\"NA_DataFilters\">\n");
            sb.append("<table>\n");
            sb.append("<tr>\n");
            sb.append("<td class=\"NA_DataFilter\">Fecha de Inicio</td>\n");
            sb.append("<td><input id=\"").append(INITIAL_DATE_PARAM).append("\" name=\"").append(INITIAL_DATE_PARAM).append("\" value=\"").append(formateadorFecha2.format(fecIni)).append("\" /></td>\n");
            sb.append("<td class=\"NA_DataFilter\">Fecha de Fin</td>\n");
            sb.append("<td><input id=\"").append(END_DATE_PARAM).append("\" name=\"").append(END_DATE_PARAM).append("\" value=\"").append(formateadorFecha2.format(fecFin)).append("\" /></td>\n");
            sb.append("</tr>\n");
            sb.append("</table>\n");
            sb.append("<div class=\"NA_DataFiltersControls\">\n");
            sb.append("<button type=\"button\" onclick=\"this.form.elements['").append(PAGE_PARAM).append("'].value='1';this.form.submit();\">Filtrar</button>\n");
            sb.append("</div>\n");
            sb.append("</div>\n");
            
            sb.append("<input type=\"hidden\" name=\"").append(PAGE_PARAM).append("\" id=\"").append(PAGE_PARAM).append("\"  value=\"").append(pagActual).append("\">\n");
            sb.append("<input type=\"hidden\" name=\"").append(ORDER_PARAM).append("\" id=\"").append(ORDER_PARAM).append("\"  value=\"").append(order).append("\">\n");
            sb.append("<input type=\"hidden\" name=\"").append(MODE_ORDER_PARAM).append("\" id=\"").append(MODE_ORDER_PARAM).append("\"  value=\"").append(ordTipo).append("\">\n");
            sb.append("<div class=\"NA_DataList_list\">\n");
            sb.append("<table>\n");
            sb.append("<tr>\n");
            sb.append("<th>\n");
            sb.append(AuditWay.USER_FIELD.toString().equalsIgnoreCase(order)? "<div class=\"NA_data_order"+ordTipo+"\"></div>": "");
            sb.append("<a href=\"javaScript:NADefaultDataChangeOrder('").append(AuditWay.USER_FIELD).append("', '").append(AuditWay.USER_FIELD.equals(order)&&ordTipo==OrderType.ASC? "DESC": "ASC").append("')\" />Usuario</a>\n");
            sb.append("</th>\n");
            sb.append("<th>\n");
            sb.append(AuditWay.ORIGIN_FIELD.toString().equalsIgnoreCase(order)? "<div class=\"NA_data_order"+ordTipo+"\"></div>": "");
            sb.append("<a href=\"javaScript:NADefaultDataChangeOrder('").append(AuditWay.ORIGIN_FIELD).append("', '").append(AuditWay.ORIGIN_FIELD.equals(order)&&ordTipo==OrderType.ASC? "DESC": "ASC").append("')\" />Origen</a>\n");
            sb.append("</th>\n");
            sb.append("<th>\n");
            sb.append(AuditWay.DATE_FIELD.toString().equalsIgnoreCase(order)? "<div class=\"NA_data_order"+ordTipo+"\"></div>": "");
            sb.append("<a href=\"javaScript:NADefaultDataChangeOrder('").append(AuditWay.DATE_FIELD).append("', '").append(AuditWay.DATE_FIELD.equals(order)&&ordTipo==OrderType.ASC? "DESC": "ASC").append("')\" />Fecha</a>\n");
            sb.append("</th>\n");
            sb.append("<th>\n");
            sb.append(AuditWay.EVENT_FIELD.toString().equalsIgnoreCase(order)? "<div class=\"NA_data_order"+ordTipo+"\"></div>": "");
            sb.append("<a href=\"javaScript:NADefaultDataChangeOrder('").append(AuditWay.EVENT_FIELD).append("', '").append(AuditWay.EVENT_FIELD.equals(order)&&ordTipo==OrderType.ASC? "DESC": "ASC").append("')\" />Evento</a>\n");
            sb.append("</th>\n");
            sb.append("<th>\n");
            sb.append(AuditWay.DETAIL_FIELD.toString().equalsIgnoreCase(order)? "<div class=\"NA_data_order"+ordTipo+"\"></div>": "");
            sb.append("<a href=\"javaScript:NADefaultDataChangeOrder('").append(AuditWay.DETAIL_FIELD).append("', '").append(AuditWay.DETAIL_FIELD.equals(order)&&ordTipo==OrderType.ASC? "DESC": "ASC").append("')\" />Detalle</a>\n");
            sb.append("</th>\n");
            sb.append("</tr>\n");
            for(int i=0; lTmp!=null&&i<lTmp.size(); i++){
                sb.append("<tr>\n");
                sb.append("<td>").append(lTmp.get(i).getUserId()).append("</td>\n");
                sb.append("<td>").append(lTmp.get(i).getOrigin()).append("</td>\n");
                sb.append("<td>").append(formateadorFecha.format(lTmp.get(i).getEventDate())).append("</td>\n");
                sb.append("<td>").append(lTmp.get(i).getEvent()).append("</td>\n");
                sb.append("<td>").append(lTmp.get(i).getDetail()).append("</td>\n");
                sb.append("</tr>\n");
            }
            
            sb.append("</table>\n");
            
            sb.append(UtilsPagination.printHtmlPagination(pags, pagActual)).append("\n");
            sb.append("</div>\n");
            sb.append("</form>\n");
            
            sb.append(getEventsDetail());
        }
        catch(Exception ex){
            DEBUGER.error("Problema con el printer de auditoria", ex);
            sb.append("<span class='NA_General_textoError'>Error al genera la lista de eventos: ").append(ex.getMessage()).append("</span>\n");
        }
        
        return sb.toString();
    }
    
    public static String getEventsDetail(){
        StringBuilder sb = new StringBuilder("");

        sb.append("<div class=\"NA_DataForm_info\">\n");
        sb.append("<dl>\n");
        for(EventAudit e: EventAudit.values()){
            sb.append("<dt>").append(e).append("</dt>");
            sb.append("<dd>").append(e.getDetail()).append("</dd>");
        }
        sb.append("</dl>\n");
        sb.append("</div>\n");
        
        return sb.toString();
    }
    
}
