package org.xwiki.component.osgi;

import java.net.URL;
import java.util.List;

public interface ModuleRepository
{
    List<URL> getModuleURLs();
}
