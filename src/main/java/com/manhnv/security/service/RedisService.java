package com.manhnv.security.service;

public interface RedisService {
    void set(String key, Object value, long time);

    Object get(String key);

    Boolean del(String key);

    Boolean expire(String key, long time);
}
