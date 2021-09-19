package spoyogabot;

import java.io.File;

import java.util.concurrent.Callable;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDateTime;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

public class TelegramIt extends TelegramLongPollingBot implements Callable {
	private LocalDateTime message2post;
	
	private String chatId;
	private String dir;
	private String botToken;
	private String botUserName;

	public TelegramIt(Map<String, String> params, LocalDateTime message2post) {
		this.message2post = message2post;
		this.chatId = params.get("CHANEL_ID");
		this.dir = SpoYogaBot.getDir() + params.get("DIR") + File.separator;
		this.botToken = params.get("BOT_TOKEN");
		this.botUserName = params.get("BOT_USERNAME");
	}
	@Override
	public Boolean call() {
		StringBuilder text2send = new StringBuilder();
		String photo_id = "";
		try(Scanner scannedMessage = new Scanner(new File(dir + message2post.toString().replace(":", ".")), "UTF-8").useDelimiter("\\R")) {
			photo_id = scannedMessage.next();
			while(scannedMessage.hasNext())
				text2send.append(scannedMessage.next() + System.lineSeparator());
		}catch(Exception e) {
			e.printStackTrace();
		}
		return sendPhoto2channel(text2send.toString(), photo_id);
	}
    public boolean sendPhoto2channel(String text, String photo_id) {
		SendPhoto message = new SendPhoto();
    	message.setParseMode("MarkdownV2");
        message.setChatId(chatId);
    	try {
    		message.setPhoto(new InputFile(photo_id));
            message.setCaption(escapes(text));
           	return execute(message).getMessageId() >= 0;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return true;
    }
    @Override
    public void onUpdateReceived(Update update) {
    }

    @Override
    public String getBotUsername() {
        return botUserName == null ? "spoyogaBot" : botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
	private String escapes(String str) {
    	return 	str.replace(".", "\\.")
		           .replace("#", "\\#")
		           .replace("!", "\\!")
		           .replace("{", "\\{")
		           .replace("}", "\\}")
		           .replace("=", "\\=")
		           .replace("|", "\\|")
		           .replace("-","\\-")
                   .replace("(","\\(")
                   .replace("[","\\[")
                   .replace(")","\\)")
                   .replace("]","\\]")
//                 .replace("*","\\*")
                   .replace("~","\\~")
                   .replace("`","\\`")
                   .replace(">","\\>")
                   .replace("+","\\+")
//    	           .replace("_","\\_")
//    	           .replace("\\_\\_","__")
    	           ;
	}
}