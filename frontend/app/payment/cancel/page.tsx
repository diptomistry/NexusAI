"use client";

import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { AlertCircle, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import Link from "next/link";

export default function PaymentCancel() {
  const searchParams = useSearchParams();
  const [transactionId, setTransactionId] = useState<string>("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const tranId = searchParams.get("tran_id");

    setTransactionId(tranId || "");
    setLoading(false);
  }, [searchParams]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="h-8 w-8 animate-spin mx-auto mb-4" />
          <p>Loading payment details...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-yellow-50 to-orange-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="mx-auto mb-4">
            <AlertCircle className="h-16 w-16 text-yellow-500" />
          </div>
          <CardTitle className="text-2xl text-yellow-600">
            Payment Cancelled
          </CardTitle>
          <CardDescription>
            You have cancelled the payment process
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {transactionId && (
            <div className="bg-yellow-50 p-4 rounded-lg">
              <div className="text-sm text-yellow-700">
                <strong>Transaction ID:</strong>
                <div className="font-mono text-xs mt-1 break-all">
                  {transactionId}
                </div>
              </div>
            </div>
          )}

          <div className="text-center space-y-3 pt-4">
            <p className="text-sm text-gray-600">
              No charges have been made to your account.
            </p>
            <div className="text-sm text-left space-y-2 text-gray-700 bg-blue-50 p-3 rounded-lg">
              <p className="font-semibold">You can still upgrade to Pro:</p>
              <ul className="space-y-1 ml-4">
                <li>• Get 500,000 AI tokens</li>
                <li>• Access to premium AI models</li>
                <li>• Priority customer support</li>
                <li>• Unlimited conversations</li>
              </ul>
            </div>
          </div>

          <div className="pt-4 space-y-2">
            <Link href="/workspace" className="w-full">
              <Button className="w-full">Try Payment Again</Button>
            </Link>
            <Link href="/" className="w-full">
              <Button variant="outline" className="w-full">
                Back to Home
              </Button>
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
