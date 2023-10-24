package neoatlantis.accesscontroller.printer.interfaces;

import java.util.Map;
import neoatlantis.accesscontroller.printer.exceptions.FormatterException;

/**
 * Definición de lo que debe contemplar un impresor de login
 * @author Hiryu (aslhiryu@gmail.com)
 */
public interface LoginPrinter {
    public Object printLogin(Map<String,Object> params) throws FormatterException;
    public Object printUserDetails(Map<String,Object> params) throws FormatterException;
}
