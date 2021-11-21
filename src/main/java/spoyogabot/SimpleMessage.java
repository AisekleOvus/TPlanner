package spoyogabot;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.io.File;

import org.telegram.telegrambots.meta.api.objects.MessageEntity;

public class SimpleMessage {
	private LocalDate messageDate;
	private LocalTime messageTime;
	private String text;
	private String reactions;
	private Map<String, String> mediaFileId;
	private List<MessageEntity> mEntities;
	private String mediaGroupId;
	private List<String[]> urlButtons;
	private static final String WARNING_MESSAGE = "/***************************************************************************/\n" 
                                                + "/*            Памятка по Telegram MarkdownV2                               */\n"
                                                + "/*                                                                         */\n"
                                                + "/*            *жирный шрифт*                                               */\n"
                                                + "/*            _наклонный шрифт_                                            */\n"
                                                + "/*            ~зачеркнутый шрифт~                                          */\n"
                                                + "/*           __подчеркнутый шрифт__                                        */\n"
                                                + "/*            [текстовая ссылка URL](http://www.example.com/)              */\n"
                                                + "/*            [текстовая ссылка на пользователя](tg://user?id=123456789)   */\n"
                                                + "/*            `равноширокий шрифт`                                         */\n"
                                                + "/*                                                                         */\n"
                                                + "/*            ```                                                          */\n"
                                                + "/*             преформатированный равноширокий шрифт                       */\n"
                                                + "/*            ```                                                          */\n"
                                                + "/*                                                                         */\n"
                                                + "/*            ```python                                                    */\n"
                                                + "/*             преформатированный равноширокий текст на языке python       */\n"
                                                + "/*            ```                                                          */\n"
                                                + "/*                                                                         */\n"
                                                + "/*            в случае, когда необходимо подчеркнуть наклонный текст       */\n"
                                                + "/*            используйте сущность(копируйте отсюда) _­__ в начале участка  */\n"
                                                + "/*            используйте сущность(копируйте отсюда) __­_ в конце участка   */\n"
                                                + "/*                                                                         */\n"
                                                + "/*                                                                         */\n"
                                                + "/*                  СЛЕДУЮЩИЕ СПЕЦСИМВОЛЫ ДОЛЖНЫ БЫТЬ ЭКРАНИРОВАНЫ         */\n"
                                                + "/*                                   символом '\\'                          */\n"
                                                + "/*                                                                         */\n"                       
                                                + "/*                       _  *  [  ]  (  )  ~  `  >  #  +  -                */\n"
                                                + "/*                                 =  |  {  }  .  !                        */\n"
                                                + "/*                                                                         */\n"
                                                + "/*                                                                         */\n"
                                                + "/***************************************************************************/\n";
	
	public SimpleMessage() {
		mediaFileId = new LinkedHashMap<>();
		mEntities = new LinkedList<>();
		urlButtons = new LinkedList<>();
	}

//  IS EMPTY
	public boolean urlButtonIsEmpty() {
		return urlButtons.isEmpty();
	}	
	public boolean dateIsEmpty() {
		return messageDate == null;
	}
	public boolean timeIsEmpty() {
		return messageTime == null;
	}
	public boolean textIsEmpty() {
		return text == null;
	}
	public boolean mediaIsEmpty() {
		return mediaFileId.isEmpty();
	}
	public boolean mediaGroupIdIsEmpty() {
		return mediaGroupId == null;
	}
	public boolean messageEntitiesIsEmpty() {
		return mEntities.isEmpty();
	}
	public boolean reactionsIsEmpty() {
		return reactions == null;
	}
//  GETTERS
	public List<String[]> getUrlButton() {
		return urlButtons;
	}	
	public LocalDate getDate() {
		return messageDate;
	}
	public LocalTime getTime() {
		return messageTime;
	}
	public String getText() {
//		System.out.println(text);
		return text;
	}
	public Map<String, String> getMediaFileId() {
		return mediaFileId;
	}
	public String getMediaGroupId() {
		return mediaGroupId;
	}
	public List<MessageEntity> getMessageEntities() {
		return mEntities;
	}
	public String getMessageReactions() {
		return reactions;
	}
	public String getMessageFileName() {
		return LocalDateTime.of(messageDate, messageTime).toString().replace(":", ".");
	}
//  SETTERS
	public void setUrlButton(List<String[]> urlButtons) {
		this.urlButtons = urlButtons;
	}
	public void setUrlButton(String[] urlButton) {
		urlButtons.add(urlButton);
	}
	public void setDate(LocalDate messageDate) {
		this.messageDate = messageDate;
	}
	public void setTime(LocalTime messageTime) {
		this.messageTime = messageTime;
	}
	public void setText(String text) {
		if(text != null)
		    this.text = text;
	}
	public void setMediaFileId(String mediaId, String mediaType) {
//		System.out.println("From SimpleMessage: " + mediaType + " " + mediaId);
		if(mediaId != null && mediaType != null) {
			mediaFileId.put(mediaId, mediaType);
			if(getMediaFileId().size() == 1)
				textCutter();
		}
	}
	public void setMediaGroupId(String mediaGroupId) {
		this.mediaGroupId = mediaGroupId;
	}
	
