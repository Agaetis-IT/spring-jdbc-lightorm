package com.agaetis.spring.jdbc.lightorm.sql;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.agaetis.spring.jdbc.lightorm.mapping.BeanMappingDescriptor;

public interface SqlGenerator {

    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor);

    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Collection<String> conditions);

    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Collection<String> conditions, Pageable pageable);

    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Pageable pageable);

    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Sort sort);

    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Collection<String> conditions, Sort sort);

    public <T, ID extends Serializable> String delete(BeanMappingDescriptor<T, ID> descriptor);

    public <T, ID extends Serializable> String delete(BeanMappingDescriptor<T, ID> descriptor, Collection<String> conditions);

    public <T, ID extends Serializable> String count(BeanMappingDescriptor<T, ID> descriptor);

    public <T, ID extends Serializable> String count(BeanMappingDescriptor<T, ID> descriptor, Collection<String> conditions);

    public <T, ID extends Serializable> String insert(BeanMappingDescriptor<T, ID> descriptor, Collection<String> columns, Collection<String> values);

    public <T, ID extends Serializable> String update(BeanMappingDescriptor<T, ID> descriptor, Collection<String> columns, Collection<String> conditions);

}