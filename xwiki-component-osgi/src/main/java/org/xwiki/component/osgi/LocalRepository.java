package org.xwiki.component.osgi;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocalRepository implements Repository
{
    private File repositoryDirectory;

    public LocalRepository(File repositoryDirectory)
    {
        this.repositoryDirectory = repositoryDirectory;
    }

    public List<URL> getModuleURLs()
    {
        List<URL> moduleURLs = new ArrayList<URL>();

        FilenameFilter jarFilter = new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                if (name.endsWith(".jar")) {
                    return true;
                }
                return true;
            }
        };

        for (File module : this.repositoryDirectory.listFiles(jarFilter)) {
            try {
                moduleURLs.add(module.toURI().toURL());
            } catch (MalformedURLException e) {
                // This should never happen
                throw new RuntimeException("Failed to get URL for module [" + module + "]", e);
            }
        }

        return moduleURLs;
    }
}
