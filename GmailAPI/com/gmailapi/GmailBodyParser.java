package com.gmailapi;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 *	THIS IS SAMPLE PARSER CLASS THIS MAY CHANGE WITH YOUR REQUIRMENT
 * @author Kamlesh
 *
 */
public class GmailBodyParser {
	
	public static List getList(List emailBodyList){
		
		List list =new ArrayList();
		for (int i = 0; i < emailBodyList.size(); i++) {
			//System.out.println("GmailBodyParser.getList.emailBodyList: "+emailBodyList.get(i));
			String jsonString = emailBodyList.get(i).toString();
			//System.out.println("GmailBodyParser.getList.parsedList:  "+parsedList.get(i));
			JsonElement jelementObj = new JsonParser().parse(jsonString).getAsJsonObject();
			//System.out.println(jelementObj.toString());			
			if(jelementObj!=null){
						GmailBodyParserJson gbj=new GmailBodyParserJson();					
						
						String value=jelementObj.getAsJsonObject().get("id").getAsString();	
						//System.out.println("id: "+value);
						
						String snippet = jelementObj.getAsJsonObject().get("snippet").getAsString();
						//System.out.println("snippet: "+snippet);
						gbj.setId(value);
						gbj.setSnippet(snippet);
						list.add(gbj);					
					}
		}
		return list;
	}
	
	public static List parse(String jsonString)
	{
		List list =new ArrayList();
		JsonElement jelementObj = new JsonParser().parse(jsonString).getAsJsonObject();
		//System.out.println(jelementObj.toString());			
		if(jelementObj!=null){
					GmailBodyParserJson gbj=new GmailBodyParserJson();					
					
					String value=jelementObj.getAsJsonObject().get("id").getAsString();	
					System.out.println("id: "+value);
					
					String snippet = jelementObj.getAsJsonObject().get("snippet").getAsString();
					System.out.println("snippet: "+snippet);
					gbj.setId(value);
					gbj.setSnippet(snippet);
					list.add(gbj);					
				}
		return list;
		
	}	
	
	public static void main(String[] args) {
		String jsonString= PASTE YOUR RESPONSE JSON STRING HERE;
		parse(jsonString);
	}

}

