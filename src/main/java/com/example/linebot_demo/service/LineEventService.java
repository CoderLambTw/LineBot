package com.example.linebot_demo.service;

import com.example.linebot_demo.util.HttpUtil;
import com.example.linebot_demo.util.UUIDUtil;
import com.example.linebot_demo.model.FlexMessageDemo;
import com.example.linebot_demo.model.LineUser;
import com.example.linebot_demo.model.LineUserMessage;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.singletonList;

@Slf4j
@Service
public class LineEventService {

    private final UUIDUtil uuidUtil;

    private final LineUserService lineUserService;

    private final LineMessagingClient lineMessagingClient;

    @Value("${line.bot.channel-token}")
    private String accessToken;
    
    @Autowired
    public LineEventService(UUIDUtil uuidUtil, LineUserService lineUserService, LineMessagingClient lineMessagingClient) {
        this.uuidUtil = uuidUtil;
        this.lineUserService = lineUserService;
        this.lineMessagingClient = lineMessagingClient;
    }

    public void handleTextContent(MessageEvent<TextMessageContent> event) throws Exception {
        HttpUtil httpUtil = new HttpUtil();
        TextMessageContent message = event.getMessage();
        String replyToken = event.getReplyToken();
        String text = message.getText();
        Source source = event.getSource();
        String userId = source.getUserId();
        log.info("[Text Message Event] - User {} sent text {}", userId, message.getText());
        this.saveUserMessage(userId, message.getText());
        String responseMessage;
        switch (text) {
            case "過往訊息" -> {
                List<String> messageLists = new ArrayList();
                List<LineUser> lineUsers = lineUserService.findAll();
                lineUsers.forEach(
                        lineUser -> lineUser.getMessages().forEach(
                                lineUserMessage -> messageLists.add(lineUserMessage.getContent())));

                for(int i = 0; i < messageLists.size(); i++){
                    messageLists.set(i , i + 1 + ". " + messageLists.get(i));
                }
                responseMessage = String.join("\n", messageLists);
                this.replyText(replyToken, responseMessage);
            }
            case "天氣" -> {
                String weatherResponse = httpUtil.get("https://opendata.cwb.gov.tw/api/v1/rest/datastore/O-A0003-001?Authorization=CWB-BEFBC2DC-A35D-45D0-88E1-BD1CCC49891F&locationName=臺北");
                JsonFactory factory = new JsonFactory();
                ObjectMapper mapper = new ObjectMapper(factory);
                JsonNode rootNode = mapper.readTree(weatherResponse);
                String locationName = rootNode.path("records").path("location").get(0).path("locationName").asText();
                String weatherTime = rootNode.path("records").path("location").get(0).path("time").path("obsTime").asText();
                String TEMP = rootNode.path("records").path("location").get(0).path("weatherElement").get(3).path("elementValue").asText();
                String HUMD = rootNode.path("records").path("location").get(0).path("weatherElement").get(4).path("elementValue").asText();
                String HUMDModify = Double.parseDouble(HUMD) * 100 + "%";
                String weather = rootNode.path("records").path("location").get(0).path("weatherElement").get(20).path("elementValue").asText();
                String imageUrl = "https://scdn.line-apps.com/n/channel_devcenter/img/fx/01_1_cafe.png";
                switch (weather.substring(0, 1)) {
                    case "晴" ->
                            imageUrl = "https://media.istockphoto.com/vectors/sun-vector-id1171354352?k=20&m=1171354352&s=170667a&w=0&h=GJY7hsu3M3iYgSKLg3cCLQ3-KMxHc-ekBH5LvbrHVRI=";
                    case "多" ->
                            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSeKCIOTvqLuVnnXvoOWu454TSS8cxkvPf11Vb6hVMiBXA68rbCAowbQ5ahQNqz5gZDJt4&usqp=CAU";
                    case "陰" ->
                            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTKa5IKqQJLDzEeXzIcjYp8xBP1YBZEkjqhkUwRwbTC4Hp551KzYqgBJpktXMusyM3qhlQ&usqp=CAU";
                    default -> {
                    }
                }
                try {
                    responseMessage = "{\"replyToken\":\"" + replyToken + "\",\"messages\":["+ FlexMessageDemo.getWeatherFlexMessageDemo(FlexMessageDemo.DEMO2, locationName, weatherTime, TEMP, HUMDModify, weather, imageUrl ) +"]}";
                    URL replyUrl = new URL("https://api.line.me/v2/bot/message/reply"); //回傳的網址
                    HttpsURLConnection con = setConnection(replyUrl, this.accessToken);//使用HttpsURLConnection建立https連線
                    DataOutputStream output = new DataOutputStream(con.getOutputStream()); //開啟HttpsURLConnection的連線
                    output.write(responseMessage.getBytes(StandardCharsets.UTF_8));  //回傳訊息編碼為utf-8
                    output.close();
                    log.info("Resp Code:" + con.getResponseCode() + "; Resp Message:" + con.getResponseMessage());//顯示回傳的結果，若code為200代表回傳成功
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            default -> {
                log.info("Return echo message " + replyToken + ":" + text);
                List<Message> defaultMessageList = new ArrayList();
                defaultMessageList.add(new TextMessage("可以麻煩你再說一次嗎？"));
                defaultMessageList.add(new StickerMessage("11538", "51626532"));
                this.reply(replyToken, defaultMessageList);
            }
        }
    }

    public void handleSticker(MessageEvent<StickerMessageContent> event) {
        StickerMessageContent message = event.getMessage();
        Source source = event.getSource();
        String userId = source.getUserId();
        log.info("[Sticker Message Event] - User {} sent sticker {}", userId, message.getStickerId());
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "這是貼圖");
    }

    public void handleLocation(MessageEvent<LocationMessageContent> event) {
        LocationMessageContent message = event.getMessage();
        Source source = event.getSource();
        String userId = source.getUserId();
        log.info("[Location Message Event] - User {} sent location {}", userId, message.getTitle());
        String replyToken = event.getReplyToken();
        reply(replyToken, new LocationMessage(
                (message.getTitle() == null) ? "Location replied" : message.getTitle(),
                message.getAddress(),
                message.getLatitude(),
                message.getLongitude()
        ));
    }

    private void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        this.reply(replyToken, new TextMessage(message));
    }


    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        reply(replyToken, messages, false);
    }

    private void reply(@NonNull String replyToken,
                       @NonNull List<Message> messages,
                       boolean notificationDisabled) {
        try {
            BotApiResponse apiResponse =
                    lineMessagingClient
                            .replyMessage(new ReplyMessage(replyToken, messages, notificationDisabled))
                            .get();

            log.info("Sent messages: {}", apiResponse);
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveFollowedUser(FollowEvent event) {
        Source source = event.getSource();
        String userId = source.getUserId();
        log.info("[Follow Event] - User {} followed this bot", userId);
        LineUser lineUser = lineUserService.findById(userId);
        if(lineUserService.isNew(lineUser)) {
            lineUser.setId(userId);
            lineUser.setStartedFollowingSince(new Date());
            lineUserService.addUser(lineUser);
            log.info("User {} does not exist, saved ...", userId);
        }
        else {
            log.info("User {} already exists, updating following status", userId);
            lineUser.setUnfollowed(false);
            lineUserService.updateUser(lineUser);
        }
    }

    public void markUserUnfollowed(UnfollowEvent event) {
        Source source = event.getSource();
        String userId = source.getUserId();
        log.info("[Unfollow Event] - User {} unfollowed this bot", userId);
        LineUser lineUser = lineUserService.findById(userId);
        lineUser.setUnfollowed(true);
        lineUserService.updateUser(lineUser);
    }

    public void saveUserMessage(String userId, String message) {
        LineUser lineUser = lineUserService.findById(userId);
        List<LineUserMessage> messages = lineUser.getMessages();
        LineUserMessage lineUserMessage = new LineUserMessage();
        lineUserMessage.setId(uuidUtil.getRandomUUID());
        lineUserMessage.setContent(message);
        lineUserMessage.setSentTime(new Date());
        messages.add(lineUserMessage);
        lineUserService.updateUser(lineUser);
    }

    public HttpsURLConnection setConnection(URL url, String accessToken) throws IOException {
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);
        con.setDoOutput(true);
        return con;
    }

}
