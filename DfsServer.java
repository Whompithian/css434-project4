/* 
 * @file   DfsClient.java
 * @brief  Implements the server for a distributed file system modeled after
 *          AFS.
 * @author Brendan Sweeney, ID #1161836
 * @date   December 13, 2012
 */
import java.io.*;
import java.util.*;
import java.rmi.*;
import java.rmi.server.*;


public class DfsServer extends UnicastRemoteObject
                    implements ServerInterface {
    private SortedSet<String> clientList;
    private Vector<FileContents> cache;
    private String owner;


    public DfsServer() throws RemoteException {
        clientList = new TreeSet<String>();
        cache = new Vector<FileContents>();
        owner = "";
    } // end default constructor


    public static void main( String args[] ) {
        if (args.length != 1) {
            System.err.println("usage: java DfsServer port#");
            System.exit(-1);
        }
        try {
            DfsServer server = new DfsServer();
            Naming.rebind("rmi://localhost:" + args[0] + "/dfsserver", server);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    public FileContents download(String myIpName,
                                 String filename, String mode) {
        try {
            InputStream file = new FileInputStream(filename);
            byte[] b = new byte[1024];
            int len = file.read(b);
            byte[] content = new byte[len];
            System.arraycopy(b, 0, content, 0, len);
            FileContents fc = new FileContents(content);
            cache.add(fc);
            file.close();
            return fc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        clientList.add(myIpName);
        return null;
    } // end download(String, String, String)
    
    
    public boolean upload(String myIpName,
                          String filename, FileContents contents) {
        return true;
    } // end upload(String, String, FileContents)
} // end class DfsServer
