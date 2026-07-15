package com.hutech.demo.service;

import com.hutech.demo.model.RoleEntity;
import com.hutech.demo.model.User;
import com.hutech.demo.repository.IRoleRepository;
import com.hutech.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final IRoleRepository roleRepository;

    public void save(User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRepository.save(user);
    }

    public void setDefaultRole(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            RoleEntity userRole = roleRepository.findByName("USER");
            if (userRole == null) {
                userRole = new RoleEntity(null, "USER", "Regular user");
                roleRepository.save(userRole);
            }
            user.getRoles().add(userRole);
            userRepository.save(user);
        });
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));
    }

    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // Quản lý user (admin)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public boolean toggleLock(Long userId) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) return false;
        User user = opt.get();
        // Không cho khóa admin
        boolean isAdmin = user.getRoles().stream()
            .anyMatch(r -> "ADMIN".equals(r.getName()));
        if (isAdmin) return false;

        user.setLocked(!user.isLocked());
        userRepository.save(user);
        return true;
    }

    public long countUsers() {
        return userRepository.count();
    }
}
