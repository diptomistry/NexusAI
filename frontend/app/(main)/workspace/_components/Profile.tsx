import React, { useContext, useEffect, useState } from "react";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { AuthContext } from "@/context/AuthContext";
import Image from "next/image";
import { Progress } from "@/components/ui/progress";
import { Button } from "@/components/ui/button";
import { Loader2Icon, WalletCardsIcon } from "lucide-react";
import axios from "axios";
import { toast } from "sonner";
import { updateUserTokens } from "@/services/database";

function Profile({ openDialog, setOpenDialog }: any) {
  const { user } = useContext(AuthContext);
  const [loading, setLoading] = useState(false);
  const [maxToken, setMaxToken] = useState<number>(0);
  useEffect(() => {
    if (user?.orderId) {
      setMaxToken(500000);
    } else {
      setMaxToken(10000);
    }
  }, [user]);

  useEffect(() => {
    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.async = true;
    script.onload = () => console.log(true); // Ensure script is loaded
    document.body.appendChild(script);

    return () => {
      document.body.removeChild(script);
    };
  }, []);

  const GenerateSubscriptionId = async () => {
    setLoading(true);
    const result = await axios.post("/api/create-subscription");
    console.log(result.data);
    MakePayment(result?.data?.id);
    setLoading(false);
  };

  const MakePayment = (subscriptionId: string) => {
    let options = {
      key: process.env.NEXT_PUBLIC_RAZORPAY_LIVE_KEY!,
      subscription_id: subscriptionId,
      name: "Tubeguruji AI Assistant App",
      description: "",
      image: "/logo.svg",
      handler: async function (resp: any) {
        console.log(resp.razorpay_payment_id);
        console.log(resp);
        if (resp?.razorpay_payment_id) {
          try {
            await updateUserTokens(
              user?.id,
              user.credits + 500000,
              resp.razorpay_subscription_id
            );
            toast("Thank You! Credits Added");
          } catch (error) {
            console.error("Error updating user order:", error);
            toast("Error updating credits");
          }
        }
      },
      prefill: {
        name: user?.name,
        email: user?.email,
      },
      notes: {},
      theme: {
        color: "#000000",
      },
    };

    //@ts-ignore
    const rzp = new window.Razorpay(options);
    rzp.open();
  };

  const cancelSubscription = async () => {
    const result = await axios.post("/api/cancel-subscription", {
      subId: user?.orderId,
    });
    console.log(result);
    toast("Subscription Canceled");
    window.location.reload();
  };

  return (
    <Dialog open={openDialog} onOpenChange={setOpenDialog}>
      {/* <DialogTrigger>Open</DialogTrigger> */}
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{}</DialogTitle>
          <DialogDescription asChild>
            <div>
              <div className="flex gap-4 items-center">
                <Image
                  src={user?.picture}
                  alt="user"
                  width={150}
                  height={150}
                  className="w-[60px] h-[60px] rounded-full"
                />
                <div>
                  <h2 className="font-bold text-lg">{user?.name}</h2>
                  <h2 className="text-gray-500">{user?.email}</h2>
                </div>
              </div>
              <hr className="my-3"></hr>
              <div className="flex flex-col gap-2">
                <h2 className="font-bold">Token Usage</h2>
                <h2>
                  {user?.credits}/{maxToken}
                </h2>
                <Progress value={(user?.credits / maxToken) * 100} />
                <h2 className="flex justify-between font-bold mt-3 text-lg">
                  Current Plan
                  <span className="p-1 bg-gray-100 rounded-md px-2 font-normal">
                    {!user?.orderId ? "Free Plan" : "Pro Plan"}
                  </span>{" "}
                </h2>
              </div>

              {!user?.orderId ? (
                <div className="p-4 border rounded-xl mt-4">
                  <div className="flex justify-between">
                    <div>
                      <h2 className="font-bold text-lg"> Pro Plan</h2>
                      <h2>500,000 Tokens</h2>
                    </div>
                    <h2 className="font-bold text-lg">$10/month</h2>
                  </div>
                  <hr className="my-3" />
                  <Button
                    className="w-full"
                    disabled={loading}
                    onClick={GenerateSubscriptionId}
                  >
                    {" "}
                    {loading ? (
                      <Loader2Icon className="animate-spin" />
                    ) : (
                      <WalletCardsIcon />
                    )}{" "}
                    Upgrade (10$)
                  </Button>
                </div>
              ) : (
                <Button
                  className="mt-4 w-full"
                  variant="secondary"
                  onClick={cancelSubscription}
                >
                  Cancel Subscription
                </Button>
              )}
            </div>
          </DialogDescription>
        </DialogHeader>
      </DialogContent>
    </Dialog>
  );
}

export default Profile;
