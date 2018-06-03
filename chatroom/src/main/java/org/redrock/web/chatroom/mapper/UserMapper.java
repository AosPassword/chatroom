package org.redrock.web.chatroom.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.redrock.web.chatroom.bean.User;

@Mapper
public interface UserMapper {
    @Select("select * from users where id = #{id}")
    public User findUserById(Integer id);

    @Insert("insert into users (username,password) values (#{username},#{password})")
    public void insertUser(User user);

    @Update("update users set password = #{password} where id =#{id}")
    public User UpdatePassword(Integer id, String password);

    @Select("select * from users where username = #{username} and password = #{password}")
    public User checkUser(User user);

    @Select("select * from users where username = #{username}")
    public User findUserByUsername(String username);


}
