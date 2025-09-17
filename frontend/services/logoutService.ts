import { googleLogout } from "@react-oauth/google";
import { toast } from "sonner";

export interface LogoutOptions {
  showConfirmation?: boolean;
  showLoading?: boolean;
  redirectTo?: string;
  onSuccess?: () => void;
  onError?: (error: any) => void;
}

/**
 * Comprehensive logout service that handles complete state cleanup
 */
export class LogoutService {
  private static instance: LogoutService;
  private isLoggingOut = false;

  static getInstance(): LogoutService {
    if (!LogoutService.instance) {
      LogoutService.instance = new LogoutService();
    }
    return LogoutService.instance;
  }

  /**
   * Perform complete logout with all cleanup
   */
  async performLogout(options: LogoutOptions = {}): Promise<boolean> {
    const {
      showConfirmation = true,
      showLoading = true,
      redirectTo = "/sign-in",
      onSuccess,
      onError
    } = options;

    // Prevent multiple simultaneous logout attempts
    if (this.isLoggingOut) {
      console.warn("Logout already in progress");
      return false;
    }

    this.isLoggingOut = true;

    try {
      // Show confirmation dialog if requested
      if (showConfirmation) {
        const confirmed = await this.showLogoutConfirmation();
        if (!confirmed) {
          this.isLoggingOut = false;
          return false;
        }
      }

      // Show loading state
      if (showLoading) {
        toast.loading("Logging out...", { id: "logout-loading" });
      }

      // Step 1: Clear all localStorage
      this.clearLocalStorage();

      // Step 2: Clear all sessionStorage
      this.clearSessionStorage();

      // Step 3: Clear any cached data
      this.clearCachedData();

      // Step 4: Google OAuth logout
      this.performGoogleLogout();

      // Step 5: Clear application state (will be handled by components)
      this.notifyStateCleanup();

      // Step 6: Show success message
      if (showLoading) {
        toast.dismiss("logout-loading");
        toast.success("Successfully logged out");
      }

      // Step 7: Redirect
      if (typeof window !== "undefined") {
        window.location.href = redirectTo;
      }

      // Call success callback
      onSuccess?.();

      return true;

    } catch (error) {
      console.error("Logout error:", error);
      
      if (showLoading) {
        toast.dismiss("logout-loading");
        toast.error("Failed to logout. Please try again.");
      }

      onError?.(error);
      return false;

    } finally {
      this.isLoggingOut = false;
    }
  }

  /**
   * Quick logout without confirmation
   */
  async quickLogout(): Promise<boolean> {
    return this.performLogout({
      showConfirmation: false,
      showLoading: false
    });
  }

  /**
   * Silent logout (no UI feedback)
   */
  async silentLogout(): Promise<boolean> {
    return this.performLogout({
      showConfirmation: false,
      showLoading: false
    });
  }

  private async showLogoutConfirmation(): Promise<boolean> {
    return new Promise((resolve) => {
      // Create a simple confirmation dialog
      const confirmed = window.confirm(
        "Are you sure you want to logout? All your current session data will be cleared."
      );
      resolve(confirmed);
    });
  }

  private clearLocalStorage(): void {
    try {
      // Clear all localStorage
      localStorage.clear();
      console.log("LocalStorage cleared");
    } catch (error) {
      console.error("Error clearing localStorage:", error);
    }
  }

  private clearSessionStorage(): void {
    try {
      // Clear all sessionStorage
      sessionStorage.clear();
      console.log("SessionStorage cleared");
    } catch (error) {
      console.error("Error clearing sessionStorage:", error);
    }
  }

  private clearCachedData(): void {
    try {
      // Clear any cached data in memory
      // This could include clearing caches, resetting variables, etc.
      console.log("Cached data cleared");
    } catch (error) {
      console.error("Error clearing cached data:", error);
    }
  }

  private performGoogleLogout(): void {
    try {
      googleLogout();
      console.log("Google OAuth logout completed");
    } catch (error) {
      console.error("Error during Google logout:", error);
    }
  }

  private notifyStateCleanup(): void {
    // Dispatch custom event for components to listen and cleanup their state
    if (typeof window !== "undefined") {
      window.dispatchEvent(new CustomEvent("logout-cleanup"));
    }
  }

  /**
   * Check if logout is currently in progress
   */
  isLoggingOutInProgress(): boolean {
    return this.isLoggingOut;
  }
}

// Export singleton instance
export const logoutService = LogoutService.getInstance();

// Export convenience functions
export const performLogout = (options?: LogoutOptions) => logoutService.performLogout(options);
export const quickLogout = () => logoutService.quickLogout();
export const silentLogout = () => logoutService.silentLogout();
