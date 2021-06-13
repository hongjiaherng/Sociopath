package org.sociopath.utils;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
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
        try {
            Properties dbProps = new Properties();
            dbProps.load(new FileInputStream("src/main/resources/org/sociopath/others/credential.properties"));

            Configuration configuration = new Configuration.Builder()
                    .uri("bolt://localhost")
                    .credentials(dbProps.getProperty("username"), dbProps.getProperty("password"))   // TODO: Change ur password in credential.properties file (src/main/resources/org/sociopath/others/credential.properties)
                    .build();
            LogManager.getLogManager().reset();

            sessionFactory = new SessionFactory(configuration, "org.sociopath", "org.sociopath.models");
        } catch (FileNotFoundException e) {
            System.out.println("Properties file not found!");
        } catch (IOException e) {
            System.out.println("Error reading properties file!");
        }
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