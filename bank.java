
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
                            if (line[1].matches("[a-zA-Z]+") && line[1].length() <= 15) {
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
                            sendMembersInfo(cName);
                            out.println(value);
                            out.flush();
                            break;
                        //If the delete-cohort command i received, function deleteCohort is called
                        case "delete-cohort":
                            cName = line[1];
                            if (deleteCohort(cName)) {
                                out.println("SUCCESS\nThe cohort has been deleted");
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
                        default:
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
    
    public static boolean sendMembersInfo(String customerName) throws IOException{
        
        if (map.isEmpty() || !(map.containsKey(customerName))) {
            return false;
        }
        customerInfo currCust = map.get(customerName);

        if (currCust.getCohort() == null) {
            return false;
        }

        List<customerInfo> cohort = currCust.getCohort();
        
        PrintWriter out = null;
        ObjectOutputStream objectOutput = null;
        for (customerInfo i : cohort) {
  
                out = new PrintWriter(i.getClientSocket().getOutputStream(), true);
                out.println("You have been added to the cohort");
                out.flush();
                
                objectOutput = new ObjectOutputStream(i.getClientSocket().getOutputStream());
                objectOutput.writeObject(cohort);
                objectOutput.close();
    
        }

        return true;
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

        public ArrayList<customerInfo> getCohort() {
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
            ret += String.format("%s", "Customer");
            ret += String.format("%30s", "Balance");
            ret += String.format("%30s", "IPv4 Address");
            ret += String.format("%30s", "Port(s)\n");

            for (int i = 0; i < generatedCohort.size(); i++) {

                generatedCohort.get(i).setCohort(generatedCohort);
                ret += String.format("%s", generatedCohort.get(i).getCustomerName());
                ret += String.format("%30.3f", generatedCohort.get(i).getBalance());
                ret += String.format("%30s", generatedCohort.get(i).getIPv4());
                ret += String.format("%30d", generatedCohort.get(i).getPortA());
                ret += " " + generatedCohort.get(i).getPortB() + "\n";
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
                out.println("You have been removed from the cohort.");
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
