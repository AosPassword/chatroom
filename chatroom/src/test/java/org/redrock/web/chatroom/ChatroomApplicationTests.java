package org.redrock.web.chatroom;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redrock.web.chatroom.utils.HandleChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChatroomApplicationTests {

    @Autowired
    HandleChatUtil handleChatUtil;
    @Test
    public void contextLoads() {
        System.out.println(handleChatUtil.toString());
    }

}
