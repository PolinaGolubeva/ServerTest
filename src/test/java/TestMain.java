import dbservice.objects.Coordinates;
import dbservice.objects.Parking;

public class TestMain {
    public static void main(String[] args) {
        Parking parking = new Parking(1, new Coordinates(15, 16), "addr1", 100, 15);
        System.out.println(parking.toString());
        String s = parking.toString();
        Parking p2 = Parking.fromJson(s);
        System.out.println("P2");
        System.out.println(p2.toString());
    }
}
