package org.xwiki.model;

import org.xwiki.model.reference.WikiReference;

import java.util.List;

public interface Wiki extends Persistable
{
    /**
     * @return the list of all space names in this wiki including nested spaces
     */
    List<String> getSpaceNames();

    /**
     * @param spaceName the name of the space
     * @return the object representing the space whose  name is passed in parameter
     */
    Space getSpace(String spaceName);

    Space createSpace(String spaceName);

    void addSpace(Space space);

    void removeSpace(String spaceName);

    boolean hasSpace(String spaceName);

    WikiReference getWikiReference();
}
