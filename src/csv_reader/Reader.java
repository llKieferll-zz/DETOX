package csv_reader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.opencsv.CSVReader;

import config_file.Field_Config;
import config_file.Field_Config.Variable;



public class Reader {

	private CSVReader reader = null;

	public Reader(){
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
	}
	
	public CSVReader getReader() {
		return reader;
	}

	public void setReader(String filename) {		
		try {
			if(reader != null) this.reader.close();
			this.reader = new CSVReader(new FileReader("./CSV/" + filename));
		} catch (FileNotFoundException e) {
			System.out.println("Arquivo \"" + filename + "\" nao encontrado!");
		} catch (IOException e) {
			System.err.println("Erro ao tentar fechar o 'reader'");
			e.printStackTrace();
		}
	}
	
	
	public ArrayList<LinkedHashMap<String, String>> readAll() {

		String[] nextLine;

		ArrayList<LinkedHashMap<String, String>> rules = new ArrayList<LinkedHashMap<String, String>>();

		ArrayList<Integer> active_variables_positions = new ArrayList<Integer>();
		ArrayList<Field_Config.Variable> active_variables = new ArrayList<Field_Config.Variable>();

		int position = 0;



		for(Variable field : Field_Config.Variable.values()){
			if(field.isActive()) {
				active_variables.add(field);
				active_variables_positions.add(position);
			}
			position++;
		}

		try {
			nextLine = reader.readNext();
			int active_variables_size = active_variables.size();
			boolean enabled = false;
			boolean enable_is_active = false;
			
			//check the the existence of "ENABLED" field in active_variables
			for (Variable variable : active_variables){
				if(variable.getName().equalsIgnoreCase("ENABLED")) {
					enable_is_active = true;
					break;
				}
			}
			
			//if it exists, we must check it so we only read the active rules
			if(enable_is_active){
				while ((nextLine = reader.readNext()) != null) {
					enabled = false;
					LinkedHashMap<String, String> rule = new LinkedHashMap<String, String>();
					for(int index = 0; index < active_variables_size; index++){
						if(active_variables.get(index).getName().equalsIgnoreCase("ENABLED")){
							if (nextLine[active_variables_positions.get(index)].equalsIgnoreCase("true")){
								enabled = true;
								continue;
							}else{
								break;
							}
						}
						rule.put(active_variables.get(index).getName(), nextLine[active_variables_positions.get(index)]);
					}
					if(enabled) rules.add(rule);
				}
				
				//if not, we simply read all rules.
			}else{
				while ((nextLine = reader.readNext()) != null) {
					LinkedHashMap<String, String> rule = new LinkedHashMap<String, String>();
					for(int index = 0; index < active_variables_size; index++){
						rule.put(active_variables.get(index).getName(), nextLine[active_variables_positions.get(index)]);
					}
					rules.add(rule);
				}
			}


		} catch (IOException e) {
			System.err.println("IO Exception reading the file");
			e.printStackTrace();
			System.exit(0);
		}
		return rules;
	}
}