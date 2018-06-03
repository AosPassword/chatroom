package org.redrock.web.chatroom.utils;

import org.redrock.web.chatroom.bean.Message;
import org.redrock.web.chatroom.bean.Relation;
import org.redrock.web.chatroom.bean.User;
import org.redrock.web.chatroom.mapper.MessageMapper;
import org.redrock.web.chatroom.mapper.RelationMapper;
import org.redrock.web.chatroom.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;

@Component
@ConfigurationProperties(prefix = "handle-been")
public class HandleChatUtil {

    @Autowired
    MessageMapper messageMapper;

    @Autowired
    RelationMapper relationMapper;

    @Autowired
    UserMapper userMapper;

    private long missTime;
    private String showAddRequestKeyWord;
    private String showFriendsKeyWord;
    private String noAcceptUserKeyWord;
    private String acceptUserKeyWord;
    private String historyKeyWord;
    private String addUserKeyWord;
    private String helpKeyWord;
    private String helpInfo;

    /**
     * @param message
     * @param messagingTemplate
     * @param oldMessages
     */
    public void indexHandle(Message message, SimpMessagingTemplate messagingTemplate, List<Message> oldMessages) throws ParseException {
        String info = message.getInfo();
        String receive_name = message.getReceive_user_name();
        System.out.println("info------------>"+info);

        if (receive_name.isEmpty()) {
            //如果收信人名字为空
            if (info.equalsIgnoreCase(helpKeyWord)) {
                //获得help信息
                message.setInfo(helpInfo);
                send(message, messagingTemplate, true, message.getSend_user_name());

            } else if (info.equalsIgnoreCase(historyKeyWord)) {
                //获取的全部的历史记录
                List<Message> list = messageMapper.findMessageHistoryByUsername(message.getSend_user_name());
                StringBuilder listinfo = new StringBuilder();
                listinfo.append(list.get(0).toString());
                for (int i = 1; i < list.size(); i++) {
                    listinfo.append("<br/>");
                    listinfo.append(list.get(i).toString());
                }
                message.setInfo(listinfo.toString());
                send(message, messagingTemplate, true, message.getSend_user_name());

            }else if (info.equalsIgnoreCase(showAddRequestKeyWord)) {
                //显示未接受的好友请求
                //首先先看看有没有
                List<Message> list = messageMapper.findNotAcceptMessage(message.getSend_user_name());
                if (list.isEmpty()) {
                    messagingTemplate.convertAndSendToUser(message.getSend_user_name(),
                            "/queue/notifications", "NO Return");
                } else {
                    //如果有则发出
                    Message requestMessage = new Message();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder = handleList(list, stringBuilder);
                    message.setDate(FormatUtil.getDate(new Date()));
                    message.setReceive_user_name(message.getSend_user_name());
                    message.setInfo(stringBuilder.toString());
                    send(message, messagingTemplate, true, message.getSend_user_name());
                }
            }else if (info.equalsIgnoreCase(showFriendsKeyWord)){
                //显示自己的所有好友
                User user=userMapper.findUserByUsername(message.getSend_user_name());
                List<Relation> list=relationMapper.findRelation(user.getId());
                //老规矩，看看是不是空的
                if (list.isEmpty()){
                    messagingTemplate.convertAndSendToUser(message.getSend_user_name(),
                            "/queue/notifications", "NO Return");
                }else {
                    //首先先把所有人的id取出来，并且去重,去掉自己
                    Set<Integer> set=new HashSet<>();
                    for (Relation relation:list){
                        set.add(relation.getAccept_invitation_id());
                        set.add(relation.getSend_invitation_id());
                    }
                    set.remove(user.getId());
                    //再遍历出所有的User放到list里面去
                    List<String> users=new ArrayList<>();
                    for (Integer integer:set){
                        User u = userMapper.findUserById(integer);
                        users.add(u.getUsername());
                    }
                    //重写message发回去
                    StringBuilder stringBuilder=new StringBuilder();
                    stringBuilder=handleList(list,stringBuilder);
                    message.setInfo(stringBuilder.toString());
                    message.setReceive_user_name(message.getSend_user_name());
                    send(message,messagingTemplate,true,message.getSend_user_name());
                }
            } else {
                //错误输入
                messagingTemplate.convertAndSendToUser(message.getSend_user_name(),
                        "/queue/notifications", "请输入" + helpKeyWord + "查看正确操作方法");
            }
        } else {
            //正确输入
            if (!(info.equalsIgnoreCase(addUserKeyWord) || info.equalsIgnoreCase(acceptUserKeyWord) || info.equalsIgnoreCase(noAcceptUserKeyWord))) {

                //先把要发的内容发出去
                send(message, messagingTemplate, false, message.getSend_user_name());
                //找到双方交互的未接收到的历史信息，如果最近的一次显示和现在的时间没有超时，则默认对方已经接受这条信息
                List<Message> eachOtherNotAccuptMessage = messageMapper.findEachOtherNotAcceptMessage(message.getSend_user_name(), message.getReceive_user_name());
                Message target_message=null;
                if (eachOtherNotAccuptMessage.size()>0) {
                    target_message = eachOtherNotAccuptMessage.get(eachOtherNotAccuptMessage.size() - 1);
                }
                if (target_message!=null) {
                    if (!isMiss(target_message)) {
                        /**
                         * 如果你所发出，他所接受的这条信息，与他所发出你所接受的上条信息之间的时间并未超时
                         * 那么就可以直接默认对方是接受到了这条信息的
                         */
                        messageMapper.insertAcceptMessage(message);
                    } else {
                        /**超时则默认对方并未接受到*/
                        messageMapper.insertNoAcceptMessage(message);
                    }
                }
            } else if (info.equalsIgnoreCase(addUserKeyWord)){
                //如果正确的发送了添加好友的请求，首先先要查询自己与对方是否是好友
                User send_user=userMapper.findUserByUsername(message.getSend_user_name());
                User receive_user=userMapper.findUserByUsername(message.getReceive_user_name());
                List<Relation> relations=relationMapper.findRelation(send_user.getId());
                boolean isFriend=false;
                for (Relation relation:relations){
                    if (relation.getAccept_invitation_id()==receive_user.getId() ||
                            relation.getSend_invitation_id()==receive_user.getId() ||
                            isFriend){
                        //如果两个人是好友
                        Message rev=new Message();
                        rev.setReceive_user_name(message.getSend_user_name());
                        rev.setInfo("你们之间已经是好友了，直接在 发送内容 中输入" +showFriendsKeyWord+
                                "查询您的好友列表");
                        send(rev,messagingTemplate,true,message.getSend_user_name());
                        isFriend=true;
                    }
                }
                if (!isFriend) {
                    //还要确认自己是否曾经发送过给对方的请求,以及对方是否曾经给自己发送过请求
                    boolean notSend=false;
                    Message sql_one=messageMapper.findNotAcceptRequestFromTowPerson(message.getSend_user_name(),addUserKeyWord,message.getReceive_user_name());
                    Message sql_two=messageMapper.findNotAcceptRequestFromTowPerson(message.getReceive_user_name(),addUserKeyWord,message.getSend_user_name());
                    if (sql_one==null && sql_two==null){
                        notSend=true;
                    }
                    if (notSend) {
                        //确认请求不存在
                        send(message, messagingTemplate, false, message.getSend_user_name());
                        //不管什么情况全部默认对方没有接受
                        messageMapper.insertNoAcceptMessage(message);
                    }else {
                        //说明请求存在
                        messagingTemplate.convertAndSendToUser(message.getSend_user_name(),
                                "/queue/notifications", "Request is exist!");
                    }
                }
            }else if (info.equalsIgnoreCase(acceptUserKeyWord)){
                //只要能发过来的请求说明一定互相都是好友
                //如果接受了对方的邀请先查询对方是否曾经发过自己没有接受加好友的请求
                Message requestMessage=messageMapper.findNotAcceptRequestFromTowPerson(message.getReceive_user_name(),addUserKeyWord,message.getSend_user_name());
                if (requestMessage.getInfo().isEmpty()){
                    //如果对方并没有发送过这条邀请
                    messagingTemplate.convertAndSendToUser(message.getSend_user_name(),
                            "/queue/notifications", "no this addUserQuestion");
                }else {
                    //说明对方曾经发出过这条邀请
                    User send_user=userMapper.findUserByUsername(requestMessage.getSend_user_name());
                    User receive_user=userMapper.findUserByUsername(requestMessage.getReceive_user_name());
                    //将关系插入关系表
                    relationMapper.insertRelation(send_user.getId(),receive_user.getId());
                    //确认加好友的记录
                    messageMapper.handleNotAcceptAddUserMessage(requestMessage.getId(),addUserKeyWord);
                }
            }else if (info.equalsIgnoreCase(noAcceptUserKeyWord)){
                //只要能发过来的请求说明一定互相都是好友
                //如果接受了对方的邀请先查询对方是否曾经发过自己没有接受加好友的请求
                Message requestMessage=messageMapper.findNotAcceptRequestFromTowPerson(message.getReceive_user_name(),addUserKeyWord,message.getSend_user_name());
                if (requestMessage.getInfo().isEmpty()){
                    //如果对方并没有发送过这条邀请
                    messagingTemplate.convertAndSendToUser(message.getSend_user_name(),
                            "/queue/notifications", "no this addUserQuestion");
                }else {
                    //说明对方曾经发出过这条邀请
                    //拒绝这条邀请
                    messageMapper.refuseNotAcceptAddUserMessage(requestMessage.getId(),addUserKeyWord);
                }
            }
        }
        //只要输出了就默认所有记录都被浏览，除了加好友的信息，加好友的信息另外处理
        for (Message m : oldMessages) {
            //将所有的老记录全部刷新为已经接受
            System.out.println("id--------->" + m.getId());
            messageMapper.handleNotAcceptMessage(m.getId(), addUserKeyWord);
        }
    }

