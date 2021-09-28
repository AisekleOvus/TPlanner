package spoyogabot;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.time.LocalTime;

import org.telegram.telegrambots.meta.api.objects.MessageEntity;

public class SimpleMessage {
	private LocalDate messageDate;
	private LocalTime messageTime;
	private String text;
	private Map<String, String> mediaFileId;
	private List<MessageEntity> mEntities;
	private String mediaGroupId;
	
	public SimpleMessage() {
		mediaFileId = new LinkedHashMap<>();
		mEntities = new LinkedList<>();
	}

//  IS EMPTY
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
		return mediaFileId == null;
	}
	public boolean mediaGroupIdIsEmpty() {
		return mediaGroupId == null;
	}
	public boolean messageEntitiesIsEmpty() {
		return mEntities.isEmpty();
	}
//  GETTERS
	public LocalDate getDate() {
		return messageDate;
	}
	public LocalTime getTime() {
		return messageTime;
	}
	public String getText() {
		return text;
	}
	public Map<String, String> getGetMedia() {
		return mediaFileId;
	}
	public String getMediaGroupId() {
		return mediaGroupId;
	}
	public List<MessageEntity> getMessageEntities() {
		return mEntities;
	}
//  SETTERS
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
		if(mediaId != null && mediaType != null)
		    mediaFileId.put(mediaId, mediaType);
	}
	public void setMediaGroupId(String mediaGroupId) {
		this.mediaGroupId = mediaGroupId;
	}
	
	public void setMessageEntities(List<MessageEntity> mEntities) {
		if(mEntities.isEmpty())
		    this.mEntities = mEntities;
	}
	
    public void resetMessage() {
    	messageDate = null;
    	messageTime = null;
    	text = null;
    	mediaFileId = null;
    	mediaGroupId = null;
    	mEntities.clear();
    }
    @Override
    public String toString() {
    	String mfidOut = "";
    	for(Map.Entry<String, String> mf :mediaFileId.entrySet()) {
    		mfidOut += mf.getValue() + " = " + mf.getKey() + System.lineSeparator();
    		System.out.println(mf.getKey() + System.lineSeparator());
    	}
    	
    	return mfidOut + text;
    }
}
