import java.rmi.*;
import java.util.*;

public interface ServerInterface extends Remote {
    public FileContents download(String ipName, String filename, String mode);
    boolean upload(String ipName, String filename, FileContents contents);
} // end interface ServerInterface
