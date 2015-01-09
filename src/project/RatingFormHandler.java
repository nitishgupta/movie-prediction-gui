package project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Servlet implementation class RatingFormHandler
 */
@WebServlet("/RatingFormHandler")
public class RatingFormHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	static double[][] ratings;
	static int[][] view;
	static int[][] pseudo;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RatingFormHandler() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void init(ServletConfig config) throws ServletException {
    	try {
			BufferedReader reader=new BufferedReader(new FileReader("C:/Users/divyanshu/Documents/AI/MoviePrediction/data/output.csv"));
			String line="";
			ratings=new double[1682][943];
			int row=0;
			while((line=reader.readLine())!=null)
			{
				String[] parts=line.split(",");
				for(int i=0;i<943&&row<1682;i++)
				{
					double score=Double.parseDouble(parts[i]);
					score=((int)(score*100))/100;
					ratings[row][i]=score;
				}
				row++;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	try{
    		BufferedReader reader = new BufferedReader(new FileReader("C:/Users/divyanshu/Documents/AI/MoviePrediction/data/view.csv"));
    		String line="";
    		view=new int[943][1682];
    		int row=0;
    		while((line=reader.readLine())!=null)
    		{
    			String[] parts=line.split(",");
    			for(int i=0;i<1682 && row<943;i++)
    			{
    				double viewCheck=Double.parseDouble(parts[i]);
    				view[row][i]=(int) viewCheck;
    			}
    			row++;
    		}
    		reader.close();
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	try{
    		BufferedReader reader = new BufferedReader(new FileReader("C:/Users/divyanshu/Documents/AI/MoviePrediction/data/pseudo.csv"));
    		String line="";
    		pseudo=new int[943][1682];
    		int row=0;
    		while((line=reader.readLine())!=null)
    		{
    			String[] parts = line.split(",");
    			for(int i=0;i<1682 && row<943;i++)
    			{
    				pseudo[row][i] = (int) Double.parseDouble(parts[i]);
    			}
    			row++;
    		}
    		reader.close();
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		PrintWriter out=response.getWriter();
		int[] neighbours = UserFormHandle.nearestNeighbours.clone();
		int movieId=MovieFormHandle.movieId;
		JSONObject js = new JSONObject();
		String viewResult="no";
		
		int numberOfViewers = checkView(neighbours,movieId, neighbours.length);
		if(numberOfViewers > neighbours.length/2)
		{
			viewResult="yes";
		}
		System.out.println(movieId+" "+neighbours[0]+" "+neighbours[1]+" "+neighbours[2]+" "+neighbours[3]+" "+neighbours[4]+" "+neighbours[5]);
		
		double rating;
		if(UserFormHandle.existingUser.equals("YES"))
		{
			System.out.println("Existing user " + UserFormHandle.userId);
			if(view[UserFormHandle.userId-1][movieId-1]==1)
			{
				rating = pseudo[UserFormHandle.userId-1][movieId-1];
				viewResult="yes";
				System.out.println("Movie already seen");
			}
			else
			{
				rating=ratings[movieId][UserFormHandle.userId];
			}
		}
		else
		{
			rating =ratings[movieId-1][neighbours[0]-1]+
					ratings[movieId-1][neighbours[1]-1]+
			        ratings[movieId-1][neighbours[2]-1]+
			        ratings[movieId-1][neighbours[3]-1]+
			        ratings[movieId-1][neighbours[4]-1]+
			        ratings[movieId-1][neighbours[5]-1]+
			        ratings[movieId-1][neighbours[6]-1]+
			        ratings[movieId-1][neighbours[7]-1]+
			        ratings[movieId-1][neighbours[8]-1]+
			        ratings[movieId-1][neighbours[9]-1];
			System.out.println(ratings[movieId-1][neighbours[0]-1]+" "+
					ratings[movieId-1][neighbours[1]-1]+" "+
			        ratings[movieId-1][neighbours[2]-1]+" "+
			        ratings[movieId-1][neighbours[3]-1]+" "+
			        ratings[movieId-1][neighbours[4]-1]+" "+
			        ratings[movieId-1][neighbours[5]-1]+" "+
			        ratings[movieId-1][neighbours[6]-1]+" "+
			        ratings[movieId-1][neighbours[7]-1]+" "+
			        ratings[movieId-1][neighbours[8]-1]+" "+
			        ratings[movieId-1][neighbours[9]-1]);
			rating=rating/10.0;
		}
		System.out.println(rating);
		rating=((int)(rating*100))/100.0;
		System.out.println("View Count="+numberOfViewers + " Ratings= "+rating);
		if(viewResult.equals("yes"))
		{
			try {
				js.put("view", "YES");
				js.put("rating", Double.toString(rating));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(rating>3)
		{
			try {
				js.put("view", "YES");
				js.put("rating", Double.toString(rating));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			try {
				js.put("view", "NO");
				js.put("rating", Double.toString(rating));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		out.print(js);
	}

	private int checkView(int[] neighbours, int movieId,int num) {
		// TODO Auto-generated method stub
		int count=0;
		for(int n=0;n<num;n++)
		{
			count+=view[neighbours[n]-1][movieId-1];
		}
		return count;
	}
}
