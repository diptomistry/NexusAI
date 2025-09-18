import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Check,
  X,
  Zap,
  Crown,
  ArrowRight,
  Star,
  Calculator,
  Clock,
  Shield,
  Users,
  Brain,
  Image as ImageIcon,
  Video,
  FileText,
} from "lucide-react";
import Image from "next/image";
import Link from "next/link";

export default function PricingPage() {
  const features = [
    {
      name: "Text Generation",
      free: "2,500 tokens",
      pro: "30,000 tokens",
      description: "~1 token per 4 words (minimum 1 token)",
    },
    {
      name: "Image Generation",
      free: "1,000 tokens per image",
      pro: "1,000 tokens per image",
      description: "1,000 tokens (success) / 100 tokens (attempt)",
    },
    {
      name: "Video Generation",
      free: "Not available",
      pro: "1,500 tokens per second",
      description: "Based on $0.15/second pricing",
    },
    {
      name: "Custom AI Assistants",
      free: "Create unlimited assistants",
      pro: "Create unlimited assistants",
      description: "Build and customize your own personal AI assistants",
    },
    {
      name: "Support",
      free: "Community",
      pro: "Priority support",
      description: "Email support with faster response times",
    },
  ];

  const assistants = [
    { name: "Code Writer", icon: FileText, category: "Productivity" },
    { name: "Personal Tutor", icon: Brain, category: "Education" },
    { name: "Finance Advisor", icon: Calculator, category: "Specialized" },
    { name: "Fitness Coach", icon: Users, category: "Health" },
    { name: "Email Writer", icon: FileText, category: "Productivity" },
    { name: "Grammar Fixer", icon: FileText, category: "Productivity" },
    { name: "YouTube Script Writer", icon: FileText, category: "Content" },
    { name: "Bug Fixer", icon: FileText, category: "Development" },
    { name: "Image Editor", icon: ImageIcon, category: "Creative" },
    { name: "Video Generator", icon: Video, category: "Creative" },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-2">
              <Image
                src="/NesusAI.png"
                alt="NexusAI Logo"
                width={40}
                height={40}
                className="rounded-lg"
              />
              <h1 className="text-2xl font-bold text-gray-900">NexusAI</h1>
            </div>
            <div className="flex space-x-4">
              <Link href="/">
                <Button variant="ghost">Home</Button>
              </Link>
              <Link href="/about">
                <Button variant="ghost">About</Button>
              </Link>
              <Link href="/ai-assistants">
                <Button>Get Started</Button>
              </Link>
            </div>
          </div>
        </div>
      </div>

      {/* Hero Section */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center">
          <Badge variant="secondary" className="mb-4">
            <Star className="w-4 h-4 mr-1" />
            Simple, Transparent Pricing
          </Badge>
          <h1 className="text-5xl font-bold text-gray-900 mb-6">
            Choose Your Perfect Plan
          </h1>
          <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
            Start free and upgrade when you need more power. All plans include
            the ability to create and customize your own personal AI assistant
            with transparent token-based pricing.
          </p>
        </div>
      </div>

      {/* Pricing Cards */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-16">
        <div className="grid md:grid-cols-2 gap-8 max-w-5xl mx-auto">
          {/* Free Plan */}
          <Card className="border-2 border-gray-200 hover:border-gray-300 transition-colors">
            <CardHeader className="text-center pb-8">
              <div className="flex justify-center mb-4">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center">
                  <Zap className="w-8 h-8 text-gray-600" />
                </div>
              </div>
              <CardTitle className="text-2xl">Free Plan</CardTitle>
              <CardDescription className="text-lg">
                Perfect for getting started
              </CardDescription>
              <div className="mt-4">
                <span className="text-4xl font-bold">৳0</span>
                <span className="text-gray-600">/month</span>
              </div>
            </CardHeader>
            <CardContent>
              <ul className="space-y-4 mb-8">
                <li className="flex items-center">
                  <Check className="w-5 h-5 text-green-500 mr-3" />
                  <span>2,500 tokens per month</span>
                </li>
                <li className="flex items-center">
                  <Check className="w-5 h-5 text-green-500 mr-3" />
                  <span>Create custom AI assistants</span>
                </li>
                <li className="flex items-center">
                  <Check className="w-5 h-5 text-green-500 mr-3" />
                  <span>Image generation (1,000 tokens/image)</span>
                </li>
                <li className="flex items-center">
                  <Check className="w-5 h-5 text-green-500 mr-3" />
                  <span>Basic support</span>
                </li>
                <li className="flex items-center">
                  <Check className="w-5 h-5 text-green-500 mr-3" />
                  <span>Community access</span>
                </li>
                <li className="flex items-center">
                  <X className="w-5 h-5 text-gray-400 mr-3" />
                  <span className="text-gray-400">Video generation</span>
                </li>
              </ul>
              <Link href="/ai-assistants">
                <Button className="w-full" variant="outline">
                  Get Started Free
                </Button>
              </Link>
            </CardContent>
          </Card>

          {/* Pro Plan */}
          <Card className="border-2 border-blue-500 hover:border-blue-600 transition-colors relative">
            <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
              <Badge className="bg-blue-600 text-white px-4 py-1">
                <Crown className="w-4 h-4 mr-1" />
                Most Popular
              </Badge>
            </div>
            <CardHeader className="text-center pb-8 pt-8">
              <div className="flex justify-center mb-4">
                <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center">
                  <Crown className="w-8 h-8 text-blue-600" />
                </div>
              </div>
              <CardTitle className="text-2xl">Pro Plan</CardTitle>
              <CardDescription className="text-lg">
                For power users and professionals
              </CardDescription>
              <div className="mt-4">
                <span className="text-4xl font-bold">৳2,000</span>
                <span className="text-gray-600">/month</span>
              </div>
            </CardHeader>
            <CardContent>
              <ul className="space-y-4 mb-8">
                <li className="flex items-center">
                  <Check className="w-5 h-5 text-green-500 mr-3" />
                  <span>30,000 tokens per month</span>
                </li>
                <li className="flex items-center">
                  <Check className="w-5 h-5 text-green-500 mr-3" />
                  <span>Unlimited custom AI assistants</span>
                </li>
                <li className="flex items-center">
                  <Check className="w-5 h-5 text-green-500 mr-3" />
                  <span>Image generation (1,000 tokens/image)</span>
                </li>
                <li className="flex items-center">
                  <Check className="w-5 h-5 text-green-500 mr-3" />
                  <span>Video generation (1,500 tokens/second)</span>
                </li>
                <li className="flex items-center">
                  <Check className="w-5 h-5 text-green-500 mr-3" />
                  <span>Priority support</span>
                </li>
              </ul>
              <Link href="/ai-assistants">
                <Button className="w-full bg-blue-600 hover:bg-blue-700">
                  Upgrade to Pro
                  <ArrowRight className="w-4 h-4 ml-2" />
                </Button>
              </Link>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Token Usage Calculator */}
      <div className="bg-white py-16">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Token Usage Calculator
            </h2>
            <p className="text-xl text-gray-600">
              Estimate how many tokens you'll need based on your usage patterns.
              Both Free and Pro users can generate images and text.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <div className="text-center p-6 border rounded-lg">
              <FileText className="w-12 h-12 text-blue-600 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Text Generation</h3>
              <p className="text-sm text-gray-600 mb-4">~1 token per 4 words</p>
              <div className="text-2xl font-bold text-blue-600">
                1,000 words = 250 tokens
              </div>
            </div>

            <div className="text-center p-6 border rounded-lg">
              <ImageIcon className="w-12 h-12 text-green-600 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Image Generation</h3>
              <p className="text-sm text-gray-600 mb-4">
                Per successful generation
              </p>
              <div className="text-2xl font-bold text-green-600">
                1 image = 1,000 tokens
              </div>
            </div>

            <div className="text-center p-6 border rounded-lg">
              <Video className="w-12 h-12 text-purple-600 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Video Generation</h3>
              <p className="text-sm text-gray-600 mb-4">Based on duration</p>
              <div className="text-2xl font-bold text-purple-600">
                1 second = 1,500 tokens
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Features Comparison */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center mb-12">
          <h2 className="text-4xl font-bold text-gray-900 mb-4">
            Detailed Feature Comparison
          </h2>
          <p className="text-xl text-gray-600">
            See exactly what's included in each plan
          </p>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full border-collapse border border-gray-300">
            <thead>
              <tr className="bg-gray-50">
                <th className="border border-gray-300 px-6 py-4 text-left font-semibold">
                  Feature
                </th>
                <th className="border border-gray-300 px-6 py-4 text-center font-semibold">
                  Free Plan
                </th>
                <th className="border border-gray-300 px-6 py-4 text-center font-semibold">
                  Pro Plan
                </th>
              </tr>
            </thead>
            <tbody>
              {features.map((feature, index) => (
                <tr
                  key={index}
                  className={index % 2 === 0 ? "bg-white" : "bg-gray-50"}
                >
                  <td className="border border-gray-300 px-6 py-4">
                    <div>
                      <div className="font-semibold">{feature.name}</div>
                      <div className="text-sm text-gray-600">
                        {feature.description}
                      </div>
                    </div>
                  </td>
                  <td className="border border-gray-300 px-6 py-4 text-center">
                    <span
                      className={
                        feature.free === "Not available"
                          ? "text-gray-400"
                          : "text-green-600"
                      }
                    >
                      {feature.free}
                    </span>
                  </td>
                  <td className="border border-gray-300 px-6 py-4 text-center">
                    <span className="text-green-600 font-semibold">
                      {feature.pro}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* AI Assistants Showcase */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center text-white mb-12">
            <h2 className="text-4xl font-bold mb-4">
              Create Your Own AI Assistants
            </h2>
            <p className="text-xl opacity-90">
              Build unlimited custom AI assistants tailored to your specific
              needs
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-5 gap-6">
            {assistants.map((assistant, index) => {
              const IconComponent = assistant.icon;
              return (
                <div
                  key={index}
                  className="bg-white/10 backdrop-blur-sm rounded-lg p-4 text-center"
                >
                  <IconComponent className="w-8 h-8 text-white mx-auto mb-2" />
                  <h3 className="font-semibold text-white mb-1">
                    {assistant.name}
                  </h3>
                  <p className="text-sm text-blue-100">{assistant.category}</p>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="bg-gray-900 text-white py-16">
        <div className="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
          <h2 className="text-4xl font-bold mb-6">
            Ready to Create Your AI Assistant?
          </h2>
          <p className="text-xl text-gray-300 mb-8">
            Join thousands of users who are already building their own custom AI
            assistants.
          </p>
          <div className="flex justify-center space-x-4">
            <Link href="/ai-assistants">
              <Button size="lg" className="bg-blue-600 hover:bg-blue-700">
                Start Free Trial
                <ArrowRight className="w-4 h-4 ml-2" />
              </Button>
            </Link>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="bg-gray-800 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid md:grid-cols-4 gap-8">
            <div>
              <div className="flex items-center space-x-2 mb-4">
                <Image
                  src="/NesusAI.png"
                  alt="NexusAI Logo"
                  width={32}
                  height={32}
                  className="rounded-lg"
                />
                <h3 className="text-xl font-bold">NexusAI</h3>
              </div>
              <p className="text-gray-400">
                Empowering your digital journey with intelligent AI assistants.
              </p>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Product</h4>
              <ul className="space-y-2 text-gray-400">
                <li>
                  <Link href="/ai-assistants" className="hover:text-white">
                    AI Assistants
                  </Link>
                </li>
                <li>
                  <Link href="/pricing" className="hover:text-white">
                    Pricing
                  </Link>
                </li>
                <li>
                  <Link href="/about" className="hover:text-white">
                    About
                  </Link>
                </li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-700 mt-8 pt-8 text-center text-gray-400">
            <p>&copy; 2024 NexusAI. All rights reserved.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
