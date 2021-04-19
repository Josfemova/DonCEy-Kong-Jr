package cr.ac.tec.ce3104.tc3;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class CmdInterpreter extends Thread {

    public CmdInterpreter() {

    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Listening for commands");
        while (true) {
            try {
                String input = reader.readLine();
                /*JSONObject x = (JSONObject) JSONValue.parse(reader.readLine());
                System.out.println(x);
                System.out.println(x.get("init"));
                // String cmd[] = reader.readLine().split(" ");
                // Command interpretation
                // TODO*/
            } catch (IOException e) {
                System.out.println("Failed to read command: " + e.getMessage());
            }
        }

    }
}
