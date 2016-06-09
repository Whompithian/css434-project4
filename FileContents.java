/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.util.*;


public class FileContents implements Serializable {
    private byte[] contents; // file contents


    public FileContents( byte[] contents ) {
        this.contents = contents;
    } // end constructor


    public void print( ) throws IOException {
        System.out.println( "FileContents = " + contents );
    } // end print()

    public byte[] get( ) {
        return contents;
    } // end get()
} // end class FileContents
