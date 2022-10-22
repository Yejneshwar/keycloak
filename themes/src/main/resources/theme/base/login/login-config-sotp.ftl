<#import "template.ftl" as layout>
<@layout.registrationLayout displayRequiredFields=false displayMessage=!messagesPerField.existsError('sotp'); section>

    <#if section = "header">
        ${msg("loginSotpTitle")}
    <#elseif section = "form">

        <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-sotp-settings-form" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcInputWrapperClass!}">
                    <label for="sotp" class="control-label">${msg("authenticatorCode")}</label> <span class="required">*</span>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="text" id="sotp" name="sotp" autocomplete="off" class="${properties.kcInputClass!}"
                           aria-invalid="<#if messagesPerField.existsError('sotp')>true</#if>"
                    />

                    <#if messagesPerField.existsError('sotp')>
                        <span id="input-error-otp-code" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('sotp'))?no_esc}
                        </span>
                    </#if>

                </div>
            </div>
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <h5 id="kc-otp-expiry" style="text-align:center;font-weight:bold"></h5>
                    <span id="otp-code-expired" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                    </span>
                </div>
            </div>
            <script>
                // Update the count down every 1 second
                var countDownDate = ${sotp.expireTime};
                console.log(new Date(${sotp.expireTime}))
                console.log(new Date(${sotp.expireTime}).getTime())
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
                <input type="submit"
                       class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                       id="saveSOTPBtn" value="${msg('doSubmit')}"
                />
                <button type="submit"
                        class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!} ${properties.kcButtonLargeClass!}"
                        id="cancelSOTPBtn" name="cancel-aia" value="true">${msg("doCancel")}
                </button>
                <button type="submit"
                    class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!} ${properties.kcButtonLargeClass!}"
                    id="cancelSOTPBtn" name="resendOTP" value="true">Resend OTP
                </button>
        </form>

        <#if sotp.attempts??>
            <div>
                <p>Attempts remaining : ${sotp.attempts!"False"}</p>
            </div>
        </#if>

    </#if>
</@layout.registrationLayout>