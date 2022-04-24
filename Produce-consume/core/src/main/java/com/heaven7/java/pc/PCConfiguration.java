package com.heaven7.java.pc;

import com.heaven7.java.base.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * the pc config .
 * @author heaven7
 */
public final class PCConfiguration {

    /**
     * init the pc configuration.
     * @param in the input stream as configuration
     */
    public static void initialize(InputStream in){
        try {
            System.getProperties().load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally{
            IOUtils.closeQuietly(in);
        }
    }
}
