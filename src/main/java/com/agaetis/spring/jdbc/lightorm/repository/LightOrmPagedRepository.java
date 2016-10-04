package com.agaetis.spring.jdbc.lightorm.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
public abstract class LightOrmPagedRepository<T, ID extends Serializable> extends LightOrmCrudRepository<T, ID> implements PagingAndSortingRepository<T, ID> {

    @Override
    public Page<T> findAll(Pageable pageable) {
        String sql = generator.select(getBeanMappingDescriptor(), pageable);

        List<T> results = getJdbcTemplate().query(sql, getRowMapper());

        return new PageImpl<T>(results, pageable, count());
    }

    @Override
    public Iterable<T> findAll(Sort sort) {
        String sql = generator.select(getBeanMappingDescriptor(), sort);

        return getJdbcTemplate().query(sql, getRowMapper());
    }
}
