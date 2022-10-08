<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','email','firstName','lastName'); section>
    <#if section = "header">
        ${msg("loginProfileTitle")}
    <#elseif section = "form">
        <form id="kc-update-profile-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">

            <div>
                    <input type="hidden" value="${(user.phoneNumberLocale!'')}" class="default">
            </div>
            <div class="${properties.kcFormGroupClass!}">
                <div id="selected" class="selected" onclick="myFunction()">
                
                    <#--  <img width="50" height="13">
		            <div class="${properties.kcLoginOTPListItemTitleClass!} country-name"></div>	  -->
                </div>
                <div class="selector">
                <#--  <div>hello</div>  -->
                <div class="${properties.kcInputWrapperClass!} options ${user.selectedLocale?if_exists}" id="Dropdown">
                 <input type="text" placeholder="Search.." id="Input" onclick="stop()" onkeyup="filterFunction()">
                    <#list user.localeData as data>
                            <div class="option" id="option">
                                <input type="hidden" value="${data.ISOName}" id="freemarkervar">
                                <img src="data:image/png;base64,${data.flag}" alt="${data.ISOName}" width="50" height="13">
                                <div class="${properties.kcLoginOTPListItemTitleClass!} calling-code">  +${data.callingCode}</div>
		                        <div class="${properties.kcLoginOTPListItemTitleClass!} country-name"> ${data.name}</div>	                        
                            </div>
                    </#list>
                </div>
                </div>
            </div>

            <#--  <div class="${properties.kcFormGroupClass!}">
                <div class="dropdown">
  <div onclick="myFunction()" class="dropbtn">Dropdown</div>
  <div id="myDropdown" class="dropdown-content">
    <input type="text" placeholder="Search.." id="myInput" onkeyup="filterFunction()">
<#list user.localeData as data>
                            <div class="option">
                                <input type="hidden" value="${data.ISOName}" id="freemarkervar">
                                <img src="data:image/png;base64,${data.flag}" alt="${data.ISOName}" width="50" height="13">
                                <div class="${properties.kcLoginOTPListItemTitleClass!} calling-code">  ${data.callingCode}</div>
		                        <div class="${properties.kcLoginOTPListItemTitleClass!} country-name"> ${data.name}</div>	                        
                            </div>
                    </#list>
  </div>
