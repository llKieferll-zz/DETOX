package anomaly_files;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Anomaly implements Comparable<Anomaly>{
	private final int rule_id;
	private LinkedHashMap<String, String> rule;
	private HashMap<LinkedHashMap<String, String>, Integer> Shadowing = new HashMap<LinkedHashMap<String, String>, Integer>();
	private HashMap<LinkedHashMap<String, String>, Integer> Correlation = new HashMap<LinkedHashMap<String, String>, Integer>();
	private HashMap<LinkedHashMap<String, String>, Integer> Generalization = new HashMap<LinkedHashMap<String, String>, Integer>();
	private HashMap<LinkedHashMap<String, String>, Integer> Redundancy = new HashMap<LinkedHashMap<String, String>, Integer>();
	
	Anomaly(LinkedHashMap<String, String> rule, int rule_id){
		this.rule_id = rule_id;
		this.rule = rule;
	}

	public LinkedHashMap<String, String> getRule() {
		return rule;
	}

	public HashMap<LinkedHashMap<String, String>, Integer> getShadowing() {
		return Shadowing;
	}

	public HashMap<LinkedHashMap<String, String>, Integer> getCorrelation() {
		return Correlation;
	}

	public HashMap<LinkedHashMap<String, String>, Integer> getGeneralization() {
		return Generalization;
	}

	public HashMap<LinkedHashMap<String, String>, Integer> getRedundancy() {
		return Redundancy;
	}
	
	public void addCorrelation(LinkedHashMap<String, String> ruleB, int ruleB_pos) {
		Correlation.put(ruleB, ruleB_pos);
	}

	public void addRedundancy(LinkedHashMap<String, String> ruleB, int ruleB_pos) {
		Redundancy.put(ruleB, ruleB_pos);
	}

	public void addGeneralization(LinkedHashMap<String, String> ruleB, int ruleB_pos) {
		Generalization.put(ruleB, ruleB_pos);
	}

	public void addShadowing(LinkedHashMap<String, String> ruleB, int ruleB_pos) {
		Shadowing.put(ruleB, ruleB_pos);
	}

	public int getID() {
		return rule_id;
	}

	@Override
	public int compareTo(Anomaly anomaly) {
		int ID_comparison = Integer.compare(rule_id, anomaly.getID());
		return ID_comparison;
	}
}
