
//Importing the Required Library
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
				//Creating a New Socket Object and Assigning the Server IP address and port
                socket = new Socket("10.120.70.113", 14000);
				//Setting the Socket as Reusable for multiple thread option.
                socket.setReuseAddress(true);
                
				//Creating a Object of a class to Handle Received mesagges sent from server
                receieServer msgFromServer = new receieServer(socket);
				//Creating a thread of the same, to run the receiving and sending process parallel
                Thread thread = new Thread(msgFromServer);
				//Starting the Thread
                thread.start(); 
                
				//Creating a class to Handle Message sent to server
				//Passing both the socket and the Thread used to receive messages fro server
                sendServer msgToServer = new sendServer(socket , thread);
                Thread thread2 = new Thread(msgToServer);
				//Creating and Running a Thread
                thread2.start();
                
				//Checking till the time both the Thread are Alive
                while(thread.isAlive() && thread2.isAlive()){                 
                }
                
				//Closing the Socket, to prevent errors in the Server Side
                socket.close();
            }
			//Catching any server/client side exception
            catch(Exception e){
                e.printStackTrace();
            }
        
    }
	
	//Declaring the class to Handle process to sedn input to server
    private static class sendServer implements Runnable{
		
		//Declaring the private varibles
        private final Socket serverSocket;
        private static Thread serverThread;
        
		//Overloaded Constructor
        public sendServer(Socket socket , Thread serverThread){
            serverSocket = socket;
            this.serverThread = serverThread;
        }
        
		//Creating a PW object
        PrintWriter out = null;
		//Defining the Runnable Object
        public void run() {
			
            try{
				//Initiating the PrintWriter Object
                out = new PrintWriter(serverSocket.getOutputStream(), true);
				//Initializing the Scanner Object
                Scanner sc = new Scanner(System.in);
                String line = null;
                
				//Keeping the Input line open until the Receiver Thread is Open
                while (serverThread.isAlive()) {
				 //Reading the Line from Command Line
                 line = sc.nextLine();
                 // sending the user input to server
                 out.println(line);
                 out.flush();
				 //Putting the Server to Sleep, for waiting the other thread to finish reading from the server output.
                 Thread.sleep(500);
                }
                
				//Closing Scanner and PW object.
                sc.close();
                out.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        
    }
    
	//Declaring the class to Handle process to receive the output from server
    private static class receieServer implements Runnable{
		//Declaring the private varibles
        private final Socket serverSocket;
		//Overloaded Constructor
        public receieServer(Socket socket){
            serverSocket = socket;
        }
		//Creating a BR object
		
        BufferedReader in = null; 
		//Defining the Runnable Object
        public void run() {
            try {
			
				//Initiating the Buffered Writer Object
                in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                System.out.println("Connected to Bank :\n");
                String reply;
				//Reading all responce from server, line-by-line till it reads 'disconnected'
                while(!(reply = in.readLine()).equals("disconnected")) {
                    System.out.println(reply);
                }
				//Closing the BR object.
                in.close();
            } catch (Exception e) {
               e.printStackTrace();
            }
            
        }
             
    }
   
}
