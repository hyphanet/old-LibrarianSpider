import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import freenet.support.URLDecoder;
import freenet.support.URLEncodedFormatException;


public class LibrarianCrawler {
	
	private static String getLapTime(long starttime) {
		long time = System.currentTimeMillis() - starttime;
		time = time / 1000;
		String ret = (time/3600) + ":";
		time = time % 3600;
		ret += (((time/60) < 10)?"0":"") + (time/60) + ":";
		time = time % 60;
		ret += ((time < 10)?"0":"") + time + ":";
		return ret;
	}
	
	public static void main(String[] args) throws Exception {
		//URI u = new URI("http://localhost:8888/");
		long starttime = System.currentTimeMillis();
		HashMap whm = new HashMap();
		Vector uriOutList = new Vector();
		LinkedList uriInList = new LinkedList();
		int uriid = 0;
		String urlsstring;
		String muststartwith = "http://127.0.0.1:8888/";
		String muststartwith2 = "http://localhost:8888/";
		uriInList.add(new URIWrapper("http://localhost:8888/USK@BPZppy07RyID~NGihHgs4AAw3fUXxgtKIrwRu5rtpWE,k5yjkAFJC93JkydKl6vpY0Zy9D8ec1ymv2XP4Tx5Io0,AQABAAE/FreeHoo/5/"));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out.txt")));
		while (uriInList.peek() != null) {
			try {
				if (uriInList.size() < 5)
					Thread.sleep(800);
			} catch (Exception ess) { ;}
			System.err.println(getLapTime(starttime));
			System.err.println("uriInList-length: " + uriInList.size());
			System.err.println("uriOutList-length: " + uriOutList.size());
			
			urlsstring = "";
			URIWrapper currenturi = (URIWrapper)uriInList.poll();
			URI u = new URI(currenturi.uri);
			BufferedReader in;
			try {
				System.err.println("(" + currenturi.tries + ") " + u.toString());
				URL url = new URL(u.toString());
				URLConnection uc = url.openConnection();
				System.err.println(uc.getContentType() );
				System.err.println(uc.getContentLength() );
				//System.err.println(uc.getHeaderField() );
				if (!(uc.getContentType().startsWith("text/")))
					continue;
				
				in = new BufferedReader(
						new InputStreamReader(uc.getInputStream()));
			} catch (Exception e) {
				System.err.println("Exception: " + e);
				if (e instanceof NullPointerException)
					e.printStackTrace();
				//e.printStackTrace();
				currenturi.tries++;
				if (currenturi.tries < 30)
					uriInList.add(currenturi);
				try {
					if (uriInList.size() < 5)
						Thread.sleep(10000);
				} catch (Exception ess) { ;}
				continue;
			}
			
			
			String indata = "";
			String line;
			while ((line = in.readLine()) != null)
				indata += " " + line.trim();
			
			//System.out.println(indata);
			
			//System.out.println("--------------------------------------");
			
			int hrefpos = 0;
			String href = "";
			while ((hrefpos = indata.indexOf("href=\"", hrefpos + 1)) > 0) {
				href = indata.substring(hrefpos + 6, indata.indexOf("\"", hrefpos+7));
				if(href.charAt(0)=='\"') href="invalid";
				try{
				urlsstring += u.resolve(href).getPath();
				//if (!href.endsWith("htm") && !href.endsWith("html") && !href.endsWith("/") && !href.endsWith("txt")) {
				String h = href.toLowerCase();
				if (h.endsWith("pg") || h.endsWith("peg") || h.endsWith("gif") || h.endsWith("mp3") || h.endsWith("avi") || h.endsWith("css")) {
							;//System.err.println("not adding: " + href);
				} else  if ((u.resolve(href).toString().startsWith(muststartwith)) || (u.resolve(href).toString().startsWith(muststartwith2))){
					URIWrapper uw = new URIWrapper(u.resolve(href).toString());
					if (!uw.equals(currenturi))
							if (!uriOutList.contains(uw)) {
								uriInList.add(new URIWrapper(u.resolve(href).toString()));
								//System.err.print(".");
							}	
				} 
				}catch (Exception e){
				}
			}
			System.out.println(indata);
			//currenturi.descr = indata.replaceAll(".*<[tT][iI][tT][lL][eE]>.*</[tT][iI][tT][lL][eE]>", "\\1");
			//currenturi.descr = indata.replaceAll(".*<[tT][iI][tT][lL][eE]>", "").replaceAll("</[tT][iI][tT][lL][eE]>.*", "");
			if ((u.resolve(href).toString().startsWith(muststartwith)) || (u.resolve(href).toString().startsWith(muststartwith2))){
				indata = indata.toLowerCase();
		//		indata = indata.replaceAll(".*<body[^>]*>", "");
				indata = indata.replaceAll("</body[^>]*", "");
				indata = indata.replaceAll("<href=\".[^\"]*\"", "><");
				indata = indata.replaceAll("<[^>]*>", "");
				//System.out.println(indata);
				System.out.println("--------------------------------------");
				
				String outdata = "";
				char ch[] = new char[1];
				boolean whitespace = true;
				for (int i = 0 ; i < indata.length() ; i++) {
					ch[0] = indata.charAt(i);
					if (Character.isJavaIdentifierPart(ch[0])) {
						outdata += new String(ch);
						whitespace = false;
					} else {
						if (!whitespace) {
							outdata += " ";
							whitespace = true;
						}
					}
				}
				System.out.println(outdata);
				
				String words[] = outdata.split(" ");
				for (int i = 0 ; i < words.length ; i++) {
					if (!whm.containsKey(words[i]))
						whm.put(words[i], new HashMap());
					HashMap khm = (HashMap)whm.get(words[i]);
					if (!khm.containsKey(new Integer(uriid)))
						khm.put(new Integer(uriid), new Vector());
					Vector wv = (Vector)khm.get(new Integer(uriid));
					wv.add(new Integer(i));
				}
				
				words = urlsstring.toLowerCase().split("[^[a-zA-Z1-90]]");
				for (int i = 0 ; i < words.length ; i++) {
					if (!whm.containsKey(words[i]))
						whm.put(words[i], new HashMap());
					HashMap khm = (HashMap)whm.get(words[i]);
					if (!khm.containsKey(new Integer(uriid)))
						khm.put(new Integer(uriid), new Vector());
					Vector wv = (Vector)khm.get(new Integer(uriid));
					wv.add(new Integer(-1));
				}
				
				uriOutList.add(currenturi);
				uriid++;
			}
		}
		
		
		
		System.err.println("===============================================");
		System.err.println("===============================================");
		System.err.println("===============================================");
		System.err.println("===============================================");
		for (int i = 0 ; i < uriOutList.size() ; i++)
			bw.write(((URIWrapper)uriOutList.get(i)).toEntry() + "\n");
		
		
		Iterator itw = whm.keySet().iterator();
		while (itw.hasNext()) {
			String word = (String)itw.next();
			System.err.print("?"+word);
			bw.write("?"+word );
			HashMap khm = (HashMap)whm.get(word);
			Iterator itk = khm.keySet().iterator();
			while (itk.hasNext()) {
				Integer keyid = (Integer)itk.next();
				bw.write(" " + keyid);
				//System.err.print(" " + keyid);
				//Vector wv = (Vector)khm.get(keyid);
				//for (int i = 0 ; i < wv.size() ; i++)
				//System.err.print(((i == 0)?"=":",") + wv.get(i));
			}
			System.err.println();
			bw.write("\n");
		}
		
		bw.close();
		
		
		/*
		 u.resolve("/aaa");
		 System.err.println(u);
		 System.err.println(u.resolve("/aaa"));
		 */
		/*
		 BucketFactory bf = new ArrayBucketFactory();
		 SaferFilter sf = new SaferFilter("", bf);
		 Bucket bucket = new bf.makeBucket(uc.getContentLength());
		 */
		//sf.run(bucket, "ss")
		
		
		// freenet.client.http.filter.SaferFilter
	}
	
