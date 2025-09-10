import { AnimatedGridPattern } from '@/components/magicui/animated-grid-pattern'
import React from 'react'
import Heder from './Heder'
import { AuroraText } from '@/components/magicui/aurora-text'
import { Button } from '@/components/ui/button'
import Link from 'next/link'
import HeroVideoDialog from '@/components/magicui/hero-video-dialog'
import { AnimatedGradientText } from '@/components/magicui/animated-gradient-text'
import { ChevronRight } from 'lucide-react'

function Hero() {
    return (
        <div >
            <Heder />

            <div className="relative flex h-[800px] w-full items-center justify-center overflow-hidden rounded-lg border bg-background p-20">
                <AnimatedGridPattern
                    numSquares={30}
                    maxOpacity={0.1}
                    duration={3}
                    repeatDelay={1}
                    className="[mask-image:radial-gradient(500px_circle_at_center,white,transparent)]
                inset-x-0 inset-y-[-30%] h-[200%] skew-y-12"

                />

                <div className='flex flex-col gap-7 items-center'>
                    <div className="group relative mx-auto flex items-center justify-center rounded-full px-4 py-1.5 shadow-[inset_0_-8px_10px_#8fdfff1f] transition-shadow duration-500 ease-out hover:shadow-[inset_0_-5px_10px_#8fdfff3f] ">
                        <span
                            className=
                            "absolute inset-0 block h-full w-full animate-gradient rounded-[inherit] bg-gradient-to-r from-[#ffaa40]/50 via-[#9c40ff]/50 to-[#ffaa40]/50 bg-[length:300%_100%] p-[1px]"

                            style={{
                                WebkitMask:
                                    "linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)",
                                WebkitMaskComposite: "destination-out",
                                mask: "linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)",
                                maskComposite: "subtract",
                                WebkitClipPath: "padding-box",
                            }}
                        />
                        🎉 <hr className="mx-2 h-4 w-px shrink-0 bg-neutral-500" />
                        <AnimatedGradientText className="text-sm font-medium">
                            Introducing Personal AI Assistant
                        </AnimatedGradientText>
                        <ChevronRight
                            className="ml-1 size-4 stroke-neutral-500 transition-transform
 duration-300 ease-in-out group-hover:translate-x-0.5"
                        />
                    </div>
                    <h1 className="text-4xl font-bold tracking-tighter md:text-5xl lg:text-7xl">
                        Yours Personal <AuroraText>AI Assistance</AuroraText>
                    </h1>
                    <Link href={'//ai-assistants'}>
                        <Button >Get Started</Button>
                    </Link>
                    <div className='max-w-2xl'>
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


        </div>
    )
}

export default Hero

