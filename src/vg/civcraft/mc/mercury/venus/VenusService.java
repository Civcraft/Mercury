package vg.civcraft.mc.mercury.venus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.bukkit.Bukkit;

import vg.civcraft.mc.mercury.config.MercuryConfigManager;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;

public class VenusService implements Runnable{
	
	private Socket socket;
	private DataInputStream input;
	private PrintWriter output;
	public String servername;
	public boolean connected = false;
	
	public VenusService(){
		enable();
	}
	
	public void destroy(){
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
			AsyncPluginBroadcastMessageEvent event = new AsyncPluginBroadcastMessageEvent(splitmsg[1], splitmsg[2]);
			Bukkit.getPluginManager().callEvent(event);
		}		
	}
	
	public void enable(){
		// setup connection & read config from yml
		servername = MercuryConfigManager.getServerName();
		//setup connection
		try {
			socket = new Socket(MercuryConfigManager.getHost(), MercuryConfigManager.getPort());
			input = new DataInputStream(socket.getInputStream());
			output = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
			if (registerVenus(servername)){
				connected = true;
			}
		} catch (UnknownHostException e) {
			Bukkit.getLogger().warning("[Mercury] Unable to connect to Venus");
			connected = false;
		} catch (IOException e) {
			Bukkit.getLogger().warning("[Mercury] Unable to connect to Venus");
			connected = false;
		}
	}
	
	//need to implement timeout for waiting for the welcome/reject message
	@SuppressWarnings("deprecation")
	private boolean registerVenus(String servername){
		output.println("register,"+ servername);
		output.flush();
		String response;
		
		try {
			while ((response = input.readLine()) != null){
				if (response.equals("welcome")){
					Bukkit.getLogger().info("[Mercury] Connected to Venus server!");
					return true;
				} else{
					Bukkit.getLogger().warning("[Mercury] Error registering with Venus: "+response);
					return false;
				}
			}
		} catch (IOException e) {
			Bukkit.getLogger().warning("[Mercury] Error registering with Venus: IO Error receiving packet");
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

	@SuppressWarnings("deprecation")
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
				Bukkit.getLogger().warning("[Mercury] Connection to Venus Server lost; reload plugin to reconnect.");
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
