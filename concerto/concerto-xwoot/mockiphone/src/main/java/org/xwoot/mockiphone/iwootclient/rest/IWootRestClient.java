package org.xwoot.mockiphone.iwootclient.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xwoot.mockiphone.iwootclient.IWootClient;
import org.xwoot.mockiphone.iwootclient.IWootClientException;

public class IWootRestClient implements IWootClient
{
    private Client client ; 
    
    static public String PAGESKEY="pages";

    private String uri;
    
    public IWootRestClient(String uri){
        this.uri=uri;
        this.client= new Client(Protocol.HTTP);   
    }
    
    //TODO voir pour mettre les uri en id de page 
    private Reference getResourceReference(String pageName) {
        Reference reference =null;
        // Create the resource reference
        if (pageName!=null&& !pageName.equals("")){
            reference = new Reference(this.uri + "/" + PAGESKEY + "/" + pageName);
        }
        else{
            reference = new Reference(this.uri + "/" + PAGESKEY);
        }
        return reference;
    }
    
    private Document getDocumentfromStream(InputStream is) throws IWootRestClientException{

        // création d'une fabrique de documents
        DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
        // création d'un constructeur de documents
        DocumentBuilder constructeur;       
        try {
            constructeur = fabrique.newDocumentBuilder();
            // get the document from the stream
            Document doc=constructeur.parse(is);

            return doc;
        } catch (ParserConfigurationException e) {
            throw new IWootRestClientException(e);
        } catch (SAXException e) {
            throw new IWootRestClientException(e);
        } catch (IOException e) {
            throw new IWootRestClientException(e);
        }

      
    }
    
    private Document getResource(Response response) throws IWootRestClientException{      
            try {
                 return this.getDocumentfromStream(response.getEntity().getStream());
            } catch (IOException e) {
                throw new IWootRestClientException(e);
            }

        
    }
    
//    private Serializable getResource(Response response) throws IWootRestClientException{
//        
//        //SUCCESS_OK => 200 resource found and return it in a entity-body
//        if (response.getStatus().equals(Status.SUCCESS_OK)){
//            if (!response.isEntityAvailable()){
//                throw new IWootRestClientException("Response status 200 (SUCCESS_OK) but no entity available");
//            }
//            try {
//            // get The resource object in the response entity
//                StreamRepresentation representation = new ObjectRepresentation<Serializable>(response.getEntity());
//                Serializable entity = ((ObjectRepresentation<Serializable>) representation).getObject();
//                return entity;
//            } catch (IOException e) {
//                throw new IWootRestClientException("Problem to get Object in flux",e);
//            } catch (IllegalArgumentException e) {
//                throw new IWootRestClientException("Problem to get Object in flux",e);
//            } catch (ClassNotFoundException e) {
//                throw new IWootRestClientException("Problem to get Object in flux",e);
//            }
//        }
//        //CLIENT_ERROR_NOT_FOUND => 404 page not found unknown uri
//        else if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)){
//            return null;
//        }
//        else {
//            throw new IWootRestClientException("Unexpected response status : "+response.getStatus());
//        }
//    }
  
    public boolean putPage(String pageName,Document page) throws IWootRestClientException{
        // Gathering informations into a XML document
       
        Representation rep2=new DomRepresentation(MediaType.APPLICATION_XML,page);
        Reference reference=this.getResourceReference(pageName);
        // Launch the request to create the resource
        Response response = this.client.put(reference, rep2);
        if (response.getStatus().equals(Status.SUCCESS_CREATED) || response.getStatus().equals(Status.SUCCESS_OK)){
            return true;
        }
        else if (response.getStatus().equals(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY) || response.getStatus().equals(Status.SERVER_ERROR_INTERNAL)){
            System.out.println(response.getStatus());
            return false;
        }
        else {
            throw new IWootRestClientException("Unexpected response status : "+response.getStatus());
        }
    }
    
    public Document getPage(String pageName) throws IWootRestClientException{
        Reference r=this.getResourceReference(pageName);
        Response response=this.client.get(r);
        
        return this.getResource(response);
    }
    
    public Document getPageList() throws IWootRestClientException{
        Reference r=this.getResourceReference(null);
        Response response=this.client.get(r);
        
        return this.getResource(response);
    }

    public String getUri() throws IWootClientException
    {
        return this.uri;
    }

    public boolean postPage(String pageName, Document page) throws IWootClientException
    {
        // Gathering informations into a XML document
        Representation rep2=new DomRepresentation(MediaType.APPLICATION_XML,page);
        Reference reference=this.getResourceReference(null);
        // Launch the request to create the resource
        Response response = this.client.post(reference, rep2);
        if (response.getStatus().equals(Status.SUCCESS_CREATED) || response.getStatus().equals(Status.SUCCESS_OK)){
            return true;
        }
        else if (response.getStatus().equals(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY) || response.getStatus().equals(Status.SERVER_ERROR_INTERNAL)){
            System.out.println(response.getStatus());
            return false;
        }
        else {
            throw new IWootRestClientException("Unexpected response status : "+response.getStatus());
        }
        
    }
}
