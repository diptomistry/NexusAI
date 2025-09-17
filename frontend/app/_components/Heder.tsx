import { Button } from "@/components/ui/button";
import Image from "next/image";
import Link from "next/link";
import React from "react";

function Heder() {
  return (
    <div className="pl-2 pr-2 shadow-md flex justify-between items-center">
      <div className="flex gap-2 items-center">
        <Image src={"/NesusAI.png"} alt="Nexus AI" width={80} height={80} />
      </div>
      <Link href={"/ai-assistants"}>
        <Button>Get Started</Button>
      </Link>
    </div>
  );
}

export default Heder;
