package com.usermanagement_service.repository;

import com.usermanagement_service.model.EmergencyContact;
import com.usermanagement_service.model.RequestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends MongoRepository<EmergencyContact, String> {
    List<EmergencyContact> findByContactUserIdAndStatus(String contactUserId, RequestStatus status);
    List<EmergencyContact> findByRequesterIdAndStatus(String requesterId, RequestStatus status);
    boolean existsByRequesterIdAndContactUserId(String requesterId, String contactUserId);
    List<EmergencyContact> findByRequesterId(String requesterId);
    List<EmergencyContact> findByContactUserId(String contactUserId);
    void deleteByRequesterId(String requesterId);
    void deleteByContactUserId(String contactUserId);
    void deleteByRequesterIdAndContactUserId(String requesterId, String contactUserId);
} 