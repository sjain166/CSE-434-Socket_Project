
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

            server = new ServerSocket(14000);
            server.setReuseAddress(true);

            while (true) {


                Socket client = server.accept();


                System.out.println("New client connected" + client.getInetAddress().getHostAddress());


                ClientHandler clientSock = new ClientHandler(client);


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
                
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String input;
                while ((input = in.readLine()) != null) {
                    String command = "";
                    String[] line = input.split(" ");
                    command = line[0];

                    switch (command) {
                        case "open":
                            if (line[1].matches("[a-zA-Z]+") && line[1].length() <= 15) {
                                double balance = Double.parseDouble(line[2]);
                                int portA = Integer.parseInt(line[4]);
                                int portB = Integer.parseInt(line[5]);
                                if (portA >= 14000 && portA <= 14499 && portB >= 14000 && portB <= 14499) {
                                    customerInfo customer = new customerInfo(line[1], balance, line[3], portA, portB , clientSocket);
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

                        case "new-cohort":
                            String cName = line[1];
                            int n = Integer.parseInt(line[2]);
                            String value = newCohort(cName, n);                       
                            out.println(value);
                            out.flush();
                            break;

                        case "delete-cohort":
                            cName = line[1];
                            if(deleteCohort(cName)){
                                out.println("SUCCESS\nThe cohort has been deleted");
                                out.flush();
                            }
                            else{
                               out.println("FAILURE");
                               out.flush(); 
                            }
                            break;

                        case "exit":
                            cName = line[1];
                            out.print(exit(cName));
                            break;

                        case "print":
                            print();
                            break;
                        default:
                            out.println("FAILURE");
                            out.flush();
                            System.out.println("Incoreect Input");
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

    public static class customerInfo {

        String customerName;
        double balance;
        String IPv4;
        int portA;
        int portB;
        Socket clientSocket;
        List<customerInfo> cohort = new ArrayList<>();

        public customerInfo(String customerName, double balance, String IPv4, int portA, int portB, Socket clientSocket) {
            this.customerName = customerName;
            this.balance = balance;
            this.IPv4 = IPv4;
            this.portA = portA;
            this.portB = portB;
            cohort = null;
            this.clientSocket = clientSocket;
        }
           
        public Socket getClientSocket() {
            return clientSocket;
        }

        public void setClientSocket(Socket clientSocket) {
            this.clientSocket = clientSocket;
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
            System.out.println("PortB : " + this.portB+"\n");
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
        for (Map.Entry<String, customerInfo> mapElement : map.entrySet()) {
            String key = mapElement.getKey();
            customerInfo value = (mapElement.getValue());
            value.printCustomer();
        }
    }

    public static String open(customerInfo customer) {

        if (!map.isEmpty() && map.containsKey(customer.getName())) {
            return "FAILURE";
        } else {
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
            String ret = "SUCCESS\n";
            ret += String.format("%s", "Customer");
            ret += String.format("%30s", "Balance");
            ret += String.format("%30s", "IPv4 Address");
            ret += String.format("%30s", "Port(s)\n");
            
            for (int i = 0; i < generatedCohort.size(); i++) {
                
                generatedCohort.get(i).setCohort(generatedCohort);
                ret += String.format("%s",generatedCohort.get(i).getCustomerName());
                ret += String.format("%30.3f",generatedCohort.get(i).getBalance());
                ret += String.format("%30s",generatedCohort.get(i).getIPv4());
                ret += String.format("%30d",generatedCohort.get(i).getPortA());
                ret += " " + generatedCohort.get(i).getPortB()+"\n";
            }
            return ret;
        }

        return "FAILURE";

    }
    
    
    public static boolean deleteCohort(String customerName) throws IOException{
        
        if(map.isEmpty() || !(map.containsKey(customerName))){
            return false;
        }
        customerInfo currCust = map.get(customerName);
        
        if(currCust.getCohort() == null){
           return false; 
        }
        
        List<customerInfo> cohort = currCust.getCohort();
        PrintWriter out = null;
        for(customerInfo i : cohort){
            if(i.getCustomerName() != customerName){
                out = new PrintWriter(i.getClientSocket().getOutputStream(), true);
                out.println("You have been removed from the cohort.");
                out.flush();
            }
            i.setCohort(null);
        }
        
        return true;
    }
    
    
    public static String exit(String customerName){
        if(map.isEmpty() || !map.containsKey(customerName) || map.get(customerName).getCohort() != null){
            return "FAILURE";
        }
        else{
            map.remove(customerName);
            return "SUCCESS";
        }
    }

}
