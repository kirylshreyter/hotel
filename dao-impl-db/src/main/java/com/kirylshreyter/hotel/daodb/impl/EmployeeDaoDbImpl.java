package com.kirylshreyter.hotel.daodb.impl;

import com.kirylshreyter.hotel.daoapi.IEmployeeDao;
import com.kirylshreyter.hotel.daodb.util.NotNullChecker;
import com.kirylshreyter.hotel.datamodel.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EmployeeDaoDbImpl implements IEmployeeDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeDaoDbImpl.class);

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private NotNullChecker notNullChecker;

    @Override
    public Long create(Employee entity) {
        LOGGER.info("Trying to create employee in table employee ...");
        if (notNullChecker.EmployeeNotNullChecker(entity)) {
            final String INSERT_SQL = "INSERT INTO employee (first_name,last_name,phone,email,position) VALUES (?,?,?,?,?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement ps = con.prepareStatement(INSERT_SQL, new String[]{"id"});
                    ps.setString(1, entity.getFirstName());
                    ps.setString(2, entity.getLastName());
                    ps.setString(3, entity.getPhone());
                    ps.setString(4, entity.getEmail());
                    ps.setString(6, entity.getPosition());
                    return ps;
                }
            }, keyHolder);
            ;
            entity.setId(keyHolder.getKey().longValue());
            Long insertedId = entity.getId();
            LOGGER.info("Employee was created, id = {}", insertedId);
            return insertedId;
        } else {
            return null;
        }
    }

    @Override
    public Employee read(Long id) {
        return null;
    }

    @Override
    public Boolean update(Employee entity) {
        LOGGER.info("Trying to update employee with id = {} in table employee...", entity.getId());
        if (notNullChecker.EmployeeNotNullChecker(entity)) {
            jdbcTemplate.update(
                    "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, position = ? where id = ?",
                    entity.getFirstName(), entity.getLastName(), entity.getPhone(), entity.getEmail(),
                    entity.getPosition(), entity.getId());
            LOGGER.info("Employee was updated, id = {}", entity.getId());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Integer delete(Long id) {
        return null;
    }

    @Override
    public List<Employee> getAll() {
        return null;
    }
}
