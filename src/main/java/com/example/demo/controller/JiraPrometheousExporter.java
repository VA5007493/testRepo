package com.example.demo.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.expression.ParseException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

@RestController
public class JiraPrometheousExporter {
	
	private  List<Integer> esatimatedPointsList = new ArrayList<>();
	private  List<Integer> confirmedPointsList = new ArrayList<>();
	private int noOfsprints ;

	@SuppressWarnings({ "unchecked", "unused" })
	@RequestMapping(value = "/jirametrics", produces = "text/plain")
	public String getJiraMetrics() throws Exception {
		URL url = new URL(
				"http://localhost:9000/api/resources?metrics=ncloc,coverage,tests,line_coverage,lines_to_cover,test_execution_time,test_errors,test_failures,test_success_density,uncovered_lines,uncovered_conditions,skipped_tests&depth=-1&scopes=FIL");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

		String output;
		System.out.println("Output from Server .... \n");
		List<JSONObject> json = null;
		String prometheusMetric = "";
		while ((output = br.readLine()) != null) {

			System.out.println(output);
			JSONParser parser = new JSONParser();
			json = (List<JSONObject>) parser.parse(output);

			break;
		}
		double metricValue = 0;
		int totalFilesCount = 0;
		if (json != null && !json.isEmpty()) {

			for (int i = 0; i < json.size(); i++) {
				JSONObject metricsList = json.get(i);// (List<JSONObject>) json.get("metrics");
				List<JSONObject> obj = (List<JSONObject>) metricsList.get("msr");
				if (obj != null) {
					for (JSONObject metric : obj) {
						String metricName = (String) metric.get("key");
						if (metricName != null && metricName.equalsIgnoreCase("coverage")) {

							metricValue += (double) metric.get("val");
							totalFilesCount++;
							// prometheusMetric = "sonar_"+metricName+"_count "+ metricValue;
						}
					}
					// String metricName = (String) obj.get("name");
					// String metricValue = (String) obj.get("id");
					// prometheusMetric = "sonar_"+metricName+"_count "+ metricValue;

				}
			}
		}
		double averageCoverage = 0;
		if (totalFilesCount != 0) {
			averageCoverage = metricValue / totalFilesCount;
		}

		prometheusMetric = "sonar_code_coverage " + averageCoverage;

		// Code Smells and DEBT
		int totalIssues = 0;
		long debt = 0;
		JSONObject issuesJson = getSonarIssues(1);
		
		if (issuesJson != null) {
			calculateDebt(debt,issuesJson);
			totalIssues = (int) issuesJson.get("total");
			int pageSize = (int) issuesJson.get("ps");
			int totalPages = 2;//(int) Math.ceil(totalIssues / pageSize);
			/*for (int i = 2; i <= totalPages; i++) {
				issuesJson = getSonarIssues(i);
				debt = calculateDebt(debt,issuesJson);
			}*/
		}
		long day = (int) TimeUnit.SECONDS.toDays(debt);
		long hoursValue = TimeUnit.SECONDS.toHours(debt) - (day * 24);
		String totalDebt = day + "D" + hoursValue + "H";
		prometheusMetric += "\n" + "sonar_code_smells " + totalIssues;
	//	prometheusMetric += "\n"+ "sonar_issues_debt " + totalDebt;

		return prometheusMetric;
	}

	private JSONObject getSonarIssues(int pageIndex)
			throws IOException, ParseException, net.minidev.json.parser.ParseException {
		URL issuesUrl = new URL("http://localhost:9000/api/issues/search?ps=10&p=" + pageIndex);
		HttpURLConnection issuesConn = (HttpURLConnection) issuesUrl.openConnection();

		if (issuesConn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + issuesConn.getResponseCode());
		}

		BufferedReader br1 = new BufferedReader(new InputStreamReader((issuesConn.getInputStream())));

		String issuesOutput;
		System.out.println("Output from Server .... \n");
		JSONObject issuesJson = null;
		while ((issuesOutput = br1.readLine()) != null) {

			System.out.println(issuesOutput);
			JSONParser parser = new JSONParser();
			issuesJson = (JSONObject) parser.parse(issuesOutput);
			break;
		}
		return issuesJson;
	}
private long calculateDebt(long debt,JSONObject issuesJson) throws Exception {
	
	List<JSONObject> issues = (List<JSONObject>) issuesJson.get("issues");
	if (issues != null) {
		for (JSONObject obj : issues) {
			String debtValue = (String) obj.get("debt");
			if(debtValue!= null) {
			if (debtValue.indexOf("d") != -1) {
				long daysValue = Integer.parseInt(debtValue.substring(0, debtValue.indexOf("d"))) * 24 * 60 * 60;
				if (debtValue.indexOf("h") != -1) {
					
					long hours = Integer.parseInt(debtValue.substring(debtValue.indexOf("d") +1, debtValue.indexOf("h"))) * 60 * 60;
					long minutes=0;
					if(debtValue.indexOf("m") != -1) {
					 minutes = Integer
							.parseInt(debtValue.substring(debtValue.indexOf("h") + 1, debtValue.indexOf("m"))) * 60;
					}
					debt += (daysValue + hours + minutes);

				}

			}
			if (debtValue.indexOf("d") == -1 && debtValue.indexOf("h") != -1) {
				
				long hours = Integer.parseInt(debtValue.substring(0, debtValue.indexOf("h"))) * 60 * 60;
				long minutes=0;
				if(debtValue.indexOf("m") != -1) {
				 minutes = Integer
						.parseInt(debtValue.substring(debtValue.indexOf("h") + 1, debtValue.indexOf("m"))) * 60;
				}
				debt += (hours + minutes);

			} else if(debtValue.indexOf("d") == -1 && debtValue.indexOf("h") == -1 && debtValue.indexOf("m") != -1) {
				long minutes = Integer
						.parseInt(debtValue.substring(0, debtValue.indexOf("m"))) * 60;
				debt += minutes;
			}
		}
		}
	}
	return debt;
}

	


}
