package com.agaetis.spring.jdbc.lightorm.sql;

import java.io.Serializable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.agaetis.spring.jdbc.lightorm.mapping.BeanMappingDescriptor;

public class SqlServer2012Generator extends BasicSqlGenerator {

    @Override
    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Pageable pageable) {
        StringBuilder sql = new StringBuilder(select(descriptor, pageable.getSort()));

        sql.append(" OFFSET ").append(pageable.getOffset()).append(" ROWS FECH NEXT ").append(pageable.getPageSize()).append(" ROWS ONLY");

        return sql.toString();
    }

    @Override
    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Sort sort) {
        StringBuilder sql = new StringBuilder(select(descriptor));

        if (sort != null) {
            sql.append(" ORDER BY");
            for (Order order : sort) {
                String property = order.getProperty();
                String column = getColumn(descriptor, property);
                sql.append(" ").append(column).append(" ").append(order.getDirection().name()).append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
        }

        return sql.toString();
    }
}
