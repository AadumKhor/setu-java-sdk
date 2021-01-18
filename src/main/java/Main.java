import exceptions.RequestException;
import helpers.SetuRequestHelper;
import models.check_status.CheckStatusResponse;
import models.generate_link.GenerateLinkResponse;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, RequestException {
        final String schemeId = "eb1b012f-73c7-4ac8-b6fe-ba733a96c4e7";
        final String secret = "b977cd5d-27ee-4b29-b329-56ef4deba748";
        SetuRequestHelper setuRequestHelper = new SetuRequestHelper(
                schemeId,
                secret,
                "418980734666016388",
                false);

        GenerateLinkResponse response = setuRequestHelper.generateLink(1000,
                20,
                "TestingName",
                "ABNCD1234",
                "EXACT");
        CheckStatusResponse status = setuRequestHelper.checkStatus(response.getPlatformBillId());
        System.out.println("UPI ID : " + status.getUpiID());
        String mockPayment = setuRequestHelper.mockPayment(10, status.getUpiID());

        System.out.println("Response  = " + response.toString());
        System.out.println("-----------------------------");
        System.out.println("Status  = " + status);
        System.out.println("-----------------------------");
        System.out.println("MockPayment  = " + mockPayment);
        System.out.println("-----------------------------");
    }
}
