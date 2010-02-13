package org.xwiki.model;

import org.xwiki.model.reference.DocumentReference;

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

    // Shouldn't we have: Wiki addWiki(String wikiName) or have Wiki createWiki(String wikiName) add the wiki?
    Wiki createWiki(String wikiName);
    void addWiki(Wiki wiki);

    void removeWiki(String wikiName);

    boolean hasWiki(String wikiName);

    // Q: Should the methods below be moved into some other class, such as a DocumentQuery component which would
    // be less generic than using the Query Manager? In JCR the ability to get an Entity from an absolute reference
    // is located in the Session class.
    // We also need a place where to store reference aliases.
    // I see 3 options:
    // - this Server class
    // - an EntityManager class
    // - an EntityReferenceManager class in charge of getting Entities from their references and in charge of
    //   setting reference aliases for entities too.

    Document getDocument(DocumentReference documentReference);
    boolean hasDocument(DocumentReference documentReference);
}
