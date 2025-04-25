package com.hltj.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hltj.api.model.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM hl_user WHERE jwcode = #{jwcode}")
    User findByJwcode(@Param("jwcode") String jwcode);

    @Select("SELECT * FROM hl_user WHERE tel = #{phone}")
    User findByPhone(@Param("phone") String phone);

    @Update("UPDATE hl_user SET credit = credit + #{credit} WHERE jwcode = #{jwcode}")
    void addCredit(@Param("jwcode") String jwcode, @Param("credit") Integer credit);

    @Update("UPDATE hl_user SET credit = credit - #{credit} WHERE jwcode = #{jwcode}")
    void subtractCredit(@Param("jwcode") String jwcode, @Param("credit") Integer credit);

    @Update("UPDATE hl_user SET avatar = #{avatar} WHERE jwcode = #{jwcode}")
    void updateAvatar(@Param("jwcode") String jwcode, @Param("avatar") String avatar);

    @Update("UPDATE hl_user SET password = #{newPassword} WHERE jwcode = #{jwcode}")
    void updatePassword(@Param("jwcode") String jwcode, @Param("newPassword") String newPassword);

    @Update("UPDATE hl_user SET name = #{name} WHERE jwcode = #{jwcode}")
    void updateName(@Param("jwcode") String jwcode, @Param("name") String name);
}