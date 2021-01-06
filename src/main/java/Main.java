import java.io.IOException;

import exceptions.RequestException;

public class Main {
    public static void main(String[] args) throws IOException, RequestException {
        SetuRequestHelper setuRequestHelper = new SetuRequestHelper(
                "eb1b012f-73c7-4ac8-b6fe-ba733a96c4e7",
                "b977cd5d-27ee-4b29-b329-56ef4deba748",
                "418980734666016388",
                false);

        String response = setuRequestHelper.generateLink(1000,
                20,
                "Name",
                "ABNCD1234",
                "EXACT");
        
        String status = setuRequestHelper.checkStatus("536938956881659249");
        String mockPayment = setuRequestHelper.mockPayment(1000, "foodstreet536938956881659249@kaypay");

        System.out.println("Response  = " + response);
        System.out.println("-----------------------------");
        System.out.println("Status  = " + status);
        System.out.println("-----------------------------");

        System.out.println("MockPayment  = " + mockPayment);
        System.out.println("-----------------------------");

    }
}
