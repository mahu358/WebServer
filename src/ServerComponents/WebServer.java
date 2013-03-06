/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ServerComponents;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Set;




/**
 *
 * @author Matus
 */
public class WebServer{

    private static WebServer instance = null;
    private HttpServer server;
    
    public WebServer() throws Exception {
    
        InetSocketAddress socket = new InetSocketAddress(8000);
        server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
}

        public static WebServer getInstance() throws Exception {
            if(instance == null) {
                instance = new WebServer();
            }
                return instance;
        }
        
     public void startServer(){
         
     this.server.start();
     
     }
     
     public void stopServer(){
     
     this.server.stop(1);
     instance =null;
     }
     
     public Boolean isRunning(){
     if(server.getExecutor()!=null)
         return true;
     else
         return false;
     }
     
     public void setServerPort(int port) throws IOException{
     
     this.server.bind(null, port);
     
     }

     
class MyHandler implements HttpHandler {
    
    

  public void handle(HttpExchange t) throws IOException {
      
      
    String root = "C:/Users/Matus/Desktop/emmas_stranka_2";

    URI uri = t.getRequestURI();
    System.out.println(root + uri.getPath());
    File file = new File(root + uri.getPath()).getCanonicalFile();
    System.out.println(file.getPath());
    /*
    if (!file.getPath().startsWith(root)) {
      // Suspected path traversal attack: reject with 403 error.
      String response = "403 (Forbidden)\n";
      t.sendResponseHeaders(403, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    } else if (!file.isFile()) {
    * 
    */
    if (!file.isFile()) {
      // Object does not exist or is not a file: reject with 404 error.
      String response = "404 (Not Found)\n";
      t.sendResponseHeaders(404, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    } else {
      // Object exists and is a file: accept with response code 200.
      t.sendResponseHeaders(200, 0);
      OutputStream os = t.getResponseBody();
      FileInputStream fs = new FileInputStream(file);
      final byte[] buffer = new byte[0x10000];
      int count = 0;
      while ((count = fs.read(buffer)) >= 0) {
        os.write(buffer,0,count);
      }
      fs.close();
      os.close();
    }
  }


}

}