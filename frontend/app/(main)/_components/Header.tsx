"use client";
import { AuthContext } from "@/context/AuthContext";
import Image from "next/image";
import React, { useContext } from "react";

function Header() {
  const { user } = useContext(AuthContext);
  return (
    <div className="pl-4 fixed shadow-sm w-full flex justify-between items-center px-14 bg-white/95 backdrop-blur-sm z-50">
      <div className="flex items-center gap-3">
        <Image
          src={"/NesusAI.png"}
          alt="Nexus AI"
          width={80}
          height={80}
          className="rounded-lg"
        />
      </div>

      {user?.picture && (
        <Image
          src={user?.picture}
          alt="User Profile"
          width={45}
          height={45}
          className="rounded-full border-2 border-gray-200"
        />
      )}
    </div>
  );
}

export default Header;
