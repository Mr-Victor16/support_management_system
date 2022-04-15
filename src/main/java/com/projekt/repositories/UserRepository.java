package com.projekt.repositories;

import com.projekt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);

    User findByUsernameOrEmail(String username, String email);

    @Query("Select u From User u Where (lower(u.name) like lower(concat('%',?1,'%')) or (lower(u.surname) like lower(concat('%',?1,'%'))) or (lower(u.username) like lower(concat('%',?1,'%'))) )")
    ArrayList<User> searchUserByNameSurnameUsername(String phrase);

    @Query("select u from User u WHERE (lower(u.email) = lower(:email))")
    ArrayList<User> searchUserByEmail(@Param("email")String email);

    ArrayList<User> findByRoles_Id(Integer id);

}
