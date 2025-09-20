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
    .insert([{ ...userData, credits: 2500, max_credits: 2500 }])
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
  // For custom assistants (id = 0), we don't check for duplicates as they are always new
  // For predefined assistants, check for duplicates to prevent re-adding
  const customAssistants = assistants.filter(assistant => assistant.id === 0);
  const predefinedAssistants = assistants.filter(assistant => assistant.id !== 0);
  
  let newAssistants = [...customAssistants]; // Custom assistants are always new
  
  if (predefinedAssistants.length > 0) {
    // Check for existing predefined assistants to prevent duplicates
    const existingAssistants = await getAllUserAssistants(userId);
    const existingAssistantIds = existingAssistants.map(a => a.assistant_id);
    
    // Filter out predefined assistants that already exist
    const newPredefinedAssistants = predefinedAssistants.filter(assistant => 
      !existingAssistantIds.includes(assistant.id)
    );
    
    newAssistants = [...newAssistants, ...newPredefinedAssistants];
  }
  
  if (newAssistants.length === 0) {
    console.log("All assistants already exist, skipping insertion");
    return [];
  }
  
  console.log(`Inserting ${newAssistants.length} new assistants (${assistants.length - newAssistants.length} duplicates skipped)`);
  
  const assistantsWithUserId = newAssistants.map(assistant => {
    // Convert display name to replicate model for storage
    const modelOption = AiModelOptions.find(
      (model) => model.name === assistant.aiModelId
    );
    const replicateModel = modelOption ? modelOption.replicateModel : 'google/gemini-2.0-flash';
    
    // Generate unique ID for custom assistants (id = 0), keep original ID for predefined assistants
    // Use a smaller range suitable for SMALLINT (0-32767)
    const assistantId = assistant.id === 0 ? Math.floor(Math.random() * 30000) + 1000 : assistant.id;
    
    return {
      assistant_id: assistantId,
      name: assistant.name,
      title: assistant.title,
      image: assistant.image,
      instruction: assistant.instruction,
      user_instruction: assistant.userInstruction,
      sample_questions: assistant.sampleQuestions,
      user_id: userId,
      ai_model_id: replicateModel
    };
  });

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