	public void setMessageEntities(List<MessageEntity> mEntities) {
		if(mEntities.isEmpty())
		    this.mEntities = mEntities;
	}
	public void setMessageReactions(String reactions) {
	    this.reactions = reactions;
	}

	public void clearMedia() {
		mediaFileId.clear();
	}

    public void resetMessage() {
    	messageDate = null;
    	messageTime = null;
    	text = null;
    	mediaFileId.clear();
    	mediaGroupId = null;
    	mEntities.clear();
    	reactions = null;
    	urlButtons.clear();
    }
    @Override
    public String toString() {

    	StringBuilder sb = new StringBuilder();
    	              sb.append(WARNING_MESSAGE);
    	for(Map.Entry<String, String> mf :mediaFileId.entrySet()) {
    		sb.append(mf.getValue() + " = " + mf.getKey() + System.lineSeparator());
    	}
    	if(!reactionsIsEmpty())
    		sb.append("reactions =" + reactions + System.lineSeparator());
    	if(!urlButtons.isEmpty()) {
    		for(String[] urlButton : urlButtons) {
        		sb.append("ButtonTitle = " + urlButton[0] + System.lineSeparator());
        		sb.append("ButtonUrl = " + urlButton[1] + System.lineSeparator());
    		}
    	}
    	sb.append(text);
    	
    	return sb.toString();
    }
    
    public static SimpleMessage restore(String path, String fileName) {
    	SimpleMessage sm = new SimpleMessage();
    	StringBuilder sb = new StringBuilder();
    	try(Scanner sc = new Scanner(new File(path + fileName)).useDelimiter("\\R")) {
        	LocalDateTime messageDT = LocalDateTime.parse(fileName.replace(".", ":"));
        	
        	sm.setDate(messageDT.toLocalDate());
        	sm.setTime(messageDT.toLocalTime());
        	
        	while(sc.hasNext()) {
        		String line = sc.next();
//        		System.out.println(line);
        		
        		if(line.contains("photo =")) {
    			    sm.setMediaFileId(line.substring(7).trim(), "photo");
//    		    	System.out.println("PHOTO_LINE: " + line + "\n");
        		}
    		    else if(line.contains("video =")) {
    			    sm.setMediaFileId(line.substring(7).trim(), "video");
//    		    	System.out.println("VIDEO_LINE: " + line + "\n");
    		    }
    		    else if(line.contains("sticker =")) {
    			    sm.setMediaFileId(line.substring(10).trim(), "sticker");
//    		    	System.out.println("VIDEO_LINE: " + line + "\n");
    		    }
    		    else if(line.contains("reactions =")) {
    			    sm.setMessageReactions(line.substring(11).trim());
 //   		    	System.out.println("REACTIONS_LINE: " + line + "\n");
    		    }
    		    else if(line.contains("ButtonTitle =")) {   		
    			    sm.setUrlButton(new String[] {line.substring(13).trim(), sc.next().substring(11).trim()});
 //   		    	System.out.println("BUTTON_TITLE_LINE: " + line + "\n");
    		    }
    		    else if(!line.startsWith("/*") && !line.endsWith("*/")) {
 //   		    	System.out.println("TEXT_LINE: " + line + "\n");
    			    sb.append(line);
    		    }
        		
        	}
        	
        	sm.setText(sb.toString());
//        	System.out.println("RESTORATION PROCEESED:  \n" + sm.toString() + "\n\n");
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	return sm;
    }
	private void textCutter() {
		String text = getText();
//		System.out.println("Before: " + text.length());
		if(text != null && text.length() > 984)
			setText(text.substring(0,984));
//		System.out.println("After: " + getText().length());
	}
}
