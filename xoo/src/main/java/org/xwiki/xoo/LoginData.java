/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwiki.xoo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The class used for storing the user settings in a special file
 * 
 * @version $Id$
 * @since 1.0 M
 */
public class LoginData
{
    /* The name of the file with the settings */
    private String filename = "XOOLoginData";

    /* The number of the credentials in the file */
    private final int numCredentials = 5;

    /**
     * @param credentials The credentials to be written
     * @return true if he operation succeeds, false otherwise
     */
    public boolean writeCredentials(String[] credentials)
    {
        boolean ret = true;
        BufferedWriter bufferedWriter = null;

        try {
            String homeDir = System.getProperty("user.home");
            String datafilename = homeDir + File.separator + filename;
            System.out.println(datafilename);
            File file = new File(datafilename);
            file.createNewFile();

            bufferedWriter = new BufferedWriter(new FileWriter(datafilename));
            for (String credential : credentials) {
                bufferedWriter.write(credential);
                bufferedWriter.newLine();
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            ret = false;
        } catch (IOException ex) {
            ex.printStackTrace();
            ret = false;
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                ret = false;
            }
        }
        return ret;
    }

    /**
     * @return the credentials stored in the special file
     */
    public String[] getCredentials()
    {

        String homeDir = System.getProperty("user.home");
        String datafilename = homeDir + File.separator + filename;
        
        BufferedReader bufferedReader = null;
        String[] ret = null;

        try {
            String[] credentials = new String[numCredentials];
            bufferedReader = new BufferedReader(new FileReader(datafilename));
            String line = null;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                credentials[i] = line;
                ++i;
            }
            ret = credentials;

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Deletes the file with the stored credentials
     */
    public void clearCredentials()
    {
        String homeDir = System.getProperty("user.home");
        String datafilename = homeDir + File.separator + filename;
        
        File file = new File(datafilename);
        file.delete();
    }

    /**
     * @return true if the file with credentials exists, false otherwise
     */
    public boolean canAutoLogin()
    {
        String homeDir = System.getProperty("user.home");
        String datafilename = homeDir + File.separator + filename;

        File file = new File(datafilename);
        return file.exists();
    }
}
