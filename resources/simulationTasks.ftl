<html>
<head>
<title>Simulation Tasks</title>
</head>
<body>
<center><h2><a href="/biomodel">BioModels</a>&nbsp;&nbsp;&nbsp;Simulation Tasks&nbsp;&nbsp;&nbsp;<#if userid?? >(user: ${userid})<#else>(not logged in)</#if></h2></center><br/><center>
<form>
<table><tbody>
<tr><td>Begin Timestamp</td><td><input type='text' name='submitLow' value='${submitLow!""}'/></td></tr>
<tr><td>End Timestamp</td><td><input type='text' name='submitHigh' value='${submitHigh!""}'/></td></tr>
<tr><td>max num rows</td><td><input type='text' name='maxRows' value='${maxRows}'/></td></tr>
<tr><td>ServerID</td><td><input type='text' name='serverId' value='${serverId!""}'/></td></tr>
<tr><td>Compute Host</td><td><input type='text' name='computeHost value='${computeHost!""}'/></td></tr>
<tr><td>Simulation ID</td><td><input type='text' name='simId' value='${simId!""}'/></td></tr>
<tr><td>Job ID (parameter scan index)</td><td><input type='text' name='jobId' value='${jobId!""}'/></td></tr>
<tr><td>Task ID (retry index)</td><td><input type='text' name='taskId' value='${taskId!""}'/></td></tr>
<tr><td>Has Data</td><td><input type='radio' name='hasData' value='all' <#if (!hasData??) || (hasData == "all")>checked=on</#if>>all</input>
						<input type='radio' name='hasData' value='yes' <#if hasData?? && hasData == "yes">checked=on</#if>>yes</input>
						<input type='radio' name='hasData' value='no' <#if hasData?? && hasData == "no">checked=on</#if>>no</input>
						</td></tr>


</tbody></table>
<br/>Simulation Status (choose at least one)<br/>
<table><tbody>
<tr><td>waiting</td><td><input type='checkbox' name='waiting' <#if waiting>checked='on'</#if>/></td><td>queued</td><td><input type='checkbox' name='queued' <#if queued>checked='on'</#if>/></td></tr>
<tr><td>dispatched</td><td><input type='checkbox' name='dispatched' <#if dispatched>checked='on'</#if>/></td><td>running</td><td><input type='checkbox' name='running' <#if running>checked='on'</#if>/></td></tr>
<tr><td>completed</td><td><input type='checkbox' name='completed' <#if completed>checked='on'</#if>/></td><td>failed</td><td><input type='checkbox' name='failed' <#if failed>checked='on'</#if>'/></td></tr>
<tr><td>stopped</td><td><input type='checkbox' name='stopped' <#if stopped>checked='on'</#if>/></td></tr>
</tbody></table>
<input type='submit' value='Search' style='font-size:large'>
</form></center>
<br/><h3>query returned ${simTasks?size} results</h3>
<table border='1'>
<tr>
<th>BioModel</th>
<th>BioModel App</th>
<th>MathModel</th>
<th>Simulation</th>
<th>User Name</th>
<th>User Key</th>
<th>Job Index</th>
<th>Task ID</th>
<th>HTC JobID</th>
<th>has data</th>
<th>Status</th>
<th>Start Date</th>
<th>Message</th>
<th>Site</th>
<th>Compute Host</th>
</tr>
<#list simTasks as simTask>
<tr>
<td><#if simTask.bioModelLink??><a href='./biomodel/${simTask.bioModelLink.bioModelKey}'>${simTask.bioModelLink.bioModelName!""}</a><#else>unknown</#if></td>
<td><#if simTask.bioModelLink??><a href='./application/${simTask.bioModelLink.simContextKey!""}'>${simTask.bioModelLink.simContextName!""}</a><#else>unknown</#if></td>
<td><#if simTask.mathModelLink??>"${simTask.mathModelLink.mathModelName!""}" (id=${simTask.mathModelLink.mathModelKey}) (branch=${simTask.mathModelLink.mathModelBranchId})<#else>unknown</#if></td>
<td>"${simTask.simName!""}" (id=${simTask.simKey!""})</td>
<td>${simTask.userName!""}</td>
<td>${simTask.userKey!""}</td>
<td>${simTask.jobIndex!""}</td>
<td>${simTask.taskId!""}</td>
<td>${simTask.htcJobId!"unknown"}</td>
<td><#if simTask.hasData>true<#else>false</#if></td>
<td>${simTask.status!""}</td>
<td>${simTask.startdate?number_to_date!""}</td>
<td>${simTask.message!""}</td>
<td>${simTask.site!""}</td>
<td><#if simTask.computeHost??>${simTask.computeHost!""}<#else>unknown</#if></td>
</tr>
</#list>
</table>
<br/>
<#if jsonResponse??> JSON response <br/>
${jsonResponse}
<br/>
</#if>

</html>
