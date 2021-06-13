package org.sociopath.utils;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.logging.LogManager;

/**
 * This class has all the necessary information and function to able to connect to the Neo4j database
 */
public class DBConnect {

    private static SessionFactory sessionFactory;

    /**
     * Start the connection with the Neo4j Database (This is for the community version 3.5.28)
     */
    public static void startCon() {
        Configuration configuration = new Configuration.Builder()
                .uri("bolt://localhost")
                .credentials("neo4j", "1234")       // TODO: Put your password here
                .build();
        LogManager.getLogManager().reset();

        sessionFactory = new SessionFactory(configuration, "org.sociopath", "org.sociopath.models");
    }

    /**
     * Open a session everytime this method is called so that every transaction can be done
     * @return Session object for running queries, save, delete, etc.
     */
    public static Session getSession() {
        return sessionFactory.openSession();
    }

    /**
     * Close the connection with the database by closing the SessionFactory
     */
    public static void closeCon(){
        sessionFactory.close();
    }

}