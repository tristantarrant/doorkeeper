package net.dataforte.doorkeeper.filter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.dataforte.doorkeeper.Doorkeeper;

public class DoorkeeperContextListener implements ServletContextListener {
	
	static Doorkeeper doorkeeper;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		doorkeeper = new Doorkeeper();
		sce.getServletContext().setAttribute(Doorkeeper.class.getName(), doorkeeper);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		

	}

}