    public void handleNotAcceptMessage(List<Message> list, SimpMessagingTemplate messagingTemplate, String send_name) throws ParseException {
        if (list.isEmpty()) {
            return;
        }
        Message lastMessage = list.get(list.size() - 1);
        Message message = new Message();
        StringBuilder stringBuilder = new StringBuilder();
        int adduser = 0;
        int msg = 0;
        for (Message m : list) {
            if (m.getInfo().equalsIgnoreCase(addUserKeyWord)) {
                adduser++;
            } else {
                msg++;
            }
        }
        if (adduser != 0) {
            //不管什么时候，如果用户不回应就使劲推好友记录
            stringBuilder.append("您有" + adduser + "条好友申请未读;")
                    .append("<br/>").append("在 发给谁 中填入")
                    .append(acceptUserKeyWord).append("在 发送内容 中填入发出邀请的好友名即可接受")
                    .append("<br/>").append("在 发给谁 中填入")
                    .append(noAcceptUserKeyWord).append("在 发送内容 中填入发出邀请的好友名即可拒绝")
                    .append("<br/>").append("在 发送内容 中填入").append(showAddRequestKeyWord)
                    .append("查询你未接受的好友请求");
        }
        if (msg != 0) {
            //检查上一个你所接受到的记录记录和这一条记录之间的时间是否超时
            boolean flag = isMiss(lastMessage);
            if (flag) {
                //只有超时之后才会提示信息没收到
                //管他呢一口气全都推出去管他想不想看
                stringBuilder.append("您有" + msg + "条消息记录未读");
                stringBuilder.append("<br/>");
//                stringBuilder.append(list.get(0).toString());
//                for (int i = 1; i < list.size(); i++) {
//                    stringBuilder.append("<br/>");
//                    stringBuilder.append(list.get(i).toString());
//                }
                stringBuilder=handleList(list,stringBuilder);
            }
        }
        message.setReceive_user_name(send_name);
        message.setInfo(stringBuilder.toString());
        message.setDate(FormatUtil.getDate(new Date()));
        send(message, messagingTemplate, true, message.getReceive_user_name());
    }

