package wikiproc2;

public class OperationHandler {
	static final int year = 677, month = 2, day = 1;
	
	static String operation(String[] args, Formatter.Template t) {
		String operation = args[0];
		
		try {
			if(operation.equals("#ifexists")) {
//				System.out.println(Formatter.preprocessFile(args[1], t));
				if(args.length > 3)	return operation(new String[] {"#ifnot",args[1].trim(),"",args[2],args[3]}, t);
				else 				return operation(new String[] {"#ifnot",args[1].trim(),"",args[2],""}, t);
			} else if(operation.equals("#if")) {
				if(Formatter.preprocessFile(args[1], t).equals(Formatter.preprocessFile(args[2], t))) {
					return args[3];
				} else {
					if(args.length > 4) return args[4];
					else return "";
				}
			} else if(operation.equals("#ifnot")) {
				if(args.length > 4)	return operation(new String[] {"#if",args[1],args[2],args[4],args[3]}, t);
				else				return operation(new String[] {"#if",args[1],args[2],"",args[3]},t);
			} else if(operation.equals("#age")) {
				if(args.length != 4 && args.length != 7) return "<span class\\uFF1Derror>Operation '"+operation+"' needs either 3 or 6 arguments!</span>";
				
				int age;
				int y2, m2, d2, y1, m1, d1;
				try {
					if(args.length == 4) {
						y2 = year; m2 = month; d2 = day;
					} else {
						y2 = Integer.parseInt(args[4]);
						m2 = Integer.parseInt(args[5]);
						d2 = Integer.parseInt(args[6]);
					}
					y1 = Integer.parseInt(args[1]);
					m1 = Integer.parseInt(args[2]);
					d1 = Integer.parseInt(args[3]);
				} catch(NumberFormatException e) {
					return "<span class\\uFF1Derror>Operation '"+operation+"' number format error!</span>";
				}
				
				age = y2-y1;
				
				if(m2 < m1 || (m2 == m1 && d2 < d1)) age--;
				
				if(args.length == 7) return "(aged "+age+")";
				else return "(age "+age+")";
			} else if(operation.equals("#time")) {
				if(args.length != 4 && args.length != 7) return "<span class\\uFF1Derror>Operation '"+operation+"' needs either 3 or 6 arguments!</span>";
				
				int y, m, d;
				int y2, m2, d2, y1, m1, d1;
				try {
					if(args.length == 4) {
						y2 = year; m2 = month; d2 = day;
					} else {
						y2 = Integer.parseInt(args[4]);
						m2 = Integer.parseInt(args[5]);
						d2 = Integer.parseInt(args[6]);
					}
					y1 = Integer.parseInt(args[1]);
					m1 = Integer.parseInt(args[2]);
					d1 = Integer.parseInt(args[3]);
				} catch(NumberFormatException e) {
					return "<span class\\uFF1Derror>Operation '"+operation+"' number format error!</span>";
				}
				
				y = y2-y1;
				if(m2 < m1 || (m2 == m1 && d2 < d1)) y--;
				
				m = (m2-m1+12)%12;
				if(d2 < d1) m--;
				String r = "(";
				if(y != 0) {
					r+=y+" year";
					if(y != 1) r+="s";
				}
				if(m != 0) {
					r+=" and "+m+" month";
					if(m != 1) r+="s";
				}
				r+=")";
				return r;
			} else {
				return "<span class\uFF1Derror>Operation '"+operation+"' does not exist!</span>";
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return "<span class\uFF1Derror>Incorrect amount of arguments for operation '"+operation+"'!</span>";
		}
	}
}
