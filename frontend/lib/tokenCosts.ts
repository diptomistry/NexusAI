// Token cost configuration for different assistant types and operations
export interface TokenCostConfig {
  baseCost: number;
  successMultiplier: number;
  failureCost: number;
}

export const TOKEN_COSTS = {
  // Text-based assistants
  'Code writer': { baseCost: 1, successMultiplier: 1, failureCost: 1 },
  'Personal tutor': { baseCost: 1, successMultiplier: 1, failureCost: 1 },
  'Study helper': { baseCost: 1, successMultiplier: 1, failureCost: 1 },
  'Finance advisor': { baseCost: 1, successMultiplier: 1, failureCost: 1 },
  'Travel planner': { baseCost: 1, successMultiplier: 1, failureCost: 1 },
  'Fitness coach': { baseCost: 1, successMultiplier: 1, failureCost: 1 },
  'Email writer': { baseCost: 1, successMultiplier: 1, failureCost: 1 },
  'Grammar fixer': { baseCost: 1, successMultiplier: 1, failureCost: 1 },
  'YouTube script writer': { baseCost: 1, successMultiplier: 1, failureCost: 1 },
  'Know all': { baseCost: 1, successMultiplier: 1, failureCost: 1 },
  'Bug fixer': { baseCost: 1, successMultiplier: 1, failureCost: 1 },
  
  // Image generation assistants - much higher costs
  'Image editor': { baseCost: 1000, successMultiplier: 1, failureCost: 100 },
  
  // Video generation assistants - highest costs ($0.15 per second)
  'Video generator': { baseCost: 10000, successMultiplier: 1, failureCost: 1000 },
  
  // Default fallback
  'default': { baseCost: 1, successMultiplier: 1, failureCost: 1 }
} as const;

export function calculateTokenCost(
  assistantName: string, 
  response: string, 
  isImageGeneration: boolean = false,
  videoDuration?: number
): number {
  const config = TOKEN_COSTS[assistantName as keyof typeof TOKEN_COSTS] || TOKEN_COSTS.default;
  
  if (isImageGeneration || assistantName === 'Image editor') {
    // Check if response contains image URLs (indicating successful generation)
    const imageUrlRegex = /https?:\/\/[^\s]+\.(jpg|jpeg|png|gif|webp|bmp|tiff|svg)(\?[^\s]*)?/gi;
    const hasImages = imageUrlRegex.test(response);
    
    return hasImages ? config.baseCost : config.failureCost;
  } else if (assistantName === 'Video generator') {
    // For video generation, calculate based on duration ($0.15 per second)
    // Assuming 1 token = $0.0001, so $0.15 = 1500 tokens per second
    if (videoDuration && videoDuration > 0) {
      return Math.round(videoDuration * 1500); // 1500 tokens per second
    }
    return config.baseCost; // Fallback to base cost
  } else {
    // For text assistants, use word-based counting with base cost
    // 1 token = 4 words, so divide word count by 4
    const wordCount = response.trim() ? response.trim().split(/\s+/).length : 0;
    const tokenCount = Math.ceil(wordCount / 4); // Round up to ensure minimum 1 token
    return Math.max(config.baseCost, tokenCount * config.successMultiplier);
  }
}

export function getTokenCostInfo(assistantName: string): string {
  const config = TOKEN_COSTS[assistantName as keyof typeof TOKEN_COSTS] || TOKEN_COSTS.default;
  
  if (assistantName === 'Image editor') {
    return `Image generation: ${config.baseCost} tokens (success) / ${config.failureCost} tokens (attempt)`;
  } else if (assistantName === 'Video generator') {
    return `Video generation: 1500 tokens per second (based on $0.15/second)`;
  } else {
    return `Text generation: ~1 token per 4 words (minimum ${config.baseCost} tokens)`;
  }
}
