import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SetuRequestHelper setuRequestHelper = new SetuRequestHelper("eb1b012f-73c7-4ac8-b6fe-ba733a96c4e7",
                "b977cd5d-27ee-4b29-b329-56ef4deba748",
                "418980734666016388",
                false);

        setuRequestHelper.generateLink(1000.0,
                20,
                "Name",
                "ABNCD1234",
                "EXACT_UP");
    }
}
