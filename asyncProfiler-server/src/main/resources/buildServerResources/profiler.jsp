<%@ include file="/include.jsp" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="settingsBean" type="org.jetbrains.teamcity.asyncProfiler.ProfilerSettingsBean" scope="request"/>
<script type="text/javascript">
    var Profiler = {
        start: function() {
            BS.ajaxRequest($('profilerForm').action, {
                parameters: 'args=' + encodeURIComponent($j('#args').val()) +
                            '&sessionName=' + encodeURIComponent($j('#sessionName').val()) +
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
            <th>Path to asprof executable:</th>
            <td>
                <c:out value="${settingsBean.profilerPath}"/>
            </td>
        </tr>
        <tr>
            <th><label for="args">Async profiler additional arguments:</label></th>
            <td>
                <forms:textField name="args" id="args" value="${settingsBean.args}" className="longField"/>
            </td>
        </tr>
        <tr>
            <th><label for="sessionName">Profiler session name:</label></th>
            <td>
                <forms:textField name="sesstionName" id="sessionName" value="${settingsBean.sessionName}" className="longField"/>
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
    <c:set var="session" value="${settingsBean.profilerSession}"/>
    <pre>Command line: <c:out value="${session.commandLine}"/></pre>
    <c:if test="${not session.finished}">
    <pre><c:out value="${session.result.stdout}"/></pre>
    <script type="text/javascript">
        window.setTimeout(function() {
            $('profilerComponent').refresh();
        }, 3000);
    </script>
    </c:if>
    <c:if test="${session.finished}">
        <c:if test="${session.failed}">
            <pre style="color:red">Execution failed</pre>
            <c:if test="${not empty session.result.exception}">
                <pre style="color:red"><c:out value="${session.result.exception.message}"/></pre>
            </c:if>
        </c:if>
        <c:if test="${not empty session.result.stdout}">
            <pre><c:out value="${session.result.stdout}"/></pre>
        </c:if>
        <c:if test="${not empty session.result.stderr}">
            <pre style="color: chocolate"><c:out value="${session.result.stderr}"/></pre>
        </c:if>
        <c:if test="${not session.failed}">
            <a class="downloadLink tc-icon_before icon16 tc-icon_download" style="float:none" href="<c:url value='/get/file/serverLogs/${session.reportPath}?forceAttachment=true'/>">Profiler report</a>
        </c:if>
    </c:if>
</c:if>
</bs:refreshable>
