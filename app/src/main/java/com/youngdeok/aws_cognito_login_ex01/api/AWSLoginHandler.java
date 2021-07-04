package com.youngdeok.aws_cognito_login_ex01.api;

/**
 * Callback used for model {@link AWSLoginModel}. This needs to be implemented when the constructor
 * of {@link AWSLoginModel} is called.
 */
public interface AWSLoginHandler {

    /**
     * Successful completion of the first step of the registration process.
     * This will output mustConfirmToComplete in case there's the need to confirm registration to
     * complete this process.
     *
     * @param mustConfirmToComplete     will be {@code true} if there's the need to confirm
     *                                  registration, otherwise {@code false}.
     */
    void onRegisterSuccess(boolean mustConfirmToComplete);

    /**
     * Successful completion of the registration process.
     */
    void onRegisterConfirmed();

    /**
     * Successful completion of the sign in process.
     */
    void onSignInSuccess();

    /**
     * Successful completion of the request for confirmation code (when registering).
     *
     * @param medium              what medium the code was sent (e-mail / phone number).
     */
    void onResendConfirmationCodeSuccess(String medium);

    /**
     * Successful completion of the request for reset user password.
     *
     * @param medium              what medium the code was sent (e-mail / phone number).
     */
    void onRequestResetUserPasswordSuccess(String medium);

    /**
     * Successful completion of the reset of the user password.
     */
    void onResetUserPasswordSuccess();

    /**
     * Failure of the process called.
     *
     * @param process       what process was called.
     * @param exception     failure details.
     * @param cause         cause of failure.
     * @param message       message of the error.
     */
    void onFailure(int process, Exception exception, int cause, String message);

}
