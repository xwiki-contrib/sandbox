package org.xwiki.model;

public interface Persistable
{
    // Note: All the other methods don't save. Need to call save() to save. For ex this allows to add several docs
    // at once before saving them all at once. This allows for optimizations.
    // Q: Should we use a more generic API? Like pass a Map<String, String>?
    void save(String comment, boolean isMinorEdit);
}
