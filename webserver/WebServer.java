import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;

import javax.imageio.spi.IIOServiceProvider;

import jdk.nashorn.internal.ir.RuntimeNode.Request;

import java.io.IOException;
import java.lang.Thread;


public class WebServer extends Thread {
    private WebServerConfig config;
    private ServerSocket ss;
    private ArrayBlockingQueue<Socket> requestQueue = new ArrayBlockingQueue<Socket>(10);
    private RequestProcessor processor;

    public WebServer(WebServerConfig config) {
        this.config = config;
        this.processor = new RequestProcessor(config.getDirectory(), config.logger, requestQueue);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if (processor != null)
            processor.interrupt();
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(config.getPort());
        }
        catch (IOException e) {
            config.logger.err("IOExcepiton while constructing server socket");
            e.printStackTrace(config.logger.getErrorStream());
        }
        processor.start();

        config.logger.log(2, "Starting server at port "+config.getPort());
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket s = ss.accept();
                requestQueue.add(s);
            }
            catch (IOException e) {
                config.logger.err("IOException while accepting socket");
                e.printStackTrace(config.logger.getErrorStream());
                break;
            }
            synchronized (processor) {
                processor.notify();
            }
        }
        config.logger.log(1, "Stopping server.");
    }
}