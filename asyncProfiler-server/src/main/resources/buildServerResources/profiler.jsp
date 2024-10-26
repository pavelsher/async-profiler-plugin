<%@ include file="/include.jsp" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="settingsBean" type="org.jetbrains.teamcity.asyncProfiler.ProfilerSettingsBean" scope="request"/>
<bs:linkScript>
</bs:linkScript>
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
        <forms:submit label="Start"/>
        <forms:saving/>
    </div>

</form>
</bs:refreshable>
