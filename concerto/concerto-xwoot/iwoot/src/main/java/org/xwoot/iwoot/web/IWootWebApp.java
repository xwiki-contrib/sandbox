package org.xwoot.iwoot.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.xwoot.iwoot.IWoot;
import org.xwoot.iwoot.xwootclient.XWootClientAPI;
import org.xwoot.iwoot.xwootclient.XWootClientException;
import org.xwoot.iwoot.xwootclient.XWootClientFactory;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.WikiContentManagerFactory;

public class IWootWebApp
{

    // singleton instance
    private static IWootWebApp instance;

    private IWoot iwoot;
    
    private boolean started = false;

    //Properties keys
    public static final String IWOOT_PROPERTIES_FILENAME = "iwoot.properties";

    public static final String IWOOT_WCM_TYPE_PROPERTY_KEY="iwoot_wcm";

    public static final String IWOOT_XWOOT_TYPE_PROPERTY_KEY="iwoot_xwoot";

    public static final String IWOOT_XWOOT_URL_PROPERTY_KEY="iwoot_xwoot_url";
    
    public static final String IWOOT_REAL_WCM_URL_PROPERTY_KEY="iwoot_real_wcm_url";
    
    public static final String IWOOT_REAL_WCM_LOGIN_PROPERTY_KEY="iwoot_real_wcm_login";
    
    public static final String IWOOT_REAL_WCM_PWD_PROPERTY_KEY="iwoot_real_wcm_pwd";
    
    //Properties values 

    public static final String IWOOT_MOCK_TYPE_PROPERTY_VALUE="mock";

    public static final String IWOOT_REAL_TYPE_PROPERTY_VALUE="real";


    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static synchronized IWootWebApp getInstance()
    {
        if(IWootWebApp.instance==null){
            IWootWebApp.instance=new IWootWebApp();
        }

        return IWootWebApp.instance;
    }



    public void init(String xwootURL,String wcm_type,String xwoot_type,String wcm_url,String wcm_login,String wcm_pwd,Integer siteId) throws IWootWebAppException{
        WikiContentManager wcm=null;
        XWootClientAPI xwoot=null;

        try {
            if (wcm_type.equals(IWOOT_REAL_TYPE_PROPERTY_VALUE)){

                wcm=WikiContentManagerFactory.getSwizzleFactory().createWCM(wcm_url, wcm_login, wcm_pwd);

            }else if(wcm_type.equals(IWOOT_MOCK_TYPE_PROPERTY_VALUE)){
                wcm=WikiContentManagerFactory.getMockFactory().createWCM();
            }else{
                throw new IWootWebAppException("Bad WCM type : "+wcm_type);
            }
        } catch (WikiContentManagerException e) {
            throw new IWootWebAppException("Problem during WCM creation",e);
        }

        try {
            if (xwoot_type.equals(IWOOT_REAL_TYPE_PROPERTY_VALUE)){
                xwoot=XWootClientFactory.getServletFactory().createXWootClient(xwootURL);
            }else if(xwoot_type.equals(IWOOT_MOCK_TYPE_PROPERTY_VALUE)){
                xwoot=XWootClientFactory.getMockFactory().createXWootClient(wcm);
            }else{
                throw new IWootWebAppException("Bad XWoot client type type : "+wcm_type);
            }
        } catch (XWootClientException e) {
            throw new IWootWebAppException("Problem during XWoot client creation",e);
        }

        this.iwoot=new IWoot(xwoot,wcm,siteId);
        this.started = true;
    }

