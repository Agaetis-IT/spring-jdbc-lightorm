package com.agaetis.spring.jdbc.lightorm.test;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.agaetis.spring.jdbc.lightorm.test.dao.CarDao;
import com.agaetis.spring.jdbc.lightorm.test.model.Car;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a
 * href="http://www.agaetis.fr">Agaetis</a> on 12/03/2015.
 */
public class TestLightOrmCrudRepository extends AbstractTest {

	@Autowired
	private CarDao carDao;

	@Test
	public void testMapping() {
		List<Car> carList = carDao.findAll();
		for (Car car : carList) {
			System.out.println(car.getName());
		}
	}

}
