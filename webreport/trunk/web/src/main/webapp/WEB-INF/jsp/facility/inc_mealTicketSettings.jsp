<%@ include file="../shared/inc_header.jsp"%>

<div class="clearCenter" style="margin-bottom: 15px">
	<table>
		<tr>
			<td class='appFieldLabel' nowrap><label
				for='numMealsRadioSelect'>Number of Meals: <span
					class="invisibleRequiredFor508">*</span>
			</label></td>
			<td style="padding: 4px; text-align: left"><span
				class='requdIndicator'>*</span></td>
			<td style="text-align: left" nowrap><label><form:radiobutton
						id="num0MealsRadioSelect"
						path="facility.stationParameters.numberOfMeals" value="0" />0</label> <label><form:radiobutton
						id="num1MealsRadioSelect"
						path="facility.stationParameters.numberOfMeals" value="1" />1</label> <label><form:radiobutton
						id="num2MealsRadioSelect"
						path="facility.stationParameters.numberOfMeals" value="2" />2</label><label><form:radiobutton
						id="num3MealsRadioSelect"
						path="facility.stationParameters.numberOfMeals" value="3" />3</label></td>
		</tr>
		<tr>
			<td colspan="2"></td>
			<td><app:errors path="facility.stationParameters.numberOfMeals"
					cssClass="msg-error" /></td>
		</tr>
		<tr class="meal1" style="display: none">
			<td class='appFieldLabel'>Meals Allowed on Days:</td>
			<td></td>
			<td style="text-align: left" nowrap><label><form:checkbox
						path="facility.stationParameters.saturdayMeal" id="saturdayMeal" />Saturday</label>
				<label><form:checkbox
						path="facility.stationParameters.sundayMeal" id="sundayMeal" />Sunday</label>
				<label><form:checkbox
						path="facility.stationParameters.holidayMeal" id="holidayMeal" />Holidays</label>
			</td>
		</tr>
		<tr class="meal1" style="display: none">
			<td class='appFieldLabel' nowrap><label
				for='mealAuthorizationTypeRadio'>Meal Authorization Type:</label></td>
			<td></td>
			<td style="text-align: left"><label><form:radiobutton
						id="mealAuthorizationTypeRadio1"
						path="facility.stationParameters.mealAuthorization" value="L" />List</label>
				<label><form:radiobutton id="mealAuthorizationTypeRadio2"
						path="facility.stationParameters.mealAuthorization" value="T" />Ticket</label>
				<app:errors path="facility.stationParameters.mealAuthorization"
					cssClass="msg-error" /></td>
		</tr>
		<tr class="meal1" style="display: none">
			<td class='appFieldLabel' nowrap>Meal Price $:<span
				class="invisibleRequiredFor508">*</span></td>
			<td style="padding: 4px; text-align: left" width="5%"><span
				class='requdIndicator'>*</span></td>
			<td><app:input path="facility.stationParameters.mealPrice"
					id="mealPrice" class="currency" maxlength="5" size="10" /> <app:errors
					path="facility.stationParameters.mealPrice" cssClass="msg-error" /></td>
		</tr>
		<tr class="meal1" style="display: none">
			<td class='appFieldLabel' nowrap>Required Hours for 1 Meal:<span
				class="invisibleRequiredFor508">*</span></td>
			<td style="padding: 4px; text-align: left" width="5%"><span
				class='requdIndicator'>*</span></td>
			<td><app:input path="facility.stationParameters.meal1Duration"
					id="meal1Duration" class="currency" maxlength="5" size="10" /> <app:errors
					path="facility.stationParameters.meal1Duration"
					cssClass="msg-error" /></td>
			<td class='appFieldLabel' nowrap>Cutoff Time for 1 Meal:<span
				class="invisibleRequiredFor508">*</span></td>
			<td style="padding: 4px; text-align: left" width="5%"><span
				class='requdIndicator'>*</span></td>
			<td><app:input path="facility.stationParameters.meal1CutoffTime"
					id="meal1CutoffTime" maxlength="4" size="10" /> <app:errors
					path="facility.stationParameters.meal1CutoffTime"
					cssClass="msg-error" /></td>
		</tr>
		<tr class="meal2" style="display: none">
			<td class='appFieldLabel' nowrap>Required Hours for 2 Meals:<span
				class="invisibleRequiredFor508">*</span></td>
			<td style="padding: 4px; text-align: left" width="5%"><span
				class='requdIndicator'>*</span></td>
			<td><app:input path="facility.stationParameters.meal2Duration"
					id="meal2Duration" maxlength="5" size="10" /> <app:errors
					path="facility.stationParameters.meal2Duration"
					cssClass="msg-error" /></td>
			<td class='appFieldLabel' nowrap>Cutoff Time for 2 Meals:<span
				class="invisibleRequiredFor508">*</span></td>
			<td style="padding: 4px; text-align: left" width="5%"><span
				class='requdIndicator'>*</span></td>
			<td><app:input path="facility.stationParameters.meal2CutoffTime"
					id="meal2CutoffTime" maxlength="4" size="10" /> <app:errors
					path="facility.stationParameters.meal2CutoffTime"
					cssClass="msg-error" /></td>
		</tr>
		<tr class="meal3" style="display: none">
			<td class='appFieldLabel' nowrap>Required Hours for 3 Meals:<span
				class="invisibleRequiredFor508">*</span></td>
			<td style="padding: 4px; text-align: left" width="5%"><span
				class='requdIndicator'>*</span></td>
			<td><app:input path="facility.stationParameters.meal3Duration"
					id="meal3Duration" maxlength="5" size="10" /> <app:errors
					path="facility.stationParameters.meal3Duration"
					cssClass="msg-error" /></td>
			<td class='appFieldLabel' nowrap>Cutoff Time for 3 Meals:<span
				class="invisibleRequiredFor508">*</span></td>
			<td style="padding: 4px; text-align: left" width="5%"><span
				class='requdIndicator'>*</span></td>
			<td><app:input path="facility.stationParameters.meal3CutoffTime"
					id="meal3CutoffTime" maxlength="4" size="10" /> <app:errors
					path="facility.stationParameters.meal3CutoffTime"
					cssClass="msg-error" /></td>
		</tr>

	</table>
</div>
