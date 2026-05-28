package com.wyx.enrollment_promotion_systemmaster.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyx.enrollment_promotion_systemmaster.entity.User;
import com.wyx.enrollment_promotion_systemmaster.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user.getRole())
        );
    }

    private java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities(Integer role) {
        String roleName = "";
        switch (role) {
            case 1:
                roleName = "ROLE_STUDENT";
                break;
            case 2:
                roleName = "ROLE_TEACHER";
                break;
            case 3:
                roleName = "ROLE_COLLEGE_ADMIN";
                break;
            case 4:
                roleName = "ROLE_SCHOOL_ADMIN";
                break;
            default:
                roleName = "ROLE_USER";
        }
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }
}
