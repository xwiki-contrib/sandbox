package org.xwiki.extension.index.internal;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.xwiki.extension.index.ExtensionIndexer;

public class LuceneIndexer implements ExtensionIndexer
{
    private ReadWriteLock indexMaintenanceLock = new ReentrantReadWriteLock();
}
