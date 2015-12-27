package config_file;

public class Field_Config {		



	public enum Relation{
		EQUALS("EQUALS", 1),
		CONTAINS("CONTAINS", 2),
		CONTAINED("CONTAINED", 3),
		NONE("NONE", 4);

		private final String name;
		private final int value;

		Relation(String name, int value){
			this.name = name;
			this.value = value;
		}

		public String getName(){
			return this.name;
		}

		public int getValue(){
			return this.value;
		}
	}



	private enum Name{
		INTERFACE("INTERFACE"),
		IFACE_NUMBER("#"),
		ENABLED("ENABLED"),
		SRC_IP("SRC_IP"),
		DST_IP("DST_IP"),
		SERVICE("SERVICE"),
		ACTION("ACTION"),
		HITS("HITS"),
		LOGGING("LOGGING"),
		TIME("TIME"),
		DESCRIPTION("DESCRIPTION");

		private final String name;

		Name(String name){
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private enum Active{
		INTERFACE(true),
		IFACE_NUMBER(false),
		ENABLED(true),
		SRC_IP(true),
		DST_IP(true),
		SERVICE(true),
		ACTION(true),
		HITS(false),
		LOGGING(false),
		TIME(false),
		DESCRIPTION(false);

		private final boolean active;

		Active(boolean active){
			this.active = active;
		}

		public boolean isActive() {
			return active;
		}
	}

	private enum Method{

		BASIC() {
			public int Compare(String A, String B){   
				if (A.equalsIgnoreCase(B)) return Relation.EQUALS.getValue();
				return Relation.NONE.getValue();
			}   
		},

		IP() {
			public int Compare(String A, String B){   
				if (A.equals(B)) return Relation.EQUALS.getValue(); //Same IPs, return equals

				if (A.equalsIgnoreCase("any")) A = "0-255.0-255.0-255.0-255"; // If "any", we replace all the octets with the 0-255 range
				else A = A.replace("*", "0-255"); // else, we replace any "*" found with the 0-255 range

				if (B.equalsIgnoreCase("any")) B = "0-255.0-255.0-255.0-255"; // Same thing
				else B = B.replace("*", "0-255");

				String[] A_octets = A.split("\\."); //get the octets of A
				String[] B_octets = B.split("\\."); //get the octets of B


				for (int i = 0; i < A_octets.length; i++) //for every octet, from left to right (network to host)
				{

					if(A_octets[i].equals(B_octets[i])){//if A's octet is the same as B's octet, we continue to the next one, if any;
						continue;
					}

					if(!A_octets[i].contains("-") && !B_octets[i].contains("-")){ //if they're not equal and also not a range, they're not related
						return Relation.NONE.getValue();
					}


					if (A_octets[i].equals("0-255")){ // if A's octet can be everything
						if(!(B_octets[i].equals("0-255"))){ // and if B's octet is anything else but not everything
							return Relation.CONTAINS.getValue(); // then it's safe to assume that, from this point, A contains B, so, we return Relation.CONTAINS
						}
					}

					if (B_octets[i].equals("0-255")){ // if B's octet can be everything
						if(!(A_octets[i].equals("0-255"))){ // and if A's octet is anything else but not everything
							return Relation.CONTAINED.getValue(); //then it's safe to assume that, from this point, A is contained in B, so, we return Relation.CONTAINED
						}
					}


					if (A_octets[i].contains("-") && !B_octets[i].contains("-")) // if A's octet represents a range of IPs but not '0-255' and B is a specific IP
					{
						short A_octet_lower_limit = Short.parseShort(A_octets[i].split("-")[0]); //we get the lower limit octet as a byte
						short A_octet_upper_limit = Short.parseShort(A_octets[i].split("-")[1]); //we get the upper limit octet as byte
						short B_octet = Short.parseShort(B_octets[i]); // we get B octet as a byte
						if (B_octet >= A_octet_lower_limit && B_octet <= A_octet_upper_limit){ //and check if B is between both limits
							return Relation.CONTAINS.getValue();// if so, it's safe to assume that, from this point, A contains B, so, we return Relation.CONTAINS
						}else{
							return Relation.NONE.getValue();
						}
					}

					if (B_octets[i].contains("-") && !A_octets[i].contains("-")) // if B's octet represents a range of IPs but not '0-255' and A is a specific IP
					{
						short B_octet_lower_limit = Short.parseShort(B_octets[i].split("-")[0]); //we get the lower limit octet as a byte
						short B_octet_upper_limit = Short.parseShort(B_octets[i].split("-")[1]); //we get the upper limit octet as byte
						short A_octet = Short.parseShort(A_octets[i]); // we get A octet as a byte
						if (A_octet >= B_octet_lower_limit && A_octet <= B_octet_upper_limit){ //and check if A is between both limits
							return Relation.CONTAINED.getValue();// if so, it's safe to assume that, from this point, A is contained in B, so, we return Relation.CONTAINED
						}else{
							return Relation.NONE.getValue();
						}
					}

					if (A_octets[i].contains("-") && B_octets[i].contains("-")) // If both octets represent a range of IPs but not '0-255'
					{
						String[] Splited_A_octet = A_octets[i].split("-"); // We split A limits
						String[] Splited_B_octet = B_octets[i].split("-"); // We split B limits


						short A_octet_lower_limit =  Short.parseShort(Splited_A_octet[0]); // We get A lower limit value
						short A_octet_higher_limit =  Short.parseShort(Splited_A_octet[1]); // We get A higher limit value
						short B_octet_lower_limit =  Short.parseShort(Splited_B_octet[0]); // We get B lower limit value
						short B_octet_higher_limit =  Short.parseShort(Splited_B_octet[1]); // We get B higher limit value

						if (A_octet_lower_limit > B_octet_higher_limit || A_octet_higher_limit < B_octet_lower_limit){
							return Relation.NONE.getValue();
						}

//						if (A_octet_lower_limit >= B_octet_lower_limit){ // If A lower limit is inside B range, at least a part o A is inside B
//							if (A_octet_higher_limit <= B_octet_higher_limit){ // So, if A higher limit is also inside B range, A is completly inside B
//								return Relation.CONTAINED.getValue(); // Therefore, A is contained in B
//							}
//						}
//
//						if (B_octet_lower_limit >= A_octet_lower_limit){ // If B lower limit is inside A range, at least a part o B is inside A
//							if (B_octet_higher_limit <= A_octet_higher_limit){ // So, if B higher limit is also inside A range, B is completly inside A
//								return Relation.CONTAINS.getValue(); // Therefore, A contains B
//							}
//						}
						/**
						 * NEED TO SPECIFY WHAT TO RETURN HERE
						 **/
						return Relation.CONTAINS.getValue(); // Else, the ranges intersect each other but not completly. B is partially shadowed by A so, for now, we return CONTAINS  
					}
				}
				return Relation.EQUALS.getValue(); //the only way to process the 'for' until the end is if both IPs are equal (because of the 'continue' clause. So, if we get here, we return Relation.EQUALS
			}   
		},

		BASIC_RANGED() {
			public int Compare(String A, String B){   
				if(A.equalsIgnoreCase("any")){ // If A can be any value
					if(!B.equalsIgnoreCase("any")){ // But B is a specific one
						return Relation.CONTAINS.getValue(); // Then A contains B
					}
				}

				if(B.equalsIgnoreCase("any")){ // If B can be any value
					if(!A.equalsIgnoreCase("any")){ // But A is a specific one
						return Relation.CONTAINED.getValue(); // Then A is contained in B
					}
				}
				if (A.equalsIgnoreCase(B)) return Relation.EQUALS.getValue(); // If they are both "any", the same specific value, or the same range, then equals.
				if (!A.contains("-") && !B.contains("-")) return Relation.NONE.getValue(); // If none of them are "any", they are not ranged and also not the same value, then none.
				if (A.contains("-")){ // If A is a range of values
					if (!(B.contains("-"))){ // But B is a specific one

						String[] Splited_A = A.split("-"); // We split A's limits
						int B_value = Integer.parseInt(B); // We get B value
						int A_lower = Integer.parseInt(Splited_A[0]); // We get A lower limit value
						int A_higher = Integer.parseInt(Splited_A[1]); // We get A higher limit value

						if (B_value >= A_lower && B_value <= A_higher){ // If B value is outside A's limits
							return Relation.CONTAINS.getValue(); // Then they are not related
						}else{
							return Relation.NONE.getValue(); // Else, B is inside A's range. Therefore, A contains B
						}
					}
				}

				if (B.contains("-")){ // If B is a range of values
					if (!(A.contains("-"))){ // But A is a specific one

						String[] Splited_B = B.split("-"); // We split B limits
						int A_value = Integer.parseInt(A); // We get A value
						int B_lower = Integer.parseInt(Splited_B[0]); // We get B lower limit value
						int B_higher = Integer.parseInt(Splited_B[1]); // We get B higher limit value

						if (A_value >= B_lower || A_value <= B_higher){ // If A value is outside B limits
							return Relation.CONTAINED.getValue(); // Then they are not related
						}else{
							return Relation.NONE.getValue(); // Else, A is inside B range. Therefore, A is contained in B
						}
					}
				}

				if (A.contains("-") && B.contains("-")){ // If both are a range of values

					String[] Splited_A = A.split("-"); // We split A limits
					String[] Splited_B = B.split("-"); // We split B limits


					int A_lower = Integer.parseInt(Splited_A[0]); // We get A lower limit value
					int A_higher = Integer.parseInt(Splited_A[1]); // We get A higher limit value
					int B_lower = Integer.parseInt(Splited_B[0]); // We get B lower limit value
					int B_higher = Integer.parseInt(Splited_B[1]); // We get B higher limit value

					if (A_lower > B_higher || A_higher < B_lower){
						return Relation.NONE.getValue();
					}

//					if (A_lower >= B_lower){ // If A lower limit is inside B range, at least a part o A is inside B
//						if (A_higher <= B_higher){ // So, if A higher limit is also inside B range, A is completly inside B
//							return Relation.CONTAINED.getValue(); // Therefore, A is contained in B
//						}
//					}
//
//					if (B_lower >= A_lower){ // If B lower limit is inside A range, at least a part o B is inside A
//						if (B_higher <= A_higher){ // So, if B higher limit is also inside A range, B is completly inside A
//							return Relation.CONTAINS.getValue(); // Therefore, A contains B
//						}
//					}
				}
				/**
				 * NEED TO SPECIFY WHAT TO RETURN HERE
				 **/
				return Relation.CONTAINS.getValue();  // Else, the ranges intersect each other but not completly. B is partially shadowed by A so, for now, we return CONTAINS  
			}   
		};

		public abstract int Compare(String A, String B);
	}

	public enum Variable{
		INTERFACE(Name.INTERFACE.getName(), Active.INTERFACE.isActive()){
			public int Compare(String A, String B){
				return Method.BASIC.Compare(A, B);
			}
		},
		
		IFACE_NUMBER(Name.IFACE_NUMBER.getName(), Active.IFACE_NUMBER.isActive()){
			public int Compare(String A, String B){
				return Method.BASIC.Compare(A, B);
			}
		},
		
		ENABLED(Name.ENABLED.getName(), Active.ENABLED.isActive()){
			public int Compare(String A, String B){
				return Method.BASIC.Compare(A, B);
			}
		},

		SRC_IP(Name.SRC_IP.getName(), Active.SRC_IP.isActive()){
			public int Compare(String A, String B){
				return Method.IP.Compare(A, B);
			}
		},

		DST_IP(Name.DST_IP.getName(), Active.DST_IP.isActive()){
			public int Compare(String A, String B){
				return Method.IP.Compare(A, B);
			}
		},
		
		SERVICE(Name.SERVICE.getName(), Active.SERVICE.isActive()){
			public int Compare(String A, String B){
				return Method.BASIC.Compare(A, B);
			}
		},


		ACTION(Name.ACTION.getName(), Active.ACTION.isActive()){
			public int Compare(String A, String B){
				return Method.BASIC.Compare(A, B);
			}
		},
		
		HITS(Name.HITS.getName(), Active.HITS.isActive()){
			public int Compare(String A, String B){
				return Method.BASIC.Compare(A, B);
			}
		},
		LOGGING(Name.LOGGING.getName(), Active.LOGGING.isActive()){
			public int Compare(String A, String B){
				return Method.BASIC.Compare(A, B);
			}
		},
		TIME(Name.TIME.getName(), Active.TIME.isActive()){
			public int Compare(String A, String B){
				return Method.BASIC.Compare(A, B);
			}
		},
		DESCRIPTION(Name.DESCRIPTION.getName(), Active.DESCRIPTION.isActive()){
			public int Compare(String A, String B){
				return Method.BASIC.Compare(A, B);
			}
		};

		private final String name;
		private final boolean active;

		Variable(String name, boolean active){
			this.name = name;
			this.active = active;
		}

		public String getName() {
			return name;
		}

		public boolean isActive() {
			return active;
		}

		public abstract int Compare(String A, String B);
	}
}
