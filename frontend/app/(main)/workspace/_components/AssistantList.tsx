"use client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { AuthContext } from "@/context/AuthContext";
import React, { useContext, useEffect, useState } from "react";
import { ASSISTANT } from "../../ai-assistants/page";
import AiAssistantsList from "@/services/AiAssistantsList";
import Image from "next/image";
import { AssistantContext } from "@/context/AssistantContext";
import { BlurFade } from "@/components/magicui/blur-fade";
import AddNewAssistant from "./AddNewAssistant";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { LogOut, UserCircle2 } from "lucide-react";
import Profile from "./Profile";
import { googleLogout } from "@react-oauth/google";
import { useRouter } from "next/navigation";
import { getAllUserAssistants } from "@/services/database";

function AssistantList() {
  const { user, setUser } = useContext(AuthContext);
  const [assistantList, setAssistantList] = useState<ASSISTANT[]>([]);
  const { assistant, setAssistant } = useContext(AssistantContext);
  const [loading, setLoading] = useState(false);
  const [openProfile, setOpenProfile] = useState(false);
  const router = useRouter();
  useEffect(() => {
    user && GetUserAssistants();
  }, [user && assistant == null]);

  const GetUserAssistants = async () => {
    setLoading(true);
    setAssistantList([]);
    try {
      const result = await getAllUserAssistants(user.id);
      console.log(result);
      // Map Supabase format to ASSISTANT format
      const mappedResult = result.map((item) => ({
        id: item.assistant_id,
        name: item.name,
        title: item.title,
        image: item.image,
        instruction: item.instruction,
        userInstruction: item.user_instruction,
        sampleQuestions: item.sample_questions,
        aiModelId: item.ai_model_id,
        _id: item.id, // Keep the Supabase ID for operations
      }));
      setAssistant(mappedResult[0]);
      setAssistantList(mappedResult);
    } catch (error) {
      console.error("Error fetching assistants:", error);
    }
    setLoading(false);
  };

  return (
    <div className="p-5 bg-secondary border-r-[1px] h-screen relative">
      <h2 className="font-bold text-lg">Your Personal AI Assistants</h2>

      <AddNewAssistant>
        <Button className="w-full mt-3">+ Add New Assistant</Button>
      </AddNewAssistant>

      <Input className="bg-white mt-3" placeholder="Search" />

      <div className="mt-5 overflow-scroll h-[64%]">
        {assistantList.map((assistant_, index) => (
          <BlurFade
            key={assistant_._id || `assistant-${assistant_.id}-${index}`}
            delay={0.25 + index * 0.05}
            inView
          >
            <div
              className={`p-2 flex gap-3 items-center
                    hover:bg-gray-200 hover:dark:bg-slate-700 
                    rounded-xl cursor-pointer mt-2
                    ${assistant_.id == assistant?.id && "bg-gray-200"}
                    `}
              onClick={() => {
                setAssistant(assistant_);
              }}
            >
              <Image
                src={assistant_.image}
                alt={assistant_.name}
                width={60}
                height={60}
                className="rounded-xl w-[60px] h-[60px]
                            object-cover"
              />
              <div>
                <h2 className="font-bold">{assistant_.name}</h2>
                <h2 className="text-gray-600 text-sm dark:text-gray-300">
                  {assistant_.title}
                </h2>
              </div>
            </div>
          </BlurFade>
        ))}

        {loading && (
          <div className="">
            {[1, 2, 3, 4, 5].map((item, index) => (
              <div
                key={index}
                className="mb-2 w-full h-[60px] bg-slate-200 animate-pulse rounded-xl"
              ></div>
            ))}
          </div>
        )}
      </div>

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <div
            className="absolute bottom-10 flex gap-3 items-center
             hover:bg-gray-200 w-[87%] p-2 rounded-xl cursor-pointer bg-secondary"
          >
            {user && (
              <Image
                src={user?.picture}
                alt="user"
                width={35}
                height={35}
                className="rounded-full"
              />
            )}
            <div>
              <h2 className="font-bold">{user?.name}</h2>
              <h2 className="text-gray-400 text-sm">
                {user?.orderId ? "Pro Plan" : "Free Plan"}
              </h2>
            </div>
          </div>
        </DropdownMenuTrigger>
        <DropdownMenuContent className="w-[200px]">
          <DropdownMenuLabel>My Account</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuItem onClick={() => setOpenProfile(true)}>
            {" "}
            <UserCircle2 /> Profile
          </DropdownMenuItem>
          <DropdownMenuItem
            onClick={() => {
              setAssistant(null);
              setUser(null);
              googleLogout();
              router.replace("/");
            }}
          >
            <LogOut /> Logout
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
      <Profile openDialog={openProfile} setOpenDialog={setOpenProfile} />
    </div>
  );
}

export default AssistantList;
