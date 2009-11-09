package org.xwoot.iwoot.restApplication.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xwoot.iwoot.IWoot;
import org.xwoot.iwoot.IWootException;
import org.xwoot.iwoot.restApplication.RestApplication;
import org.xwoot.iwoot.xwootclient.XWootClientException;
import org.xwoot.iwoot.xwootclient.XWootClientFactory;
import org.xwoot.wikiContentManager.WikiContentManager;
import org.xwoot.wikiContentManager.WikiContentManagerException;
import org.xwoot.wikiContentManager.WikiContentManagerFactory;

public class PageResourceTest
{
    static private RestApplication RESTAPI;

    static private WikiContentManager WCM;

    static private Component COMPONENT;

    static private Client CLIENT;

    /** Base application URI. */
    private static final String APPLICATION_URI = "http://localhost:8182/iwoot";

    private static final String TESTSPACENAME = "SpaceTest";

    @BeforeClass
    static public void setup() throws WikiContentManagerException, XWootClientException
    {
        // Create a new WikiContentManager (resources container)
        WCM = WikiContentManagerFactory.getMockFactory().createWCM();

        // Create a new IWoot module (interface between IPhone and XWoot)
        IWoot iwoot = new IWoot(XWootClientFactory.getMockFactory().createXWootClient(WCM), WCM, Integer.valueOf(1));

        // Create a new REST api which use IWoot to access to resources
        RESTAPI = new RestApplication(iwoot);

        // Create new REstlet Component (Servers managements)
        COMPONENT = new Component();

        // Add a new HTTP server listening on port 8182.
        COMPONENT.getServers().add(Protocol.HTTP, 8182);

        // Attach the sample application.
        COMPONENT.getDefaultHost().attach("/iwoot", RESTAPI);

        // Start the component.
        try {
            COMPONENT.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Define an HTTP client.
        CLIENT = new Client(Protocol.HTTP);

    }

    @AfterClass
    static public void finalTearDown() throws Exception
    {
        // Stop the server
        COMPONENT.stop();
    }

    /**
     * 
     *  Test to get an existing page.
     * 
     * @throws WikiContentManagerException
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     * @throws ParserConfigurationException 
     * @throws SAXException 
     */
    @Test
    public void testGetPage() throws WikiContentManagerException, IOException, IllegalArgumentException,
    ClassNotFoundException, ParserConfigurationException, SAXException
    {
        String pageId=TESTSPACENAME+".page0";
        String pageContent="Content of existing page";

        // Create the resource reference
        Reference reference = new Reference(APPLICATION_URI + "/" + PagesResource.KEY + "/" + pageId);

        // Use client to get the wanted resource 
        Response response = CLIENT.get(reference);

        // Verify not found
        Assert.assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        // Create a new Page in resources container
        Map page = WCM.createPage(pageId, pageContent);

        // Use client to get the wanted resource
        response = CLIENT.get(reference);

        // Verify success
        Assert.assertEquals(Status.SUCCESS_OK, response.getStatus());
        // verify entity
        Assert.assertTrue(response.isEntityAvailable());
        //response.getEntity().write(System.out);

        Document doc=this.getDocumentfromStream(response.getEntity().getStream());

        // verify resource equality 

        //<xwikipage href="http://localhost:8182/iwoot/pages/SpaceTest.page0" id="SpaceTest.page0">
        //  <entries>
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_XWIKIPAGE,doc.getFirstChild().getNodeName());
        Assert.assertEquals(page.get(WikiContentManager.ID),doc.getDocumentElement().getAttribute(PageResource.KEY));
        Assert.assertEquals(reference.toString(),doc.getDocumentElement().getAttribute(WikiContentManager.XML_ATTRIBUTE_NAME_HREF));
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRIES,doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getNodeName());