    /**
     * 推送方法
     *
     * @param message           所要推送的信息
     * @param messagingTemplate 推送信息所用的类
     * @param isunidirectional  是否只单方向推送
     * @param send_name         向哪个人单方向推送
     */
    public void send(Message message, SimpMessagingTemplate messagingTemplate,
                     boolean isunidirectional, String send_name) {
        if (isunidirectional) {
            messagingTemplate.convertAndSendToUser(send_name,
                    "/queue/notifications",
                    "[" + message.getDate() + "]" + "<br/>"
                            + "<--------------------><br/>" + message.getInfo() + "<br/>" +
                            "<-------------------->");
        } else {
            messagingTemplate.convertAndSendToUser(message.getSend_user_name(),
                    "/queue/notifications",
                    "[" + message.getDate() + "]" +
                            message.getSend_user_name() + "---->" + message.getReceive_user_name() +
                            ":" + message.getInfo());

            messagingTemplate.convertAndSendToUser(message.getReceive_user_name(),
                    "/queue/notifications",
                    "[" + message.getDate() + "]" +
                            message.getSend_user_name() + "---->" + message.getReceive_user_name() +
                            ":" + message.getInfo());
        }
    }

    public StringBuilder handleList(List list,StringBuilder stringBuilder){
        stringBuilder.append(list.get(0).toString());
        for (int i = 1; i < list.size(); i++) {
            stringBuilder.append("<br/>");
            stringBuilder.append(list.get(i).toString());
        }
        return stringBuilder;
    }
    /**
     * 判断信息是否超时
     * @param message
     * @return
     * @throws ParseException
     */
    public boolean isMiss(Message message) throws ParseException {
        long oldDate = FormatUtil.transToDate(message.getDate()).getTime();
        System.out.println("oldTime------------->" + oldDate);
        long nowDate = new Date().getTime();
        System.out.println("nowTime------------->" + nowDate);
        return nowDate - oldDate > missTime;
    }

