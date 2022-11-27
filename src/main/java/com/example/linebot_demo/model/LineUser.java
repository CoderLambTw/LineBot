package com.example.linebot_demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class LineUser {

    @Id
    private String id;
    private Date startedFollowingSince;
    private List<LineUserMessage> messages = new ArrayList<>();
    private boolean unfollowed = false;

}
