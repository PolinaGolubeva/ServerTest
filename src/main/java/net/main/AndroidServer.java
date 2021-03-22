package net.main;

import dbservice.objects.Parking;
import dbservice.services.DBService;
import net.parking.WebSocketParkingServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class AndroidServer implements Runnable {
    private DBService<Parking> parkingDBService;

    public AndroidServer(DBService<Parking> parkingDBService) {
        this.parkingDBService = parkingDBService;
    }

    public void init() throws Exception {
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        context.addServlet(new ServletHolder(new WebSocketParkingServlet(this.parkingDBService)), "/client");

        ResourceHandler resource_handler = new ResourceHandler();
        //resource_handler.setDirectoriesListed(true);
        //resource_handler.setResourceBase("public_html");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, context});
        server.setHandler(handlers);

        server.start();
        System.out.println("Android server started");
        //server.join();
    }

    @Override
    public void run() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
