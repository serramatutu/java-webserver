import java.lang.*;

public class App {
    private static WebServer ws;

    public static void main(String[] args) {
        ws = new WebServer(WebServerConfig.fromArgs(args));
        ws.start();

        try {
            ws.join();
        }
        catch (InterruptedException e) {}
    }
}