package spoyogabot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class Scheduler implements Runnable {
	private String dir;
	private LocalDateTime currentMessage;
	private ConcurrentHashMap<File, ScheduledFuture<Boolean>> messagesSent;
	private ScheduledExecutorService scheduler;
	private Set<LocalDateTime> timesQueue;
	private Map<String, String> params;

	public Scheduler(Map<String, String> params) {
		this.params = params;
		this.dir = SpoYogaBot.getDir() + params.get("DIR") + File.separator;
	}
	
	@Override
	public void run() {
	    timesQueue = new TreeSet<>();
	    messagesSent = new ConcurrentHashMap<>();
	    scheduler = Executors.newScheduledThreadPool(1);
    	try {
    	    schedule();
    	} catch(Exception e) {
    	    e.printStackTrace();    	
    	}
	}
	public void schedule() {
		while(true) {
			try {
				Thread.sleep(10000);
				for(/*LocalDateTime*/ File nextMessage : getMessages()) {
					ScheduledFuture<Boolean> testFuture = null;
					if(!messagesSent.containsKey(nextMessage)) {
						testFuture = scheduler.schedule(new TelegramIt(params, nextMessage), LocalDateTime.now().until(LocalDateTime.parse(nextMessage.getName().replace(".",":")), ChronoUnit.SECONDS), TimeUnit.SECONDS);
					    messagesSent.put(nextMessage, testFuture);
					}
				}
				for(Map.Entry<File, ScheduledFuture<Boolean>> futureMessageEntry : messagesSent.entrySet()) {
					if(futureMessageEntry.getValue().isDone()) {
                        messagesSent.remove(futureMessageEntry.getKey(), futureMessageEntry.getValue());
//						new File(dir + futureMessageEntry.getKey().toString().replace(":",".")).delete();
						futureMessageEntry.getKey().delete();
//						System.out.println(dir + futureMessageEntry.getKey().toString().replace(":",".") + " has deleted !");
					}
				}
									
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	public Set<File> getMessages() throws Exception{
		/*return Arrays.stream(new File(dir).listFiles())
				     .filter(file -> file.getName().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}\\.\\d{2}(\\.\\d{2})*"))
                     .map(file -> LocalDateTime.parse(file.getName().replace(".", ":")))
                     .sorted()
                     .collect(Collectors.toSet());*/
		return Files.walk(Paths.get(dir))
				          .filter(Files::isRegularFile)
				          .map(Path::toFile)
//				          .map(file -> LocalDateTime.parse(file.getName().replace(".", ":")))
				          .filter(file -> file.getName().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}\\.\\d{2}(\\.\\d{2})*"))
				          .sorted((one, another) -> {
							  LocalDateTime firstF = LocalDateTime.parse(one.getName().replace(".", ":"));
							  LocalDateTime anotherF = LocalDateTime.parse(another.getName().replace(".", ":"));
							  return firstF.compareTo(anotherF);
						  })
				          .collect(Collectors.toSet());

	}
	public LocalDateTime getDateTime() {
		return LocalDateTime.now();
	}
}