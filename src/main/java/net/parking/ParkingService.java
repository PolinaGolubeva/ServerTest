package net.parking;

import dbservice.objects.Order;
import dbservice.objects.Parking;
import dbservice.services.DBService;
import net.notifiers.Listener;
import net.notifiers.Manager;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ParkingService {
    private DBService<Parking> parkingDBService;
    private Set<ParkingWebSocket> webSockets;

    public ParkingService(DBService<Parking> parkingDBService) {
        this.parkingDBService = parkingDBService;
        this.webSockets = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public void sendMessage(Long id, String data) {
        for (ParkingWebSocket user : webSockets) {
            try {
                if (user.getId().equals(id))
                    user.sendString(data);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void add(ParkingWebSocket webSocket) {
        webSockets.add(webSocket);
    }

    public void remove(ParkingWebSocket webSocket) {
        webSockets.remove(webSocket);
    }

    public DBService<Parking> getDBService() { return parkingDBService; }

}