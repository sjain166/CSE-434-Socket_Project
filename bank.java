
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class bank {

    static HashMap<String, customerInfo> map = new HashMap<>();

    public static void main(String args[]) throws IOException {
        ServerSocket server = null;

        try {

            // server is listening on port 1234
            server = new ServerSocket(1234);
            server.setReuseAddress(true);
            // running infinite loop for getting
            // client request
            while (true) {

                // socket object to receive incoming client
                // requests
                Socket client = server.accept();
//
//                // Displaying that new client is connected
//                // to server
                System.out.println("New client connected" + client.getInetAddress().getHostAddress());
//
//                // create a new thread object
                ClientHandler clientSock = new ClientHandler(client);

//
//                // This thread will handle the client
//                // separately
                new Thread(clientSock).start();

            }
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

    private static class ClientHandler implements Runnable {

        private final Socket clientSocket;

        // Constructor
        public ClientHandler() {
            this.clientSocket = null;
        }

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            System.out.println("NULL");
            PrintWriter out = null;
            BufferedReader in = null;
            try {

////                // get the outputstream of client
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String input;
                while ((input = in.readLine()) != null) {
                    String command = "";
                    String[] line = input.split(" ");
                    //line = in.readLine().split(" ");
                    command = line[0];
                    System.out.println(command);

                    switch (command) {
                        case "open":
                            if (line[1].matches("[a-zA-Z]+") && line[1].length() <= 15) {
                                double balance = Double.parseDouble(line[2]);
                                int portA = Integer.parseInt(line[4]);
                                int portB = Integer.parseInt(line[5]);
                                if (portA >= 14000 && portA <= 14499 && portB >= 14000 && portB <= 14499) {
                                    customerInfo customer = new customerInfo(line[1], balance, line[3], portA, portB);
                                    out.println(open(customer));
                                    out.flush();
                                } else {
                                    out.println("Failure");
                                    out.flush();
                                }

                            } else {
                                out.println("Failure");
                                out.flush();
                            }

                            break;

                        case "new-cohort":
                            String cName = line[1];
                            int n = Integer.parseInt(line[2]);
                            String value = newCohort(cName, n);
//                            for(int i = 0 ; i <= n+2 ; i++){
//                                
//                            }
                            
                            out.println(value);
                            out.flush();
                            //map.get(cName).printCohort();
                            break;

                        case "delete-cohort":

                            break;

                        case "exit":

                            break;

                        case "print":
                            print();
                            out.println("Printing");
                            out.flush();
                            break;
                        default:
                            out.println("FAILURE");
                            out.flush();
                            System.out.println("Input a correct ");
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("I am coming here");
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

    public static class customerInfo {

        String customerName;
        double balance;
        String IPv4;
        int portA;
        int portB;
        List<customerInfo> cohort = new ArrayList<>();

        public customerInfo(String customerName, double balance, String IPv4, int portA, int portB) {
            this.customerName = customerName;
            this.balance = balance;
            this.IPv4 = IPv4;
            this.portA = portA;
            this.portB = portB;
            cohort = null;
        }

        public List<customerInfo> getCohort() {
            return cohort;
        }

        public void setCohort(List<customerInfo> cohort) {
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

        public void printCustomer() {
            System.out.println("Customer Name : " + this.customerName);
            System.out.println("Customer Balance : " + this.balance);
            System.out.println("Cutomer IPv4 : " + this.IPv4);
            System.out.println("PortA : " + this.portA);
            System.out.println("PortB : " + this.portB);
        }
        
        public void printCohort(){
            if(this.cohort.isEmpty()){
                System.out.println("Do not have a cohort");
                return;
            }
            for(customerInfo i : cohort){
                i.printCustomer();
            }
        }

    }

    public static void print() {

        System.out.println("I am in print");
        System.out.println(map.size());
        for (Map.Entry<String, customerInfo> mapElement : map.entrySet()) {
            String key = mapElement.getKey();
            customerInfo value = (mapElement.getValue());
            value.printCustomer();
        }
    }

    public static String open(customerInfo customer) {

        if (!map.isEmpty() && map.containsKey(customer.getName())) {
            return "ERROR";
        } else {
            System.out.println("Success");
            map.put(customer.getName(), customer);
            return "SUCCESS";
        }
    }

    public static String newCohort(String customerName, int n) {

        if (n > map.size() || !map.containsKey(customerName) || n < 2) {
            return "FAILURE";
        }

        customerInfo currCust = map.get(customerName);
        if(currCust.getCohort() != null){
            return "FAILURE";
        }
        int count = 1;
        List<customerInfo> generatedCohort = new ArrayList<>();
        generatedCohort.add(currCust);

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

        if (count == n) {
            String ret = "SUCCESS#";
            ret += "Customer\t\tBalance\t\tIPv4 Address\t\tPort(s)#";
            for (int i = 0; i < generatedCohort.size(); i++) {
                
                generatedCohort.get(i).setCohort(generatedCohort);
                ret+= generatedCohort.get(i).getCustomerName()+"\t\t"+generatedCohort.get(i).getBalance()+"\t\t"+generatedCohort.get(i).getIPv4()+"\t\t"+generatedCohort.get(i).getPortA()+" " + generatedCohort.get(i).getPortB()+"#";
            }
            System.out.println(ret);
            return ret;
        }

        return "FAILURE";

    }

}
