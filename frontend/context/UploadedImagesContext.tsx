"use client";
import { createContext, useContext, useState, useEffect } from "react";

interface UploadedImagesContextType {
  uploadedUrl: string | null;
  setUploadedUrl: (url: string | null) => void;
  clearUploadedUrl: () => void;
  latestGeneratedImages: string[];
  setLatestGeneratedImages: (images: string[]) => void;
}

const UploadedImagesContext = createContext<UploadedImagesContextType | null>(
  null
);

export const UploadedImagesProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const [uploadedUrl, setUploadedUrl] = useState<string | null>(null);
  const [latestGeneratedImages, setLatestGeneratedImages] = useState<string[]>(
    []
  );

  const clearUploadedUrl = () => {
    setUploadedUrl(null);
  };

  // Listen for logout cleanup event
  useEffect(() => {
    const handleLogoutCleanup = () => {
      setUploadedUrl(null);
      setLatestGeneratedImages([]);
    };

    window.addEventListener("logout-cleanup", handleLogoutCleanup);
    return () =>
      window.removeEventListener("logout-cleanup", handleLogoutCleanup);
  }, []);

  return (
    <UploadedImagesContext.Provider
      value={{
        uploadedUrl,
        setUploadedUrl,
        clearUploadedUrl,
        latestGeneratedImages,
        setLatestGeneratedImages,
      }}
    >
      {children}
    </UploadedImagesContext.Provider>
  );
};

export const useUploadedImages = () => {
  const context = useContext(UploadedImagesContext);
  if (!context) {
    throw new Error(
      "useUploadedImages must be used within an UploadedImagesProvider"
    );
  }
  return context;
};
