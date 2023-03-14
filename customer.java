
//Importing the Required Library
import java.io.IOException;
import java.io.*;
import java.util.Scanner;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class customer {

    public static HashMap<String, customerInfo> cohortList = new HashMap<>();
    public static customerInfo tempState;
    public static customerInfo globalState;

    public static void main(String args[]) throws IOException {

        Socket socket = null;
        try {
            //Creating a New Socket Object and Assigning the Server IP address and port
            socket = new Socket("localhost", 14000);
//				//Setting the Socket as Reusable for multiple thread option.
//                socket.setReuseAddress(true);

            //Creating a Object of a class to Handle Received mesagges sent from server
            receieServer msgFromServer = new receieServer(socket);
            //Creating a thread of the same, to run the receiving and sending process parallel
            Thread thread = new Thread(msgFromServer);
            //Starting the Thread
            thread.start();

            //Creating a class to Handle Message sent to server
            //Passing both the socket and the Thread used to receive messages fro server
            sendServer msgToServer = new sendServer(socket, thread);
            Thread thread2 = new Thread(msgToServer);
            //Creating and Running a Thread
            thread2.start();

            //Checking till the time both the Thread are Alive
            while (thread.isAlive() && thread2.isAlive()) {
            }

            //Closing the Socket, to prevent errors in the Server Side
            socket.close();
        } //Catching any server/client side exception
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Declaring the class to Handle process to sedn input to server
    private static class sendServer implements Runnable {

        //Declaring the private varibles
        private final Socket serverSocket;
        private static Thread serverThread;

        //Overloaded Constructor
        public sendServer(Socket socket, Thread serverThread) {
            serverSocket = socket;
            this.serverThread = serverThread;
        }

        //Creating a PW object
        PrintWriter out = null;
        //Defining the Runnable Object

        public void run() {

            try {
                //Initiating the PrintWriter Object
                out = new PrintWriter(serverSocket.getOutputStream(), true);
                //Initializing the Scanner Object
                Scanner sc = new Scanner(System.in);
                String line = null;

                //Keeping the Input line open until the Receiver Thread is Open
                while (serverThread.isAlive()) {
                    //Reading the Line from Command Line
                    line = sc.nextLine();
                    String[] commands = line.split(" ");

                    switch (commands[0]) {
                        case "open":
                            globalState = new customerInfo(commands[1], Double.parseDouble(commands[2]), commands[3], Integer.parseInt(commands[4]), Integer.parseInt(commands[5]));
                            tempState = new customerInfo(commands[1], Double.parseDouble(commands[2]), commands[3], Integer.parseInt(commands[4]), Integer.parseInt(commands[5]));
                            miniServer clientServer = new miniServer(Integer.parseInt(commands[5]));
                            new Thread(clientServer).start();
                            out.println(line);
                            out.flush();
                            break;
                        case "new-cohort":
                            out.println(line);
                            out.flush();
                            break;
                        case "delete-cohort":
                            out.println(line);
                            out.flush();
                            break;
                        case "exit":
                            out.println(line);
                            out.flush();
                            break;
                        case "deposit":
                            int dAmount = Integer.parseInt(commands[1]);
                            tempState.setBalance(tempState.getBalance() + dAmount);
                            System.out.println("The new Balance is " + tempState.getBalance() + "\n");
                            break;
                        case "withdrawal":
                            int wAmount = Integer.parseInt(commands[1]);
                            if ((tempState.getBalance() - wAmount) < 0) {
                                System.out.println("No Sufficient Balance to Withdraw");
                                break;
                            }
                            tempState.setBalance(tempState.getBalance() - wAmount);
                            System.out.println("The new Balance is " + tempState.getBalance() + "\n");
                            break;
                        case "transfer":
                            int amt = Integer.parseInt(commands[1]);
                            String q = commands[2];

                            if (cohortList.isEmpty() || !cohortList.containsKey(q)) {
                                System.out.println("There are no customers in your cohort");
                                break;
                            }

                            if ((tempState.getBalance() - amt) < 0) {
                                System.out.println("Not Sufficient Funds");
                                break;
                            }

                            tempState.setBalance(tempState.getBalance() - amt);

                            String ip = cohortList.get(q).getIPv4();
                            int port = cohortList.get(q).getPortB();
                            
                            int labelUpdate = Integer.parseInt(q.substring(q.length()-1 , q.length()));
                            tempState.getFirst_label_sent()[labelUpdate]++; 
                            
                            String tCommand = "transfer " + amt + " " + q + " " + tempState.getCustomerName() + " " + tempState.getFirst_label_sent()[labelUpdate];
                            Socket socket = new Socket(ip, port);
                            PrintWriter o = new PrintWriter(socket.getOutputStream(), true);
                            
                            System.out.println("The Trasnfer was Successfull , the new balance is" + tempState.getBalance() + " " + tempState.getFirst_label_sent()[labelUpdate] +"-"+ tempState.getLast_label_recv()[labelUpdate]);

                            o.println(tCommand);
                            o.flush();
                            o.close();
                            break;
                        default:
                            out.println(line);
                            out.flush();
                            break;

                    }

                    //Putting the Server to Sleep, for waiting the other thread to finish reading from the server output.
                    Thread.sleep(500);
                }

                //Closing Scanner and PW object.
                sc.close();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static class miniServer implements Runnable {

        private int port;
        private ServerSocket server = null;

        public miniServer(int port) throws SocketException, IOException {
            this.port = port;
            server = new ServerSocket(this.port);
            server.setReuseAddress(true);
        }

        @Override
        public void run() {

            try {
                while (true) {
                    Socket client = server.accept();
                    ClientHandler clientSock = new ClientHandler(client);
                    new Thread(clientSock).start();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static class ClientHandler implements Runnable {

        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;

            try {

                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String input;
                while (!clientSocket.isClosed() && (input = in.readLine()) != null) {
                    String command = "";
                    String[] line = input.split(" ");
                    command = line[0];
                    switch(command){
                        case "transfer":
                            int amt = Integer.parseInt(line[1]);
                            String p = line[2];
                            int updateLabel = Integer.parseInt(line[3].substring(line[3].length()-1 , line[3].length()));
                            
                            if((tempState.getLast_label_recv()[updateLabel] - Integer.parseInt(line[4])) > 1 ){
                                //checkrollBack();
                            }
                            tempState.getLast_label_recv()[updateLabel] = Integer.parseInt(line[4]);
                            tempState.setBalance(tempState.getBalance() + amt);
                            
                            System.out.println("The Trasnfer was Successfull , the new balance is" + tempState.getBalance() + " " + tempState.getFirst_label_sent()[updateLabel] +" - "+ tempState.getLast_label_recv()[updateLabel]);
                            
                            
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //Declaring the class to Handle process to receive the output from server
    private static class receieServer implements Runnable {
        //Declaring the private varibles

        private final Socket serverSocket;
        //Overloaded Constructor

        public receieServer(Socket socket) throws IOException {
            serverSocket = socket;
            //serverSocket = new Socket(ip, port);
            //serverSocket.setReuseAddress(true);
            //Setting the Socket as Reusable for multiple thread option.
        }
        //Creating a BR object

        BufferedReader in = null;
        ObjectInputStream obj = null;
        //Defining the Runnable Object

        public void run() {
            try {

                //Initiating the Buffered Writer Object
                in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                System.out.println("Connected to Bank :\n");
                String reply;
                //Reading all responce from server, line-by-line till it reads 'disconnected'
                boolean cohortAdd = false;

                while (!(reply = in.readLine()).equals("disconnected")) {
//                    if(!reply.substring(0, 4).equals("Cust")){
//                        cohortAdd = false;
//                    }    
//                    
//                    if(cohortAdd){
//                        String[] customer = reply.split("\t");
//                        System.out.println(customer[0] + "-" + customer[1] + "-" + customer[2] + "-" + customer[3] + "-" + customer[4]);
//                    }
//                    
//                    if(reply.substring(0, 4).equals("Cust")){
//                        cohortAdd = true;
//                    }

                    if (!reply.equals('\n') && reply.charAt(0) >= 48 && reply.charAt(0) <= 57) {

                        int nunOfCustomer = Integer.parseInt(reply);
                        reply = in.readLine();
                        System.out.println(reply);

                        reply = in.readLine();
                        System.out.println(reply);

                        reply = in.readLine();
                        System.out.println(reply);

                        //ArrayList<customerInfo> list = new ArrayList<>();
                        for (int i = 1; i <= nunOfCustomer; i++) {

                            reply = in.readLine();
                            String[] customer = reply.split("\t");
                            System.out.println(reply);
                            //System.out.println(customer[0] + "-" + customer[1] + "-" + customer[2] + "-" + customer[3] + "-" + customer[4]);
                            customerInfo newCustomer = new customerInfo(customer[0], Double.parseDouble(customer[1]), customer[2], Integer.parseInt(customer[3]), Integer.parseInt(customer[4]));
                            cohortList.put(customer[0], newCustomer);

                            //receieServer msgFromServer = new receieServer(customer[2] ,Integer.parseInt(customer[4]));
                            //Creating a thread of the same, to run the receiving and sending process parallel
                            //Thread thread = new Thread(msgFromServer);
                            //Starting the Thread
                            //thread.start(); 
                        }

                        System.out.println("");

                    } else {
                        System.out.println(reply);
                    }

                }
                //Closing the BR object.
                in.close();
                obj.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}
