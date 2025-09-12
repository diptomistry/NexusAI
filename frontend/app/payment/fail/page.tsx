"use client";

import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { XCircle, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import Link from "next/link";

export default function PaymentFail() {
  const searchParams = useSearchParams();
  const [transactionId, setTransactionId] = useState<string>("");
  const [reason, setReason] = useState<string>("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const tranId = searchParams.get("tran_id");
    const reasonParam = searchParams.get("reason");

    setTransactionId(tranId || "");
    setReason(reasonParam || "Payment failed due to unknown reason");
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
    <div className="min-h-screen bg-gradient-to-br from-red-50 to-orange-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="mx-auto mb-4">
            <XCircle className="h-16 w-16 text-red-500" />
          </div>
          <CardTitle className="text-2xl text-red-600">
            Payment Failed
          </CardTitle>
          <CardDescription>
            Unfortunately, your payment could not be processed
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {transactionId && (
            <div className="bg-red-50 p-4 rounded-lg">
              <div className="text-sm text-red-700">
                <strong>Transaction ID:</strong>
                <div className="font-mono text-xs mt-1 break-all">
                  {transactionId}
                </div>
              </div>
            </div>
          )}

          {reason && (
            <div className="bg-orange-50 p-4 rounded-lg">
              <div className="text-sm text-orange-700">
                <strong>Reason:</strong>
                <div className="mt-1">{reason}</div>
              </div>
            </div>
          )}

          <div className="text-center space-y-3 pt-4">
            <p className="text-sm text-gray-600">
              Don't worry! You can try again or contact our support team.
            </p>
            <div className="text-sm text-left space-y-2 text-gray-700 bg-gray-50 p-3 rounded-lg">
              <p className="font-semibold">Common solutions:</p>
              <ul className="space-y-1 ml-4">
                <li>• Check your card details</li>
                <li>• Ensure sufficient balance</li>
                <li>• Try a different payment method</li>
                <li>• Contact your bank if needed</li>
              </ul>
            </div>
          </div>

          <div className="pt-4 space-y-2">
            <Link href="/workspace" className="w-full">
              <Button className="w-full">Try Again</Button>
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
