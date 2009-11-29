import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class Prog{
	public static void main(String[] args){
		Registry reg = new Registry();
		reg.add(new Server("192.168.1.2",101,"test","aa",10));
	}
}