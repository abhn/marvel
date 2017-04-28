package cultoftheunicorn.marvel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;


import android.util.Log;


public class Labels {

	String mPath;
	class label {
		public label(String s, int n) {
			thelabel=s;
			num=n;
		}

		int num;
		String thelabel;
	}
	//	HashMap<Integer,String> thelist=new HashMap<Integer,String>();

	ArrayList<label> thelist=new ArrayList<label>();

	public Labels(String Path)
	{
		mPath=Path;
	}

	public boolean isEmpty()
	{
		return !(thelist.size()>0);
	}

	public void add(String s,int n)
	{
		thelist.add( new label(s,n));
	}

	public String get(int i) {
		Iterator<label> Ilabel = thelist.iterator();
		while (Ilabel.hasNext()) {
			label l = Ilabel.next();
			if (l.num==i)
				return l.thelabel;
		}
		return "";
	}

	public int get(String s) {
		Iterator<label> Ilabel = thelist.iterator();
		while (Ilabel.hasNext()) {
			label l = Ilabel.next();
			if (l.thelabel.equalsIgnoreCase(s))
				return l.num;
		}
		return -1;
	}

	public void Save() {
		try {
			File f=new File (mPath+"faces.txt");
			f.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			Iterator<label> Ilabel = thelist.iterator();
			while (Ilabel.hasNext()) {
				label l = Ilabel.next();
				bw.write(l.thelabel+","+l.num);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("error",e.getMessage()+" "+e.getCause());
			e.printStackTrace();
		}
	}

	public void Read() {
		try {

			FileInputStream fstream = new FileInputStream(
					mPath+"faces.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			String strLine;
			thelist= new ArrayList<label>();
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				StringTokenizer tokens=new StringTokenizer(strLine,",");
				String s=tokens.nextToken();
				String sn=tokens.nextToken();

				thelist.add(new label(s,Integer.parseInt(sn)));
			}
			br.close();
			fstream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int max() {
		int m=0;
		Iterator<label> Ilabel = thelist.iterator();
		while (Ilabel.hasNext()) {
			label l = Ilabel.next();
			if (l.num>m) m=l.num;
		}
		return m;
	}

}
