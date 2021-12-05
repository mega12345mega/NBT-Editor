package com.luneruniverse.minecraft.mod.nbteditor.sessionchanger;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.Agent;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

public class SessionChanger {

	private static SessionChanger instance;
	private final UserAuthentication auth;
	private final MicrosoftLogin microsoftLogin;
	
	public static SessionChanger getInstance() {
		if (instance == null) {
			instance = new SessionChanger();
		}

		return instance;
	}
	
	//Creates a new Authentication Service. 
	private SessionChanger() {
		UUID notSureWhyINeedThis = UUID.randomUUID(); //Idk, needs a UUID. Seems to be fine making it random
		AuthenticationService authService = new YggdrasilAuthenticationService(MinecraftClient.getInstance().getNetworkProxy(), notSureWhyINeedThis.toString());
		auth = authService.createUserAuthentication(Agent.MINECRAFT);
		authService.createMinecraftSessionService();
		microsoftLogin = new MicrosoftLogin();
	}

	
	//Online mode
	//Checks if your already loggin in to the account.
	public void setUser(String email, String password) {
		if(!MinecraftClient.getInstance().getSession().getUsername().equals(email) || MinecraftClient.getInstance().getSession().getAccessToken().equals("0")){

			this.auth.logOut();
			this.auth.setUsername(email);
			this.auth.setPassword(password);
			try {
				this.auth.logIn();
				Session session = new Session(this.auth.getSelectedProfile().getName(), UUIDTypeAdapter.fromUUID(auth.getSelectedProfile().getId()), this.auth.getAuthenticatedToken(), Optional.empty(), Optional.empty(), Session.AccountType.byName(this.auth.getUserType().name()));
				setSession(session);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	public void loginMicrosoftUser() {
		microsoftLogin.login(() -> {
			System.out.println("[Session Changer] Logged in");
		});
	}

	//Sets the session.
	//You need to make this public, and remove the final modifier on the session Object.
	void setSession(Session session) {
		try {
			MinecraftClient mc = MinecraftClient.getInstance();
			Field field = mc.getClass().getDeclaredField("session");
			field.setAccessible(true);
			field.set(mc, session);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Login offline mode
	//Just like MCP does
	public void setUserOffline(String username) {
		this.auth.logOut();
		Session session = new Session(username, username, "0", Optional.empty(), Optional.empty(), Session.AccountType.LEGACY);
		setSession(session);
	}

}