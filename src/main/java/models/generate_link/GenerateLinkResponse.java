package models.generate_link;

import models.Error;

import java.util.Objects;

public class GenerateLinkResponse {
    private final String status;
    private final Boolean success;
    private final Data data;
    private final Error error;

    public GenerateLinkResponse(String status, Boolean success, Data data, Error error) {
        this.status = status;
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getName() {
        if (data == null) {
            return "Empty";
        }
        return data.getName();
    }

    public String getUpiId() {
        if (data == null) {
            return "Empty UPI Id";
        }
        return Objects.requireNonNullElse(data.getPaymentLink().getUpiID(), "Empty");
    }

    public String getPlatformBillId() {
        if (data == null) {
            return "Empty platformBillId";
        }
        return Objects.requireNonNullElse(data.getPlatformBillID(), "Empty");
    }

    public String getError() {
        if (error == null) {
            return "No error";
        }
        return error.getTitle();
    }

    @Override
    public String toString() {
        return "GenerateLinkResponse status : " + status + " " +
                "success : " + success + " " +
                "name : " + getName() + " " +
                "upiId : " + getUpiId() + " " +
                "platformBillId : " + getPlatformBillId() +
                "error : " + getError()
                ;

    }
}
