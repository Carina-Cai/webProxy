import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WebProxy {

    // the port number 
    private static int port;
    
    private ServerSocket sSocket;

    static HashMap<String, File> cache = new HashMap<>();

    static List<String> blockedWebsites = new ArrayList<>();

    static ArrayList<Thread> runningThreads;
    
    public static void main(String[] args) throws IOException {
        if( args.length > 0 ) port = Integer.valueOf(args[0]);
                                                                                                                                                                  
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter website(s) to blcok (separate by space). Press 'Enter' if nothing to be blocked");
        String sites = scanner.nextLine();
        String[] splited = sites.split("\\s+");
        for (String s : splited) {
            blockedWebsites.add(s);
            System.out.println(s + " blocked successfully");
        }
        scanner.close();
            

        // Pass the request to the Handler so that it can be multi-threaded
        WebProxy webP = new WebProxy(port);
        webP.listen();
    }

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
            Thread thread = new Thread(new ProxyHandler(socket, cache, blockedWebsites));
            thread.start();
        }
    }
}
