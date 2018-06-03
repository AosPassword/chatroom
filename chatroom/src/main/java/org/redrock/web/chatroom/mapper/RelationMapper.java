package org.redrock.web.chatroom.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.redrock.web.chatroom.bean.Relation;

import java.util.List;

@Mapper
public interface RelationMapper {
    @Insert("insert into users_relation (send_invitation_id,accept_invitation_id)" +
            " values (#{send_invitation_id},#{accept_invitation_id})")
    public void insertRelation(@Param("send_invitation_id") long send_invitation_id,@Param("accept_invitation_id") long accept_invitation_id);

    @Select("select * from users_relation where send_invitation_id = #{id} or accept_invitation_id = #{id}")
    public List<Relation> findRelation(@Param("id") long id);
}
