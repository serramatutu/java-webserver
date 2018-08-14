import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.InterruptedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Date;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;

public class RequestProcessor extends Thread {
    private String directory;
    private Logger logger;
    private ArrayBlockingQueue<Socket> requestQueue;
    
    public RequestProcessor(String directory, Logger logger, ArrayBlockingQueue<Socket> requestQueue) {
        this.directory = directory;
        this.requestQueue = requestQueue;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            while (requestQueue.size() <= 0) {
                synchronized (this) {
                    try {
                        this.wait();
                    }
                    catch (InterruptedException e) {}
                }
            }

            String[] req = null;
            Socket socket = null;
            // processa uma request
            try {
                socket = requestQueue.take();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                req = reader.readLine().split(" ");
            }
            catch (InterruptedException e) {
                logger.warn(1, "InterruptedException while taking from queue");
                continue;
            }
            catch (IOException e) {
                logger.warn(1, "IOException while reading socket");
                e.printStackTrace(logger.getStream());
                continue;
            }

            if (req[0].equals("GET")) {
                String addr = req[1];
                logger.log(2, "Request to "+addr);

                String res = null;

                Path path = Paths.get(this.directory + addr);
                if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(path)) {
                    try {
                        byte[] content = Files.readAllBytes(path);
                        res = "HTTP/1.1 200 OK\r\n" +
                              "Connection: close\r\n" +
                              "Date: " + new Date().toString() + "\r\n" +
                              "Server: Apache/1.3.0 (Unix)\r\n" +
                              "Last-Modified: " + Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toString() + "\r\n" +
                              "Content-Length: " + content.length + "\r\n" +
                              "Content-Type: text/html " + "\r\n" +
                              "Encoding: UTF-8\r\n\r\n" +
                              new String(content);
                    }
                    catch (IOException e) {
                        logger.warn(1, "IOException while reading " + path.toString());
                        res = "HTTP/1.1 500 Internal Server Error\r\n";
                    }
                }
                else {
                    res = "HTTP/1.1 404 Not Found\r\n" +
                          "Encoding: UTF-8\r\n";
                    logger.log(2, "File "+path.toString()+" does not exist");
                }

                try {
                    socket.getOutputStream().write(res.getBytes(StandardCharsets.UTF_8));
                    socket.close();
                }
                catch (IOException e) {
                    logger.warn(1, "Unable to respond to " + socket.getInetAddress());
                }
            }
        }
    }
}