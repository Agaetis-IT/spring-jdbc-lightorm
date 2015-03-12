package com.agaetis.spring.jdbc.lightorm.repository;

import com.agaetis.spring.jdbc.lightorm.annotation.Column;
import com.agaetis.spring.jdbc.lightorm.annotation.Id;
import com.agaetis.spring.jdbc.lightorm.annotation.Table;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a href="http://www.agaetis.fr">Agaetis</a>  on 12/03/2015.
 */
public class BeanMappingDescriptor<T> {
    private Class<T> tableClass;
    private String tableName = null;
    private String escapedTableName = null;
    private List<FieldMappingDescriptor> fieldMappingDescriptors = new LinkedList<FieldMappingDescriptor>();
    private List<FieldMappingDescriptor> fieldIdMappingDescriptors = new LinkedList<FieldMappingDescriptor>();
    private boolean withCompositeKey;
    private boolean idAutoIncremented = false;
    private String escapedCharacter;


    public BeanMappingDescriptor(Class<T> tableClass, String escapedCharacter) {
        this.escapedCharacter = escapedCharacter;
        initialize(tableClass);
    }

    private void initialize(Class<T> tableClass) {
        this.tableClass = tableClass;
        retrieveTableName();
        retrieveFields();
    }

    private void retrieveTableName() {
        if (!tableClass.isAnnotationPresent(Table.class))
            tableName = tableClass.getSimpleName().toLowerCase();
        else {
            Table annotation = tableClass.getAnnotation(Table.class);
            if (annotation.value().isEmpty())
                tableName = tableClass.getSimpleName().toLowerCase();
            else
                tableName = annotation.value();
        }
        escapedTableName = escapedCharacter + tableName + escapedCharacter;
    }

    private void retrieveFields() {
        for (Field field : tableClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Column.class)) {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(tableClass, field.getName());

                if(pd.getReadMethod()==null)
                    throw new InvalidDataAccessResourceUsageException("Pas de getter pour le champ " + field.getName());
                if(pd.getWriteMethod()==null)
                    throw new InvalidDataAccessResourceUsageException("Pas de setter pour le champ " + field.getName());

                FieldMappingDescriptor fieldMappingDescriptor = new FieldMappingDescriptor(tableClass, field, escapedCharacter);

                if (field.isAnnotationPresent(Id.class))
                    fieldIdMappingDescriptors.add(fieldMappingDescriptor);
                else
                    fieldMappingDescriptors.add(fieldMappingDescriptor);
            }
        }

         validateMappings();

        if(fieldIdMappingDescriptors.size()>1)
            withCompositeKey=true;

        if(fieldIdMappingDescriptors.size()==1)
            idAutoIncremented = fieldIdMappingDescriptors.get(0).isIdAutoIncrement();
    }

    private boolean validateMappings() {
        // Il faut au moins un Id
        if(fieldIdMappingDescriptors.isEmpty())
            throw new InvalidDataAccessResourceUsageException("No Id Field found on class " + tableClass.getCanonicalName());


        // Pas d'autoincrement s'il y a plusieurs clés
        if(fieldIdMappingDescriptors.size()>1)
        for(FieldMappingDescriptor fieldId : fieldIdMappingDescriptors) {
            if(fieldId.isIdAutoIncrement())
                throw new InvalidDataAccessResourceUsageException("Impossible d'avoir un id autoIncrement sur plusieurs clés " + tableClass.getCanonicalName());
        }

        return true;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isWithCompositeKey() {
        return withCompositeKey;
    }

    public boolean isIdAutoIncremented() {
        return idAutoIncremented;
    }

    public List<FieldMappingDescriptor> getFieldMappingDescriptors() {
        return fieldMappingDescriptors;
    }

    public List<FieldMappingDescriptor> getFieldIdMappingDescriptors() {
        return fieldIdMappingDescriptors;
    }

    public String getEscapedTableName() {
        return escapedTableName;
    }
}

