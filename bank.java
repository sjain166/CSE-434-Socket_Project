import java.net.*;
import java.io.*;
public class bank{
	public static void main(String args[]) throws IOException{
            ServerSocket ss = new ServerSocket(4999);
            Socket s = ss .accept();
            
            System.out.println("Client connected");
            
            InputStreamReader in = new InputStreamReader(s.getInputStream());
            BufferedReader bf = new BufferedReader(in);
            
            String str = bf.readLine();
            System.out.println(str);
	}
}