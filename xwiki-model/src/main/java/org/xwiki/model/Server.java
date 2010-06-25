package org.xwiki.model;

import org.xwiki.model.reference.DocumentReference;

import java.util.List;

/**
 * An XWiki Server is made of one or several {@link org.xwiki.model.Wiki}s. This is the top most
 * object of the XWiki Model.
 */
public interface Server extends Entity
{
    /**
     * @return the list of all Wiki objects inside this Server
     */
    List<Wiki> getWikis();

    Wiki getWiki(String wikiName);

    Wiki addWiki(String wikiName);

    void removeWiki(String wikiName);

    boolean hasWiki(String wikiName);

    // Q: Should the methods below be moved into some other class, such as a DocumentQuery component which would
    // be less generic than using the Query Manager? In JCR the ability to get a Node from an absolute reference
    // is located in the Session class.
    // We also need a place where to store reference aliases.
    // I see 3 options:
    // A- this Server class
    // B- an EntityManager class
    // C- an EntityReferenceManager class in charge of getting Entities from their references and in charge of
    //   setting reference aliases for entities too.
    //
    // A is nice because the user can use: $mydoc = $server.getEntity(EntityReference)
    // With A, it would use an EntityReferenceManager under the hood which would manage aliases

    Document getDocument(DocumentReference documentReference);
    boolean hasDocument(DocumentReference documentReference);
}
