
import java.io.IOException;
import java.net.Socket;
import java.io.*;
import java.util.Scanner;

public class customer{
    public static void main(String args[]) throws IOException{
            Socket newSocket = new Socket("localhost" , 4999);
            
            Scanner sc = new Scanner(System.in);
            System.out.println("Register a New Connection :");
            String msg = sc.nextLine();
            
            PrintWriter pw = new PrintWriter(newSocket.getOutputStream());
            pw.println(msg);
            pw.flush();
            
    }
}
