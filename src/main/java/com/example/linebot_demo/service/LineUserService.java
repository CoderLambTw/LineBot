package com.example.linebot_demo.service;

import com.example.linebot_demo.model.LineUser;
import com.example.linebot_demo.model.LineUserMessage;
import com.example.linebot_demo.repository.LineUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LineUserService {
    
    private LineUserRepository lineUserRepository;

    @Autowired
    public void setLineUserRepository(LineUserRepository lineUserRepository) {
        this.lineUserRepository = lineUserRepository;
    }

    public List<LineUser> findAll() {
        return this.lineUserRepository.findAll();
    }

    public boolean isNew(LineUser lineUser) {
        if(StringUtils.hasText(lineUser.getId())) {
            return false;
        }
        return true;
    }

    public LineUser findById(String id) {
        Optional<LineUser> lineUser = this.lineUserRepository.findById(id);
        return lineUser.orElse(new LineUser());       
    }

    public LineUser addUser(LineUser lineUser) {
        if(Objects.isNull(lineUser)) {
            throw new RuntimeException("Cannot be null");
        }
        return this.lineUserRepository.save(lineUser);
    }

    public LineUser updateUser(LineUser lineUser) {
        return this.lineUserRepository.save(lineUser);
    }
}
