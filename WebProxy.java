import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;

public class WebProxy {

    // the port number 
    private static int port;
    
    public static void main(String[] args) throws IOException {
        if( args.length > 0 ) port = Integer.valueOf(args[0]);

        // Pass the request to the Handler so that it can be multi-threaded
        WebProxy webP = new WebProxy(port);
        webP.listen();
    }

    private ServerSocket sSocket;

    static HashMap<String, File> cache = new HashMap<>();

    static ArrayList<Thread> runningThreads;

    public WebProxy(int port) {
        try {
            sSocket = new ServerSocket(port);
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new thread to transmit the client request, 
     * meanwhile keeps listening to any incoming requests
     * @throws IOException
     */

    public void listen() throws IOException {
        
        while(true) {
            // the connection is made
            Socket socket = sSocket.accept();

            // Creates a new thread and its further action will be handled by ProxyHandler
            Thread thread = new Thread(new ProxyHandler(socket, cache));
            thread.start();
        }
    }
}
