
import java.io.IOException;
import java.io.*;
import java.util.Scanner;
import java.net.*;

public class customer {

    public static void main(String args[]) throws IOException {
        try ( Socket socket = new Socket("localhost", 1234)) {

            // writing to server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // reading from server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // object of scanner class
            Scanner sc = new Scanner(System.in);
            String line = null;

            while (!"exit".equalsIgnoreCase(line)) {

                // reading from user
                line = sc.nextLine();

                // sending the user input to server
                out.println(line);
                out.flush();

                // displaying server reply
                System.out.println("Server replied "+ in.readLine());
            }

            // closing the scanner object
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //Hi how are you doing today? Sidharth sucks cock for timepass

    }
}
