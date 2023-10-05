package neoAtlantis.utilidades.accessController.resourcesFilter;

/**
 * Definici&oacute;n de una excepci&oacute;n del filtro de acceso, el cual se 
 * evalua por {@link neoAtlantis.utilidades.accessController.resourcesFilter.interfaces.ResourceAccessAllower Validador de Accesos a Recursos}
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 1.0
 */
public class FilterException {
    private String recurso;
    private TypeException tipo;

    /**
     * Genera una Excepci&oacute;n de filtro de acceso
     * @param recurso Recurso que se omitir&aacute;
     * @param tipo Tipo de recurso
     */
    public FilterException(String recurso, TypeException tipo){
        if( recurso.startsWith("/") ){
            this.recurso=recurso.substring(1);
        }
        else{
            this.recurso=recurso;
        }
        this.tipo=tipo;
    }

    /**
     * Genera una Excepci&oacute;n de filtro de acceso
     * @param recurso Recurso que se omitir&aacute;
     */
    public FilterException(String recurso){
        this(recurso, TypeException.PAGE);
    }

    /**
     * Recupera el recurso que esta omitido
     * @return Recurso omitido
     */
    public String getRecurso() {
        return recurso;
    }

    /**
     * Recupera el tipo de recurso omitido
     * @return Tipo de recurso
     */
    public TypeException getTipo() {
        return tipo;
    }

    /**
     * Asigna el tipo de recurso a omitir
     * @param tipo Tipo de recurso
     */
    public void setTipo(TypeException tipo) {
        this.tipo = tipo;
    }
}
