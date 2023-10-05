package neoAtlantis.utilidades.accessController.objects;

/**
 * Definici&oacute;n d eun permiso.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.0
 */
public class Permission {
    private String nombre;
    private String valor;
    private String descripcion;

    /**
     * Constructor.
     * @param nombre Nombre del permiso
     * @param valor Valor del permiso
     * @param descripcion  Descripci&oacute;n del permiso
     */
    public Permission(String nombre, String valor, String descripcion){
        if(nombre==null || nombre.length()==0){
            throw new RuntimeException("No se puede generar un permiso sin nombre.");
        }

        this.nombre=PermissionEntity.limpiaNombre(nombre);
        this.valor=PermissionEntity.limpiaNombre(valor);
        this.descripcion=PermissionEntity.limpiaNombre(descripcion);
    }

    /**
     * Constructor.
     * @param nombre Nombre del permiso
     * @param valor Valor del permiso
     */
    public Permission(String nombre, String valor){
        this(nombre, valor, "SIN DESCRIPCION");
    }

    /**
     * Constructor.
     * @param nombre Nombre del permiso
     */
    public Permission(String nombre){
        this(nombre, "TRUE");
    }

    
    
    /**
     * Recupero el nombre
     * @return Nombre del permiso
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Recupera el valor
     * @return Valor del permiso
     */
    public String getValor() {
        return valor;
    }

    /**
     * Asigna valor al permiso
     * @param valor Valor a asignar
     */
    public void setValor(String valor) {
        this.valor = PermissionEntity.limpiaNombre(valor);
    }

    /**
     * Recupera de descripci&oacute;n
     * @return Descripci&oacute;n del permiso
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Genera la informac&oacute;n del permiso.
     * @return Informac&oacute;n del permiso
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("");

        sb.append("/***********************  PERMISO  **********************//").append(System.getProperty("line.separator"));
        sb.append("Nombre: ").append(this.nombre).append(System.getProperty("line.separator"));
        sb.append("Valor: ").append(this.valor).append(System.getProperty("line.separator"));
        sb.append("Descripcion: ").append(this.descripcion).append(System.getProperty("line.separator"));
        sb.append("/****************************************************//").append(System.getProperty("line.separator"));

        return sb.toString();
    }
}
