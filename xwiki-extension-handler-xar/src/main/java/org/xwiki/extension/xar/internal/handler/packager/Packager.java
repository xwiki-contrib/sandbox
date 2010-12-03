package org.xwiki.extension.xar.internal.handler.packager;

import java.io.File;
import java.io.IOException;

public interface Packager
{
    ImportResult importXAR(File xarFile, String wiki) throws IOException;
}
