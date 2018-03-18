package com.spoohapps.jble6lowpand.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class FileBasedKnownDeviceRepository implements KnownDeviceRepository {

	//private final WhitelistFileWatcher watcher;
	private final Path filePath;

	private final Set<BTAddress> knownDevices;

    private final Logger logger = LoggerFactory.getLogger(FileBasedKnownDeviceRepository.class);

    public FileBasedKnownDeviceRepository() {
        this(Paths.get(System.getProperty("user.home"),"whitelist.conf"));
    }
	
	public FileBasedKnownDeviceRepository(Path filePath) {
		//this.watcher = new WhitelistFileWatcher(filePath, this::onFileChanged);
		this.filePath = filePath;
		knownDevices = new CopyOnWriteArraySet<>();
	}

	public void startWatcher() {
        init();
        //watcher.start();
    }

    public void stopWatcher() {
	    //watcher.stop();
    }

    private void init() {
        Set<BTAddress> whitelistedAddresses = getStoredAddresses();
        knownDevices.addAll(whitelistedAddresses);
    }

    private void onFileChanged() {
        Set<BTAddress> whitelistedAddresses = getStoredAddresses();
        knownDevices.retainAll(whitelistedAddresses);
        knownDevices.addAll(whitelistedAddresses);
    }

	@Override
	public boolean contains(BTAddress address) {
		return knownDevices.contains(address);
	}

	@Override
	public void add(BTAddress address) {
        Set<BTAddress> copy = getStoredAddresses();
        copy.add(address);
        setStoredAddresses(copy);
	}

	@Override
	public void remove(BTAddress address) {
        Set<BTAddress> copy = getStoredAddresses();
        copy.remove(address);
        setStoredAddresses(copy);
	}

	@Override
	public void clear() {
        setStoredAddresses(new HashSet<>());
	}

	@Override
    public Set<BTAddress> getAll() {
        return new HashSet<>(knownDevices);
    }

	private synchronized void setStoredAddresses(Set<BTAddress> addresses) {
        try {
            byte[] bytes = addresses.stream().map(BTAddress::toString).collect(Collectors.joining(System.getProperty("line.separator"))).getBytes();
            Files.write(filePath, bytes);
        } catch (IOException ioe) {
            logger.error("error writing whitelist file");
        }
    }

	private synchronized Set<BTAddress> getStoredAddresses() {
	    try {
            return Files.lines(filePath).map(BTAddress::new).collect(Collectors.toCollection(HashSet::new));
        } catch (IOException ioe) {
            logger.error("error reading whitelist file");
        }
        return new HashSet<>();
    }

	private class WhitelistFileWatcher {

        private final Path filePath;
        private AtomicBoolean stopped = new AtomicBoolean(false);
        private final Runnable fileChanged;

        WhitelistFileWatcher(Path filePath, Runnable fileChanged) {
            this.filePath = filePath;
            this.fileChanged = fileChanged;
        }

        void start() {
            new Thread(this::run).start();
        }

        void stop() {
            stopped.set(true);
        }

        void run() {
            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                Path path = filePath.getParent();
                path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                while (!stopped.get()) {
                    WatchKey key;
                    try { key = watcher.poll(25, TimeUnit.MILLISECONDS); }
                    catch (InterruptedException e) { return; }
                    if (key == null) { Thread.yield(); continue; }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            Thread.yield();
                            continue;
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY
                                && filename.toString().equals(filePath.getFileName().toString())) {
                            fileChanged.run();
                        }
                        boolean valid = key.reset();
                        if (!valid) { break; }
                    }
                    Thread.yield();
                }
            } catch (Throwable e) {
                logger.error("error starting file watcher");
            }
        }
    }
}
