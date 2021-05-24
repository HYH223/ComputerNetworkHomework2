import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class server_app {

	ServerSocket ss = null;
	static ArrayList<Client> clients = new ArrayList<Client>();

	public static void main(String[] args) {

		server_app server = new server_app();

		try {
			server.ss = new ServerSocket(55555);
			System.out.println("Server > Server Socket is Created...");
			while (true) {
				Socket socket = server.ss.accept();
				Client c = new Client(socket, server.clients.size());
				server.clients.add(c);
				c.start();
			}

		} catch (SocketException e) {
			System.out.println("Server > 소켓 관련 예외 발생, 서버종료");
		} catch (IOException e) {
			System.out.println("Server > 입출력 예외 발생");
		}
	}
}

class Client extends Thread {
	Socket socket;
	String cid = "noname";
	int index;
	SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	long start_time = System.currentTimeMillis();

	Client(Socket _socket, int _index) {
		this.socket = _socket;
		this.index = _index;
	}

	public void run() {
		try {
			boolean run = true;
			InputStream is = socket.getInputStream();
			DataInputStream dis = new DataInputStream(is);

			OutputStream os = socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			while (run) {
				String msg = dis.readUTF();
				msg=custom_base64_decoder(msg);
				System.out.println(msg);
				String[] array = msg.split("///");
				String[] type = array[0].split(":");
				String[] request = array[1].split(":");
				String[] cid = array[2].split(":");
				String[] num_reg = array[3].split(":");

				if (request[1].equals("Hi")) {
					try {
						this.cid = cid[1];
						dos.writeUTF(Response(100, "Success"));
					} catch (Exception e) {
						dos.writeUTF(Response(300, "fail"));
					}

				}

				else if (request[1].equals("CurrentTime")) {
					try {
						String current_time = date_format.format(System.currentTimeMillis());
						dos.writeUTF(Response(130, current_time));
					} catch (Exception e) {
						dos.writeUTF(Response(300, "fail"));
					}

				}

				else if (request[1].equals("ConnectionTime")) {
					try {
						long connection_time = (System.currentTimeMillis() - this.start_time) / 1000;
						dos.writeUTF(Response(150, Long.toString(connection_time) + "sec"));
					} catch (Exception e) {
						dos.writeUTF(Response(300, "fail"));
					}
				}

				else if (request[1].equals("ClientList")) {
					try {
						int last_num = server_app.clients.size();
						String clientlist = "";
						for (int i = 0; i < last_num; i++) {
							clientlist = clientlist + server_app.clients.get(i).socket.getLocalAddress()
									+ server_app.clients.get(i).cid;
						}
						dos.writeUTF(Response(200, clientlist));
					} catch (Exception e) {
						dos.writeUTF(Response(300, "fail"));
					}

				}

				else if (request[1].equals("Quit")) {
					try {
						dos.writeUTF(Response(250, "Success"));
						server_app.clients.remove(this.index);
						run = false;
					} catch (Exception e) {
						dos.writeUTF(Response(300, "fail"));
					}
				}

				else {
					dos.writeUTF(Response(300, "fail"));
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	String Response(int code, String value) {
		String msg = "Type:type2///Statecode:" + code + "///" + value + "///END_MSG";
		return custom_base64_encoder(msg);
	}

	String custom_base64_encoder(String text) {
		int count = 0;
		String utf_8_msg = "";
		String base64_msg = "";
		String[] temp = new String[100];
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
	}

}