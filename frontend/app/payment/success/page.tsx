"use client";

import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { CheckCircle, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import Link from "next/link";

export default function PaymentSuccess() {
  const searchParams = useSearchParams();
  const [transactionId, setTransactionId] = useState<string>("");
  const [status, setStatus] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const [paymentStatus, setPaymentStatus] = useState<any>(null);

  useEffect(() => {
    const tranId = searchParams.get("tran_id");
    const statusParam = searchParams.get("status");

    if (tranId) {
      setTransactionId(tranId);
      setStatus(statusParam || "");

      // Fetch payment status from backend
      fetchPaymentStatus(tranId);
    } else {
      setLoading(false);
    }
  }, [searchParams]);

  const fetchPaymentStatus = async (tranId: string) => {
    try {
      const response = await fetch(`/api/payment/status/${tranId}`);
      if (response.ok) {
        const data = await response.json();
        setPaymentStatus(data);
      }
    } catch (error) {
      console.error("Error fetching payment status:", error);
    } finally {
      setLoading(false);
    }
  };

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
    <div className="min-h-screen bg-gradient-to-br from-green-50 to-blue-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="mx-auto mb-4">
            <CheckCircle className="h-16 w-16 text-green-500" />
          </div>
          <CardTitle className="text-2xl text-green-600">
            Payment Successful!
          </CardTitle>
          <CardDescription>
            Your payment has been processed successfully
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {transactionId && (
            <div className="bg-green-50 p-4 rounded-lg">
              <div className="text-sm text-green-700">
                <strong>Transaction ID:</strong>
                <div className="font-mono text-xs mt-1 break-all">
                  {transactionId}
                </div>
              </div>
            </div>
          )}

          {paymentStatus && (
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span>Amount:</span>
                <span className="font-semibold">
                  {paymentStatus.currency} {paymentStatus.amount}
                </span>
              </div>
              <div className="flex justify-between">
                <span>Status:</span>
                <span className="font-semibold text-green-600 capitalize">
                  {paymentStatus.status}
                </span>
              </div>
              {paymentStatus.createdAt && (
                <div className="flex justify-between">
                  <span>Date:</span>
                  <span className="font-semibold">
                    {new Date(paymentStatus.createdAt).toLocaleDateString()}
                  </span>
                </div>
              )}
            </div>
          )}

          <div className="text-center space-y-3 pt-4">
            <p className="text-sm text-gray-600">
              🎉 Congratulations! You now have access to:
            </p>
            <ul className="text-sm text-left space-y-1 text-gray-700">
              <li>✅ 500,000 AI tokens</li>
              <li>✅ Priority support</li>
              <li>✅ Advanced AI models</li>
              <li>✅ Unlimited conversations</li>
            </ul>
          </div>

          <div className="pt-4 space-y-2">
            <Link href="/workspace" className="w-full">
              <Button className="w-full">Go to Workspace</Button>
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
