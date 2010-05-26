package org.xwiki.extension;

import java.io.File;

public interface LocalExtension extends Extension
{
    File getFile();

    /**
     * @return true if the the extension has been installed only because it was a dependency of another extension
     *         installer by user
     */
    boolean isDependency();
}
