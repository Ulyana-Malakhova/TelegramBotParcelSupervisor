package org.example.Service;


public interface ServiceInterface<T> {
    void save(T Dto);
    T get(Long id);
}
