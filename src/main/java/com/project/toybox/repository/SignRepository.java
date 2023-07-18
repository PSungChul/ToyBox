package com.project.toybox.repository;

import com.project.toybox.config.UserDetailsConfig;
import com.project.toybox.entity.Sign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SignRepository extends JpaRepository<Sign, Object> {
    Sign findById(String id);

    Sign findByPhoneNumber(String phoneNumber);

    Sign findByEmail(String email);

    Sign findByIdAndNameAndEmail(String id, String name, String email);

    Sign findByNameAndBirthdayAndPhoneNumber(String name, String birthday, String phoneNumber);

    Sign findByIdAndNameAndBirthdayAndPhoneNumber(String id, String name, String birthday, String phoneNumber);

    @Query("UPDATE Sign s SET s.pwd = :pwd WHERE s.id = :id")
    @Modifying
    void updateByPwd(@Param("id") String id, @Param("pwd") String pwd);

    @Query("UPDATE Sign s SET s.id = :id WHERE s.phoneNumber = :phoneNumber")
    @Modifying
    void updateByNaverId(@Param("id") String id, @Param("phoneNumber") String phoneNumber);

    @Query("UPDATE Sign s SET s.id = :id WHERE s.email = :email")
    @Modifying
    void updateByGoogleId(@Param("id") String id, @Param("email") String email);
}
