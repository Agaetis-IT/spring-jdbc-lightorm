package com.agaetis.spring.jdbc.lightorm.sql;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.NullHandling;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mapping.PropertyPath;

import com.agaetis.spring.jdbc.lightorm.mapping.BeanMappingDescriptor;

public class BasicSqlGenerator implements SqlGenerator {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#select(java.lang.String
     * )
     */
    @Override
    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor) {
        return String.format("SELECT * FROM %s", descriptor.getEscapedTableName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#select(java.lang.String
     * , java.util.Collection)
     */
    @Override
    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Collection<String> conditions) {
        return String.format("SELECT * FROM %s WHERE %s", descriptor.getEscapedTableName(), StringUtils.join(conditions, " AND "));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#select(java.lang.String
     * , org.springframework.data.domain.Pageable)
     */
    @Override
    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Pageable pageable) {
        StringBuilder sql = new StringBuilder(select(descriptor, pageable.getSort()));

        pageable(pageable, sql);

        return sql.toString();
    }

    protected void pageable(Pageable pageable, StringBuilder sql) {
        sql.append(" LIMIT ").append(pageable.getPageSize()).append(" OFFSET ").append(pageable.getOffset());
    }

    @Override
    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Collection<String> conditions, Pageable pageable) {
        StringBuilder sql = new StringBuilder(select(descriptor, conditions, pageable.getSort()));

        pageable(pageable, sql);

        return sql.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#select(java.lang.String
     * , org.springframework.data.domain.Sort)
     */
    @Override
    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Sort sort) {
        StringBuilder sql = new StringBuilder(select(descriptor));

        sort(descriptor, sort, sql);

        return sql.toString();
    }

    protected <T, ID extends Serializable> void sort(BeanMappingDescriptor<T, ID> descriptor, Sort sort, StringBuilder sql) {
        if (sort != null) {
            sql.append(" ORDER BY");
            for (Order order : sort) {
                String column = getColumn(descriptor, order.getProperty());
                sql.append(" ").append(column).append(" ").append(order.getDirection().name());
                NullHandling nulls = order.getNullHandling();
                if (nulls != null) {
                    switch (nulls) {
                    case NULLS_FIRST:
                        sql.append(" NULLS FIRST ");
                        break;
                    case NULLS_LAST:
                        sql.append(" NULLS LAST ");
                        break;
                    default:
                    }
                }
                sql.append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
        }
    }

    @Override
    public <T, ID extends Serializable> String select(BeanMappingDescriptor<T, ID> descriptor, Collection<String> conditions, Sort sort) {
        StringBuilder sql = new StringBuilder(select(descriptor, conditions));

        sort(descriptor, sort, sql);

        return sql.toString();
    }

    protected <T, ID extends Serializable> String getColumn(BeanMappingDescriptor<T, ID> descriptor, String property) {
        PropertyPath path = PropertyPath.from(property, descriptor.getDomainClass());
        return descriptor.getPaths().get(path).getColumnName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#delete(java.lang.String
     * )
     */
    @Override
    public <T, ID extends Serializable> String delete(BeanMappingDescriptor<T, ID> descriptor) {
        return String.format("DELETE FROM %s", descriptor.getEscapedTableName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#delete(java.lang.String
     * , java.util.Collection)
     */
    @Override
    public <T, ID extends Serializable> String delete(BeanMappingDescriptor<T, ID> descriptor, Collection<String> conditions) {
        return String.format("DELETE FROM %s WHERE %s", descriptor.getEscapedTableName(), StringUtils.join(conditions, " AND "));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#count(java.lang.String)
     */
    @Override
    public <T, ID extends Serializable> String count(BeanMappingDescriptor<T, ID> descriptor) {
        return String.format("SELECT COUNT(*) FROM %s", descriptor.getEscapedTableName());
    }

    @Override
    public <T, ID extends Serializable> String count(BeanMappingDescriptor<T, ID> descriptor, Collection<String> conditions) {
        return String.format("SELECT COUNT(*) FROM %s WHERE %s", descriptor.getEscapedTableName(), StringUtils.join(conditions, " AND "));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#insert(java.lang.String
     * , java.util.Collection, java.util.Collection)
     */
    @Override
    public <T, ID extends Serializable> String insert(BeanMappingDescriptor<T, ID> descriptor, Collection<String> columns, Collection<String> values) {
        return String.format("INSERT INTO %s (%s) VALUES (%s)", descriptor.getEscapedTableName(), StringUtils.join(columns, ","), StringUtils.join(values, ","));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator#update(java.lang.String
     * , java.util.Collection, java.util.Collection)
     */
    @Override
    public <T, ID extends Serializable> String update(BeanMappingDescriptor<T, ID> descriptor, Collection<String> columns, Collection<String> conditions) {
        return String.format("UPDATE %s SET %s WHERE %s", descriptor.getEscapedTableName(), StringUtils.join(columns, ","), StringUtils.join(conditions, " AND "));
    }

}
