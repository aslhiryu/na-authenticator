package neoAtlantis.utils.accessController.jmx;

/**
 *
 * @author Hiryu (aslhiryu@gmail.com)
 */
public interface InfoStatusUsers {
    public int getConnectedUsers();
    public int getBlockedUsers();
    public int getConnectedUsersMaximum();
    public int getBlockedUsersMaximum();
    public double getConnectionLifetimeAverage();
    public double getConnectionLifetimeMaximun();
    public double getConnectionLifetimeMinimal();
}