	private static class URIWrapper implements Comparable {
		public String uri;
		public int tries = 0;
		public String descr = null;
		
		public URIWrapper(String uri) {
			this.uri = uri;
		}
		
		public int compareTo(Object o) {
			if (!(o instanceof URIWrapper))
				return -1;
			
			return ((URIWrapper)o).mkShortURI().compareTo(mkShortURI());
		}
		
		public boolean equals(Object o) {
			if (!(o instanceof URIWrapper))
				return false;
			
			return ((URIWrapper)o).mkShortURI().equals(mkShortURI());
		}
		
		public String mkShortURI() {
			String suri;
			try {
				URI u = new URI(uri);
				suri = new URI("http://127.0.0.1",
						"", "", u.getPort(),
						u.getPath().replaceAll("^freenet:", ""), u.getQuery(),
				"").toString(); 
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return null;
			}
			if ((suri.indexOf("@") < 0) || (suri.indexOf("@") > 30)) {
				try {
					suri = URLDecoder.decode(suri);
				} catch (URLEncodedFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return suri;
		}
		
		public String toEntry() {
			URI u = null;
			try {
				u = new URI(uri);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				return "![ERROR]";
			}
			
			return ("!"+ u.getPath() + ((descr==null)?"":("\n+"+descr)));
		}
	}
	
}
