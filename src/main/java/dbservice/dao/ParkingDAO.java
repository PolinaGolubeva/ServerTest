package dbservice.dao;

import dbservice.exceptions.DBException;
import dbservice.executor.Executor;
import exceptions.ModelException;
import dbservice.objects.Coordinates;
import dbservice.objects.Parking;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParkingDAO {
    private Executor executor;

    /** Initializes executor of SQL queries
     * @param connection provided by connector
     * @return ParkingDAO
     **/
    public ParkingDAO(Connection connection) {
        executor = new Executor(connection);
    }

    /** Creates parking table if it does not exist in current DB
     * @throws SQLException
     **/
    public void createTable() throws SQLException {
        executor.execUpdate("CREATE TABLE IF NOT EXISTS parkings " +
                "(id UNSIGNED PRIMARY KEY AUTOINCREMENT, " +
                "latitude DOUBLE, " +
                "longitude DOUBLE, " +
                "info VARCHAR(256), " +
                "capacity INT, " +
                "available INT CHECK (available <= capacity), " +
                "UNIQUE (latitude, longitude))");
    }

    /** Inserts new parking into table
     * @param parking to be inserted in parking table
     * @return id of inserted parking
     * @throws SQLException
     * @throws DBException
     * @throws ModelException
     **/
    public Long insert(Parking parking)
            throws SQLException, DBException, ModelException {
        Coordinates coordinates = parking.getCoordinates();
        String info = parking.getInfo();
        int capacity = parking.getCapacity();
        int available = parking.getAvailable();
        String query = "INSERT INTO parkings VALUES (NULL, " +
                String.format("%.4f",coordinates.getLatitude()) + ", " +
                String.format("%.4f",coordinates.getLongitude()) + ", '" +
                info + "', " +
                capacity + ", " +
                available + ")";
        executor.execUpdate(query);
        try {
            Long id =  getId(coordinates);
            parking.setId(id);
            return id;
        } catch (DBException e) {
            throw new DBException("Insertion in \"Parking\" table failed");
        } catch (ModelException e) {
            throw new ModelException(e.getMessage() + "\n Current parking already exists");
        }
    }

    /** Returns parking by id
     * @param id of parking
     * @return Parking with current id
     * @throws SQLException
     * @throws DBException
     **/
    public Parking get(long id) throws SQLException, DBException {
        String query = "SELECT * FROM parkings WHERE id=" + id;
        Parking parking = executor.execQuery(query, result -> {
            if (!result.isLast()) {
                result.next();
                double latitude = result.getDouble(2);
                double longitude = result.getDouble(3);
                String info = result.getString(4);
                int capacity = result.getInt(5);
                int available = result.getInt(6);
                return new Parking(id, new Coordinates(latitude, longitude), info, capacity, available);
            } else { return null; }
        });
        if (parking != null)
            return parking;
        else
            throw new DBException("Parking not found");
    }

    /** Returns id of parking with current coordinates
     * @param coordinates of parking to be found
     * @return id of parking if found one
     * @throws SQLException
     * @throws DBException
     **/
    public Long getId (Coordinates coordinates) throws SQLException, DBException {
        String query = "SELECT id FROM parkings WHERE " +
                "latitude=" + String.format("%.4f", coordinates.getLatitude())
                + " and longitude=" + String.format("%.4f",coordinates.getLongitude());
        Long id = executor.execQuery(query, result -> {
            if (result.next()) {
                result.next();
                return result.getLong(1);
            }
            return null;
        });
        if (id != null)
            return id;
        else
            throw new DBException("Parking not found");
    }

    /** Returns list of all parkings presented in parkings table
     * @return List of parkings
     * @throws SQLException
     **/
    public List<Parking> getAll() throws SQLException {
        String query = "SELECT * FROM parkings";
        ArrayList<Parking> parkings = executor.execQuery(query, result -> {
            ArrayList<Parking> p = new ArrayList<Parking>();
                while (result.next()) {
                    long id = result.getLong(1);
                    Coordinates coordinates =
                            new Coordinates(result.getDouble(2),
                                            result.getDouble(3));
                    String info = result.getString(4);
                    int capacity = result.getInt(5);
                    int available = result.getInt(6);
                    p.add(new Parking(id, coordinates, info, capacity, available));
                }
                return p;
        });
        return parkings;
    }

    /** Changes state of current parking (info, available, etc)
     * @param parking with all fields that need to be updated
     * @return id of updated parking (if one was updated)
     * @throws SQLException
     **/
    public Long update(Parking parking) throws SQLException {
        String query = "UPDATE parkings SET capacity=" + parking.getCapacity()
                + ", available=" + parking.getAvailable()
                + ", info=" + parking.getInfo()
                + " WHERE id=" + parking.getId();
        executor.execUpdate(query);
        return parking.getId();
    }

    /** Deletes current parking from the parkings table
     * @param id of parking to be deleted
     * @throws SQLException
     **/
    public void delete (long id) throws SQLException {
        String query = "DELETE FROM parkings WHERE id=" + id;
        executor.execUpdate(query);
    }

    /** Deletes the parkings table from DB
     * @throws SQLException
     **/
    public void dropTable() throws SQLException {
        executor.execUpdate("DROP TABLE parkings");
    }

}
