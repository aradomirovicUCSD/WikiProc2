package wikiproc2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Stack;

public class Formatter {
	static String readFile(File f) throws FileNotFoundException {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String fileString = "", line;
		try {
			fileString = reader.readLine();
			while((line = reader.readLine()) != null) {
				fileString+="\n"+line;
			}
		} catch (IOException e) { e.printStackTrace(); }
		try { reader.close(); } catch (IOException e) { e.printStackTrace(); }
		return fileString;
	}
	
	static String preprocessFile(String s) {
		return preprocessFile(s, null);
	}
	
	static String preprocessFile(String s, Template t) {
		StringCharacterIterator dfa = new StringCharacterIterator(s);
		String processed = "";
		
		int inLink = -1, braceCount = 0, closeBraceCount = 0;
		boolean inTag = false;
		int bracketCount = 0, closeBracketCount = 0;
		int lastOpen = -1;
		Stack<Integer> brackets = new Stack<>();
		char c = 0, b; int i, l; boolean print;
		while(true) {
			b = c;
			c = dfa.current();
			i = dfa.getIndex();
			l = processed.length();
			print = true;
			
			if(c == '<') inTag = true;
			if(c == '>') inTag = false;
			if(c == '=' && inTag) c = '\uFF1D';
			
			if(c == '[' && inLink == -1) {
				braceCount++;
//				print = false;
				
				if(braceCount == 2) inLink = processed.length();
			} else {
				braceCount = 0;
			}
			
			if(c == ']' && inLink != -1) {
				closeBraceCount++;
//				print = false;
				
				if(closeBraceCount == 2) {
//					processed = processed.substring(0, inLink) + appendLink(processed.substring(inLink));
					inLink = -1;
				}
			} else {
				closeBraceCount = 0;
			}
			
			if(c == '\n') {
				if(!brackets.isEmpty()) {
					if(dfa.next() != '*') print = false;
					dfa.previous();
				}
			}
			
			if(c == '{') {
				print = false;
				
				bracketCount++;
				
				if(bracketCount == 2) {
//					if(inParagraph) {
//						processed += "</p>";
//						inParagraph = false;
//						l = processed.length();
//					}
					brackets.push(l); // for the one that was missed
//					System.out.print(brackets.peek()+"; ");
				}
				if(bracketCount >= 2) {
					brackets.push(l);
//					System.out.print(brackets.peek()+" ");
				}
				
//				System.out.println(brackets.size());
			} else if(bracketCount > 0) {
				if(bracketCount == 1) processed+='{';
				bracketCount = 0;
			}
			
			if(c == '}') {
				print = false;
//				System.out.println("}, "+i);
				
				if(closeBracketCount == 1) lastOpen = brackets.pop();
				if(closeBracketCount >= 1) {
					int newOpen = brackets.peek();
					if(newOpen == lastOpen) {
						brackets.pop();
						closeBracketCount++;
//						System.out.println(lastOpen);
					} else {
//						System.out.println(closeBracketCount);
						if(closeBracketCount > 1) {
							processed = appendEmbed(processed, closeBracketCount, lastOpen, t);
//							System.out.println(lastOpen);
						} else processed+='}';
						closeBracketCount = 0;
					}
				} 
				closeBracketCount++;
//				System.out.println(closeBracketCount);
			} else if(closeBracketCount > 0) {
				if(closeBracketCount > 1) {
					processed = appendEmbed(processed, closeBracketCount, lastOpen, t);
//					System.out.println(lastOpen);
				} else {
					processed+='}';
				}
				closeBracketCount = 0;
			}
			
			if(c == '|' && inLink != -1 && (brackets.isEmpty() || inLink > brackets.peek())) {
//				System.out.println(brackets.size());
				c = '\u2016';
			}
			
			if(c == '\u0009') print = false;
			
			if(c == '\uFFFF') {
				break;
			}
			
			if(print) {
				
				processed+=c;
			}
			
			dfa.next();
		}
		
		return processed;
	}
	
	private static boolean lastCharPrintable(StringCharacterIterator i) {
		int idx = i.getIndex();
		char c;
		while(i.getIndex() > 0) {
			if((c = i.previous()) == '\n') continue;
			
			if(c != '{' && c != '}') {
				i.setIndex(idx);
				return true;
			} else {
				i.setIndex(idx);
				return false;
			}
		}
		i.setIndex(idx);
		return false;
	}
	
	static String appendEmbed(String processed, int count, int lastOpen, Template t) {
		count-=1;
		String template;
		
		if(count == 2)
			template = runTemplate(processed.substring(lastOpen), t);
		else if(t != null)
			template = t.runInput(processed.substring(lastOpen));
		else
			template = "<span class\uFF1Derror>Non-template tried to use input value here!</span>";
		
		return processed.substring(0, lastOpen) + template;
	}
	
