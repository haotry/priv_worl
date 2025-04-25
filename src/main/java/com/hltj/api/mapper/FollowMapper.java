package com.hltj.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hltj.api.model.Follow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FollowMapper extends BaseMapper<Follow> {

    @Select("SELECT COUNT(*) FROM hl_follow WHERE follower_jwcode = #{jwcode}")
    Integer countFollowing(@Param("jwcode") String jwcode);

    @Select("SELECT COUNT(*) FROM hl_follow WHERE followed_jwcode = #{jwcode}")
    Integer countFollowers(@Param("jwcode") String jwcode);

    @Select("SELECT COUNT(*) FROM hl_follow WHERE follower_jwcode = #{followerJwcode} AND followed_jwcode = #{followedJwcode}")
    Integer checkFollowStatus(@Param("followerJwcode") String followerJwcode, @Param("followedJwcode") String followedJwcode);

    @Select("SELECT followed_jwcode FROM hl_follow WHERE follower_jwcode = #{jwcode}")
    List<String> findFollowingJwcodes(@Param("jwcode") String jwcode);
}