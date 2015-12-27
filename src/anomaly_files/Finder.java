package anomaly_files;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import config_file.Field_Config;
import config_file.Field_Config.Variable;

public class Finder {

	//These should be changed to reflect the "FIELDS" variable inside "Variables.java" instead of being declared this way.
	//Maybe use the header of the rules to get the names? What if no header is specified?

	//rules relation//
	private static int EXACT = 1;
	private static int CORRELATED = 2;
	private static int SUBSET = 3;
	private static int SUPERSET = 4;
	private static int DISJOINT = 5;
	private static int NONE = 6;


	public static HashMap<LinkedHashMap<String, String>, Anomaly> find(ArrayList<LinkedHashMap<String, String>> rules){
		int i, j, rules_length;

		LinkedHashMap<String, String> ruleX;
		LinkedHashMap<String, String> ruleY;
		LinkedHashMap<String, String> ruleZ;

		ArrayList<Variable> active_variables = new ArrayList<Variable>();
		for (Variable variable : Field_Config.Variable.values()){ //For every variable in the config file
			if (variable.isActive()){ // We get only those that the user decided to use
				if (!variable.getName().equalsIgnoreCase("ACTION") && !variable.getName().equalsIgnoreCase("ENABLED")){ // Except ACTION, since it needs to be checked in a diferent way and ENABLED since this checks was already made when reading the file
					active_variables.add(variable);
				}
			}
		}

		rules_length = rules.size(); // Total number of rules

		HashMap<LinkedHashMap<String, String>, Anomaly> anomaly_list = new HashMap<LinkedHashMap<String, String>, Anomaly>(); // Map of anomalies using the entire rule as Key


		for(i=0; i<rules_length; i++){ // For each rule...

			ruleX = rules.get(i); 

			for(j=i+1; j<rules_length; j++){ // For each other rule whose priority is lower than the ith rule

				ruleY = rules.get(j);

				int rules_relation = NONE; // We start with no initial relation assumed between rules
				int field_relation;

				
				for (Variable field : active_variables){ // For every field in both rules (corresponding fields)
					// except the ACTION field (we want to check that one last, so it was not included in active_variables)
						field_relation = field.Compare(ruleX.get(field.getName()), ruleY.get(field.getName()));
						rules_relation = process_field_relation(rules_relation, field_relation);
				}

				//Now we know that every single field (but ACTION) has been processed.
				//We now check the ACTION of both rules to determined if there is any anomaly based on the relation found between the rules being analyzed

				String ACTION = Field_Config.Variable.ACTION.getName();
				if(rules_relation != DISJOINT){ // If rules are not disjoint, then we may have an anomaly (the BIG CANNON???)

					if(rules_relation == CORRELATED && 
							Field_Config.Variable.ACTION.Compare(ruleX.get(ACTION), ruleY.get(ACTION)) 
							== Field_Config.Relation.NONE.getValue()){ //If the rules relation is correlated and they have DIFFERENT actions (deny/accept or accept/deny)
						//Rx C. Ry, Rx[action] != Ry[action]
						if(!anomaly_list.containsKey(ruleX)){ // If this is the first anomaly found for ruleX
							Anomaly anomaly = new Anomaly(ruleX, i+1); // We create an instance of ruleX's anomalies (with ruleX and it's number as identifiers. I+1 because counter starts at 0)
							
							anomaly.addCorrelation(ruleY, j+1); // We add the ruleY to the correct anomaly type (correlation in this case)
							anomaly_list.put(ruleX, anomaly); // And map ruleX as a rule that has anomalies
						}else{
							anomaly_list.get(ruleX).addCorrelation(ruleY, j+1); //Else, that's not the first time. It means ruleX was already mapped, since it has anomalies. We just add the one we just found to it (correlation in this case)
						}
					} 
					else if(rules_relation == SUPERSET){ //If the rules relation is superset or exact
						if(Field_Config.Variable.ACTION.Compare(ruleX.get(ACTION), ruleY.get(ACTION)) 
								== Field_Config.Relation.EQUALS.getValue()){  // and they have the same actions (deny/deny or accept/accept)
							//Rx[order] < Ry[order], Ry I.M Rx, Rx[action] = Ry[action]
							if(!anomaly_list.containsKey(ruleY)){
								Anomaly anomaly = new Anomaly(ruleY, j+1);
								anomaly.addRedundancy(ruleX, i+1);
								anomaly_list.put(ruleY, anomaly);
							}else{
								anomaly_list.get(ruleY).addRedundancy(ruleX, i+1);
							}
						}else{ // If they have DIFFERENT actions (deny/accept or accept/deny)
							//Rx[order] < Ry[order], Ry I.M. Rx, Rx[action] != Ry[action]
							if(!anomaly_list.containsKey(ruleY)){
								Anomaly anomaly = new Anomaly(ruleY, j+1);
								anomaly.addShadowing(ruleX, i+1);
								anomaly_list.put(ruleY, anomaly);
							}else{
								anomaly_list.get(ruleY).addShadowing(ruleX, i+1);
							}
						}
					}
					else if(rules_relation == EXACT){ //If the rules relation is exact or subset
						if(Field_Config.Variable.ACTION.Compare(ruleX.get(ACTION), ruleY.get(ACTION)) 
								== Field_Config.Relation.EQUALS.getValue()){ // and they have the same actions (deny/deny or accept/accept)
							//Rx[order] < Ry[order], Rx E.M. Ry, Rx[action] = Ry[action]
							if(!anomaly_list.containsKey(ruleY)){
								Anomaly anomaly = new Anomaly(ruleY, j+1);
								anomaly.addRedundancy(ruleX, i+1);
								anomaly_list.put(ruleY, anomaly);
							}else{
								anomaly_list.get(ruleY).addRedundancy(ruleX, i+1);
							}
						}else{ // If they have DIFFERENT actions (deny/accept or accept/deny)
							//Rx[order] < Ry[order], Rx E.M. Ry, Rx[action] != Ry[action]
							if(!anomaly_list.containsKey(ruleY)){
								Anomaly anomaly = new Anomaly(ruleY, j+1);
								anomaly.addShadowing(ruleX, i+1);
								anomaly_list.put(ruleY, anomaly);
							}else{
								anomaly_list.get(ruleY).addShadowing(ruleX, i+1);
							}
						}
					}
					else if(rules_relation == SUBSET){ //If the rules relation is exact or subset
						//Rx[order] < Ry[order], Rx I.M. Ry, Rx[action] = Ry[action]
						if(Field_Config.Variable.ACTION.Compare(ruleX.get(ACTION), ruleY.get(ACTION)) 
								== Field_Config.Relation.EQUALS.getValue()){ // and they have the same actions (deny/deny or accept/accept)
							int XZ_field_relation = NONE;
							int XZ_rules_relation = NONE;
							boolean Z_exists = false;

							// !E Rz where Rx[order] < Rz[order] < Ry[order], Rx{I.M., C.} Rz, Rx[action] 6= Rz[action]
							for(int z=i+1; z<j; z++){
								ruleZ = rules.get(z);
								for (Variable field : active_variables){ // For every field in both rules (corresponding fields)
									// except the ACTION field (we want to check that one last, so it is not included in active_variables)
									if (!field.getName().equalsIgnoreCase("ENABLED")){
										XZ_field_relation = field.Compare(ruleX.get(field.getName()), ruleZ.get(field.getName()));
										XZ_rules_relation = process_field_relation(XZ_rules_relation, XZ_field_relation);
									}
								}
								if (XZ_rules_relation == SUBSET || XZ_rules_relation == CORRELATED){
									if(Field_Config.Variable.ACTION.Compare(ruleX.get(ACTION), ruleZ.get(ACTION)) 
											== Field_Config.Relation.NONE.getValue()){
										Z_exists = true;
										break;
									}
								}
							}

							if(!Z_exists){
								if(!anomaly_list.containsKey(ruleX)){
									Anomaly anomaly = new Anomaly(ruleX, i+1);
									anomaly.addRedundancy(ruleY, j+1);
									anomaly_list.put(ruleX, anomaly);
								}else{
									anomaly_list.get(ruleX).addRedundancy(ruleY, j+1);
								}
							}




						}else{ // If they have DIFFERENT actions (deny/accept or accept/deny)
							//Rx[order] < Ry[order], Rx I.M. Ry, Rx[action] != Ry[action]
							if(!anomaly_list.containsKey(ruleY)){
								Anomaly anomaly = new Anomaly(ruleY, j+1);
								anomaly.addGeneralization(ruleX, i+1);
								anomaly_list.put(ruleY, anomaly);
							}else{
								anomaly_list.get(ruleY).addGeneralization(ruleX, i+1);
							}
						}
					}
				}
			}
		}
		return anomaly_list;
	}

