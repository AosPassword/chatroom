package org.redrock.web.chatroom.mapper;

import org.apache.ibatis.annotations.*;
import org.redrock.web.chatroom.bean.Message;
import org.redrock.web.chatroom.bean.User;

import java.util.List;

//private String send_user_name;
//private String info;
//private String receive_user_name;
//private Date date;
//private int isAccept;

@Mapper
public interface MessageMapper {
    @Select("select * from messages where id = #{id}")
    public Message findMessageById(Integer id);

    //查询所有与你有关的信息
    @Select("select * from messages where receive_user_name = #{username} or send_user_name = #{username}")
    public List<Message> findMessageHistoryByUsername(String username);

//    @Select("SELECT * FROM  (SELECT * FROM messages WHERE receive_user_name = #{username}) as history WHERE id BETWEEN #{oldid} AND #{newid}")
//    public List<Message> findNotAcceptHistoryMessageById(int oldid,int newid,String username);

    //插入默认对方未接受的信息
    @Insert("insert into messages (send_user_name,info,receive_user_name,date,isAccept) values (#{send_user_name},#{info},#{receive_user_name},#{date},0)")
    public void insertNoAcceptMessage(Message message);

    //插入默认对方已经接受的信息
    @Insert("insert into messages (send_user_name,info,receive_user_name,date,isAccept) values (#{send_user_name},#{info},#{receive_user_name},#{date},1)")
    public void insertAcceptMessage(Message message);

    //查询你所收到，他所发出并且你没有接受的信息
    @Select("select * from messages where receive_user_name = #{username} and send_user_name = #{target_name} and isAccept = 0")
    public List<Message> findEachOtherNotAcceptMessage(@Param("username") String username,@Param("target_name") String target_name);

    //查询你所有未接受的信息
    @Select("select * from messages where receive_user_name = #{username} and isAccept = 0")
    public List<Message> findNotAcceptMessage(String username);

    //将未接受的信息变为已经接受的信息
    @Update("update messages set isAccept = 1 where id =#{id} and info != #{key}")
    public void handleNotAcceptMessage(@Param("id") Integer id,@Param("key") String key);

    //查询自己未接受的好友请求
    @Select ("select * from messages where receive_user_name = #{username} and isAccept = 0 and info = #{key}")
    public List<Message> findNotAcceptAddUserRequest(@Param("username") String send_name,@Param("key") String key);

    //查询发送方给接收方发的关于key内容的未接受的信息
    @Select ("select * from messages where send_user_name= #{send_name} and isAccept = 0 and info = #{key} and receive_user_name = #{receive_name}")
    public Message findNotAcceptRequestFromTowPerson(@Param("send_name") String send_name,@Param("key") String key,@Param("receive_name") String receive_name);

    //将未接受的加好友信息变为已经接受的信息
    @Update("update messages set isAccept = 1 where id =#{id} and info = #{key}")
    public void handleNotAcceptAddUserMessage(@Param("id") Integer id,@Param("key") String key);
    //拒绝未接受的加好友信息变为已经接受的信息
    @Update("update messages set isAccept = 2 where id =#{id} and info = #{key}")
    public void refuseNotAcceptAddUserMessage(@Param("id") Integer id,@Param("key") String key);

}
