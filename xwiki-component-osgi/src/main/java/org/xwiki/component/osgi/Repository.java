package org.xwiki.component.osgi;

import java.net.URL;
import java.util.List;

public interface Repository
{
    List<URL> getModuleURLs();
}
