
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
                socket = new Socket("localhost", 1234);
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
                
                while (!"!!!!!".equalsIgnoreCase(line)) {
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
                System.out.println("Server Replied :");
                String reply;
                while((reply = in.readLine()) != null ) {
                    System.out.println(reply);
                }
                
            } catch (Exception e) {
               e.printStackTrace();
            }
            
        }
             
    }
//        try ( Socket socket = new Socket("localhost", 1234)) {
//
//            // writing to server
//            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//
//            // reading from server
//            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//            // object of scanner class
//            Scanner sc = new Scanner(System.in);
//            String line = null;
//
//            while (!"exit".equalsIgnoreCase(line)) {
//
//                // reading from user
//                
//                line = sc.nextLine();
//
//                // sending the user input to server
//                out.println(line);
//                out.flush();
//                
//                String valueFromServer = in.readLine();
//                for(int i = 0 ; i < valueFromServer.length(); i++){
//                    if(valueFromServer.charAt(i) == '#'){
//                        System.out.println("");
//                    }
//                    else
//                        System.out.print(valueFromServer.charAt(i));
//                }
//                System.out.println("");
//                
//                // displaying server reply
//                
////                System.out.println("Server Replied :");
////                String reply;
////                while (!(reply = in.readLine()).equals("")) {
////                    System.out.println(reply);
////                    System.out.println("123");
////                }
//
////                do{
////                   System.out.println("Server replied "+ in.readLine()); 
////                }while(in.readLine() != null);
//                
//            }
//
//            // closing the scanner object
//            sc.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }   

    
}
