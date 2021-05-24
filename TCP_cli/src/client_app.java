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
<<<<<<< HEAD
				System.out.println("서버에서 받아온 인코딩된 메세지 : "+msg);
				msg = custom_base64_decoder(msg);
				System.out.println("서버에서 받아온 디코딩된 메세지 : "+msg);
=======
				System.out.println(msg);
>>>>>>> 6721abc214354caa40feabf8b83d500433919cfc
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
<<<<<<< HEAD
		System.out.println("클라이언트에서 인코딩된 메세지 : "+custom_base64_encoder(msg));
		return custom_base64_encoder(msg);
	}

	String custom_base64_encoder(String text) {
		int count = 0;
		String utf_8_msg = "";
		String base64_msg = "";
		String[] temp = new String[966];
		String key = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

		String words = text;

		for (byte b : words.getBytes(StandardCharsets.UTF_8)) {
			utf_8_msg += bytesTobinary((byte) b);
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

	static String bytesTobinary(Byte b) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			if (((0x80 >>> i) & b) == 0)
				builder.append("0");
			else
				builder.append("1");
		}
		return builder.toString();
	}

	String custom_base64_decoder(String text) throws Exception {
		String key = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
		String[] array = text.split("");
		int temp;
		int i = 0;
		String decoding_msg = "";
		for (int count = 0; count < array.length; count++) {
			temp = key.indexOf(array[count]);
			if (temp == 64) {
				temp = 0;
				i++;
			}
			decoding_msg += String.format("%6s", Integer.toBinaryString(temp)).replaceAll(" ", "0");
		}

		switch (i) {
		case 0:
			break;
		case 1:
			decoding_msg = decoding_msg.substring(0, decoding_msg.length() - 8);
			break;
		case 2:
			decoding_msg = decoding_msg.substring(0, decoding_msg.length() - 16);
			break;
		default:
			System.out.print("error");
		}
		byte[] b = binaryTobyteArray(decoding_msg);
		return new String(b, "UTF-8");

	}

	static byte[] binaryTobyteArray(String str) {
		int count = str.length() / 8;
		byte[] b = new byte[count];
		for (int i = 0; i < count; i++) {
			String temp = str.substring(i * 8, (i + 1) * 8);
			b[i] = binaryTobyte(temp);
		}
		return b;
	}

	static byte binaryTobyte(String str) {
		byte temp = 0, total = 0;
		for (int i = 0; i < 8; i++) {
			if (str.charAt(7 - i) == '1')
				temp = (byte) (1 << i);
			else
				temp = 0;
			total = (byte) (temp | total);
		}
		return total;
=======
		return msg;
>>>>>>> 6721abc214354caa40feabf8b83d500433919cfc
	}

}
