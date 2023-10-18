package neoAtlantis.utils.accessController.web.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import neoAtlantis.utils.accessController.objects.User;
import neoAtlantis.utils.accessController.web.listeners.AccessControllerPublisher;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class PhotoUserServlet extends HttpServlet {
    private static final Logger DEBUGER = Logger.getLogger(PhotoUserServlet.class);
    
    public static final String PATH_SERVICE="/neoAtlantis/resources/web/photoUserView.service";

    private String type;
    private String extension;
    

    
    
    
    
    // Contructores ------------------------------------------------------------

    public PhotoUserServlet(){
        this("image/png");
    }

    public PhotoUserServlet(String type){
        this.type=type;
        if( type!=null && type.indexOf('/')>0 ){
            this.extension=type.substring(type.indexOf('/')+1);
        }
        else{
            this.extension="png";
        }
    }

    
    
    
    
    // Metodos publicos---------------------------------------------------------
        
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        User user=(User)request.getSession().getAttribute(AccessControllerPublisher.USER_KEY);
        ByteArrayOutputStream buffer=new ByteArrayOutputStream();
        int nRead;
        InputStream file;
        
        DEBUGER.debug("Intenta renderizar la foto  del usuario: "+user);
        try{
            response.setContentType(this.type);
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Disposition", "attachment; filename=\"photo."+this.extension+"\"");
            
            //valida que exista usuario en sesion y que tenga foto
            if( user!=null && user.getPhoto()!=null ){
                response.getOutputStream().write(user.getPhoto());
                response.flushBuffer();

                DEBUGER.debug("Se despliega la foto");
            }
            else{
                file=this.getClass().getResourceAsStream("noPhoto.png");
                byte[] data = new byte[16384];
                while( (nRead = file.read(data, 0, data.length))!=-1 ){
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                
                response.getOutputStream().write( buffer.toByteArray() );
                response.flushBuffer();

                DEBUGER.debug("se despliega la generica");
            }
        }
        catch(Exception ex){
            DEBUGER.error("No logre cargar la foto", ex);
        }
    }    

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        this.doGet(request, response);
    }
}
