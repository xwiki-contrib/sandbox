package org.xwiki.extension.index;

public interface ExtensionIndexer
{
    void lock();

    void unlock();

    void addExtension();

    void removeExtension();

    void clean();
}
