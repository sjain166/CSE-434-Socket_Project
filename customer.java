
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
                socket = new Socket("10.120.70.113", 14000);
                socket.setReuseAddress(true);
                
                receieServer msgFromServer = new receieServer(socket);
                Thread thread = new Thread(msgFromServer);
                thread.start(); 
                
                sendServer msgToServer = new sendServer(socket , thread);
                Thread thread2 = new Thread(msgToServer);
                thread2.start();
                
                while(thread.isAlive() && thread2.isAlive()){                 
                }
                
                socket.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        
    }
    
    private static class sendServer implements Runnable{
        private final Socket serverSocket;
        private static Thread serverThread;
        
        public sendServer(Socket socket , Thread serverThread){
            serverSocket = socket;
            this.serverThread = serverThread;
        }
        
        PrintWriter out = null; 
        public void run() {
            try{
                out = new PrintWriter(serverSocket.getOutputStream(), true);
                Scanner sc = new Scanner(System.in);
                String line = null;
                
                while (serverThread.isAlive()) {
                 line = sc.nextLine();
                 // sending the user input to server
                 out.println(line);
                 out.flush();
                 Thread.sleep(500);
                }
                
                sc.close();
                out.close();
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
                in.close();
                //System.out.println("Reading Thread Disconnected");
            } catch (Exception e) {
               e.printStackTrace();
            }
            
        }
             
    }
   
}
