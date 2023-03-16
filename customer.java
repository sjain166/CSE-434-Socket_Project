
//Importing the Required Library
import java.io.IOException;
import java.io.*;
import java.util.Scanner;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class customer {

    public static HashMap<String, customerInfo> cohortList = new HashMap<>();
    public static customerInfo tempState;
    public static customerInfo globalState;
    public static HashSet<customerInfo> chkt_cohort = new HashSet<>();
    public static int chk_counter = 0;
    public static int roll_counter = 0;
    public static Socket bankSocket = null;

    public static void main(String args[]) throws IOException {

        try {
            //Creating a New Socket Object and Assigning the Server IP address and port
            bankSocket = new Socket("localhost", 14000);
            
            //Creating a Object of a class to Handle Received mesagges sent from server
            receieServer msgFromServer = new receieServer(bankSocket);
            //Creating a thread of the same, to run the receiving and sending process parallel
            Thread thread = new Thread(msgFromServer);
            //Starting the Thread
            thread.start();

            //Creating a class to Handle Message sent to server
            //Passing both the socket and the Thread used to receive messages fro server
            sendServer msgToServer = new sendServer(bankSocket, thread);
            Thread thread2 = new Thread(msgToServer);
            //Creating and Running a Thread
            thread2.start();

            //Checking till the time both the Thread are Alive
            while (thread.isAlive() && thread2.isAlive()) {
            }

            //Closing the Socket, to prevent errors in the Server Side
            bankSocket.close();
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
                            //Creating the First copy of the Global and the Temporary States when the User register for the first time;
                            globalState = new customerInfo(commands[1], Double.parseDouble(commands[2]), commands[3], Integer.parseInt(commands[4]), Integer.parseInt(commands[5]));
                            tempState = new customerInfo(commands[1], Double.parseDouble(commands[2]), commands[3], Integer.parseInt(commands[4]), Integer.parseInt(commands[5]));
                            //Starting to Listen on the Port B for the User
                            miniServer clientServer = new miniServer(Integer.parseInt(commands[5]));
                            //Running a Separate Thread for Peer Listening
                            new Thread((Runnable) clientServer).start();
                            chkt_cohort.add(tempState);
                            out.println(line);
                            out.flush();
                            break;
                        case "new-cohort":
                            //Creating New Cohort Command
                            out.println(line);
                            out.flush();
                            break;
                        case "delete-cohort":
                            //Delete Cohort Command
                            out.println(line);
                            out.flush();
                            break;
                        case "exit":
                            //Exit Customer
                            out.println(line);
                            out.flush();
                            break;
                        case "deposit":
                            //To Deposit a Set amount on the Temp State of the User
                            int dAmount = Integer.parseInt(commands[1]);
                            tempState.setBalance(tempState.getBalance() + dAmount);
                            System.out.println("The new Balance after deposit is $" + tempState.getBalance() + "\n\n");
                            break;
                        case "withdrawal":
                            //Withdrawal
                            int wAmount = Integer.parseInt(commands[1]);
                            if ((tempState.getBalance() - wAmount) < 0) {
                                System.out.println("!!!No Sufficient Balance to Withdraw!!!\n\n");
                                break;
                            }
                            tempState.setBalance(tempState.getBalance() - wAmount);
                            System.out.println("The new Balance after withdrawal is $" + tempState.getBalance() + "\n\n");
                            break;
                        case "transfer":
                            //Initiating Transfer
                            int amt = Integer.parseInt(commands[1]);
                            String q = commands[2];

                            if (cohortList.isEmpty() || !cohortList.containsKey(q)) {
                                System.out.println("!!!There are no customers in your cohort!!! \n\n");
                                break;
                            }

                            if ((tempState.getBalance() - amt) < 0) {
                                System.out.println("!!!Not Sufficient Funds!!! \n\n");
                                break;
                            }

                            //Update the balance of Sender
                            tempState.setBalance(tempState.getBalance() - amt);

                            //Getting the IP and Port Information of Distint Client
                            String ip = cohortList.get(q).getIPv4();
                            int port = cohortList.get(q).getPortB();

                            int labelUpdate = Integer.parseInt(q.substring(q.length() - 1, q.length()));
                            tempState.getFirst_label_sent()[labelUpdate]++;

                            //Generating the Transfer string and the Lable
                            String tCommand = "transfer " + amt + " " + q + " " + tempState.getCustomerName() + " " + tempState.getFirst_label_sent()[labelUpdate];
                            //Creating a Socket for the Diistinct Client
                            Socket socket = new Socket(ip, port);
                            PrintWriter o = new PrintWriter(socket.getOutputStream(), true);
                            System.out.println("$" + amt + " has been sent to " + q + "\n\n");
                            //Pushing the Commands and Flushing
                            o.println(tCommand);
                            o.flush();
                            o.close();
                            break;
                        case "lost-transfer":
                            //Generating Fouls Transfers
                            int lAmt = Integer.parseInt(commands[1]);
                            q = commands[2];
                            if ((tempState.getBalance() - lAmt) < 0) {
                                System.out.println("!!!Not Sufficient Funds!!!");
                                break;
                            }

                            //Changing the Balance in the Local state of Client
                            tempState.setBalance(tempState.getBalance() - lAmt);
                            labelUpdate = Integer.parseInt(q.substring(q.length() - 1, q.length()));
                            tempState.getFirst_label_sent()[labelUpdate]++;
                            //Pushing the Commands and Flushing
                            ip = cohortList.get(q).getIPv4();
                            port = cohortList.get(q).getPortB();
                            socket = new Socket(ip, port);
                            o = new PrintWriter(socket.getOutputStream(), true);
                            //Generating Lost-Transfer Case
                            tCommand = "lost-transfer " + lAmt + " " + q + " " + tempState.getCustomerName();
                            System.out.println("$" + lAmt + " fake Transaction has been initiated for client" + q + "\n\n");
                            o.println(tCommand);
                            o.flush();
                            o.close();
                            break;

                        case "checkpoint":
                            //Pushing initiate-checkpoint to all the Peer in the chck_cohort
                            for (customerInfo i : chkt_cohort) {
                                socket = new Socket(i.getIPv4(), i.getPortB());
                                o = new PrintWriter(socket.getOutputStream(), true);
                                o.println("initiate-checkpoint " + tempState.getCustomerName());
                                o.flush();
                                o.close();
                            }
                            System.out.println("Checkpoiting Initiated\n");
                            Thread.sleep(1200);
                            //Counter of 'yes' recived by the Initiater , if matches the number of clients in chk_cohort then Make it Permanent.
                            if (chk_counter == chkt_cohort.size()) {
                                for (customerInfo i : chkt_cohort) {
                                    socket = new Socket(i.getIPv4(), i.getPortB());
                                    o = new PrintWriter(socket.getOutputStream(), true);
                                    o.println("permanent-checkpoint " + tempState.getCustomerName());
                                    o.flush();
                                    o.close();
                                }

                                System.out.println("Checkpointing Successful !!!\n\n");
                            } //Incase there would have been a fake transaction then checkpointing is not approved. Hence, automatically triggers the Rollback procedure.
                            else {
                                System.out.println("Checkpointing Failed -> Initating Rollback...\n\n");
                                Thread.sleep(1200);
                                iniRollback("prepare_to_rollback");
                                Thread.sleep(500);
                                if (roll_counter == cohortList.size()) {
                                    iniRollback("confirm_rollback");
                                }
                            }
                            break;
                        //User Defined Input to Print the Current Cohort memebers;
                        case "print cohort":
                            for (Map.Entry<String, customerInfo> mapElement : cohortList.entrySet()) {
                                customerInfo value = mapElement.getValue();
                                value.printCustomer();
                            }
                            break;
                        //Command to trigger Rollback algorithm manually, a permanent rollback is triggered on 'yes' received 
                        case "rollback":
                            System.out.println("Rollback Initiated...\n");
                            iniRollback("prepare_to_rollback");
                            Thread.sleep(1200);
                            if (roll_counter == cohortList.size()) {
                                iniRollback("confirm_rollback");
                            }
                            System.out.println("Rollback Completed...\n");
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

    //Function to Complete the initial and Final Rolled Back
    public static void iniRollback(String dValue) throws IOException {
        for (Map.Entry<String, customerInfo> mapElement : cohortList.entrySet()) {
            customerInfo value = mapElement.getValue();
            //Creating a new Socket for Individual Peer from its IP and PORT.
            Socket socket = new Socket(value.getIPv4(), value.getPortB());
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println(dValue + " " + tempState.getCustomerName());
            pw.flush();
        }

    }

    //Creating a Listner for all the Other Peers in the Network
    public static class miniServer implements Runnable {

        private int port;
        private static ServerSocket server = null;

        public miniServer(int port) throws SocketException, IOException {
            this.port = port;
            server = new ServerSocket(this.port);
            server.setReuseAddress(true);
        }

        public void run() {

            try {
                while (true) {
                    //Accepting all the incomming request.
                    Socket client = server.accept();
                    ClientHandler clientSock = new ClientHandler(client);
                    new Thread(clientSock).start();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Class To Listen Incoming Peer Messages
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
                while ( !clientSocket.isClosed() && (input = in.readLine()) != null) {
                    String command = "";
                    String[] line = input.split(" ");
                    command = line[0];
                    switch (command) {
                        case "transfer":
                            //Upon Receiving a Trasnfer the Sender Client has been added to the chkt_cohort list.
                            chkt_cohort.add(cohortList.get(line[3]));
                            int amt = Integer.parseInt(line[1]);
                            String p = line[2];
                            int updateLabel = Integer.parseInt(line[3].substring(line[3].length() - 1, line[3].length()));

                            //Check for a Fake Transaction
                            if (tempState.isWill_rollback() && (Integer.parseInt(line[4]) - tempState.getLast_label_recv()[updateLabel]) > 1 && tempState.isResumeExecution()) {
                                tempState.setShouldRollBack(true);
                                System.out.println("Lost Transfers Detected -> Rollback Initiated...\n");

                                iniRollback("prepare_to_rollback");
                                Thread.sleep(1200);
                                if (roll_counter == cohortList.size()) {
                                    iniRollback("confirm_rollback");
                                }
                                System.out.println("Rollback Completed...\n");

                                break;
                            }

                            //Receive Lable is been updated
                            tempState.getLast_label_recv()[updateLabel] = Integer.parseInt(line[4]);
                            //Update the Balance of the Receiver 
                            tempState.setBalance(tempState.getBalance() + amt);

                            //Printing The added balance.
                            System.out.println("$" + amt + " has been received. The total balance is $" + tempState.getBalance());
                            break;

                        case "lost-transfer":
                            //On receiving the lost-transaction the client is still added to the chkt_cohort, and the setWill_checkpoint is set to false.
                            chkt_cohort.add(cohortList.get(line[3]));
                            tempState.setWill_checkPoint(false);
                            break;

                        case "initiate-checkpoint":
                            //Cheking for the WillCheckpoint Flag

                            if (tempState.isWill_checkPoint()) {
                                String ip = cohortList.get(line[1]).getIPv4();
                                int port = cohortList.get(line[1]).getPortB();
                                Socket socket = new Socket(ip, port);
                                //Creating a socket for the Sender to Send the Confirmation MSG
                                out = new PrintWriter(socket.getOutputStream(), true);
                                //'yes' to confirm checkpointing
                                out.println("yes");
                                out.flush();
                                out.close();
                            }
                            break;
                        case "yes":
                            //On receiving 'yes' the counter has been increased
                            chk_counter++;
                            break;

                        case "yes_to_roll":
                            //On receiving 'yes_to_roll' the counter has been increased to rollback
                            roll_counter++;
                            break;

                        case "permanent-checkpoint":
                            //Chaging the Global State Balance
                            globalState.setBalance(tempState.getBalance());

                            //Changing the First_label_sent and last_label_received
                            globalState.getFirst_label_sent()[0] = tempState.getFirst_label_sent()[0];
                            globalState.getFirst_label_sent()[1] = tempState.getFirst_label_sent()[1];
                            globalState.getFirst_label_sent()[2] = tempState.getFirst_label_sent()[2];

                            globalState.getLast_label_recv()[0] = tempState.getLast_label_recv()[0];
                            globalState.getLast_label_recv()[1] = tempState.getLast_label_recv()[1];
                            globalState.getLast_label_recv()[2] = tempState.getLast_label_recv()[2];

                            //Chaning the Global state in the local cohort List
                            cohortList.put(globalState.getCustomerName(), globalState);
                            //Clearing the chkt_cohort and counter
                            chkt_cohort.clear();
                            chkt_cohort.add(globalState);
                            chk_counter = 0;

                            //Sending a update promt to 
                            PrintWriter pw = new PrintWriter(bankSocket.getOutputStream());
                            pw.println("update " + globalState.getCustomerName() + " " + globalState.getBalance());
                            pw.flush();
                            Thread.sleep(800);

                            break;

                        case "prepare_to_rollback":
                            //Checking for roll_back conditions
                            if (tempState.isWill_rollback() && tempState.isResumeExecution()) {
                                tempState.setResumeExecution(false);
                                Socket socket = new Socket(cohortList.get(line[1]).getIPv4(), cohortList.get(line[1]).getPortB());
                                pw = new PrintWriter(socket.getOutputStream(), true);
                                pw.println("yes_to_roll");
                                pw.flush();
                            }
                            break;
                        case "confirm_rollback":
                            //Swapping the tempState with Global State on rollback
                            tempState = globalState;
                            pw = new PrintWriter(bankSocket.getOutputStream());
                            pw.println("rollback " + globalState.getCustomerName() + " " + globalState.getBalance());
                            pw.flush();
                            roll_counter = 0;
                            break;
                        default:
                            System.out.println("ERROR\n\n");
                            break;
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
                boolean cohortAdd = false;

                while (!(reply = in.readLine()).equals("disconnected")) {

                    //Seperating the Cohort Information Responce from the Server to create a copy of cohort in clien
                    
                    if (!reply.isEmpty() && reply.charAt(0) >= 48 && reply.charAt(0) <= 57) {
                        int nunOfCustomer = Integer.parseInt(reply);
                        reply = in.readLine();
                        System.out.println(reply);

                        reply = in.readLine();
                        System.out.println(reply);

                        reply = in.readLine();
                        System.out.println(reply);

                        for (int i = 1; i <= nunOfCustomer; i++) {

                            reply = in.readLine();
                            String[] customer = reply.split("\t");
                            System.out.println(reply);
                            customerInfo newCustomer = new customerInfo(customer[0], Double.parseDouble(customer[1]), customer[2], Integer.parseInt(customer[3]), Integer.parseInt(customer[4]));
                            cohortList.put(customer[0], newCustomer);
                        }

                        System.out.println("");

                    } else {
                        System.out.println(reply);
                    }
                }
                //Closing the BR object.
                in.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}
