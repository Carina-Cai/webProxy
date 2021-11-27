import java.io.*;
import java.net.*;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sound.sampled.SourceDataLine;

/**
 * This class handles the requests that pass through the proxy, both incoming and outgoing ones.
 * It creates a new thread everytime there is a new request comes in
 */
public class ProxyHandler implements Runnable {

    private Socket cSocket;
    HashMap<String, File> cache;
    private BufferedWriter serverResponseBW; // Send response from the proxy to the client
    private BufferedReader clientRequestBR; // Read request from the client to the proxy

    public ProxyHandler(Socket cSocket, HashMap<String, File> cache) throws IOException {
        this.cSocket = cSocket;
        this.cache = cache;
        serverResponseBW = new BufferedWriter(new OutputStreamWriter(cSocket.getOutputStream()));
        clientRequestBR = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
    }

    /**
     * Read the request and implement the corresponding method
     */
    public void run() {

        String request = null;
        SimpleDateFormat timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            request = clientRequestBR.readLine();

            System.out.println("====== Request " + request + " has been received on " + timeStamp.format(new Date()));

        } catch (IOException e) {
            System.out.println("Error Reading the request from client");
            e.printStackTrace();
        }
        
		// Get the type of the request
		String requestMethod = request.substring(0,request.indexOf(' '));
		// get the string of the URL by removing request type and space
		String requestURL = request.substring(request.indexOf(' ')+1);

		// Remove everything after the next space
		requestURL = requestURL.substring(0, requestURL.indexOf(' '));

		// Prepend http:// if necessary to create correct URL
		if(!requestURL.substring(0,4).equals("http")){
			requestURL = "http:/" + requestURL;
		}

        System.out.println(requestMethod + " " + requestURL);


