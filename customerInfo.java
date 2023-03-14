
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

public class customerInfo implements Serializable {

        String customerName;
        double balance;
        String IPv4;
        int portA;
        int portB;
        Socket clientSocket;
        ArrayList<customerInfo> cohort = new ArrayList<customerInfo>();
        int[] last_label_recv = new int[4];
        int[] first_label_sent = new int[4];

    public int[] getLast_label_recv() {
        return last_label_recv;
    }

    public void setLast_label_recv(int[] last_label_recv) {
        this.last_label_recv = last_label_recv;
    }

    public int[] getFirst_label_sent() {
        return first_label_sent;
    }

    public void setFirst_label_sent(int[] first_label_sent) {
        this.first_label_sent = first_label_sent;
    }
        
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
        
         public customerInfo(String customerName, double balance, String IPv4, int portA, int portB) {
            
            //Customer information 
            this.customerName = customerName;
            this.balance = balance;
            this.IPv4 = IPv4;
            this.portA = portA;
            this.portB = portB;
            cohort = null;
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
