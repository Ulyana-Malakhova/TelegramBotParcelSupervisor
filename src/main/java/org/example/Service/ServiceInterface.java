package org.example.Service;


public interface ServiceInterface<T> {
    void save(T Dto) throws Exception;
    T get(Long id);
}