        //      <entry><key>renderContent</key><value>Content of existing page</value></entry>
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY,    doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(0).getNodeName());
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY_KEY,      doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(0).getChildNodes().item(0).getNodeName());
        Assert.assertEquals(WikiContentManager.RENDERCONTENT,         doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(0).getChildNodes().item(0).getTextContent());
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY_VALUE,    doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(0).getChildNodes().item(1).getNodeName());
        Assert.assertEquals(pageContent,                        doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(0).getChildNodes().item(1).getTextContent());

        //      <entry><key>content</key><value>Content of existing page</value></entry>
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY,    doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(1).getNodeName());
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY_KEY,      doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeName());
        Assert.assertEquals(WikiContentManager.CONTENT,         doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(1).getChildNodes().item(0).getTextContent());
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY_VALUE,    doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(1).getChildNodes().item(1).getNodeName());
        Assert.assertEquals(pageContent,                        doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(1).getChildNodes().item(1).getTextContent());


        //      <entry><key>space</key><value>SpaceTest</value></entry>
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY,    doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(2).getNodeName());
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY_KEY,      doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(2).getChildNodes().item(0).getNodeName());
        Assert.assertEquals(WikiContentManager.SPACE,           doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(2).getChildNodes().item(0).getTextContent());
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY_VALUE,    doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(2).getChildNodes().item(1).getNodeName());
        Assert.assertEquals(TESTSPACENAME,                      doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(2).getChildNodes().item(1).getTextContent());

        //      <entry><key>title</key><value>page0</value></entry>
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY,    doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(3).getNodeName());
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY_KEY,      doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(3).getChildNodes().item(0).getNodeName());
        Assert.assertEquals(WikiContentManager.TITLE,           doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(3).getChildNodes().item(0).getTextContent());
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY_VALUE,    doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(3).getChildNodes().item(1).getNodeName());
        Assert.assertEquals("page0",                            doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(3).getChildNodes().item(1).getTextContent());

        //      <entry><key>id</key><value>SpaceTest.page0</value></entry>
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY,    doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(4).getNodeName());
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY_KEY,      doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(4).getChildNodes().item(0).getNodeName());
        Assert.assertEquals(WikiContentManager.ID,              doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(4).getChildNodes().item(0).getTextContent());
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_ENTRY_VALUE,    doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(4).getChildNodes().item(1).getNodeName());
        Assert.assertEquals(TESTSPACENAME+".page0",             doc.getDocumentElement().getElementsByTagName(WikiContentManager.XML_NODE_NAME_ENTRIES).item(0).getChildNodes().item(4).getChildNodes().item(1).getTextContent());
    }

    /**
     * Test to remove an existing page.
     * 
     * @throws WikiContentManagerException
     */
    @Test
    public void testRemoveRepresentations() throws WikiContentManagerException
    {
        String pageId=TESTSPACENAME+".test";
        String pageContent="Page to be deleted ! ";
        // Create the resource to remove
        if (!WCM.existPage(pageId)) {
            WCM.createPage(pageId, pageContent);
        }

        // create the resource reference
        Reference pageUri = new Reference(APPLICATION_URI + "/" + PagesResource.KEY + "/" + pageId);
        Request request = new Request(Method.DELETE, pageUri);

        // verify the resource exist
        Assert.assertEquals(pageContent, WCM.getPageContent(pageId));

        // Use client to delete resource
        Response resp = CLIENT.handle(request);

        // verify success
        Assert.assertEquals(Status.SUCCESS_NO_CONTENT, resp.getStatus());

        // verify resource is deleted
        Assert.assertFalse(WCM.existPage(pageId));

    }

    /**
     * Test to create a new page, and update it.
     * 
     * @throws WikiContentManagerException
     * @throws IWootException 
     * 
     */
    @Test
    public void testCreateAndSetPage() throws WikiContentManagerException, IWootException
    {
        String pageId=TESTSPACENAME+".test";
        String pageContent="Content of new page";
        String pageContent2="Modified Content of existing page";

        // The resource to create musn't exist
        if (WCM.existPage(pageId)) {
            WCM.removePage(pageId);
        }

        // verify the resource do not exist
        assertFalse(WCM.existPage(pageId));

        // Gathering informations into a XML document
        Map pageMap=new Hashtable<String, String>();

        pageMap.put(WikiContentManager.ID, pageId);
        pageMap.put(WikiContentManager.TITLE,"test");
        pageMap.put(WikiContentManager.SPACE,TESTSPACENAME);
        pageMap.put(WikiContentManager.CONTENT,pageContent);

        Document doc=WCM.toXml(pageId,APPLICATION_URI + "/" + PagesResource.KEY+"/"+pageId , pageMap);

        Representation rep=new DomRepresentation(MediaType.APPLICATION_XML,doc);
        // Launch the request to create the resource
        Response response = CLIENT.post(APPLICATION_URI + "/" + PagesResource.KEY,rep );

        // verify success
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // verify the resource exist
        assertEquals(pageContent, WCM.getPageContent(pageId));

        // get the URI of the created page
        Reference r=response.getEntity().getIdentifier();


        // Gathering informations into a XML document
        pageMap.remove(WikiContentManager.CONTENT);
        pageMap.put(WikiContentManager.CONTENT,pageContent2);
        doc=WCM.toXml(pageId,APPLICATION_URI + "/" + PagesResource.KEY+"/"+pageId , pageMap);
        Representation rep2=new DomRepresentation(MediaType.APPLICATION_XML,doc);

        // Launch the request to create the resource
        response = CLIENT.put(r, rep2);

        // verify success
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        // verify the resource exist
        assertEquals(pageContent2, WCM.getPageContent(pageId));
    }

    /**
     * Test to create a new page, and update it.
     * 
     * @throws WikiContentManagerException
     * @throws ClassNotFoundException 
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws IWootException 
     * 
     */
    @Test
    public void testGetPages() throws WikiContentManagerException, IllegalArgumentException, IOException, ClassNotFoundException, ParserConfigurationException, SAXException, IWootException
    {
        String pageId1=TESTSPACENAME+".page1";
        String pageId2=TESTSPACENAME+".page2";
        String pageContent1="Content of new page";
        String pageContent2="Content of newx page Blabla";

        WCM.removeSpace(TESTSPACENAME);

        /*****************/
        // Get empty list /
        /*****************/
        // Launch the request to get the resource list
        Response response = CLIENT.get(APPLICATION_URI + "/" + PagesResource.KEY);

        // get The list in the response entity
        //        StreamRepresentation representation = new ObjectRepresentation<Serializable>(response.getEntity());
        //        List pageTemp = (List) ((ObjectRepresentation<Serializable>) representation).getObject();
        //        assertEquals(0,pageTemp.size());

        Document doc=this.getDocumentfromStream(response.getEntity().getStream());
        // <?xml version="1.0" encoding="UTF-8"?>
        // <XWikiPageList size="0"/>
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_XWIKIPAGELIST,doc.getFirstChild().getNodeName());
        Assert.assertEquals(Integer.valueOf(0),Integer.valueOf(doc.getFirstChild().getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_LISTSIZE).getNodeValue()));

        // response.getEntity().write(System.out);

        /********************/
        // Create first page /
        /********************/

        // The resource to create musn't exist
        if (WCM.existPage(pageId1)) {
            WCM.removePage(pageId1);
        }

        // verify the resource do not exist
        assertFalse(WCM.existPage(pageId1));

        // Gathering informations into a XML document
        Map pageMap=new Hashtable<String, String>();

        pageMap.put(WikiContentManager.ID, pageId1);
        pageMap.put(WikiContentManager.TITLE,"page1");
        pageMap.put(WikiContentManager.SPACE,TESTSPACENAME);
        pageMap.put(WikiContentManager.CONTENT,pageContent1);

        doc=WCM.toXml(pageId1,APPLICATION_URI + "/" + PagesResource.KEY+"/"+pageId1 , pageMap);

        Representation rep = new DomRepresentation(MediaType.APPLICATION_XML,doc);

        // Launch the request to create the resource
        response = CLIENT.post(APPLICATION_URI + "/" + PagesResource.KEY, rep);

        // verify success
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // verify the resource exist
        assertEquals(pageContent1, WCM.getPageContent(pageId1));

        /***********************/
        // Get list with 1 page /
        /***********************/
        // Launch the request to get the resource list
        response = CLIENT.get(APPLICATION_URI + "/" + PagesResource.KEY);
        //response.getEntity().write(System.out);
        doc=this.getDocumentfromStream(response.getEntity().getStream());

        //<?xml version="1.0" encoding="UTF-8"?>
        //<XWikiPageList size="1">
        //  <xwikipage href="http://localhost:8182/iwoot/pages/SpaceTest.page1" id="SpaceTest.page1"/>
        //</XWikiPageList>

        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_XWIKIPAGELIST,doc.getFirstChild().getNodeName());
        Assert.assertEquals(Integer.valueOf(1),Integer.valueOf(doc.getFirstChild().getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_LISTSIZE).getNodeValue()));
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_XWIKIPAGE,doc.getFirstChild().getChildNodes().item(0).getNodeName());
        Assert.assertEquals(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID,doc.getFirstChild().getChildNodes().item(0).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID).getNodeName());
        Assert.assertEquals(pageId1,doc.getFirstChild().getChildNodes().item(0).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID).getTextContent());
        Assert.assertEquals(WikiContentManager.XML_ATTRIBUTE_NAME_HREF,doc.getFirstChild().getChildNodes().item(0).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_HREF).getNodeName());
        Assert.assertEquals(APPLICATION_URI + "/" + PagesResource.KEY + "/" +pageId1,doc.getFirstChild().getChildNodes().item(0).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_HREF).getTextContent());

        /********************/
        // Create a second page /
        /********************/

        // The resource to create musn't exist
        if (WCM.existPage(pageId2)) {
            WCM.removePage(pageId2);
        }

        // verify the resource do not exist
        assertFalse(WCM.existPage(pageId2));

        // Gathering informations into a XML document
        Map pageMap2=new Hashtable<String, String>();

        pageMap2.put(WikiContentManager.ID, pageId2);
        pageMap2.put(WikiContentManager.TITLE,"page2");
        pageMap2.put(WikiContentManager.SPACE,TESTSPACENAME);
        pageMap2.put(WikiContentManager.CONTENT,pageContent2);

        doc=WCM.toXml(pageId2,APPLICATION_URI + "/" + PagesResource.KEY+"/"+pageId2 , pageMap2);

        Representation rep2 = new DomRepresentation(MediaType.APPLICATION_XML,doc);

        // Launch the request to create the resource
        response = CLIENT.post(APPLICATION_URI + "/" + PagesResource.KEY, rep2);

        // verify success
        assertEquals(Status.SUCCESS_CREATED, response.getStatus());

        // verify the resource exist
        assertEquals(pageContent2, WCM.getPageContent(pageId2));

        /***********************/
        // Get list with 2 page /
        /***********************/
        // Launch the request to get the resource list
        response = CLIENT.get(APPLICATION_URI + "/" + PagesResource.KEY);
        //response.getEntity().write(System.out);
        doc=this.getDocumentfromStream(response.getEntity().getStream());
        //<?xml version="1.0" encoding="UTF-8"?>
        //<XWikiPageList size="2">
        //  <XWikiPage href="http://localhost:8182/iwoot/pages/SpaceTest.page2" id="SpaceTest.page2"/>
        //  <XWikiPage href="http://localhost:8182/iwoot/pages/SpaceTest.page1" id="SpaceTest.page1"/>
        //</XWikiPageList>
        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_XWIKIPAGELIST,doc.getFirstChild().getNodeName());
        Assert.assertEquals(Integer.valueOf(2),Integer.valueOf(doc.getFirstChild().getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_LISTSIZE).getNodeValue()));

        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_XWIKIPAGE,       doc.getFirstChild().getChildNodes().item(0).getNodeName());
        Assert.assertEquals(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID,doc.getFirstChild().getChildNodes().item(0).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID).getNodeName());
        Assert.assertEquals(pageId2,doc.getFirstChild().getChildNodes().item(0).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID).getTextContent());
        Assert.assertEquals(WikiContentManager.XML_ATTRIBUTE_NAME_HREF,doc.getFirstChild().getChildNodes().item(0).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_HREF).getNodeName());
        Assert.assertEquals(APPLICATION_URI + "/" + PagesResource.KEY + "/" +pageId2,doc.getFirstChild().getChildNodes().item(0).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_HREF).getTextContent());

        Assert.assertEquals(WikiContentManager.XML_NODE_NAME_XWIKIPAGE,       doc.getFirstChild().getChildNodes().item(1).getNodeName());
        Assert.assertEquals(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID,doc.getFirstChild().getChildNodes().item(1).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID).getNodeName());
        Assert.assertEquals(pageId1,                                    doc.getFirstChild().getChildNodes().item(1).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID).getTextContent());
        Assert.assertEquals(WikiContentManager.XML_ATTRIBUTE_NAME_HREF,       doc.getFirstChild().getChildNodes().item(1).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_HREF).getNodeName());
        Assert.assertEquals(APPLICATION_URI + "/" + PagesResource.KEY + "/" +pageId1,doc.getFirstChild().getChildNodes().item(1).getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_HREF).getTextContent());   
    }


    //    @Test
    //    public void testXTream() throws WikiContentManagerException {
    //        Map page=WCM.createPage("test.essai", "Dans ton cul lulu");
    //        XStream xstream = new XStream(new JettisonMappedXmlDriver());
    //        xstream.alias("page", Map.class);
    //        
    //        System.out.println(xstream.toXML(page));     
    //    }
    //    

    private Document getDocumentfromStream(InputStream is) throws ParserConfigurationException, SAXException, IOException{

        // création d'une fabrique de documents
        DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();

        // création d'un constructeur de documents
        DocumentBuilder constructeur = fabrique.newDocumentBuilder();

        // get the document from the stream
        Document doc=constructeur.parse(is);

        return doc;
    }

}
