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
		return msg;
	}
}