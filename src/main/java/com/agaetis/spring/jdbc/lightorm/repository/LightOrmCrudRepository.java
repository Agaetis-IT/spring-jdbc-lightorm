package com.agaetis.spring.jdbc.lightorm.repository;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.agaetis.spring.jdbc.lightorm.annotation.LazyLoading;
import com.agaetis.spring.jdbc.lightorm.rowmapper.AnnotatedBeanPropertyRowMapper;

/**
 * @author <a href="https://github.com/rnicob">Nicolas Roux</a> - <a href="http://www.agaetis.fr">Agaetis</a>
 * Created on 12/03/2015.
 */
public abstract class LightOrmCrudRepository<T> {
    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private BeanMappingDescriptor<T> beanMappingDescriptor;

    @Value("${datasource.escapedcharacter}")
    private String escapedCharacter;

    private RowMapper<T> defaultRowMapper;

    public abstract Class<T> getTableClass();

    public LightOrmCrudRepository() {
        RegisteredDao.registerDao(this);
    }

    protected BeanMappingDescriptor<T> getBeanMappingDescriptor() {
        return beanMappingDescriptor;
    }

    protected String getEscapedTableName() {
        return beanMappingDescriptor.getEscapedTableName();
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

        defaultRowMapper = AnnotatedBeanPropertyRowMapper.newInstance(getTableClass());

        beanMappingDescriptor = new BeanMappingDescriptor<T>(getTableClass(), escapedCharacter);
    }

    protected RowMapper<T> getRowMapper() {
        return defaultRowMapper;
    }

    public T createOrUpdate(T obj) {
        List<FieldMappingDescriptor> fieldIdDescriptors = beanMappingDescriptor.getFieldIdMappingDescriptors();

        for (FieldMappingDescriptor fieldIdDescriptor : fieldIdDescriptors) {
            Object value = fieldIdDescriptor.readFieldValue(obj);

            if (value == null)
                return create(obj);
            if (Number.class.isAssignableFrom(value.getClass()) && value.equals(0))
                return create(obj);
        }

        try {
            findById(extractIdParams(obj));
        } catch (EmptyResultDataAccessException e) {
            return create(obj);
        }

        update(obj);
        return obj;
    }

