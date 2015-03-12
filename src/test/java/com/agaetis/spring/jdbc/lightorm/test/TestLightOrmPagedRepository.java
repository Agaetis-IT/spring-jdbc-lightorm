package com.agaetis.spring.jdbc.lightorm.test;

import com.agaetis.spring.jdbc.lightorm.test.dao.CarDao;
import com.agaetis.spring.jdbc.lightorm.test.model.Car;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Created by <a href="https://github.com/rnicob">Nicolas Roux</a> - <a href="http://www.agaetis.fr">Agaetis</a>  on 12/03/2015.
 */
public class TestLightOrmPagedRepository  extends AbstractTest {

    @Autowired
    private CarDao carDao;

    @Test
    public void findAll() {
        //PageRequest request = new PageRequest(0, 3);
        //List<Car> carList = carDao.findAll(request);

        //assert carList.size()==3;


    }
}
