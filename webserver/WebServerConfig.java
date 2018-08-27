public class WebServerConfig {
    private int port = DEFAULT_PORT;
    private String directory = "./bin";

    public Logger logger = new Logger();

    public int getPort() {
        if (this.port != DEFAULT_PORT)
            return this.port;
        return DEFAULT_PORT;
    }

    public String getDirectory() {
        return this.directory;
    }

    public static WebServerConfig fromArgs(String[] args) {
        return new WebServerConfig();
    }

    public static final int DEFAULT_PORT = 8080;
}