package com.janumart.listener;

import com.janumart.util.AppConfig;
import com.janumart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Application lifecycle: configures the shared HikariCP pool, bootstraps the
 * schema/seed data on first run, and closes the pool on shutdown.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        log.info("========== JanuMart starting up ==========");
        try {
            AppConfig.load();
            DBUtil.init(AppConfig.properties());
            DBUtil.bootstrap(log);

            ctx.setAttribute("APP_NAME", AppConfig.get("app.name", "JanuMart"));
            ctx.setAttribute("APP_TAGLINE", AppConfig.get("app.tagline", "Accessories for Human."));
            ctx.setAttribute("APP_CATEGORIES", com.janumart.model.Category.all());
            log.info("JanuMart started successfully.");
        } catch (Exception e) {
            log.error("JanuMart failed to start", e);
            throw new IllegalStateException("Application could not be initialized", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("JanuMart shutting down.");
        DBUtil.closePool();
    }
}