package neoAtlantis.utilidades.ctrlAcceso.interfaces;

import java.util.*;

/**
 * Definici&oacute;n de una entidad a la cual se le pueden asignar y validar sus permisos.
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class EntidadConPermisos {

    private Properties permisos;

    /**
     * Genera una entidad con permisos
     */
    public EntidadConPermisos(){
        this.permisos = new Properties();
    }

    /**
     * Agrega un permiso a la entidad
     * @param permiso Nombre del permiso
     * @param valor Valor del permiso
     */
    public void agregarPermiso(String permiso, String valor) {
        if( permiso==null ){
            return;
        }

        this.permisos.setProperty(this.limpiaNombre(permiso), valor);
    }

    /**
     * Agrega un permiso a la entidad
     * @param permiso Permiso a agregar (asigna 'true' como valor del permiso)
     */
    public void agregarPermiso(String permiso) {
        if( permiso==null ){
            return;
        }

        this.agregarPermiso(permiso, "true");
    }

    /**
     * Verifica si tiene activo un permiso
     * @param permiso Permiso a validar
     * @return true si esta activo (para validar si esta activo revisa si el valor del permiso coincide con 'activo', 'ok', 'yes', 'si' o 'true')
     */
    public boolean validaPermiso(String permiso) {
        if( permiso==null ){
            return false;
        }

        String v = this.permisos.getProperty(permiso.toUpperCase());

        if (v != null &&
                (v.equalsIgnoreCase("activo") || v.equalsIgnoreCase("ok") ||
                v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("si") ||
                v.equalsIgnoreCase("true"))) {
            return true;
        }

        return false;
    }

    /**
     * Obtiene el valor dado a un permiso
     * @param permiso Permiso a obtener
     * @return valor del permiso
     */
    public String obtienePermiso(String permiso) {
        if( permiso==null ){
            return null;
        }

        return this.permisos.getProperty(permiso.toUpperCase());
    }

    /**
     * Genera la informac&oacute;n de la entidad.
     * @return Informaci&oacute;n de la entidad
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("");
        String tmp;


        sb.append("//--------------- Permisos ---------------//\n");
        if (this.permisos != null) {
            Enumeration noms = this.permisos.keys();
            while (noms.hasMoreElements()) {
                tmp = (String) noms.nextElement();
                sb.append("\t").append(tmp).append(": ").append(this.permisos.getProperty(tmp)).append("\n");
            }
        }
        sb.append("//---------------------------------------//\n");

        return sb.toString();
    }

    /**
     * Genera una cadena con un nombre limpio de caracteres no validos.
     * @param nombre Cadena con el nombre
     * @return Nombre sin caracteres validos
     */
    public String limpiaNombre(String nombre){
        return nombre.toUpperCase().replace('Á', 'A').replace('É', 'E').replace('Í', 'I').replace('Ó', 'O').replace('Ú', 'U');
    }
}