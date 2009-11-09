package org.xwoot.iwoot.xwootclient.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.w3c.dom.NodeList;
import org.xwoot.iwoot.xwootclient.XWootClientAPI;

public class XWootClientServlet implements XWootClientAPI
{

    final String xwootUrl;

    final String addNeighborContext = "/synchronize.do?action=addNeighbor";

    final String cpConnectionContext = "/synchronize.do?action=cpconnection";

    final String informationContext = "/information?request=";
    
    final String pageList = "listLastPages&id=";

    final String isXWootInitialized=  "isXWootInitialized";

    final String isCPConnected="isWikiConnected";

    final String isP2PNetworkConnected="isP2PNetworkConnected";
    

    public XWootClientServlet(String xwootURL)
    {
        this.xwootUrl = xwootURL;
    }

//    public boolean addNeighbour(String neighborURL)
//    {
//        URL to;
//        try {
//            to = new URL(this.xwootUrl + this.addNeighborContext + "&neighbor=" + neighborURL);
//            HttpURLConnection init = (HttpURLConnection) to.openConnection();
//            init.disconnect();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        // TODO Auto-generated method stub
//        return true;
//    }

    private Document getInfos(String wantedValue) {
        try {
            URL url = new URL(this.xwootUrl+this.informationContext+wantedValue);

            try {
                URLConnection URLconnection = url.openConnection ();
                HttpURLConnection httpConnection = (HttpURLConnection)URLconnection;

                int responseCode = httpConnection.getResponseCode ();
                if ( responseCode == HttpURLConnection.HTTP_OK) 
                {
                    InputStream in = httpConnection.getInputStream ();
                    SAXBuilder sxb = new SAXBuilder();
                    try
                    {
                        Document document = sxb.build(in);
                        return document;
                    } catch(JDOMException e) {
                        e.printStackTrace ();
                    }
                } else 
                {
                    System.out.println("HTTP connection response != HTTP_OK" );
                } 
            } catch ( IOException e ) { 
                e.printStackTrace ( ) ;
            } 
        } catch ( MalformedURLException e ) {  
            e.printStackTrace ( ) ;
        } 
        return null;
    } 

    public void connectToContentManager()
    {
        URL to;
        try {
            to = new URL(this.xwootUrl + this.cpConnectionContext + "&switch=on");
            HttpURLConnection init = (HttpURLConnection) to.openConnection();
            init.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromContentManager()
    {
        URL to;
        try {
            to = new URL(this.xwootUrl + this.cpConnectionContext + "&switch=off");
            HttpURLConnection init = (HttpURLConnection) to.openConnection();
            init.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isContentManagerConnected()
    {
        Boolean result=Boolean.valueOf(getInfos(this.isCPConnected).getRootElement().getValue());
        if (result==null){
            return false;
        }
        return result.booleanValue();
    }
    
    public boolean isConnectedToP2PNetwork()
    {
        Boolean result=Boolean.valueOf(getInfos(this.isP2PNetworkConnected).getRootElement().getValue());
        if (result==null){
            return false;
        }
        return result.booleanValue();
        
    }
    
    public List getPageList(String id) throws XWootClientServletException{
        Document doc=this.getInfos(this.pageList+id);
        DOMOutputter domOutputter = new DOMOutputter();
        org.w3c.dom.Document documentDOM;
        try {
            documentDOM = domOutputter.output(doc);
        } catch (JDOMException e) {
           throw new XWootClientServletException(e);
        }
       
        if (documentDOM==null){
            return null;
        }
        if (documentDOM.getFirstChild()==null){
            return null;
        }
        
        if(documentDOM.getFirstChild().getChildNodes()==null){
            return null;
        }
        
        NodeList nodeList=documentDOM.getFirstChild().getChildNodes();
        
        List pageL=new ArrayList<String>();
        
        for (int i=0;i<nodeList.getLength();i++){
            if (nodeList.item(i).getNodeName().equals("page") && nodeList.item(i).getTextContent()!=null ){
                pageL.add(nodeList.item(i).getTextContent());
            }
        }       
        return pageL;  
    }
}
