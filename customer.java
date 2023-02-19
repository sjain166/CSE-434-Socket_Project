
import java.io.IOException;
import java.io.*;
import java.util.Scanner;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class customer {

    public static void main(String args[]) throws IOException {
            Socket socket = null;
            try{
                socket = new Socket("localhost", 14000);
                socket.setReuseAddress(true);
                
                sendServer msgToServer = new sendServer(socket);
                receieServer msgFromServer = new receieServer(socket);
                
                new Thread(msgToServer).start();
                new Thread(msgFromServer).start();
                
            }
            catch(Exception e){
                e.printStackTrace();
            }
        
    }
    
    private static class sendServer implements Runnable{
        private final Socket serverSocket;
        
        public sendServer(Socket socket){
            serverSocket = socket;
        }
        PrintWriter out = null; 
        public void run() {
            try{
                out = new PrintWriter(serverSocket.getOutputStream(), true);
                Scanner sc = new Scanner(System.in);
                String line = null;
                
                while (!serverSocket.isClosed()) {
                 line = sc.nextLine();
                 // sending the user input to server
                 out.println(line);
                 out.flush();    
                }
                
                sc.close();  
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        
    }
    
    private static class receieServer implements Runnable{
        private final Socket serverSocket;
        public receieServer(Socket socket){
            serverSocket = socket;
        }
        BufferedReader in = null; 
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                System.out.println("Connected to Bank :\n");
                String reply;
                while(!(reply = in.readLine()).equals("disconnected")) {
                    System.out.println(reply);
                }
                serverSocket.close();
                
            } catch (Exception e) {
               e.printStackTrace();
            }
            
        }
             
    }
   
}
