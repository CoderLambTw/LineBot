package com.example.linebot_demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class LineUserMessage {
    
    @Id
    private String id;
    private String content;
    private Date sentTime;

}
