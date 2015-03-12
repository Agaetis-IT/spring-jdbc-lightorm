package com.agaetis.spring.jdbc.lightorm.repository;

import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a href="http://www.agaetis.fr">Agaetis</a>  on 12/03/2015.
 */
public abstract class LightOrmPagedRepository<T> extends LightOrmCrudRepository<T>{

    public List<T> findAll(Pageable pageable) {
        return getJdbcTemplate().query("select * from "
                + getBeanMappingDescriptor().getEscapedTableName()
                + " offset " + pageable.getPageNumber() + " limit " + pageable.getPageSize(),
                getRowMapper());
    }
}
