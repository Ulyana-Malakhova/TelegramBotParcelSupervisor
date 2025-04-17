package org.example.Service;


import org.example.Dto.UserDto;
import org.example.Entity.User;

import java.util.List;

public interface ServiceInterface<T, K> {
    void save(T Dto) throws Exception;
    T get(Long id) throws Exception;
    List<T> toDto(List<K> entities);
}
