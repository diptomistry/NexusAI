import { supabase, User, UserAiAssistant } from '@/lib/supabase'
import AiModelOptions from './AiModelOptions'

// User functions
export const createUser = async (userData: {
  name: string
  email: string
  picture: string
}): Promise<User | null> => {
  // Check if user exists
  const { data: existingUser } = await supabase
    .from('users')
    .select('*')
    .eq('email', userData.email)
    .single()

  if (existingUser) return existingUser

  // Create new user
  const { data, error } = await supabase
    .from('users')
    .insert([{ ...userData, credits: 5000, max_credits: 5000 }])
    .select()
    .single()

  if (error) throw error
  return data
}

export const getUser = async (email: string): Promise<User | null> => {
  const { data, error } = await supabase
    .from('users')
    .select('*')
    .eq('email', email)
    .single()

  if (error) return null
  return data
}

export const updateUserTokens = async (
  userId: string,
  credits: number,
  orderId?: string
): Promise<void> => {
  const updateData: any = { credits }
  if (orderId) updateData.order_id = orderId

  const { error } = await supabase
    .from('users')
    .update(updateData)
    .eq('id', userId)

  if (error) throw error
}

// Assistant functions
export const insertSelectedAssistants = async (
  assistants: any[],
  userId: string
): Promise<string[]> => {
  const assistantsWithUserId = assistants.map(assistant => {
    // Convert display name to replicate model for storage
    const modelOption = AiModelOptions.find(
      (model) => model.name === assistant.aiModelId
    );
    const replicateModel = modelOption ? modelOption.replicateModel : 'google/gemini-2.0-flash';
    
    return {
      assistant_id: assistant.id,
      name: assistant.name,
      title: assistant.title,
      image: assistant.image,
      instruction: assistant.instruction,
      user_instruction: assistant.userInstruction,
      sample_questions: assistant.sampleQuestions,
      user_id: userId,
      ai_model_id: replicateModel
    };
  })

  const { data, error } = await supabase
    .from('user_ai_assistants')
    .insert(assistantsWithUserId)
    .select('id')

  if (error) throw error
  return data.map(item => item.id)
}

export const getAllUserAssistants = async (userId: string): Promise<UserAiAssistant[]> => {
  const { data, error } = await supabase
    .from('user_ai_assistants')
    .select('*')
    .eq('user_id', userId)
    .order('created_at', { ascending: false })

  if (error) throw error
  return data || []
}

export const updateUserAiAssistant = async (
  id: string,
  userInstruction: string,
  aiModelId: string
): Promise<void> => {
  // Convert display name to replicate model for storage
  const modelOption = AiModelOptions.find(
    (model) => model.name === aiModelId
  );
  const replicateModel = modelOption ? modelOption.replicateModel : 'google/gemini-2.0-flash';
  
  const { error } = await supabase
    .from('user_ai_assistants')
    .update({
      user_instruction: userInstruction,
      ai_model_id: replicateModel
    })
    .eq('id', id)

  if (error) throw error
}

export const deleteAssistant = async (id: string): Promise<void> => {
  const { error } = await supabase
    .from('user_ai_assistants')
    .delete()
    .eq('id', id)

  if (error) throw error
}
