
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class bank {
    static HashMap<String, customerInfo> map = new HashMap<>();
    String init12;
    
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

                // Displaying that new client is connected
                // to server
                System.out.println("New client connected"+ client.getInetAddress().getHostAddress());

                // create a new thread object
                ClientHandler clientSock = new ClientHandler(client);

                // This thread will handle the client
                // separately
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
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            try {

                // get the outputstream of client
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // get the inputstream of client
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String[] line;
                line = in.readLine().split(" ");
                String command  = line[0];
                
                switch(command)
                {
                    case "open":
                      if(line[1].matches("[a-zA-Z]+") && line[1].length()<=15)
                      {
                          double balance = Double.parseDouble(line[2]);
                          int portA = Integer.parseInt(line[4]);
                          int portB = Integer.parseInt(line[5]);
                          
                          customerInfo customer = new customerInfo(line[1], balance, line[3], portA, portB );
                          open(customer);
                          break;
                          
                      }
                      
                        
                       break;
                       
                    case "new-cohort":
                        
                        break;
                        
                    case "delete-cohort":
                        
                        break;
                        
                    case "exit":
                        
                        break;
                        
                    default:
                        
                        System.out.println("Input a correct ");
                }
                
                
                

            } catch (IOException e) {
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
    
    
    public static class customerInfo{
        
        String customerName;
        double balance;
        String IPv4;
        int portA;
        int portB;
        List<customerInfo> cohort = new ArrayList<>();
        
        public customerInfo(String customerName, double balance, String IPv4, int portA , int portB){
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
        
        public String getName(){
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
        
        public void printCustomer(){
            System.out.println("Customer Name : " + this.customerName);
            System.out.println("Customer Balance : " + this.balance);
            System.out.println("Cutomer IPv4 : " + this.IPv4);
            System.out.println("PortA : " + this.portA);
            System.out.println("PortB : " + this.portB);
        }
        

    }
    
    public static String open(customerInfo customer){
        
        
        if(!map.isEmpty() && map.containsKey(customer.getName())){
            return "ERROR";
        }
        else{
            map.put(customer.getName(), customer);
            for (Map.Entry<String,customerInfo> mapElement : map.entrySet()) {
                String key = mapElement.getKey();
                customerInfo value = (mapElement.getValue());
                value.printCustomer();
            }
            return "SUCCESS";
        }
    }

}
