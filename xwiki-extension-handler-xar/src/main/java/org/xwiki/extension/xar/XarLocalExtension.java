package org.xwiki.extension.xar;

import java.util.List;

import org.xwiki.extension.LocalExtension;

public interface XarLocalExtension extends LocalExtension
{
    List<LocalPage> getPages();
}
