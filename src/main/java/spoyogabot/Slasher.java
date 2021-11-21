package spoyogabot;

import java.util.Set;
import java.util.List;
import java.util.TreeSet;
import java.util.ArrayList;

import org.telegram.telegrambots.meta.api.objects.MessageEntity;

public class Slasher {
	private List<MessageEntity> slashOffsets;
	private String modifiedString;
	
	public Slasher(String originString) {
		modifiedString = originString;
		slashOffsets = new ArrayList<>();
		initiator(originString);
	}
	private void initiator(String originString) {
		byte esc = 92;
		char escape = (char) esc;
		int offset = 0;
		int sumLen = 0;
		StringBuilder sbOS = new StringBuilder(originString);
		ArrayList<Character> warningSigns = new ArrayList<>();
		warningSigns.add('\\');
		warningSigns.add('.');
		warningSigns.add('#');
		warningSigns.add('!');
		warningSigns.add('{');
		warningSigns.add('}');
		warningSigns.add('=');
		warningSigns.add('|');
		warningSigns.add('-');
		warningSigns.add('_');
		warningSigns.add('(');
		warningSigns.add('[');
		warningSigns.add(')');
		warningSigns.add(']');
		warningSigns.add('~');
		warningSigns.add('`');
		warningSigns.add('>');
		warningSigns.add('+');
		warningSigns.add('*');
		
		for(int i = 0; i < sbOS.length(); i++) {
			if(warningSigns.contains(sbOS.charAt(i))) {
//				sbOS.insert(i, escape);
//				System.out.println("escaping " + sbOS.charAt(i));
				slashOffsets.add(new MessageEntity("slash", i, 1, "", null, "", "\\"));
//				i++;
			}
		}
//		modifiedString = sbOS.toString();
	}
	public List<MessageEntity> getSlashOffsets() {
		return slashOffsets;
	}
/*	public String getModifiedString() {
		return modifiedString;
	}*/
}