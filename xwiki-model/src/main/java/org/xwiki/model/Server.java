package org.xwiki.model;

import org.xwiki.model.reference.EntityReference;

import java.util.List;

/**
 * An XWiki Server is made of one or several {@link org.xwiki.model.Wiki}s. This is the top most
 * object of the XWiki Model.
 */
public interface Server extends Persistable
{
    /**
     * @return the list of all Wiki objects inside this Server
     */
    List<Wiki> getWikis();

    Wiki getWiki(String wikiName);

    Wiki addWiki(String wikiName);

    void removeWiki(String wikiName);

    boolean hasWiki(String wikiName);

    Entity getEntity(EntityReference reference);
    boolean hasEntity(EntityReference reference);
    void removeEntity(EntityReference reference);
}
