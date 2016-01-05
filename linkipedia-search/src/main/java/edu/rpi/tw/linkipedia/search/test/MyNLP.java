/**
 * Linkipedia, Copyright (c) 2015 Tetherless World Constellation 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.rpi.tw.linkipedia.search.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.rpi.tw.linkipedia.search.utils.Mention;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

public class MyNLP {
	static String queryPrefix = "000000";
	   public static void main(String[] args) throws IOException {

		      String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";

		      if (args.length > 0) {
		        serializedClassifier = args[0];
		      }

		      AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);

		      if (args.length > 1) {
		        String fileContents = IOUtils.slurpFile(args[1]);
		        List<List<CoreLabel>> out = classifier.classify(fileContents);
		        for (List<CoreLabel> sentence : out) {
		          for (CoreLabel word : sentence) {
		            System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');
		          }
		          System.out.println();
		        }
		        out = classifier.classifyFile(args[1]);
		        for (List<CoreLabel> sentence : out) {
		          for (CoreLabel word : sentence) {
		            System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');
		          }
		          System.out.println();
		        }

		      } else {
		        String s1 = "AT&T has a cute TV commercial for its U-verse TV app in which the co-pilot of a space capsule rattling through a difficult re-entry tells the pilot, This is very exciting! But I'm at my stop. The flummoxed pilot responds, Come again? and the co-pilot explains, I'm watching this on the train. It's so hard to leave. Good luck with everything! The co-pilot, now dressed in business attire, emerges from a train holding the smartphone he was using to watch the movie while on his way to work. The key is that he was holding a phone. Why not a tablet, which would seem the logical choice for watching movies while on the go? The commercial underscores what the market is telling us - that the tablet - a product introduced by Apple barely four years ago - may be a high-tech hula hoop. Some industry watchers think tablets have a bright future. Research company Gartner said last week that it expects tablet sales to eclipse PC sales in 2015 and predicted that tablet sales will grow by 25 percent from this year to next. Tablets could indeed overtake PCs, which have suffered from lackluster sales for years. But the tablet market itself appears to be flattening out, as evidenced by recent reports from big tablet makers. Apple, the market leader, reported a decline in tablet shipments in its latest fiscal quarter, while No. 2 Samsung called its tablet sales sluggish.";
		        String s2 = "Mississippi, has Hillary's Time Come? I read a fascinating piece in the WSJ today. The author spoke about how Obama's approval ratings are at a dismal low. He apparently appears to be unable to pull Congeress together to accomplish much, and in the author's opinion, it would only get worse if he got elected to a second term. He would like Obama to bow out for a 2nd term, and have Hillary Clinton run for president. He believes that Hillary has the best chance of bringing the country together. I don't know how she feels about running, but, IMO, I think that it is a great idea. She certainly has the smarts, the experience, and the approval of the majority of citizens. I think that the people on the Republican side of the fence, are a poor bunch. There is not a one in the bunch who would capture the imagination of the American people. The voters who will decide this election, IMO are the moderates and the independents. THEY certainly will not vote for any of the anti-gay, pro-life bible bangers. What do you think? Should Obama step down for the good of the country? Should Hillary run? @Phoenix32890, Hillary's time has come, and gone... She would make her self no friend of the Democratic party by dividing them and losing the black vote... No one is going to vote for her that did not vote for Mr. Obama, and a whole lot less Obama voters will vote for her... Don't worry though.. She has enough sense to not try to run against a sitting president from her own party... @Phoenix32890, The voters who will decide this election, IMO are the moderates and the independents. THEY certainly will not vote for any of the anti-gay, pro-life bible bangers. Not sure how you determined this factoid. Clearly most who vote for Democratic candidates would follow this voting pattern but not any Tea Baggers or typical Republicans (even those who claim the moderate social mantel). In the next election with record lows in voting, the most... passionate voters will vote their allegedly principled candidates in. What does that mean? Independents and so-called moderates WILL LIKELY not be turning up to the polling booths in any large numbers to authenticate your thesis. @tsarstepan, Best case scenario: Obama gets reelected and the country continues to slide backwards even if the Democrats gain a greater foothold of the US Senate and increase their collective mass in the US Congress. Worst case scenario: George W. Bush II (any Republican candidate other then Huntsman, Romney, or Ron Paul) will take the election. Then that means the Republicans would likely further gain in Congress and even the Senate perhaps. Then the country slides right back into the deepest of recessions. @tsarstepan, I'm not saying I don't like the idea. The theory behind a Hilary Clinton candidacy would a great improvement over a stalled and uninspiring Obama presidency. I'm not too enthusiastic over it actually be plausible even in the best of scenarios. @tsarstepan, I suspect you are wrong on the voting inclinations of the moderates, and especially the ones you call tea baggers. Actually, I think you are wrong on the moderate/independent voter turnout, too, but I could be wrong. I'm expecting a fairly good anti Obama turnout amongst that group. I could accept Hillary. Like Phoenix, I'm not greatly impressed by the Republican field. Not crazy about the presumed Democratic candidate either. On the other hand, Obama stepping aside seems even less likely than Cain giving up. I guess that in summary, I'm not optimistic about the future of the country for the next five years.";
		        //System.out.println(classifier.classifyToString(s1));
		        String content = classifier.classifyWithInlineXML(s2);
		        System.out.println(content);
		        ArrayList<String> mentions = getMentions(content);
		        int currentPosition = 0;
		        ArrayList<Mention> mention_list = new ArrayList<Mention>();
		        //String context = "";
		        
		        for(int i = 0 ; i < mentions.size(); i++){
		        	String mention = mentions.get(i);
		        	Mention current_mention = new Mention();
		        	int start = s2.indexOf(mention);
		        	int end = start + mention.length();
		        	int globalStart = start + currentPosition;
		        	int globalEnd = end + currentPosition;
		        	//System.out.println(mention+": "+globalStart+" "+globalEnd);
		        	if(end >= s2.length())
		        		break;
		        	s2 = s2.substring(end);
		        	currentPosition = globalEnd;
		        	current_mention.setMention(mention, globalStart, globalEnd);
		        	
		        	int contextStart = i - 3;
		        	int contextEnd = i +3;
		        	
		        	if(contextStart < 0)
		        		contextStart = 0;
		        	if(contextEnd > mentions.size())
		        		contextEnd = mentions.size();
		        	
		        	for(int j = contextStart; j < contextEnd ;j++){
		        		if(i!=j)
		        		current_mention.addContext(mentions.get(j));
		        	}
		        	mention_list.add(current_mention);
		        	//System.out.println(currentPosition);
		        }
		        
		        
		        int queryId = 0;
		        String DocId = "DF-199-193696-586_5767";
		        for(Mention m:mention_list){
		        	String myQeuryIdString = getQueryId(queryId);
		        	System.out.println("EL14_ENG_"+myQeuryIdString+","+DocId+","+m);
		        	queryId++;
		        }
		        
		        //System.out.println(classifier.classifyToString(s2, "xml", true));
//		        int i=0;
//		        for (List<CoreLabel> lcl : classifier.classify(s2)) {
//		        	System.out.println(lcl);
//		          for (CoreLabel cl : lcl) {
//		            //System.out.println(i++ + ":");
//		            System.out.println(cl+" "+cl.category()+" "+cl.value());
//		          }
//		        }
//		      }
		      }
	   }
	   public static ArrayList<String> getMentions(String content){
		   //Pattern p = Pattern.compile("<PERSON></PERSON>");
		   ArrayList<String> mentions = new ArrayList<String>();
		   Matcher m = Pattern.compile("<PERSON>(.*?)</PERSON>|<LOCATION>(.*?)</LOCATION>|<ORGANIZATION>(.*?)</ORGANIZATION>")
		       .matcher(content);
		   while (m.find()) {
		     //allMatches.add(m.group());
			   if(m.group(1)!=null){
				   //System.out.println(m.group(1));
				   mentions.add(m.group(1));
			   }else if(m.group(2)!=null){
				   //System.out.println(m.group(2));
				   mentions.add(m.group(2));
			   }else if(m.group(3)!=null){
				   //System.out.println(m.group(3));
				   mentions.add(m.group(3));
			   }
		   }
		   
		   return mentions;
	   }
	   public static ArrayList<String> getDocId(String content){
		   //Pattern p = Pattern.compile("<PERSON></PERSON>");
		   ArrayList<String> mentions = new ArrayList<String>();
		   Matcher m = Pattern.compile("<PERSON>(.*?)</PERSON>|<LOCATION>(.*?)</LOCATION>|<ORGANIZATION>(.*?)</ORGANIZATION>")
		       .matcher(content);
		   while (m.find()) {
		     //allMatches.add(m.group());
			   if(m.group(1)!=null){
				   //System.out.println(m.group(1));
				   mentions.add(m.group(1));
			   }else if(m.group(2)!=null){
				   //System.out.println(m.group(2));
				   mentions.add(m.group(2));
			   }else if(m.group(3)!=null){
				   //System.out.println(m.group(3));
				   mentions.add(m.group(3));
			   }
		   }
		   
		   return mentions;
	   }
	   public static ArrayList<String> getDocIdList(String content){
		   //Pattern p = Pattern.compile("<PERSON></PERSON>");
		   ArrayList<String> mentions = new ArrayList<String>();
		   Matcher m = Pattern.compile("<docid>(.*?)</docid>")
		       .matcher(content);
		   while (m.find()) {
		     //allMatches.add(m.group());
			   if(m.group(1)!=null){
				   System.out.println(m.group(1));
				   //mentions.add(m.group(1));
			   }
		   }
		   return null;
	   }
		private static String getQueryId(int queryId){
			queryId++;
			if(queryId == 0)
				return queryPrefix.substring(1)+queryId;
			int start = (int)Math.log10(queryId);
			if(start < queryPrefix.length())
				return queryPrefix.substring(start)+queryId;
			return "";
		}
}
