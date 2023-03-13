
//Importing the Required Library
import java.io.IOException;
import java.io.*;
import java.util.Scanner;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class customer {
    
    public ArrayList<customerInfo> cohortList = new ArrayList<>();
    
    public static void main(String args[]) throws IOException {
            Socket socket = null;
            try{
				//Creating a New Socket Object and Assigning the Server IP address and port
                socket = new Socket("localhost", 14000);
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
                boolean startCohort = false;
                                
                while(!(reply = in.readLine()).equals("disconnected")) {
                    if(reply.equals("You have been added to the cohort")){
                        try{
                            ObjectInputStream obj = new ObjectInputStream(serverSocket.getInputStream());
                            Object object = obj.readObject();
                            ArrayList<customerInfo> arr = (ArrayList<customerInfo>) object;
                            arr.get(0).printCohort();
                            obj.close();
                            Thread.sleep(500);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    System.out.println(reply);
                    
                }
				//Closing the BR object.
                in.close();
            } catch (Exception e) {
               e.printStackTrace();
            }
            
        }
        

             
    }
    
    
    public static class customerInfo implements Serializable {

        String customerName;
        double balance;
        String IPv4;
        int portA;
        int portB;
        Socket clientSocket;
        ArrayList<customerInfo> cohort = new ArrayList<customerInfo>();

        //customerInfo class defines the properties of the customer such as the name, balance, IPv4address etc.
        public customerInfo(String customerName, double balance, String IPv4, int portA, int portB, Socket clientSocket) {
            
            //Customer information 
            this.customerName = customerName;
            this.balance = balance;
            this.IPv4 = IPv4;
            this.portA = portA;
            this.portB = portB;
            cohort = null;
            this.clientSocket = clientSocket;
        }
        //Getters and Setter Functions

        public Socket getClientSocket() {
            return clientSocket;
        }

        public void setClientSocket(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public List<customerInfo> getCohort() {
            return cohort;
        }

        public void setCohort(ArrayList<customerInfo> cohort) {
            this.cohort = cohort;
        }

        public String getName() {
            return this.customerName;
        }

        public String getCustomerName() {
            return customerName;
        }

        public double getBalance() {
            return balance;
        }

        public String getIPv4() {
            return IPv4;
        }

        public int getPortA() {
            return portA;
        }

        public int getPortB() {
            return portB;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }

        public void setIPv4(String IPv4) {
            this.IPv4 = IPv4;
        }

        public void setPortA(int portA) {
            this.portA = portA;
        }

        public void setPortB(int portB) {
            this.portB = portB;
        }

        //Prints Customer Information
        public void printCustomer() {
            System.out.println("Customer Name : " + this.customerName);
            System.out.println("Customer Balance : " + this.balance);
            System.out.println("Cutomer IPv4 : " + this.IPv4);
            System.out.println("PortA : " + this.portA);
            System.out.println("PortB : " + this.portB + "\n");
        }

        public void printCohort() {
            if (this.cohort.isEmpty()) {
                System.out.println("Do not have a cohort");
                return;
            }
            for (customerInfo i : cohort) {
                i.printCustomer();
            }
        }

    }
   
}
