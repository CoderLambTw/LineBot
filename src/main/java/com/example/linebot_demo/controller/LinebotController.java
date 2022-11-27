package com.example.linebot_demo.controller;


import com.example.linebot_demo.service.LineEventService;
import com.example.linebot_demo.service.LineUserService;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@LineMessageHandler
@Slf4j
public class LinebotController {

    private final LineEventService lineEventService;

    private final LineUserService lineUserService;

    public LinebotController(LineEventService lineEventService, LineUserService lineUserService) {
        this.lineEventService = lineEventService;
        this.lineUserService = lineUserService;
    }


    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
        lineEventService.handleTextContent(event);
    }

    @EventMapping
    public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
        lineEventService.handleSticker(event);
    }

    @EventMapping
    public void handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
        lineEventService.handleLocation(event);
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }

    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        lineEventService.saveFollowedUser(event);
    }

    @EventMapping
    public void handleUnfollowEvent(UnfollowEvent event) {
        lineEventService.markUserUnfollowed(event);
    }


}
