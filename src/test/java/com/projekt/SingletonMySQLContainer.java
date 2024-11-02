package com.projekt;

import org.testcontainers.containers.MySQLContainer;

public class SingletonMySQLContainer extends MySQLContainer<SingletonMySQLContainer> {
    private static final String IMAGE_VERSION = "mysql:8.0";
    public static SingletonMySQLContainer container;

    public SingletonMySQLContainer() {
        super(IMAGE_VERSION);
    }

    static {
        container = new SingletonMySQLContainer()
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass")
                    .withReuse(true);
        container.start();

        Runtime.getRuntime().addShutdownHook(new Thread(container::stop));
    }
}
