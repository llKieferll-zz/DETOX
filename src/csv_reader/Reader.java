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
	private String filename = null;

	public Reader(String filename){
		try {
			this.reader = new CSVReader(new FileReader("./CSV/" + filename));
			this.filename = filename;
		} catch (FileNotFoundException e) {
			System.out.println("Arquivo \"" + filename + "\" nao encontrado!");
		}
	}


	public String getFilename() {
		return filename;
	}


	public void setFilename(String filename) {
		this.filename = filename;
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