package com.agaetis.spring.jdbc.lightorm.test.dao;

import org.springframework.stereotype.Repository;

import com.agaetis.spring.jdbc.lightorm.repository.LightOrmPagedRepository;
import com.agaetis.spring.jdbc.lightorm.test.model.Service;
import com.agaetis.spring.jdbc.lightorm.test.model.ServiceId;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
@Repository
public class ServiceDao extends LightOrmPagedRepository<Service, ServiceId> {

}
