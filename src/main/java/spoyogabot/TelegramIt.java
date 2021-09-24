package spoyogabot;

import java.io.File;

import java.util.concurrent.Callable;
import java.util.Map;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;

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
		try {
		    return sendMedia2channel();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
    private boolean sendMedia2channel() throws Exception{
    	int photosAllTogather = 0;
    	int videosAllTogather = 0;
    	List<String> photoList = new ArrayList<>();
    	List<String> videoList = new ArrayList<>();
		StringBuilder text2send = new StringBuilder();
		try(Scanner scannedMessage = new Scanner(new File(dir + message2post.toString().replace(":", ".")), "UTF-8").useDelimiter("\\R")) {
			while(scannedMessage.hasNext()) {
				String nextStr = scannedMessage.next();
				
				if(nextStr.contains("photo ="))
					photoList.add(nextStr.substring(8));
				
				else if(nextStr.contains("video ="))
					videoList.add(nextStr.substring(8));
				
				else
		    	    text2send.append(nextStr + System.lineSeparator());
			}
		} catch (Exception e) {
		    e.printStackTrace();
		    return false;
		}
		
		photosAllTogather = photoList.size();
		videosAllTogather = videoList.size();
        if((photosAllTogather + videosAllTogather) == 0) {
        	SendMessage message = new SendMessage();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
            message.setText(escapes(text2send.toString()));
            execute(message);

        } else if(photosAllTogather < 2 && videosAllTogather == 0){
    		SendPhoto message = new SendPhoto();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
    	    message.setPhoto(new InputFile(photoList.get(0)));
            message.setCaption(escapes(text2send.toString()));
            execute(message);
        } else if(videosAllTogather < 2 && photosAllTogather == 0){
    		SendVideo message = new SendVideo();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
    	    message.setVideo(new InputFile(videoList.get(0)));
            message.setCaption(escapes(text2send.toString()));
            execute(message);
        } else if ((photosAllTogather + videosAllTogather) > 1) {

        	List<InputMedia> medias = new ArrayList<>();
        	
            for(int i = 0; i < photoList.size(); i++) {        		
            	InputMedia media = (InputMedia) new InputMediaPhoto();
            	if(i == 0)
            	    media.setCaption(escapes(text2send.toString()));
            	media.setMedia(photoList.get(i));
            	medias.add(media);
        	}
        	for(int i = 0; i < videoList.size(); i++) {
            	InputMedia media = (InputMedia) new InputMediaVideo();
            	if(i == 0)
            	    media.setCaption(escapes(text2send.toString()));
            	media.setMedia(videoList.get(i));
            	medias.add(media);
        	}
        	
            SendMediaGroup message = new SendMediaGroup();
            message.setChatId(chatId);
            message.setMedias(medias);
            execute(message);
            
        	SendMessage apendix = new SendMessage();
        	apendix.setParseMode("MarkdownV2");
        	apendix.setChatId(chatId);
        	apendix.setText(escapes(text2send.toString()));
            execute(apendix);
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
		byte esc = 92;
		char escape = (char) esc;
    	return 	str.replace(".", escape + ".")
		           .replace("#", escape + "#")
		           .replace("!", escape + "!")
		           .replace("{", escape + "{")
		           .replace("}", escape + "}")
		           .replace("=", escape + "=")
		           .replace("|", escape + "|")
		           .replace("-", escape + "-")
                   .replace("(", escape + "(")
                   .replace("[", escape + "[")
                   .replace(")", escape + ")")
                   .replace("]", escape + "]")
                   .replace("~", escape + "~")
                   .replace("`", escape + "`")
                   .replace(">", escape + ">")
                   .replace("+", escape + "+")
    	           ;
	}
}