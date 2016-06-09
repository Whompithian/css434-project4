import java.rmi.*;
import java.util.*;

public interface ClientInterface extends Remote {
    public boolean invalidate();    // set the client’s file state to “Invalid”
    public boolean writeback();     // request client to upload current cache.
} // end interface ClientInterface
