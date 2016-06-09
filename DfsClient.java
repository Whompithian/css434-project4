/* 
 * @file   DfsClient.java
 * @brief  Implements the client for a distributed file system modeled after
 *          AFS.
 * @author Brendan Sweeney, ID #1161836
 * @date   December 13, 2012
 */
import java.io.*;
import java.util.*;
import java.rmi.*;
import java.rmi.server.*;
        
        
public class DfsClient extends UnicastRemoteObject
                    implements ClientInterface {
    private final static int ERROR       = 0;

    private final static int INVALID     = 0;
    private final static int READ_SHARED = 1;
    private final static int WRITE_OWNED = 2;
    private final static int RELEASE_OWN = 3;

    private final static int READ_MODE   = 10;
    private final static int WRITE_MODE  = 11;

    private final static String CACHE_NAME = "/tmp/bps7.txt";
    private final static String EDITOR     = "/usr/bin/emacs";

    private String  fileName;
    private Boolean write;
    private Boolean owner;
    private int     state;
    private FileContents    local;
    private ServerInterface server;
    
    
    public DfsClient() throws RemoteException {
        fileName = "";
        write = false;
        owner = false;
        state = INVALID;
    } // end constructor
    
    
    public static void main(String args[]) {
        String address, port;   // connection fields
        int mode;
        
        if (args.length != 2) {
            System.err.println("usage: java DfsClient server port#");
            System.exit(-1);
        } // end if (args.length != 2)

        address = args[0];
        port    = args[1];
        try {
            DfsClient client = new DfsClient();
            Naming.rebind("rmi://localhost:" + port + "/dfsclient", client);
            client.connect(address, port);
            do {
                mode = client.readInput();
                if (mode == READ_MODE) {
                    client.readFile();
                } // end if (mode.compareTo(READ_MODE) == 0)
                else if (mode == WRITE_MODE) {
                    client.writeFile();
                } // end else if (mode.compareTo(WRITE_MODE) == 0)
            } while (mode > 0);// end while(runner.readInput())
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } // end try new DfsClient();
    } // end main(String[])


    public boolean invalidate() { // set the DFS client’s file state to “Invalid”
        if (state == READ_SHARED) {
            state = INVALID;
        } // end if (state == READ_SHARED)
        return true;
    } // end invalidate()


    public boolean writeback() { // request the DFS client to upload its current cache.
        if (state == WRITE_OWNED) {
            state = RELEASE_OWN;
        } // end if (state == WRITE_OWNED)
        return true;
    } // end writeback()


    public void connect(String address, String port) {
        try {
            server = (ServerInterface)Naming.lookup(
                      "rmi://" + address + ":" + port + "/dfsserver");
        } catch (Exception e) {
            e.printStackTrace();
        } // end try Naming.lookup(...)
    } // end connect(String, String)
    
    
    private int readInput() {
        String getName = "", getMode = "";
        BufferedReader input = 
                new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("FileClient: Next file to open (~ to exit)");
        while (getName.compareTo("") == 0) {
            System.out.print("File name: ");
            try {
                getName = input.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            } // end try fileName = input.readLine();
        } // end while(fileName.compareTo("") == 0)
        if (getName.compareTo("~") == 0) {
            return 0;
        } // end if (getName.compareTo("~") == 0)
        while (getMode.compareToIgnoreCase("r") != 0 &&
               getMode.compareToIgnoreCase("w") != 0) {
            System.out.print("How(r/w): ");
            try {
                getMode = input.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            } // end try fileMode = input.readLine();
        } // end while(fileMode.compareTo(READ_MODE) != 0...)

        fileName = getName;

        if (getMode.compareToIgnoreCase("r") == 0) {
            return READ_MODE;
        } // end if (getMode.compareToIgnoreCase("r") == 0)
        if (getMode.compareToIgnoreCase("w") == 0) {
            return WRITE_MODE;
        } // end if (getMode.compareToIgnoreCase("w") == 0)

        return ERROR;
    } // end readInput(String)


    private boolean checkCache() {
        return true;
    } // end checkCache()


    private int readFile() {
        FileOutputStream file = null;
        switch (state) {
            case INVALID:
                try {
                    local = server.download(getClientHost(), fileName, "r");
                    file = new FileOutputStream(CACHE_NAME);
                    file.write(local.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }  finally {
                    if (file != null) {
                        try {
                            file.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } // end try file.close();
                    } // end if (file != null)
                } // end try file = new FileOutputStream(CACHE_NAME);
                write = false;
                owner = false;
                state = READ_SHARED;
                break;
            case READ_SHARED:
                if (!checkCache()) {
                    state = INVALID;
                } // end if()
                break;
            case WRITE_OWNED:
                break;
        } // end switch (state)
        edit("400");
        return 0;
    } // end openFile()


    private int writeFile() {
        FileOutputStream file = null;
        switch (state) {
            case INVALID:
                try {
                    local = server.download(getClientHost(), fileName, "w");
                    file = new FileOutputStream(CACHE_NAME);
                    file.write(local.get());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (file != null) {
                        try {
                            file.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } // end try file.close();
                    } // end if (file != null)
                } // end try file = new FileOutputStream(CACHE_NAME);
                write    = false;
                owner    = false;
                state    = WRITE_OWNED;
                break;
            case READ_SHARED:
                return state;
            case WRITE_OWNED:
                return state;
        } // end switch (state)
        edit("600");
        return 0;
    } // end writeFile()


    private void edit(String mode) {
        String[] cmdarray = new String[3];
        cmdarray[0] = "chmod";
        cmdarray[1] = mode;
        cmdarray[2] = CACHE_NAME;
        try {
            Runtime runtime = Runtime.getRuntime( );
            Process process = runtime.exec( cmdarray );
            int retval = process.waitFor( );
        } catch ( Exception e ) {
            e.printStackTrace( );
        } // end try Runtime runtime = Runtime.getRuntime( );
        cmdarray = new String[2];
        cmdarray[0] = EDITOR;
        cmdarray[1] = CACHE_NAME;
        try {
            Runtime runtime = Runtime.getRuntime( );
            Process process = runtime.exec( cmdarray );
            int retval = process.waitFor( );
        } catch ( Exception e ) {
            e.printStackTrace( );
        } // end try Runtime runtime = Runtime.getRuntime( );
        if (state == RELEASE_OWN) {
            state = READ_SHARED;
        } // end if (state == RELEASE_OWN)
    } // end edit(String)
} // end class DfsClient