        if (requestMethod.equals("GET")) {
            File file = null;
            if (this.cache.containsKey(requestURL)) file = cache.get(requestURL);
            // Check if the requested website has a cache
            if (file != null) {
                System.out.println("====== Cache found for " + requestURL + "\n");
                sendCache(file);
            } else {
                System.out.println("====== Obtain non-cached contents");
                try {
                    // Fetch contents from the webpage
                    obtainContent(requestURL);
                } catch(IOException e) {
                    System.out.println("Can't fetch contents.");
                }
                
            }
        }
        try {
            cSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a cache file to the client
     */
    private void sendCache(File file) {
        try {

            SimpleDateFormat timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String fileType = file.getName().substring(file.getName().lastIndexOf('.'));

            String response;

            // check if the file is an image
            if (fileType.contains(".png") || fileType.contains(".jpg") || fileType.contains(".jpeg") || fileType.contains(".gif") || fileType.contains(".ico")) {
                // Read image
                BufferedImage im = ImageIO.read(file);
                if (im == null) {
                    response = "HTTP/1.0 404 NOT FOUND\n" + timeStamp.format(new Date()) + "\r\n";
                    serverResponseBW.write(response);
                    serverResponseBW.flush();
                } else {
                    response = "HTTP/1.0 200 OK\n" + timeStamp.format(new Date()) + "\r\n";
                    serverResponseBW.write(response);
                    serverResponseBW.flush();
                    ImageIO.write(im, fileType.substring(1), cSocket.getOutputStream());
                }
            }

            // if the file is purely text
            else {
                BufferedReader cacheFileBR = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                response = "HTTP/1.0 200 OK\n" + timeStamp.format(new Date()) + "\r\n";
                serverResponseBW.write(response);

                String line;
                while ((line = cacheFileBR.readLine()) != null) {
                    serverResponseBW.write(line);
                }
                serverResponseBW.flush();
                cacheFileBR.close();
            }
        } catch (IOException e) {
            System.out.println("Error occurred sending cached file to client");
            e.printStackTrace();
        }
    }

    /**
     * Tries to obtain the requested contents from the server
     * and sends it back to the client.
     * Stores the contents in the cache.
     */

    public void obtainContent(String url) throws IOException {

        SimpleDateFormat timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String keyURL = url;
        // get the file name and replace the illegal characters
        String fileName = url.replace("/", "__");
        fileName = fileName.replace(".", "_");
        // Obtain the class of the file
        int fileTypeIdx = url.lastIndexOf('.');
        String fileType = url.substring(fileTypeIdx);

        if (fileType.contains("/")) fileType += ".html";
        else if (url.contains(".")) fileName += fileType;

        //make cache file
        File cacheFile = null;
        BufferedWriter cacheFileBW = null;
        int count = 0;
        try {
            File dir = new File("CachedFiles");
            if (!dir.exists()) {
                dir.mkdir();
            }
            cacheFile = new File("CachedFiles/" + fileName);
            if (!cacheFile.exists()) {
                cacheFile.createNewFile();
            }
            cacheFileBW = new BufferedWriter(new FileWriter(cacheFile));
        } catch (NullPointerException e) {
            System.out.println("failed to create a cache file");
        }
        if (!url.contains("www.bom.gov.au")) {
            url = "http://www.bom.gov.au" + url.substring(6);
        }
        URL urlAddr = new URL(url);
        //Read a image
        // if (fileType.contains(".jpg") || fileType.contains(".jpeg") || fileType.contains(".png") || fileType.contains(".gif") || fileType.contains(".ico")) {
        //     URL newURL = new URL(url);
        //     URLConnection uConn = newURL.openConnection();
        //     uConn.setRequestProperty(
        //     "User-Agent",
        //     "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
        //     BufferedImage img = ImageIO.read(uConn.getInputStream());

        //     if (img == null) {
        //         serverResponseBW.write("HTTP/1.0 404 NOT FOUND \n" + "\r\n");
        //         serverResponseBW.flush();
        //     } else {
        //         //read text
        //         serverResponseBW.write("HTTP/1.0 200 OK\n" + "\r\n");
        //         serverResponseBW.flush();
        //         // write to cache file
        //         ImageIO.write(img, fileType.substring(1), cacheFile);
        //         // write to socket.
        //         ImageIO.write(img, fileType.substring(1), this.cSocket.getOutputStream());
        //     }
        //Read a image
        if((url.substring(url.lastIndexOf(".")).contains(".jpg"))||(url.substring(url.lastIndexOf(".")).contains(".jpeg"))||(url.substring(url.lastIndexOf(".")).contains(".png"))||(url.substring(url.lastIndexOf(".")).contains(".gif"))){
            URL image = new URL(url);
            URLConnection uc = image.openConnection();
            uc.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            BufferedImage img = ImageIO.read(uc.getInputStream());
            System.out.println("image"+img);
            if(img==null){
                System.out.println("Image " + fileName + " was null");
                serverResponseBW.write("HTTP/1.0 404 NOT FOUND \n" + "\r\n");
                serverResponseBW.flush();
            }else{
                //read text
                serverResponseBW.write("HTTP/1.0 200 OK\n"  + "\r\n");
                serverResponseBW.flush();
                // write to cache file
                ImageIO.write(img, url.substring(url.lastIndexOf(".")).substring(1),cacheFile);
                // write to socket.
                ImageIO.write(img, url.substring(url.lastIndexOf(".")).substring(1),cSocket.getOutputStream());
            }
        } else {
            URLConnection proxy2Server = (URLConnection) urlAddr.openConnection();
            proxy2Server.setRequestProperty("User-Agent", "Mozilla");

            BufferedReader cacheBR = new BufferedReader(new InputStreamReader(proxy2Server.getInputStream()));

            serverResponseBW.write("HTTP/1.0 200 OK\n" + "\r\n");
            String line;
            while ((line = cacheBR.readLine()) != null) {
                //Modifies the content, by replacing displayed Australian capital city names with random city
                String modification = line;
                // Count the number of times we modify
                String[] allWords = line.split("//s+");
                for (int i = 0; i < allWords.length; i++) {
                    if (allWords[i].equals("Canberra") || allWords[i].equals("ACT")) count++;
                }

                if (line.contains("Canberra")) {
                    modification = line.replace("Canberra", "Sillicon Valley");
                }

                if (line.contains("ACT")) {
                    modification = line.replace("ACT", "California");
                }

                serverResponseBW.write(modification);
                //without rename that link
                cacheFileBW.write(line);

            }
            serverResponseBW.flush();
            if (cacheBR != null) cacheBR.close();
        }
        cacheFileBW.flush();
        this.cache.put(keyURL, cacheFile);

        if (cacheFileBW != null) cacheFileBW.close();
        if (serverResponseBW != null) serverResponseBW.close();
        System.out.println("The number of times we modified in this page is " + String.valueOf(count));
    }

}