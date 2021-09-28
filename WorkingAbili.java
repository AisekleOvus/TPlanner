package spoyogabot;

import java.util.stream.Stream;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Scanner;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;

public class Abili extends AbilityBot {

	private String dir;

	private SimpleMessage savedMessage;
	private int lastMessageId;
	private int step;
	private LocalDate messageDate;
	private LocalTime messageTime;
//	private String savedMessage;
	private String userSaidControll;
	private List<Integer> messages;
	private String admins;
	private String creatorId;
	private Update lastUpdate;
	private ReplyKeyboardMarkup replyKeyboardMarkup;
	
	public Abili(Map<String, String> params) {
		super(params.get("BOT_TOKEN"), params.get("BOT_USERNAME"));
		this.dir = SpoYogaBot.getDir() + params.get("DIR") + File.separator;
		this.admins = params.get("ADMINS_IDS");
		this.creatorId = params.get("BOT_CREATOR_ID");
		messages = new ArrayList<>();
		step = 1;
//		savedMessage = "";
		replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Очередь публикации");
        keyboard.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
	}

	
	public void keepDialog(SendMessage message, String chatId, String text, boolean disableWebPagePreview) {
		message.setParseMode("MarkdownV2");
        message.setChatId(chatId);
        message.setDisableWebPagePreview(disableWebPagePreview);
        text = !text.equals(null) ? text : "?";
        message.setText(text);
        message.setReplyMarkup(replyKeyboardMarkup);
        try {
        	lastMessageId = execute(message).getMessageId();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public void keepDialog(SendPhoto message, String chatId, String text, String photo_id, boolean disableWebPagePreview) {
		message.setParseMode("MarkdownV2");
        message.setChatId(chatId);
        message.setPhoto(new InputFile(photo_id));
        text = !text.equals(null) ? text : "?";
        message.setCaption(text);
        message.setReplyMarkup(replyKeyboardMarkup);
        try {
        	lastMessageId = execute(message).getMessageId();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
    @Override
    public void onUpdateReceived(Update update) {
    	User user = null;
    	String userSaid = null;
    	String chatId = null;
    	String photo_id = null;
    	String video_id = null;
        
    	if(update.hasCallbackQuery()) {
            userSaid = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            if(userSaid.contains("delete_")) {
                deleteMessage(chatId, userSaid.substring(7).replace(":","."));
            } else {
        		ArrayList<String> messagesQueue = (ArrayList<String>) getMessagesList();
        		try {
            		if(messagesQueue.contains(userSaid))
            			showTheMessage(chatId, userSaid);
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
            }
    	}

        if (update.hasMessage() && admins.contains("@" + update.getMessage().getFrom().getUserName())){
            Message recivedMessage = update.getMessage();
            chatId = recivedMessage.getChatId().toString();
            
            
            if("/start".equals(recivedMessage.getText())) {
            } 
            else if ("Очередь публикации".equals(recivedMessage.getText())) {
            	showPublicationQueue(chatId);
            } else if (recivedMessage.hasText()) {
            	userSaid = recivedMessage.getText(); 
//            	System.out.println("Получено тексовое сообщение: " + System.lineSeparator() + userSaid);
            }
            
            if(recivedMessage.hasPhoto()) {
        		userSaid = update.getMessage().getCaption();
        		photo_id = update.getMessage().getPhoto().stream()
        				.findFirst()
        				.orElse(null)
        				.getFileId();
 //           	System.out.println("Получено сообщение с фото: "+ System.lineSeparator() + photo_id + System.lineSeparator() + userSaid);
            }
            if(recivedMessage.hasVideo()) {
        		userSaid = update.getMessage().getCaption();
        		video_id = update.getMessage().getVideo().getFileId();
        		
 //           	System.out.println("Получено сообщение с видео: "+ System.lineSeparator() + video_id + System.lineSeparator() + userSaid);
            }
            if(savedMessage == null && userSaid != null) {
            	savedMessage = new SimpleMessage();

 //           	System.out.println("Сохраняем текст");
            	savedMessage.setText(userSaid);
            	if(photo_id != null) {
 //           		System.out.println("Сохраняем фото");
            		savedMessage.setMediaFileId(photo_id, "photo");
            	}
            	if(video_id != null) {
    //        		System.out.println("Сохраняем видео");
            		savedMessage.setMediaFileId(video_id, "video");
            	}
            	if(recivedMessage.getMediaGroupId() != null) {
 //           		System.out.println("Media Group detected : " + recivedMessage.getMediaGroupId());
            		savedMessage.setMediaGroupId(recivedMessage.getMediaGroupId());
            	}
            	    askDate(chatId);
            } 
            else if(savedMessage != null && update.getMessage().getCaption() == null) {
            	// Если ID медиагруппы, полученного сообщения совпадает с сохраненным, значит идет передача медиагруппы и нужно добавить
            	if(savedMessage.getMediaGroupId() != null && savedMessage.getMediaGroupId().equals(recivedMessage.getMediaGroupId())) {
                	if(photo_id != null)
                		savedMessage.setMediaFileId(photo_id, "photo");

                	if(video_id != null)
                		savedMessage.setMediaFileId(video_id, "video");

            	}
            	else if(savedMessage.dateIsEmpty() && checkDate(userSaid, chatId)) {
            	    savedMessage.setDate(messageDate);
            	    askTime(chatId);
            	}
            	else if(savedMessage.timeIsEmpty() && checkTime(userSaid, chatId)) {
            	    savedMessage.setTime(messageTime);
            	    saveMessage(chatId);
            	}
            }
        }
     }
    
    private void askDate(String chatId) {

    	if(!savedMessage.mediaGroupIdIsEmpty()) {
        	SendMessage message = new SendMessage();
        	message.setParseMode("MarkdownV2");
        	String text = "К сожалению Telegram, пока не разрешает ботам отправлять в чаты Медиа\\-Альбомы с полноценным форматированием, ваши" 
        			      + " медиафайлы будут отправлены одним сообщением, а текст будет отправлен следом отдельным сообщением\\.";

        	keepDialog(message, chatId, text, false);
    	}
    	SendMessage message = new SendMessage();
    	message.setParseMode("MarkdownV2");
    	String text = "Введите дату публикации\\(дд\\.мм\\.гггг\\)";
    	step = 2;
    	keepDialog(message, chatId, text, false);
    }
    private void askTime(String chatId) {
    	SendMessage message = new SendMessage();
    	message.setParseMode("MarkdownV2");
    	String text = "Введите время публикации\\(ЧЧ:ММ:СС\\)";
    	step = 3;
    	keepDialog(message, chatId, text, false);
    }
    private boolean checkDate(String date, String chatId) {
    	try {
    	    messageDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.uuuu"));
    	} catch (DateTimeParseException dtpe) {
        	SendMessage message = new SendMessage();
            message.setChatId(chatId);
            String text = "Дата дожна быть в формате \n *ДД\\.ММ\\.ГГГГ*";
    		keepDialog(message, chatId, text, false);
    		return false;
    	}
	
    	return true;
    }
    private boolean checkTime(String time, String chatId) {
    	try {
    	    messageTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss"));
    	} catch (DateTimeParseException dtpe) {
        	SendMessage message = new SendMessage();
            message.setChatId(chatId);
            String text = "Время дожно быть в формате \n *ЧЧ:ММ:СС*";
    		keepDialog(message, chatId, text, false);
    		return false;
    	}
    	return true;
    }
    private void saveMessage(String chatId) {
    	LocalDateTime messageFileName = LocalDateTime.of(savedMessage.getDate(), savedMessage.getTime());

    	SendMessage message = new SendMessage();    	
		String answerText = "**Ok**\nСообщение будет опубликовано\n" + "**" + messageFileName.toLocalDate().format(DateTimeFormatter.ofPattern("dd\\.MM\\.uuuu"))
    			+ "в " + messageFileName.toLocalTime() + "**";
    	keepDialog(message, chatId, answerText,false);
    
    	try(FileWriter fwr = new FileWriter(new File(dir+messageFileName.toString().replace(":", ".")), StandardCharsets.UTF_8)) {
        	fwr.append(savedMessage.toString());
        	fwr.flush();
    	} catch (IOException ioe) {
    		ioe.printStackTrace();
    	} finally {
    	    savedMessage = null;
    	}
    }
    public void showTheMessage(String chatId, String message2show) throws Exception {
    	int photosAllTogather = 0;
    	int videosAllTogather = 0;
    	List<String> photoList = new ArrayList<>();
    	List<String> videoList = new ArrayList<>();
    	InlineKeyboardMarkup inlineKeyboardMarkup =new InlineKeyboardMarkup();
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
		InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
		inlineKeyboardButton.setText("Удалить");
		inlineKeyboardButton.setCallbackData("delete_" + message2show);
		List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
		keyboardButtonsRow.add(inlineKeyboardButton);
		rowList.add(keyboardButtonsRow);
    	inlineKeyboardMarkup.setKeyboard(rowList);
   	
		StringBuilder text2send = new StringBuilder();
		text2send.append("Запланировано на *" + LocalDateTime.parse(message2show.replace(".", ":")).format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm:ss")).toString() + "*" + System.lineSeparator());
		String photo_id = "";
		try(Scanner scannedMessage = new Scanner(new File(dir + message2show), "UTF-8").useDelimiter("\\R")) {
			while(scannedMessage.hasNext()) {
				String nextStr = scannedMessage.next();
				
				if(nextStr.contains("photo =")) {
					photoList.add(nextStr.substring(8));
//				    System.out.println(nextStr.substring(8));
			    }			
				else if(nextStr.contains("video =")) {
					videoList.add(nextStr.substring(8));
//					System.out.println(nextStr.substring(8));
				}
				
				else
		    	    text2send.append(nextStr + System.lineSeparator());
			}
		} catch (Exception e) {
		    e.printStackTrace();
		}
		photosAllTogather = photoList.size();
		videosAllTogather = videoList.size();
        if((photosAllTogather + videosAllTogather) == 0) {
        	SendMessage message = new SendMessage();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
            message.setText(escapes(text2send.toString()));
        	message.setReplyMarkup(inlineKeyboardMarkup);
            execute(message);

        } else if(photosAllTogather < 2 && videosAllTogather == 0){
    		SendPhoto message = new SendPhoto();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
    	    message.setPhoto(new InputFile(photoList.get(0)));
            message.setCaption(escapes(text2send.toString()));
        	message.setReplyMarkup(inlineKeyboardMarkup);
            execute(message);
        } else if(videosAllTogather < 2 && photosAllTogather == 0){
    		SendVideo message = new SendVideo();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
    	    message.setVideo(new InputFile(videoList.get(0)));
            message.setCaption(escapes(text2send.toString()));
        	message.setReplyMarkup(inlineKeyboardMarkup);
            execute(message);
        } else if ((photosAllTogather + videosAllTogather) > 1) {

        	List<InputMedia> medias = new ArrayList<>();
        	
//        	for(String photoId : photoList) {
        	for(int i = 0; i < photoList.size(); i++) {
 //       		System.out.println("Extract photoId: " + photoId);
            	InputMedia media = (InputMedia) new InputMediaPhoto();
/*            	if(i == 0)
            	    media.setCaption(escapes(text2send.toString()));*/
            	media.setMedia(photoList.get(i));
            	medias.add(media);
        	}
//        	for(String videoId : videoList) {
        	for(int i = 0; i < videoList.size(); i++) {
//        		System.out.println("Extract videoId: " + videoList.get(i));
            	InputMedia media = (InputMedia) new InputMediaVideo();
/*            	if(i == 0)
            	    media.setCaption(escapes(text2send.toString()));*/
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
        	apendix.setReplyMarkup(inlineKeyboardMarkup);
            execute(apendix);
        }
    }
    public List<String> getMessagesList() {
    	List<String> mNames = new ArrayList<>();
    	try(Stream<Path> paths = Files.walk(Paths.get(dir))){
    		mNames = paths.filter(Files::isRegularFile)
    				      .collect(ArrayList::new, (al, p) -> al.add(p.getFileName().toString()), ArrayList::addAll);
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	return mNames;
    }
    public void showPublicationQueue(String chatId) {
    	SendMessage message = new SendMessage();
    	InlineKeyboardMarkup inlineKeyboardMarkup =new InlineKeyboardMarkup();
    	ArrayList<String> mNames = (ArrayList<String>) getMessagesList(); 
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

    	for(String mname : mNames) {
    		 InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
    		 inlineKeyboardButton.setText(mname.replace(".", ":"));
    		 inlineKeyboardButton.setCallbackData(mname);
    		 List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
    		 keyboardButtonsRow.add(inlineKeyboardButton);
    		 rowList.add(keyboardButtonsRow);
    	}
    	inlineKeyboardMarkup.setKeyboard(rowList);
    	message.setReplyMarkup(inlineKeyboardMarkup);
    	message.setChatId(chatId);
    	message.setText(mNames.isEmpty() ? "Очередь публикаций пуста !" : "Очередь публикации :");
    	try {
    		execute(message);
    	} catch (Exception e) {}
    }
    private void deleteMessage(String chatId, String message2delete) {
    	new File(dir + message2delete).delete();
    	keepDialog(new SendMessage(), chatId, "Сообщение не будет опубликовано \\- оно удалено \\!", true);
//		System.out.println(dir + message2delete + " has deleted !");
    	
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
	@Override
	public int creatorId() {
	    return creatorId == null ? 706141860 : Integer.parseInt(creatorId);
	}

}