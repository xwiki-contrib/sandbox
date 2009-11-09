/**
 * 
 *        -- class header / Copyright (C) 2008  100 % INRIA / LGPL v2.1 --
 * 
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  Copyright (C) 2008  100 % INRIA
 *  Authors :
 *                       
 *                       Gerome Canals
 *                     Nabil Hachicha
 *                     Gerald Hoster
 *                     Florent Jouille
 *                     Julien Maire
 *                     Pascal Molli
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 *  INRIA disclaims all copyright interest in the application XWoot written
 *  by :    
 *          
 *          Gerome Canals
 *         Nabil Hachicha
 *         Gerald Hoster
 *         Florent Jouille
 *         Julien Maire
 *         Pascal Molli
 * 
 *  contact : maire@loria.fr
 *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  
 */

package org.xwoot.lpbcast.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Utility class for network operations.
 * 
 * @version $Id$
 */
public final class NetUtil
{
    /** Timeout for network operations in ms. */
    public static final int READ_TIME_OUT = 60000;

    /** The buffer size for network operations. */
    public static final int READ_BUFFER_SIZE = 1024;

    /** The name prefix of the temporary file where to store the downloaded content. */
    public static final String DOWNLOADED_STATE_FILE_NAME = "downloadedState";

    /** The extension of the temporary file where to store the downloaded content. */
    public static final String DOWNLOADED_STATE_FILE_EXTENSION = ".zip";

    /** The HTTP header field for Content-type. */
    public static final String HTTP_CONTENT_TYPE_HEADER = "Content-type";

    /**
     * Disable instantiation of utility class.
     */
    private NetUtil()
    {
        // void
    }

    /**
     * Download a file through HTTP.
     * 
     * @param url the location of the file.
     * @return the location on drive where the file was downloaded or null if the Content-type is "null".
     * @throws IOException if problems occur while transferring or connecting.
     */
    public static synchronized File getFileViaHTTPRequest(URL url) throws IOException
    {
        File file = null;
        URLConnection connection = null;
        InputStream connectionInputStream = null;
        BufferedOutputStream bos = null;

        try {
            connection = url.openConnection();

            if (connection != null) {
                String contentTypeHeaderField = connection.getHeaderField(HTTP_CONTENT_TYPE_HEADER);
                if (contentTypeHeaderField == null || contentTypeHeaderField.equals("null")) {
                    return null;
                }
            } else {
                return null;
            }

            file = File.createTempFile(DOWNLOADED_STATE_FILE_NAME, DOWNLOADED_STATE_FILE_EXTENSION);
            bos = new BufferedOutputStream(new FileOutputStream(file.getAbsolutePath()));

            connectionInputStream = connection.getInputStream();
            byte[] buffer = new byte[READ_BUFFER_SIZE];
            int numRead;

            while ((numRead = connectionInputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, numRead);
                bos.flush();
            }
        } catch (Exception e) {
            throw new IOException("Problems while getting file from Url: " + url + "\n " + e);
        } finally {
            try {
                if (connectionInputStream != null) {
                    connectionInputStream.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                throw new IOException("Problems closing the output file.\n" + e.getMessage());
            }
        }

        return file;
    }

    /**
     * Normalizes a given URL by applying {@link URI#normalize()}, removing trailing "/" and making sure the path part
     * starts with "/".
     * 
     * @param url the url in string format to normalize.
     * @return the normalized url as specified in the description.
     * @throws URISyntaxException if problems parsing the url occur.
     * @throws NullPointerException if the given url is null.
     */
    public static String normalize(String url) throws URISyntaxException, NullPointerException
    {
        String pathSeparator = "/";

        URI uri = new URI(url);
        uri = uri.normalize();

        String path = uri.getPath();

        if (!path.startsWith(pathSeparator)) {
            path = pathSeparator + path;
        }

        if (path.endsWith(pathSeparator)) {
            path = path.substring(0, path.length() - 1);
        }

        String urlStr = uri.getScheme() + "://" + uri.getHost();
        int port = uri.getPort();

        if (port != -1) {
            urlStr = urlStr + ":" + port;
        }

        urlStr = urlStr + path;

        return urlStr;
    }

    /**
     * @param url the url where to send the object to.
     * @param object the object to send
     * @throws IOException if problems occur while sending.
     */
    public static synchronized void sendObjectViaHTTPRequest(URL url, Object object) throws IOException
    {
        HttpURLConnection init = null;
        ObjectOutputStream out = null;

        try {
            init = (HttpURLConnection) url.openConnection();
            init.setConnectTimeout(NetUtil.READ_TIME_OUT);
            init.setReadTimeout(NetUtil.READ_TIME_OUT);
            init.setUseCaches(false);
            init.setDoOutput(true);
            init.setRequestProperty(HTTP_CONTENT_TYPE_HEADER, "application/octet-stream");

            out = new ObjectOutputStream(init.getOutputStream());
            out.writeObject(object);
            out.flush();

            init.getResponseCode();
        } catch (SocketTimeoutException s) {
            // Read timed out - try another time...
            if (init != null) {
                init.getResponseCode();
            }
        } catch (Exception e) {
            throw new IOException("Problems while sending the object " + object + " via HTTP at url " + url + "\n"
                + e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (init != null) {
                    init.disconnect();
                }
            } catch (Exception e) {
                throw new IOException("Problems closing the connection.\n" + e.getMessage());
            }
        }
    }

    /**
     * Tests whether a field exists in the HTTP header of a url.
     * 
     * @param url the url to test.
     * @param header the header field to test.
     * @return true if the header exists, false otherwise.
     * @throws IOException if problems occur connecting to the url.
     */
    public static synchronized boolean testHTTPRequestHeader(URL url, String header) throws IOException
    {
        HttpURLConnection init = (HttpURLConnection) url.openConnection();

        init.setConnectTimeout(NetUtil.READ_TIME_OUT);
        init.setReadTimeout(NetUtil.READ_TIME_OUT);
        init.setUseCaches(false);

        init.setRequestMethod("GET");

        return (init.getHeaderField(header) != null);
    }
}
