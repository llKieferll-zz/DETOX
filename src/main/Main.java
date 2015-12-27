package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import anomaly_files.Anomaly;
import anomaly_files.Finder;
import config_file.Field_Config;
import config_file.Field_Config.Variable;
import csv_reader.Reader;
import rule_files.RulesOperations;


public class Main {

	public static void main(String[] args) {

		boolean no_action_field = true;
		for (Variable var : Field_Config.Variable.values()){
			if (var.isActive()){
				if (var.getName().equalsIgnoreCase("ACTION")) {
					no_action_field = false; 
					break;
				}
			}
		}

		if (no_action_field) {
			System.err.println("There is no ACTION field in desired FIELDS or it is not an active field. "
					+ "Please, add or activate the ACTION field in file \"Field_Config.java\", inside \"Active\" enum.");
			System.exit(0);
		}

		Reader reader = new Reader("regras.csv");
		ArrayList<LinkedHashMap<String, String>> rules;

		long startTimeReading = System.nanoTime(); // Start timer for CSV reading
		rules = reader.readAll();
		long endTimeReading = System.nanoTime(); // Stop timer for CSV reading

		int size_before = rules.size();
		long startTimeParsing = System.nanoTime(); // Start timer for rules adjusting (breaking rules with "," into atomic rules)
		rules = RulesOperations.adjustRules(rules);
		long endTimeParsing = System.nanoTime(); // Stop timer for rules adjusting
		int size_after = rules.size();
		int total_elements = size_after * Field_Config.Variable.values().length;
		///////////////////////////////////////
		///////////////////////////////////////
		///////////////////////////////////////		
		//GETTING ALL ENABLED ALIAS INTO FILE//
		///////////////////////////////////////
		///////////////////////////////////////
		///////////////////////////////////////

		//		BufferedWriter output = null;
		//		File file = new File("Alias unique.txt");
		//		try {
		//			output = new BufferedWriter(new FileWriter(file));
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//
		//		ArrayList<String> aliases = new ArrayList<String>();
		//		for(LinkedHashMap<String, String> rule : rules){
		//			for(Entry<String, String> entry : rule.entrySet()){
		//				if (entry.getKey().equalsIgnoreCase("SRC_IP") || entry.getKey().equalsIgnoreCase("DST_IP")){
		//					if(!aliases.contains(entry.getValue())) {
		//						if (!entry.getValue().startsWith("1") && !entry.getValue().startsWith("2")) aliases.add(entry.getValue());
		//					}
		//				}
		//			}
		//		}
		//		
		//		for(String alias : aliases){
		//			try {
		//				output.write(alias);
		//				output.newLine();
		//			} catch (IOException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//		}
		//		
		//		if(output != null)
		//			try {
		//				output.close();
		//			} catch (IOException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		///////////////////////////////////////
		///////////////////////////////////////
		///////////////////////////////////////
		///////////////////////////////////////
		///////////////////////////////////////
		///////////////////////////////////////


		ArrayList<String> Aliases = new ArrayList<String>();
		ArrayList<String> Aliases_Values = new ArrayList<String>();
		try {
			File file = new File("Alias.txt");
			FileReader fileReader;
			fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			String Split[];
			while ((line = bufferedReader.readLine()) != null) {
				Split = line.split(" ");
				Aliases.add(Split[0]);
				Aliases_Values.add(Split[1]);
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		for(LinkedHashMap<String, String> rule : rules){
			
			if(Aliases.contains(rule.get("SRC_IP"))){
				rule.replace("SRC_IP", Aliases_Values.get(Aliases.indexOf(rule.get("SRC_IP"))));
			}
			
			if(Aliases.contains(rule.get("DST_IP"))){
				rule.replace("DST_IP", Aliases_Values.get(Aliases.indexOf(rule.get("DST_IP"))));
			}
			
		}
		
		//Print the new set of rules (all are atomic now...hopefully)

		ArrayList<Field_Config.Variable> active_variables = new ArrayList<Field_Config.Variable>();
		for(Variable field : Field_Config.Variable.values()){
			if(field.isActive()) {
				if(!field.getName().equalsIgnoreCase("ENABLED")){
					active_variables.add(field);
				}
			}
		}
//		
//		int counter = 1;
//		for(LinkedHashMap<String, String> rule : rules){
//			System.out.print(counter + ": ");
//			for(Variable field : active_variables){
//				System.out.print(rule.get(field.getName()) + " | ");
//			}
//			System.out.println();
//			counter++;
//		}
//		System.out.println();

		long startTimeFinding = System.nanoTime(); // Start timer for anomaly finder
		HashMap<LinkedHashMap<String, String>, Anomaly> anomaly_list = Finder.find(rules);
		long endTimeFinding = System.nanoTime(); // Stop timer for anomaly finder

		List<Anomaly> anomaly_array = new ArrayList<Anomaly>(anomaly_list.values());
		Collections.sort(anomaly_array);
		
		int absolute_correlation = 0;
		HashSet<LinkedHashMap<String, String>> percentage_correlation = new HashSet<LinkedHashMap<String, String>>();
		int absolute_generalization = 0;
		HashSet<LinkedHashMap<String, String>> percentage_generalization = new HashSet<LinkedHashMap<String, String>>();
		int absolute_redundancy = 0;
		HashSet<LinkedHashMap<String, String>> percentage_redundancy = new HashSet<LinkedHashMap<String, String>>();
		int absolute_shadowing = 0;
		HashSet<LinkedHashMap<String, String>> percentage_shadowing = new HashSet<LinkedHashMap<String, String>>();
		HashSet<LinkedHashMap<String, String>> total_inconsistencies = new HashSet<LinkedHashMap<String, String>>();
		
		for(Anomaly anomaly : anomaly_array){
			
			absolute_correlation += anomaly.getCorrelation().size();
			absolute_generalization += anomaly.getGeneralization().size();
			absolute_redundancy += anomaly.getRedundancy().size();
			absolute_shadowing += anomaly.getShadowing().size();
			
			//rule
			LinkedHashMap<String, String> anomaly_rule = new LinkedHashMap<String, String>();
			anomaly_rule = anomaly.getRule();
			System.out.print("RULE Nº " + anomaly.getID() + ": ");
			
			for(Variable field : active_variables){
				String field_value = anomaly_rule.get(field.getName());
				if(Aliases_Values.contains(field_value)){
					field_value = Aliases.get(Aliases_Values.indexOf(field_value));
				}
				if(field_value.equalsIgnoreCase("*.*.*.*")){
					System.out.print("* . * . * . *\t|\t");
				}else{
					if(field_value.length() < 7) System.out.print(field_value + "\t\t|\t");
					else System.out.print(field_value + "\t|\t");
				}
			}
			System.out.println();

			HashMap<LinkedHashMap<String, String>, Integer> anomaly_list_iterator;
			
			//correlation
			anomaly_list_iterator = anomaly.getCorrelation();
			for(LinkedHashMap<String, String> rule : anomaly_list_iterator.keySet()){
				
				if(!percentage_correlation.contains(rule)) percentage_correlation.add(rule);
				if(!percentage_correlation.contains(anomaly_rule)) percentage_correlation.add(anomaly_rule);
				
				if(!total_inconsistencies.contains(rule)) total_inconsistencies.add(rule);
				if(!total_inconsistencies.contains(anomaly_rule)) total_inconsistencies.add(anomaly_rule);
				
				System.out.print("corr to ");
				System.out.print(anomaly_list_iterator.get(rule) + ": ");
				for(Variable field : active_variables){
					String field_value = rule.get(field.getName());
					if(Aliases_Values.contains(field_value)){
						field_value = Aliases.get(Aliases_Values.indexOf(field_value));
					}
					if(field_value.equalsIgnoreCase("*.*.*.*")){
						System.out.print("* . * . * . *\t|\t");
					}else{
						if(field_value.length() < 7) System.out.print(field_value + "\t\t|\t");
						else System.out.print(field_value + "\t|\t");
					}
				}
				System.out.println();
			}



			//generalization
			anomaly_list_iterator = anomaly.getGeneralization();
			for(LinkedHashMap<String, String> rule : anomaly_list_iterator.keySet()){
				
				if(!percentage_generalization.contains(rule)) percentage_generalization.add(rule);
				if(!percentage_generalization.contains(anomaly_rule)) percentage_generalization.add(anomaly_rule);
				
				if(!total_inconsistencies.contains(rule)) total_inconsistencies.add(rule);
				if(!total_inconsistencies.contains(anomaly_rule)) total_inconsistencies.add(anomaly_rule);
				
				System.out.print("gene of ");
				System.out.print(anomaly_list_iterator.get(rule) + ": ");
				for(Variable field : active_variables){
					String field_value = rule.get(field.getName());
					if(Aliases_Values.contains(field_value)){
						field_value = Aliases.get(Aliases_Values.indexOf(field_value));
					}
					if(field_value.equalsIgnoreCase("*.*.*.*")){
						System.out.print("* . * . * . *\t|\t");
					}else{
						if(field_value.length() < 7) System.out.print(field_value + "\t\t|\t");
						else System.out.print(field_value + "\t|\t");
					}
				}
				System.out.println();
			}



			//redundancy
			anomaly_list_iterator = anomaly.getRedundancy();
			for(LinkedHashMap<String, String> rule : anomaly_list_iterator.keySet()){
				
				if(!percentage_redundancy.contains(rule)) percentage_redundancy.add(rule);
				if(!percentage_redundancy.contains(anomaly_rule)) percentage_redundancy.add(anomaly_rule);
				
				if(!total_inconsistencies.contains(rule)) total_inconsistencies.add(rule);
				if(!total_inconsistencies.contains(anomaly_rule)) total_inconsistencies.add(anomaly_rule);
				
				System.out.print("redu to ");
				System.out.print(anomaly_list_iterator.get(rule) + ": ");
				for(Variable field : active_variables){
					String field_value = rule.get(field.getName());
					if(Aliases_Values.contains(field_value)){
						field_value = Aliases.get(Aliases_Values.indexOf(field_value));
					}
					if(field_value.equalsIgnoreCase("*.*.*.*")){
						System.out.print("* . * . * . *\t|\t");
					}else{
						if(field_value.length() < 7) System.out.print(field_value + "\t\t|\t");
						else System.out.print(field_value + "\t|\t");
					}
				}
				System.out.println();
			}



			//shadowing
			anomaly_list_iterator = anomaly.getShadowing();
			for(LinkedHashMap<String, String> rule : anomaly_list_iterator.keySet()){
				
				if(!percentage_shadowing.contains(rule)) percentage_shadowing.add(rule);
				if(!percentage_shadowing.contains(anomaly_rule)) percentage_shadowing.add(anomaly_rule);
				
				if(!total_inconsistencies.contains(rule)) total_inconsistencies.add(rule);
				if(!total_inconsistencies.contains(anomaly_rule)) total_inconsistencies.add(anomaly_rule);
				
				System.out.print("shad by ");
				System.out.print(anomaly_list_iterator.get(rule) + ": ");
				for(Variable field : active_variables){
					String field_value = rule.get(field.getName());
					if(Aliases_Values.contains(field_value)){
						field_value = Aliases.get(Aliases_Values.indexOf(field_value));
					}
					if(field_value.equalsIgnoreCase("*.*.*.*")){
						System.out.print("* . * . * . *\t|\t");
					}else{
						if(field_value.length() < 7) System.out.print(field_value + "\t\t|\t");
						else System.out.print(field_value + "\t|\t");
					}
				}
				System.out.println();
			}
			System.out.println();
		}

		System.out.println("Rules before: " + size_before);
		System.out.println("Rules after: " + size_after);
		System.out.println("Total elements: " + total_elements);
		System.out.println();
		
		System.out.println("A total of " + absolute_correlation + " correlations were found.");
		System.out.println(percentage_correlation.size() + " out of " + size_after + " have a correlation inconsistency (" + (float)(100.0 * (float)percentage_correlation.size()/(float)size_after) + "%)");
		System.out.println();
		
		System.out.println("A total of " + absolute_generalization + " generalizations were found.");
		System.out.println(percentage_generalization.size() + " out of " + size_after + " have a generalization inconsistency (" + (float)(100.0 * (float)percentage_generalization.size()/(float)size_after) + "%)");
		System.out.println();
		
		System.out.println("A total of " + absolute_redundancy + " redundancy were found.");
		System.out.println(percentage_redundancy.size() + " out of " + size_after + " have a redundancy inconsistency (" + (float)(100.0 * (float)percentage_redundancy.size()/(float)size_after) + "%)");
		System.out.println();
		
		System.out.println("A total of " + absolute_shadowing + " shadowing were found.");
		System.out.println(percentage_shadowing.size() + " out of " + size_after + " have a shadowing inconsistency (" + (float)(100.0 * (float)percentage_shadowing.size()/(float)size_after) + "%)");
		System.out.println();
		
		System.out.println("A total of " + total_inconsistencies.size() + " rules showed inconsistencies (" + (float)(100.0 * (float)total_inconsistencies.size()/(float)size_after) + "%)");
		System.out.println();
		// Timers are all in nanosec, so we need to convert to millisec. Hence, the the "/1000000"
		System.out.println("Reading the CSV took " + (endTimeReading - startTimeReading)/1000000 + " milliseconds");
		System.out.println("Parsing (adjusting) the rules took " + (endTimeParsing - startTimeParsing)/1000000 + " milliseconds");
		System.out.println("Finding anomalies took " + (endTimeFinding - startTimeFinding)/1000000 + " milliseconds");
	}

}
