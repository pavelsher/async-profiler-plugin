<%@ include file="/include.jsp" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="settingsBean" type="org.jetbrains.teamcity.asyncProfiler.ProfilerSettingsBean" scope="request"/>
<script type="text/javascript">
    var Profiler = {
        start: function() {
            BS.ajaxRequest($('profilerForm').action, {
                parameters: 'profilerPath=' + encodeURIComponent($j('#profilerPath').val()) +
                            '&args=' + encodeURIComponent($j('#args').val()) +
                            '&startProfiler=1',
                onComplete: function(transport) {
                    if (transport.responseXML) {
                        BS.XMLResponse.processErrors(transport.responseXML, {
                            onProfilerProblemError: function(elem) {
                                alert(elem.firstChild.nodeValue);
                            }
                        });
                    }

                    $('profilerComponent').refresh();
                }
            });

            return false;
        }
    };
</script>
<c:url var="actionUrl" value="/admin/diagnostics/asyncProfiler.html"/>
<bs:refreshable containerId="profilerComponent" pageUrl="${pageUrl}">
<bs:messages key="profilerMessage"/>

<form action="${actionUrl}" id="profilerForm">
    <table class="runnerFormTable" style="margin-top: 0.5em">
        <tr class="groupingTitle">
            <td colspan="2">Async profiler settings</td>
        </tr>
        <tr>
            <th><label for="profilerPath">Path to asprof executable:</label></th>
            <td>
                <forms:textField name="profilerPath" id="profilerPath" value="${settingsBean.profilerPath}" className="longField"/>
            </td>
        </tr>
        <tr>
            <th><label for="args">Additional arguments:</label></th>
            <td>
                <forms:textField name="args" id="args" value="${settingsBean.args}" className="longField"/>
            </td>
        </tr>
        <tr>
            <th>Profiler results directory:</th>
            <td><a href="<c:url value='/admin/admin.html?item=diagnostics&tab=logs&'/>"><c:out value="${settingsBean.reportsPath}"/></a></td>
        </tr>
    </table>

    <div class="saveButtonsBlock">
        <forms:submit label="Start" onclick="return Profiler.start()" disabled="${not empty settingsBean.profilerSession and not settingsBean.profilerSession.finished}"/>
    </div>

</form>

<c:if test="${not empty settingsBean.profilerSession}">
    <pre><c:out value="${settingsBean.profilerSession.result.stdout}"/></pre>
    <c:if test="${not settingsBean.profilerSession.finished}">
    <script type="text/javascript">
        window.setTimeout(function() {
            $('profilerComponent').refresh();
        }, 3000);
    </script>
    </c:if>
</c:if>
</bs:refreshable>
