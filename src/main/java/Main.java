import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SetuRequestHelper setuRequestHelper = new SetuRequestHelper("", "", "", false);

        setuRequestHelper.generateLink(1000.0, 20, "Name", "ABNCD1234", "EXACT");
    }
}
