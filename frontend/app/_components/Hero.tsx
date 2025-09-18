import { AnimatedGridPattern } from "@/components/magicui/animated-grid-pattern";
import React from "react";
import Heder from "./Heder";
import { AuroraText } from "@/components/magicui/aurora-text";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import Image from "next/image";
import HeroVideoDialog from "@/components/magicui/hero-video-dialog";
import { AnimatedGradientText } from "@/components/magicui/animated-gradient-text";
import {
  ChevronRight,
  ArrowRight,
  Sparkles,
  Zap,
  Brain,
  Users,
  Star,
} from "lucide-react";

function Hero() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 dark:from-slate-900 dark:via-slate-800 dark:to-indigo-900">
      <Heder />

      {/* Hero Section */}
      <section className="relative overflow-hidden">
        {/* Background Elements */}
        <div className="absolute inset-0">
          <AnimatedGridPattern
            numSquares={30}
            maxOpacity={0.05}
            duration={3}
            repeatDelay={1}
            className="[mask-image:radial-gradient(800px_circle_at_center,white,transparent)]
                        inset-x-0 inset-y-[-30%] h-[200%] skew-y-12"
          />
          {/* Gradient Orbs */}
          <div className="absolute top-20 left-10 w-72 h-72 bg-gradient-to-r from-blue-400/20 to-purple-400/20 rounded-full blur-3xl animate-pulse"></div>
          <div className="absolute bottom-20 right-10 w-96 h-96 bg-gradient-to-r from-pink-400/20 to-orange-400/20 rounded-full blur-3xl animate-pulse delay-1000"></div>
        </div>

        <div className="relative container mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-32">
          <div className="text-center max-w-6xl mx-auto">
            {/* Announcement Badge */}
            <Link href="/ai-assistants">
              <div className="group relative mx-auto flex items-center justify-center rounded-full px-6 py-3 shadow-[inset_0_-8px_10px_#8fdfff1f] transition-all duration-500 ease-out hover:shadow-[inset_0_-5px_10px_#8fdfff3f] bg-white/80 backdrop-blur-sm border border-blue-200/50 w-fit mb-8 cursor-pointer hover:scale-105">
                <span
                  className="absolute inset-0 block h-full w-full animate-gradient rounded-[inherit] bg-gradient-to-r from-[#ffaa40]/50 via-[#9c40ff]/50 to-[#ffaa40]/50 bg-[length:300%_100%] p-[1px]"
                  style={{
                    WebkitMask:
                      "linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)",
                    WebkitMaskComposite: "destination-out",
                    mask: "linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)",
                    maskComposite: "subtract",
                    WebkitClipPath: "padding-box",
                  }}
                />
                <Sparkles className="w-4 h-4 text-yellow-500 mr-2" />
                <AnimatedGradientText className="text-sm font-medium">
                  Introducing Personal AI Assistant
                </AnimatedGradientText>
                <ChevronRight className="ml-2 size-4 stroke-neutral-500 transition-transform duration-300 ease-in-out group-hover:translate-x-0.5" />
              </div>
            </Link>

            {/* Main Heading */}
            <h1 className="text-5xl md:text-6xl lg:text-8xl font-bold tracking-tight mb-8">
              <span className="block text-slate-900 dark:text-white mb-4">
                Your Personal
              </span>
              <span className="block">
                <AuroraText className="text-6xl md:text-7xl lg:text-9xl">
                  AI Assistant
                </AuroraText>
              </span>
            </h1>

            {/* Subheading */}
            <p className="text-xl md:text-2xl text-slate-600 dark:text-slate-300 mb-12 max-w-3xl mx-auto leading-relaxed">
              Experience the future of AI-powered assistance. Get personalized
              help, intelligent insights, and seamless automation tailored just
              for you.
            </p>

            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-16">
              <Link href="/sign-in">
                <Button
                  size="lg"
                  className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white px-8 py-4 text-lg font-semibold shadow-2xl hover:shadow-3xl transition-all duration-300 transform hover:scale-105"
                >
                  <Zap className="w-5 h-5 mr-2" />
                  Get Started Free
                  <ArrowRight className="w-5 h-5 ml-2" />
                </Button>
              </Link>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-3xl mx-auto mb-16">
              <div className="text-center">
                <div className="text-3xl md:text-4xl font-bold text-purple-600 dark:text-purple-400 mb-2">
                  Custom
                </div>
                <div className="text-slate-600 dark:text-slate-300">
                  Personal AI Assistant
                </div>
              </div>
              <div className="text-center">
                <div className="text-3xl md:text-4xl font-bold text-green-600 dark:text-green-400 mb-2">
                  99.9%
                </div>
                <div className="text-slate-600 dark:text-slate-300">Uptime</div>
              </div>
            </div>

            {/* Video Section */}
            <div className="max-w-4xl mx-auto">
              <HeroVideoDialog
                className="block dark:hidden"
                animationStyle="from-center"
                videoSrc="https://www.youtube.com/embed/uBgFPmieR6M?si=5x9XfVImGJnrU5kw"
                thumbnailSrc="/thumbnail.png"
                thumbnailAlt="Hero Video"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Features Preview Section */}
      <section className="py-20 bg-white/50 dark:bg-slate-900/50 backdrop-blur-sm">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold text-slate-900 dark:text-white mb-6">
              Why Choose NEXUS AI?
            </h2>
            <p className="text-xl text-slate-600 dark:text-slate-300 max-w-3xl mx-auto">
              Experience the power of personalized AI assistance designed to
              understand and adapt to your unique needs.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
            <div className="text-center p-8 rounded-2xl bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-slate-800 dark:to-slate-700 border border-blue-200/50 dark:border-slate-600/50 hover:shadow-xl transition-all duration-300">
              <div className="w-16 h-16 bg-gradient-to-r from-blue-500 to-purple-500 rounded-2xl flex items-center justify-center mx-auto mb-6">
                <Brain className="w-8 h-8 text-white" />
              </div>
              <h3 className="text-2xl font-bold text-slate-900 dark:text-white mb-4">
                Intelligent & Adaptive
              </h3>
              <p className="text-slate-600 dark:text-slate-300">
                Our AI learns from your interactions to provide increasingly
                personalized and relevant assistance.
              </p>
            </div>

            <div className="text-center p-8 rounded-2xl bg-gradient-to-br from-purple-50 to-pink-50 dark:from-slate-800 dark:to-slate-700 border border-purple-200/50 dark:border-slate-600/50 hover:shadow-xl transition-all duration-300">
              <div className="w-16 h-16 bg-gradient-to-r from-purple-500 to-pink-500 rounded-2xl flex items-center justify-center mx-auto mb-6">
                <Users className="w-8 h-8 text-white" />
              </div>
              <h3 className="text-2xl font-bold text-slate-900 dark:text-white mb-4">
                Create Your Own
              </h3>
              <p className="text-slate-600 dark:text-slate-300">
                Build and customize your personal AI assistant tailored to your
                specific needs and preferences.
              </p>
            </div>

            <div className="text-center p-8 rounded-2xl bg-gradient-to-br from-green-50 to-emerald-50 dark:from-slate-800 dark:to-slate-700 border border-green-200/50 dark:border-slate-600/50 hover:shadow-xl transition-all duration-300">
              <div className="w-16 h-16 bg-gradient-to-r from-green-500 to-emerald-500 rounded-2xl flex items-center justify-center mx-auto mb-6">
                <Star className="w-8 h-8 text-white" />
              </div>
              <h3 className="text-2xl font-bold text-slate-900 dark:text-white mb-4">
                Premium Quality
              </h3>
              <p className="text-slate-600 dark:text-slate-300">
                Built with cutting-edge technology and designed for reliability,
                security, and performance.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonials Section */}
      <section className="py-20 bg-gradient-to-br from-slate-900 via-blue-900 to-purple-900 dark:from-slate-950 dark:via-slate-900 dark:to-indigo-950">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold text-white mb-6">
              What Our Users Say
            </h2>
            <p className="text-xl text-blue-100 max-w-3xl mx-auto">
              Join thousands of satisfied users who have transformed their
              productivity with NEXUS AI.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
            <div className="bg-white/10 backdrop-blur-sm rounded-2xl p-8 border border-white/20 hover:bg-white/15 transition-all duration-300">
              <div className="flex items-center mb-6">
                <div className="w-12 h-12 bg-gradient-to-r from-blue-400 to-purple-400 rounded-full flex items-center justify-center text-white font-bold text-lg">
                  S
                </div>
                <div className="ml-4">
                  <h4 className="text-white font-semibold">Sarah Chen</h4>
                  <p className="text-blue-200 text-sm">Product Manager</p>
                </div>
              </div>
              <p className="text-blue-100 leading-relaxed">
                "NEXUS AI has revolutionized how I manage my daily tasks. The
                personalized assistants understand my workflow and provide
                exactly what I need, when I need it."
              </p>
              <div className="flex text-yellow-400 mt-4">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} className="w-5 h-5 fill-current" />
                ))}
              </div>
            </div>

            <div className="bg-white/10 backdrop-blur-sm rounded-2xl p-8 border border-white/20 hover:bg-white/15 transition-all duration-300">
              <div className="flex items-center mb-6">
                <div className="w-12 h-12 bg-gradient-to-r from-green-400 to-blue-400 rounded-full flex items-center justify-center text-white font-bold text-lg">
                  M
                </div>
                <div className="ml-4">
                  <h4 className="text-white font-semibold">
                    Michael Rodriguez
                  </h4>
                  <p className="text-blue-200 text-sm">Software Engineer</p>
                </div>
              </div>
              <p className="text-blue-100 leading-relaxed">
                "The AI assistants are incredibly intelligent and adaptive.
                They've helped me debug code, write documentation, and even plan
                my projects more effectively."
              </p>
              <div className="flex text-yellow-400 mt-4">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} className="w-5 h-5 fill-current" />
                ))}
              </div>
            </div>

            <div className="bg-white/10 backdrop-blur-sm rounded-2xl p-8 border border-white/20 hover:bg-white/15 transition-all duration-300">
              <div className="flex items-center mb-6">
                <div className="w-12 h-12 bg-gradient-to-r from-purple-400 to-pink-400 rounded-full flex items-center justify-center text-white font-bold text-lg">
                  A
                </div>
                <div className="ml-4">
                  <h4 className="text-white font-semibold">Alex Thompson</h4>
                  <p className="text-blue-200 text-sm">Content Creator</p>
                </div>
              </div>
              <p className="text-blue-100 leading-relaxed">
                "As a content creator, I need different types of assistance
                throughout my workflow. NEXUS AI provides specialized assistants
                for each task, making my work so much more efficient."
              </p>
              <div className="flex text-yellow-400 mt-4">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} className="w-5 h-5 fill-current" />
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-600">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-4xl md:text-5xl font-bold text-white mb-6">
            Ready to Transform Your Productivity?
          </h2>
          <p className="text-xl text-blue-100 mb-12 max-w-3xl mx-auto">
            Join thousands of users who are already experiencing the future of
            AI-powered assistance.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
            <Link href="/sign-in">
              <Button
                size="lg"
                className="bg-white text-blue-600 hover:bg-blue-50 px-8 py-4 text-lg font-semibold shadow-2xl hover:shadow-3xl transition-all duration-300 transform hover:scale-105"
              >
                <Zap className="w-5 h-5 mr-2" />
                Start Free Trial
                <ArrowRight className="w-5 h-5 ml-2" />
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-slate-900 text-white py-16">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div className="col-span-1 md:col-span-2">
              <div className="flex items-center space-x-3 mb-6">
                <div className="relative">
                  <Image
                    src={"/NesusAI.png"}
                    alt="Nexus AI"
                    width={40}
                    height={40}
                    className="rounded-lg"
                  />
                  <div className="absolute -top-1 -right-1 w-3 h-3 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full animate-pulse"></div>
                </div>
                <div>
                  <span className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
                    NEXUS AI
                  </span>
                  <p className="text-sm text-slate-400">
                    Personal AI Assistant
                  </p>
                </div>
              </div>
              <p className="text-slate-300 mb-6 max-w-md">
                Experience the future of AI-powered assistance with
                personalized, intelligent, and adaptive AI companions designed
                to enhance your productivity.
              </p>
              <div className="flex space-x-4">
                <div className="w-10 h-10 bg-slate-800 rounded-lg flex items-center justify-center hover:bg-slate-700 transition-colors cursor-pointer">
                  <span className="text-sm font-bold">f</span>
                </div>
                <div className="w-10 h-10 bg-slate-800 rounded-lg flex items-center justify-center hover:bg-slate-700 transition-colors cursor-pointer">
                  <span className="text-sm font-bold">t</span>
                </div>
                <div className="w-10 h-10 bg-slate-800 rounded-lg flex items-center justify-center hover:bg-slate-700 transition-colors cursor-pointer">
                  <span className="text-sm font-bold">in</span>
                </div>
              </div>
            </div>

            <div>
              <h3 className="text-lg font-semibold mb-6">Product</h3>
              <ul className="space-y-3">
                <li>
                  <Link
                    href="/ai-assistants"
                    className="text-slate-300 hover:text-white transition-colors"
                  >
                    AI Assistants
                  </Link>
                </li>
                <li>
                  <Link
                    href="/pricing"
                    className="text-slate-300 hover:text-white transition-colors"
                  >
                    Pricing
                  </Link>
                </li>
              </ul>
            </div>

            <div>
              <h3 className="text-lg font-semibold mb-6">Company</h3>
              <ul className="space-y-3">
                <li>
                  <Link
                    href="/about"
                    className="text-slate-300 hover:text-white transition-colors"
                  >
                    About
                  </Link>
                </li>
              </ul>
            </div>
          </div>

          <div className="border-t border-slate-800 mt-12 pt-8 flex flex-col md:flex-row justify-between items-center">
            <p className="text-slate-400 text-sm">
              © 2024 NEXUS AI. All rights reserved.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default Hero;
