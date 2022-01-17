package com.luneruniverse.minecraft.mod.nbteditor.sessionchanger;

import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

public class LoginUtil {

    public static String lastMojangUsername = null;
    public static boolean needsRefresh = true;
    public static boolean wasOnline = false;
    private static long lastCheck = -1L;

    private static YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(MinecraftClient.getInstance().getNetworkProxy(), UUID.randomUUID().toString());
    private static YggdrasilUserAuthentication userAuth = (YggdrasilUserAuthentication) authService.createUserAuthentication(Agent.MINECRAFT);
    private static YggdrasilMinecraftSessionService minecraftSessionService = (YggdrasilMinecraftSessionService) authService.createMinecraftSessionService();

    public static void updateOnlineStatus() {
        needsRefresh = true;
        isOnline();
    }

    public static boolean isOnline() {
        if (!needsRefresh && System.currentTimeMillis() - lastCheck < 1000 * 10) {
            return wasOnline;
        }
        Session session = MinecraftClient.getInstance().getSession();
        String uuid = UUID.randomUUID().toString();
        needsRefresh = false;
        lastCheck = System.currentTimeMillis();
        try {
            minecraftSessionService.joinServer(session.getProfile(), session.getAccessToken(), uuid);
            if (minecraftSessionService.hasJoinedServer(session.getProfile(), uuid, null).isComplete()) {
                wasOnline = true;
                return true;
            } else {
                wasOnline = false;
                return false;
            }
        } catch (AuthenticationException e) {
            wasOnline = false;
            return false;
        }
    }

    public static void loginMs(MicrosoftLogin.MinecraftProfile profile) {
        Session session = new Session(profile.name, profile.id, profile.token.accessToken, Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
        setSession(session);
    }

    public static Optional<Boolean> loginMojangOrLegacy(String username, String password) {
        try {
            if (password.isEmpty()) {
                Session session = new Session(username, UUID.nameUUIDFromBytes(username.getBytes()).toString(), null, Optional.empty(), Optional.empty(), Session.AccountType.LEGACY);
                setSession(session);
                return Optional.of(true);
            }
            userAuth.setUsername(username);
            userAuth.setPassword(password);
            userAuth.logIn();

            String name = userAuth.getSelectedProfile().getName();
            String uuid = UUIDTypeAdapter.fromUUID(userAuth.getSelectedProfile().getId());
            String token = userAuth.getAuthenticatedToken();
            String type = userAuth.getUserType().getName();
            userAuth.logOut();

            Session session = new Session(name, uuid, token, Optional.empty(), Optional.empty(), Session.AccountType.byName(type));
            setSession(session);
            lastMojangUsername = username;
            return Optional.of(true);
        } catch (AuthenticationUnavailableException e) {
            return Optional.empty();
        } catch (AuthenticationException e) {
            return Optional.of(false);
        }
    }

    private static void setSession(Session session) {
        needsRefresh = true;
        updateOnlineStatus();
        SessionChanger.getInstance().setSession(session);
    }

}
