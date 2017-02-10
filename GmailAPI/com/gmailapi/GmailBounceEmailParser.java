package com.gmailapi;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * THIS IS SAMPLE PARSER THIS MAY CHANGE WITH YOUR REQUIREMENT
 * @author Kamlesh
 *
 */
public class GmailEmailParser {
	
	public static List parse(String jsonString)
	{
		
		String id="";
		List list =new ArrayList();
		JsonElement jelementObj = new JsonParser().parse(jsonString).getAsJsonObject();
		System.out.println(jelementObj.toString());
		GmailBounceEmailParserJson gbj=null;
		//List list=new ArrayList();
			if(jelementObj!=null){
				
					JsonArray msg = jelementObj.getAsJsonObject().get("messages").getAsJsonArray();
					System.out.println("msg.size: "+msg.size());
					if(msg!=null){
						for(int i=0; i<msg.size(); i++){
						JsonObject first = msg.getAsJsonArray().get(i).getAsJsonObject();
						System.out.println("JsonBobject2: "+first);
						
					String value=first.getAsJsonObject().get("id").getAsString();	
					System.out.println("id: "+value);
					gbj= new GmailBounceEmailParserJson();
					gbj.setId(id);
					list.add(value);
					
						}
						}
			}
		return list;
	}	
	
	public static void main(String[] args) {
		String jsonString= PASTE YOUR RESPONSE STRING HERE;
		parse(jsonString);
	}

}
