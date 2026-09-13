package Exception;

/*
* Imagine you are building a user authentication system. When a user logs in, your system calls a third-party SMS provider (like Twilio) to send a One-Time Password (OTP) to their phone.Network drops, API timeouts, or the provider going down are not programming bugs—your code is perfect, but the external world is unreliable. If the SMS fails, the app shouldn't just crash. The developer must handle this failure (e.g., by falling back to sending the OTP via Email instead).
* */
public class CustomException {
    public static void main(String[] args) {
        NotificationService service = new NotificationService();

        // COMPILER FORCES A TRY-CATCH HERE
        try {
            service.sendOtp("+1234567890", "5541");
        } catch (SmsDeliveryException e) {
            // The developer is FORCED to think about a recovery plan
            System.out.println("ALERT: SMS failed. Activating fallback mechanism...");
            sendOtpViaEmail("+1234567890", "5541");
        }
    }

    private static void sendOtpViaEmail(String phone, String otp) {
        System.out.println("OTP successfully redirected and sent via Email instead!");
    }
}

// Extending Exception makes this a CHECKED exception
class SmsDeliveryException extends Exception {
    public SmsDeliveryException(String message) {
        super(message);
    }
    public SmsDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}

class NotificationService {
    public void sendOtp(String phoneNumber, String otp) throws SmsDeliveryException {
        boolean networkOrApiFailed = true; // Simulating a network dropout
        if (networkOrApiFailed) {
            // The code is fine, but the external gateway failed
            // ❌ COMPILER ERROR: Unhandled exception type SmsDeliveryException.
            // Means If the method does not throws the exception we will get compilation error because we are extending the Exception class.
            // Also we need to sorround the calling method with try catch block or the method should throws the exception.
            throw new SmsDeliveryException("SMS Gateway timeout. Failed to deliver OTP to " + phoneNumber);
        }
        System.out.println("OTP sent successfully via SMS!");
    }
}

