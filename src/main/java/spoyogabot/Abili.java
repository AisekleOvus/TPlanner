package spoyogabot;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Iterator;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
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
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;

import org.apache.commons.validator.routines.UrlValidator;

public class Abili extends AbilityBot {

	private String dir;
	private SimpleMessage savedMessage;
	private Message messageEditText;
	private Message timeLineText;
	private Message messageEditMedia;
	private Message messageToReturn;
	private Message titleMessage;
	private Message appendixMessage;
	private List<Message> editable;
	private List<Message> wwhMessage;
	private List<Integer> messages;
	private String step;
	private String urlButtonTitle;
	private String urlButtonHref;
	private String oldFileName;
	private String admins;
    private String channels;
    private String channelName;
	private String creatorId;
	private String userSaidControll;
	private LocalDate messageDate;
	private LocalTime messageTime;
	private Update lastUpdate;
	private ReplyKeyboardMarkup replyKeyboardMarkup;


	private Set<Integer> messageToDeleteId;


	private boolean changingPostTime;
	private Path exceptDeletedMessage;

	private boolean editorModeOn;
	private String messageToEdit;
	
	public Abili(Map<String, String> params) {
		super(params.get("BOT_TOKEN"), params.get("BOT_USERNAME"));
		this.dir = SpoYogaBot.getDir() + params.get("DIR") + File.separator;
		this.admins = params.get("ADMINS_IDS");
		this.creatorId = params.get("BOT_CREATOR_ID");
        this.channels = params.get("CHANNELS_ID");
		changingPostTime = false;
		messageToDeleteId = new HashSet<>();
		wwhMessage = new ArrayList<>();
        editable = new ArrayList<>();
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

	
	public Message keepDialog(SendMessage message, String chatId, String text, boolean disableWebPagePreview) {
		Integer mId = 0;
		message.setParseMode("MarkdownV2");
        message.setChatId(chatId);
        message.setDisableWebPagePreview(disableWebPagePreview);
        text = !text.equals(null) ? text : "?";
        message.setText(text);
        message.setReplyMarkup(replyKeyboardMarkup);
        try {
        	return execute(message);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
	}
	
	public Message keepDialog(SendPhoto message, String chatId, String text, String photo_id, boolean disableWebPagePreview) {
		Integer mId = 0;
		message.setParseMode("MarkdownV2");
        message.setChatId(chatId);
        message.setPhoto(new InputFile(photo_id));
        text = !text.equals(null) ? text : "?";
        message.setCaption(text);
        message.setReplyMarkup(replyKeyboardMarkup);
        try {
        	return execute(message);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
	}
	private void buttonDeleter(Message message) {
		buttonDeleter(null, message);
		
	}
	private void buttonDeleter(String buttonToDelete, Message message) {
		List<List<InlineKeyboardButton>> rowList = null;
		List<List<InlineKeyboardButton>> timeLineRowList = null;
		InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		InlineKeyboardMarkup timeLineInlineKeyboardMarkup = new InlineKeyboardMarkup();
		
		if(buttonToDelete == null) {
//			rowList = message.getReplyMarkup().getKeyboard()
			rowList = messageEditText.getReplyMarkup().getKeyboard()
                    .stream()
                    .map(ikbList -> ikbList.stream()
                  		                 .map(ikb -> { 
                  		                	              if(!ikb.getText().contains("❌") && ikb.getUrl() != null) {
                  		                	                  ikb.setCallbackData(ikb.getText() + "_❌");
                  		                	                  ikb.setText("❌ " + ikb.getText());
                                                              ikb.setUrl(null);
                  		                                  }
                  		                               return ikb;
                  		                 })
                  		                 .collect(Collectors.toList()))
                    .collect(Collectors.toList());

			inlineKeyboardMarkup.setKeyboard(rowList);	

			EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
//			emrm.setChatId(message.getChatId().toString());
			emrm.setChatId(messageEditText.getChatId().toString());
			emrm.setMessageId(messageEditText.getMessageId());
			emrm.setReplyMarkup(inlineKeyboardMarkup);

			try {
			    execute(emrm);
			} catch (Exception e) {
//			    e.printStackTrace();
			}
		} else {
//			List<List<InlineKeyboardButton>> timeLineRowList = null;
			String deleteButtonName = buttonToDelete.substring(0, (buttonToDelete.indexOf("_❌")));
			List<String[]> oldUrlButtonsList = savedMessage.getUrlButton();
			for(int i = 0; i < oldUrlButtonsList.size(); i++) {
				if(deleteButtonName.equals(oldUrlButtonsList.get(i)[0])) {
					oldUrlButtonsList.remove(i);
				}
			}
			savedMessage.setUrlButton(oldUrlButtonsList);
			
			rowList = messageEditText.getReplyMarkup().getKeyboard()//;
/*			int removeButtonIndx = 0;

			for(List<InlineKeyboardButton> row : rowList) {
				System.out.print(buttonToDelete + " = ");
				row.stream().forEach(ikb -> System.out.println(ikb.getCallbackData()));
                if(row.stream().anyMatch(ikb -> ikb.getCallbackData().contains(buttonToDelete))) {
					rowList.remove(removeButtonIndx + 1);
					break;
				}
				removeButtonIndx++;
			}*/

                    .stream()
                    .map(ikbList -> ikbList.stream()
                  		                 .filter(ikb -> !ikb.getCallbackData().contains(buttonToDelete))
                  		                 .collect(Collectors.toList()))
                    .collect(Collectors.toList());
			
			System.out.println("oldUrlButtonsList contains " + oldUrlButtonsList.size() + " elments(s)");
			if(oldUrlButtonsList.isEmpty()) {
//				rowList = rowList.stream()
				timeLineRowList = timeLineText.getReplyMarkup().getKeyboard().stream()
                                 .map(ikbList -> ikbList.stream()
              		                                    .filter(ikb -> !ikb.getText().contains("Удалить кнопку"))
              		                                    .collect(Collectors.toList()))
                                 .collect(Collectors.toList());

				timeLineInlineKeyboardMarkup.setKeyboard(timeLineRowList);
				EditMessageReplyMarkup tlemrm = new EditMessageReplyMarkup();
				tlemrm.setChatId(timeLineText.getChatId().toString());
				tlemrm.setMessageId(timeLineText.getMessageId());
				tlemrm.setReplyMarkup(timeLineInlineKeyboardMarkup);

				try {
					execute(tlemrm);
				} catch (Exception e) {
//			    e.printStackTrace();
				}
			}

			inlineKeyboardMarkup.setKeyboard(rowList);	

			EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
//			emrm.setChatId(message.getChatId().toString());
			emrm.setChatId(timeLineText.getChatId().toString());
			emrm.setMessageId(messageEditText.getMessageId());
			emrm.setReplyMarkup(inlineKeyboardMarkup);
			
			try {
			    execute(emrm);
			} catch (Exception e) {
//			    e.printStackTrace();
			}
			messageEditText.setReplyMarkup(inlineKeyboardMarkup);
//			saveMessage(chatId, true);
		}
	}
    private void yesClearButtonHandler(String chatId, Message mem) {
        savedMessage.clearMedia();
        try {
            abortarium(chatId);
            Message mmm = showTheMessage(chatId, savedMessage.toString());
//            editorMode(mmm, chatId, false);
            editorMode(timeLineText, chatId, false);
        } catch(Exception e) {
            e.printStackTrace();
        }
 /*       InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
// wwhMessage - contains displayed messages

        for(List<InlineKeyboardButton> ikbl : mem.getReplyMarkup().getKeyboard()) {
            List<InlineKeyboardButton> buttonRow = ikbl;

            if(ikbl.stream().anyMatch(ikb -> ikb.getCallbackData().contains("noClear"))) {
                List<InlineKeyboardButton> clearButtonRow = new ArrayList<>();
                InlineKeyboardButton clearMediaButton = new InlineKeyboardButton();
                clearMediaButton.setText("🖼 Очистить медиа");
                clearMediaButton.setCallbackData("clearMedia_");
                clearButtonRow.add(clearMediaButton);
                rowList.add(clearButtonRow);
            } else {
                rowList.add(buttonRow);
            }*/
 /*       }

        inlineKeyboardMarkup.setKeyboard(rowList);
        EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
        emrm.setChatId(chatId);
        emrm.setMessageId(mem.getMessageId());
        emrm.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(emrm);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
    private void noClearButtonHandler(String chatId, Message mem) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

//        for(List<InlineKeyboardButton> ikbl : mem.getReplyMarkup().getKeyboard()) {
        for(List<InlineKeyboardButton> ikbl : timeLineText.getReplyMarkup().getKeyboard()) {
            List<InlineKeyboardButton> buttonRow = ikbl;

            if(ikbl.stream().anyMatch(ikb -> ikb.getCallbackData().contains("noClear"))) {
                List<InlineKeyboardButton> clearButtonRow = new ArrayList<>();
                InlineKeyboardButton clearMediaButton = new InlineKeyboardButton();
                clearMediaButton.setText("🖼 Очистить медиа");
                clearMediaButton.setCallbackData("clearMedia_");
                clearButtonRow.add(clearMediaButton);
                rowList.add(clearButtonRow);
            } else {
                rowList.add(buttonRow);
            }
        }

        inlineKeyboardMarkup.setKeyboard(rowList);
        EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
        emrm.setChatId(chatId);
//        emrm.setMessageId(mem.getMessageId());
        emrm.setMessageId(timeLineText.getMessageId());
        emrm.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(emrm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	private void clearMedia(String chatId, Message mem) {

//        boolean status = mem.getReplyMarkup().getKeyboard().stream()
        boolean status = timeLineText.getReplyMarkup().getKeyboard().stream()
				.flatMap(ikbl -> ikbl.stream())
				.anyMatch(ikb -> ikb.getText().contains("Очистить медиа"));
		if(status) {
			InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
			List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

//			for(List<InlineKeyboardButton> ikbl : mem.getReplyMarkup().getKeyboard()) {
            for(List<InlineKeyboardButton> ikbl : timeLineText.getReplyMarkup().getKeyboard()) {
				List<InlineKeyboardButton> buttonRow = ikbl;

				for(InlineKeyboardButton ikb : ikbl) {
					if(ikb.getText().contains("Очистить медиа")) {
						buttonRow = new ArrayList<>();
						InlineKeyboardButton yesClearMediaButton = new InlineKeyboardButton();
						yesClearMediaButton.setText("✅ ДА");
						yesClearMediaButton.setCallbackData("yesClear");
						buttonRow.add(yesClearMediaButton);

						InlineKeyboardButton noClearMediaButton = new InlineKeyboardButton();
						noClearMediaButton.setText("❎ НЕТ");
						noClearMediaButton.setCallbackData("noClear");
						buttonRow.add(noClearMediaButton);
					}
				}
				rowList.add(buttonRow);
			}
			inlineKeyboardMarkup.setKeyboard(rowList);
			EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
			emrm.setChatId(chatId);
//			emrm.setMessageId(mem.getMessageId());
            emrm.setMessageId(timeLineText.getMessageId());
			emrm.setReplyMarkup(inlineKeyboardMarkup);
			try {
				execute(emrm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void addMedia(String chatId, Message mem) {
//        System.out.println("The message: \n" + savedMessage.toString());
		messageEditMedia = mem;
		addMedia(chatId, null, null, null);
		
	}
	private void addMedia(String chatId, Integer messageId, String mediaId, String mediaType) {
//		System.out.println("The message: \n" + savedMessage.toString());
		if(mediaType == null) {
			step = "wantedMedia";
			SendMessage message = new SendMessage();
			messageToDeleteId.add(keepDialog(message, chatId, "Добавляйте медиа по одному файлу\nТекст в медиа будет проигнорирован\nЕсли редактируемая публикация содержит текст длиннее 984 знаков \\- он будет сокоращен до этого значения", false).getMessageId());
		} else {
			step = null;
			messageDeleter(chatId);
            savedMessage.setMediaFileId(mediaId, mediaType);
            try {
            	abortarium(chatId);
                Message mmm = showTheMessage(chatId, savedMessage.toString());
 //               editorMode(mmm, chatId, true);
//                System.out.println("addMedia(): timeLineText: \n" + timeLineText);
                editorMode(timeLineText, chatId, true);
            } catch(Exception e) {
            	e.printStackTrace();
            }
            
		}
	}
	
    @Override
    public void onUpdateReceived(Update update) {
    	User user = null;
    	String userSaid = null;
    	String chatId = null;
    	String photo_id = null;
    	String video_id = null;
    	String sticker_id = null;

    	if(update.hasCallbackQuery()) {
            userSaid = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getMessage().getChatId().toString();

            if(userSaid.contains("\uD83D\uDCE3")) {
                String cname = userSaid.substring(3, userSaid.lastIndexOf("_"));
                String whatFor = userSaid.substring(userSaid.lastIndexOf("_") + 1);
                channelName = cname;
//                System.out.println(whatFor);
                if("showPubQueue".equals(whatFor))
                    showPublicationQueue(chatId);
				if("postMessage".equals(whatFor)) {
                    savedMessage.setChannelName(cname);
                    whenToPost(chatId);
                }
            }
            if(userSaid.contains("noClear")) {
                noClearButtonHandler(chatId, update.getCallbackQuery().getMessage());
            }
            if(userSaid.contains("yesClear")) {
                yesClearButtonHandler(chatId, update.getCallbackQuery().getMessage());
            }
            if(userSaid.contains("addMedia_")) {
            	addMedia(chatId, update.getCallbackQuery().getMessage());

				if(savedMessage.getMediaFileId().size() == 1 && userSaid.length() > 984)
			        warnSignCounter(chatId, false);
            }
			if(userSaid.contains("clearMedia_")) {
				clearMedia(chatId, update.getCallbackQuery().getMessage());
			}
            
            if(userSaid.contains("saveEdited_")) {
            	editorModeOn = false;
            	saveMessage(chatId, true);
            }
            
            if(userSaid.contains("_❌")) {
            	editorModeOn = true;
//            	buttonDeleter(userSaid, update.getCallbackQuery().getMessage());
                buttonDeleter(userSaid, messageEditText);
            }
            if(userSaid.contains("deleteUrlButton")) {
//            	buttonDeleter(update.getCallbackQuery().getMessage());
                buttonDeleter(messageEditText);
            }
            
            if(userSaid.contains("rewriteMessage_")) {
                editorModeOn = true;
            	step = "rewriteMessageText";

//            	messageEditText = update.getCallbackQuery().getMessage();
            	messageToDeleteId.remove(messageEditText.getMessageId());
            	
            	SendMessage eMessage = new SendMessage();
			                eMessage.setChatId(chatId);
			                eMessage.setText("Пришлите новый текст публикации:");
            	
            	try {
            		messageToDeleteId.add(execute(eMessage).getMessageId());
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }
            if(userSaid.contains("c_choice")) {
                messageDeleter(chatId, update.getCallbackQuery().getMessage().getMessageId());
                abortarium(chatId);
                editorModeOn = false;
                setChannelSelection(chatId,"showPubQueue");
            }
            if(userSaid.contains("show_pub_queue")) {
                messageDeleter(chatId, update.getCallbackQuery().getMessage().getMessageId());
                abortarium(chatId);
                editorModeOn = false;
                if(channelName == null)
                   setChannelSelection(chatId,"showPubQueue");
                else
                    showPublicationQueue(chatId);

            }
            
            if(userSaid.contains("delete_")) {
try {
                exceptDeletedMessage = Path.of(dir,savedMessage.getChannelName(),savedMessage.getMessageFileName());
                deleteMessage(chatId, savedMessage.getMessageFileName(), savedMessage.getChannelName(), false);
            	step = null;
            	
                if(wwhMessage != null) {
                    for(Message wwhm : wwhMessage)
                    	messageToDeleteId.add(wwhm.getMessageId());
                }
                messageDeleter(chatId);
                
            	if(appendixMessage != null) {
            	    messageDeleter(chatId, appendixMessage.getMessageId());
            	    appendixMessage = null;
            	}
            	if(timeLineText !=null) {
            	    messageDeleter(chatId, timeLineText.getMessageId());
            	    timeLineText = null;
            	}
                
                showPublicationQueue(chatId);
}catch(Exception e) {
	e.printStackTrace();
}

            } 
            if(userSaid.contains("returnButton_") ) {
            	urlButtonTitle = null;
            	urlButtonHref = null;
            	messageDeleter(chatId);
                try {
                	SendMessage message = new SendMessage();
                	message.setChatId(messageToReturn.getChatId().toString());
                	message.setText(messageToReturn.getText());
//                	message.setParseMode("MarkdownV2");
                	message.setReplyMarkup(messageToReturn.getReplyMarkup());
                	message.setEntities(messageToReturn.getEntities());
                	
                	execute(message);
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
            /*else*/ if(userSaid.contains("pub_later") && savedMessage != null) {
            	step = "askDate";
            	askDate(chatId);

    	    }
             if(userSaid.contains("pub_at_once") && savedMessage != null) {
            	savedMessage.setDate(LocalDate.now());
            	savedMessage.setTime(LocalTime.now().plusSeconds(5L).truncatedTo(ChronoUnit.SECONDS));

                 exceptDeletedMessage = Path.of(dir,savedMessage.getChannelName(),savedMessage.getMessageFileName());

            	editorModeOn = false;
				
            	saveMessage(chatId);
            	

    	    }
            if(userSaid.contains("rewrite_pub")) {
            	saveMessage(chatId, true);

    	    }
            if(userSaid.contains("setDate_")) {
//            	System.out.println("date is " + userSaid.substring(8));
            	checkDate(userSaid.substring(8), chatId);
            	savedMessage.setDate(messageDate);
            	step = "askTime";
            	askTime(chatId);

    	    }
            if(userSaid.contains("post_Message")) {
            	if(!editable.isEmpty()) {
            	    messageDeleter(chatId, editable);
            	    editable.clear();
            	}
            	messageDeleter(chatId);
            	
            	try {
            		changeTitleMessage(chatId);
            	    whatWeHave(chatId);
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            	setChannelSelection(chatId, "postMessage");
            }
            if(userSaid.contains("abort_Message")) {
                abortarium(chatId);
                startMessage(chatId);
            }
            if(userSaid.contains("cancel_Button")) {
            	settingsMenu(chatId);
            }
            if(userSaid.contains("add_Reactions")) {
            	step = "askReactions";
            	addMessageReactions(chatId);
    	    }
            if(userSaid.contains("add_Url")) {
            	if(!editable.isEmpty()) {
            	    messageDeleter(chatId, editable);
            	    editable.clear();
            	}
            	step = "addUrlButton";
            	if(!editorModeOn) {
                	try {
                		changeTitleMessage(chatId);
                	    whatWeHave(chatId);
                	} catch (Exception e) {
                		e.printStackTrace();
                	}
            	} else {
//            		messageEditText = update.getCallbackQuery().getMessage();
            	}
            	
            	addUrlButton(chatId);
    	    } 
            if(userSaid.contains("changeTime_")) {
            	editorModeOn = true;
            	oldFileName = LocalDateTime.of(savedMessage.getDate(), savedMessage.getTime()).toString().replace(":",".");
            	changingPostTime = true;
//            	messageEditText = update.getCallbackQuery().getMessage();
            	deleteMessage(chatId, oldFileName, savedMessage.getChannelName(), true);
            	
            	whenToPost(chatId);

    	    }
            if(userSaid.contains("rmButton")) {
            	String voterChat = chatId; 
            	Integer mId = update.getCallbackQuery().getMessage().getMessageId();
            	InlineKeyboardMarkup newInlineKeyboardMarkup = update.getCallbackQuery().getMessage().getReplyMarkup();
            	reflex(mId.toString(), voterChat, newInlineKeyboardMarkup);
            	
            }
            if(userSaid.contains("editMessage_")) { // editMessage_[0, 1]_messageToEdit

            	Boolean hasMedia = userSaid.substring(12,13).equals("0") ? false : userSaid.substring(12,13).equals("1") ? true : null;
            	Message mEd = update.getCallbackQuery().getMessage();
            	editorMode(mEd, chatId, hasMedia);

    	    }
            if(userSaid.contains("📆_")) {
            	
        		ArrayList<String> messagesQueue = (ArrayList<String>) getMessagesList();
        		try {
            		if(messagesQueue.contains(userSaid.substring(userSaid.indexOf("_")+1)))
            			showTheMessage(chatId, userSaid.substring(userSaid.indexOf("_")+1));
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
            }
            messageToReturn = update.getCallbackQuery().getMessage();
    	}

        if (update.hasMessage() && admins.contains("@" + update.getMessage().getFrom().getUserName())){
        	List<MessageEntity> mEntities = null;
            Message recivedMessage = update.getMessage();
            Integer mId = recivedMessage.getMessageId();
            chatId = recivedMessage.getChatId().toString();
            
            if (recivedMessage.hasText() && "rewriteMessageText".equals(step)) {

            	userSaid = recivedMessage.getText(); 
            	mEntities = recivedMessage.getEntities();
            	savedMessage.setText(texter(chatId, userSaid, mEntities));

            	if(messageTextEditor(chatId, messageEditText, userSaid, mEntities))
            		messageDeleter(chatId,recivedMessage.getMessageId());
            		messageDeleter(chatId);
 
//            	saveMessage(chatId, true); // rewrite

                return;
            }

            if("/start".equals(recivedMessage.getText())) {
            	startMessage(chatId);
            } 
            else if ("Очередь публикации".equals(recivedMessage.getText())) {
                messageToDeleteId.add(recivedMessage.getMessageId());
//                System.out.println("channels: " + channels);
                setChannelSelection(chatId, "showPubQueue");
//            	showPublicationQueue(chatId);
            } else if (recivedMessage.hasText()) {
            	userSaid = recivedMessage.getText(); 
            	mEntities = recivedMessage.getEntities();
/*            	System.out.println("Получено тексовое сообщение: " + System.lineSeparator() + userSaid);
            	System.out.println(step);*/
            	
            	if(savedMessage != null) {
					if("wantedMedia".equals(step)) {
						messageDeleter(chatId, update.getMessage().getMessageId());
						return;
					}
            		messageToDeleteId.add(recivedMessage.getMessageId());
            	}
            	
                if(step != null) {
                	if(step.contains("askReactions")) {

            	    	addMessageReactions(chatId, userSaid);
                	    return;
            	    }
                	if(step.contains("addUrlButton")) {
                		if(urlButtonTitle == null) {
                			urlButtonTitle = userSaid;
                		}else {
                			if(!isURL(chatId, userSaid, 0))
                			    return;
                			urlButtonHref = userSaid;
//                			System.out.println(urlButtonHref + " is checked !");
                		}
                		addUrlButton(chatId);
                	    return;
            	    }
                 	if(step.contains("askDate")) {
                 		checkDate(userSaid, chatId);
                	    savedMessage.setDate(messageDate);
                	    askTime(chatId);
                	    step = "askTime";
                	    return;
            	    }
            	    if(step.contains("askTime")) {
            	    	if(!checkTime(userSaid, chatId))
            	    		return;
            	    	
                	    savedMessage.setTime(messageTime);
                	    
                	    if(editorModeOn) {
                	    	messageDeleter(chatId);
                	        messageTextEditor(chatId, messageEditText, savedMessage.getText(), savedMessage.getMessageEntities());
                	        
                	    } else {
                	        saveMessage(chatId);
                	    }
                	    return;
            	    }
                 	if(step.contains("changeDate")) {

                	    return;
            	    }
            	    if(step.contains("changeTime")) {

                	    return;
            	    }
                }
            }
            
            if(recivedMessage.hasPhoto()) {
        		String uSaid = recivedMessage.getCaption();
        		       uSaid = uSaid == null ? "" : uSaid;
//                       System.out.println("Get photo with caption: \n" + uSaid);
        		List<MessageEntity> mediaEntities = recivedMessage.getCaptionEntities();
        		String photoId = update.getMessage().getPhoto().stream()
        				.findFirst()
        				.orElse(null)
        				.getFileId();
        		
            	if("wantedMedia".equals(step)) {
            		messageToDeleteId.add(update.getMessage().getMessageId());
            		addMedia(chatId, messageEditMedia.getMessageId(), photoId, "photo");

					if(savedMessage.getMediaFileId().size() == 1 && userSaid.length() > 984)
						warnSignCounter(chatId, false);

            	} else {
                	editable.add(recivedMessage);
            		userSaid = uSaid;
            		mEntities = mediaEntities;
            		photo_id = photoId;   
            	}
            }
            if(recivedMessage.hasVideo()) {
        		String uSaid = recivedMessage.getCaption();
        		       uSaid = uSaid == null ? "" : uSaid;
        		List<MessageEntity> mediaEntities = recivedMessage.getCaptionEntities();
        		String videoId = update.getMessage().getVideo().getFileId();
        		
            	if("wantedMedia".equals(step)) {
            		messageToDeleteId.add(update.getMessage().getMessageId());
            		addMedia(chatId, messageEditMedia.getMessageId(), videoId, "video");

					if(savedMessage.getMediaFileId().size() == 1 && userSaid.length() > 984)
						warnSignCounter(chatId, false);

            	} else {
                	editable.add(recivedMessage);
            		userSaid = uSaid;
            		mEntities = mediaEntities;
            		video_id = videoId;  
            	}
        		
            }
            if(recivedMessage.hasSticker()) {
				if("wantedMedia".equals(step)) {
					messageDeleter(chatId, update.getMessage().getMessageId());
					return;
				}
            	editable.add(recivedMessage);
        		userSaid = "";
//        		mEntities = recivedMessage.getCaptionEntities();
        		sticker_id = update.getMessage().getSticker().getFileId();
//                System.out.println("Получено сообщение с видео: "+ System.lineSeparator() + video_id + System.lineSeparator() + userSaid);
        		
            }
            if(savedMessage == null && userSaid != null) {
            	savedMessage = new SimpleMessage();
            	editable.add(recivedMessage);
            	
//            	System.out.println("Сохраняем текст: " + userSaid);

            	savedMessage.setText(texter(chatId, userSaid, mEntities));
//                System.out.println("savedMessage.getTExt(): \n" +savedMessage.getText());

            	if(photo_id != null) {
//                    System.out.println("Сохраняем фото");            		
            		savedMessage.setMediaFileId(photo_id, "photo");
					
            	}
            	if(video_id != null) {
//            		System.out.println("Сохраняем видео");
            		savedMessage.setMediaFileId(video_id, "video");
            	}
            	if(sticker_id != null) {

            		savedMessage.setMediaFileId(sticker_id, "sticker");
            	}
            	if(recivedMessage.getMediaGroupId() != null) {
 //           		System.out.println("Mediagroup " + recivedMessage.getMediaGroupId() + " has detected !");
            		savedMessage.setMediaGroupId(recivedMessage.getMediaGroupId());
            	}
            	

//                whenToPost(chatId);
/*            	if(!savedMessage.mediaGroupIdIsEmpty()) {
                	SendMessage message = new SendMessage();
                	message.setParseMode("MarkdownV2");
                	String text = "К сожалению Telegram, пока, не разрешает ботам отправлять в чаты Медиа\\-Альбомы с полноценным форматированием,"
                			      + " в целях сохранения форматирования текста сообщения медиафалы и текст будут отосланы подряд, друг за другом\\.";


                	messageToDeleteId.add(keepDialog(message, chatId, text, false).getMessageId());
            	}*/
 /*           	try {
                	if(changeTitleMessage(chatId)) {
                		messageDeleter(chatId, editable);
            		    whatWeHave(chatId);
                	}
            	} catch (Exception e) {
            		e.printStackTrace();
            	}*/
            	settingsMenu(chatId);

            }
            else if(!"wantedMedia".equals(step) && savedMessage != null && update.getMessage().getCaption() == null) {
            	// Если ID медиагруппы, полученного сообщения совпадает с сохраненным, значит идет передача медиагруппы и нужно добавить
            	if(savedMessage.getMediaGroupId() != null && savedMessage.getMediaGroupId().equals(recivedMessage.getMediaGroupId())) {
                	if(photo_id != null) {
                		savedMessage.setMediaFileId(photo_id, "photo");

                	}

                	if(video_id != null) {
                		savedMessage.setMediaFileId(video_id, "video");

                	}
                	
                	if(sticker_id != null) {
                		savedMessage.setMediaFileId(sticker_id, "sticker");

                	}

            	}

            }
        }

     }

    
    
    private boolean messageTextEditor(String chatId, Message messageEditText, String text, List<MessageEntity> mEntities) {
try {
	    EditMessageText emt = null;
	    EditMessageCaption emc = null;
	    
    	String firstStringText = null;
    	String changeTimeFirstStringText = "__Запланировано на *" + LocalDateTime.of(savedMessage.getDate(), savedMessage.getTime()).format(DateTimeFormatter.ofPattern("dd\\.MM\\.yyyy в HH:mm:ss")).toString() + "*__";

// 		    firstStringText = new Scanner(messageEditText.getText()).useDelimiter("\\R").next();
//			 System.out.println(firstStringText);

/*			 System.out.println("messageEditText.getText():\n" + messageEditText.getText());
			 System.out.println("text:\n" + text);*/

   	
    		if(changingPostTime) {
				firstStringText = changeTimeFirstStringText;
				emt = new EditMessageText();
				emt.setChatId(chatId);
				emt.setMessageId(timeLineText.getMessageId());
				emt.setParseMode("MarkdownV2");
				emt.setText(firstStringText);
//				emt.setEntities(mEntities);
//				emt.setReplyMarkup(messageEditText.getReplyMarkup());
				emt.setReplyMarkup(timeLineText.getReplyMarkup());
				changingPostTime = false;
			}    		
    		else if(messageEditText.getCaption() == null) {
				emt = new EditMessageText();
            	emt.setChatId(chatId);
            	emt.setMessageId(messageEditText.getMessageId());
            	emt.setText(text);
            	emt.setEntities(mEntities);
            	emt.setReplyMarkup(messageEditText.getReplyMarkup());

        	} else {
	            emc = new EditMessageCaption();
            	emc.setChatId(chatId);
            	emc.setMessageId(messageEditText.getMessageId());
            	emc.setCaption(text);
            	emc.setCaptionEntities(mEntities);
            	emc.setReplyMarkup(messageEditText.getReplyMarkup());
    	}
        	try {
        		if(emt != null)
        		    execute(emt);
        		if(emc != null)
        			execute(emc);
        	} catch (Exception e) {
        		    e.printStackTrace();
        		    return false;
        	}
}catch(Exception e) {
	e.printStackTrace();
}

    	return true;
    	
    }
    
    private void abortarium(String chatId) {
    	if(editable != null && !editable.isEmpty()) {
    	    messageDeleter(chatId, editable);
    	    
//    	    editable.forEach(e -> System.out.println("Editable:\n" + e.getText()));
    	    
    	    editable.clear();
    	}
    	if(wwhMessage != null && !wwhMessage.isEmpty()) {
    	    messageDeleter(chatId, wwhMessage);
    	    
//    	    wwhMessage.forEach(e -> System.out.println("What We Have:\n" + e.getText()));
    	    
    	    wwhMessage.clear();
    	}
    	if(appendixMessage != null) {
    	    messageDeleter(chatId, appendixMessage.getMessageId());
    	    appendixMessage = null;
    	}
    	if(timeLineText != null) {
    	    messageDeleter(chatId, timeLineText.getMessageId());
    	    timeLineText = null;
    	}
    	
    	messageDeleter(chatId);
    	step = null;
    	if(titleMessage != null) {
    		messageDeleter(chatId, titleMessage.getMessageId());
    	    titleMessage = null;
    	}
    	urlButtonTitle = null;
    	urlButtonHref = null;
    }
    
    public boolean isURL(String chatId, String url, int index){
        String newUrl = url;

        String[] schemes = {"http","https","tg","ftp","ftps"}; // DEFAULT schemes = "http", "https", "ftp"
        UrlValidator urlValidator = new UrlValidator(schemes);

        if(!url.startsWith(schemes[index]))
        	newUrl = schemes[index] + "://" + url;
        
        if (urlValidator.isValid(newUrl)) {
           return true;
        } else {
        	if(index == 4) {
    		    messageDeleter(chatId);
    		    SendMessage message = new SendMessage();
    		    message.setChatId(chatId);
    		    message.setParseMode("MarkdownV2");
    		    message.setText("__*URL\\-кнопка*__\nНадпись: " + escaping(urlButtonTitle) + "\nНеверная URL\\-сылка: " + url.replace(".", "\\.") + "\nПришлите правильную ссылку:");
//    		    message.setReplyMarkup(cancelButtonGenerator());
    		    
    		    try {
    		        messageToDeleteId.add(execute(message).getMessageId());
    		    } catch(Exception e) {
    		    	e.printStackTrace();
    		    }
    		    return false;
        	}
        	index++;
		    return isURL(chatId, url, index);
		    
        }
     }
    
    private void messageDeleter(String chatId) {
    	messageDeleter(chatId, null, null);
    }
    private void messageDeleter(String chatId, Integer messId) {
    	messageDeleter(chatId, messId, null);
    }
    private void messageDeleter(String chatId, List<Message> messages) {
    	messageDeleter(chatId, null, messages);
    }
    private void messageDeleter(String chatId, Integer messId, List<Message> messages) {
    	if(messages != null && !messages.isEmpty()) {
 //   		System.out.println("Delete from List");
    		for(Message message : messages) {
        		try {
            	    DeleteMessage delete = new DeleteMessage(chatId, message.getMessageId());
            	    execute(delete); 
//            	    System.out.println("Deleting message from List: " + message.getMessageId());
            	    messId = null;
        		} catch (Exception e) {
//        			System.out.println("Can't delete message from List: " + message.getMessageId());
        			//e.printStackTrace();
        		}
    		}
    		
    	}else if(messId != null) {
//    		System.out.println("Delete from variable messId");
    		try {
        	    DeleteMessage delete = new DeleteMessage(chatId, messId);
        	    execute(delete);
//        	    System.out.println("Deleting message: " + messId);

    		} catch (Exception e) {
//    			System.out.println("Can't delete message from variable messId: " + messId);
//    			e.printStackTrace();
    		} finally {
        	    messId = null;	
    		}
    		
    	} else {
        	if(!messageToDeleteId.isEmpty()) {
//        		System.out.println("Delete from messageToDeleteId set");
                Iterator<Integer> midIterator = messageToDeleteId.iterator();
        		while(midIterator.hasNext()) {
        			Integer mId = midIterator.next();
                	try {
            	    DeleteMessage delete = new DeleteMessage(chatId, mId);
            	    execute(delete);
 //           	    System.out.println("Deleting message: " + mId);
                	} catch (Exception e) {
                		
                		//e.printStackTrace();
//                		System.out.println("Can't delete message from messageToDeleteId set: " + mId);
                	}
        		}
           		messageToDeleteId.clear();

        	}
    	}
    }
    
    private void reflex(String mId, String voterChat, InlineKeyboardMarkup newInlineKeyboardMarkup) {
// Работа с сервером, отсылаем ему JSON, он уже работает с ДБ
/*        for(List<InlineKeyboardButton> row : newInlineKeyboardMarkup.getKeyboard()) {
    	    for(InlineKeyboardButton button : row) {
    		    if(userSaid.equals(button.getCallbackData())) {
    			    String[] divider = button.getText().split(" ");   // 0 - smile 1 - digit (counter)
    		    	if(divider.length > 1 ) {
    			        divider[1] = Integer.valueOf((Integer.valueOf(divider[1]) + 1)).toString();
    			        button.setText(divider[0] + " " + divider[1]);
    		    	} else {
    			        button.setText(divider[0] + " 1");            					
    			    }
    		    }
    	    }
        }

        EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
        emrm.setChatId(chatId);
        emrm.setMessageId(mId);
        emrm.setReplyMarkup(newInlineKeyboardMarkup);
        try {
        	execute(emrm);
        } catch (Exception e) {
        	e.printStackTrace();
        }*/
    }
    private void addUrlButton(String chatId) {
//    	System.out.println("editorModeOn " + editorModeOn);
    	if(!editorModeOn)
    	    messageDeleter(chatId);
    	
    	SendMessage message =  new SendMessage();
    	message.setChatId(chatId);
    	
    	if(urlButtonTitle == null) 
        	message.setText("Что будет написано на кнопке ?");
    	
        if(urlButtonTitle != null && urlButtonHref == null)
    	    message.setText("Пришлите URL ссылку для кнопки.");

        try {
        if(urlButtonTitle != null && urlButtonHref != null) {
        	String[] urlButton = new String[]{urlButtonTitle, urlButtonHref};
    		savedMessage.setUrlButton(urlButton);       	

        		if(!editorModeOn) {
            		messageDeleter(chatId, editable);
            		editable.clear();
            	    changeTitleMessage(chatId);
        		    whatWeHave(chatId);
        		    settingsMenu(chatId);
        		}
        		if(editorModeOn) {
        			messageDeleter(chatId);
        	    	InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        	    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        	    	List<List<InlineKeyboardButton>> newButtonsList = new ArrayList<>();

                if(messageEditText != null && messageEditText.getReplyMarkup() != null) {
                    rowList.addAll(messageEditText.getReplyMarkup().getKeyboard()); // try to add all message buttons
                }
        	    	
        	    	
        	    	long pasteButtonIndex = rowList.stream()
        	    		                           .flatMap(rl -> rl.stream())
        	    			                       .filter(ikb -> ikb.getUrl() != null)
        	    			                       .count();
        			
        			InlineKeyboardButton addURLButton = new InlineKeyboardButton();
        			addURLButton.setText(urlButtonTitle);
        			addURLButton.setUrl(urlButtonHref);
        			List<InlineKeyboardButton> addURLButtonRow = new ArrayList<>();
        			addURLButtonRow.add(addURLButton);
        			newButtonsList.add(addURLButtonRow);
 
        			rowList.addAll((int)pasteButtonIndex, newButtonsList);
 	
        			inlineKeyboardMarkup.setKeyboard(rowList);
        	    	
        	        EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
        	        emrm.setChatId(chatId);
        	        emrm.setMessageId(messageEditText.getMessageId());
        	        emrm.setReplyMarkup(inlineKeyboardMarkup);
        			
        	        messageEditText.setReplyMarkup(inlineKeyboardMarkup); // Try to applicate changes to editMessageText 
        	        
        	        execute(emrm);
        	        editMessageMenu(chatId);
        		}
        		
            	urlButtonTitle = null;
            	urlButtonHref = null;
				step = null;
        	} else {
        	    messageToDeleteId.add(execute(message).getMessageId());
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    }
    private void editMessageMenu(String chatId) {  // just watches all changes in whatWeHave and set up editor menu
		System.out.println("editMessageMenu");
		boolean isRowListChanged = false;		
    	InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
		InlineKeyboardMarkup inlineKeyboardMarkupEditMenu = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowListEditMenu = new ArrayList<>();
		rowListEditMenu.addAll(timeLineText.getReplyMarkup().getKeyboard());
		if(messageEditText.getReplyMarkup() != null)
    	    rowList.addAll(messageEditText.getReplyMarkup().getKeyboard()); // try to add all message buttons
//		rowList.addAll(timeLineText.getReplyMarkup().getKeyboard()) ; // try to add all message buttons

    	boolean haveUrlButtons = rowList.stream()
                                       .flatMap(rl -> rl.stream())
                                       .anyMatch(ikb -> ikb.getUrl() != null);
    	
    	
    	boolean haveDeleteUrlButton = rowListEditMenu.stream()
                                           .flatMap(rl -> rl.stream())
                                           .anyMatch(ikb -> ikb.getCallbackData() != null && ikb.getCallbackData().contains("deleteUrlButton"));

    	
    	int addUrlButtonIndex = 0;
    	
//    	for(List<InlineKeyboardButton> rows : rowList) {
		for(List<InlineKeyboardButton> rows : rowListEditMenu) {
    		boolean catchYa = rows.stream().anyMatch(ikb -> ikb.getText().contains("Добавить кнопку"));
            if(catchYa) break;
			addUrlButtonIndex++;
    	}
    	

//    	System.out.println("haveUrlButtons: " + haveUrlButtons + " deleteUrlButtonIndex: " + haveDeleteUrlButton + " addUrlButtonIndex: " + addUrlButtonIndex);

        if(!haveDeleteUrlButton && haveUrlButtons) {
			InlineKeyboardButton addDeleteURLButton = new InlineKeyboardButton();
			addDeleteURLButton.setText("\uD83D\uDEAE Удалить кнопку");
			addDeleteURLButton.setCallbackData("deleteUrlButton");
			List<InlineKeyboardButton> addDeleteURLButtonRow = new ArrayList<>();
			addDeleteURLButtonRow.add(addDeleteURLButton);
//			rowList.add((int)addUrlButtonIndex, addDeleteURLButtonRow);
			rowListEditMenu.add((int)addUrlButtonIndex, addDeleteURLButtonRow);
			isRowListChanged = true;
        }

		inlineKeyboardMarkup.setKeyboard(rowList);
		inlineKeyboardMarkupEditMenu.setKeyboard(rowListEditMenu);
    	
        EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
        emrm.setChatId(chatId);
//        emrm.setMessageId(messageEditText.getMessageId());
		emrm.setMessageId(timeLineText.getMessageId());
        emrm.setReplyMarkup(inlineKeyboardMarkupEditMenu);
        
//        messageEditText.setReplyMarkup(inlineKeyboardMarkup); //
		timeLineText.setReplyMarkup(inlineKeyboardMarkupEditMenu); //
        
    	try {
    		if(isRowListChanged) {
    		    execute(emrm);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private boolean changeTitleMessage(String chatId) {
    	try {    	
    	if(titleMessage != null) {
    		messageDeleter(chatId, titleMessage.getMessageId());
    		titleMessage = null;
    	}

    	
    	SendMessage emt = new SendMessage();
    	emt.setChatId(chatId);
    	emt.disableNotification();
    	emt.setParseMode("MarkdownV2");
    	emt.setText("То, что вы собираетесь опубликовать\\:");
    	
    	
//    	try {
//    		execute(dm);
    		titleMessage = execute(emt);
    	} catch (Exception e) {
    		e.printStackTrace();
//        	System.out.println("\n\nHere we are in changeTitleMessage: " + titleMessage.getText() + " id: " + titleMessage.getMessageId() );
    		return false;
    	}
    	
    	return true;
    }
    
    private void addMessageReactions(String chatId) {
    	addMessageReactions(chatId, null);
    }
    private void addMessageReactions(String chatId, String reactions) {
    	SendMessage message = new SendMessage();
    	if(reactions == null) {
    		message.setChatId(chatId);
    		message.setText("Пришлите \"реакции\" разделяя их \"/\" ");
//    		message.setReplyMarkup(cancelButtonGenerator());
    		try {
    			execute(message);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	} else {
    		savedMessage.setMessageReactions(reactions);
    		keepDialog(message, chatId, "Реакции \"" + reactions + "\" были добавлены к сообщению", false);
    		settingsMenu(chatId);
    	}
    }
    
    private void askDate(String chatId) {
    	
    	messageDeleter(chatId);
    	
    	InlineKeyboardMarkup inlineKeyboardMarkup =new InlineKeyboardMarkup();
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
		InlineKeyboardButton todayKeyboardButton = new InlineKeyboardButton();
		todayKeyboardButton.setText("Сегодня");
		todayKeyboardButton.setCallbackData("setDate_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.uuuu")));
		InlineKeyboardButton tomorrowKeyboardButton = new InlineKeyboardButton();
		tomorrowKeyboardButton.setText("Завтра");
		tomorrowKeyboardButton.setCallbackData("setDate_" + LocalDate.now().plusDays(1L).format(DateTimeFormatter.ofPattern("dd.MM.uuuu")));
		InlineKeyboardButton dayAfterTomorrowKeyboardButton = new InlineKeyboardButton();
		dayAfterTomorrowKeyboardButton.setText("Послезавтра");
		dayAfterTomorrowKeyboardButton.setCallbackData("setDate_" + LocalDate.now().plusDays(2L).format(DateTimeFormatter.ofPattern("dd.MM.uuuu")));
		List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
		keyboardButtonsRow.add(todayKeyboardButton);
		keyboardButtonsRow.add(tomorrowKeyboardButton);
		keyboardButtonsRow.add(dayAfterTomorrowKeyboardButton);
		rowList.add(keyboardButtonsRow);
    	inlineKeyboardMarkup.setKeyboard(rowList);

/*    	if(!savedMessage.mediaGroupIdIsEmpty()) {
        	SendMessage message = new SendMessage();
        	message.setParseMode("MarkdownV2");
        	String text = "К сожалению Telegram, пока не разрешает ботам отправлять в чаты Медиа\\-Альбомы с полноценным форматированием, ваши" 
        			      + " медиафайлы будут отправлены одним сообщением, а текст будет отправлен следом отдельным сообщением\\. Вы можете соз"
        			      + "дать сообщение и сохранить его для отложенной публикации, а, затем, перейти в очередь публикации, и выбрав его, посмотреть, "
        			      + "как оно будет выглядеть при отправке\\.";

        	keepDialog(message, chatId, text, false);
    	}*/
    	try {
        	SendMessage message = new SendMessage();
        	message.setParseMode("MarkdownV2");
        	message.setText("Выберите день публикации или укажите его в сообщении \\(дд\\.мм\\.гггг\\)");
        	message.setReplyMarkup(inlineKeyboardMarkup);
        	message.setChatId(chatId);
            messageToDeleteId.add(execute(message).getMessageId());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    private void startMessage(String chatId) {
    	savedMessage = null;

        channelName = null;
        step = null;
        messageDate = null;
        messageTime = null;
        messageEditText = null;
        timeLineText = null;

        if(editorModeOn) {
            messageDeleter(chatId);
            editorModeOn = false;
            changingPostTime = false;
        }

//    	System.out.println("startMessage: " + savedMessage);
//    	messageDeleter(chatId);
    	SendMessage message = new SendMessage();
        message.setChatId(chatId);
        String text = "\n\nОтправьте боту то, что вы хотите опубликовать";
		titleMessage = keepDialog(message, chatId, text, false);
//		System.out.println("keepDialog returns: " + titleMessage.getText());
    }
    private InlineKeyboardMarkup cancelButtonGenerator() {
    	InlineKeyboardMarkup inlineKeyboardMarkup =new InlineKeyboardMarkup();
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
		InlineKeyboardButton cancelButton = new InlineKeyboardButton();
		cancelButton.setText("↩️ Вернуться");
		cancelButton.setCallbackData("returnButton_");
		List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
		keyboardButtonsRow.add(cancelButton);
		rowList.add(keyboardButtonsRow);
		inlineKeyboardMarkup.setKeyboard(rowList);
		return inlineKeyboardMarkup;
    }
    private void settingsMenu(String chatId) {

    	try {
    	InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
/*		InlineKeyboardButton addReactionButton = new InlineKeyboardButton();
		addReactionButton.setText("👍👎 Добавить реакции ?");
		addReactionButton.setCallbackData("add_Reactions");*/
		InlineKeyboardButton urlButton = new InlineKeyboardButton();
		urlButton.setText("🌐 Добавить кнопку с URL");
		urlButton.setCallbackData("add_Url");
		InlineKeyboardButton continueButton = new InlineKeyboardButton();
		continueButton.setText("⏩ Просто продолжить");
		continueButton.setCallbackData("post_Message");
		InlineKeyboardButton abortButton = new InlineKeyboardButton();
		abortButton.setText("⛔️ Отменить публикацию");
		abortButton.setCallbackData("abort_Message");
/*		List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
		keyboardButtonsRow1.add(addReactionButton);*/
		List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
		keyboardButtonsRow2.add(urlButton);
		List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();
		keyboardButtonsRow3.add(continueButton);
		List<InlineKeyboardButton> keyboardButtonsRow4 = new ArrayList<>();
		keyboardButtonsRow4.add(abortButton);
//		rowList.add(keyboardButtonsRow1);
		rowList.add(keyboardButtonsRow2);
		rowList.add(keyboardButtonsRow3);
		rowList.add(keyboardButtonsRow4);
    	inlineKeyboardMarkup.setKeyboard(rowList);   
    	
        	SendMessage message = new SendMessage();
        	message.setParseMode("MarkdownV2");
        	message.setText("Дополнительные настройки публикации: ");
        	message.setReplyMarkup(inlineKeyboardMarkup);
        	message.setChatId(chatId);
        	messageToDeleteId.add(execute(message).getMessageId());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    private void whenToPost(String chatId) {
    	if(savedMessage == null)
    		return;

        messageDeleter(chatId);

    	InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
		InlineKeyboardButton atOnceKeyboardButton = new InlineKeyboardButton();
		atOnceKeyboardButton.setText("🔥 Опубликовать сейчас");
		atOnceKeyboardButton.setCallbackData("pub_at_once");
		InlineKeyboardButton laterKeyboardButton = new InlineKeyboardButton();
		laterKeyboardButton.setText("⏰ Опубликовать позднее");
		laterKeyboardButton.setCallbackData("pub_later");
		List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
		keyboardButtonsRow1.add(atOnceKeyboardButton);
		List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
		keyboardButtonsRow2.add(laterKeyboardButton);
		rowList.add(keyboardButtonsRow1);
		rowList.add(keyboardButtonsRow2);
    	inlineKeyboardMarkup.setKeyboard(rowList);
    	try {
        	SendMessage message = new SendMessage();
        	message.setParseMode("MarkdownV2");
        	message.setText("Вы можете опубликовать сообщение прямо сейчас или установить дату и время публикации");
        	message.setReplyMarkup(inlineKeyboardMarkup);
        	message.setChatId(chatId);
        	
        	messageToDeleteId.add(execute(message).getMessageId());
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private void askTime(String chatId) {
    	messageDeleter(chatId);
    	try {
        	SendMessage message = new SendMessage();
        	message.setParseMode("MarkdownV2");
        	message.setText("Выбрана дата " + messageDate.format(DateTimeFormatter.ofPattern("dd.MM.uuuu")).replace(".", "\\.") + "\nТеперь укажите время публикации в формате *ЧЧ:ММ:СС* или *ЧЧ:ММ*");
        	message.setChatId(chatId);
        	messageToDeleteId.add(execute(message).getMessageId());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    private boolean checkDate(String date, String chatId) {
    	try {
    	    messageDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.uuuu"));
    	} catch (DateTimeParseException dtpe) {
        	SendMessage message = new SendMessage();
            message.setChatId(chatId);
            String text = "Дата дожна быть в формате \n *ДД\\.ММ\\.ГГГГ*";
            messageToDeleteId.add(keepDialog(message, chatId, text, false).getMessageId());
    		return false;
    	}
	
    	return true;
    }
    private boolean checkTime(String time, String chatId) {
    	try {
    		if(time.contains(".")) {
    			int divCount = time.split("\\.").length;
    			if(divCount == 3)
    	            messageTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH.mm.ss"));
    			if(divCount == 2)
    	            messageTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH.mm"));    			
    		} else if(time.contains(":")) {
    			int divCount = time.split(":").length;
    			if(divCount == 3)
    	            messageTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss"));
    			if(divCount == 2)
    	            messageTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
    		} else {
    			throw new DateTimeParseException("Wrong Time Format", time, 0);
    		}
    		
//    		System.out.println("message time: " + messageTime);
    	} catch (DateTimeParseException dtpe) {
        	SendMessage message = new SendMessage();
            message.setChatId(chatId);
            String text = "Формат времени может быть \n *ЧЧ:ММ:СС* \n *ЧЧ:ММ* \n *ЧЧ\\.ММ\\.СС* \n *ЧЧ\\.ММ*";
            messageToDeleteId.add(keepDialog(message, chatId, text, false).getMessageId());
    		return false;
    	}
    	return true;
    }
    private void saveMessage(String chatId) {
    	saveMessage(chatId, false);
    }
    private void saveMessage(String chatId, boolean rewriteSavedMessage) {

    	LocalDateTime messageFileName = LocalDateTime.of(savedMessage.getDate(), savedMessage.getTime());
    	SendMessage message = new SendMessage();  
    	File mFile = new File(dir + channelName + File.separator + messageFileName.toString().replace(":", "."));
    	
    	if(mFile.exists() && !rewriteSavedMessage) {
        	InlineKeyboardMarkup inlineKeyboardMarkup =new InlineKeyboardMarkup();
        	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
    		InlineKeyboardButton rewriteButton = new InlineKeyboardButton();
    		rewriteButton.setText("🔥 Заменить");
    		rewriteButton.setCallbackData("rewrite_pub");
    		InlineKeyboardButton laterKeyboardButton = new InlineKeyboardButton();
    		laterKeyboardButton.setText("⏰ Изменить время");
    		laterKeyboardButton.setCallbackData("pub_later");
    		List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
    		keyboardButtonsRow1.add(rewriteButton);
//    		List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
    		keyboardButtonsRow1.add(laterKeyboardButton);
    		rowList.add(keyboardButtonsRow1);
//    		rowList.add(keyboardButtonsRow2);
        	inlineKeyboardMarkup.setKeyboard(rowList);
        	message.setParseMode("MarkdownV2");
    		message.setChatId(chatId);
    		message.setText("__*На эту дату и время уже запланирована другая публикация*__\\.\nВы можете заменить старую публикацию новой, или изменить время новой публикации:");
    		message.setReplyMarkup(inlineKeyboardMarkup);
    		try {
    			messageToDeleteId.add(execute(message).getMessageId());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		return;
    	}
    	String answerText = "";
    	if(mFile.exists() && rewriteSavedMessage && !editorModeOn) { // NOT EDITOR MODE
    		
    		answerText = "Ok\nСообщение будет заменено на новое и будет опубликовано в канале *" + savedMessage.getChannelName().replace("/","") + "* \n" + "*" + messageFileName.toLocalDate().format(DateTimeFormatter.ofPattern("dd\\.MM\\.uuuu"))
    			    + " в " + messageFileName.toLocalTime() + "*";
    		
 //   		System.out.println(answerText);
    		
    		abortarium(chatId);

    		messageToDeleteId.add(keepDialog(message, chatId, answerText,false).getMessageId());

    	}
    	if(!mFile.exists() && !editorModeOn) {
    		
    		answerText = "Ok\nСообщение будет опубликовано\nв канале: *" + savedMessage.getChannelName().replace("/","") + "* \n" + "*" + messageFileName.toLocalDate().format(DateTimeFormatter.ofPattern("dd\\.MM\\.uuuu"))
    			    + " в " + messageFileName.toLocalTime() + "*";
    		
 //   		System.out.println(answerText);
    		
    		abortarium(chatId);
    		messageToDeleteId.add(keepDialog(message, chatId, answerText,false).getMessageId());

    	}

    	try(FileWriter fwr = new FileWriter(new File(dir + savedMessage.getChannelName() + messageFileName.toString().replace(":", ".")), StandardCharsets.UTF_8)) {
        	fwr.append(savedMessage.toString());
        	fwr.flush();

    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {

            channelName = null;
    	    step = null;
    	    messageDate = null;
    	    messageTime = null;
            messageEditText = null;
            timeLineText = null;
    	    messageToEdit = messageFileName.toString().replace(":", ".");
    	    
        	if(editorModeOn) {
        		messageDeleter(chatId);
                editorModeOn = false;
                changingPostTime = false;
        	} else {
    	    	startMessage(chatId);
        	}

    	}
    }
    
    public void editorMode(Message mEd, String chatId,/* String messageToEdit,*/ boolean hasMedia) {
    	editorModeOn = true;
//    	messageEditText = mEd;
    	
    	InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
    	List<List<InlineKeyboardButton>> newButtonsList = new ArrayList<>();
//    	rowList.addAll(messageEditText.getReplyMarkup().getKeyboard()) ; // try to add all message buttons
//		rowList.addAll(mEd.getReplyMarkup().getKeyboard()) ; // try to add all message buttons
        rowList.addAll(timeLineText.getReplyMarkup().getKeyboard()) ; // try to add all message buttons

		InlineKeyboardButton addMediaButton = new InlineKeyboardButton();
		addMediaButton.setText("🖼 Добавить медиа");
		addMediaButton.setCallbackData("addMedia_"+messageToEdit);
		List<InlineKeyboardButton> addMediaButtonRow = new ArrayList<>();
		addMediaButtonRow.add(addMediaButton);
		newButtonsList.add(addMediaButtonRow);
	    	
		if(hasMedia) {
	    	
			InlineKeyboardButton clearMediaButton = new InlineKeyboardButton();
			clearMediaButton.setText("🖼 Очистить медиа");
			clearMediaButton.setCallbackData("clearMedia_"+messageToEdit);
			List<InlineKeyboardButton> clearMediaButtonRow = new ArrayList<>();
			clearMediaButtonRow.add(clearMediaButton);
			newButtonsList.add(clearMediaButtonRow);
		}
	
		InlineKeyboardButton addButtonMessageButton = new InlineKeyboardButton();
		addButtonMessageButton.setText("\uD83C\uDD95 Добавить кнопку");
		addButtonMessageButton.setCallbackData("add_Url"+messageToEdit);
		List<InlineKeyboardButton> addButtonMessageButtonRow = new ArrayList<>();
		addButtonMessageButtonRow.add(addButtonMessageButton);
		newButtonsList.add(addButtonMessageButtonRow);
		
		InlineKeyboardButton rewriteMessageButton = new InlineKeyboardButton();
		rewriteMessageButton.setText("✏️ Редактировать текст");
		rewriteMessageButton.setCallbackData("rewriteMessage_"+messageToEdit);
		List<InlineKeyboardButton> rewriteMessageButtonRow = new ArrayList<>();
		rewriteMessageButtonRow.add(rewriteMessageButton);
		newButtonsList.add(rewriteMessageButtonRow);
		
		InlineKeyboardButton changeTimeButton = new InlineKeyboardButton();
		changeTimeButton.setText("⏰ Изменить время");
		changeTimeButton.setCallbackData("changeTime_"+messageToEdit);
		List<InlineKeyboardButton> changeTimeButtonRow = new ArrayList<>();
		changeTimeButtonRow.add(changeTimeButton);
		newButtonsList.add(changeTimeButtonRow);
    	
		InlineKeyboardButton saveButton = new InlineKeyboardButton();
		saveButton.setText("✅ Сохранить");
		saveButton.setCallbackData("saveEdited_" + messageToEdit);
		List<InlineKeyboardButton> saveButtonRow = new ArrayList<>();
		saveButtonRow.add(saveButton);
		newButtonsList.add(saveButtonRow);
		
		rowList.addAll(rowList.size() - 2 , newButtonsList); 
		rowList.remove(rowList.size() - 2);
		inlineKeyboardMarkup.setKeyboard(rowList);
    	
//		System.out.println("\n\n\n\n\n" + mEd.getText() + "\n\nKeyboard size: " + mEd.getReplyMarkup().getKeyboard().size());
		
      EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
        emrm.setChatId(chatId);
        emrm.setMessageId(mEd.getMessageId());
        emrm.setReplyMarkup(inlineKeyboardMarkup);
        
//      messageEditText.setReplyMarkup(inlineKeyboardMarkup); // Try to applicate changes to editMessageText
		timeLineText.setReplyMarkup(inlineKeyboardMarkup); // Try to applicate changes to editMessageText
		
		
/*		SendMessage emrm = new SendMessage();
		emrm.setChatId(chatId);
        emrm.setReplyMarkup(inlineKeyboardMarkup);
        emrm.setText(timeLineText.getText());
        emrm.setEntities(timeLineText.getEntities());*/
        
		
        try {
        	execute(emrm);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        editMessageMenu(chatId);
    }
    
    public Message showTheMessage(String chatId, String message2show) throws Exception {
        System.out.println("ShowTheMessage method");
		Scanner scannedMessage = new Scanner("");
    	Message resultMessage = null;
//		System.out.println("message2show: -> " + message2show);
		if(message2show.startsWith("/**************")) {
			scannedMessage = new Scanner(message2show).useDelimiter("\\R");
//			System.out.println(message2show);
		} else {
	        scannedMessage = new Scanner(new File(dir + channelName + File.separator + message2show), "UTF-8").useDelimiter("\\R");
	    	editorModeOn = true;
	    	messageDeleter(chatId);
	    	messageToEdit = message2show;
	    	savedMessage = SimpleMessage.restore(dir + channelName + File.separator, message2show);  // Restore publication from file to savedMessage variable
		}
		message2show = savedMessage.getMessageFileName();
    	int photosAllTogather = 0;
    	int videosAllTogather = 0;
    	Integer hasMedia = 0;
    	List<String> photoList = new ArrayList<>();
    	List<String> videoList = new ArrayList<>();
    	InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
		InlineKeyboardMarkup inlineKeyboardMarkupButtons = new InlineKeyboardMarkup();
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
		List<List<InlineKeyboardButton>> inlineKeyboardMarkupButtonsRowList = new ArrayList<>();
   	
		StringBuilder text2send = new StringBuilder();
//		text2send.append("__Запланировано на *" + LocalDateTime.parse(savedMessage.getMessageFileName().replace(".", ":")).format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm:ss")).toString().replace(".", "\\.") + "*__" + System.lineSeparator()
//		+ System.lineSeparator());
		String photo_id = "";
		String stickerId = null;
		
		String reflections = "";
		String urlButtonTitle = "";
		String urlButtonHref = "";
		while(scannedMessage.hasNext()) {
			String nextStr = scannedMessage.next();
			if(!nextStr.startsWith("/*") && !nextStr.endsWith("*/")) {
                if(nextStr.contains("toChannel ="))
                    channelName = nextStr.substring(11);
                
				else if(nextStr.contains("photo ="))
					photoList.add(nextStr.substring(8));
			    			
				else if(nextStr.contains("video =")) 
					videoList.add(nextStr.substring(8));
				
				else if(nextStr.contains("sticker =")) 
					stickerId = nextStr.substring(10);
				
				else if(nextStr.contains("reactions =")) {
					List<InlineKeyboardButton> reflactionButtonRow = new ArrayList<>();
					for(String reflection : nextStr.substring(11).trim().split("/")) {
						InlineKeyboardButton reflectionButton = new InlineKeyboardButton();
						reflectionButton.setText(reflection);
						reflectionButton.setCallbackData("smile");
						reflactionButtonRow.add(reflectionButton);
					}
//					rowList.add(reflactionButtonRow);
					inlineKeyboardMarkupButtonsRowList.add(reflactionButtonRow);
				}
				
				else if(nextStr.contains("ButtonTitle =")) {
					urlButtonTitle = nextStr.substring(13).trim();
					urlButtonHref = scannedMessage.next().substring(11).trim();
					
//					System.out.println("urlButtonTitle: " + urlButtonTitle + "\nurlButtonHref: " + urlButtonHref);
					
					InlineKeyboardButton urlButton = new InlineKeyboardButton();
					urlButton.setText(urlButtonTitle);
					urlButton.setUrl(urlButtonHref);
					List<InlineKeyboardButton> urlButtonRow = new ArrayList<>();
					urlButtonRow.add(urlButton);
//					rowList.add(urlButtonRow);
					inlineKeyboardMarkupButtonsRowList.add(urlButtonRow);
				
				}
				
				else
		    	    text2send.append(nextStr + System.lineSeparator());
			}
		}
		inlineKeyboardMarkupButtons.setKeyboard(inlineKeyboardMarkupButtonsRowList);
		
//		System.out.println(text2send.toString());
		hasMedia = (photoList.isEmpty() && videoList.isEmpty() && stickerId == null) ? 0 : 1;

		InlineKeyboardButton returnButton = new InlineKeyboardButton();
		returnButton.setText("↩️ Возврат");
//		returnButton.setCallbackData("returnButton_");
		returnButton.setCallbackData("show_pub_queue");
		List<InlineKeyboardButton> returnButtonRow = new ArrayList<>();
		returnButtonRow.add(returnButton);
		rowList.add(returnButtonRow);
    	inlineKeyboardMarkup.setKeyboard(rowList);
    	
		InlineKeyboardButton editMessageButton = new InlineKeyboardButton();
		editMessageButton.setText("✏️ Редактировать");
		editMessageButton.setCallbackData("editMessage_" + hasMedia.toString() + "_" + message2show);
		List<InlineKeyboardButton> editMessageButtonRow = new ArrayList<>();
		editMessageButtonRow.add(editMessageButton);
		rowList.add(editMessageButtonRow);
    	inlineKeyboardMarkup.setKeyboard(rowList);
/*		
		InlineKeyboardButton changeTimeButton = new InlineKeyboardButton();
		changeTimeButton.setText("⏰ Изменить время");
		changeTimeButton.setCallbackData("changeTime_"+message2show);
		List<InlineKeyboardButton> changeTimeButtonRow = new ArrayList<>();
		changeTimeButtonRow.add(changeTimeButton);
		rowList.add(changeTimeButtonRow);
    	inlineKeyboardMarkup.setKeyboard(rowList);*/
    	
		InlineKeyboardButton deleteButton = new InlineKeyboardButton();
		deleteButton.setText("🗑 Удалить");
		deleteButton.setCallbackData("delete_" + message2show);
		List<InlineKeyboardButton> deleteButtonRow = new ArrayList<>();
		deleteButtonRow.add(deleteButton);
		rowList.add(deleteButtonRow);
    	inlineKeyboardMarkup.setKeyboard(rowList);
		
		photosAllTogather = photoList.size();
		videosAllTogather = videoList.size();
		
		if(stickerId != null) {
        	SendSticker message = new SendSticker();
            message.setChatId(chatId);
//        	message.setReplyMarkup(inlineKeyboardMarkup);
			message.setReplyMarkup(inlineKeyboardMarkupButtons);
        	message.setSticker(new InputFile(stickerId));
        	resultMessage = execute(message);
        	wwhMessage.add(resultMessage);
			messageEditText = resultMessage;
			return resultMessage;
		}
		
        if((photosAllTogather + videosAllTogather) == 0) {
        	SendMessage message = new SendMessage();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
            message.setText(text2send.toString());
//        	message.setReplyMarkup(inlineKeyboardMarkup);
			message.setReplyMarkup(inlineKeyboardMarkupButtons);
        	resultMessage = execute(message);
        	wwhMessage.add(resultMessage);


        } else if(photosAllTogather < 2 && videosAllTogather == 0){
    		SendPhoto message = new SendPhoto();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
    	    message.setPhoto(new InputFile(photoList.get(0)));
            message.setCaption(text2send.toString());
//        	message.setReplyMarkup(inlineKeyboardMarkup);
			message.setReplyMarkup(inlineKeyboardMarkupButtons);
        	resultMessage = execute(message);
        	wwhMessage.add(resultMessage);

        	
        } else if(videosAllTogather < 2 && photosAllTogather == 0){
    		SendVideo message = new SendVideo();
        	message.setParseMode("MarkdownV2");
            message.setChatId(chatId);
    	    message.setVideo(new InputFile(videoList.get(0)));
            message.setCaption(text2send.toString());
//        	message.setReplyMarkup(inlineKeyboardMarkup);
			message.setReplyMarkup(inlineKeyboardMarkupButtons);
        	resultMessage = execute(message);
        	wwhMessage.add(resultMessage);
        	
        } else if ((photosAllTogather + videosAllTogather) > 1) {

        	List<InputMedia> medias = new ArrayList<>();
        	boolean weHaveCaption = false;
        	for(int i = 0; i < photoList.size(); i++) {
            	InputMedia media = (InputMedia) new InputMediaPhoto();
/*				if(i == 0 && inlineKeyboardMarkupButtonsRowList.isEmpty()) {
					media.setCaption(text2send.toString());
					media.setParseMode("MarkdownV2");
					weHaveCaption = true;
				}*/
            	media.setMedia(photoList.get(i));
            	medias.add(media);
        	}
        	for(int i = 0; i < videoList.size(); i++) {
            	InputMedia media = (InputMedia) new InputMediaVideo();
/*				if(i == 0 && !weHaveCaption && inlineKeyboardMarkupButtonsRowList.isEmpty()) {
					media.setCaption(text2send.toString());
					media.setParseMode("MarkdownV2");
				}*/
            	media.setMedia(videoList.get(i));
            	medias.add(media);
        	}
        	
            SendMediaGroup message = new SendMediaGroup();
            message.setChatId(chatId);
            message.setMedias(medias);

            
            wwhMessage = execute(message);

//			if(inlineKeyboardMarkupButtons.getKeyboard().size() > 0) {
				SendMessage apendix = new SendMessage();
				apendix.setParseMode("MarkdownV2");
				apendix.setChatId(chatId);
				apendix.setText(text2send.toString());
				apendix.setReplyMarkup(inlineKeyboardMarkupButtons);
				appendixMessage = execute(apendix);
				resultMessage = appendixMessage;
//				messageEditText = resultMessage;

			//}
        }

		SendMessage tlm = new SendMessage();
		tlm.setParseMode("MarkdownV2");
		tlm.setChatId(chatId);
		tlm.setText("__Запланировано на *" + LocalDateTime.parse(savedMessage.getMessageFileName().replace(".", ":")).format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm:ss")).toString().replace(".", "\\.") + "*__");
		tlm.setReplyMarkup(inlineKeyboardMarkup);
//		appendixMessage = execute(apendix);
//		timeLineText = appendixMessage;
		timeLineText = execute(tlm);

		messageEditText = resultMessage;
        return resultMessage;
    }
    
    public List<String> getMessagesList() {

    	List<String> mNames = new ArrayList<>();
    	try(Stream<Path> paths = Files.walk(Paths.get(dir + channelName))){
    		if(exceptDeletedMessage == null) {
        		mNames = paths.filter(Files::isRegularFile)
						 .filter(file -> file.getFileName().toString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}\\.\\d{2}(\\.\\d{2})*"))
  				         .collect(ArrayList::new, (al, p) -> al.add(p.getFileName().toString()), ArrayList::addAll);
    		} else {
        		mNames = paths.filter(Files::isRegularFile)
						.filter(file -> file.getFileName().toString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}\\.\\d{2}(\\.\\d{2})*"))
        				.filter(p -> p.compareTo(exceptDeletedMessage) != 0)
						.collect(ArrayList::new, (al, p) -> al.add(p.getFileName().toString()), ArrayList::addAll);
    		}
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	return mNames;
    }

    public void showPublicationQueue(String chatId) {
    	savedMessage = null;
//    	System.out.println("showPublicationQueue: " + savedMessage);
    	if(titleMessage != null)
    	    messageToDeleteId.add(titleMessage.getMessageId());
    	
		messageDeleter(chatId);
    	SendMessage message = new SendMessage();
    	InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    	ArrayList<String> mNames = (ArrayList<String>) getMessagesList(); 
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
    	List<InlineKeyboardButton> abortButtonsRow = new ArrayList<>();
        List<InlineKeyboardButton> returnButtonsRow = new ArrayList<>();


    	for(String mname : mNames) {
    		 InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
    		 inlineKeyboardButton.setText("📆 " + LocalDateTime.parse(mname.replace(".", ":")).format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm:ss")));
    		 inlineKeyboardButton.setCallbackData("📆_" + mname);
    		 List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
    		 keyboardButtonsRow.add(inlineKeyboardButton);
    		 rowList.add(keyboardButtonsRow);
    	}
    	
		InlineKeyboardButton abortButton = new InlineKeyboardButton();
		abortButton.setText("📝 Создать новую публикацию ?");
		abortButton.setCallbackData("abort_Message");
		abortButtonsRow.add(abortButton);
		rowList.add(abortButtonsRow);
        InlineKeyboardButton returnButton = new InlineKeyboardButton();
        returnButton.setText("↩️ Выбор канала");
        returnButton.setCallbackData("c_choice");
        returnButtonsRow.add(returnButton);
        rowList.add(returnButtonsRow);
		
    	inlineKeyboardMarkup.setKeyboard(rowList);
    	message.setReplyMarkup(inlineKeyboardMarkup);
    	message.setChatId(chatId);
    	message.setText(mNames.isEmpty() ? "Очередь публикаций пуста !" : "Очередь публикации :");
    	
    	try {
    		int mesId = execute(message).getMessageId();
    		messageToDeleteId.add(mesId);
//    		System.out.println("Очередь публикации ID: " + mesId);

    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
 //           channelName = null;
        }
    }
    private void deleteMessage(String chatId, String message2delete, String cName, boolean silence) {
//    	new File(dir + cName + File.separator + message2delete).delete();
    	new File(dir + cName + File.separator + message2delete).delete();
    	if(!silence) {
    		String whatMessage = LocalDateTime.parse(message2delete.replace(".", ":")).format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm:ss")).toString().replace(".", "\\.");
    	    keepDialog(new SendMessage(), chatId, "Сообщение,\nзапланированное " + whatMessage + ",\nне будет опубликовано \\- оно удалено \\!", true);
//    	    startMessage(chatId);
    	}
    	
    }

	private void warnSignCounter(String chatId, boolean caption) {
		if(caption)
			messageToDeleteId.add(keepDialog(new SendMessage(), chatId, "Длинна текста под медиа *не должна превышать 984 символов* \\!\nСообщение будет уменьшено до этого значения\n" +
					"Вы можете отложить публикацию и воспользоваться режимом редактирования для изменения текста сообщения",true).getMessageId());
		else
			messageToDeleteId.add(keepDialog(new SendMessage(), chatId, "Длинна текста для медиагруппы или обычного тектового сообщения *не должна превышать 4056 символов* \\!\nСообщение будет уменьшено до этого значения\n" +
					"Вы можете отложить публикацию и воспользоваться режимом редактирования для изменения текста сообщения",true).getMessageId());

	}

    // userSaid passing through escapes method becomes larger. It's new length must be consider in MessageEntity list 
    private String texter(String chatId, String userSaid, List<MessageEntity> entities){
    	// Special class Slasher used for tracking down warning symbols  (e.g #.-+*|{}[] etc)
    	// it has two methods
    	// One is getSlashOffsets() wich returns TreeSet containing offsets of those symbols
    	// Another one is getModifiedString() returning slash-modified userSaid string

		if(savedMessage.mediaIsEmpty() && userSaid.length() > 4056)
			warnSignCounter(chatId, false);
/*		if(savedMessage.getMediaFileId().size() > 1 && userSaid.length() > 4056)
			warnSignCounter(chatId, false);*/


//		System.out.println("mediasize: " + savedMessage.getMediaFileId().size() + " usersaid length: " +  userSaid.length());

    	if(entities == null)
    		entities = new ArrayList<>();
    	Slasher sc = new Slasher(userSaid);
    	List<MessageEntity> slashOffsets = sc.getSlashOffsets();
    	entities.addAll(slashOffsets); // Combaine entities and slashes
/*    	int countNewLength = entities.stream()
    			                     .mapToInt(me -> me.getLength())
    			                     .sum();
    	     countNewLength += userSaid.length();
    	     
    	for(int i = 0; i < ( countNewLength - userSaid.length()); i++ )
    		userSaid +=" ";*/
    	
/*    	entities .forEach(me -> {
    		System.out.println(me.getType() + " offset: " + me.getOffset());
    	});*/
//    	System.out.println(sc.getModifiedString());
    	
    	
    	TreeMap<Integer, String> offsets = null;
//    	StringBuilder usb = new StringBuilder(sc.getModifiedString());
    	StringBuilder usb = new StringBuilder(userSaid);
    	
    	
    	Map<String, String[]> simpleEn = new HashMap<>();
    	simpleEn.put("bold", new String[]{"*","*"});
    	simpleEn.put("italic", new String[]{"_","_"});
    	simpleEn.put("code", new String[]{"```","```"});
    	simpleEn.put("pre", new String[]{"`","`"});
    	simpleEn.put("text_link", new String[]{"[","]"});
    	simpleEn.put("underline", new String[]{"__", "__"});
    	simpleEn.put("strikethrough", new String[]{"~", "~"});
    	simpleEn.put("slash", new String[]{"\\", ""});      // Slashes          

    	if( entities != null ) {
            //  Collect TreeMap with offsets mapped to entities
    		//  if TreeMap have contained allready offset we just summ entities
    		offsets = entities.stream()
//    				  .peek(System.out::println)
    				  .filter(en -> simpleEn.containsKey(en.getType()))
    				  .collect(TreeMap::new, 
    						  (tm, en) -> { 

    							 
    							 int rightOffset = en.getOffset();
    							 int rigthLength = en.getLength();
    							 
/*    							 int stepCounter = 0;
    							 Iterator<Integer> iterOff = slashOffsets.iterator();
    							 while(iterOff.hasNext()) {
    								 Integer slashOffset = iterOff.next();
    								 if(en.getOffset() > slashOffset) 
    									 stepCounter++;
    								 
    							 }
								 rightOffset = en.getOffset() + stepCounter;

    							 
    							 stepCounter = 0;
    							 Iterator<Integer> iterLen = slashOffsets.iterator();
    							 while(iterLen.hasNext()) {
    								 Integer slashOffset = iterLen.next();
    								 if(slashOffset > rightOffset && (rightOffset + en.getLength()) > slashOffset)
									     stepCounter++;
    								 
    							 }
								 rigthLength = en.getLength() + stepCounter;*/

    							 
     	    					 tm.computeIfPresent(rightOffset,  (k, v) -> 
     	    					 {
     	    					     return v.contains("_") && simpleEn.get(en.getType())[0].contains("_")
     	    					    		                ? v + (new Character('\u00AD').toString()) + simpleEn.get(en.getType())[0]
     	    					    		                : v  + simpleEn.get(en.getType())[0]
     	    					 ;});
     	    					 
    	    					 tm.putIfAbsent(rightOffset, simpleEn.get(en.getType())[0]);
    	    					 
    	    					 if("text_link".equals(en.getType())) {                          // text_link handler
         	    					 tm.computeIfPresent(rightOffset + rigthLength, (k, v) -> 
         	    					 {
         	    					     return simpleEn.get(en.getType())[1] + "(" + en.getUrl() + ")" + v
         	    					 ;});
        	    					 tm.putIfAbsent(rightOffset + rigthLength, simpleEn.get(en.getType())[1]  + "(" + en.getUrl() + ")");

    	    					 } else {
         	    					 tm.computeIfPresent(rightOffset + rigthLength, (k, v) -> 
         	    					 {
         	    					     return v.contains("_") && simpleEn.get(en.getType())[1].contains("_")
         	    					    		                ? simpleEn.get(en.getType())[1] + (new Character('\u00AD').toString()) + v
         	    					    		                : simpleEn.get(en.getType())[1] + v
         	    					 ;});
        	    					 tm.putIfAbsent(rightOffset + rigthLength, simpleEn.get(en.getType())[1]);
    	    					 }
    	    				  },
    						  TreeMap::putAll
    				  );
    		
    		
    		int plusSize = 0;
    		
    		for(Map.Entry<Integer, String> offEntry: offsets.entrySet()) {
    			usb.insert(offEntry.getKey() + plusSize, offEntry.getValue());
    		    plusSize += offEntry.getValue().length();
//    		    System.out.println(usb);
    		}
    				
    	}/* else {
    		System.out.println("No entities");
    	}
    	System.out.println("NoMoreEntities OUT \n" + noMoreEntities(usb.toString()));*/

		return usb.toString();

    }

	private String escaping(String str) {
		byte esc = 92;
		char escape = (char) esc;
    	return 	str.replace("\\", "\\\\")
 			   .replace(".", "\\.")
	           .replace("#", "\\#")
	           .replace("!", "\\!")
	           .replace("{", "\\{")
	           .replace("}", "\\}")
	           .replace("=", "\\=")
	           .replace("|", "\\|")
	           .replace("-", "\\-")
               .replace("(", "\\(")
               .replace("[", "\\[")
               .replace(")", "\\)")
               .replace("]", "\\]")
               .replace("~", "\\~")
               .replace("`", "\\`")
               .replace(">", "\\>")
               .replace("+", "\\+")
               .replace("*", "\\*")
    	           ;
	}
    
	private String escapes(String str) {
		byte esc = 92;
		char escape = (char) esc;
    	return 	str.replace("\\", "\\\\")
 			   .replace(".", "\\\\.")
	           .replace("#", "\\\\#")
	           .replace("!", "\\\\!")
	           .replace("{", "\\\\{")
	           .replace("}", "\\\\}")
	           .replace("=", "\\\\=")
	           .replace("|", "\\\\|")
	           .replace("-", "\\\\-")
               .replace("(", "\\\\(")
               .replace("[", "\\\\[")
               .replace(")", "\\\\)")
               .replace("]", "\\\\]")
               .replace("~", "\\\\~")
               .replace("`", "\\\\`")
               .replace(">", "\\\\>")
               .replace("+", "\\\\+")
               .replace("*", "\\\\*")
    	           ;
	}
	private String noMoreEntities(String str){	
    	return 	str.replaceAll("(?<!\\\\)\\.", "\\\\\\\\\\.")
  			   .replaceAll("(?<!\\\\)\\#", "\\\\\\\\\\#")
  			   .replaceAll("(?<!\\\\)\\!", "\\\\\\\\\\!")
  			   .replaceAll("(?<!\\\\)\\{", "\\\\\\\\\\{")
  			   .replaceAll("(?<!\\\\)\\}", "\\\\\\\\\\}")
  			   .replaceAll("(?<!\\\\)\\=", "\\\\\\\\\\=")
  			   .replaceAll("(?<!\\\\)\\|", "\\\\\\\\\\|")
  			   .replaceAll("(?<!\\\\)\\-", "\\\\\\\\\\-")
  			   .replaceAll("(?<!\\\\)\\(", "\\\\\\\\\\(")
  			   .replaceAll("(?<!\\\\)\\[", "\\\\\\\\\\[")
  			   .replaceAll("(?<!\\\\)\\)", "\\\\\\\\\\)")
  			   .replaceAll("(?<!\\\\)\\]", "\\\\\\\\\\]")
  			   .replaceAll("(?<!\\\\)\\~", "\\\\\\\\\\~")
  			   .replaceAll("(?<!\\\\)\\`", "\\\\\\\\\\`")
  			   .replaceAll("(?<!\\\\)\\>", "\\\\\\\\\\>")
  			   .replaceAll("(?<!\\\\)\\+", "\\\\\\\\\\+")
  			   .replaceAll("(?<!\\\\)\\*", "\\\\\\\\\\*")
  			   .replaceAll("(?<!\\\\)\\_", "\\\\\\\\\\_")
               .replace("\\.", ".")
 			   .replace("\\#", "#")
		       .replace("\\!", "!")
		       .replace("\\{", "{")
		       .replace("\\}", "}")
		       .replace("\\=", "=")
		       .replace("\\|", "|")
		       .replace("\\-", "-")
               .replace("\\(", "(")
               .replace("\\[", "[")
               .replace("\\)", ")")
               .replace("\\]", "]")
               .replace("\\~", "~")
               .replace("\\`", "`")
               .replace("\\'", "'")
               .replace("\\>", ">")
               .replace("\\+", "+")
               .replace("\\*", "*")
               .replace("\\_", "_")
    	       ;
	}
	@Override
	public int creatorId() {
	    return creatorId == null ? 706141860 : Integer.parseInt(creatorId);
	}
	
    private void whatWeHave(String chatId) throws Exception{
     System.out.println("WhatWeHave method");
    	if(!wwhMessage.isEmpty()) {
    		for(Message wwhm : wwhMessage)
    		    messageDeleter(chatId, wwhm.getMessageId());
    		
    		wwhMessage.clear();
    	}
    	
    	if(appendixMessage != null) {   		
    		messageDeleter(chatId, appendixMessage.getMessageId());
    		appendixMessage = null;
    	}
    	if(timeLineText != null) {
    		messageDeleter(chatId, timeLineText.getMessageId());
    		timeLineText = null;
    	}
    	
    	Map<String, String> media = savedMessage.getMediaFileId();
    	InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    	List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
    	
    	if(!savedMessage.reactionsIsEmpty()) {
			List<InlineKeyboardButton> reflactionButtonRow = new ArrayList<>();
			for(String reflection : savedMessage.getMessageReactions().split("/")) {
				InlineKeyboardButton reflectionButton = new InlineKeyboardButton();
				reflectionButton.setText(reflection);
				reflectionButton.setCallbackData("smile");
				reflactionButtonRow.add(reflectionButton);
			}
			rowList.add(reflactionButtonRow);
    	}
    	
    	if(!savedMessage.urlButtonIsEmpty()) {
	
            for(String[] urlB : savedMessage.getUrlButton()) {
    			List<InlineKeyboardButton> urlButtonRow = new ArrayList<>();
    			InlineKeyboardButton urlButton = new InlineKeyboardButton();
    			urlButton.setText(urlB[0]);
    			urlButton.setUrl(urlB[1]);
    			urlButtonRow.add(urlButton);
    			rowList.add(urlButtonRow);
            }

    	}
    	
    	
		inlineKeyboardMarkup.setKeyboard(rowList);
    	
        if (savedMessage.mediaIsEmpty()) {

        	SendMessage message = new SendMessage();
        	message.setChatId(chatId);
        	message.setText(savedMessage.getText());

        	if(!savedMessage.messageEntitiesIsEmpty()) {
        		message.setEntities(savedMessage.getMessageEntities());
        	} else {
            	message.setParseMode("MarkdownV2");
        	}
            if(rowList.size() > 0)
				message.setReplyMarkup(inlineKeyboardMarkup);
            
			wwhMessage.add(execute(message));
 

        }else if(media.size() == 1) {
        	Map.Entry<String, String> mediaEntry = media.entrySet().iterator().next();
        	String mediaId = mediaEntry.getKey();
        	String mediaType = mediaEntry.getValue();
        	
        	
        	if("photo".equals(mediaType)) {
        		SendPhoto photo = new SendPhoto();
        		photo.setChatId(chatId);
        		photo.setPhoto(new InputFile(mediaId));
        		photo.setCaption(savedMessage.getText());
        		
        		if(!savedMessage.messageEntitiesIsEmpty()) {
        			photo.setCaptionEntities(savedMessage.getMessageEntities());
        		} else {
            		photo.setParseMode("MarkdownV2");
        		}
                if(rowList.size() > 0)
                	photo.setReplyMarkup(inlineKeyboardMarkup);
                
                wwhMessage.add(execute(photo));                
        		
        	}
        	
        	if("video".equals(mediaType)) {
        		SendVideo video = new SendVideo();
        		video.setChatId(chatId);
        		video.setVideo(new InputFile(mediaId));
        		video.setCaption(savedMessage.getText());
        		
        		if(!savedMessage.messageEntitiesIsEmpty()) {
        			video.setCaptionEntities(savedMessage.getMessageEntities());
        		} else {
        			video.setParseMode("MarkdownV2");
        		}
                if(rowList.size() > 0)
                	video.setReplyMarkup(inlineKeyboardMarkup);
                
                wwhMessage.add(execute(video));       
        	}
        	if("sticker".equals(mediaType)) {
        		SendSticker sticker = new SendSticker();
        		sticker.setChatId(chatId);
        		sticker.setSticker(new InputFile(mediaId));
//        		sticker.setParseMode("MarkdownV2");
//        		sticker.setCaption(savedMessage.getText());
/*        		if(!savedMessage.messageEntitiesIsEmpty())
        			sticker.setCaptionEntities(savedMessage.getMessageEntities());*/
        		
                if(rowList.size() > 0)
                	sticker.setReplyMarkup(inlineKeyboardMarkup);
                
                wwhMessage.add(execute(sticker));       
        	}
        }else if(media.size() > 1) {
        	List<String> photoList = new ArrayList<>();
        	List<String> videoList = new ArrayList<>();
        	
        	for(Map.Entry<String, String> entry : media.entrySet()) {
        		if(entry.getValue().contains("photo"))
        			photoList.add(entry.getKey());
        		if(entry.getValue().contains("video"))
        			videoList.add(entry.getKey());
        	}
        	
        	List<InputMedia> medias = new ArrayList<>();
        	
            for(int i = 0; i < photoList.size(); i++) {        		
            	InputMedia mediaPhoto = (InputMedia) new InputMediaPhoto();

            	mediaPhoto.setMedia(photoList.get(i));
            	medias.add(mediaPhoto);
        	}
        	for(int i = 0; i < videoList.size(); i++) {
            	InputMedia mediaVideo = (InputMedia) new InputMediaVideo();

            	mediaVideo.setMedia(videoList.get(i));
            	medias.add(mediaVideo);
        	}
        	
            SendMediaGroup message = new SendMediaGroup();
            message.setChatId(chatId);
            message.setMedias(medias);
            wwhMessage = execute(message);
            
        	SendMessage apendix = new SendMessage();
        	
        	apendix.setChatId(chatId);
        	apendix.setText(savedMessage.getText());
        	
        	if(!savedMessage.messageEntitiesIsEmpty()) {
        		apendix.setEntities(savedMessage.getMessageEntities());
        	} else {
        		apendix.setParseMode("MarkdownV2");
        	}
            if(rowList.size() > 0)
            	apendix.setReplyMarkup(inlineKeyboardMarkup);
        	
            appendixMessage = execute(apendix);
        	
        }

    }
    
    private void setChannelSelection(String chatId, String whatFor) {
        SendMessage message = new SendMessage();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> abortButtonsRow = new ArrayList<>();


        for(String сname : channels.split(" ")) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText("\uD83D\uDCE3 " + сname/*.substring(1)*/);
            inlineKeyboardButton.setCallbackData("\uD83D\uDCE3_" + сname/*.substring(1)*/ + "_" + whatFor);
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            keyboardButtonsRow.add(inlineKeyboardButton);
            rowList.add(keyboardButtonsRow);
        }

        inlineKeyboardMarkup.setKeyboard(rowList);
        message.setReplyMarkup(inlineKeyboardMarkup);
        message.setChatId(chatId);
        message.setText("Виберите канал:");
        try {
            messageToDeleteId.add(execute(message).getMessageId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 

}