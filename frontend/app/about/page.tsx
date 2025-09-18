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
  Brain,
  Zap,
  Shield,
  Users,
  Target,
  Lightbulb,
  CheckCircle,
  ArrowRight,
  Star,
  Settings,
  Wand2,
} from "lucide-react";
import Image from "next/image";
import Link from "next/link";

export default function AboutPage() {
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
              <Link href="/pricing">
                <Button variant="outline">Pricing</Button>
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
            Custom AI Assistant Platform
          </Badge>
          <h1 className="text-5xl font-bold text-gray-900 mb-6">
            Create Your Own
            <span className="text-blue-600"> Personal AI Assistant</span>
          </h1>
          <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
            NexusAI empowers you to build and customize your own personal AI
            assistant tailored to your specific needs. Create, train, and deploy
            AI assistants that understand your unique workflow and preferences.
          </p>
          <div className="flex justify-center space-x-4">
            <Link href="/ai-assistants">
              <Button size="lg" className="bg-blue-600 hover:bg-blue-700">
                Create Your Assistant
                <ArrowRight className="w-4 h-4 ml-2" />
              </Button>
            </Link>
            <Link href="/pricing">
              <Button size="lg" variant="outline">
                View Pricing
              </Button>
            </Link>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center mb-16">
          <h2 className="text-4xl font-bold text-gray-900 mb-4">
            Why Choose NexusAI?
          </h2>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            We combine advanced AI technology with user-friendly design to
            deliver exceptional value across all your digital needs.
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          <Card className="border-0 shadow-lg hover:shadow-xl transition-shadow">
            <CardHeader>
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4">
                <Wand2 className="w-6 h-6 text-blue-600" />
              </div>
              <CardTitle>Custom Creation</CardTitle>
              <CardDescription>
                Build your own AI assistant from scratch with custom
                instructions, personality, and specialized knowledge for your
                specific needs.
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="border-0 shadow-lg hover:shadow-xl transition-shadow">
            <CardHeader>
              <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mb-4">
                <Zap className="w-6 h-6 text-green-600" />
              </div>
              <CardTitle>Lightning Fast</CardTitle>
              <CardDescription>
                Get instant responses and results. Our optimized infrastructure
                ensures minimal latency for all operations.
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="border-0 shadow-lg hover:shadow-xl transition-shadow">
            <CardHeader>
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mb-4">
                <Shield className="w-6 h-6 text-purple-600" />
              </div>
              <CardTitle>Secure & Private</CardTitle>
              <CardDescription>
                Your data is protected with enterprise-grade security. We never
                share your information with third parties.
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="border-0 shadow-lg hover:shadow-xl transition-shadow">
            <CardHeader>
              <div className="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center mb-4">
                <Settings className="w-6 h-6 text-orange-600" />
              </div>
              <CardTitle>Easy Customization</CardTitle>
              <CardDescription>
                Intuitive interface for customizing your AI assistant's
                behavior, responses, and capabilities without any coding
                knowledge.
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="border-0 shadow-lg hover:shadow-xl transition-shadow">
            <CardHeader>
              <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center mb-4">
                <Target className="w-6 h-6 text-red-600" />
              </div>
              <CardTitle>Precision & Accuracy</CardTitle>
              <CardDescription>
                Our AI assistants deliver highly accurate results tailored to
                your specific requirements and context.
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="border-0 shadow-lg hover:shadow-xl transition-shadow">
            <CardHeader>
              <div className="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center mb-4">
                <Lightbulb className="w-6 h-6 text-yellow-600" />
              </div>
              <CardTitle>Continuous Learning</CardTitle>
              <CardDescription>
                Your AI assistant learns and adapts from your interactions,
                becoming more personalized and effective over time.
              </CardDescription>
            </CardHeader>
          </Card>
        </div>
      </div>

      {/* How It Works Section */}
      <div className="bg-white py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              How to Create Your Personal AI Assistant
            </h2>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              Building your custom AI assistant is simple and intuitive.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <div className="text-center p-6">
              <div className="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-white">1</span>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-3">
                Define Your Assistant
              </h3>
              <p className="text-gray-600">
                Choose a name, personality, and specific role for your AI
                assistant. Set the tone and style of responses you prefer.
              </p>
            </div>

            <div className="text-center p-6">
              <div className="w-16 h-16 bg-green-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-white">2</span>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-3">
                Customize Instructions
              </h3>
              <p className="text-gray-600">
                Write detailed instructions about what your assistant should do,
                how it should behave, and what knowledge it should have.
              </p>
            </div>

            <div className="text-center p-6">
              <div className="w-16 h-16 bg-purple-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-white">3</span>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-3">
                Start Using
              </h3>
              <p className="text-gray-600">
                Your custom AI assistant is ready! Start chatting and watch it
                learn and adapt to your specific needs and preferences.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Stats Section */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center text-white mb-12">
            <h2 className="text-4xl font-bold mb-4">
              Trusted by Users Worldwide
            </h2>
            <p className="text-xl opacity-90">
              Join thousands of satisfied users who rely on NexusAI for their
              daily tasks
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-8 text-center">
            <div>
              <div className="text-4xl font-bold text-white mb-2">
                Unlimited
              </div>
              <div className="text-blue-100">Custom Assistants</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-white mb-2">99.9%</div>
              <div className="text-blue-100">Uptime</div>
            </div>
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="bg-white py-16">
        <div className="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
          <h2 className="text-4xl font-bold text-gray-900 mb-6">
            Ready to Create Your Personal AI Assistant?
          </h2>
          <p className="text-xl text-gray-600 mb-8">
            Start building your custom AI assistant today and experience the
            power of personalized AI that truly understands your needs.
          </p>
          <div className="flex justify-center space-x-4">
            <Link href="/ai-assistants">
              <Button size="lg" className="bg-blue-600 hover:bg-blue-700">
                Create Your Assistant
                <ArrowRight className="w-4 h-4 ml-2" />
              </Button>
            </Link>
            <Link href="/pricing">
              <Button size="lg" variant="outline">
                View Pricing Plans
              </Button>
            </Link>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="bg-gray-900 text-white py-12">
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
                Create your own personal AI assistant tailored to your needs.
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
          <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400">
            <p>&copy; 2024 NexusAI. All rights reserved.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
