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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;

public class TelegramIt extends TelegramLongPollingBot implements Callable {
	private LocalDateTime message2post;
	private InlineKeyboardMarkup buttonsToAdd;
	
	private String chatId;
	private String dir;
	private String botToken;
	private String botUserName;
	private String[] urlButton;

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
	public InlineKeyboardMarkup createReactions(String reactions) {
		String[] reactionsIconsArray = reactions.split("/");
		
    	InlineKeyboardMarkup inlineKeyboardMarkup =new InlineKeyboardMarkup();
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
		List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
		
    	for(String reactionIcon : reactionsIconsArray) {
    		InlineKeyboardButton rmdButton = new InlineKeyboardButton();
    		rmdButton.setText(reactionIcon);
    		rmdButton.setCallbackData("rmButton_" + reactionIcon);
    		keyboardButtonsRow.add(rmdButton);
    	}
    	
		rowList.add(keyboardButtonsRow);
    	inlineKeyboardMarkup.setKeyboard(rowList);
    	
    	return buttonsToAdd == null ? inlineKeyboardMarkup : mixButtons(inlineKeyboardMarkup);
	}
	
	public InlineKeyboardMarkup createUrlButton(String title, String href) {
		
    	InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
		List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
		
		InlineKeyboardButton urlButton = new InlineKeyboardButton();
		urlButton.setText(title);
		urlButton.setUrl(href);
		keyboardButtonsRow.add(urlButton);
    	
		rowList.add(keyboardButtonsRow);
    	inlineKeyboardMarkup.setKeyboard(rowList);
    	
    	return buttonsToAdd == null ? inlineKeyboardMarkup : mixButtons(inlineKeyboardMarkup);		
	}
	
	private InlineKeyboardMarkup mixButtons(InlineKeyboardMarkup ikmToAdd) {
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
		
	    List<InlineKeyboardButton> row1 = buttonsToAdd.getKeyboard().get(0).size() != 1 
		    		                          ? buttonsToAdd.getKeyboard().get(0)
		    		                          : ikmToAdd.getKeyboard().get(0);
  	    List<InlineKeyboardButton> row2 = buttonsToAdd.getKeyboard().get(0).size() == 1 
		    		                          ? buttonsToAdd.getKeyboard().get(0)
		    		                          : ikmToAdd.getKeyboard().get(0);		    		                          

		
		rowList.add(row1);
		rowList.add(row2);
    	inlineKeyboardMarkup.setKeyboard(rowList);                        
		return inlineKeyboardMarkup;
		
	}
    private boolean sendMedia2channel() throws Exception{
  	
    	int photosAllTogather = 0;
    	int videosAllTogather = 0;
    	List<String> photoList = new ArrayList<>();
    	List<String> videoList = new ArrayList<>();
    	String sticker = null;
		StringBuilder text2send = new StringBuilder();
		try(Scanner scannedMessage = new Scanner(new File(dir + message2post.toString().replace(":", ".")), "UTF-8").useDelimiter("\\R")) {
			while(scannedMessage.hasNext()) {
				String nextStr = scannedMessage.next();
				if(!nextStr.startsWith("/*") && !nextStr.endsWith("*/")) {
					if(nextStr.contains("photo ="))
						photoList.add(nextStr.substring(8));
					
					else if(nextStr.contains("video ="))
						videoList.add(nextStr.substring(8));
					
					else if(nextStr.contains("sticker ="))
						sticker = nextStr.substring(10);
					
					else if(nextStr.contains("reactions ="))
						buttonsToAdd = createReactions(nextStr.substring(11).trim());
					
					else if(nextStr.contains("ButtonTitle =")) {
						String title = nextStr.substring(13).trim();
						String href = scannedMessage.next().substring(11).trim();
						buttonsToAdd = createUrlButton(title, href);
						
					}
					
					else
			    	    text2send.append(nextStr + System.lineSeparator());
				}
			}
		} catch (Exception e) {
		    e.printStackTrace();
		    return false;
		}
		
		photosAllTogather = photoList.size();
		videosAllTogather = videoList.size();

		if(sticker != null) {
        	SendSticker message = new SendSticker();
            message.setChatId(chatId);
            message.setSticker(new InputFile(sticker));
            
            if(buttonsToAdd != null)
                message.setReplyMarkup(buttonsToAdd);
            
            execute(message);
            return true;
		}
		
        if((photosAllTogather + videosAllTogather) == 0) {
        	SendMessage message = new SendMessage();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
            message.setText(text2send.toString());
            
            if(buttonsToAdd != null)
                message.setReplyMarkup(buttonsToAdd);
            
            execute(message);

        } else if(photosAllTogather < 2 && videosAllTogather == 0){
    		SendPhoto message = new SendPhoto();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
    	    message.setPhoto(new InputFile(photoList.get(0)));
            message.setCaption(text2send.toString());
            
            if(buttonsToAdd != null)
                message.setReplyMarkup(buttonsToAdd);
            
            execute(message);
        } else if(videosAllTogather < 2 && photosAllTogather == 0){
    		SendVideo message = new SendVideo();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
    	    message.setVideo(new InputFile(videoList.get(0)));
            message.setCaption(text2send.toString());
            
            if(buttonsToAdd != null)
                message.setReplyMarkup(buttonsToAdd);
            
            execute(message);
        } else if ((photosAllTogather + videosAllTogather) > 1) {

        	List<InputMedia> medias = new ArrayList<>();
        	
            for(int i = 0; i < photoList.size(); i++) {        		
            	InputMedia media = (InputMedia) new InputMediaPhoto();
/*            	if(i == 0)
            	    media.setCaption(text2send.toString());*/
            	media.setMedia(photoList.get(i));
            	medias.add(media);
        	}
        	for(int i = 0; i < videoList.size(); i++) {
            	InputMedia media = (InputMedia) new InputMediaVideo();
/*            	if(i == 0)
            	    media.setCaption(text2send.toString());*/
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
        	apendix.setText(text2send.toString());
        	
            if(buttonsToAdd != null)
            	apendix.setReplyMarkup(buttonsToAdd);
        	
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
/*	private String escapes(String str) {
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
	}*/
}