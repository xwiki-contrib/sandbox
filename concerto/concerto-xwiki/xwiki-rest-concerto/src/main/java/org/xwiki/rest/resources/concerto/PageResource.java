package org.xwiki.rest.resources.concerto;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.velocity.VelocityContext;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xwiki.rest.XWikiResource;
import org.xwiki.velocity.VelocityManager;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Path("/pages/{pageId}")
public class PageResource extends XWikiResource
{
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Representation getPage(@PathParam("pageId") String pageId) throws XWikiException, IOException
    {
        if (pageId == null) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        if (pageId.indexOf('.') == -1) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        if (xwikiApi.exists(pageId)) {
            com.xpn.xwiki.api.Document doc = xwikiApi.getDocument(pageId);
            return buildRepresentation(doc);
        } else {
            throw new WebApplicationException(Status.GONE);
        }
    }

    @PUT
    @POST
    @Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Representation storePage(@PathParam("pageId") String pageId, String data) throws IOException, XWikiException
    {
        String title = null;
        String content = null;
        
        DomRepresentation entity = new DomRepresentation(new StringRepresentation(data, org.restlet.data.MediaType.APPLICATION_XML));        
        for (Node node : entity.getNodes("xwikipage/entries/entry/key")) {
            if (node.getTextContent().equals("title")) {
                Node valueNode = node.getNextSibling();
                if (valueNode != null) {
                    title = valueNode.getTextContent();
                }
            }

            if (node.getTextContent().equals("content")) {
                Node valueNode = node.getNextSibling();
                if (valueNode != null) {
                    content = valueNode.getTextContent();
                }
            }
        }
        
        /* Update or create document */
        com.xpn.xwiki.api.Document doc = xwikiApi.getDocument(pageId);
        if (doc != null) {
            if (!doc.getLocked()) {
                if (title != null || content != null) {
                    if (content != null) {
                        doc.setContent(content);
                    }

                    if (title != null) {
                        doc.setTitle(title);
                    }

                    doc.save();

                    Representation representation = buildRepresentation(doc);
                    
                    return representation;
                } else {
                    throw new WebApplicationException(Status.BAD_REQUEST);
                }
            } else {
                throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
            }
        } else {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }        
    }

    /**
     * <p>
     * Build an XML representation of an XWiki page
     * </p>
     * <p>
     * {@link http://concerto.xwiki.com/xwiki/bin/view/IWoot/REST_Spec}
     * </p>
     * 
     * @param doc The XWiki page document.
     * @return An XML representation of the XWiki page.
     * @throws IOException
     * @throws XWikiException
     */
    private Representation buildRepresentation(com.xpn.xwiki.api.Document doc) throws IOException, XWikiException
    {
        DomRepresentation representation = new DomRepresentation(org.restlet.data.MediaType.APPLICATION_XML);
        Document d = representation.getDocument();
        Element r = d.createElement("xwikipage");
        Element e = d.createElement("entries");

        e.appendChild(createEntry(d, "space", doc.getSpace()));
        e.appendChild(createEntry(d, "title", doc.getTitle()));
        e.appendChild(createEntry(d, "content", doc.getContent()));

        /* Render content */
        xwikiContext.setAction("view");
        XWikiDocument xwikiDocument = xwiki.getDocument(doc.getFullName(), xwikiContext);
        VelocityManager velocityManager = (VelocityManager) Utils.getComponent(VelocityManager.ROLE);
        VelocityContext vcontext = velocityManager.getVelocityContext();
        xwiki.prepareDocuments(xwikiContext.getRequest(), xwikiContext, vcontext);
        String renderedContent = xwiki.getRenderingEngine().renderText(doc.getContent(), xwikiDocument, xwikiContext);

        e.appendChild(createEntry(d, "renderedContent", renderedContent));

        r.appendChild(e);
        d.appendChild(r);

        return representation;
    }

    /**
     * <p>
     * Helper method for building the representation. Creates an XML entity "entry" containing two other entities "key"
     * and "value" as children.
     * </p>
     * <p>
     * {@link http://concerto.xwiki.com/xwiki/bin/view/IWoot/REST_Spec}
     * </p>
     * 
     * @param d The main XML document
     * @param key The content for the "key" entity.
     * @param value The content for the "value" entity.
     * @return
     */
    private Element createEntry(Document d, String key, String value)
    {
        Element e = d.createElement("entry");
        Element k = d.createElement("key");
        k.setTextContent(key);
        Element v = d.createElement("value");
        v.setTextContent(value);
        e.appendChild(k);
        e.appendChild(v);

        return e;
    }
}
