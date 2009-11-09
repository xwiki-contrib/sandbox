package org.xwiki.model;

import java.util.List;

import javax.jcr.Workspace;

import org.xwiki.model.Space;

public interface Wiki extends Workspace
{
    /**
     * @return the list of all space names of this wiki including nested spaces
     */
    List<String> getSpaceNames();

    Space getSpace(String spaceName);

    Space createSpace(String spaceName);

    void addSpace(Space space);

    void removeSpace(String spaceName);
}
