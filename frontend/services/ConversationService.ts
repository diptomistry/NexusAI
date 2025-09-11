import axios from 'axios';
import { Conversation, CreateConversationRequest, AddMessageRequest, ConversationMessage } from '../types/conversation';

const API_BASE_URL = process.env.NEXT_PUBLIC_SPRING_BOOT_URL || 'http://localhost:8080';

export class ConversationService {
  
  // Create a new conversation
  static async createConversation(request: CreateConversationRequest): Promise<Conversation> {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/conversations`, request);
      return response.data;
    } catch (error) {
      console.error('Error creating conversation:', error);
      throw error;
    }
  }

  // Get all conversations for a user
  static async getUserConversations(userId: string): Promise<Conversation[]> {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/conversations/user/${userId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching user conversations:', error);
      throw error;
    }
  }

  // Get conversations for a user by assistant
  static async getUserConversationsByAssistant(userId: string, assistantId: string): Promise<Conversation[]> {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/conversations/user/${userId}/assistant/${assistantId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching conversations by assistant:', error);
      throw error;
    }
  }

  // Get a conversation with all messages
  static async getConversationWithMessages(conversationId: number): Promise<Conversation> {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/conversations/${conversationId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching conversation with messages:', error);
      throw error;
    }
  }

  // Add a message to a conversation
  static async addMessage(request: AddMessageRequest): Promise<ConversationMessage> {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/conversations/messages`, request);
      return response.data;
    } catch (error) {
      console.error('Error adding message to conversation:', error);
      throw error;
    }
  }

  // Delete a conversation
  static async deleteConversation(conversationId: number): Promise<void> {
    try {
      await axios.delete(`${API_BASE_URL}/api/conversations/${conversationId}`);
    } catch (error) {
      console.error('Error deleting conversation:', error);
      throw error;
    }
  }
}
