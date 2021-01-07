import exceptions.RequestException;
import okhttp3.*;

import java.io.IOException;
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
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String PROD_URL = "https://prod.setu.co/api";
    private static final String SANDBOX_URL = "https://sandbox.setu.co/api";
    private final String productionInstance;
    private final Boolean isProduction;

    private final SetuJwtHelper jwtHelper;

    public SetuRequestHelper(String schemeId, String secret, String productInstance, Boolean isProduction) {
        this.productionInstance = productInstance;
        this.isProduction = isProduction;
        jwtHelper = new SetuJwtHelper(schemeId, secret);
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
        String exactUp = "{\"amount\": " + "{\"maximum\" : \"0\", " + "\"minimum\": " + amount + "}}";
        String exactDown = "{\"amount\":" + " {\"minimum\" : \"0\"," + " \"maximum\": " + amount + "}}";

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
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestMethod(method);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    public String generateLink(int amount, int expiresInDays, String payeeName, String refId, String exactness)
            throws IOException {
        String path = "/payment-links";
        URL url = getURL(path);
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(expiresInDays);
        // input json
        String jsonInputString = "{" + "\"amount\": {\"currencyCode\" : \"INR\", \"value\": " + amount + "},"
                + "\"amountExactness\": \"" + exactness + "\"," + "\"billerBillID\": \"" + refId + "\","
                + "\"dueDate\": \"" + expiryDate.toString() + "Z\"," + "\"expiryDate\": \"" + expiryDate.toString()
                + "Z\"," + "\"name\": \"" + payeeName + "\"";

        if (!exactness.equals("EXACT")) {
            jsonInputString += ",\"validationRules\": " + validationRules(exactness, amount);
        }
        jsonInputString += "}";
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = RequestBody.create(JSON, jsonInputString);
        Request request = new Request.Builder().url("https://sandbox.setu.co/api/payment-links").method("POST", body)
                .addHeader("X-Setu-Product-Instance-ID", productionInstance)
                .addHeader("Authorization", jwtHelper.yieldBearerToken()).addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String checkStatus(String platformBillId) throws IOException {
        String path = "/payment-links/" + platformBillId;
        URL url = getURL(path);
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder().url("https://sandbox.setu.co/api/payment-links/536938956881659249")
                .addHeader("X-Setu-Product-Instance-ID", productionInstance)
                .addHeader("Authorization", jwtHelper.yieldBearerToken()).addHeader("Content-Type", "application/json")
                .method("GET", null).build();
        Response response = client.newCall(request).execute();
        // output in json format

        return response.body().string();
    }

    public String mockPayment(int amount, String upiId) throws IOException, RequestException {
        String path = "/triggers/funds/addCredit";
        URL url = getURL(path);
        String jsonInputString = "{" + "\"amount\" :"  + amount + ","
                + "\"destinationAccount\": { \"accountID\" : \"" + upiId + "\"},"
                + "\"sourceAccount\": { \"accountID\" : \"customer@vpa\"}," + "\"type\" : \"UPI\"" + "\"}";

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = RequestBody.create(JSON, jsonInputString);
        Request request = new Request.Builder().url(url.toString())
                .addHeader("X-Setu-Product-Instance-ID", productionInstance)
                .addHeader("Authorization", jwtHelper.yieldBearerToken()).addHeader("Content-Type", "application/json")
                .method("POST", body).build();
        Response response = client.newCall(request).execute();

        if(response.code() != 200){
            throw new RequestException("Request Failed", "101", response.code());
        }
        return "Mock Success";
    }
}
