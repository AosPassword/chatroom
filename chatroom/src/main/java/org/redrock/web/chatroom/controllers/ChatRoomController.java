package org.redrock.web.chatroom.controllers;

import com.google.gson.Gson;


import org.redrock.web.chatroom.bean.Message;
import org.redrock.web.chatroom.mapper.MessageMapper;
import org.redrock.web.chatroom.mapper.UserMapper;
import org.redrock.web.chatroom.utils.FormatUtil;
import org.redrock.web.chatroom.utils.HandleChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;


@Controller
public class ChatRoomController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    UserMapper userMapper;

    @Autowired
    MessageMapper messageMapper;

    @Autowired
    HandleChatUtil handleChatUtil;

    /**
     * spring is the master of the world!!!
     *
     * @param msg
     * @param principal
     */
    @MessageMapping("/chat")
    public void handleChat(String msg, Principal principal) throws ParseException, InterruptedException {
        Gson gson = new Gson();
        Message message = gson.fromJson(msg, Message.class);
        message.setSend_user_name(principal.getName());
        Date date = new Date();
        message.setDate(FormatUtil.getDate(date));
        //查询所有未收到的信息列表
        List<Message> list = messageMapper.findNotAcceptMessage(message.getSend_user_name());
        handleChatUtil.handleNotAcceptMessage(list, messagingTemplate, message.getSend_user_name());
        //确保先提示信息然后再发消息
        Thread.sleep(60);
        handleChatUtil.indexHandle(message, messagingTemplate, list);
    }

    @RequestMapping("/chatroom")
    public String tran() {
        return "chatroom";
    }

}
