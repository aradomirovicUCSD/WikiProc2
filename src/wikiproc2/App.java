package wikiproc2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class App {
	private static File dir, raws, pages;
	public static void main(String[] args) {
		dir = new File(args[0]);
		raws = new File(dir.getPath()+"\\raws");
		pages = new File(raws.getPath()+"\\page");
		
//		System.out.println(pages.exists());
//		System.out.println(pages.getAbsolutePath());
		
		long t = System.currentTimeMillis();
		for(File f : pages.listFiles()) {
			parse(f);
		}
		t = (System.currentTimeMillis() - t);
		System.out.println("Parsed wiki in "+t+"ms");
	}
	
	static File getFile(String address) {
		String folder, file;
		address = address.toLowerCase();
		
		int i;
		if((i = address.indexOf(':')) != -1) {
			folder = address.substring(0,i);
			file = address.substring(i+1);
		} else {
			folder = "page";
			file = address;
		}
		
		File f = new File(raws.getPath()+"\\"+folder+"\\"+file+".txt");
		
		return new File(raws.getPath()+"\\"+folder+"\\"+file+".txt");
	}
	
	static void parse(File f) {
		String raw;
		try {
			raw = Formatter.readFile(f);
		} catch (FileNotFoundException e) {
			return;
		}
		
		// format
		
		String contents = "<html lang=en><head>", name = f.getName();
		name = name.substring(0,name.indexOf('.'));
		
		contents += "\n<title>"+name+"</title>"
				+ "\n<link rel=\"stylesheet\" href=\"..\\wiki.css\">"
				+ "<meta charset=\"UTF-8\">"
				+ "\n</head><body><div class=wikitext>"
				+ "<h1>"+name+"</h1>";
		
		contents += Formatter.formatFile(Formatter.preprocessFile(raw));
		
		contents += "\n</div></body></html>";
		
		// write
		
		name = name.toLowerCase()+".html";;
		
		File write = new File(dir.getPath()+"\\wiki\\"+name);
		
		try {
			if(!write.exists()) write.createNewFile();
			FileWriter writer = new FileWriter(write);
			writer.write(contents);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
