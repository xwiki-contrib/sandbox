package org.xwiki.wikiimporter.importer;

import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

/**
 * Common import parameters.
 * 
 * @version $Id$
 */
public class WikiImportParameters
{
    /**
     * When not null all the imported pages should go in the targeted wiki. Otherwise it's importer choice.
     */
    private WikiReference wiki;

    /**
     * When not null all the imported pages should go in the targeted space. Otherwise it's importer choice.
     */
    private SpaceReference space;

    public SpaceReference getSpace()
    {
        return this.space;
    }

    public void setSpace(SpaceReference space)
    {
        this.space = space;
    }
}
