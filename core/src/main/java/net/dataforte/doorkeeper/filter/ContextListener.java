package net.dataforte.doorkeeper.filter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.dataforte.doorkeeper.Doorkeeper;

/**
 * This context listener binds Doorkeeper's lifecycle to that of the webapp 
 * 
 * @author Tristan Tarrant
 *
 */
public class ContextListener implements ServletContextListener {
	
	static Doorkeeper doorkeeper;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		doorkeeper = Doorkeeper.getInstance(sce.getServletContext());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		doorkeeper.close();
	}

}
