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
	byte[] base64_msg;

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

			System.out.println("Command(Hi, CurrentTime, ConnectionTime, ClientList, Quit)※대소문자를 구분합니다.");
			while (run) {
				System.out.print("Command:");
				String command = sn.nextLine();
				if (command.equals("Hi")) {
					this.cid = client_app.cid;
					System.out.println(custom_base64(Request("Hi")));
					dos.writeUTF(Request("Hi"));
					System.out.println(dis.readUTF());
				}

				else if (command.equals("CurrentTime")) {
					dos.writeUTF(Request("CurrentTime"));
					System.out.println(dis.readUTF());
				}

				else if (command.equals("ConnectionTime")) {
					dos.writeUTF(Request("ConnectionTime"));
					System.out.println(dis.readUTF());
				}

				else if (command.equals("ClientList")) {
					dos.writeUTF(Request("ClientList"));
					System.out.println(dis.readUTF());
				}

				else if (command.equals("Quit")) {
					dos.writeUTF(Request("Quit"));
					System.out.println(dis.readUTF());
					run = false;
				}

				else {
					dos.writeUTF(Request(command));
					System.out.println(dis.readUTF());
				}
				System.out.println("");
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

	String custom_base64(String text) {
		int count = 0;
		String utf_8_msg = "";
		String base64_msg = "";
		String[] temp = new String[100];
		String key = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

		String words = text;

		for (byte b : words.getBytes(StandardCharsets.UTF_8)) {
			utf_8_msg += bytesToBinaryString((byte) b);
		}

		int num = utf_8_msg.length() / 8;

		while (num > 3) {
			num %= 3;
		}

		switch (num) {
		case 0:
			break;
		case 1:
			utf_8_msg += "0000000000000000";
			break;
		case 2:
			utf_8_msg += "00000000";
			break;
		default:
			System.out.print("error");
		}

		for (count = 0; count < utf_8_msg.length(); count++)
			temp[count / 6] = utf_8_msg.substring((count / 6) * 6, count + 1);

		count = 0;
		while (true) {
			if (temp[count] != null) {
				base64_msg += key.charAt(Integer.parseInt(temp[count], 2));
				count++;
			} else {
				switch (num) {
				case 0:
					break;
				case 1:
					base64_msg = base64_msg.substring(0, count - 2);
					base64_msg += "==";
					break;
				case 2:
					base64_msg = base64_msg.substring(0, count - 1);
					base64_msg += "=";
					break;
				default:
					System.out.print("error");
				}
				break;
			}
		}
		return base64_msg;
	}

	static String bytesToBinaryString(Byte b) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			builder.append(((0x80 >>> i) & b) == 0 ? '0' : '1');
		}
		return builder.toString();
	}
}
