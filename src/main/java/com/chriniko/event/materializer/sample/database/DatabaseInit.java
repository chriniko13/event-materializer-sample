package com.chriniko.event.materializer.sample.database;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Log4j2
@Component
public class DatabaseInit {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    void init() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    connection.setAutoCommit(false);
                    statement.execute("DROP TABLE IF EXISTS test.post_events");
                    statement.execute("CREATE TABLE if not exists test.post_events ( " +
                            "  id BIGINT NOT NULL AUTO_INCREMENT, " +
                            "  creation_date TIMESTAMP NOT NULL, " +
                            "  payloadAsJson LONGTEXT NOT NULL, " +
                            "  class TEXT NOT NULL, " +
                            "  PRIMARY KEY (id)" +
                            ")"
                    );
                    connection.commit();
                } catch (Exception error) {
                    connection.rollback();
                    throw error;
                }
            }

        } catch (Exception e) {
            log.error("Could not initialize database, message: {}", e.getMessage());
        }
    }
}
