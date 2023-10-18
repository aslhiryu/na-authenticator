package neoAtlantis.utils.accessController.printer.interfaces;

import java.util.Map;
import neoAtlantis.utils.apps.printer.exceptions.FormatterException;

/**
 * Definición de lo que debe contemplar un impresor de auditoria
 * @author Hiryu (aslhiryu@gmail.com)
 */
public interface AuditPrinter {
    public Object printAudit(Map<String,Object> params) throws FormatterException;
}
