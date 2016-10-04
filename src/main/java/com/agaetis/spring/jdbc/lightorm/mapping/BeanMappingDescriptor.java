package com.agaetis.spring.jdbc.lightorm.mapping;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.mapping.model.IllegalMappingException;

import com.agaetis.spring.jdbc.lightorm.annotation.Column;
import com.agaetis.spring.jdbc.lightorm.annotation.Id;
import com.agaetis.spring.jdbc.lightorm.annotation.Table;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
public class BeanMappingDescriptor<T, ID extends Serializable> {
    private Class<T>                                   domainClass;

    private String                                     tableName                 = null;

    private String                                     escapedTableName          = null;

    private List<ColumnMappingDescriptor>              columnsMappingDescriptors = new LinkedList<ColumnMappingDescriptor>();

    private IdMappingDescriptor<ID>                    idMappingDescriptor;

    private Map<PropertyPath, ColumnMappingDescriptor> paths                     = new HashMap<PropertyPath, ColumnMappingDescriptor>();

    private String                                     escapedCharacter;

    public BeanMappingDescriptor(Class<T> domainClass, String escapedCharacter) {
        this.escapedCharacter = escapedCharacter;
        this.domainClass = domainClass;

        initialize();
    }

    private void initialize() {
        retrieveTableName();
        retrieveFields();
    }

    private void retrieveTableName() {
        String schema = null;
        if (!domainClass.isAnnotationPresent(Table.class)) {
            tableName = domainClass.getSimpleName().toLowerCase();
        } else {
            Table annotation = domainClass.getAnnotation(Table.class);
            if (annotation.value().isEmpty()) {
                tableName = domainClass.getSimpleName().toLowerCase();
            } else {
                tableName = annotation.value();
                if (!annotation.schema().isEmpty()) {
                    schema = annotation.schema();
                }
            }
        }
        if (schema == null) {
            escapedTableName = escapedCharacter + tableName + escapedCharacter;
        } else {
            escapedTableName = escapedCharacter + schema + escapedCharacter + "." + escapedCharacter + tableName + escapedCharacter;
        }
    }

    private void retrieveFields() {
        for (Field field : domainClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Column.class)) {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(domainClass, field.getName());

                if (pd.getReadMethod() == null) {
                    throw new IllegalMappingException("No  getter for field " + field.getName() + " on class " + domainClass.getCanonicalName());
                }
                if (pd.getWriteMethod() == null) {
                    throw new IllegalMappingException("No  setter for field " + field.getName() + " on class " + domainClass.getCanonicalName());
                }

                if (field.isAnnotationPresent(Id.class)) {
                    if (idMappingDescriptor != null) {
                        throw new IllegalMappingException("Id field already found on class " + domainClass.getCanonicalName());
                    }
                    idMappingDescriptor = new IdMappingDescriptor<ID>(domainClass, field, escapedCharacter);
                    if (!idMappingDescriptor.isComposite()) {
                        ColumnMappingDescriptor id = idMappingDescriptor.getColumns().get(0);
                        paths.put(PropertyPath.from(id.getField().getName(), domainClass), id);
                    } else {
                        for (ColumnMappingDescriptor column : idMappingDescriptor.getColumns()) {
                            paths.put(PropertyPath.from(idMappingDescriptor.field.getName() + "." + column.getField().getName(), domainClass), column);
                        }
                    }
                } else {
                    ColumnMappingDescriptor column = new ColumnMappingDescriptor(domainClass, field, escapedCharacter);
                    columnsMappingDescriptors.add(column);
                    paths.put(PropertyPath.from(column.getField().getName(), domainClass), column);
                }
            }
        }

        validateMappings();
    }

    private boolean validateMappings() {
        // Il faut au moins un Id
        if (idMappingDescriptor == null) {
            throw new IllegalMappingException("No Id Field found on class " + domainClass.getCanonicalName());
        }

        return true;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnMappingDescriptor> getColumnsMappingDescriptors() {
        return columnsMappingDescriptors;
    }

    public IdMappingDescriptor<ID> getIdMappingDescriptor() {
        return idMappingDescriptor;
    }

    public String getEscapedTableName() {
        return escapedTableName;
    }

    public Map<PropertyPath, ColumnMappingDescriptor> getPaths() {
        return paths;
    }

    public Class<T> getDomainClass() {
        return domainClass;
    }

}
