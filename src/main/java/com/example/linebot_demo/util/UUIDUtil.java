package com.example.linebot_demo.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class UUIDUtil {

    public String getRandomUUID(){
        return UUID.randomUUID().toString();
    }
}
