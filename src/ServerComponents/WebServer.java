/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ServerComponents;

import com.sun.net.httpserver.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JLabel;
import javax.swing.JTextArea;




/**
 *
 * @author Matus
 */
public class WebServer{

    private static WebServer instance = null;
    private HttpServer server;
    private String contentPath;
    private InetSocketAddress socket;
    private BasicAuthenticator basicAuthenticator;
    private HttpContext context;
    public  String login,password;
    private Boolean passwordAuthentication;
    private JTextArea jTextArea1;
    private Calendar cal;
    private DateFormat dateFormat;
    private int port;
    
    public WebServer(int port,String login,String password,Boolean passwordAuthentication,JTextArea jTextArea1) throws Exception {
        this.port=port;
        socket = new InetSocketAddress(this.port);
        server = HttpServer.create(socket, 0);
        this.login=login;
        this.password=password;
        this.passwordAuthentication=passwordAuthentication;
        context = server.createContext("/", new MyHandler(this.login,this.password,this.passwordAuthentication));
        server.setExecutor(null);
        this.jTextArea1=jTextArea1;
        cal = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    }

    public static WebServer getInstance(int port,String login,String password,Boolean passwordAuthentication,JTextArea jTextArea1) throws Exception {
            if(instance == null) {
                instance = new WebServer(port,login,password,passwordAuthentication,jTextArea1);
            }
                return instance;
        }
        
     public void startServer(JLabel jlabel16, JLabel jlabel17,JLabel jlabel18){
     jlabel16.setText(contentPath);
     jlabel17.setText(Integer.toString(this.port));    
     jlabel18.setText( (passwordAuthentication) ? "Enabled" : "Disabled");
     this.server.start();
     jTextArea1.append(dateFormat.format(cal.getTime()) +" Server started on Port "+this.server.getAddress().getPort()+"\n");
     }
     
     public void stopServer(){
     jTextArea1.append(dateFormat.format(cal.getTime()) +" Server stopped \n");
     this.server.stop(1);
     instance =null;
     }
     
     public Boolean isRunning(){
     if(server.getExecutor()!=null)
         return true;
     else
         return false;
     }
          
     public void setContentPath(String path) {
         contentPath = path;

}
    public void setAuhtData(String login,String password,Boolean passwordAuthentication){
        this.passwordAuthentication=passwordAuthentication;
        this.password=password;
        this.login=login;
        
}

     
private class MyHandler implements HttpHandler {

    public String postedlogin=null,postedpassword = null;
    public String reqlogin,reqpassword;
    public Boolean Authentication;
    public HttpExchange httpexch;
    
    
    public MyHandler(String login,String password,Boolean Authentication){
    this.Authentication = Authentication;
    this.reqlogin=login;
    this.reqpassword=password;
    }
    
  
  @Override
  public void handle(HttpExchange t) throws IOException {
      
      
      if(passwordAuthentication==true){
            basicAuthenticator = new BasicAuthenticator("cPanel") {
            
 
            @Override
            public boolean checkCredentials(String login, String password) {        
                if(login.equals(reqlogin)&&password.equals(reqpassword)){
                return true;
                }
                else{
                return false;
                }
                
            }
        
    };

    if(basicAuthenticator.authenticate(t) instanceof BasicAuthenticator.Failure ){
        jTextArea1.append(dateFormat.format(cal.getTime()) +" " + t.getRequestMethod().toString()+" "+t.getRemoteAddress().getHostString()+" "+t.getRemoteAddress().getPort()+" Auhtentication Failure\n");

         String response = "<html><head><title>Bad Login/Password Auhentication Required !</title></head><body><h1>Bad Login/Password</h1><p>You supplied the wrongcredentials (e.g., bad password), or yourbrowser doesn't understand how to supplythe credentials required.</p></body></html>";
         t.sendResponseHeaders(401, response.length());
         try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }
        
        }
    
    if(basicAuthenticator.authenticate(t) instanceof BasicAuthenticator.Retry ){
        jTextArea1.append(dateFormat.format(cal.getTime()) +" " + t.getRequestMethod().toString()+" "+t.getRemoteAddress().getHostString()+" "+t.getRemoteAddress().getPort()+" Requesting Auhtentication\n");
                 String response = "<html><head><title>Auhentication Required !</title></head><body><h1>Auhentication Required !</h1></body></html>";
         t.sendResponseHeaders(401, response.length());
         try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }
        
        }
       
    
    if(basicAuthenticator.authenticate(t) instanceof BasicAuthenticator.Success){
        getResponseData(t);
    }
    }
      
      
      else
      
      {
      getResponseData(t);
      }
    
    

    
    

    }


   public void getResponseData(HttpExchange t) throws IOException{
      
      
      File file;
    URI uri = t.getRequestURI();

    if(uri.toString().length()==1){
        
        file = new File(contentPath + "/index.html").getCanonicalFile();
    }
    else{
        file = new File(contentPath + uri.getPath()).getCanonicalFile();
    }
    String authstring = (passwordAuthentication) ? " Auth-OK " : " ";
    jTextArea1.append(dateFormat.format(cal.getTime())+" " + t.getRequestMethod().toString()+authstring+t.getRemoteAddress().getHostString()+" "+t.getRemoteAddress().getPort()+" "+uri.toString()+"\n");
    
    if (!file.getPath().startsWith(contentPath)) {
     
      String response = "403 (Forbidden)\n";
      t.sendResponseHeaders(403, response.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }
    } else if (!file.isFile()) {
   
      String response = "404 (Not Found)\n";
      t.sendResponseHeaders(404, response.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }
    } else {
   
      t.sendResponseHeaders(200, 0);
                try (OutputStream os = t.getResponseBody(); 
                     FileInputStream fs = new FileInputStream(file)) 
                {
                    final byte[] buffer = new byte[0x10000];
                    int count = 0;
                    while ((count = fs.read(buffer)) >= 0) {
                      os.write(buffer,0,count);
                    }
                }
                
    }
    t.getResponseBody().close();
      
      
      
      
      }








  
    }





















    
  }




