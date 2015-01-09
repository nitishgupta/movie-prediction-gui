package project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Servlet implementation class UserFormHandle
 */
@WebServlet("/UserFormHandle")
public class UserFormHandle extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	static int[] nearestNeighbours;
	static String existingUser;
	private static HashMap<Integer,String> userMap=null;
	static int userId;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserFormHandle() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		nearestNeighbours=new int[10];
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("C:/Users/divyanshu/Documents/AI/MoviePrediction/data/user.csv"));
			String line=reader.readLine();
			userMap = new HashMap<Integer,String>();
			
			while((line=reader.readLine())!=null)
			{
				//System.out.println(line);
				String values="";
				String parts[] = line.split(",");
				int key=Integer.parseInt(parts[0]);
				values=parts[1]+","+parts[2]+","+parts[3]+","+parts[4];
				userMap.put(new Integer(key), values);
			}
			reader.close();
			System.out.println("User hash map create");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("IN SERVELET");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		String age=request.getParameter("age");
		String sex=request.getParameter("sex");
		String profession=request.getParameter("profession");
		String zip=request.getParameter("zip");
		
		String values=age+","+sex+","+profession+","+zip;
		existingUser = userMap.containsValue(values) ? "YES" : "NO";
		if(existingUser.equals("YES"))
		{
			for(Entry<Integer,String> entry : userMap.entrySet())
			{
				if(values.equals(entry.getValue()))
				{
					userId = entry.getKey();
				}
			}
		}
		else
			userId=-1;
		
		try {
			getNearestNeighbours(Double.parseDouble(age),Double.parseDouble(zip),profession,sex);
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Existing "+existingUser);
		JSONObject js = new JSONObject();
		try {
			js.put("existing", existingUser);
			js.put("first", userMap.get(nearestNeighbours[0]));
			js.put("second", userMap.get(nearestNeighbours[1]));
			js.put("third", userMap.get(nearestNeighbours[2]));
			js.put("fourth", userMap.get(nearestNeighbours[3]));
			js.put("fifth", userMap.get(nearestNeighbours[4]));
			js.put("sixth", userMap.get(nearestNeighbours[5]));
			js.put("seventh", userMap.get(nearestNeighbours[6]));
			js.put("eighth", userMap.get(nearestNeighbours[7]));
			js.put("ninth", userMap.get(nearestNeighbours[8]));
			js.put("tenth", userMap.get(nearestNeighbours[9]));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.print(js);
	}

	public void getNearestNeighbours(double age, double zip, String prof, String sex) throws Exception
	{
		DataSource reader = new DataSource("C:/Users/divyanshu/Documents/AI/MoviePrediction/data/user1.csv");
		Instances users = reader.getDataSet();
		
		double ageMin = users.attributeStats(1).numericStats.min;
		double ageMax = users.attributeStats(1).numericStats.max;
		double ageRange = ageMax - ageMin;
		double zipMin = users.attributeStats(4).numericStats.min;
		double zipMax = users.attributeStats(4).numericStats.max;
		double zipRange = zipMax - zipMin;
		double [] distance=new double[users.numInstances()];
		int[] userIds=new int[users.numInstances()];
		double wz=1,wa=1,ws=1,wp=1;
		
		System.out.println(age+" "+sex+" "+prof+" "+zip);
		String s="",p="";
		for(int i=0;i<users.numInstances();i++)
		{
			userIds[i]=(int) users.instance(i).value(0);
		}
		for(int i=0; i<users.numInstances(); i++)
		{
			double distanceAge=0, distanceSex=0, distanceProf=0, distanceZip=0;
			
			distance[i] = 0;
			switch((int)users.instance(i).value(2))
			{
			case 0: s="M";break;
			case 1: s="F";break;
			}
			
			switch((int)users.instance(i).value(3))
			{
			case 0: p="administrator";break;
			case 1: p="artist";break;
			case 2: p="doctor";break;
			case 3: p="educator";break;
			case 4: p="engineer";break;
			case 5: p="entertainment";break;
			case 6: p="executive";break;
			case 7: p="healthcare";break;
			case 8: p="homemaker";break;
			case 9: p="lawyer";break;
			case 10: p="librarian";break;
			case 11: p="marketing";break;
			case 12: p="other";break;
			case 13: p="none";break;
			case 14: p="programmer";break;
			case 15: p="retired";break; 
			case 16: p="salesman";break;
			case 17: p="scientist";break;
			case 18: p="student";break;
			case 19: p="technician";break;
			case 20: p="writer";break;
			}
			
			if( !s.equalsIgnoreCase(sex))
				distanceSex = 1;
			if( !p.equalsIgnoreCase(prof))
				distanceProf = 1;
			
			distanceAge = Math.pow(( (users.instance(i).value(1) - ageMin)/ageRange -  (age - ageMin)/ageRange ), 2);
			distanceZip = Math.pow(( (users.instance(i).value(4) - zipMin)/zipRange -  (zip - zipMin)/zipRange ), 2);
			
			distance[i] = wa*distanceAge + ws*distanceSex + wp*distanceProf + wz*distanceZip;
		}
		for(int i=0;i<users.numInstances()-1;i++)
		{
			for(int j=0;j<users.numInstances()-i-1;j++)
			{
				if(distance[j]>distance[j+1])
				{
					double tmp=distance[j];
					distance[j]=distance[j+1];
					distance[j+1]=tmp;
					int tm=userIds[j];
					userIds[j]=userIds[j+1];
					userIds[j+1]=tm;
				}
			}
		}
		System.out.println(distance[0]+" "+distance[1]+" "+distance[2]+" "+distance[3]+" "+distance[4]+" "+distance[5]+" ");
		System.out.println(userIds[0]+" "+userIds[1]+" "+userIds[2]+" "+userIds[3]+" "+userIds[4]+" "+userIds[5]+" ");
		nearestNeighbours[0]=userIds[0];
		nearestNeighbours[1]=userIds[1];
		nearestNeighbours[2]=userIds[2];
		nearestNeighbours[3]=userIds[3];
		nearestNeighbours[4]=userIds[4];
		nearestNeighbours[5]=userIds[5];
		nearestNeighbours[6]=userIds[6];
		nearestNeighbours[7]=userIds[7];
		nearestNeighbours[8]=userIds[8];
		nearestNeighbours[9]=userIds[9];
	}
}
