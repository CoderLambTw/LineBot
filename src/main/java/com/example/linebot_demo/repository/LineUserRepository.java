package com.example.linebot_demo.repository;

import com.example.linebot_demo.model.LineUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LineUserRepository extends MongoRepository<LineUser, String> {

}

