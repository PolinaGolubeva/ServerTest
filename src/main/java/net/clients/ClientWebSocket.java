package net.clients;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dbservice.objects.Order;
import dbservice.objects.Parking;
import net.utils.MessageGenerator;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
@WebSocket
public class ClientWebSocket {
    private ClientService clientService;
    private Session session;

    public ClientWebSocket(ClientService clientService) {
        this.clientService = clientService;
    }

    @OnWebSocketConnect
    public void onOpen(Session session) {
        System.out.println("Connection with client established");
        clientService.add(this);
        this.session = session;
    }

    @OnWebSocketMessage
    public void onMessage(String data) {
        System.out.println("Message received from client: " + data);
        if (data.startsWith(MessageGenerator.GET_ALL_PARKINGS)) {
            sendAllParkings();
        }
        if (data.startsWith(MessageGenerator.SEND_ORDER)) {
            getOrder(data);
        }
        if (data.startsWith(MessageGenerator.GET_QR)) {
            sendQR(data);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        clientService.remove(this);
    }

    public void sendString(String data) {
        try {
            session.getRemote().sendString(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendBinary(byte[] bytes) {
        try {
            session.getRemote().sendBytes(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAllParkings() {
        List<Parking> pList = clientService.getParkingDBService().getAll();
        String msg = MessageGenerator.SEND_ALL_PARKINGS;
        String json = (new Gson().toJson(pList));
        msg += json;
        clientService.sendMessage(msg, this);
        System.out.println("All parkings sent: " + msg);
    }

    private void getOrder(String data) {
        String msg = data.replace(MessageGenerator.SEND_ORDER, "");
        try {
            Order order = Order.fromJson(msg);
            Long oldId = order.getId();
            String response = MessageGenerator.SEND_ORDER_ID + oldId + "|";
            System.out.println("Order received: " + order.toString());
            clientService.getOrderDBService().insert(order);
            Long newId = order.getId();
            response += newId;
            clientService.sendMessage(response, this);
        } catch (JsonParseException e) {
            String response = MessageGenerator.ERROR + "Error: order was not received, wrong format";
        } catch (NullPointerException e) {
            String response = MessageGenerator.ERROR + "Error: empty order";
        }
    }

    private void sendQR(String data) {
        System.out.println("QR-code request: " + data);
        data = data.replace(MessageGenerator.GET_QR, "");
        Long id = Long.parseLong(data);
        clientService.sendQR(id, this);
    }

}