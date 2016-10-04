package com.agaetis.spring.jdbc.lightorm.sql;

import java.io.Serializable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.agaetis.spring.jdbc.lightorm.mapping.BeanMappingDescriptor;

public class SqlServer2012Generator extends BasicSqlGenerator {

    @Override
    protected <T, ID extends Serializable> void sort(BeanMappingDescriptor<T, ID> descriptor, Sort sort, StringBuilder sql) {
        if (sort != null) {
            sql.append(" ORDER BY");
            for (Order order : sort) {
                String property = order.getProperty();
                String column = getColumn(descriptor, property);
                sql.append(" ").append(column).append(" ").append(order.getDirection().name()).append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
        }
    }

    @Override
    protected void pageable(Pageable pageable, StringBuilder sql) {
        sql.append(" OFFSET ").append(pageable.getOffset()).append(" ROWS FECH NEXT ").append(pageable.getPageSize()).append(" ROWS ONLY");
    }

}
