package org.xwoot.iwoot.restApplication.resources;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.xwoot.iwoot.IWootException;
import org.xwoot.iwoot.restApplication.RestApplication;


public class PagesResource extends BaseResource
{
    /** List of items. */
    Document pageList;

    public final static String KEY="pages";

    public PagesResource(Context context, Request request, Response response) {
        super(context, request, response);

        // Get the items directly from the "persistence layer".

        try {
            this.pageList = ((RestApplication)getApplication()).getPageList(this.getRequest().getOriginalRef().toString());
        } catch (IWootException e) {
            e.printStackTrace();
            this.pageList = null;
        }

        // modifications of this resource via POST requests are not allow 
        setModifiable(true);

        // Declare the kind of representations supported by this resource.
        getVariants().add(new Variant(RestApplication.USINGMEDIATYPE));
    }

    /**
     * Returns a listing of all registered items.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (this.pageList!=null){
            return new DomRepresentation(MediaType.APPLICATION_XML,this.pageList);
        }   
        return null;
    }

    /**
     * Handle POST requests: create a new page.
     * 
     *  OK => SUCCESS_CREATED (HTTP RFC - 10.2.2 201 Created)
     *  
     *  ALREADY EXIST => CLIENT_ERROR_CONFLICT (HTTP RFC - 10.4.10 409 Conflict)
     *  ERROR DURING CREATION (missing parameters) => CLIENT_ERROR_UNPROCESSABLE_ENTITY (WEBDAV RFC - 10.3 422 Unprocessable Entity)
     *  PROBLEM DURING CREATION (catch exception) => SERVER_ERROR_INTERNAL (HTTP RFC - 10.5.1 500 Internal Server Error)
     *  
     */
    @Override
    public void acceptRepresentation(Representation entity)
    throws ResourceException {
        DomRepresentation rep=null;
        Document document=null;
        if (entity.getMediaType().equals(MediaType.APPLICATION_XML)){
             try {
                 rep=new DomRepresentation(entity);
                 document= rep.getDocument();
            } catch (IOException e) {
              throw new ResourceException(e);
            }
        }
        else{
            getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
            return ;
        }
       // String pageId=document.getFirstChild().getAttributes().getNamedItem(WikiContentManager.XML_ATTRIBUTE_NAME_XWIKIPAGEID).getTextContent();
        try{
            // Check that the item is not already registered.
            if (((RestApplication)getApplication()).existPage(document)) {
                getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
            }else {
                if (!((RestApplication)getApplication()).createPage(document)){
                    getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
                }
            }
        }catch(IWootException e){
            e.printStackTrace();
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        // Set the response's status and entity
        getResponse().setStatus(Status.SUCCESS_CREATED);
        // Indicates where is located the new resource.
       
        try {
            rep.setIdentifier(getRequest().getResourceRef().getIdentifier()
                + "/" + ((RestApplication)getApplication()).getPageId(document));
        } catch (IWootException e) {
            e.printStackTrace();
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        getResponse().setEntity(rep);
    }
}
