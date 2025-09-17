export default [
  {
    id: 1,
    name: "Google: Gemini 2.0 Flash",
    replicateModel: "google/gemini-2.0-flash", // Use Gemini directly, not through Replicate
    logo: "/google.png",
  },
  {
    id: 2,
    name: "OpenAI: GPT-4o-mini",
    replicateModel: "openai/o4-mini",
    logo: "/chatgpt.png",
  },
  {
    id: 3,
    name: "OpenAI: GPT-5",
    replicateModel: "openai/gpt-5",
    logo: "/chatgpt.png",
  },
  {
    id: 4,
    name: "Anthropic: Claude 4 Sonnet",
    replicateModel: "anthropic/claude-4-sonnet",
    logo: "/anthropic.png",
  },
  {
    id: 5,
    name: "Anthropic: Claude 3.7 Sonnet",
    replicateModel: "anthropic/claude-3.7-sonnet",
    logo: "/anthropic.png",
  },
  {
    id: 6,
    name: "DeepSeek: DeepSeek V3",
    replicateModel: "deepseek-ai/deepseek-v3",
    logo: "/deepseek.png",
  },
  // Image generation and editing models
  {
    id: 102,
    name: "Flux Kontext Max (Text Effects)",
    replicateModel: "black-forest-labs/flux-kontext-max",
    logo: "/flux.svg",
    imageOnly: true, // Keep this for now to show only in Image editor
  },
  {
    id: 103,
    name: "Qwen: Image Edit",
    replicateModel: "qwen/qwen-image-edit",
    logo: "/qwen-color.svg",
    imageOnly: true, // Keep this for now to show only in Image editor
  },
  // Video generation models
  {
    id: 201,
    name: "Seedance-1-Pro",
    replicateModel: "bytedance/seedance-1-pro",
    logo: "/video-generate.jpg",
    videoOnly: true, // Show only in Video generator
  },
];
