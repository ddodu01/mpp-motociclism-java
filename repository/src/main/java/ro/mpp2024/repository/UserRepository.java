package ro.mpp2024.repository;

import ro.mpp2024.User;

public interface UserRepository extends Repository<Long, User> {
    User findByUsername(String username);
}