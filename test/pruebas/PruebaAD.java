package pruebas;

import com.sun.jna.platform.win32.Guid.GUID;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.util.Hashtable;
import java.util.UUID;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import neoAtlantis.utils.accessController.authentication.LDAPAuthentication;

/**
 *
 * @author desarrollo.alberto
 */
public class PruebaAD {
    public static String base="DC=economia,DC=gob,DC=mx";
    public static String user="desarrollo.alberto";
    public static String url="ldap://172.18.53.100:389";
    
    
    public static void main(String[] args) throws Exception {
        //personaLogeada();
        busquedaPoUniqueID();   
    }
    
    
    public static void busquedaPoUniqueID() throws Exception {
        Hashtable auth = new Hashtable();
        String dn=LDAPAuthentication.getDNString("activeDirectory", base,user);
        //String filtro="sAMAccountName=desarrollo.alberto";
        String filtro="objectGUID=\\67\\b3\\40\\a2\\ea\\07\\48\\f1\\b3\\5d\\7d\\15\\dc\\1c\\60\\61";
        SearchResult sr;
        Attributes attrs;
        Attribute foto;
        int b;
        byte[] id;

        auth.put(Context.SECURITY_PRINCIPAL, dn);
        auth.put(Context.SECURITY_CREDENTIALS, "BLUEMARY");

            auth.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
            auth.put(Context.PROVIDER_URL, url);
            auth.put(Context.SECURITY_AUTHENTICATION, "simple");
            auth.put(Context.REFERRAL, "follow");
            auth.put("com.sun.jndi.ldap.read.timeout", ""+30000);
        
        DirContext ctx = new InitialDirContext(auth);   

            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration res = ctx.search(base, filtro, searchCtls);
            if( res.hasMore() ){
                System.out.println("Todo ok");
                
                sr = (SearchResult)res.next();
                attrs = sr.getAttributes();
                
                if (attrs != null) {
                    System.out.println("Tiene atributos");
                    
                    foto=attrs.get("displayName");
                    System.out.println("Nombre: "+foto);
                    
                    foto=attrs.get("thumbnailPhoto");                    
                    System.out.println("Foto: "+foto);
                    
                    foto=attrs.get("objectSid");                    
                    System.out.println("SID: "+foto);
                    id=((String)foto.get()).getBytes();
                    System.out.println("SID: "+foto.get().getClass());
                    System.out.println("SID: "+id);
                    System.out.println("SID: "+convertToBindingString(id));
                    System.out.println("SID: "+getFormatGUID(id));
                    System.out.println("SID: "+getFormatGUID2(id));
                    System.out.println("SID: "+getFormatGUID3(id));
                    /*GUID idTmp=GUID.fromString((String)foto.get());
                    System.out.println("SID: "+idTmp);
                    System.out.println("SID: "+idTmp.toGuidString());*/                    
                    
                    foto=attrs.get("objectGUID");                    
                    System.out.println("GUID: "+foto);
                    id=((String)foto.get()).getBytes();
                    System.out.println("GUID: "+foto.get().getClass());
                    System.out.println("GUID: "+id);
                    System.out.println("GUID: "+Base64.encode(id));
                    System.out.println("GUID: "+convertToBindingString(id));
                    /*idTmp=GUID.fromString((String)foto.get());
                    System.out.println("GUID: "+idTmp);
                    System.out.println("GUID: "+idTmp.toGuidString());*/
// Where GUID is a byte array returned by a previous LDAP search
                    System.out.println("GUID: "+getFormatGUID(id));
                    System.out.println("GUID: "+getFormatGUID2(id));
                    System.out.println("GUID: "+getFormatGUID3(id));
                    
                    System.out.println("ORI:  67b340a2-ea07-48f1-b35d-7d15dc1c6061");
                    System.out.println("GUID: "+getFormatGUID4(id));
                }
            }
            
            ctx.close();
    }
    
    static String getFormatGUID4(byte[] data){
        StringBuilder cTmp = new StringBuilder("");
        cTmp.append( AddLeadingZero((int)data[3] & 0xFF) );
        
        return cTmp.toString();
    }
    
    static String getFormatGUID3(byte[] data){
        StringBuilder cTmp = new StringBuilder("");
        int b;
        
        for (int c=0;c<data.length;c++) {
            if(data[c]>=0)
                b=(int )data[c]  ;                
            else
                b=Math.abs((int )data[c]) ;
            cTmp.append("|").append(b).append("[").append( Integer.toString(b, 16) ).append("]");
        }
        
        return cTmp.toString();
    }
    
    static String getFormatGUID2(byte[] data){
        StringBuilder cTmp = new StringBuilder("");
        
        for (int c=0;c<data.length;c++) {
            cTmp.append("|").append( Integer.toString(data[c], 16) );
        }
        
        return cTmp.toString();
    }
    
    static String getFormatGUID(byte[] data){
        StringBuilder cTmp = new StringBuilder("");
        for (int c=0;c<data.length;c++) {
            //byteGUID = byteGUID + "\\" + AddLeadingZero((int)data[c] & 0xFF);
            cTmp.append( AddLeadingZero((int)data[c] & 0xFF) );
//            System.out.println(c+": "+AddLeadingZero((int)data[c] & 0xFF) );
        }
        
        return cTmp.toString();
    }

	static String AddLeadingZero(int k) {
		return (k <= 0xF)?"0" + Integer.toHexString(k):Integer.toHexString(k);
	}
        
        
        
        
public static String convertToBindingString(byte[] objectGUID) {
    StringBuilder displayStr = new StringBuilder();

    displayStr.append("<GUID=");
    displayStr.append(convertToDashedString(objectGUID));
    displayStr.append(">");

    return displayStr.toString();
}

public static String convertToDashedString(byte[] objectGUID) {
    StringBuilder displayStr = new StringBuilder();

    displayStr.append(prefixZeros((int) objectGUID[3] & 0xFF));
    displayStr.append(prefixZeros((int) objectGUID[2] & 0xFF));
    displayStr.append(prefixZeros((int) objectGUID[1] & 0xFF));
    displayStr.append(prefixZeros((int) objectGUID[0] & 0xFF));
    displayStr.append("-");
    displayStr.append(prefixZeros((int) objectGUID[5] & 0xFF));
    displayStr.append(prefixZeros((int) objectGUID[4] & 0xFF));
    displayStr.append("-");
    displayStr.append(prefixZeros((int) objectGUID[7] & 0xFF));
    displayStr.append(prefixZeros((int) objectGUID[6] & 0xFF));
    displayStr.append("-");
    displayStr.append(prefixZeros((int) objectGUID[8] & 0xFF));
    displayStr.append(prefixZeros((int) objectGUID[9] & 0xFF));
    displayStr.append("-");
    displayStr.append(prefixZeros((int) objectGUID[10] & 0xFF));
    displayStr.append(prefixZeros((int) objectGUID[11] & 0xFF));
    displayStr.append(prefixZeros((int) objectGUID[12] & 0xFF));
    displayStr.append(prefixZeros((int) objectGUID[13] & 0xFF));
    displayStr.append(prefixZeros((int) objectGUID[14] & 0xFF));
    displayStr.append(prefixZeros((int) objectGUID[15] & 0xFF));

    return displayStr.toString();
}

private static String prefixZeros(int value) {
    if (value <= 0xF) {
        StringBuilder sb = new StringBuilder("0");
        sb.append(Integer.toHexString(value));

        return sb.toString();

    } else {
        return Integer.toHexString(value);
    }
}    
}
