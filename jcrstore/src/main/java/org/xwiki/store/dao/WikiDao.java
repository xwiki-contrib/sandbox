package org.xwiki.store.dao;


public interface WikiDao
{
    boolean exist(String wiki);

    void create(String wiki);

    void delete(String wiki);
}
