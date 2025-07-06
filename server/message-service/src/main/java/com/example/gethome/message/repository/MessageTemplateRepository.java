package com.example.gethome.message.repository;

import com.example.gethome.message.model.MessageTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageTemplateRepository extends MongoRepository<MessageTemplate, String> {
    
    List<MessageTemplate> findByType(MessageTemplate.TemplateType type);
    
    List<MessageTemplate> findByIsActive(boolean isActive);
    
    Optional<MessageTemplate> findByNameAndIsActive(String name, boolean isActive);
    
    List<MessageTemplate> findByTypeAndIsActive(MessageTemplate.TemplateType type, boolean isActive);
} 