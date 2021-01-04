import exceptions.RequestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SetuRequestHelper {
    private static final String PROD_URL = "https://prod.setu.co/api";
    private static final String SANDBOX_URL = "https://sandbox.setu.co/api";

    private final String schemeId;
    private final String secret;
    private final String productionInstance;
    private final Boolean isProduction;

    private final SetuJwtHelper jwtHelper;

    public SetuRequestHelper(String schemeId, String secret, String productInstance, Boolean isProduction) {
        this.schemeId = schemeId;
        this.secret = secret;
        this.productionInstance = productInstance;
        this.isProduction = isProduction;
        jwtHelper = new SetuJwtHelper(this.schemeId, this.secret);
    }

    private URL getURL(String path) throws MalformedURLException {
        URL url;
        try {
            url = isProduction ? new URL(PROD_URL + path) : new URL(SANDBOX_URL + path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Logger.getGlobal().log(Level.WARNING, "Malformed URL. Please check the endpoint provided.");
            throw new MalformedURLException();
        }
        return url;
    }

    private String validationRules(String exactness, double amount) {
        String exactUp = "{\"amount\": " +
                "{\"maximum\" : \"0\", " +
                "\"minimum\": \"" + amount +
                "\"}";
        String exactDown = "{\"amount\":" +
                " {\"minimum\" : \"0\"," +
                " \"maximum\": \"" + amount +
                "\"}";

        if (exactness.equals("EXACT_UP")) {
            return exactUp;
        }
        return exactDown;
    }

    private HashMap<String, String> generateSetuHeaders() {
        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", jwtHelper.yieldBearerToken());
        header.put("X-Setu-Product-Instance-ID", productionInstance);
        return header;
    }

    private HttpURLConnection defaultConnection(URL url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        HashMap<String, String> headers = generateSetuHeaders();
        for (Map.Entry<String, String> me : headers.entrySet()) {
            connection.setRequestProperty(me.getKey(), me.getValue());
        }
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setDoOutput(true);
        return connection;
    }

    public HashMap<String, String> generateLink(double amount,
                                                int expiresInDays,
                                                String payeeName,
                                                String refId,
                                                String exactness) throws IOException {
        HashMap<String, String> res = new HashMap<>();
        String path = "/payment-links";
        URL url = getURL(path);
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(expiresInDays);

        // input json
        String jsonInputString = "{" +
                "\"amount\": {\"currencyCode\" : \"INR\", \"value\": \"" + amount + "\"}," +
                "\"amountExactness\": \"" + exactness + "\"," +
                "\"billerBillID\": \"" + refId + "\"," +
                "\"dueDate\": \"" + expiryDate.toString() + "Z\"," +
                "\"expiryDate\": \"" + expiryDate.toString() + "Z\"," +
                "\"name\": \"" + payeeName + "\"," +
                "\"validationRules\": " + validationRules(exactness, amount) +
                "}";
        System.out.println(jsonInputString);
        HttpURLConnection connection = defaultConnection(url, "POST");

        // provide request body
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Request made");

        // read the response in json format
        try (BufferedReader br =
                     new BufferedReader(
                             new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public HashMap<String, String> checkStatus(String platformBillId) throws IOException {
        String path = "/payment-links/" + platformBillId;
        URL url = getURL(path);
        HashMap<String, String> headers = generateSetuHeaders();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        for (Map.Entry<String, String> me : headers.entrySet()) {
            connection.setRequestProperty(me.getKey(), me.getValue());
        }
        connection.setRequestMethod("GET");

        // output in json format

        return new HashMap<>();
    }

    public String mockPayment(double amount, String upiId) throws IOException, RequestException {
        String path = "/triggers/funds/addCredit";
        URL url = getURL(path);
        String jsonInputString = "{" +
                "\"amount\" : \"" + amount + "\"," +
                "\"destinationAccount\": { \"accountID\" : \"" + upiId + "\"}," +
                "\"sourceAccount\": { \"accountID\" : \"customer@vpa\"}," +
                "\"type\" : \"UPI\"" +
                "\"}";
        HttpURLConnection connection = defaultConnection(url, "POST");
        // provide request body
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int statusCode = connection.getResponseCode();

        if (statusCode != 200) {
            throw new RequestException("Mock request failed", "101", statusCode);
        }

        return "Mock Success";
    }
}
