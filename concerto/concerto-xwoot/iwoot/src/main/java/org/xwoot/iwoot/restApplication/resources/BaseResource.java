package org.xwoot.iwoot.restApplication.resources;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public abstract class BaseResource extends Resource
{
    //public static final String XML_DECLARATION = "<?xml version='1.0' encoding='UTF-8' ?>\n";


    public BaseResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    public Document getPage(Representation entity) throws ResourceException{
        // création d'une fabrique de documents
        DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();

        // création d'un constructeur de documents
        DocumentBuilder constructeur;
        try {
            constructeur = fabrique.newDocumentBuilder();
            // get the document from the stream
            Document doc=constructeur.parse(entity.getStream());
            return doc;
        } catch (ParserConfigurationException e) {
            throw new ResourceException(e);
        } catch (SAXException e) {
            throw new ResourceException(e);
        } catch (IOException e) {
            throw new ResourceException(e);

        }

        //        Map result=null;
        //        if(entity!=null){ 
        //            // get the page 
        //            Form form = new Form(entity);
        //            result=form.getValuesMap();
        //        }
        //        return result;
    }
}