    public T create(T obj) {
        Map<String, Object> fieldIdParams = extractIdParams(obj);
        Map<String, Object> fieldParams = extractNonIdParams(obj);

        // On vérifie que les clés primaires soient saisies si on est pas sur
        // une clé auto incrémentée
        if (!beanMappingDescriptor.isIdAutoIncremented()) {
            for (Object value : fieldIdParams.values()) {
                if (value == null)
                    throw new InvalidDataAccessApiUsageException("Impossible de créer un objet sans clé primaire renseignée");
            }
        }

        Map<String, Object> allParams = new HashMap<String, Object>(fieldIdParams.size() + fieldParams.size());
        allParams.putAll(fieldIdParams);
        allParams.putAll(fieldParams);

        // Création de la liste des champs à mettre à jour
        List<String> updatedValueFields = new ArrayList<String>(allParams.size());
        List<String> updatedFields = new ArrayList<String>(allParams.size());
        for (FieldMappingDescriptor field : beanMappingDescriptor.getFieldMappingDescriptors()) {
            // création des conditions
            updatedFields.add(field.getEscapedColumnName());
            updatedValueFields.add(":" + field.getColumnName());
        }

        // La cl� n'est pas g�n�r�e automatiquement
        if (!beanMappingDescriptor.isIdAutoIncremented()) {
            for (FieldMappingDescriptor field : beanMappingDescriptor.getFieldIdMappingDescriptors()) {
                // création des conditions
                updatedFields.add(field.getEscapedColumnName());
                updatedValueFields.add(":" + field.getColumnName());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(beanMappingDescriptor.getEscapedTableName()).append(" ( ").append(StringUtils.join(updatedFields, ",")).append(") values (")
                .append(StringUtils.join(updatedValueFields, " , ")).append(")");

        if (beanMappingDescriptor.isIdAutoIncremented())
            createWithKeyHolder(obj, sb.toString(), allParams);
        else
            namedParameterJdbcTemplate.update(sb.toString(), allParams);
        return obj;
    }

    private void createWithKeyHolder(T obj, String sql, Map<String, Object> allParams) {
        FieldMappingDescriptor fieldIdDescriptor = beanMappingDescriptor.getFieldIdMappingDescriptors().get(0);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource parameterSource = buildParameterSource(allParams);

        namedParameterJdbcTemplate.update(sql, parameterSource, keyHolder, new String[]{fieldIdDescriptor.getColumnName()});

        if (fieldIdDescriptor.getField().getType().equals(Integer.class))
            fieldIdDescriptor.writeFieldValue(obj, keyHolder.getKey().intValue());
        else if (fieldIdDescriptor.getField().getType().equals(Long.class))
            fieldIdDescriptor.writeFieldValue(obj, keyHolder.getKey().longValue());
        else
            throw new InvalidDataAccessApiUsageException("Auto-incremented field [" + fieldIdDescriptor.getField().getName() + "] of type [" + fieldIdDescriptor.getField().getType().getCanonicalName() + "]not managed ");
    }

    private MapSqlParameterSource buildParameterSource(Map<String, Object> parameters) {

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        for (Entry<String, Object> parameter : parameters.entrySet()) {
            String key = parameter.getKey();
            Object value = parameter.getValue();

            if (value == null)
                parameterSource.addValue(key, value);
            else if (value.getClass().isEnum())
                parameterSource.addValue(key, value, Types.VARCHAR);
            else
                parameterSource.addValue(key, value);
        }
        return parameterSource;
    }

    public void update(T obj) {
        Map<String, Object> fieldIdParams = extractIdParams(obj);
        Map<String, Object> fieldParams = extractNonIdParams(obj);

        // On vérifie que les clés primaires soient saisies
        for (Object value : fieldIdParams.values()) {
            if (value == null)
                throw new InvalidDataAccessApiUsageException("Impossible de mettre à jour un objet sans clé primaire renseignée");
        }

        Map<String, Object> allParams = new HashMap<String, Object>(fieldIdParams.size() + fieldParams.size());
        allParams.putAll(fieldIdParams);
        allParams.putAll(fieldParams);

        // Création des conditions sur la clé primaire
        List<String> sqlConditions = new ArrayList<String>(fieldIdParams.size());

        for (FieldMappingDescriptor fieldId : beanMappingDescriptor.getFieldIdMappingDescriptors()) {
            // création des conditions
            sqlConditions.add(fieldId.getEscapedColumnName() + "=:" + fieldId.getColumnName());
        }

        // Création de la liste des champs à mettre à jour
        List<String> updatedFields = new ArrayList<String>(fieldParams.size());
        for (FieldMappingDescriptor field : beanMappingDescriptor.getFieldMappingDescriptors()) {
            // création des conditions
            updatedFields.add(field.getEscapedColumnName() + "=:" + field.getColumnName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("update ").append(beanMappingDescriptor.getEscapedTableName()).append(" set ").append(StringUtils.join(updatedFields, ",")).append(" where ")
                .append(StringUtils.join(sqlConditions, " and "));

        MapSqlParameterSource parameterSource = buildParameterSource(allParams);

        namedParameterJdbcTemplate.update(sb.toString(), parameterSource);
    }

    public List<T> create(List<T> objList) {
        for (T obj : objList)
            create(obj);
        return objList;
    }

    public List<T> update(List<T> objList) {
        for (T obj : objList)
            update(obj);
        return objList;
    }

    public List<T> createOrUpdate(List<T> objList) {
        for (T obj : objList)
            createOrUpdate(obj);
        return objList;
    }

    public List<T> findAll() {
        return jdbcTemplate.query("select * from " + beanMappingDescriptor.getEscapedTableName(), getRowMapper());
    }

    public T findById(Object id) {
        if (beanMappingDescriptor.getFieldIdMappingDescriptors().size() != 1)
            throw new InvalidDataAccessApiUsageException("Le nombre de champs ID ne correspondent pas [" + this.getTableClass().getCanonicalName() + "]");

        FieldMappingDescriptor fieldMappingDescriptor = beanMappingDescriptor.getFieldIdMappingDescriptors().get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("select * from ").append(beanMappingDescriptor.getEscapedTableName()).append(" where ").append(fieldMappingDescriptor.getEscapedColumnName()).append("=:")
                .append(fieldMappingDescriptor.getColumnName());

        Map<String, Object> params = Collections.singletonMap(fieldMappingDescriptor.getColumnName(), id);

        return namedParameterJdbcTemplate.queryForObject(sb.toString(), params, getRowMapper());

    }

    /**
     * @param ids
     *            clé : nom de colonne non echappé, valeur : objet
     * @return
     */
    public T findById(Map<String, Object> ids) {
        if (beanMappingDescriptor.getFieldIdMappingDescriptors().size() != ids.size())
            throw new InvalidDataAccessApiUsageException("Le nombre de champs ID ne correspondent pas [" + this.getTableClass().getCanonicalName() + "]");

        List<String> sqlConditions = new ArrayList<String>(ids.size());

        for (FieldMappingDescriptor fieldId : beanMappingDescriptor.getFieldIdMappingDescriptors()) {
            // Si on ne trouve pas la correspondance
            if (!ids.keySet().contains(fieldId.getColumnName()))
                throw new InvalidDataAccessApiUsageException("Le champs ID [" + fieldId.getColumnName() + "] recherché ne correspondent pas [" + this.getTableClass().getCanonicalName() + "]");

            // création des conditions
            sqlConditions.add(fieldId.getEscapedColumnName() + "=:" + fieldId.getColumnName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("select * from ").append(beanMappingDescriptor.getEscapedTableName()).append(" where ").append(StringUtils.join(sqlConditions, " and "));

        return namedParameterJdbcTemplate.queryForObject(sb.toString(), ids, getRowMapper());
    }

    public void delete(T obj) {
        Map<String, Object> params = extractIdParams(obj);

        // On vérifie que les clés primaires soient saisies
        for (Object value : params.values()) {
            if (value == null)
                throw new InvalidDataAccessApiUsageException("Impossible de supprimer un objet sans clé primaire renseignée");
        }

        List<String> sqlConditions = new ArrayList<String>(params.size());

        for (FieldMappingDescriptor fieldId : beanMappingDescriptor.getFieldIdMappingDescriptors()) {
            // création des conditions
            sqlConditions.add(fieldId.getEscapedColumnName() + "=:" + fieldId.getColumnName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("delete from ").append(beanMappingDescriptor.getEscapedTableName()).append(" where ").append(StringUtils.join(sqlConditions, " and "));

        namedParameterJdbcTemplate.update(sb.toString(), params);
    }

    public void deleteAll() {
        String sql = "delete from " + beanMappingDescriptor.getEscapedTableName();
        jdbcTemplate.update(sql);
    }

    protected Map<String, Object> extractIdParams(T obj) {
        return extractParams(obj, beanMappingDescriptor.getFieldIdMappingDescriptors());
    }

    protected Map<String, Object> extractNonIdParams(T obj) {
        return extractParams(obj, beanMappingDescriptor.getFieldMappingDescriptors());
    }

    /**
     * @param obj
     * @param fieldMappingDescriptors
     * @return renvoie une map avec en clé le nom de la colonne (non échapé)
     *         et en valeur la valeur pour ce champ
     */
    private Map<String, Object> extractParams(T obj, List<FieldMappingDescriptor> fieldMappingDescriptors) {
        HashMap<String, Object> params = new HashMap<String, Object>();

        for (FieldMappingDescriptor fieldMappingDescriptor : fieldMappingDescriptors) {
            Object value = fieldMappingDescriptor.readFieldValue(obj);
            params.put(fieldMappingDescriptor.getColumnName(), value);
        }

        return params;
    }

    public void loadLazyProperties(T obj, String[] properties) {
        if (properties.length == 0)
            return;

        for (String property : properties) {
            Class<T> tableClass = getTableClass();
            try {
                Field fieldToLoad = tableClass.getDeclaredField(property);
                LazyLoading lazyLoading = fieldToLoad.getAnnotation(LazyLoading.class);
                if (lazyLoading == null)
                    throw new InvalidDataAccessApiUsageException("LazyLoading annotation not found on field [" + property + "], Table Class [" + getTableClass().getName() + "]");

                PropertyDescriptor pdWithKey = BeanUtils.getPropertyDescriptor(tableClass, lazyLoading.value());

                LightOrmCrudRepository<?> dao = RegisteredDao.getDao(fieldToLoad.getType());
                if (dao == null)
                    throw new InvalidDataAccessApiUsageException("Dao not found for Table Class [" + getTableClass().getName() + "]");

                Object id = pdWithKey.getReadMethod().invoke(obj);
                if (id == null)
                    return;

                Object objectLoaded = dao.findById(id);
                PropertyDescriptor pdToLoad = BeanUtils.getPropertyDescriptor(tableClass, property);
                pdToLoad.getWriteMethod().invoke(obj, objectLoaded);

            } catch (NoSuchFieldException e) {
                throw new InvalidDataAccessApiUsageException("Field [" + property + "] does not exist on Table Class [" + getTableClass().getName() + "]");
            } catch (BeansException e) {
                throw new InvalidDataAccessApiUsageException("Field [" + property + "] does not exist on Table Class [" + getTableClass().getName() + "]");
            } catch (InvocationTargetException e) {
                throw new InvalidDataAccessApiUsageException("Field [" + property + "] does not exist on Table Class [" + getTableClass().getName() + "]");
            } catch (IllegalAccessException e) {
                throw new InvalidDataAccessApiUsageException("Field [" + property + "] does not exist on Table Class [" + getTableClass().getName() + "]");
            }
        }
    }

}
