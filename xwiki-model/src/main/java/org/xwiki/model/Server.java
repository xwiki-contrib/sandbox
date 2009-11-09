package org.xwiki.model;

import java.util.List;

import javax.jcr.Repository;

import org.xwiki.model.Wiki;

/**
 * An XWiki Server is made of one or several {@link org.xwiki.model.Wiki}s. This is the top most
 * object of the XWiki Model.
 */
public interface Server extends Repository
{
    /**
     * @return the list of all wiki names inside this Server
     */
    List<String> getWikiNames();

    Wiki getWiki(String wikiName);

    Wiki createWiki(String wikiName);
    
    void addWiki(Wiki wiki);

    void removeWiki(String wikiName);
}
