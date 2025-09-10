import { createClient } from '@supabase/supabase-js'

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://placeholder.supabase.co'
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || 'placeholder-key'

export const supabase = createClient(supabaseUrl, supabaseAnonKey)

// Types
export type User = {
  id: string
  name: string
  email: string
  picture: string
  credits: number
  order_id?: string
  created_at: string
  updated_at: string
}

export type UserAiAssistant = {
  id: string
  assistant_id: number
  name: string
  title: string
  image: string
  instruction: string
  user_instruction: string
  sample_questions: string[]
  ai_model_id?: string
  user_id: string
  created_at: string
  updated_at: string
}
