package org.xwiki.store.dao;

public interface GenericDao<I, E>
{
    E load(I id) throws Exception;

    void save(E entity) throws Exception;

    void delete(I id) throws Exception;
}
