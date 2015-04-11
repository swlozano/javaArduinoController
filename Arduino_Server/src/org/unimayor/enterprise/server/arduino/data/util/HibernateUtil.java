package org.unimayor.enterprise.server.arduino.data.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;

/**
 * @netbeans.hibernate.util
 */
public class HibernateUtil {

    private static Logger log = Logger.getLogger("Aplicacion");
    private static SessionFactory sessionFactory;
    public static final ThreadLocal session = new ThreadLocal();
    private static Properties propiedades;

    private static SessionFactory getSessionFactory() {
        try {
            if (sessionFactory == null) {
                propiedades = new Properties();
                try {
                    propiedades.load(new FileInputStream("server.properties"));
                } catch (IOException ex) {
                    log.error("Error al leer el archivo de propiedades: server.properties");
                }
                
                Configuration cfg = new Configuration().configure(File.separator + "org" + File.separator + "seratic" + File.separator + "enterprise" + File.separator + "server" + File.separator + "himatch" + File.separator + "data" + File.separator + "util" + File.separator + "hibernate.cfg.xml");

                for (Entry<Object, Object> e : propiedades.entrySet()) {
                    cfg.setProperty(""+e.getKey(), ""+e.getValue());
                }
                //sessionFactory = new Configuration().configure(File.separator + "org" + File.separator + "seratic" + File.separator + "enterprise" + File.separator + "server" + File.separator + "chathimatchserver" + File.separator + "data" + File.separator + "util" + File.separator + "hibernate.cfg.xml").buildSessionFactory();
                sessionFactory=cfg.buildSessionFactory();
            }
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
        return sessionFactory;
    }

    public static Session currentSession() throws HibernateException {
        Session s = (Session) session.get();
        if (s == null) {
            s = getSessionFactory().openSession();
            session.set(s);
        }
        return s;
    }

    public static void closeSession() throws HibernateException {
        Session s = (Session) session.get();
        session.set(null);
        if (s != null) {
            s.close();
        }
    }
}