</div>
            </div>  -->
                    


            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="phoneNumber" class="${properties.kcLabelClass!}">${msg("phoneNumber")}</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="text" id="phoneNumber" name="phoneNumber" value="${(user.phoneNumber!'')}"
                           class="${properties.kcInputClass!}"
                           aria-invalid="<#if messagesPerField.existsError('phoneNumber')>true</#if>"
                    />

                    <#if messagesPerField.existsError('phoneNumber')>
                        <span id="input-error-email" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('phoneNumber'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>

           

            
            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <#if isAppInitiatedAction??>
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}" />
                    <button class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}" type="submit" name="cancel-aia" value="true" />${msg("doCancel")}</button>
                    <#else>
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}" />
                    </#if>
                </div>
            </div>
        </form>
        <script type="text/javascript" src="${url.resourcesCommonPath}/node_modules/jquery/dist/jquery.min.js"></script>
            <script type="text/javascript">
            function myFunction() {
                event.stopPropagation();
                document.getElementById("Dropdown").classList.toggle("show");
            }
            function stop() {
                event.stopPropagation();
                <#--  document.getElementById("Dropdown").classList.toggle("show");  -->
            }
            //function showPosition(position) {
            // $.getJSON('http://maps.googleapis.com/maps/api/geocode/json?', {
            //            latlng: position.coords.latitude+','+position.coords.longitude
            //        }, function(result) {
            //            alert(result.countryName);
            //        });
            //}
            function filterFunction() {
              var input, filter, ul, li, a, i;
              input = document.getElementById("Input");
              filter = input.value.toUpperCase();
              div = document.getElementById("Dropdown");
              a = div.getElementsByClassName("country-name");
              for (i = 0; i < a.length; i++) {
                txtValue = a[i].textContent || a[i].innerText;
                txtValue.toUpperCase().indexOf(filter) > -1 ? a[i].parentElement.style.display = "inline-flex" : a[i].parentElement.style.display = "none";
              }
            }

            $(document).ready(function() {
                $(window).click(function() {
                    console.log("hello")
                    
                    document.getElementById("Dropdown").classList.remove("show");
                });
                // Country Single Select
                $('.option').click(function(event) {
                    console.log(event)
                    
                    
                    //if (navigator.geolocation) {
                    //navigator.geolocation.getCurrentPosition(showPosition);
                    //}

                    
                    console.log(defaultLocale)
                    console.log("click")
                  if ($(this).hasClass('pf-m-selected'))
                  { $(this).removeClass('pf-m-selected'); $(this).children().removeAttr('name'); $(this).children().removeAttr('id');$(this).removeAttr('id', 'clone'); }
                  else
                  { $('.option').removeClass('pf-m-selected');
                  $('.option').children().removeAttr('name');
                  $('.option').children().removeAttr('id');
                  $('.option').removeAttr('id', 'clone');
                  $(this).addClass('pf-m-selected'); $(this).children('input').attr('name', 'selectedLocale');  $(this).children('input').attr('id', 'freemarkervar');$(this).attr('id', 'clone');
                  document.getElementById("selected").removeChild(document.getElementById("selected").firstChild) 
                  document.getElementById("selected").appendChild(document.getElementById("clone").cloneNode(true)) 
                  $(".selected").children().children().removeAttr('name') 
                  $(".selected").children().children('input').attr('name',"phoneNumberLocale") 
                  $(".selected").children().children('input').attr('for',"phoneNumberLocale") 
                  $(".selected").children().children('input').attr('id',"phoneNumberLocale") 
                  }
                  var val=document.getElementById("freemarkervar").value;  
                    console.log(val);
                });


                var defaultLocale = $('.option').children("input").filter(function( index,element ) {
                return element.value == $('.default')[0].value;
                });
                
                if (defaultLocale.length != 0) {
                    console.log("click")
                    defaultLocale.click();
                }
                else {
                    defaultLocale = $(".option")[0]
                    if (defaultLocale){
                        defaultLocale.click();
                    }
                }
              });
            </script>
    </#if>
</@layout.registrationLayout>
<style>
img{
    border-radius:20px;
}
.selector{
    position:relative;
}
.options{
    display:none;
    border-radius:2px;
    box-shadow:0px 20px 50px -10px black;
    width:100%;
    max-height:500px;
    background-color:whitesmoke;
    z-index:20;
    position:absolute;
    overflow-y:auto;
    
}
.option{
    border-radius:20px;
    transition:all 0.2s ease-in-out;
    display:inline-flex;
    width:100%;
    align-items:center;
    margin-bottom:2%;
}
.option:hover{
    background-color:grey;
}
.selected{
    transition:all 0.2s ease-in-out;
    display:inline-flex;
    width:100%;
    align-items:center;
    margin-bottom:2%;
}
.calling-code{
    width:15%;
}
.country-name{
    width:50%;
    margin:0!important;
}
.option > div,img{
    margin-right:10%;
}




.dropbtn {
  background-color: #04AA6D;
  color: white;
  padding: 16px;
  font-size: 16px;
  border: none;
  cursor: pointer;
}

.dropbtn:hover, .dropbtn:focus {
  background-color: #3e8e41;
}

#Input {
  top:0;
  position: -webkit-sticky;
  position: sticky;
  box-sizing: border-box;
  background-image: url('searchicon.png');
  background-position: 14px 12px;
  background-repeat: no-repeat;
  font-size: 16px;
  padding: 14px 20px 12px 45px;
  border: none;
  border-radius:2px;
  border-bottom: 2px solid #ddd;
  width:100%;
  margin-top:5%;
  margin-bottom:5%;
}

#Input:focus {outline: 2px solid #ddd;}

.dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-content {
  display: none;
  position: absolute;
  background-color: #f6f6f6;
  min-width: 230px;
  overflow: auto;
  border: 1px solid #ddd;
  z-index: 1;
}

.dropdown-content a {
  color: black;
  padding: 12px 16px;
  text-decoration: none;
  display: block;
}

.dropdown a:hover {background-color: #ddd;}

.show {display: block;}
</style>