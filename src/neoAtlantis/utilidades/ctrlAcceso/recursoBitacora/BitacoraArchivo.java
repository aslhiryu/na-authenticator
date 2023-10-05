package neoAtlantis.utilidades.ctrlAcceso.recursoBitacora;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import neoAtlantis.utilidades.ctrlAcceso.recursoBitacora.interfaces.EventoBitacora;
import neoAtlantis.utilidades.ctrlAcceso.recursoBitacora.interfaces.RecursoBitacorador;

/**
 * RecursoBitacorador que utiliza un archivo de texto para registrar los eventos.
 * @version 1.0
 * @author Hiryu (asl_hiryu@yahoo.com)
 */
public class BitacoraArchivo extends RecursoBitacorador {
    /**
     * Versi&oacute;n de la clase.
     */
    public static final String VERSION = "1.0";
    private File file;

    /**
     * Genera una RecursoBitacorador por archivo de texto.
     * @param archivo Ruta completa del archivo de texto
     * @throws java.io.FileNotFoundException
     */
    public BitacoraArchivo(String archivo) throws FileNotFoundException {
        this(new File(archivo));
    }

    /**
     * Genera una RecursoBitacorador por archivo de texto.
     * @param file Archivo de texto
     * @throws java.io.FileNotFoundException
     */
    public BitacoraArchivo(File file) throws FileNotFoundException {
        this.file = file;
        File fp = this.file.getParentFile();

        fp.mkdirs();
    }

    /**
     * Registra un evento en el archivo de texto.
     * @param usuario Usuario del que se desea registrar el evento
     * @param terminal Terminal desde donde se realiza el evento
     * @param origen Equipo desde donde se realiza el evento
     * @param evento Evento que se realiza
     * @param detalle Detalle del evento
     * @throws java.lang.Exception
     */
    public void escribeBitacora(String usuario, String terminal, String origen, EventoBitacora evento, String detalle) throws Exception {
        StringBuffer sb = new StringBuffer("");
        FileOutputStream out = new FileOutputStream(this.file, true);

        sb.append("[").append(this.sdf.format(new Date())).append("][").append(usuario).append("][").append(terminal).append("][").append(origen).append("][").append(evento).append("] ").append(detalle).append(this.finLinea);

        out.write(sb.toString().getBytes("UTF-8"));
        out.close();
    }

    /**
     * Extrae los datos del archivo de texto para poder trabajar con ellos.
     * @param ini Registro desde donde se desea obtener la informaci&oacute;n
     * @param tam Numero de registro que se desean obtener
     * @param fil Filtros que se desean aplicar para obtener los registros
     * @return Registros obtenidos
     * @throws java.lang.Exception
     */
    public String[][] generaReporte(int ini, int tam, Properties fil) throws Exception {
        FileInputStream in = new FileInputStream(this.file);
        int c;
        long pos = 0;
        int reg = 0, j = 0;
        StringBuffer sb = new StringBuffer("");
        ArrayList al = new ArrayList();

        in.skip(ini);
        while ((c = in.read()) != -1) {
            if (reg >= tam) {
                break;
            }
            if (c == '\n') {
                al.add(parseaCadena(sb.toString()));
                //System.out.println("-> "+sb.toString());
                sb = new StringBuffer("");
                reg++;
            } else {
                sb.append((char) c);
            }

            pos++;
            j++;
        }
        in.close();
        this.posicion = ini + pos;

        //System.out.println("C: "+pos+", R:"+reg);

        String[][] res = new String[al.size()][6];

        for (int i = 0; i < al.size(); i++) {
            res[i] = (String[]) al.get(i);
        }

        return res;
    }

    private String[] parseaCadena(String cad) {
        String[] s = new String[5];
        StringBuffer sb = new StringBuffer("");
        char[] chars = cad.toCharArray();
        int i = 0;

        for (int j = 0; j < 4; j++) {
            sb = new StringBuffer("");

            for (; i < chars.length && chars[i] != ']'&&chars[i] != '\n'; i++) {
                if (chars[i] == '[') {
                    continue;
                }

                sb.append(chars[i]);
                //System.out.println("- " + chars[i]);
            }

            s[j] = sb.toString().trim();
            //System.out.println("++ " + s[j]);
            i++;
        }
        sb = new StringBuffer("");
        for (; i < chars.length; i++) {
            sb.append(chars[i]);
        }
        s[4] = sb.toString().trim();
        //System.out.println("++ " + s[5]);

        return s;
    }
}
