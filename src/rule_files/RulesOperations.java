package rule_files;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class RulesOperations {
	
	public static ArrayList<LinkedHashMap<String, String>> adjustRules(ArrayList<LinkedHashMap<String, String>> rules) {

		LinkedHashMap<String, String> old_rule;
		ArrayList<LinkedHashMap<String, String>> new_rules = new ArrayList<LinkedHashMap<String, String>>();
		boolean rule_ok = true;

		while(!rules.isEmpty()){ 
			old_rule = rules.remove(0); //We get the first rule from the list
			rule_ok = true;

			for(String old_field_key : old_rule.keySet()){ //and process all the fields until either all fields are processed or we hit a multiple field
				if (old_rule.get(old_field_key).contains(",")){ // which are fields that contains multiple instances, divided with ","
					rules = splitRule(rules, old_rule, old_field_key); //If we find one, we split the whole rule accordingly
					rule_ok = false; // and flag that the rule may not be entirely processed
					break; // so, we break the field checking and start over with the rules generated
				}
			}
			if (rule_ok){
				new_rules.add(old_rule);
				rule_ok = true;
			}
		}
		return new_rules;
	}

	private static ArrayList<LinkedHashMap<String, String>> splitRule(ArrayList<LinkedHashMap<String, String>> rules, HashMap<String, String> old_rule, String old_field_key) {

		String[] tokenized_field = old_rule.get(old_field_key).split("[,]");
		for(String token : tokenized_field){
			if(!token.isEmpty()){
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, String> new_rule = (LinkedHashMap<String, String>) old_rule.clone(); 
				new_rule.remove(old_field_key);
				new_rule.put(old_field_key, token);
				rules.add(0, new_rule);
			}
		}
		return rules;
	}
}