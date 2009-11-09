package org.xwiki.rest.resources.concerto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xwiki.rest.XWikiResource;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.plugin.watchlist.WatchListPlugin;

@Path("/pages")
public class PagesResource extends XWikiResource
{
    private static class PageInfo
    {
        String id;

        String href;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Representation getPages(@QueryParam("timestamp") @DefaultValue("0") Long timestamp) throws IOException, XWikiException
    {
        List<PageInfo> pages = getWatchedPages(timestamp);

        DomRepresentation representation = new DomRepresentation(org.restlet.data.MediaType.APPLICATION_XML);
        Document d = representation.getDocument();
        Element r = d.createElement("xwikipages");
        r.setAttribute("size", String.format("%d", pages.size()));

        for (PageInfo page : pages) {
            Element p = d.createElement("xwikipage");
            p.setAttribute("id", page.id);
            p.setAttribute("href", page.id);
            r.appendChild(p);
        }

        d.appendChild(r);

        return representation;
    }

    /**
     * Build watched pages list.
     * 
     * @return
     * @throws XWikiException 
     */
    public List<PageInfo> getWatchedPages(long timestamp) throws XWikiException
    {
        List<PageInfo> result = new ArrayList<PageInfo>();

        WatchListPlugin watchList = (WatchListPlugin) xwiki.getPlugin("watchlist", xwikiContext);
        List<String> watchedSpaces = watchList.getWatchedSpaces(xwikiUser, xwikiContext);

        for (String space : watchedSpaces) {
            /* Extract the space name */
            String[] components = space.split(":");
            if (components.length == 2) {
                space = components[1];
            } else {
                space = components[0];
            }

            List<String> pageIds = xwikiApi.getSpaceDocsName(space);
            for (String pageId : pageIds) {
                com.xpn.xwiki.api.Document doc = xwikiApi.getDocument(String.format("%s.%s", space, pageId));
                if (doc != null) {
                    if (doc.getDate().getTime() > timestamp) {
                        PageInfo page = new PageInfo();
                        page.id = doc.getFullName();
                        page.href = doc.getExternalURL("view");
                        if (!result.contains(page)) {
                            result.add(page);
                        }
                    }
                }
            }
        }

        List<String> watchedPagesIds = watchList.getWatchedDocuments(xwikiUser, xwikiContext);

        for (String pageId : watchedPagesIds) {
            com.xpn.xwiki.api.Document doc = xwikiApi.getDocument(pageId);
            if (doc != null) {
                PageInfo page = new PageInfo();
                page.id = doc.getFullName();
                page.href = doc.getExternalURL("view");
                if (!result.contains(page)) {
                    result.add(page);
                }
            }
        }

        return result;
    }
}
