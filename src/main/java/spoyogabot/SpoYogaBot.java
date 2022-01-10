package spoyogabot;

import java.util.Map;
import java.util.HashMap;
import java.awt.SystemTray;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SpoYogaBot {
	private static String dir;

    public static void main(String[] args) {
        

    	// Let's try Tray
    	
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
//          return;
        }
        else {
            Image image = Toolkit.getDefaultToolkit().getImage("tplogo.png");

            final PopupMenu popup = new PopupMenu();
            final TrayIcon trayIcon = new TrayIcon(image, "TPlanner", popup);
            final SystemTray tray = SystemTray.getSystemTray();

            MenuItem blankItem = new MenuItem("");
            MenuItem exitItem = new MenuItem("Выход");
            exitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(1);
                }
            });
            popup.add(exitItem);
            popup.add(blankItem);

            trayIcon.setPopupMenu(popup);
            trayIcon.setImageAutoSize(true);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.out.println("TrayIcon could not be added.");
            }
        }
    	//
    	
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
//    	String scanned = "";
        String scanned = null;
    	HashMap<String, String> params = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader("settings"))) {
		    while((scanned = br.readLine()) != null) {
				if(scanned.startsWith("\\") || scanned.startsWith("//") || scanned.startsWith("#") || scanned.startsWith(";")) continue;
                
                if(scanned.contains("DIR"))
                	params.put("DIR", scanned.substring(3).replace("=", "").trim());
                if(scanned.contains("BOT_TOKEN"))
                	params.put("BOT_TOKEN", scanned.substring(9).replace("=", "").trim());
                if(scanned.contains("BOT_NAME"))
                	params.put("BOT_NAME", scanned.substring(8).replace("=", "").trim());
                if(scanned.contains("CHANNELS_ID")) {
                    String channels = scanned.substring(11).replace("=", "").trim();
                    params.put("CHANNELS_ID", channels);
                    for(String cname : channels.split(" ")) {
                        cname = cname/*.substring(1)*/;
                        Path chanDirPath = Path.of(params.get("DIR") + File.separator + cname);
                        if(Files.notExists(chanDirPath)) {
                            try {
                                Files.createDirectories(chanDirPath);
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if(scanned.contains("ADMINS_IDS"))
                	params.put("ADMINS_IDS", scanned.substring(10).replace("=", "").trim());
                if(scanned.contains("BOT_USERNAME"))
                	params.put("BOT_USERNAME", scanned.substring(12).replace("=", "").trim());
                if(scanned.contains("BOT_CREATOR_ID"))
                	params.put("BOT_CREATOR_ID", scanned.substring(14).replace("=", "").trim());
			}
        } catch (Exception e) {
	        e.printStackTrace();
	    }

    	return params;
    }
	private static String getInstallDir() {

    	return System.getProperty("user.dir") + File.separator;
    }
	public static String getDir() {
		return dir;
	}
}