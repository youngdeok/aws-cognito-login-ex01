package com.youngdeok.aws_cognito_login_ex01.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.model.InvalidParameterException;
import com.amazonaws.services.cognitoidentityprovider.model.LimitExceededException;
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;
import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This represents a model for login operations on AWS Mobile Hub. It manages login operations
 * such as:
 * - Sign In
 * - Sign Up
 * - Confirm Sign Up
 * - Resend Confirmation Code
 * - Recover Password
 * - Sign Out
 * - Delete Account
 * - Get User Name (current signed in)
 * - Get User E-mail (current signed in)
 *
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class AWSLoginModel {

    // constants
    // AWS attributes
    private final String ATTR_EMAIL = "email";
    private final String ATTR_USERNAME = "preferred_username";
    // saved values on shared preferences
    private static final String SHARED_PREFERENCE = "SavedValues";
    private static final String PREFERENCE_USER_NAME = "awsUserName";
    private static final String PREFERENCE_USER_EMAIL = "awsUserEmail";
    // message errors used in more than one place
    private static final String MESSAGE_UNKNOWN_ERROR = "Unknown error. Check internet connection.";
    private static final String MESSAGE_USER_NOT_FOUND = "User does not exist.";
    // current process requested
    public static final int PROCESS_SIGN_IN = 1;
    public static final int PROCESS_REGISTER = 2;
    public static final int PROCESS_CONFIRM_REGISTRATION = 3;
    public static final int PROCESS_RESEND_CONFIRMATION_CODE = 4;
    public static final int PROCESS_REQUEST_RESET_PASSWORD = 5;
    public static final int PROCESS_RESET_PASSWORD = 6;
    // error causes
    public static final int CAUSE_MUST_CONFIRM_FIRST = 1;
    public static final int CAUSE_USER_NOT_FOUND = 2;
    public static final int CAUSE_INCORRECT_PASSWORD = 3;
    public static final int CAUSE_LIMIT_EXCEEDED = 4;
    public static final int CAUSE_USER_ALREADY_EXISTS = 5;
    public static final int CAUSE_INVALID_PARAMETERS = 6;
    public static final int CAUSE_UNKNOWN = 999;

    // interface handler
    private AWSLoginHandler mCallback;

    // control variables
    private String userName, userPassword;
    private int currentProcessInResetPassword;
    private Context mContext;
    private CognitoUserPool mCognitoUserPool;
    private CognitoUser mCognitoUser;

    // Handler of the signInUser method
    private AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            // Get details of the logged user (in this case, only the e-mail)
            mCognitoUser = mCognitoUserPool.getCurrentUser();
            mCognitoUser.getDetailsInBackground(new GetDetailsHandler() {
                @SuppressLint("ApplySharedPref")
                @Override
                public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                    // Save in SharedPreferences
                    SharedPreferences.Editor editor = mContext.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE).edit();
                    String email = cognitoUserDetails.getAttributes().getAttributes().get(ATTR_EMAIL);
                    String userName = cognitoUserDetails.getAttributes().getAttributes().get(ATTR_USERNAME);
                    editor.putString(PREFERENCE_USER_EMAIL, email);
                    editor.putString(PREFERENCE_USER_NAME, userName);
                    editor.commit();
                    mCallback.onSignInSuccess();
                }

                @Override
                public void onFailure(Exception exception) {
                    mCallback.onFailure(PROCESS_SIGN_IN, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR);
                }
            });

        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
            final AuthenticationDetails authenticationDetails = new AuthenticationDetails(userName, userPassword, null);
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            authenticationContinuation.continueTask();
            userPassword = "";
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
            // Not implemented for this Model
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            // Not implemented for this Model
        }

        @Override
        public void onFailure(Exception exception) {
            userPassword = "";
            if (exception instanceof UserNotConfirmedException) {
                mCallback.onFailure(PROCESS_SIGN_IN, exception, CAUSE_MUST_CONFIRM_FIRST, "User not confirmed.");
            } else if (exception instanceof UserNotFoundException) {
                mCallback.onFailure(PROCESS_SIGN_IN, exception, CAUSE_USER_NOT_FOUND, MESSAGE_USER_NOT_FOUND);
            } else if (exception instanceof NotAuthorizedException) {
                mCallback.onFailure(PROCESS_SIGN_IN, exception, CAUSE_INCORRECT_PASSWORD, "Incorrect username or password.");
            } else {
                mCallback.onFailure(PROCESS_SIGN_IN, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR);
            }
        }
    };

    // Handler for
    private final ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler() {
        @Override
        public void onSuccess() {
            mCallback.onResetUserPasswordSuccess();
        }

        @Override
        public void getResetCode(ForgotPasswordContinuation continuation) {
            forgotPasswordContinuation = continuation;
            mCallback.onRequestResetUserPasswordSuccess(continuation.getParameters().getDeliveryMedium());
        }

        @Override
        public void onFailure(Exception exception) {
            if (exception instanceof LimitExceededException) {
                mCallback.onFailure(currentProcessInResetPassword, exception, CAUSE_LIMIT_EXCEEDED, "Limit exceeded. Wait to try again");
            } else if (exception instanceof UserNotFoundException) {
                mCallback.onFailure(currentProcessInResetPassword, exception, CAUSE_USER_NOT_FOUND, MESSAGE_USER_NOT_FOUND);
            } else if (exception instanceof InvalidParameterException) {
                mCallback.onFailure(currentProcessInResetPassword, exception, CAUSE_INVALID_PARAMETERS, "User not confirmed. Cannot send e-mail.");
            } else {
                mCallback.onFailure(currentProcessInResetPassword, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR);
            }
        }
    };

    private ForgotPasswordContinuation forgotPasswordContinuation;

    /**
     * Constructs the model for login functions in AWS Mobile Hub.
     *
     * @param context         REQUIRED: Android application context.
     * @param callback        REQUIRED: Callback handler for login operations.
     *
     */
    public AWSLoginModel(Context context, AWSLoginHandler callback) {
        mContext = context;
        mCallback = callback;
        IdentityManager identityManager = IdentityManager.getDefaultIdentityManager();
        try{
            JSONObject myJSON = identityManager.getConfiguration().optJsonObject("CognitoUserPool");
            final String COGNITO_POOL_ID = myJSON.getString("PoolId");
            final String COGNITO_CLIENT_ID = myJSON.getString("AppClientId");
            final String COGNITO_CLIENT_SECRET = myJSON.getString("AppClientSecret");
            final String REGION = myJSON.getString("Region");
            mCognitoUserPool = new CognitoUserPool(context, COGNITO_POOL_ID, COGNITO_CLIENT_ID, COGNITO_CLIENT_SECRET, Regions.fromName(REGION));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers new user to the AWS Cognito User Pool.
     *
     * This will trigger {@link AWSLoginHandler} interface defined when the constructor was called.
     *
     * @param userName         REQUIRED: Username to be registered. Must be unique in the User Pool.
     * @param userEmail        REQUIRED: E-mail to be registered. Must be unique in the User Pool.
     * @param userPassword     REQUIRED: Password of this new account.
     *
     */
    public void registerUser(String userName, String userEmail, String userPassword) {
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        userAttributes.addAttribute(ATTR_EMAIL, userEmail);
        userAttributes.addAttribute(ATTR_USERNAME, userName);

        final SignUpHandler signUpHandler = new SignUpHandler() {

            @Override
            public void onSuccess(CognitoUser user, SignUpResult signUpResult) {
                mCognitoUser = user;
                mCallback.onRegisterSuccess(!signUpResult.isUserConfirmed());
            }

            @Override
            public void onFailure(Exception exception) {
                if (exception instanceof UsernameExistsException) {
                    mCallback.onFailure(PROCESS_REGISTER, exception, CAUSE_USER_ALREADY_EXISTS, "Username or e-mail already exists.");
                } else if (exception instanceof InvalidParameterException) {
                    mCallback.onFailure(PROCESS_REGISTER, exception, CAUSE_INVALID_PARAMETERS, "Invalid parameters.");
                } else {
                    mCallback.onFailure(PROCESS_REGISTER, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR);
                }
            }
        };

        mCognitoUserPool.signUpInBackground(userName, userPassword, userAttributes, null, signUpHandler);
    }

    /**
     * Confirms registration of the new user in AWS Cognito User Pool.
     *
     * This will trigger {@link AWSLoginHandler} interface defined when the constructor was called.
     *
     * @param confirmationCode      REQUIRED: Code sent from AWS to the user.
     */
    public void confirmRegistration(String confirmationCode) {
        final GenericHandler confirmationHandler = new GenericHandler() {
            @Override
            public void onSuccess() {
                mCallback.onRegisterConfirmed();
            }

            @Override
            public void onFailure(Exception exception) {
                mCallback.onFailure(PROCESS_CONFIRM_REGISTRATION, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR);
            }
        };

        mCognitoUser.confirmSignUpInBackground(confirmationCode, false, confirmationHandler);
    }

    /**
     * Sign in process. If succeeded, this will save the user name and e-mail in SharedPreference of
     * this context.
     *
     * This will trigger {@link AWSLoginHandler} interface defined when the constructor was called.
     *
     * @param userName               REQUIRED: Username.
     * @param userPassword           REQUIRED: Password.
     */
    public void signInUser(String userName, String userPassword) {
        this.userName = userName;
        this.userPassword = userPassword;

        mCognitoUser = mCognitoUserPool.getUser(userName);
        mCognitoUser.getSessionInBackground(authenticationHandler);
    }

    /**
     * Re-sends the confirmation code from the current user
     */
    public void resendConfirmationCode() {
        mCognitoUser.resendConfirmationCodeInBackground(new VerificationHandler() {
            @Override
            public void onSuccess(CognitoUserCodeDeliveryDetails verificationCodeDeliveryMedium) {
                mCallback.onResendConfirmationCodeSuccess(verificationCodeDeliveryMedium.getDeliveryMedium());
            }

            @Override
            public void onFailure(Exception exception) {
                mCallback.onFailure(PROCESS_RESEND_CONFIRMATION_CODE, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR);
            }
        });
    }

    /**
     * Requests the reset of the user's password (in case of forgotten password).
     * This method sends the reset code to the user.
     *
     * @param userName          REQUIRED: Username.
     */
    public void requestResetUserPassword(String userName) {
        currentProcessInResetPassword = PROCESS_REQUEST_RESET_PASSWORD;
        mCognitoUser = mCognitoUserPool.getUser(userName);
        mCognitoUser.forgotPasswordInBackground(forgotPasswordHandler);
    }

    /**
     * Resets current user password if the resetCode matches with the one sent to the user (when
     * requestResetUserPassword was called).
     *
     * @param resetCode         REQUIRED: should be same code received when request was called.
     * @param newPassword       REQUIRED: new password.
     */
    public void resetUserPasswordWithCode(String resetCode, String newPassword) {
        currentProcessInResetPassword = PROCESS_RESET_PASSWORD;
        forgotPasswordContinuation.setVerificationCode(resetCode);
        forgotPasswordContinuation.setPassword(newPassword);
        forgotPasswordContinuation.continueTask();
    }

    /**
     * Signs out from current session.
     */
    public static void doUserLogout() {
        IdentityManager identityManager = IdentityManager.getDefaultIdentityManager();
        identityManager.signOut();
    }

    /**
     * Gets the user name saved in SharedPreferences.
     *
     * @param context               REQUIRED: Android application context.
     * @return                      user name saved in SharedPreferences.
     */
    public static String getSavedUserName(Context context) {
        SharedPreferences savedValues = context.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return savedValues.getString(PREFERENCE_USER_NAME, "");
    }

    /**
     * Gets the user e-mail saved in SharedPreferences.
     *
     * @param context               REQUIRED: Android application context.
     * @return                      user e-mail saved in SharedPreferences.
     */
    public static String getSavedUserEmail(Context context) {
        SharedPreferences savedValues = context.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return savedValues.getString(PREFERENCE_USER_EMAIL, "");
    }

}
