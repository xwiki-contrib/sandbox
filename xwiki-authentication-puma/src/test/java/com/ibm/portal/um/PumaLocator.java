package com.ibm.portal.um;

import java.util.List;

public interface PumaLocator
{
    List<Group> findGroupsByPrincipal(Principal principal, boolean recurse);
}
