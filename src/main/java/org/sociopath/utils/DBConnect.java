package org.sociopath.utils;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.logging.LogManager;

public class DBConnect {

    private static SessionFactory sessionFactory;

    public static void startCon() {
        Configuration configuration = new Configuration.Builder()
                .uri("bolt://localhost")
                .credentials("neo4j", "QWERTyuiop12#")       // TODO: Put your password here
                .build();
        LogManager.getLogManager().reset();

        sessionFactory = new SessionFactory(configuration,  "org.sociopath", "org.sociopath.models");
    }

    public static Session getSession() {
        return sessionFactory.openSession();
    }

    public static void closeCon(){
        sessionFactory.close();
    }

}