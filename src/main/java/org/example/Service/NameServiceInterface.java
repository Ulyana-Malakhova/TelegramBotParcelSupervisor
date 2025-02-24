package org.example.Service;

/**
 * Интерфейс для простых объектов-сущностей
 * @param <T> класс сущности
 */
public interface NameServiceInterface<T> {
    /**
     * Получение сущности по названию
     * @param name название
     * @return объект-сущность
     */
    T findByName(String name);
}
