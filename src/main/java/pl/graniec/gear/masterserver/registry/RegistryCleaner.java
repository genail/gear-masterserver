package pl.graniec.gear.masterserver.registry;

import static com.google.common.base.Preconditions.checkState;

import java.util.Timer;
import java.util.TimerTask;

public class RegistryCleaner {

	// static fields
	
	private static final int CLEANSE_PERIOD_MS = 60000;
	private static final RegistryCleaner INSTANCE = new RegistryCleaner();

	
	// static methods
	
	public static RegistryCleaner getInstance() {
		return INSTANCE;
	}
	
	
	// non-static fields
	
	private Timer cleanTimer;
	
	
	// non-static methods
	
	private RegistryCleaner() {
		// empty
	}
	
	
	public synchronized void start() {
		checkState(cleanTimer == null);
		cleanTimer = new Timer();
		
		final TimerTask registryCleanseTask = new TimerTask() {
			@Override
			public void run() {
				Registry.getInstance().cleanse();
			}
		};
		
		cleanTimer.schedule(
				registryCleanseTask,
				CLEANSE_PERIOD_MS,
				CLEANSE_PERIOD_MS
		);
	}
	
	public synchronized void stop() {
		checkState(cleanTimer != null);
		cleanTimer.cancel();
		cleanTimer = null;
	}
	
	
}
