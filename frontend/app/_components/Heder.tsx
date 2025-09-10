import { Button } from '@/components/ui/button'
import Image from 'next/image'
import Link from 'next/link'
import React from 'react'

function Heder() {
    return (
        <div className='p-4 shadow-md flex justify-between items-center'>
            <div className='flex gap-2 items-center'>
                <Image src={'/logo.svg'} alt='log' width={40} height={40}

                />
                <h2 className='font-bold text-lg'>AI Genius</h2>
            </div>
            <Link href={'/ai-assistants'}>
                <Button >Get Started</Button>
            </Link>
        </div>
    )
}

export default Heder