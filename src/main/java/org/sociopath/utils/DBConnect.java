package org.sociopath.utils;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.logging.LogManager;

public class DBConnect {

    private static Configuration configuration;
    private static SessionFactory sessionFactory;

    public static void startCon() {
        configuration= new Configuration.Builder()
                .uri("bolt://localhost")
                .credentials("neo4j", "1234")
                .build();
        LogManager.getLogManager().reset();
        sessionFactory = new SessionFactory(configuration, "node", "org.school");
    }

    public static Session getSession() {
        return sessionFactory.openSession();
    }

    public static void closeCon(){
        sessionFactory.close();
    }
}