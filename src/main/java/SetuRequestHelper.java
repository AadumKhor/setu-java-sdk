import com.google.gson.Gson;
import exceptions.RequestException;
import models.check_status.CheckStatusResponse;
import models.generate_link.GenerateLinkRequest;
import models.generate_link.GenerateLinkResponse;
import okhttp3.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
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

    public GenerateLinkResponse generateLink(int amount, int expiresInDays, String payeeName, String refId, String exactness)
            throws IOException {
        String path = "/payment-links";
        URL url = getURL(path);
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(expiresInDays);
        // input json
        String jsonInputString = new GenerateLinkRequest(amount,
                expiryDate.toString(),
                payeeName, refId,
                exactness).generateLinkJson();
        System.out.println(jsonInputString);
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = RequestBody.create(JSON, jsonInputString);
        Request request = new Request.Builder().url(url.toString())
                .method("POST", body)
                .addHeader("X-Setu-Product-Instance-ID", productionInstance)
                .addHeader("Authorization", jwtHelper.yieldBearerToken())
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return new Gson().fromJson(response.body().string(), GenerateLinkResponse.class);
    }

    public CheckStatusResponse checkStatus(String platformBillId) throws IOException {
        String path = "/payment-links/" + platformBillId;
        URL url = getURL(path);
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(url.toString())
                .addHeader("X-Setu-Product-Instance-ID", productionInstance)
                .addHeader("Authorization", jwtHelper.yieldBearerToken())
                .addHeader("Content-Type", "application/json")
                .method("GET", null).build();
        Response response = client.newCall(request).execute();
        // output in json format
        return new Gson().fromJson(response.body().string(), CheckStatusResponse.class);
    }

    public String mockPayment(int amount, String upiId) throws IOException, RequestException {
        String path = "/triggers/funds/addCredit";
        URL url = getURL(path);
        String jsonInputString = "{" + "\"amount\" :" + amount + ","
                + "\"destinationAccount\": { \"accountID\" : \"" + upiId + "\"},"
                + "\"sourceAccount\": { \"accountID\" : \"customer@vpa\"}," + "\"type\" : \"UPI\"" + "\"}";

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = RequestBody.create(JSON, jsonInputString);
        Request request = new Request.Builder().url(url.toString())
                .addHeader("X-Setu-Product-Instance-ID", productionInstance)
                .addHeader("Authorization", jwtHelper.yieldBearerToken()).addHeader("Content-Type", "application/json")
                .method("POST", body).build();
        Response response = client.newCall(request).execute();

        if (response.code() != 200) {
            throw new RequestException("Request Failed", "101", response.code());
        }
        return "Mock Success";
    }
}
