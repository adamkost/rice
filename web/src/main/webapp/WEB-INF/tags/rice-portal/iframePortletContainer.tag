<%--
 Copyright 2005-2009 The Kuali Foundation
 
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl2.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/rice-portal/jsp/sys/riceTldHeader.jsp" %>

<%@ attribute name="channelTitle" required="true" %>
<%@ attribute name="channelUrl" required="true" %>
<%@ attribute name="appContextName" required="true" %>

<script type="text/javascript">
  var getLocation = function(href) {
    var location = document.createElement("a");
    location.href = href;
    return location;
  };

  var channelLocation = getLocation("${channelUrl}");
  var channelProtocolHost = channelLocation.protocol + '//' + channelLocation.host + "/${appContextName}/";

  var remote;
  if (jQuery.browser.msie){
    remote = channelProtocolHost + "rice-portal/scripts/easyXDM/resize_intermediate.html?url=/"
            + encodeURIComponent(channelLocation.pathname + channelLocation.search);
  } else {
    remote = channelProtocolHost + "rice-portal/scripts/easyXDM/resize_intermediate.html?url="
            + encodeURIComponent(channelLocation.pathname + channelLocation.search);
  }

  new easyXDM.Socket(/** The configuration */{
    remote: remote,
    swf: channelProtocolHost + "rice-portal/scripts/easyXDM/easyxdm.swf",
    container: "embedded",
    props: {
      style: {
        width: "100%"
      }
    },
    onMessage: function(message, origin) {
      var availableHeight = window.innerHeight - 250;
      if (availableHeight > message) {
        this.container.getElementsByTagName("iframe")[0].style.height = availableHeight + "px";
      } else {
        this.container.getElementsByTagName("iframe")[0].style.height = message + "px";
      }
    }
  });

</script>

<div id="embedded">
</div>
