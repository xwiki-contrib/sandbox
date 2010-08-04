package org.xwiki.wikiimporter.importer;

import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyName;

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
    private String targetWiki;

    /**
     * When not null all the imported pages should go in the targeted space. Otherwise it's importer choice.
     */
    private String targetSpace;

    private boolean preserveHistory = true;

    @PropertyName("Target wiki")
    public String getTargetWiki()
    {
        return this.targetWiki;
    }

    public void setTargetWiki(String targetWiki)
    {
        this.targetWiki = targetWiki;
    }

    @PropertyName("Target space")
    public String getTargetSpace()
    {
        return this.targetSpace;
    }

    public void setTargetSpace(String targetSpace)
    {
        this.targetSpace = targetSpace;
    }

    /**
     * @return the preserveHistory
     */
    public boolean getPreserveHistory()
    {
        return preserveHistory;
    }

    /**
     * @param preserveHistory the preserveHistory to set
     */
    @PropertyName("Preserve history")
    @PropertyDescription("Select true to preserve history")
    public void setPreserveHistory(boolean preserveHistory)
    {
        this.preserveHistory = preserveHistory;
    }
}
