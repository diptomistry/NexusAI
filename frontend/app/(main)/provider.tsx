"use client";
import React, { useContext, useEffect, useState } from "react";
import Header from "./_components/Header";
import { GetAuthUserData } from "@/services/GlobalApi";
import { useRouter, usePathname } from "next/navigation";
import { AuthContext } from "@/context/AuthContext";
import { AssistantContext } from "@/context/AssistantContext";
import { getUser } from "@/services/database";

function Provider({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const router = useRouter();
  const pathname = usePathname();
  const { user, setUser } = useContext(AuthContext);
  const [assistant, setAssistant] = useState();
  useEffect(() => {
    CheckUseAuth();
  }, []);

  const CheckUseAuth = async () => {
    const token = localStorage.getItem("user_token");
    //Get New Access Token
    const userData = token && (await GetAuthUserData(token));
    if (!userData?.email) {
      router.replace("/sign-in");
      return;
    }
    // Get User Info From Database
    try {
      const result = await getUser(userData.email);
      setUser(result);
    } catch (e) {
      console.error("Error fetching user:", e);
    }
  };
  return (
    <div>
      <AssistantContext.Provider value={{ assistant, setAssistant }}>
        {pathname !== "/workspace" && <Header />}
        {children}
      </AssistantContext.Provider>
    </div>
  );
}

export default Provider;
