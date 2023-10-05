package neoAtlantis.utils.accessController.objects;

import java.io.Serializable;

/**
 * Definici&oacute;n d eun permiso.
 * @author Hiryu (aslhiryu@gmail.com)
 * @version 2.0
 */
public class Permission implements Serializable{
    private String id;
    private String nombre;
    private String valor;
    private String descripcion;

    /**
     * Constructor.
     * @param id Identificador del permiso
     * @param nombre Nombre del permiso
     */
    public Permission(String id, String nombre){
        if(nombre==null || nombre.length()==0){
            throw new RuntimeException("No se puede generar un permiso sin nombre o sin id.");
        }

        this.id=id;
        this.nombre=PermissionEntity.limpiaNombre(nombre);
        this.valor="TRUE";
        this.descripcion="Sin descripción";
    }

    /**
     * Constructor.
     * @param nombre Nombre del permiso
     */
    public Permission(String nombre){
        this(PermissionEntity.limpiaNombre(nombre), nombre);
    }

    public String getId() {
        return this.id;
    }
    
    /**
     * Recupero el nombre
     * @return Nombre del permiso
     */
    public String getName() {
        return this.nombre;
    }

    /**
     * Recupera el valor
     * @return Valor del permiso
     */
    public String getValue() {
        return this.valor;
    }

    /**
     * Asigna valor al permiso
     * @param valor Valor a asignar
     */
    public void setValue(String valor) {
        this.valor = valor;
    }

    public void setDescription(String description) {
        this.descripcion = description;
    }

    /**
     * Recupera de descripci&oacute;n
     * @return Descripci&oacute;n del permiso
     */
    public String getDescription() {
        return descripcion;
    }

    /**
     * Genera la informac&oacute;n del permiso.
     * @return Informac&oacute;n del permiso
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");

        sb.append("/***********************  ").append(this.getClass()).append("  **********************//").append(System.getProperty("line.separator"));
        sb.append("ID: ").append(this.id).append(System.getProperty("line.separator"));
        sb.append("Nombre: ").append(this.nombre).append(System.getProperty("line.separator"));
        sb.append("Valor: ").append(this.valor).append(System.getProperty("line.separator"));
        sb.append("Descripcion: ").append(this.descripcion).append(System.getProperty("line.separator"));
        sb.append("/****************************************************//").append(System.getProperty("line.separator"));

        return sb.toString();
    }
}
