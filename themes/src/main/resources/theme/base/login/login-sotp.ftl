<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('totp'); section>
    <#if section="header">
        ${msg("doLogIn")}
    <#elseif section="form">
        <form id="kc-otp-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}"
            method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="otp" class="${properties.kcLabelClass!}">${msg("loginOtpOneTime")}</label>
                </div>

            <div class="${properties.kcInputWrapperClass!}">
                <input id="otp" name="otp" autocomplete="off" type="text" class="${properties.kcInputClass!}"
                       autofocus aria-invalid="<#if messagesPerField.existsError('totp')>true</#if>"/>

                <#if messagesPerField.existsError('totp')>
                    <h4 id="input-error-otp-code" class="${properties.kcInputErrorMessageClass!}"
                          aria-live="polite">
                        ${kcSanitize(messagesPerField.get('totp'))?no_esc}
                    </h4>
                </#if>
            </div>
        </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                        <#if otpLogin.attempts??>
                            <div>
                                <p>Attempts remaining : ${otpLogin.attempts!"False"}</p>
                            </div>
                        </#if>
                        <button type="submit"
                            class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!} ${properties.kcButtonLargeClass!}"
                            id="resendSOTPBtn" name="resendOTP" value="true">Resend Verification Code
                        </button>

                    </div>
                </div>
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcLabelWrapperClass!}">
                        <h5 id="kc-otp-expiry" style="text-align:center;font-weight:bold"></h5>
                        <span id="otp-code-expired" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                        </span>
                    </div>
                </div>
                    <input
                        class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                        name="login" id="kc-login" type="submit" value="${msg("doLogIn")}" />
                </div>
            </div>
            <script>
                // Update the count down every 1 second
                var countDownDate = ${otpLogin.expireTime};
                console.log(new Date(${otpLogin.expireTime}))
                console.log(new Date(${otpLogin.expireTime}).getTime())
                var x = setInterval(function() {
                
                  // Get today's date and time
                  var now = new Date().getTime();
                  // Find the distance between now and the count down date
                  var distance = countDownDate - now;
                  // Time calculations for days, hours, minutes and seconds
                  var days = Math.floor(distance / (1000 * 60 * 60 * 24));
                  var hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
                  var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
                  var seconds = Math.floor((distance % (1000 * 60)) / 1000);
                  document.getElementById("kc-otp-expiry").innerHTML = (days ? days + "d " : "") + (hours ? hours + "h " : "")
                  + (minutes ? minutes + "m " : "") + (seconds ? seconds + "s " : "");
                  // If the count down is finished, write some text
                  if (distance < 0) {
                    clearInterval(x);
                    document.getElementById("kc-otp-expiry").innerHTML = "";
                    document.getElementById("otp-code-expired").innerHTML = "Verification code expired!";
                  }
                }, 1000);
            </script>
        </form>
    </#if>
</@layout.registrationLayout>