	private static int process_field_relation(int rules_relation, int field_relation) {
		if (field_relation == Field_Config.Relation.EQUALS.getValue() ){ //  If ruleX's field is equal ruleY's field and
			if (rules_relation == NONE){ // there was no previous relation
				return EXACT; // then the current relation is EXACT
			}else{
				return rules_relation;
			}
		} 

		else if(field_relation == Field_Config.Relation.CONTAINS.getValue()) { // else, if ruleX's field contains (is a superset) of B's field and 
			if (rules_relation == SUBSET || rules_relation == CORRELATED){ // if previous fields relation resulted in resulted in ruleX having a relation of SUBSET or CORRELATION
				return CORRELATED; // then the current relation so far is CORRELATION
			}else if(rules_relation != DISJOINT){// else, if previous fields relation did not already resulted in DISJOINT, 
				return SUPERSET; // then the current relation so far is SUPERSET
			}
		} 

		else if(field_relation == Field_Config.Relation.CONTAINED.getValue()) { // else, if ruleX's field is contained in (is a subset) ruleY's field and 
			if (rules_relation == SUPERSET || rules_relation == CORRELATED){ // if previous fields relation resulted in ruleX having a relation of SUPERSET or CORRELATION with ruleY
				return CORRELATED; // then the current relation so far is CORRELATION
			}else if(rules_relation != DISJOINT){// else, if previous fields relation did not already resulted in DISJOINT, 
				return SUBSET; // then the current relation so far is SUBSET
			}
		}
		return DISJOINT; // else, since none of the above matched, ruleX's and ruleY's field are DISJOINT
	}
}
