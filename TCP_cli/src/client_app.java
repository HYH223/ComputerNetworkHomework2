import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.io.*;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public class client_app {
	Socket mySocket = null;
	static int Num_req = 1;
	static String cid;

	public static void main(String[] args) {
		client_app client = new client_app();
		System.out.println("사용자의 CID(NickName)를 설정해주세요. ※ 10자 이하");
		Scanner scanner = new Scanner(System.in);
		cid = scanner.nextLine();

		try {
			client.mySocket = new Socket("localhost", 55555);
			System.out.println("Client > 서버로 연결되었습니다.");

			Client c = new Client(client.mySocket);
			c.start();

		} catch (Exception e) {
			System.out.println("Connection Fail");
		}
	}
}

class Client extends Thread {
	Socket socket;
	String cid = "noname";

	Client(Socket _socket) {
		this.socket = _socket;
	}

	public void run() {
		try {
			boolean run = true;
			InputStream is = socket.getInputStream();
			DataInputStream dis = new DataInputStream(is);

			OutputStream os = socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			Scanner sn = new Scanner(System.in);
			String msg = "";

			System.out.println("Command(Hi, CurrentTime, ConnectionTime, ClientList, Quit)※대소문자를 구분합니다.");
			while (run) {
				System.out.print("Command:");
				String command = sn.nextLine();
				if (command.equals("Hi")) {
					this.cid = client_app.cid;
					dos.writeUTF(Request("Hi"));
				}

				else if (command.equals("CurrentTime"))
					dos.writeUTF(Request("CurrentTime"));

				else if (command.equals("ConnectionTime"))
					dos.writeUTF(Request("ConnectionTime"));

				else if (command.equals("ClientList"))
					dos.writeUTF(Request("ClientList"));

				else if (command.equals("Quit")) {
					dos.writeUTF(Request("Quit"));
					run = false;
				}

				else
					dos.writeUTF(Request(command));

				msg = dis.readUTF();
				System.out.println(msg);
				String[] array = msg.split("///");
				System.out.println(array[2]);
			}
			System.out.println("이용해주셔서 감사합니다.");

		} catch (Exception e) {
			System.out.println("Exception");
		}
	}

	String Request(String request) {
		String msg = "Type:type1///Request:" + request + "///cid:" + this.cid + "///Num_Req:" + client_app.Num_req
				+ "///END_MSG";
		client_app.Num_req++;
		return msg;
	}

}