    public MessageMapper getMessageMapper() {
        return messageMapper;
    }

    public void setMessageMapper(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    public String getHistoryKeyWord() {
        return historyKeyWord;
    }

    public void setHistoryKeyWord(String historyKeyWord) {
        this.historyKeyWord = historyKeyWord;
    }

    public String getAddUserKeyWord() {
        return addUserKeyWord;
    }

    public void setAddUserKeyWord(String addUserKeyWord) {
        this.addUserKeyWord = addUserKeyWord;
    }

    public String getHelpKeyWord() {
        return helpKeyWord;
    }

    public void setHelpKeyWord(String helpKeyWord) {
        this.helpKeyWord = helpKeyWord;
    }

    public String getHelpInfo() {
        return helpInfo;
    }

    public void setHelpInfo(String helpInfo) {
        this.helpInfo = helpInfo;
    }

    @Override
    public String toString() {
        return "HandleChatUtil{" +
                "messageMapper=" + messageMapper +
                ", relationMapper=" + relationMapper +
                ", userMapper=" + userMapper +
                ", missTime=" + missTime +
                ", showAddRequestKeyWord='" + showAddRequestKeyWord + '\'' +
                ", showFriendsKeyWord='" + showFriendsKeyWord + '\'' +
                ", noAcceptUserKeyWord='" + noAcceptUserKeyWord + '\'' +
                ", acceptUserKeyWord='" + acceptUserKeyWord + '\'' +
                ", historyKeyWord='" + historyKeyWord + '\'' +
                ", addUserKeyWord='" + addUserKeyWord + '\'' +
                ", helpKeyWord='" + helpKeyWord + '\'' +
                ", helpInfo='" + helpInfo + '\'' +
                '}';
    }

    public String getNoAcceptUserKeyWord() {
        return noAcceptUserKeyWord;
    }

    public void setNoAcceptUserKeyWord(String noAcceptUserKeyWord) {
        this.noAcceptUserKeyWord = noAcceptUserKeyWord;
    }


    public long getMissTime() {
        return missTime;
    }

    public void setMissTime(long missTime) {
        this.missTime = missTime;
    }

    public RelationMapper getRelationMapper() {
        return relationMapper;
    }

    public void setRelationMapper(RelationMapper relationMapper) {
        this.relationMapper = relationMapper;
    }

    public UserMapper getUserMapper() {
        return userMapper;
    }

    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public String getShowAddRequestKeyWord() {
        return showAddRequestKeyWord;
    }

    public void setShowAddRequestKeyWord(String showAddRequestKeyWord) {
        this.showAddRequestKeyWord = showAddRequestKeyWord;
    }

    public String getShowFriendsKeyWord() {
        return showFriendsKeyWord;
    }

    public void setShowFriendsKeyWord(String showFriendsKeyWord) {
        this.showFriendsKeyWord = showFriendsKeyWord;
    }

    public String getAcceptUserKeyWord() {
        return acceptUserKeyWord;
    }

    public void setAcceptUserKeyWord(String acceptUserKeyWord) {
        this.acceptUserKeyWord = acceptUserKeyWord;
    }
}
