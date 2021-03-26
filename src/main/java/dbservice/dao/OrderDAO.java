package dbservice.dao;

import dbservice.exceptions.DBException;
import dbservice.executor.Executor;
import exceptions.ModelException;
import dbservice.objects.Order;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private Executor executor;

    /** Initializes executor of SQL queries
    * @param connection provided by connector
    * @return OrderDAO
    **/
    public OrderDAO(Connection connection) {
        executor = new Executor(connection);
    }

    /**
     *  Creates table with orders if it does not exist in DB
     * @throws SQLException
     **/
    public void createTable() throws SQLException {
            executor.execUpdate("CREATE TABLE IF NOT EXISTS orders " +
                    "(id UNSIGNED PRIMARY KEY AUTOINCREMENT, " +
                    "parking_id UNSIGNED, " +
                    "car_number VARCHAR(10), " +
                    "startt UNSIGNED, " +
                    "fin UNSIGNED CHECK (fin > startt), " +
                    "payment_info VARCHAR(256))");
    }

    /** Inserts new order in DB
     * @param order - new order from client
     * @return <code> Long </code> id of order inserted in table
     * @throws SQLException
     * @throws DBException
     * @throws ModelException
     **/
    public Long insert(Order order) throws SQLException, DBException, ModelException {
        long parkingId = order.getParkingId();
        String carNumber = order.getCarNumber();
        long startt = order.getStart().getTime();
        long fin = order.getFinish().getTime();
        String paymentInfo = order.getPaymentInfo();
        executor.execUpdate("INSERT INTO orders VALUES (NULL, " + parkingId + ", '" +
                carNumber + "', " +
                startt + ", " +
                fin + ", '" +
                paymentInfo + "')");
        try {
            Long id = getId(order);
            order.setId(id);
            return id;
        } catch (DBException e) {
            throw new DBException("Insertion in table \"Orders\" failed");
        } catch (ModelException e) {
            throw new ModelException(e.getMessage() + "\n Current order already exists");
        }
    }

    /** Gets id of order in order table by it's other fields jdbc update row
     * @param order
     * @return id of the given order
     * @throws SQLException
     * @throws DBException
     **/
    public Long getId(Order order) throws SQLException, DBException {
        String query = "SELECT id FROM orders WHERE " +
                "parking_id='" + order.getParkingId() +
                "' AND startt=" + order.getStart().getTime() +
                " AND fin=" + order.getFinish().getTime();
        Long res =  executor.execQuery(query, result -> {
            if (result.next())
                return result.getLong(1);
            else
                return null;
        });
        if (res != null)
            return res;
        else
            throw new DBException("Order not found");
    }

    /** Returns order with current id
     * @param id of order
     * @return order with current id if found one
     * @throws SQLException
     **/
    public Order get(long id) throws SQLException {
        String query = "SELECT * FROM orders WHERE id=" + id;
        return executor.execQuery(query, result -> {
            if (!result.isLast()) {
                result.next();
                long parkingId = result.getLong(2);
                String carNumber = result.getString(3);
                long startt = result.getLong(4);
                long fin = result.getLong(5);
                String paymentInfo = result.getString(6);
                return new Order(id, parkingId, carNumber, startt, fin, paymentInfo);
            } else
                return null;
        });
    }

    /** Returns all orders in current table
     * @return List of orders
     * @throws SQLException
     **/
    public List<Order> getAll() throws SQLException {
        return executor.execQuery("SELECT * FROM orders", result -> {
                    ArrayList<Order> orderList = new ArrayList<>();
                    while (result.next()) {
                        long id = result.getLong(1);
                        long parkingId = result.getLong(2);
                        String carNumber = result.getString(3);
                        long startt = result.getLong(4);
                        long fin = result.getLong(5);
                        String paymentInfo = result.getString(6);
                        orderList.add(new Order(id, parkingId, carNumber, startt, fin, paymentInfo));
                    }
                    return orderList;
        });
    }

    /** Changes state of current order (payment info, car number, etc)
     * @param order with all fields that need to be updated
     * @return id of updated order (if one was updated)
     * @throws SQLException
     **/
    public Long update(Order order) throws SQLException {
        String query = "UPDATE orders SET parking_id=" + order.getParkingId() +
                ", car_number='" + order.getCarNumber() +
                "', startt=" + order.getStart().getTime() +
                ", fin=" + order.getStart().getTime() +
                ", payment_info='" + order.getPaymentInfo() +
                "' WHERE id=" + order.getId();
        executor.execUpdate(query);
        return order.getId();
    }

    /** Deletes order with current id
     * @param id of order to be deleted
     * @throws SQLException
     **/
    public void delete(long id) throws SQLException {
        String query = "DELETE FROM orders WHERE id=" + id;
        executor.execUpdate(query);
    }

    /** Deletes the order table from DB
     * @throws SQLException
     **/
    public void dropTable() throws SQLException {
        executor.execUpdate("DROP TABLE orders");
    }
}
