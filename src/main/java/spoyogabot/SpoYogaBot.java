package spoyogabot;

import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

import java.io.File;
import java.nio.file.Paths;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class SpoYogaBot {
	private static String dir;

    public static void main(String[] args) {
    	dir = getInstallDir();
    	new Thread(new Scheduler(getParams())).start();
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Abili(getParams()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private static Map<String, String> getParams() {
    	String scanned = "";
    	HashMap<String, String> params = new HashMap<>();

    	try(Scanner scanner = new Scanner(new File(getInstallDir() + "settings")).useDelimiter("\\R")) {
    		while(scanner.hasNext()) {
    			scanned = scanner.next();
                if(scanned.startsWith("\\") || scanned.startsWith("//") || scanned.startsWith("#") || scanned.startsWith(";")) continue;
                
                if(scanned.contains("DIR"))
                	params.put("DIR", scanned.substring(3).replace("=", "").trim());
                if(scanned.contains("BOT_TOKEN"))
                	params.put("BOT_TOKEN", scanned.substring(9).replace("=", "").trim());
                if(scanned.contains("BOT_NAME"))
                	params.put("BOT_NAME", scanned.substring(8).replace("=", "").trim());
                if(scanned.contains("CHANEL_ID"))
                	params.put("CHANEL_ID", scanned.substring(9).replace("=", "").trim());
                if(scanned.contains("ADMINS_IDS"))
                	params.put("ADMINS_IDS", scanned.substring(10).replace("=", "").trim());
                if(scanned.contains("BOT_USERNAME"))
                	params.put("BOT_USERNAME", scanned.substring(12).replace("=", "").trim());
                if(scanned.contains("BOT_CREATOR_ID"))
                	params.put("BOT_CREATOR_ID", scanned.substring(14).replace("=", "").trim());
    		}
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
//    	params.forEach((k, v) -> System.out.println(k + " === " + v));
    	System.out.println(getDir() + params.get("DIR"));
    	return params;
    }
	private static String getInstallDir() {
    	try {
    		String installDir = Paths.get(SpoYogaBot.class        // These several strings of code help to understand where we are
    				           .getProtectionDomain()                   //
                               .getCodeSource()                         //
                               .getLocation()                           //
                               .getPath()).getParent().toString() + File.separator;//
    		System.out.println(installDir.replace(":", ""));
    		return installDir/*.replace(":", "")*/;
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	return "";
    }
	public static String getDir() {
		return dir;
	}
}