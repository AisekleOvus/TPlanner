package spoyogabot;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.LocalTime;

public class SimpleMessage {
	private LocalDate messageDate;
	private LocalTime messageTime;
	private String text;
	private Map<String, String> mediaFileId;
	private String mediaGroupId;
	
	public SimpleMessage() {
		mediaFileId = new HashMap<>();
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
    public void resetMessage() {
    	messageDate = null;
    	messageTime = null;
    	text = null;
    	mediaFileId = null;
    	mediaGroupId = null;
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
