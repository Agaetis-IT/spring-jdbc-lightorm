package com.agaetis.spring.jdbc.lightorm.repository;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.GenericTypeResolver;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.Assert;

import com.agaetis.spring.jdbc.lightorm.annotation.LazyLoading;
import com.agaetis.spring.jdbc.lightorm.mapping.BeanMappingDescriptor;
import com.agaetis.spring.jdbc.lightorm.mapping.ColumnMappingDescriptor;
import com.agaetis.spring.jdbc.lightorm.mapping.IdMappingDescriptor;
import com.agaetis.spring.jdbc.lightorm.rowmapper.AnnotatedBeanPropertyRowMapper;
import com.agaetis.spring.jdbc.lightorm.sql.SqlGenerator;

/**
 * @author <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 *         href="http://www.agaetis.fr">Agaetis</a> Created on 12/03/2015.
 */
public abstract class LightOrmCrudRepository<T, ID extends Serializable> implements CrudRepository<T, ID> {

    protected final Logger               logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataSource                   dataSource;

    private JdbcTemplate                 jdbcTemplate;

    private NamedParameterJdbcTemplate   namedParameterJdbcTemplate;

    private BeanMappingDescriptor<T, ID> beanMappingDescriptor;

    @Value("${lightorm.datasource.escapedcharacter}")
    private String                       escapedCharacter;

    private RowMapper<T>                 defaultRowMapper;

    private Class<T>                     domainClass;

    @Autowired
    protected SqlGenerator               generator;

    public Class<T> getDomainClass() {
        return domainClass;
    }

    @SuppressWarnings("unchecked")
    public LightOrmCrudRepository() {
        domainClass = (Class<T>) GenericTypeResolver.resolveTypeArguments(getClass(), LightOrmCrudRepository.class)[0];
        RegisteredDao.registerDao(this);
    }

    protected BeanMappingDescriptor<T, ID> getBeanMappingDescriptor() {
        return beanMappingDescriptor;
    }

    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    @PostConstruct
    private void postConstruct() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        beanMappingDescriptor = new BeanMappingDescriptor<T, ID>(getDomainClass(), escapedCharacter);

