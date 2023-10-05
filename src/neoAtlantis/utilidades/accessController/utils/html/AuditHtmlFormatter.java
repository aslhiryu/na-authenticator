package neoAtlantis.utilidades.accessController.utils.html;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import neoAtlantis.utilidades.accessController.audit.AuditEvent;
import neoAtlantis.utilidades.accessController.audit.interfaces.EventAudit;
import neoAtlantis.utilidades.objects.Event;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class AuditHtmlFormatter {
    static final private Logger LOGGER=Logger.getLogger(AuditHtmlFormatter.class);
    
    public static String formateaBitacora(HttpServletRequest request, List<Event> regs, Map<String,Object> data){
        StringBuilder sb=new StringBuilder("<table border=0 class='NA_bitacora_tablaPrincipal'>\n");
        int pag=1;
        
        try{
            pag=Integer.parseInt(request.getParameter("pag"));
        }catch(Exception ex){}

        sb.append("<form name='NA_bitacora_formaBitacora' action='").append(request.getContextPath()).append(request.getServletPath()).append("'>\n");
        sb.append("<tr>\n");
        sb.append("<td class='NA_titulo'>Bitacora de Eventos</td>\n");
        sb.append("</tr>\n");
        sb.append(generaTablaDatos(regs, pag, data));
        sb.append("<tr>\n");
        sb.append("<td>\n");
        sb.append(generaTablaForma(data));
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</form>\n");
        
        sb.append("</table>\n");
        
        return sb.toString();
    }
    
    public static String generaTablaDatos(List<Event> regs, int pag, Map<String,Object> data){
        StringBuilder sb=new StringBuilder("");
        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        boolean ultimo=false;
        
        sb.append("<tr>\n");
        sb.append("<td>\n");
        sb.append("<table border=0 class='NA_bitacora_tablaDatos'>\n");
        //para los titulos de columnas
        sb.append("<tr class='NA_titulo'>\n");            
        sb.append("<td>Usuario</td>\n");
        sb.append("<td>Origen</td>\n");
        sb.append("<td>Fecha</td>\n");
        sb.append("<td>Tipo de Evento</td>\n");
        sb.append("<td>Evento</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");            
        sb.append("<td><input name='user' style='width:100%' value='").append(data!=null&&data.get("user")!=null? (""+data.get("user")).replaceAll("%", ""): "").append("' /></td>\n");
        sb.append("<td><input name='origin' style='width:100%' value='").append(data!=null&&data.get("origin")!=null? (""+data.get("origin")).replaceAll("%", ""): "").append("' /></td>\n");
        sb.append("<td><input name='date' style='width:100%' value='").append(data!=null&&data.get("date")!=null? data.get("date"): "").append("' /></td>\n");
        sb.append("<td><input name='event' style='width:100%' value='").append(data!=null&&data.get("event")!=null? (""+data.get("event")).replaceAll("%", ""): "").append("' /></td>\n");
        sb.append("<td><input name='detail' style='width:100%' value='").append(data!=null&&data.get("detail")!=null? (""+data.get("detail")).replaceAll("%", ""): "").append("' /></td>\n");
        sb.append("</tr>\n");

        //pinta datos
        for(int i=0; regs!=null&&i<regs.size(); i++){
            //valido si terminaron los registros
            if( regs.get(i)==null ){
                ultimo=true;
                break;
            }
            
            sb.append("<tr class='NA_renglon_tipo").append((i%2)+1).append("'>\n");
            sb.append("<td>").append(((AuditEvent)regs.get(i)).getUsuario()).append("</td>\n");
            sb.append("<td>").append(regs.get(i).getOrigen()).append("</td>\n");
            sb.append("<td>").append(sdf.format(regs.get(i).getFecha()) ).append("</td>\n");
            sb.append("<td>").append(recuperaTextoTipoEvento(((AuditEvent)regs.get(i)).getEvento())).append("</td>\n");
            sb.append("<td>").append(regs.get(i).getDetalle()).append("</td>\n");
            sb.append("</tr>\n");
        }

        sb.append("</table>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("<td>\n");
        if( pag>1 ){
            sb.append("<a href='#' class='NA_boton_anterior' onclick='document.NA_bitacora_formaBitacora.pag.value=\"").append(pag-1).append("\";document.NA_bitacora_formaBitacora.submit();' title='Anterior'>&nbsp;</a>\n");
        }
        if( !ultimo ){
            sb.append("<a href='#' class='NA_boton_siguiente' onclick='document.NA_bitacora_formaBitacora.pag.value=\"").append(pag+1).append("\";document.NA_bitacora_formaBitacora.submit();' title='Siguiente'>&nbsp;</a>\n");
        }
        sb.append("</td>\n");
        sb.append("</tr>\n");
        
        return sb.toString();
    }
    
    public static String generaTablaForma(Map<String,Object> data){
        StringBuilder sb=new StringBuilder("");
        
        LOGGER.debug("Parametros para la forma: "+data);
        sb.append("<table border=0 class='NA_bitacora_tablaCaptura'>\n");
        sb.append("<tr>\n");
        sb.append("<td class='NA_campo'>No. de Registros</td>");
        sb.append("<td class='NA_dato' colspan=2>");
        sb.append("<select name='regs'>\n");
        sb.append("<option value='5'").append(data!=null&&data.get("regs")!=null&&(Integer)data.get("regs")==5? " selected": "").append(">5</option>\n");
        sb.append("<option value='10'").append(data!=null&&data.get("regs")!=null&&(Integer)data.get("regs")==10? " selected": "").append(">10</option>\n");
        sb.append("<option value='20'").append(data!=null&&data.get("regs")!=null&&(Integer)data.get("regs")==20? " selected": "").append(">20</option>\n");
        sb.append("<option value='50'").append(data!=null&&data.get("regs")!=null&&(Integer)data.get("regs")==50? " selected": "").append(">50</option>\n");
        sb.append("</select>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("<td class='NA_campo'>Ordernar</td>");
        sb.append("<td class='NA_dato'>");
        sb.append("<select name='order'>\n");
        sb.append("<option value='date'").append(data!=null&&data.get("order")!=null&&data.get("order").equals("date")? " selected": "").append(">Fecha</option>\n");
        sb.append("<option value='origin'").append(data!=null&&data.get("order")!=null&&data.get("order").equals("origin")? " selected": "").append(">Origen</option>\n");
        sb.append("<option value='type'").append(data!=null&&data.get("order")!=null&&data.get("order").equals("type")? " selected": "").append(">Tipo de Evento</option>\n");
        sb.append("<option value='user'").append(data!=null&&data.get("order")!=null&&data.get("order").equals("user")? " selected": "").append(">Usuario</option>\n");
        sb.append("</select>\n");
        sb.append("<select name='orderType'>\n");
        sb.append("<option value='asc'").append(data!=null&&data.get("orderType")!=null&&data.get("orderType").equals("asc")? " selected": "").append(">Ascendente</option>\n");
        sb.append("<option value='desc'").append(data!=null&&data.get("orderType")!=null&&data.get("orderType").equals("desc")? " selected": "").append(">Decendente</option>\n");
        sb.append("</select>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("<td class='NA_campo' colspan=3><input type='button' value='Aplicar' onclick='this.form.submit();' /></td>");
        sb.append("</tr>\n");
        sb.append("</table>\n");
        sb.append("<input type='hidden' name='pag' value='1' />\n");
        
        return sb.toString();
    }
    
    //--------------------------------------------------------------------------
    
    public static String recuperaTextoTipoEvento(EventAudit tipo){
        if( tipo==EventAudit.LOGEO ){
            return "Logeo (L)";
        }
        else if( tipo==EventAudit.RECHAZO ){
            return "Rechazo (R)";
        }
        else if( tipo==EventAudit.BLOQUEO ){
            return "Bloqueo (B)";
        }
        else if( tipo==EventAudit.INGRESO ){
            return "Ingreso (I)";
        }
        else if( tipo==EventAudit.EGRESO ){
            return "Egreso (O)";
        }
        else if( tipo==EventAudit.NEGOCIO ){
            return "Negocio (N)";
        }
        
        return "Acceso (A)";
    }
}
