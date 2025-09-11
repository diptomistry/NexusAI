package com.example.ai_assistant_backend.service;

import com.example.ai_assistant_backend.dto.*;
import com.example.ai_assistant_backend.model.Conversation;
import com.example.ai_assistant_backend.model.Message;
import com.example.ai_assistant_backend.repository.ConversationRepository;
import com.example.ai_assistant_backend.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    public ConversationDTO createConversation(CreateConversationRequest request) {
        Conversation conversation = new Conversation(
                request.getUserId(),
                request.getAssistantId(),
                request.getTitle());

        Conversation saved = conversationRepository.save(conversation);
        return convertToDTO(saved);
    }

    public List<ConversationDTO> getUserConversations(String userId) {
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ConversationDTO> getUserConversationsByAssistant(String userId, String assistantId) {
        List<Conversation> conversations = conversationRepository
                .findByUserIdAndAssistantIdOrderByUpdatedAtDesc(userId, assistantId);
        return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ConversationDTO getConversationWithMessages(Long conversationId) {
        Conversation conversation = conversationRepository.findByIdWithMessages(conversationId);
        if (conversation == null) {
            throw new RuntimeException("Conversation not found with id: " + conversationId);
        }
        return convertToDTOWithMessages(conversation);
    }

    public MessageDTO addMessage(AddMessageRequest request) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(
                        () -> new RuntimeException("Conversation not found with id: " + request.getConversationId()));

        Message message = new Message(conversation, request.getRole(), request.getContent());
        Message saved = messageRepository.save(message);

        // Update conversation's updated_at timestamp
        conversation.preUpdate();
        conversationRepository.save(conversation);

        return convertMessageToDTO(saved);
    }

    public void deleteConversation(Long conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new RuntimeException("Conversation not found with id: " + conversationId);
        }
        conversationRepository.deleteById(conversationId);
    }

    private ConversationDTO convertToDTO(Conversation conversation) {
        return new ConversationDTO(
                conversation.getId(),
                conversation.getUserId(),
                conversation.getAssistantId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt());
    }

    private ConversationDTO convertToDTOWithMessages(Conversation conversation) {
        ConversationDTO dto = convertToDTO(conversation);
        if (conversation.getMessages() != null) {
            List<MessageDTO> messageDTOs = conversation.getMessages().stream()
                    .map(this::convertMessageToDTO)
                    .collect(Collectors.toList());
            dto.setMessages(messageDTOs);
        }
        return dto;
    }

    private MessageDTO convertMessageToDTO(Message message) {
        return new MessageDTO(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt());
    }
}
