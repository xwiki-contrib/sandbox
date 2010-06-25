package org.xwiki.model;

import java.util.List;

public interface Wiki extends Entity
{
    /**
     * @return the list of top level Space objects in this wiki (excluding nested spaces)
     */
    List<Space> getSpaces();

    /**
     * @param spaceName the name of the space
     * @return the object representing the space whose  name is passed in parameter
     */
    Space getSpace(String spaceName);

    Space addSpace(String spaceName);

    void removeSpace(String spaceName);

    boolean hasSpace(String spaceName);
}
