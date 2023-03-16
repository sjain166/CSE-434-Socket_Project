
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class bank {

    static HashMap<String, customerInfo> map = new HashMap<>(); //creating hashmap to store customer objects with their name as the key value

    public static void main(String args[]) throws IOException {
        ServerSocket server = null;

        try {
            //creating a new server socket at port 14000
            server = new ServerSocket(14000);
            server.setReuseAddress(true);

            while (true) {

                // The code below checks for incoming customer connections and starts a new thread to handle each client
                Socket client = server.accept();

                System.out.println("New client got connected!!!");

                ClientHandler clientSock = new ClientHandler(client);

                new Thread(clientSock).start();

            }
            //The code below will catch any exception thrown while starting the server and closes the server socket if it is not null
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // This class implements the Runnable interface to handle client requests
    private static class ClientHandler implements Runnable {

        private final Socket clientSocket;
        private customerInfo customer;

        //Constructors to set the clientSocket attributes
        public ClientHandler() {
            this.clientSocket = null;
        }

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            try {

                // The code below will handle the customer requests coming from the customer input stream and parsing that input
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String input;
                while (!clientSocket.isClosed() && (input = in.readLine()) != null) {

                    //The command from the customer side is split to check the first word and decide what to implement
                    String command = "";
                    String[] line = input.split(" ");
                    command = line[0];

                    switch (command) {
                        //If the open command is received the ClientHandler class will check if the command is valid and create a customer object if it and if not a Failure message is returned
                        case "open":
                            if (line[1].length() <= 15) {
                                double balance = Double.parseDouble(line[2]);
                                int portA = Integer.parseInt(line[4]);
                                int portB = Integer.parseInt(line[5]);
                                if (portA >= 14000 && portA <= 14499 && portB >= 14000 && portB <= 14499) {
                                    customerInfo customer = new customerInfo(line[1], balance, line[3], portA, portB, clientSocket);
                                    out.println(open(customer));
                                    out.flush();
                                } else {
                                    out.println("FAILURE");
                                    out.flush();
                                }

                            } else {
                                out.println("FAILURE");
                                out.flush();
                            }

                            break;
                        // if the new-cohort command is recieved, function new Cohort is called to create a cohort
                        case "new-cohort":
                            String cName = line[1];
                            int n = Integer.parseInt(line[2]);
                            String value = newCohort(cName, n);
                            sendMembersInfo(cName, value);
                            break;
                        //If the delete-cohort command i received, function deleteCohort is called
                        case "delete-cohort":
                            cName = line[1];
//                            Socket socket = new Socket(map.get(cName).getIPv4() , map.get(cName).getPortB());
//                            PrintWriter pw = new PrintWriter(socket.getOutputStream() , true);
                            
                            if (deleteCohort(cName)) {
                                out.println("SUCCESS : The cohort has been deleted");
                                out.flush();
                            } else {
                                out.println("FAILURE");
                                out.flush();
                            }
                            
                            break;
                        //If the exit command is received we call the exit method 
                        case "exit":
                            cName = line[1];
                            if (exit(cName).equals("SUCCESS")) {
                                out.println("SUCCESS");
                                out.flush();
                                out.println("disconnected");
                                out.flush();

                            } else {
                                out.print("FAILURE");
                                out.flush();
                            }
                            break;

                        case "print":
                            print();
                            break;
                        case "update":
                            map.get(line[1]).setBalance(Double.parseDouble(line[2]));
                            System.out.println("The customer " + map.get(line[1]).getCustomerName() + " has been updated to " + map.get(line[1]).getBalance() + "\n");
                            Thread.sleep(800);
                            break;
                        case "rollback":
                            map.get(line[1]).setBalance(Double.parseDouble(line[2]));
                            System.out.println("The customer " + map.get(line[1]).getCustomerName() + " has been rolled back to " + map.get(line[1]).getBalance()+"\n");
                            Thread.sleep(800);
                            break;
                        default:
                            System.out.println(input);
                            out.println("FAILURE");
                            out.flush();
                            System.out.println("Incorrect Input");
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static boolean sendMembersInfo(String customerName, String cohortList) throws IOException {

        if (map.isEmpty() || !(map.containsKey(customerName))) {
            return false;
        }
        customerInfo currCust = map.get(customerName);

        if (currCust.getCohort() == null) {
            return false;
        }

        List<customerInfo> cohort = currCust.getCohort();

        PrintWriter out = null;
        for (customerInfo i : cohort) {

            out = new PrintWriter(i.getClientSocket().getOutputStream(), true);

            out.println(cohort.size());
            out.flush();

            out.println("You have been added to the cohort");
            out.flush();

            out.println(cohortList);
            out.flush();
        }

        return true;
    }

    //This method is to print all the customers in the bank and is used for testing purposes
    public static void print() {
        for (Map.Entry<String, customerInfo> mapElement : map.entrySet()) {
            String key = mapElement.getKey();
            customerInfo value = (mapElement.getValue());
            value.printCustomer();
        }
    }

    //Function to open a new customer account
    public static String open(customerInfo customer) {

        if (!map.isEmpty() && map.containsKey(customer.getName())) {
            return "FAILURE";
        } else {
            map.put(customer.getName(), customer);
            return "SUCCESS";
        }
    }

    //Function to create a new Cohort, n being the number of people that should be in the cohort
    public static String newCohort(String customerName, int n) {

        if (n > map.size() || !map.containsKey(customerName) || n < 2) {
            return "FAILURE";
        }

        //Checks if the customer trying to create a cohort is not in another cohort at the bank and if he is Failure message is returned 
        customerInfo currCust = map.get(customerName);
        if (currCust.getCohort() != null) {
            return "FAILURE";
        }
        int count = 1;
        ArrayList<customerInfo> generatedCohort = new ArrayList<>();
        generatedCohort.add(currCust);

        //Adds random customer to the cohort whose cohort value is null
        for (Map.Entry<String, customerInfo> mapElement : map.entrySet()) {
            if (count == n) {
                break;
            }

            customerInfo value = (mapElement.getValue());
            if (value.getCohort() == null && !value.customerName.equals(customerName)) {
                count++;
                generatedCohort.add(value);
            }

        }

        //formatted output to show the memebers added to the cohort to the customer who invoked the command
        if (count == n) {
            String ret = "SUCCESS\n";
            ret += "Customer";
            ret += "\t" + "Balance";
            ret += "\t" + "IPv4 Address";
            ret += "\t" + "Port(s)" + "\n";

            for (int i = 0; i < generatedCohort.size(); i++) {

                generatedCohort.get(i).setCohort(generatedCohort);
                if (i == generatedCohort.size() - 1) {
                    ret += generatedCohort.get(i).getCustomerName();
                    ret += "\t" + generatedCohort.get(i).getBalance();
                    ret += "\t" + generatedCohort.get(i).getIPv4();
                    ret += "\t" + generatedCohort.get(i).getPortA();
                    ret += "\t" + generatedCohort.get(i).getPortB();
                } else {
                    ret += generatedCohort.get(i).getCustomerName();
                    ret += "\t" + generatedCohort.get(i).getBalance();
                    ret += "\t" + generatedCohort.get(i).getIPv4();
                    ret += "\t" + generatedCohort.get(i).getPortA();
                    ret += "\t" + generatedCohort.get(i).getPortB() + "\n";
                }

            }
            return ret;
        }

        return "FAILURE";

    }

    //Function to delete a cohort from the bank and
    public static boolean deleteCohort(String customerName) throws IOException {

        if (map.isEmpty() || !(map.containsKey(customerName))) {
            return false;
        }
        customerInfo currCust = map.get(customerName);

        if (currCust.getCohort() == null) {
            return false;
        }

        List<customerInfo> cohort = currCust.getCohort();
        PrintWriter out = null;
        for (customerInfo i : cohort) {
            if (i.getCustomerName() != customerName) {
                out = new PrintWriter(i.getClientSocket().getOutputStream(), true);
                out.println("You have been removed from the cohort.\n");
                out.flush();
            }
            i.setCohort(null);
        }

        return true;
    }

    //Functions to exit from the bank if and only if the customer is not present in a cohort
    public static String exit(String customerName) {
        if (map.isEmpty() || !map.containsKey(customerName) || map.get(customerName).getCohort() != null) {
            return "FAILURE";
        } else {
            System.out.println(customerName + " got disconnected!!!");
            map.remove(customerName);
            return "SUCCESS";
        }
    }

}
