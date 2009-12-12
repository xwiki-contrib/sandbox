package org.xwiki.model;

import org.xwiki.bridge.DocumentName;

import java.util.List;

/**
 * An XWiki Server is made of one or several {@link org.xwiki.model.Wiki}s. This is the top most
 * object of the XWiki Model.
 */
public interface Server extends Persistable
{
    /**
     * @return the list of all wiki names inside this Server
     */
    List<String> getWikiNames();

    Wiki getWiki(String wikiName);

    Wiki createWiki(String wikiName);
    
    void addWiki(Wiki wiki);

    void removeWiki(String wikiName);

    boolean hasWiki(String wikiName);

    // Q: Should the methods below be moved into some other class, such as a DocumentQuery component which would
    // be less generic than using the Query Manager?

    // Q: Is this ok?
    Document getDocument(DocumentName documentName);

    // Should we also have getSpace(SpaceName spaceName)?

    // Q: Is this ok?
    boolean hasDocument(DocumentName documentName);
}
