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

function Profile({ openDialog, setOpenDialog }: any) {
  const { user } = useContext(AuthContext);
  const [loading, setLoading] = useState(false);
  const [maxToken, setMaxToken] = useState<number>(0);

  useEffect(() => {
    if (user?.order_id) {
      setMaxToken(30000);
    } else {
      setMaxToken(2500);
    }
  }, [user]);

  const initiateSSLCommerzPayment = async () => {
    setLoading(true);
    try {
      console.log("User ID:", user?.id);
      console.log("User ID type:", typeof user?.id);

      const paymentRequest = {
        userId: user?.id,
        totalAmount: 2000.0, // 2000 BDT for Pro Plan
        currency: "BDT",
        customerName: user?.name || "",
        customerEmail: user?.email || "",
        customerPhone: "01700000000", // Default phone, should be collected from user
        customerAddress: "Dhaka, Bangladesh",
        customerCity: "Dhaka",
        customerState: "Dhaka",
        customerPostcode: "1000",
        customerCountry: "Bangladesh",
        productName: "NexusAI Pro Plan",
        productCategory: "Digital Service",
        productProfile: "general",
      };

      console.log("Payment request:", paymentRequest);

      const response = await axios.post(
        "/api/payment/initiate",
        paymentRequest
      );

      if (response.data.success && response.data.gatewayPageUrl) {
        // Redirect to SSLCommerz payment gateway
        window.location.href = response.data.gatewayPageUrl;
      } else {
        toast.error(
          "Payment initiation failed: " +
            (response.data.message || "Unknown error")
        );
      }
    } catch (error: any) {
      console.error("Payment initiation error:", error);
      toast.error("Payment initiation failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const cancelSubscription = async () => {
    try {
      // Since we're not using subscription model with SSLCommerz,
      // we just need to reset the user's order ID
      // You might want to implement this endpoint in your backend
      const response = await axios.post("/api/cancel-subscription", {
        userId: user?.id,
        orderId: user?.order_id,
      });

      toast.success("Subscription Canceled");
      window.location.reload();
    } catch (error) {
      console.error("Cancel subscription error:", error);
      toast.error("Failed to cancel subscription");
    }
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
                {(() => {
                  const remainingCredits =
                    typeof user?.credits === "number" ? user.credits : 0;
                  const totalCredits =
                    typeof user?.max_credits === "number" &&
                    user.max_credits > 0
                      ? user.max_credits
                      : typeof maxToken === "number"
                      ? maxToken
                      : 0;
                  const usedCredits = Math.max(
                    0,
                    Math.min(totalCredits, totalCredits - remainingCredits)
                  );
                  const progress =
                    totalCredits > 0 ? (usedCredits / totalCredits) * 100 : 0;
                  return (
                    <>
                      <h2>
                        {usedCredits}/{totalCredits}
                      </h2>
                      <Progress value={progress} />
                    </>
                  );
                })()}
                <h2 className="flex justify-between font-bold mt-3 text-lg">
                  Current Plan
                  <span className="p-1 bg-gray-100 rounded-md px-2 font-normal">
                    {!user?.order_id ? "Free Plan" : "Pro Plan"}
                  </span>{" "}
                </h2>
              </div>

              {!user?.orderId ? (
                <div className="p-4 border rounded-xl mt-4">
                  <div className="flex justify-between">
                    <div>
                      <h2 className="font-bold text-lg"> Pro Plan</h2>
                      <h2>30,000 Tokens</h2>
                    </div>
                    <h2 className="font-bold text-lg">৳2,000/month</h2>
                  </div>
                  <hr className="my-3" />
                  <Button
                    className="w-full"
                    disabled={loading}
                    onClick={initiateSSLCommerzPayment}
                  >
                    {" "}
                    {loading ? (
                      <Loader2Icon className="animate-spin" />
                    ) : (
                      <WalletCardsIcon />
                    )}{" "}
                    Upgrade (৳2,000)
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
