package vg.civcraft.mc.mercury.venus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.Logger;  // Shaded with Bukkit
import org.apache.logging.log4j.LogManager;  // Shaded with Bukkit

import vg.civcraft.mc.mercury.config.MercuryConfigManager;
import vg.civcraft.mc.mercury.events.EventManager;

public class VenusService extends Thread{

	private final static Logger logger = LogManager.getLogger(VenusService.class);
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	public String servername;
	public boolean connected = false;

	public VenusService(){
		enable();
	}

	public void teardown(){
		if (connected){
			// lock message queue
			this.shutdown();
		}
	}

	// message structure when receiving is: msg,plugin,message
	private void receiveMessage(String message){
		if (message.isEmpty()){return;}
		final String[] splitmsg = message.split(",", 3);
		if (splitmsg[0].equals("msg")){
			EventManager.fireMessage(null, splitmsg[1], splitmsg[2]);
		}
	}

	public void enable(){
		// setup connection & read config from yml
		servername = MercuryConfigManager.getServerName();
		//setup connection
		try {
			socket = new Socket(MercuryConfigManager.getHost(), MercuryConfigManager.getPort());
			input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			if (registerVenus(servername)){
				connected = true;
			}
		} catch (UnknownHostException e) {
			logger.warn("[Mercury] Unable to connect to Venus");
			connected = false;
		} catch (IOException e) {
			logger.warn("[Mercury] Unable to connect to Venus");
			connected = false;
		}
	}

	//need to implement timeout for waiting for the welcome/reject message
	private boolean registerVenus(String servername){
		output.println("register,"+ servername);
		output.flush();
		String response;

		try {
			while ((response = input.readLine()) != null){
				if (response.equals("welcome")){
					logger.info("[Mercury] Connected to Venus server!");
					return true;
				} else{
					logger.warn("[Mercury] Error registering with Venus: "+response);
					return false;
				}
			}
		} catch (IOException e) {
			logger.warn("[Mercury] Error registering with Venus: IO Error receiving packet");
			return false;
		}
		return false;
	}

	private synchronized void shutdown() {
		try {
			input.close();
			output.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connected = false;
	}

	@Override
	public void run() {
		if (connected == false){return;}
		String recv;
		while (connected && !socket.isClosed() && socket.isConnected()){
			try {
				if ((recv = input.readLine()) != null){
					receiveMessage(recv);
				}
			} catch (IOException e) {
				logger.warn("[Mercury] Connection to Venus Server lost; reload plugin to reconnect.");
				shutdown();
				break;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
	}

	public synchronized void sendMessage(String destination, String message, String plugin) {
		// queue up message to be sent.
		// message structure when sending is: msg,server,plugin,message
		if (connected == false){return;}
		output.println("msg,"+destination+","+plugin+","+message);
		output.flush();
	}

}