	static String formatFile(String s) {
		StringCharacterIterator dfa = new StringCharacterIterator(s);
		String processed = "";
		
//		System.out.println(s);
		
		boolean inList = false;
		int newlines = 0; boolean inParagraph = false;
		int equalCount = 0, h2 = -1, h3 = -1, h4 = -1; 
		int inLink = -1, braceCount = 0, closeBraceCount = 0;
		boolean bold = false, italic = false; int apostCount = 0;
		char c; int i, l; boolean print;
		while(true) {
			c = dfa.current();
			i = dfa.getIndex();
			l = processed.length();
			print = true;
			
			if(c == '\uFF1D') c = '=';
			if(c == '\u2016') c = '|';
			
			if(c == '[' && inLink == -1) {
				braceCount++;
				print = false;
				
				if(braceCount == 2) inLink = l;
			} else {
				if(braceCount == 1) processed+="[";
				braceCount = 0;
			}
			
			if(c == ']' && inLink != -1) {
				closeBraceCount++;
				print = false;
				
				if(closeBraceCount == 2) {
					processed = processed.substring(0, inLink) + appendLink(processed.substring(inLink));
					inLink = -1;
				}
			} else {
				if(closeBraceCount == 1) processed+="]";
				closeBraceCount = 0;
			}
			
			if(c == '=') {
				print = false;
				equalCount++;
			} else if(equalCount > 0) {
				if(equalCount == 2) {
					if(h2 == -1) h2 = l;
					else {
						String part = processed.substring(h2);
						processed = processed.substring(0, h2) + "<h2 id=\""+part+"\">"+part+"</h2>";
						h2 = -1;
					}
				} else if(equalCount == 3) {
					if(h3 == -1) h3 = l;
					else {
						String part = processed.substring(h3);
						processed = processed.substring(0, h3) + "<h3 id=\""+part+"\">"+part+"</h3>";
						h3 = -1;
					}
				} else if(equalCount == 4) {
					if(h4 == -1) h4 = l;
					else {
						String part = processed.substring(h4);
						processed = processed.substring(0, h4) + "<h4 id=\""+part+"\">"+part+"</h4>";
						h4 = -1;
					}
				} else {
					for(; equalCount > 0; equalCount--) {
						processed+='=';
					}
				}
				
				equalCount = 0;
			}
			
			if(c == '*' && newlines > 0) {
				inList = true;
				print = false;
				processed+="<li>";
			}
			
			if(c == '\n') {
				print = false;
				newlines++;
				if(inList) {
					inList = false;
					processed+="</li>";
				}
			} else if(newlines > 0) {
				if(newlines > 1 && inParagraph) {
					if(inParagraph) processed+="</p>";
					
					inParagraph = false;
				}
//				else {
//					for(;newlines > 1; newlines-=2) {
//						processed+="<br/>";
//					}
//					if(newlines == 1) processed+=" ";
//				}
				newlines = 0;
			}
			
			if(c == '\'') {
				print = false;
				apostCount++;
			} else if(apostCount > 0) {
				if(apostCount == 3 || apostCount == 5) {
					if(bold) processed+="</b>";
					else processed+="<b>";
					
					bold = !bold;
				}
				if(apostCount == 2 || apostCount == 5) {
					if(italic) processed+="</i>";
					else processed+="<i>";
					
					italic = !italic;
				}
				if(apostCount != 2 && apostCount != 3 && apostCount != 5) {
					for(int j = 0; j < apostCount; j++) {
						processed+='\'';
					}
				}
				apostCount = 0;
			}
			
			if(c == '\uFFFF') {
				if(inParagraph) processed+="</p>";
				break;
			}
			
			if(print) {
				if(!inParagraph && (h2 == -1 && h3 == -1 && h4 == -1)) {
					processed+="<p>";
					inParagraph = true;
				}
				
				processed+=c;
			}
			
			dfa.next();
		}
		
		return processed;
	}
	
	static String appendLink(String s) {
		String[] args = s.split("\\|");
//		System.out.println(args.length);
		
		String r;
		
		String link = args[0];
		boolean exists = link.contains("http") || App.getFile(link).isFile();
		
		if(!link.contains("http") && link.indexOf('#') != 0) link = (link+".html").toLowerCase();
		
		r = "<a href=\""+link+"\" ";
		
		if(!exists) r+="noexist";
		
		if(args.length == 1) r += ">"+args[0]+"</a>";
//		System.out.println(args[0]+" - "+args[1]);
		else r += ">"+args[1]+"</a>";
		
		return r;
	}
	
	static HashMap<String,Template> templates = new HashMap<>();
	
	static String runTemplate(String s, Template nest) {
		String[] args = s.split("\\|");
		String name = args[0].trim();
		
//		if(name.equals("infobox\\data")) {
//			for(int i = 1; i < args.length; i++) System.out.println(args[i]);
//			System.out.println("--");
//		}
		
		if(name.charAt(0) == '#') return OperationHandler.operation(args, nest);
		
		Template template = templates.get(name);
		if(template == null) {
			try {
				template = new Template(name,App.getFile("Template:"+name));
			} catch (FileNotFoundException e) {
				return "<span class\uFF1Derror>Template '"+name+"' not found!</span>";
			}
		}
		template.setData(args);
		
//		System.out.println(name+" :: "+preprocessFile(template.unformatted,template));
//		System.out.println(name);
		return preprocessFile(template.unformatted,template);
	}
	
	static class Template {
		String unformatted;
		
		HashMap<String,String> data;
		
		public Template(String name, File f) throws FileNotFoundException {
			unformatted = readFile(f);
			data = new HashMap<>();
			
			templates.put(name, this);
		}
		
		void setData(String[] args) {
			data.clear();
			for(int i = 1; i < args.length; i++) {
				String[] d = args[i].split("=");
				
				if(d.length > 1) data.put(d[0], d[1]);
				else data.put(""+(i-1),d[0]);
			}
		}
		
		String runInput(String var) {
			String[] d = var.split("\\|");
			String id = d[0];
			
			if(id.lastIndexOf('|') == id.length()-1) id = id.substring(0, id.length()-1);
			id = id.trim();
			
			String r = data.get(id);
			if(r == null) {
//				System.out.println(id);
				if(d.length > 1) r = d[1];
				else {
					if(d.length > 1) r = "<span class\uFF1Derror>Input '"+d[0]+"' not found!</span>";
					else return "";
				}
			}
			r = r.trim();
			
//			if(id.equals("1")) System.out.println(r);
			return r;
		}
	}
}