        defaultRowMapper = AnnotatedBeanPropertyRowMapper.newInstance(beanMappingDescriptor);
    }

    protected RowMapper<T> getRowMapper() {
        return defaultRowMapper;
    }

    @Override
    public <S extends T> S save(S entity) {

        if (isNew(entity)) {
            return create(entity);
        }

        if (findOne(getId(entity)) == null) {
            return create(entity);
        }

        update(entity);

        return entity;
    }

    @SuppressWarnings("unchecked")
    private <S extends T> ID getId(S entity) {
        IdMappingDescriptor<ID> idDescriptor = beanMappingDescriptor.getIdMappingDescriptor();

        return (ID) idDescriptor.readFieldValue(entity);
    }

    private <S extends T> boolean isNew(S entity) {
        IdMappingDescriptor<ID> idDescriptor = beanMappingDescriptor.getIdMappingDescriptor();
        Object id = idDescriptor.readFieldValue(entity);

        if (id == null) {
            return true;
        }

        if (!idDescriptor.isComposite()) {
            if (Number.class.isAssignableFrom(id.getClass()) && id.equals(0)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean exists(ID id) {
        Assert.notNull(id, "The given id must not be null!");

        return findOne(id) != null;
    }

    @Override
    public long count() {
        String sql = generator.count(beanMappingDescriptor);
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    @Override
    public <S extends T> Iterable<S> save(Iterable<S> entities) {
        List<S> result = new ArrayList<S>();

        if (entities == null) {
            return result;
        }

        for (S entity : entities) {
            result.add(save(entity));
        }

        return result;
    }

    private void createWithKeyHolder(T obj, String sql, MapSqlParameterSource parameterSource) {
        IdMappingDescriptor<ID> idDescriptor = beanMappingDescriptor.getIdMappingDescriptor();

        ColumnMappingDescriptor columnDescriptor = idDescriptor.getColumns().get(0);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, parameterSource, keyHolder, new String[] { columnDescriptor.getColumnName() });

        Field field = columnDescriptor.getField();

        if (field.getType().equals(Integer.class)) {
            columnDescriptor.writeFieldValue(obj, keyHolder.getKey().intValue());
        } else if (field.getType().equals(Long.class)) {
            columnDescriptor.writeFieldValue(obj, keyHolder.getKey().longValue());
        } else if (field.getType().equals(Byte.class)) {
            columnDescriptor.writeFieldValue(obj, keyHolder.getKey().byteValue());
        } else if (field.getType().equals(Double.class)) {
            columnDescriptor.writeFieldValue(obj, keyHolder.getKey().doubleValue());
        } else if (field.getType().equals(Float.class)) {
            columnDescriptor.writeFieldValue(obj, keyHolder.getKey().floatValue());
        } else if (field.getType().equals(Short.class)) {
            columnDescriptor.writeFieldValue(obj, keyHolder.getKey().shortValue());
        } else {
            throw new InvalidDataAccessApiUsageException("Auto-incremented field [" + field.getName() + "] of type [" + field.getType().getCanonicalName() + "] not managed.");
        }
    }

    private MapSqlParameterSource buildParameterSource(Map<String, Object> parameters) {

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        for (Entry<String, Object> parameter : parameters.entrySet()) {
            String key = parameter.getKey();
            Object value = parameter.getValue();

            if (value == null) {
                parameterSource.addValue(key, value);
            } else if (value.getClass().isEnum()) {
                parameterSource.addValue(key, value, Types.VARCHAR);
            } else {
                parameterSource.addValue(key, value);
            }
        }
        return parameterSource;
    }

    protected <S extends T> S create(S obj) {
        Map<String, Object> fieldIdParams = extractIdParams(obj);
        Map<String, Object> fieldParams = extractColmunParams(obj);

        // On vérifie que les clés primaires soient saisies si on est pas sur
        // une clé auto incrémentée
        boolean autoIncremented = beanMappingDescriptor.getIdMappingDescriptor().isAutoIncremented();

        if (!autoIncremented) {
            for (Object value : fieldIdParams.values()) {
                if (value == null) {
                    throw new InvalidDataAccessApiUsageException("Unable to create entity with not auto incremented id and null value");
                }
            }
        }

        Map<String, Object> allParams = new HashMap<String, Object>(fieldIdParams.size() + fieldParams.size());
        allParams.putAll(fieldIdParams);
        allParams.putAll(fieldParams);

        // Création de la liste des champs à mettre à jour
        List<String> updatedValueFields = new ArrayList<String>(allParams.size());
        List<String> updatedFields = new ArrayList<String>(allParams.size());

        for (ColumnMappingDescriptor field : beanMappingDescriptor.getColumnsMappingDescriptors()) {
            // création des conditions
            updatedFields.add(field.getEscapedColumnName());
            updatedValueFields.add(":" + field.getColumnName());
        }

        // La clé n'est pas generee automatiquement
        if (!autoIncremented) {
            for (ColumnMappingDescriptor field : beanMappingDescriptor.getIdMappingDescriptor()) {
                // création des conditions
                updatedFields.add(field.getEscapedColumnName());
                updatedValueFields.add(":" + field.getColumnName());
            }
        }

        String sql = generator.insert(beanMappingDescriptor, updatedFields, updatedValueFields);

        MapSqlParameterSource parameterSource = buildParameterSource(allParams);

        if (autoIncremented) {
            createWithKeyHolder(obj, sql, parameterSource);
        } else {
            namedParameterJdbcTemplate.update(sql, parameterSource);
        }
        return obj;
    }

    protected void update(T obj) {
        Map<String, Object> fieldIdParams = extractIdParams(obj);
        Map<String, Object> fieldParams = extractColmunParams(obj);

        // On vérifie que les clés primaires soient saisies
        for (Object value : fieldIdParams.values()) {
            if (value == null) {
                throw new InvalidDataAccessApiUsageException("Impossible de mettre à jour un objet sans clé primaire renseignée");
            }
        }

        Map<String, Object> allParams = new HashMap<String, Object>(fieldIdParams.size() + fieldParams.size());
        allParams.putAll(fieldIdParams);
        allParams.putAll(fieldParams);

        // Création des conditions sur la clé primaire
        List<String> sqlConditions = new ArrayList<String>(fieldIdParams.size());

        for (ColumnMappingDescriptor fieldId : beanMappingDescriptor.getIdMappingDescriptor()) {
            // création des conditions
            sqlConditions.add(fieldId.getEscapedColumnName() + "=:" + fieldId.getColumnName());
        }

        // Création de la liste des champs à mettre à jour
        List<String> updatedFields = new ArrayList<String>(fieldParams.size());
        for (ColumnMappingDescriptor field : beanMappingDescriptor.getColumnsMappingDescriptors()) {
            // création des conditions
            updatedFields.add(field.getEscapedColumnName() + "=:" + field.getColumnName());
        }

        String sql = generator.update(beanMappingDescriptor, updatedFields, sqlConditions);

        MapSqlParameterSource parameterSource = buildParameterSource(allParams);

        namedParameterJdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public List<T> findAll() {
        String sql = generator.select(beanMappingDescriptor);
        return jdbcTemplate.query(sql, getRowMapper());
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids) {
        if ((ids == null) || !ids.iterator().hasNext()) {
            return Collections.emptyList();
        }

        List<T> results = new ArrayList<T>();

        for (ID id : ids) {
            results.add(findOne(id));
        }

        return results;
    }

    @Override
    public T findOne(ID id) {
        List<String> sqlConditions = new ArrayList<String>(beanMappingDescriptor.getIdMappingDescriptor().size());

        for (ColumnMappingDescriptor fieldId : beanMappingDescriptor.getIdMappingDescriptor()) {
            // création des conditions
            sqlConditions.add(fieldId.getEscapedColumnName() + "=:" + fieldId.getColumnName());
        }

        String sql = generator.select(beanMappingDescriptor, sqlConditions);

        Map<String, Object> params = extractIdParams(id);

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, params, getRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public T findOne(ID id, String... properties) {

        T result = findOne(id);
        if (result == null) {
            return null;
        }

        try {
            loadLazyProperties(result, properties);
        } catch (EmptyResultDataAccessException e) {
            logger.warn("Unable to load properties [" + properties.toString() + "] for [" + domainClass.getCanonicalName() + " - " + id + "]");
        }

        return result;
    }

    @Override
    public void delete(ID id) {
        Map<String, Object> params = extractIdParams(id);

        // On vérifie que les clés primaires soient saisies
        for (Object value : params.values()) {
            if (value == null) {
                throw new InvalidDataAccessApiUsageException("Impossible de supprimer un objet sans clé primaire renseignée");
            }
        }

        List<String> sqlConditions = new ArrayList<String>(beanMappingDescriptor.getIdMappingDescriptor().size());

        for (ColumnMappingDescriptor fieldId : beanMappingDescriptor.getIdMappingDescriptor()) {
            // création des conditions
            sqlConditions.add(fieldId.getEscapedColumnName() + "=:" + fieldId.getColumnName());
        }
        String sql = generator.delete(beanMappingDescriptor, sqlConditions);

        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public void delete(T obj) {
        Map<String, Object> params = extractIdParams(obj);

        // On vérifie que les clés primaires soient saisies
        for (Object value : params.values()) {
            if (value == null) {
                throw new InvalidDataAccessApiUsageException("Impossible de supprimer un objet sans clé primaire renseignée");
            }
        }

        List<String> sqlConditions = new ArrayList<String>(beanMappingDescriptor.getIdMappingDescriptor().size());

        for (ColumnMappingDescriptor fieldId : beanMappingDescriptor.getIdMappingDescriptor()) {
            // création des conditions
            sqlConditions.add(fieldId.getEscapedColumnName() + "=:" + fieldId.getColumnName());
        }

        String sql = generator.delete(beanMappingDescriptor, sqlConditions);

        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");

        for (T entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        String sql = generator.delete(beanMappingDescriptor);
        jdbcTemplate.update(sql);
    }

    protected Map<String, Object> extractIdParams(T obj) {
        IdMappingDescriptor<ID> descriptor = beanMappingDescriptor.getIdMappingDescriptor();

        Map<String, Object> params = new HashMap<String, Object>(descriptor.getColumns().size());

        if (!descriptor.isComposite()) {
            for (ColumnMappingDescriptor column : descriptor) {
                Object value = column.readFieldValue(obj);
                params.put(column.getColumnName(), value);
            }
        } else {
            ID id = descriptor.getIdClass().cast(descriptor.readFieldValue(obj));
            return extractIdParams(id);
        }
        return params;
    }

    protected Map<String, Object> extractIdParams(ID id) {
        IdMappingDescriptor<ID> descriptor = beanMappingDescriptor.getIdMappingDescriptor();

        Map<String, Object> params = new HashMap<String, Object>(descriptor.getColumns().size());

        if (descriptor.isComposite()) {
            for (ColumnMappingDescriptor column : descriptor) {
                Object value = column.readFieldValue(id);
                params.put(column.getColumnName(), value);
            }
        } else {
            params.put(descriptor.getColumns().get(0).getColumnName(), id);
        }
        return params;
    }

    protected Map<String, Object> extractColmunParams(T obj) {
        Map<String, Object> params = new HashMap<String, Object>(beanMappingDescriptor.getColumnsMappingDescriptors().size());

        for (ColumnMappingDescriptor column : beanMappingDescriptor.getColumnsMappingDescriptors()) {
            Object value = column.readFieldValue(obj);
            params.put(column.getColumnName(), value);
        }

        return params;
    }

    public void loadLazyProperties(T obj, String[] properties) {
        if (properties.length == 0) {
            return;
        }

        for (String property : properties) {
            Class<T> tableClass = getDomainClass();
            try {
                Field fieldToLoad = tableClass.getDeclaredField(property);
                LazyLoading lazyLoading = fieldToLoad.getAnnotation(LazyLoading.class);
                if (lazyLoading == null) {
                    throw new InvalidDataAccessApiUsageException("LazyLoading annotation not found on field [" + property + "], Table Class [" + getDomainClass().getName() + "]");
                }

                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(tableClass, lazyLoading.value());

                @SuppressWarnings("unchecked")
                LightOrmCrudRepository<Object, Serializable> dao = (LightOrmCrudRepository<Object, Serializable>) RegisteredDao.getDao(fieldToLoad.getType());
                if (dao == null) {
                    throw new InvalidDataAccessApiUsageException("Dao not found for Table Class [" + getDomainClass().getName() + "]");
                }

                Serializable id = (Serializable) pd.getReadMethod().invoke(obj);
                if (id == null) {
                    return;
                }

                Object objectLoaded = dao.findOne(id);
                PropertyDescriptor pdToLoad = BeanUtils.getPropertyDescriptor(tableClass, property);
                pdToLoad.getWriteMethod().invoke(obj, objectLoaded);

            } catch (Exception e) {
                throw new InvalidDataAccessApiUsageException("Field [" + property + "] does not exist on Table Class [" + getDomainClass().getName() + "]");
            }
        }
    }

    protected String getEscapedTableName() {
        return beanMappingDescriptor.getEscapedTableName();
    }
}
