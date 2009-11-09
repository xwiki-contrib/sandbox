package org.xwoot.iwoot.restApplication;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.MediaType;
import org.w3c.dom.Document;
import org.xwoot.iwoot.IWoot;
import org.xwoot.iwoot.IWootException;
import org.xwoot.iwoot.restApplication.resources.PagesResource;
import org.xwoot.iwoot.restApplication.resources.PageResource;

public class RestApplication extends Application
{   

    private IWoot iwoot;
    public final static MediaType USINGMEDIATYPE=MediaType.APPLICATION_XML;


    public RestApplication() {
        super();
    }
    
    public RestApplication(Context context)
    {
        super(context);
        IWoot iw=(IWoot)context.getAttributes().get("iwoot");
        this.iwoot=iw;
    }

    public RestApplication(IWoot iwoot) {
        super();
        this.iwoot=iwoot;

    }

    public IWoot getIwoot()
    {
        return this.iwoot;
    }
    public void setIwoot(IWoot iwoot)
    {
        this.iwoot = iwoot;
    }

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createRoot() {
        // Create a router Restlet that defines routes.
        Router router = new Router(getContext());

        // Defines a route for the resource "list of pages"
        router.attach("/"+PagesResource.KEY, PagesResource.class);
        // Defines a route for the page resource : /iwoot/pages/{id} 
        // {id} is a generic value for the pagename like : /iwoot/pages/space1.page1
        router.attach("/"+PagesResource.KEY+"/{"+PageResource.KEY+"}", PageResource.class);

        return router;
    }


    /**
     * Return the page id.
     *
     * @param id : the id of the wanted page
     * @return the page id.
     */
    public Document getPage(String id, String href) throws IWootException{
        return this.iwoot.getPage(id,href);
    }

    /**
     * Remove the page id.
     *
     * @param id : the id of the page to remove
     * @return boolean
     */
    public boolean removePage(String id) throws IWootException {
        return this.iwoot.removepage(id);
    }

    /**
     * Store the page .
     *
     * @param document : the page to store
     * @return boolean
     */
    public boolean storePage(String id,Document document) throws IWootException{
        return this.iwoot.storePage(id,document);
    }

    public boolean existPage(Document page) throws IWootException
    {
        return this.iwoot.existPage(page);
    }

    public boolean createPage(Document newPage) throws IWootException
    {
        return this.iwoot.createPage(newPage);
    }
    public Document getPageList(String pagesHRef) throws IWootException
    {
        return this.iwoot.getPageList(pagesHRef);
    }

    public String getPageId(Document document) throws IWootException
    {
        return this.iwoot.getPageId(document);
    }

}
