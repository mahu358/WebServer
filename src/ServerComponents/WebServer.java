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
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import org.apache.commons.net.util.SubnetUtils;





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
    public  String login,password;
    private Boolean passwordAuthentication;
    private JTextArea logTextArea;
    private Calendar cal;
    private DateFormat dateFormat;
    private int port;
    private Vector<String> ipAddressesList;
    private Boolean ipVerification;
    private Boolean isWhiteList;
    
    public WebServer(int port,String login,String password,Boolean passwordAuthentication,JTextArea logTextArea) throws Exception {
        this.port=port;
        socket = new InetSocketAddress(this.port);
        server = HttpServer.create(socket, 0);
        this.login=login;
        this.password=password;
        this.passwordAuthentication=passwordAuthentication;
        server.createContext("/", new MyHandler(this.login,this.password,this.passwordAuthentication));
        server.setExecutor(null);
        this.logTextArea=logTextArea;
        cal = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        this.ipAddressesList = new Vector<>();

    }

    public static WebServer getInstance(int port,String login,String password,Boolean passwordAuthentication,JTextArea jTextArea1) throws Exception {
            if(instance == null) {
                instance = new WebServer(port,login,password,passwordAuthentication,jTextArea1);
            }
                return instance;
        }
        
     public void startServer(JLabel currentServerContentLabel,JLabel currentServerPortLabel,JLabel currentBasicAuthStatusLabel,JLabel currentIPVerificationStatusLabel){
     currentServerContentLabel.setText(contentPath);
     currentServerPortLabel.setText(Integer.toString(this.port));    
     currentBasicAuthStatusLabel.setText( (passwordAuthentication) ? "Enabled" : "Disabled");
     if (ipVerification)
        {
            currentIPVerificationStatusLabel.setText( (isWhiteList) ? "White List Enabled" : "Black List Enabled");
        }
     else
        {
            currentIPVerificationStatusLabel.setText("Disabled");
        }
     this.server.start();
     this.logTextArea.append(dateFormat.format(cal.getTime()) +" Server started on Port "+this.server.getAddress().getPort()+"\n");

     }
     
     public void stopServer(){
     logTextArea.append(dateFormat.format(cal.getTime()) +" Server stopped \n");
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
    public void setAuhtData(String login,String password,Boolean passwordAuthentication,Boolean ipVerification,Vector<String> ipList,Boolean isWhiteList){
        this.passwordAuthentication=passwordAuthentication;
        this.ipVerification=ipVerification;
        this.password=password;
        this.login=login;
        this.ipAddressesList.clear();
        if(ipVerification)
            for(String copylist: ipList){
                this.ipAddressesList.add(copylist);
            }
        
        this.isWhiteList=isWhiteList;
}

     
    public void showSecuritySettingsInLog() {
        if(passwordAuthentication){
        logTextArea.append("\nBasic Login/Password Authentication-Enabled\n"+"Login:" + this.login +"\n"+"Password:" + this.password+"\n");
        }
        else{
        logTextArea.append("\nBasic Login/Password Authentication-Disabled\n");
        }
        
        if(ipVerification){
        String str=(isWhiteList) ? "White List\n" : "Black List Enabled\n";
        logTextArea.append("\nIP Authentication-Enabled\n"+"Method-" + str +"IP Addresses in list:");
        for(String ip: ipAddressesList){
            logTextArea.append(ip+"\n                     ");
        }
        logTextArea.append("\n");
        }
        else
        {
        logTextArea.append("\nIP Authentication-Disabled\n");
        }

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
      
      
      if(   passwordAuthentication ){
          
          if(   !ipVerification || (ipVerification&&isValidIp(t.getRemoteAddress().getAddress().toString().substring(1)))     ){
          
            basicAuthenticator = new BasicAuthenticator("Web Server Authentication") {
            
 
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
        logTextArea.append(dateFormat.format(cal.getTime()) +" " + t.getRequestMethod().toString()+" "+t.getRemoteAddress().getHostString()+" "+t.getRemoteAddress().getPort()+" Auhtentication Failure\n");

         String response = "<html><head><title>Bad Login/Password Auhentication Required !</title></head><body><h1>Bad Login/Password</h1><p>You supplied the wrongcredentials (e.g., bad password), or yourbrowser doesn't understand how to supplythe credentials required.</p></body></html>";
         t.sendResponseHeaders(401, response.length());
         try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }
        
        }
    
    if(basicAuthenticator.authenticate(t) instanceof BasicAuthenticator.Retry ){
        logTextArea.append(dateFormat.format(cal.getTime()) +" " + t.getRequestMethod().toString()+" "+t.getRemoteAddress().getHostString()+" "+t.getRemoteAddress().getPort()+" Requesting Basic Auhtentication\n");
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
          getResponseDataForDeniedIP(t);
          }
      }
      
      
      else
      
      { //System.out.println("          "+t.getRemoteAddress().getAddress().toString().substring(1));
          if(  (ipVerification&&isValidIp(t.getRemoteAddress().getAddress().toString().substring(1))) || !ipVerification   )
          {
              getResponseData(t);
          }
          else
          {
          getResponseDataForDeniedIP(t);
        }

      }
   
    }
    
    

    
    

    }

   public void getResponseDataForDeniedIP(HttpExchange t) throws IOException{
   
             logTextArea.append(dateFormat.format(cal.getTime()) +" " + t.getRequestMethod().toString()+" "+t.getRemoteAddress().getHostString()+" "+t.getRemoteAddress().getPort()+" IP Denied\n");
         String response;
          if(ipVerification)
          {
              response = "<html><head><title>Auhentication Failed, Access from your IP is denied  !</title></head><body><h1>Auhentication Failed, Access from your IP is denied  ! !</h1></body></html>";
          }
          else
          {
              response = "<html><head><title>Bad Login/Password Auhentication Required !</title></head><body><h1>Bad Login/Password</h1><p>You supplied the wrongcredentials (e.g., bad password), or yourbrowser doesn't understand how to supplythe credentials required.</p></body></html>";
          }
          t.sendResponseHeaders(200, response.length());
            try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
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
    logTextArea.append(dateFormat.format(cal.getTime())+" " + t.getRequestMethod().toString()+authstring+t.getRemoteAddress().getHostString()+" "+t.getRemoteAddress().getPort()+" "+uri.toString()+"\n");
    
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



    private Boolean isIPinList(String ipAddress){

     
        for(String ip: ipAddressesList){
            
            if(ip.contains("/")){

                SubnetUtils utils = new SubnetUtils(ip);
                if(utils.getInfo().isInRange(ipAddress))
                    return true;
            }
            else{
                if(ip.trim().equals(ipAddress.trim()))
                    return true;
            }
            
       }

        return false;
        
    }
    

    private Boolean isValidIp(String ipAddress){
    
        if(isWhiteList){
            if(isIPinList(ipAddress))
                return true;
        }
        else{
            if(!isIPinList(ipAddress))
                return true;
        
        }
    return false;
    }

  
    }







