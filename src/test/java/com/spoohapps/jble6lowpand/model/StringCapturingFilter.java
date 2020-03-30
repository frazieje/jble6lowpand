package com.spoohapps.jble6lowpand.model;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StringCapturingFilter extends BaseFilter {

    private static Logger logger = LoggerFactory.getLogger(StringCapturingFilter.class);

    private static final CopyOnWriteArrayList<String> readQueue = new CopyOnWriteArrayList<>();

    private static final CopyOnWriteArrayList<String> writeQueue = new CopyOnWriteArrayList<>();

    @Override
    public NextAction handleRead(final FilterChainContext ctx) {

        String message = ctx.getMessage();

        logger.debug("adding to read journal: {} from {}", message, ctx.getAddress());

        readQueue.add(message);

        return ctx.getInvokeAction();
    }

    @Override
    public NextAction handleWrite(final FilterChainContext ctx) {

        String message = ctx.getMessage();

        logger.debug("adding to write journal: {}", message);

        writeQueue.add(message);

        return ctx.getInvokeAction();
    }

    public List<String> getReadQueue() {
        return new ArrayList<>(readQueue);
    }

    public List<String> getWriteQueue() {
        return new ArrayList<>(writeQueue);
    }

    public void recycle() {
        readQueue.clear();
        writeQueue.clear();
    }
}
