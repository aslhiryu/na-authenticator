package neoAtlantis.utils.accessController.jmx;

import neoAtlantis.utils.accessController.blocker.interfaces.BlockerWay;
import org.apache.log4j.Logger;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public class InfoStatusUsersMBean implements InfoStatusUsers{
    private static final Logger DEBUGGER=Logger.getLogger(InfoStatusUsersMBean.class);
    
    private BlockerWay bw;
    private int maxBlo;
    private int maxCon;
    private double minLif;
    private double maxLif;
    
    public InfoStatusUsersMBean(BlockerWay bw){
        this.bw=bw;
    }


    @Override
    public int getConnectedUsers() {
        int val=0;
        
        if(this.bw==null  ){
            DEBUGGER.debug("Recupera los usuarios, pero aun no se tiene un BlockerWay");            
        }
        else{
            DEBUGGER.debug("Recupera los usuarios con un contexto existente");
            val= bw.getConnections().size();
            
            if(val>this.maxCon){
                this.maxCon=val;
            }
        }
        
        return val;
    }
    
    @Override
    public int getBlockedUsers() {
        int val=0;
        
        if(this.bw==null  ){
            DEBUGGER.debug("Recupera los usuarios bloqueados, pero aun no se tiene un BlockerWay");            
        }
        else{
            DEBUGGER.debug("Recupera los usuarios con un contexto existente");
            val= bw.getBlocked().size();
            
            if(val>this.maxBlo){
                this.maxBlo=val;
            }
        }
        
        return val;
    }
    
    @Override
    public int getConnectedUsersMaximum(){
        return this.maxCon;
    }
    
    @Override
    public int getBlockedUsersMaximum(){
        return this.maxBlo;
    }
    
    @Override
    public double getConnectionLifetimeAverage(){
        return (this.minLif+this.maxLif)/2;
    }
    
    @Override
    public double getConnectionLifetimeMaximun(){
        return this.maxLif;
    }
    
    @Override
    public double getConnectionLifetimeMinimal(){
        return this.minLif;
    }
    
    
    
    
    
    public void updateLifeConnection(double lifeTime){
        if( lifeTime>this.maxLif ){
            this.maxLif=lifeTime;
        }
        if (this.minLif==0 || lifeTime<this.minLif){
            this.minLif=lifeTime;
        }
    }
}
