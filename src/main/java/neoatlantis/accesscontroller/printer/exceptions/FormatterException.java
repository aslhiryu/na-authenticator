package neoatlantis.accesscontroller.printer.exceptions;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class FormatterException extends RuntimeException {
    public FormatterException(Exception ex){
        super(ex);
    }    

    public FormatterException(String ex){
        super(ex);
    }    
}