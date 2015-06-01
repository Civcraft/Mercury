package vg.civcraft.mc.mercury.venus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.bukkit.Bukkit;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.MercuryPlugin;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;

public class MercuryService implements Runnable{
	
	private static MercuryPlugin plugin = MercuryPlugin.instance;
	private static MercuryAPI api = MercuryAPI.instance;
	private Socket socket;
	private DataInputStream input;
	private PrintWriter output;
	public String servername;
	public boolean connected = false;
	
	public MercuryService(MercuryPlugin plugin){
		enable();
	}
	
	public void destroy(){
		if (connected){
			// lock message queue
			this.shutdown();
		}
	}
	
	// message structure when receiving is: msg,plugin,message
	@SuppressWarnings("deprecation")
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
		servername = plugin.getConfig().getString("server-name").toLowerCase();
		//setup connection
		try {
			socket = new Socket(plugin.getConfig().getString("host"),plugin.getConfig().getInt("port"));
			input = new DataInputStream(socket.getInputStream());
			output = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
			if (registerVenus()){
				connected = true;
			}
		} catch (UnknownHostException e) {
			Bukkit.getLogger().warning(plugin.name+"Unable to connect to Venus");
			connected = false;
		} catch (IOException e) {
			Bukkit.getLogger().warning(plugin.name+"Unable to connect to Venus");
			connected = false;
		}
	}
	
	//need to implement timeout for waiting for the welcome/reject message
	@SuppressWarnings("deprecation")
	private boolean registerVenus() throws IOException{
		output.println("register,"+ servername);
		output.flush();
		String response;
		
		while ((response = input.readLine()) != null){
			if (response.equals("welcome")){
				Bukkit.getLogger().info(plugin.name+"Connected to Venus server!");
				return true;
			} else{
				Bukkit.getLogger().warning(plugin.name+"Error registering with Venus: "+response);
				return false;
			}
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
		String recv;
		while (connected && !socket.isClosed() && socket.isConnected()){
			try {
				if ((recv = input.readLine()) != null){
					receiveMessage(recv);
				}
			} catch (IOException e) {
				Bukkit.getLogger().warning(plugin.name+"Connection to Venus Server lost; reload plugin to reconnect.");
				shutdown();
				break;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
	}

	public synchronized void sendMessage(String destination, String plugin, String message) {
		// queue up message to be sent.
		// message structure when sending is: msg,server,plugin,message
		output.println("msg,"+destination+","+plugin+","+message);
		output.flush();
	}

}
