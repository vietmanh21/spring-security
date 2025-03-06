package com.manhnv.security.service;

import com.manhnv.security.dto.UserResponse;
import com.manhnv.security.exception.CustomException;
import com.manhnv.security.model.Role;
import com.manhnv.security.model.User;
import com.manhnv.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse profile(String email) {
        UserResponse response = new UserResponse();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));
        BeanUtils.copyProperties(user, response);
        response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return response;
    }

    public List<UserResponse> listUser() {
        List<UserResponse> listUser = new ArrayList<>();
        userRepository.findAll().forEach(user -> {
            UserResponse response = new UserResponse();
            BeanUtils.copyProperties(user, response);
            response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
            listUser.add(response);
        });
        return listUser;
    }
}