    public Properties getProperties(String path)
    {
        Properties p = new Properties();
        try {
            FileInputStream fis = new FileInputStream(path);
            p.load(fis);
            fis.close();
        } catch (IOException ex) {
            // Cannot load properties, return empty properties.
        }
        return p;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public IWoot getIWootEngine()
    {
        return this.iwoot;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public boolean isStarted()
    {
        return this.started;
    }

    public void savePropertiesInFile(String path, String comments, Properties p) throws IOException
    {
        File f = new File(path);
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        p.store(fos, comments);
        fos.flush();
        fos.close();
    }

    public String updatePropertiesFiles(HttpServletRequest request, String propertiesPath) throws IOException
    {
        String result = "";
        Properties p;

        // Update properties.
        p = updatePropertiesFromRequest(request, propertiesPath);
        result += this.validateProperties(p);
        if (result.equals("")) {
            this.savePropertiesInFile(propertiesPath, " -- IWoot properties --", p);
        }

        return result;
    }

    public Properties updatePropertiesFromRequest(ServletRequest request, String propertiesPath)
    {
        Properties p = getProperties(propertiesPath);
        if (!StringUtils.isEmpty(request.getParameter(IWOOT_WCM_TYPE_PROPERTY_KEY))) {
            p.put(IWOOT_WCM_TYPE_PROPERTY_KEY, request.getParameter(IWOOT_WCM_TYPE_PROPERTY_KEY));
        }
        if (!StringUtils.isEmpty(request.getParameter(IWOOT_REAL_WCM_URL_PROPERTY_KEY))) {
            p.put(IWOOT_REAL_WCM_URL_PROPERTY_KEY, request.getParameter(IWOOT_REAL_WCM_URL_PROPERTY_KEY));
        }
        if (!StringUtils.isEmpty(request.getParameter(IWOOT_REAL_WCM_LOGIN_PROPERTY_KEY))) {
            p.put(IWOOT_REAL_WCM_LOGIN_PROPERTY_KEY, request.getParameter(IWOOT_REAL_WCM_LOGIN_PROPERTY_KEY));
        }
        if (!StringUtils.isEmpty(request.getParameter(IWOOT_REAL_WCM_PWD_PROPERTY_KEY))) {
            p.put(IWOOT_REAL_WCM_PWD_PROPERTY_KEY, request.getParameter(IWOOT_REAL_WCM_PWD_PROPERTY_KEY));
        }
        if (!StringUtils.isEmpty(request.getParameter(IWOOT_XWOOT_TYPE_PROPERTY_KEY))) {
            p.put(IWOOT_XWOOT_TYPE_PROPERTY_KEY, request.getParameter(IWOOT_XWOOT_TYPE_PROPERTY_KEY));
        }
        if (!StringUtils.isEmpty(request.getParameter(IWOOT_XWOOT_URL_PROPERTY_KEY))) {
            p.put(IWOOT_XWOOT_URL_PROPERTY_KEY, request.getParameter(IWOOT_XWOOT_URL_PROPERTY_KEY));
        }
        
        return p;
    }

    /**
     * Checks that the XWoot configuration is good.
     * 
     * @param p The configuration to validate.
     * @return A list of error messages to display to the user, as a <code>String</code>. If the configuration is good,
     *         then an <string>empty <code>String</code></strong> is returned.
     * @todo Message localization.
     */
    private String validateProperties(Properties p)
    {
        String result = "";        
        
        // Check WCM 
        if (p.get(IWOOT_WCM_TYPE_PROPERTY_KEY) == null) {
            result += "Please enter a non-empty WCM type (mock or real).\n";
        }else {
            if (!p.get(IWOOT_WCM_TYPE_PROPERTY_KEY).equals(IWOOT_MOCK_TYPE_PROPERTY_VALUE) 
                && !p.get(IWOOT_WCM_TYPE_PROPERTY_KEY).equals(IWOOT_REAL_TYPE_PROPERTY_VALUE)){
                result += "Please enter a valid WCM type (mock or real).\n";
            }else{
                if (p.get(IWOOT_WCM_TYPE_PROPERTY_KEY).equals(IWOOT_REAL_TYPE_PROPERTY_VALUE)){
                    
                    // Check that the WCM URL is valid.
                    if (p.get(IWOOT_REAL_WCM_URL_PROPERTY_KEY) == null) {
                        result += "Please enter a non-empty IWoot address.\n";
                    } else {
                        try {
                            new URL((String) p.get(IWOOT_REAL_WCM_URL_PROPERTY_KEY));
                        } catch (MalformedURLException e) {
                            result += "Please enter a valid IWoot address (the given URL is malformed)\n";
                        }
                    }
                    
                    // Check that the WCM login is valid.
                    if (p.get(IWOOT_REAL_WCM_LOGIN_PROPERTY_KEY) == null) {
                        result += "Please enter a non-empty wcm login.\n";
                    }
                    
                    // Check that the WCM pwd is valid.
                    if (p.get(IWOOT_REAL_WCM_PWD_PROPERTY_KEY) == null) {
                        result += "Please enter a non-empty wcm pwd.\n";
                    }
                }
            }
        }
        
        // Check XWOOT
        if (p.get(IWOOT_XWOOT_TYPE_PROPERTY_KEY) == null) {
            result += "Please enter a non-empty XWOOT type (mock or real).\n";
        }else {
            if (!p.get(IWOOT_XWOOT_TYPE_PROPERTY_KEY).equals(IWOOT_MOCK_TYPE_PROPERTY_VALUE) 
                && !p.get(IWOOT_XWOOT_TYPE_PROPERTY_KEY).equals(IWOOT_REAL_TYPE_PROPERTY_VALUE)){
                result += "Please enter a valid XWOOT type (mock or real).\n";
            }else{
                
                // Check that the XWoot URL is valid.
                if (p.get(IWOOT_XWOOT_URL_PROPERTY_KEY) == null) {
                    result += "Please enter a non-empty IWoot address.\n";
                } else {
                    try {
                        new URL((String) p.get(IWOOT_XWOOT_URL_PROPERTY_KEY));
                    } catch (MalformedURLException e) {
                        result += "Please enter a valid IWoot address (the given URL is malformed)\n";
                    }

                }
            }
        }
        
        return result;
    }
}
