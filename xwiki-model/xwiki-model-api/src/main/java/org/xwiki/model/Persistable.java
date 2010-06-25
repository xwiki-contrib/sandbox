package org.xwiki.model;

import java.util.Map;

public interface Persistable
{
    // Note: All the other methods don't save. Need to call save() to save. For ex this allows to add several docs
    // at once before saving them all at once. This allows for optimizations.
    void save(String comment, boolean isMinorEdit, Map<String, String> extraParameters);
}
