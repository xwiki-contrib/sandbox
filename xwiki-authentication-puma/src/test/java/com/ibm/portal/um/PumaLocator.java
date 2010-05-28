package com.ibm.portal.um;

import java.util.List;

public interface PumaLocator
{
    List<Group> findGroupsByPrincipal(User user, boolean recurse);
}
